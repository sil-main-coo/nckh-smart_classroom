package vn.viethoang.truong.smartclass.View.Home.Fragment;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


import vn.viethoang.truong.smartclass.Check.CheckConnectInternet;
import vn.viethoang.truong.smartclass.R;
import vn.viethoang.truong.smartclass.SendMail.GMailSender;
import vn.viethoang.truong.smartclass.View.Alarm.ListAlarmDevice;

import static android.content.Context.MODE_PRIVATE;


public class ControlFirstClassFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private static FirebaseDatabase database = FirebaseDatabase.getInstance();
    private static DatabaseReference myRef;

    private static ImageView imgDen;
    private static ImageView imgQuat;
    private FloatingActionButton fabSendmail;
    private static TextView txtSoNguoi;
    public static TextView txtContentVoice;
    private static final String TAG= "SensorSTT";
    private static String preferencesName= "data";
    private static SharedPreferences preferences;
    private static Context thisContext;
    private ProgressDialog dialog;
    private LinearLayout lnAlarm;

    private static MediaPlayer mp;

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;

    public ControlFirstClassFragment() {

    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view=  inflater.inflate(R.layout.layout, container, false);
        imgDen= view.findViewById(R.id.btnLight);
        imgQuat= view.findViewById(R.id.btnFan);
        lnAlarm= view.findViewById(R.id.lnAlarm);
        txtContentVoice= view.findViewById(R.id.txtContentVoice);
        fabSendmail= view.findViewById(R.id.fabSendmail);
        txtSoNguoi= view.findViewById(R.id.txtSoNguoi);

        mp = MediaPlayer.create(getContext(), R.raw.button);

        thisContext= container.getContext();



        preferences = thisContext.getSharedPreferences("open", MODE_PRIVATE);

        if(CheckConnectInternet.haveNetworkConnection(getContext())) {
            // Đọc trạng thái thiết bị
            readDataFromFB("den");
            readDataFromFB("quat");
            readDataFromFB("songuoi");
        }else {
            Toast.makeText(getContext(), "Hãy kiểm tra lại kết nối internet !", Toast.LENGTH_LONG).show();
        }



        if(getDataInReferences("switch").equals("1")){
            txtContentVoice.setTextColor(Color.WHITE);
            txtContentVoice.setText(R.string.sayTut);

        }else{
            txtContentVoice.setTextColor(Color.parseColor("#757575"));
            txtContentVoice.setText("Bộ điều khiển đã tắt");
        }


        fabSendmail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                xuLySendEmail();
            }
        });

        imgDen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(CheckConnectInternet.haveNetworkConnection(getContext())) {
                    xuLyBatTatDen();
                }else{
                    Toast.makeText(getContext(), "Hãy kiểm tra lại kết nối internet !", Toast.LENGTH_LONG).show();
                }
            }
        });

        imgQuat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(CheckConnectInternet.haveNetworkConnection(getContext())) {
                    xuLyBatTatQuat();
                }else{
                    Toast.makeText(getContext(), "Hãy kiểm tra lại kết nối internet !", Toast.LENGTH_LONG).show();
                }
            }
        });

        lnAlarm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getActivity(), ListAlarmDevice.class));
            }
        });

        // Log.e("on Cr", "name "+ preferencesName);

        return view;
    }


    // Lấy dữ liệu thiết bị tắt mở trên references
    private static String getDataInReferences(String name){
        preferences= thisContext.getSharedPreferences(preferencesName,MODE_PRIVATE);
        String value= preferences.getString(name,"");
        return value;
    }

    private void xuLySendEmail() {
        AlertDialog.Builder builder= new AlertDialog.Builder(getContext());
        View view= getLayoutInflater().inflate(R.layout.sendmail, null);

        ImageView imgClose= view.findViewById(R.id.imgClose);
        Button btnSend= view.findViewById(R.id.btnSend);
        final EditText edMail= view.findViewById(R.id.edMail);
        builder.setView(view);
        final AlertDialog dialog= builder.create();

        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                send(edMail.getText().toString());
            }
        });
        imgClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.cancel();
            }
        });

        dialog.show();

    }

    private void send(String email) {
        String userName= "smarthome.kma@gmail.com";
        String pass= "jvmain99";
        String subject= "[Smart Classroom] - Phòng 103";
        String body="Học kỳ I năm học 2018 - 2019\n\n" +
                "Phòng 103:\n" +
                "Số người: "+txtSoNguoi.getText().toString()+
                "\nTrạng thái đèn: "+imgDen.getId()+
                "\nTrạng thái quạt: "+imgQuat.getId();
        String recipients= email;

        if(CheckConnectInternet.haveNetworkConnection(getContext())){
            if(checkMail(email)) {
                SendMailTask sendMailTask = new SendMailTask(userName, pass, subject, body, recipients);
                sendMailTask.execute();
            }else {
                Toast.makeText(getContext(), "Kiểm tra lại định dạng mail !", Toast.LENGTH_LONG).show();
            }
        }else{
            Toast.makeText(getContext(), "Hãy kiểm tra lại kết nối internet !", Toast.LENGTH_LONG).show();
        }

    }

    private boolean checkMail(String email) {
        int find1 = email.indexOf("@");
        int  find2 = email.indexOf('.');
        return (email.contains("@") && email.contains(".") && find2 > find1 ? true : false);
    }

    class SendMailTask extends AsyncTask<Void, Void, String>{

        private String userName, pass, subject, body, recipients;

        SendMailTask(String userName, String pass, String subject, String body, String recipients){
            this.userName= userName;
            this.body= body;
            this.subject= subject;
            this.pass= pass;
            this.recipients= recipients;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            dialog= new ProgressDialog(getContext());
            dialog.setMessage("Đang gửi thông số...");
            if(!dialog.isShowing())
                dialog.show();
        }

        @Override
        protected String doInBackground(Void... voids) {
            try {
                GMailSender sender = new GMailSender(userName, pass);
                sender.sendMail(subject,
                        body,
                        userName,
                        recipients);
            } catch (Exception e) {
                return "Error SendMail" +e.getMessage();
            }
            return "OK";
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            if(dialog.isShowing())
                dialog.cancel();
            if(s.equals("OK")) {
                AlertDialog.Builder builder= new android.support.v7.app.AlertDialog.Builder(getContext());
                builder.setMessage("Gửi thành công !\nVui lòng kiểm tra email");
                builder.setCancelable(false);
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                });
                AlertDialog alertDialog = builder.create();
                alertDialog.show();
            }else{
                AlertDialog.Builder builder= new android.support.v7.app.AlertDialog.Builder(getContext());
                builder.setMessage("Gửi thất bại !\nLỗi: "+s);
                builder.setCancelable(false);
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                });
                AlertDialog alertDialog = builder.create();
                alertDialog.show();
            }

        }
    }

    public void xuLyBatTatQuat() {
        mp.start();
        if(imgQuat.getId()==-1){
            writeToFirebaseDB("quat", 1);
            saveToPreferences("quat", "1");
        }
        if(imgQuat.getId()==0){
            writeToFirebaseDB("quat", 0);
            saveToPreferences("quat", "0");
        }
    }

    public void xuLyBatTatDen() {
        mp.start();
        if(imgDen.getId()==-1){
            writeToFirebaseDB("den", 1);
            saveToPreferences("den", "1");
        }
        if(imgDen.getId()==0){
            writeToFirebaseDB("den", 0);
            saveToPreferences("den", "0");
        }
    }


    public static void readDataFromFB(final String name){
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

                        if(valueInt== 1){
                            dataIsChangedToOn(imgDen);
                        }else{
                            dataIsChangedToOff(imgDen);
                        }
                        break;
                    case "quat":
                        valueInt = dataSnapshot.getValue(Integer.class);
                        Log.d(TAG, name+" is: " + valueInt);
                        saveToPreferences("quat", String.valueOf(valueInt));

                        if(valueInt== 1){
                            dataIsChangedToOn(imgQuat);
                        }else{
                            dataIsChangedToOff(imgQuat);
                        }
                        break;
                    case "songuoi":
                        valueInt = dataSnapshot.getValue(Integer.class);
                        Log.d(TAG, name+" is: " + valueInt);
                        saveToPreferences("songuoi", String.valueOf(valueInt));

                        showCountPersonToText(valueInt);
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


    public static void saveToPreferences(String name, String value) {
        preferences= thisContext.getSharedPreferences(preferencesName,MODE_PRIVATE);
        SharedPreferences.Editor editor= preferences.edit();
        editor.putString(name, value);
        //editor.putBoolean("SAVE",chkLuuDangNhap.isChecked());
        editor.apply();
        Log.e("RE", "ok");
    }


    private static void showCountPersonToText(long value) {
        txtSoNguoi.setText(value+"");
    }


    private static void dataIsChangedToOn(ImageView btn) {
        btn.setImageResource(R.drawable.on);
        btn.setId(0);
    }

    private static void dataIsChangedToOff(ImageView btn) {
        btn.setImageResource(R.drawable.off);
        btn.setId(-1);
    }

    private void writeToFirebaseDB(String reference, int value){
        // Write data to fbdb
        database = FirebaseDatabase.getInstance();
        myRef = database.getReference(reference);

        myRef.setValue(value);
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

    }

    @Override
    public void onPause() {
        super.onPause();
        Log.e("SENSOR","om Pause");
    }

    @Override
    public void onResume() {
        super.onResume();

    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
}
