package vn.viethoang.truong.smartclass.Adapter;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.Toast;


import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Calendar;
import java.util.List;

import vn.viethoang.truong.smartclass.R;
import vn.viethoang.truong.smartclass.View.Alarm.AlarmActivity;
import vn.viethoang.truong.smartclass.View.Alarm.AlarmAction;
import vn.viethoang.truong.smartclass.View.Alarm.Model.AlarmDevice;

import static android.content.Context.MODE_PRIVATE;
import static vn.viethoang.truong.smartclass.View.Home.Fragment.ControlFirstClassFragment.saveToPreferences;

public class RCVAlarmAdapter extends RecyclerView.Adapter<RCVAlarmAdapter.RecyclerViewHolder> {
    private List<AlarmDevice> data;
    private Context context;
    private int action; // Hành động bật / tắt

    private static FirebaseDatabase database = FirebaseDatabase.getInstance();
    private static DatabaseReference myRef;

    private static final String TAG= "RCVAdapter";


    public RCVAlarmAdapter(List<AlarmDevice> data, Context context, int action) {
        this.data = data;
        this.context= context;
        this.action= action;
    }

    @Override
    public RecyclerViewHolder onCreateViewHolder( ViewGroup viewGroup, int i) {
        LayoutInflater inflater = LayoutInflater.from(viewGroup.getContext());
        View view = inflater.inflate(R.layout.item_alarm_device, viewGroup, false);
        return new RecyclerViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final RecyclerViewHolder rvh, final int i) {
        final String nameDevice= data.get(i).getDeviceName();
        final String hour= data.get(i).getHour();
        final String minute= data.get(i).getMinute();
        final String date= data.get(i).getDate();
        final String month= data.get(i).getMonth();
        int i_monthCustom= Integer.parseInt(month)+1;
        final String year= data.get(i).getYear();
        final String dayOfWeek= data.get(i).getDayOfweek();
      //  String sRepeats= data.get(i).getsRepeats();

//        Log.e(TAG, sRepeats);

        int code = 0;
        final int sel= data.get(i).getSelect();

        if(action==0){
            code= i+50;
        }else if(action==1){
            code= i+100;
        }

        String name="";

        if(nameDevice.equalsIgnoreCase("đèn"))
            name= "den";
        else if(nameDevice.equalsIgnoreCase("quạt"))
            name= "quat";

        final String finalName1 = name;

        readDataFromFB(rvh, name);

        rvh.txtDeviceName.setText(nameDevice);
        rvh.txtTime.setText(hour+" : "+minute);

        if(dayOfWeek!=null){
            switch (Integer.valueOf(dayOfWeek)) {
                case 1:
                    rvh.txtDate.setText("Chủ nhật, " + date + "/" + i_monthCustom + "/" + year);
                    break;
                case 2:
                    rvh.txtDate.setText("Thứ 2, " + date + "/" + i_monthCustom + "/" + year);
                    break;
                case 3:
                    rvh.txtDate.setText("Thứ 3, " + date + "/" + i_monthCustom + "/" + year);
                    break;
                case 4:
                    rvh.txtDate.setText("Thứ 4, " + date + "/" + i_monthCustom + "/" + year);
                    break;
                case 5:
                    rvh.txtDate.setText("Thứ 5, " + date + "/" + i_monthCustom + "/" + year);
                    break;
                case 6:
                    rvh.txtDate.setText("Thứ 6, " + date + "/" + i_monthCustom + "/" + year);
                    break;
                case 7:
                    rvh.txtDate.setText("Thứ 7, " + date + "/" + i_monthCustom + "/" + year);
                    break;
            }
        }


        // Xét đã hẹn giờ rồi?
        if(sel==1) {
            if(!rvh.swSelect.isChecked())
                rvh.swSelect.setChecked(true);

            AlarmAction alarmAction =
                    new AlarmAction(context,
                            Integer.valueOf(hour), Integer.valueOf(minute),
                            Integer.valueOf(date), Integer.valueOf(month), Integer.valueOf(year)
                            , finalName1, action, code);
            alarmAction.xuLyHenGio();
            Log.e("Adapter", 1.1 + "");

        }
        else   {
            if(rvh.swSelect.isChecked()) {
                rvh.swSelect.setChecked(false);
                AlarmAction alarmAction = new AlarmAction(context, finalName1, action,code);
                alarmAction.cancelAlarm();
                Log.e("Adapter", 1.2 + "");
            }
        }


        // Xử lý chọn item
        final String finalName = name;
        rvh.layoutItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent= new Intent(context, AlarmActivity.class);
                // Truyền tên thiết bị sang activity hẹn giờ
                intent.putExtra("name", finalName);
                // Truyền vị trí / code intent
                intent.putExtra("pos", i);
                intent.putExtra("action", action);

                context.startActivity(intent);
            }
        });

        // Xử lý bật/tắt switch hẹn giờ
        final int finalCode = code;
        rvh.swSelect.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(rvh.swSelect.isChecked()){
                    int i_hour= Integer.parseInt(hour);
                    int i_minute= Integer.parseInt(minute);
                    int i_date= Integer.parseInt(date);
                    int i_month= Integer.parseInt(month);
                    int i_year= Integer.parseInt(year);

                    Calendar my = Calendar.getInstance();
                    my.set(i_year, i_month, i_date);
                    //Log.e(TAG, i_hour+" - " + i_minute+" - "+ i_date+" - "+ i_month+" - "+ i_year );
                    if(my.compareTo(Calendar.getInstance())>0) {

                        AlarmAction alarmAction =
                                new AlarmAction(context, i_hour, i_minute, i_date, i_month, i_year, finalName1, action, finalCode);
                        alarmAction.xuLyHenGio();
                        Toast.makeText(context,
                                "Bật hẹn giờ " +
                                        String.valueOf(hour + " : " + minute + ", " + date + "/" + month + "/" + year)
                                , Toast.LENGTH_LONG).show();
                        Log.e("Adapter", 2 + "");
                    }
                    else{
                        Toast.makeText(context,"Thời gian đã cũ. Vui lòng chọn thời gian mới" , Toast.LENGTH_LONG).show();
                        rvh.swSelect.setChecked(false);
                    }
                }else {
                    AlarmAction alarmAction = new AlarmAction(context, finalName1, action, finalCode);
                    alarmAction.cancelAlarm();
                    Log.e("Adapter", 2+"");
                }
            }
        });
    }



    @Override
    public int getItemCount() {
        return data.size();
    }

    public class RecyclerViewHolder extends RecyclerView.ViewHolder {
        TextView txtDeviceName, txtTime, txtSttDevice, txtDate;
        Switch swSelect;
        TableLayout layoutItem;
        public RecyclerViewHolder(View itemView) {
            super(itemView);
            txtDeviceName = itemView.findViewById(R.id.txtNameDevice);
            txtTime = itemView.findViewById(R.id.txtTimeSave);
            txtDate= itemView.findViewById(R.id.txtDate);
            txtSttDevice= itemView.findViewById(R.id.txtSttDevice);
            swSelect = itemView.findViewById(R.id.swSelect);
            layoutItem= itemView.findViewById(R.id.layoutItem);
        }
    }


    public static void readDataFromFB(final RecyclerViewHolder rvh, final String name){
        // Read from the database
        myRef = database.getReference(name);
        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                int valueInt;

                switch (name){
                    case "den":
                        valueInt = dataSnapshot.getValue(Integer.class);
                        Log.d(TAG, name+" is: " + valueInt);
                        saveToPreferences("den", String.valueOf(valueInt));

                        if(valueInt==1){
                            // đèn bật
                            rvh.txtSttDevice.setTextColor(Color.GREEN);
                            rvh.txtSttDevice.setText("đang bật");

                        }else if(valueInt==0){
                            // Đèn tắt
                            rvh.txtSttDevice.setTextColor(Color.GRAY);
                            rvh.txtSttDevice.setText("đang tắt");
                        }

                        break;
                    case "quat":
                        valueInt = dataSnapshot.getValue(Integer.class);
                        Log.d(TAG, name+" is: " + valueInt);
                        saveToPreferences("quat", String.valueOf(valueInt));

                        if(valueInt==1
                                ){
                            // quat bật
                            rvh.txtSttDevice.setTextColor(Color.GREEN);
                            rvh.txtSttDevice.setText("đang bật");

                        }else if(valueInt==0){
                            // quat tắt
                            rvh.txtSttDevice.setTextColor(Color.GRAY);
                            rvh.txtSttDevice.setText("đang tắt");
                        }
                        break;

                    default: Log.e(TAG, "Xem lại thông số truyền vào");

                }


            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.w(TAG, "Failed to read value.", error.toException());
            }
        });

    }

}
