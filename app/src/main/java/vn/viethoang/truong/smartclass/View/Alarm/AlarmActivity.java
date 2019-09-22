package vn.viethoang.truong.smartclass.View.Alarm;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.format.Time;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import vn.viethoang.truong.smartclass.Check.CheckFirstRun;
import vn.viethoang.truong.smartclass.R;


public class AlarmActivity extends AppCompatActivity {

    private static FirebaseDatabase database = FirebaseDatabase.getInstance();  // firebase
    private static DatabaseReference myRef; // firebase

    private static final String TAG= "AlarmActivity";
    private static String preferencesName= "alarm";
    private static SharedPreferences preferences;

    Button btnExit, btnSave, btnCalendar;
    TextView txtDoW, txtDate, txtMonth, txtYear;
    TextView txtStringNow, txtTittle;
    TextView txtMon, txtTue, txtWed, txtThurs, txtFri, txtSat, txtSun;

    final String[] hours= new String[24];  // mảng chứa giờ
    final String[] minutes= new String[60];  // mảng chứa phút
    NumberPicker pickerHour;
    NumberPicker pickerMinute;

    Calendar c ;  // Khai báo lịch

    int hour ;  // biến chứa thời gian (tiếng) hiện tại
    int minute;  // biến chứa thời gian (phút) hiện tại
    int date; // biến chứa thời gian (phút) hiện tại
    int month, year, dayOfweek;

    String string_gio, string_phut;
    String string_d, string_m_default, string_m_custom, string_y, string_dow;  // biến chứa thời gian (day month year thứ) hẹn giờ

    HashMap<String, String> hmDevice;  // hash map lưu trạng thái hẹn giờ thiết bị
    StringBuilder sb = new StringBuilder(); // sb lưu các thứ lặp lại
    String[] playlists;

    static int sttDevice;
    int position;  // code dùng để hẹn giờ trong intent
    String nameDevice;
    int action; // hành động bật tắt

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alarm_layout);

        // Lấy dữ liệu của thiết bị cần hẹn giờ
        Intent iData= getIntent();
        nameDevice= iData.getStringExtra("name");  // tên thiết bị
        position= iData.getIntExtra("pos", -1);  // vị trí? -> để tạo code hẹn giờ
        action= iData.getIntExtra("action", -1);  // hành động bật tắt


        c = Calendar.getInstance();  // Trả về giờ phút hiện tại
        // biến int lưu time hiện tại
        hour= c.get(Calendar.HOUR_OF_DAY);  // Lấy giá trị giờ
        minute = c.get(Calendar.MINUTE);  // Lấy giá trị phút
        month = c.get(Calendar.MONTH);  // Lấy giá trị phút
        year = c.get(Calendar.YEAR);  // Lấy giá trị phút
        date = c.get(Calendar.DAY_OF_MONTH);  // Lấy giá trị phút
        dayOfweek = c.get(Calendar.DAY_OF_WEEK);  // Lấy giá trị phút

        // Chuỗi chứa thời gian đang chọn trên UI
        string_d= chuanHoaSo(date);
        string_m_default= String.valueOf(month);
        string_m_custom= chuanHoaSo(month+1); // Vì tháng trả về bắt đầu từ 0 nên ta tăng 1 để hiển thị
        string_y= chuanHoaSo(year);
        string_dow= String.valueOf(dayOfweek);  //

        createPickerTime();  // Tạo mảng picker giờ : phút
        addControls();
        addEvents();

    }

    // Đọc dữ liệu của thiết bị đang hẹn giờ
    private static void readDataFromFB(final String nameDevice) {
        // Read from the database
        myRef = database.getReference(nameDevice);
        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                int valueInt;

                switch (nameDevice){
                    case "den":
                        valueInt = dataSnapshot.getValue(Integer.class);
                        Log.d(TAG, nameDevice+" is: " + valueInt);

                        if(valueInt==1){
                            // đèn bật
                            sttDevice= 1;
                        }else {
                            // Đèn tắt
                            sttDevice= 0;
                        }
                        break;
                    case "quat":
                        valueInt = dataSnapshot.getValue(Integer.class);
                        Log.d(TAG, nameDevice+" is: " + valueInt);

                        if(valueInt== 1){
                            // quạt bật
                            sttDevice= 1;
                        }else{
                            // quạt tắt
                            sttDevice= 0;
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


    private void addControls() {

        hmDevice= new HashMap<>(); // hm lưu 2 thiết bị đèn và quạt
        hmDevice.put("light", null);
        hmDevice.put("fan", null);

        btnExit= findViewById(R.id.btnExit);
        btnSave= findViewById(R.id.btnSave);
        btnCalendar= findViewById(R.id.btnCalendar);
        txtDoW= findViewById(R.id.txtDOW);
        txtDate= findViewById(R.id.txtDOM);
        txtMonth= findViewById(R.id.txtMonth);
        txtYear= findViewById(R.id.txtYear);
        txtStringNow= findViewById(R.id.txtNowtus);
        txtTittle= findViewById(R.id.txtTittle);

        // Setup hiển thị lặp lại
        setupShowRepeat();

        setUItxt(string_dow, string_d,string_m_custom,string_y); // set text hiển thị ngày hiện tại
        if(action==0)
            txtTittle.setText("HẸN GIỜ TẮT");
        else if(action==1)
            txtTittle.setText("HẸN GIỜ BẬT");

        // Setup picker giờ phút
        pickerHour =  findViewById(R.id.pickerHour);  // picker Giờ
        pickerMinute  = findViewById(R.id.pickerMinute);    // picker phút

        pickerHour.setMaxValue(23);
        pickerHour.setMinValue(00);
        pickerHour.setDisplayedValues(hours);
        pickerHour.setWrapSelectorWheel(true);
        pickerHour.setValue(hour);
        pickerHour.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);

        pickerMinute.setMaxValue(59);
        pickerMinute.setMinValue(00);
        pickerMinute.setDisplayedValues(minutes);
        pickerMinute.setWrapSelectorWheel(true);
        pickerMinute.setValue(minute);
        pickerMinute.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);

        pickerHour.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker picker, int oldVal, int newVal){
                comparingTime(); // Hàm 0
            }
        });

        pickerMinute.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                comparingTime();
            }
        });

    }

    private void setupShowRepeat() {
        txtMon= findViewById(R.id.txtMon);
        txtTue= findViewById(R.id.txtTue);
        txtWed= findViewById(R.id.txtWed);
        txtThurs= findViewById(R.id.txtThurs);
        txtFri= findViewById(R.id.txtFri);
        txtSat= findViewById(R.id.txtSat);
        txtSun= findViewById(R.id.txtSunday);
        Log.e(TAG, "repeat");


        Log.e(TAG, "lan dau");
        setDefaultKey(txtMon);
        setDefaultKey(txtTue);
        setDefaultKey(txtWed);
        setDefaultKey(txtThurs);
        setDefaultKey(txtFri);
        setDefaultKey(txtSat);
        setDefaultKey(txtSun);

        String repeat = getDataInReferences(nameDevice+action+"Repeat");
        Log.e(TAG+"StringRE", repeat);

        if(!repeat.isEmpty()){
            String day;
            playlists = repeat.split(",");
            for (int i = 0; i < playlists.length; i++) {
                sb.append(playlists[i]).append(",");
                day= playlists[i];
                if (day.equals("2")) {
                    setBackgroundDaySelectedRepeat(txtMon);
                    continue;
                }
                if (day.equals("3")) {
                    setBackgroundDaySelectedRepeat(txtTue);
                    continue;
                }
                if (day.equals("4")) {
                    setBackgroundDaySelectedRepeat(txtWed);
                    continue;
                }
                if (day.equals("5")) {
                    setBackgroundDaySelectedRepeat(txtThurs);
                    continue;
                }
                if (day.equals("6")) {
                    setBackgroundDaySelectedRepeat(txtFri);
                    continue;
                }
                if (day.equals("7")) {
                    setBackgroundDaySelectedRepeat(txtSat);
                    continue;
                }
                if (day.equals("C")) {
                    setBackgroundDaySelectedRepeat(txtSun);
                    continue;
                }
            }
        }

        setEventTextRepeat(txtMon);
        setEventTextRepeat(txtTue);
        setEventTextRepeat(txtWed);
        setEventTextRepeat(txtThurs);
        setEventTextRepeat(txtFri);
        setEventTextRepeat(txtSat);
        setEventTextRepeat(txtSun);

    }

    private void setEventTextRepeat(final TextView txtName){
        txtName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(txtName.getId()==0)
                    setEventDaySelectedRepeat(txtName);
                else
                    setBackgroundDayNoSelectRepeat(txtName);
            }
        });
    }

    private void setDefaultKey(TextView txtName) {
        txtName.setId(0);
    }

    @SuppressLint("ResourceType")
    private void setBackgroundDaySelectedRepeat(TextView txtName) {
        txtName.setBackgroundResource(R.drawable.bg_day_selected);
        txtName.setId(1);
    }

    @SuppressLint("ResourceType")
    private void setEventDaySelectedRepeat(TextView txtName) {
        setBackgroundDaySelectedRepeat(txtName);
        //Log.e(TAG, txtName.getText().toString().trim());
        sb.append(txtName.getText().toString().trim()).append(",");
        //Log.e(TAG, sb.toString());
    }

    @SuppressLint("ResourceType")
    private void setBackgroundDayNoSelectRepeat(TextView txtName) {
        txtName.setBackgroundResource(R.drawable.bg_day_no_select);
        //Log.e(TAG, txtName.getText().toString().trim());
        int start= sb.indexOf(txtName.getText().toString());
        sb.delete(start, start+2);
       // Log.e(TAG, sb.toString());
        txtName.setId(0);
    }

    private void comparingTime(){
        if(pickerHour.getValue()<hour
                || pickerHour.getValue()==hour && pickerMinute.getValue()<minute){
            Calendar cal = Calendar.getInstance();

            cal.add(Calendar.DAY_OF_YEAR, 1);
            string_d  = String.valueOf(cal.get(Calendar.DAY_OF_MONTH));
            string_m_default = String.valueOf(cal.get(Calendar.MONTH));
            string_m_custom= chuanHoaSo(Integer.valueOf(string_m_default)+1);
            string_dow= String.valueOf(cal.get(Calendar.DAY_OF_WEEK));
            string_y= String.valueOf(cal.get(Calendar.YEAR));

            setUItxt(string_dow,string_d, string_m_custom, string_y);
            txtStringNow.setText("");
        }

        if(pickerHour.getValue()>=hour && pickerMinute.getValue()>=minute){
            Calendar cal = Calendar.getInstance();

            string_d  = String.valueOf(cal.get(Calendar.DAY_OF_MONTH));
            string_m_default = String.valueOf(cal.get(Calendar.MONTH));
            string_dow= String.valueOf(cal.get(Calendar.DAY_OF_WEEK));
            string_y= String.valueOf(cal.get(Calendar.YEAR));

            setUItxt(string_dow,string_d, string_m_custom, string_y);
            txtStringNow.setText("Hôm nay - ");

        }

    }

    private void addEvents() {

        // xử lý thoát
        btnExit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        // xử lý hẹn giờ
        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                string_gio= chuanHoaSo(pickerHour.getValue());
                string_phut= chuanHoaSo(pickerMinute.getValue());
                string_d= chuanHoaSo(Integer.parseInt(string_d));

                string_m_custom= chuanHoaSo(Integer.parseInt(string_m_default)+1);

                saveToPreferences(nameDevice+action+"Hour", string_gio);
                saveToPreferences(nameDevice+action+"Minute", string_phut);
                saveToPreferences(nameDevice+action+"Date", string_d);
                saveToPreferences(nameDevice+action+"Month", string_m_default);
                saveToPreferences(nameDevice+action+"DayOfWeek", string_dow);
                saveToPreferences(nameDevice+action+"Year", string_y);
                saveToPreferences(nameDevice+action+"Selected", 1+"");
                saveToPreferences(nameDevice+action+"Repeat", sb.toString());
                Toast.makeText(AlarmActivity.this,
                        "Bật hẹn giờ "+
                                string_gio+" : "+string_phut+", "+string_d+"/"+string_m_custom+"/"+string_y
                        , Toast.LENGTH_LONG).show();
                finish();
            }
        });


        // Xử lý show lịch
        btnCalendar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               showCalendar();
            }
        });


    }

    private void showCalendar() {
        AlertDialog.Builder builder= new AlertDialog.Builder(AlarmActivity.this);
        View view= getLayoutInflater().inflate(R.layout.calendar_dialog, null);
        Button btnCalExit= view.findViewById(R.id.btnCalExit);
        Button btnCalSel= view.findViewById(R.id.btnCalSel);
        final CalendarView calendarView= view.findViewById(R.id.calendar);


        Time myTimeMin1 = new Time();
        myTimeMin1.set(date, month, year);
        calendarView.setMinDate(myTimeMin1.toMillis(true));

        Time myTimeMin2 = new Time();
        myTimeMin2.set(Integer.valueOf(string_d), Integer.valueOf(string_m_default), Integer.valueOf(string_y));
        calendarView.setDate(myTimeMin2.toMillis(true));



        builder.setView(view);
        final AlertDialog dialog= builder.create();

        // Thiết lập vị trí alert trên màn hình
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        WindowManager.LayoutParams wmlp = dialog.getWindow().getAttributes();
        wmlp.gravity = Gravity.BOTTOM | Gravity.CENTER ;

        dialog.show();

        btnCalExit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.cancel();
            }
        });

        // Lấy thời gian khi thay đổi chọn lịch
        calendarView.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {
            @Override
            public void onSelectedDayChange(CalendarView view, int year, int month, int dayOfMonth) {
                string_d= chuanHoaSo(dayOfMonth);
                string_m_default= String.valueOf(month);
                string_m_custom= chuanHoaSo(month+1);
                string_y= chuanHoaSo(year);

                Calendar calendar = Calendar.getInstance();
                calendar.set(year, month, dayOfMonth);
                string_dow = chuanHoaSo(calendar.get(Calendar.DAY_OF_WEEK));

            }
        });

        btnCalSel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setUItxt(string_dow, string_d, string_m_custom, string_y);

                Calendar my = Calendar.getInstance();
                my.set(Integer.valueOf(string_y), Integer.valueOf(string_m_default), Integer.valueOf(string_d));

                if(my.compareTo(Calendar.getInstance())==0) {
                    txtStringNow.setText("Hôm nay - ");
                }else {
                    txtStringNow.setText("");
                }

//                Toast.makeText(AlarmActivity.this,
//                        "Date:  "+string_dow+"/"+string_d+"/"+string_m_custom+"/"+string_y, Toast.LENGTH_LONG).show();
                dialog.cancel();
            }
        });
    }

    private void setUItxt(String string_dow, String string_d, String string_m_custom, String string_y) {
        switch (Integer.valueOf(string_dow)){
            case 1:
                txtDoW.setText("Chủ nhật, ");
                break;
            case 2:
                txtDoW.setText("Thứ 2, ");
                break;
            case 3:
                txtDoW.setText("Thứ 3, ");
                break;
            case 4:
                txtDoW.setText("Thứ 4, ");
                break;
            case 5:
                txtDoW.setText("Thứ 5, ");
                break;
            case 6:
                txtDoW.setText("Thứ 6, ");
                break;
            case 7:
                txtDoW.setText("Thứ 7, ");
                break;
        }
        txtDate.setText(string_d+"/");
        txtMonth.setText(string_m_custom+"/");
        txtYear.setText(string_y);
    }

    private String chuanHoaSo(int i){
        if(i <=9)
            return "0".concat(String.valueOf(i));
        return String.valueOf(i);
    }


    // Lấy dữ liệu thiết bị tắt mở trên references
    private String getDataInReferences(String name){
        preferences= getSharedPreferences(preferencesName,MODE_PRIVATE);
        String value= preferences.getString(name,"");
        return value;
    }


    private void saveToPreferences(String name, String value) {
        preferences= getSharedPreferences(preferencesName,MODE_PRIVATE);
        SharedPreferences.Editor editor= preferences.edit();
        editor.putString(name, value);
        editor.apply();
        Log.e("RE_AlarmActivity", "ok");
    }


    private void createPickerTime() {
        for(int i=0; i<=23; i++){
            if(i<=9)
                hours[i]= "0"+i;
            else
                hours[i]= String.valueOf(i);
        }
        for(int i=0; i<=59; i++){
            if(i<=9)
                minutes[i]= "0"+i;
            else
                minutes[i]= String.valueOf(i);
        }
    }

}
