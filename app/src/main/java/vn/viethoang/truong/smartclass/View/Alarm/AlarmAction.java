package vn.viethoang.truong.smartclass.View.Alarm;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import android.widget.Toast;

import java.util.Calendar;

import vn.viethoang.truong.smartclass.Receiver.AlarmReceiver;
import vn.viethoang.truong.smartclass.View.Alarm.AlarmActivity;
import vn.viethoang.truong.smartclass.View.Alarm.Fragment.AlarmTurnOnFragment;

import static android.content.Context.MODE_PRIVATE;

public class AlarmAction {
    Calendar c = Calendar.getInstance();  // Trả về giờ phút hiện tại ;  // Khai báo lịch
    AlarmManager alarmManager;
    PendingIntent pi;  // pendingIntent dùng để gửi request gọi thông báo

    int hour, minute, date, month, year;
    String nameDevice;
    int action;
    int code;

    private static String preferencesName= "alarm";
    private static SharedPreferences preferences;

    private Context context;


    // Action hẹn giờ
    public AlarmAction(Context context, int hour, int minute, int date, int month, int year, String nameDevice, int action, int code){
        this.context= context;
        this.hour= hour;
        this.minute= minute;
        this.nameDevice= nameDevice;
        this.code= code;
        this.action= action;
        this.date= date;
        this.month= month;
        this.year= year;

        Intent intentAlarm = new Intent(context, AlarmReceiver.class); // Intent truyền sang Broadcast
        System.out.println("calling Alarm receiver ");

        // truyền thông số bật/tắt sang broadcast -> service
        intentAlarm.putExtra("name", nameDevice); //
        intentAlarm.putExtra("code", code); //
        intentAlarm.putExtra("action", action); // hành động bật tắt 0 1
        alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        pi = PendingIntent.getBroadcast(context, code, intentAlarm, PendingIntent.FLAG_UPDATE_CURRENT);

    }

    // Action hủy hẹn giờ
    public AlarmAction(Context context, String nameDevice, int action, int code){
        this.context= context;
        this.code= code;
        this.nameDevice= nameDevice;
        this.action= action;

        Intent intentAlarm = new Intent(context, AlarmReceiver.class); // Intent truyền sang Broadcast
        alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        pi = PendingIntent.getBroadcast(context, code, intentAlarm, PendingIntent.FLAG_UPDATE_CURRENT);
    }


    public void xuLyHenGio() {
        c.set(Calendar.HOUR_OF_DAY, hour);
        c.set(Calendar.MINUTE, minute);
        c.set(Calendar.DATE, date);
        c.set(Calendar.MONTH, month);
        c.set(Calendar.YEAR, year);
        //Toast.makeText(AlarmActivity.this, "Xu ly hen gio", Toast.LENGTH_LONG).show();

        int currentapiVersion = android.os.Build.VERSION.SDK_INT;

        if (currentapiVersion < android.os.Build.VERSION_CODES.KITKAT){
            alarmManager.set(AlarmManager.RTC_WAKEUP,  c.getTimeInMillis(), pi);
        } else {
            if (currentapiVersion < android.os.Build.VERSION_CODES.M) {
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, c.getTimeInMillis(), pi);
            } else {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, c.getTimeInMillis(), pi);
            }
        }

        saveToPreferences(nameDevice+action+"Selected", 1+"");

    }

    public void cancelAlarm(){
        alarmManager.cancel(pi);
        saveToPreferences(nameDevice+action+"Selected", 0+"");
        Log.e("AlarmAction", "cancel");
    }

    private void saveToPreferences(String name, String value) {
        preferences= context.getSharedPreferences(preferencesName,MODE_PRIVATE);
        SharedPreferences.Editor editor= preferences.edit();
        editor.putString(name, value);
        editor.apply();
        Log.e("RE_AlarmAction", "ok");
    }



}
