package vn.viethoang.truong.smartclass.Receiver;


import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import vn.viethoang.truong.smartclass.R;
import vn.viethoang.truong.smartclass.View.Alarm.AlarmActivity;
import vn.viethoang.truong.smartclass.View.Alarm.Fragment.AlarmTurnOffFragment;
import vn.viethoang.truong.smartclass.View.Alarm.Fragment.AlarmTurnOnFragment;
import vn.viethoang.truong.smartclass.View.Home.HomeActivity;

import static android.content.Context.MODE_PRIVATE;
import static android.content.Context.NOTIFICATION_SERVICE;


public class AlarmReceiver extends BroadcastReceiver {
    private static FirebaseDatabase database = FirebaseDatabase.getInstance();
    private static DatabaseReference myRef;

    private static String preferencesName= "alarm";
    private static SharedPreferences preferences;
    private static final String TAG= "AlarmActivity";

    public AlarmReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String nameDevice="";
        String nameUTF8 = null, stringAction = null;
        int action, code;
        Intent resultIntent = null;

        nameDevice= intent.getStringExtra("name");
        action= intent.getIntExtra("action", -1);
        code= intent.getIntExtra("code", -1);

        if(nameDevice.equals("den"))
            nameUTF8= "đèn";
        else if(nameDevice.equals("quat"))
            nameUTF8= "quạt";
        if(action==0)
            stringAction= "tắt";
        else if(action==1)
            stringAction= "bật";

        writeToFirebaseDB(nameDevice, action);
        saveToPreferences(context,nameDevice+action+"Selected", 0+"");

        long[] v = {500, 3000};     // Đặt thời gian rung của thiết bị

        String CHANNEL_ID = "my_channel_01";
        CharSequence name = "my_channel";
        String Description = "This is my channel";
        final NotificationManager mgr = (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);


        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {


            int importance = NotificationManager.IMPORTANCE_LOW;
            NotificationChannel notificationChannel = new NotificationChannel(CHANNEL_ID, name, importance);
            notificationChannel.enableLights(true);
            notificationChannel.setLightColor(Color.RED);
            notificationChannel.setDescription(Description);
            notificationChannel.enableVibration(true);
            notificationChannel.setVibrationPattern(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400});
            mgr.createNotificationChannel(notificationChannel);
        }

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context, CHANNEL_ID);

        mBuilder.setSmallIcon(R.mipmap.ic_launcher) // notification icon
                .setContentTitle("Thông báo hẹn giờ")
                .setContentText("Đã "+stringAction+" "+nameUTF8)
                .setVibrate(v)                               // Thiết lập rung thiết bị
                .setTicker("Đã tới giờ !")
                .setAutoCancel(true); // clear notification after click

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            try {
                Uri notification = Uri.parse("android.resource://" + context.getPackageName() + "/" + R.raw.f);
                Ringtone r = RingtoneManager.getRingtone(context, notification);
                r.play();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }else {
            Uri alarmSound = Uri.parse("android.resource://" + context.getPackageName() + "/" + R.raw.f);
            mBuilder.setSound(alarmSound);
        }

        resultIntent = new Intent(context, HomeActivity.class);
        PendingIntent resultPendingIntent =
                PendingIntent.getActivity(context, code, resultIntent,PendingIntent.FLAG_UPDATE_CURRENT);

        mBuilder.setContentIntent(resultPendingIntent);
        NotificationManager mNotificationManager =
                (NotificationManager)context.getSystemService(NOTIFICATION_SERVICE);
        mNotificationManager.notify(code, mBuilder.build());
    }

    // Hàm gửi data lên firebase
    private void writeToFirebaseDB(String reference, int value){
        // Write data to fbdb
        database = FirebaseDatabase.getInstance();
        myRef = database.getReference(reference);

        myRef.setValue(value);
    }

    private void saveToPreferences(Context context, String name, String value) {
        preferences= context.getSharedPreferences(preferencesName,MODE_PRIVATE);
        SharedPreferences.Editor editor= preferences.edit();
        editor.putString(name, value);
        editor.apply();
        Log.e("RE_AlarmAction", "ok");
    }



}