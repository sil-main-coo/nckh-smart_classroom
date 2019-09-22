package vn.viethoang.truong.smartclass.View.Home;

import android.Manifest;
import android.app.AlarmManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Build;
import android.speech.tts.TextToSpeech;
import android.support.annotation.NonNull;
import android.support.design.widget.BaseTransientBottomBar;
import android.support.design.widget.NavigationView;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.security.AccessControlContext;
import java.util.Locale;

import edu.cmu.pocketsphinx.Assets;
import edu.cmu.pocketsphinx.Hypothesis;
import edu.cmu.pocketsphinx.SpeechRecognizerSetup;
import vn.viethoang.truong.smartclass.Check.CheckConnectInternet;
import vn.viethoang.truong.smartclass.R;
import vn.viethoang.truong.smartclass.View.Home.Activity.AboutAppActivity;
import vn.viethoang.truong.smartclass.View.Home.Activity.TutActivity;
import vn.viethoang.truong.smartclass.View.Home.Fragment.ControlFirstClassFragment;

import static java.security.AccessController.getContext;

public class HomeActivity extends AppCompatActivity implements edu.cmu.pocketsphinx.RecognitionListener {
    private DrawerLayout drawerLayout;  // Drawer menu
    private NavigationView nv;   // NavigationView hiển thị drawerview
    private ActionBarDrawerToggle toggle;  // drawer button
    private Toolbar toolbar;   // toolbar
    private ViewPager pager;  // pager show content tablayout:  rooms...
    private TabLayout tabLayout;  //  tablayout
    private PagerAdapter pagerAdapter;   // pager tab layout
    private Switch swVoice;   // switch on/off voice control
    private MediaPlayer mediaPlayer;  // play ring

    private static SharedPreferences preferences;   // save data: data devices, status, v.v.v
    private static String preferencesName= "data";  // name sharedPreferences
    private static final String TAG= "HOME";

    private static FirebaseDatabase database = FirebaseDatabase.getInstance();  // get firebase/ interactive(tương tác) to firebase
    private static DatabaseReference myRef;  // firebase data,

    private static TextToSpeech mytts;  // speech text.
    private edu.cmu.pocketsphinx.SpeechRecognizer recognizer;  // library listen voice

    /* Named searches allow to quickly reconfigure the decoder */
    private static final String KWS_SEARCH = "wakeup";  // key chọn đánh thức thiết bị
    private static final String MENU_SEARCH = "menu";   // key chọn thiết bị thực hiện theo menu

    /* Keyword we are looking for to activate menu */
    private static final String KEYPHRASE = "ok my phone";  // từ khởi đầu để đánh thức thiết bị

    /* Used to handle permission request */
    private static final int PERMISSIONS_REQUEST_RECORD_AUDIO = 1;  // mã code xin quyền record audio
    private static final int PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE= 2;  //mã xin quyền truy cập bộ nhớ

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // Check if user has given permission to record audio
        int permissionCheck = ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.RECORD_AUDIO);
        int permissionCheck2= ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE);

        // Xin quyền thiết bị
        if (permissionCheck != PackageManager.PERMISSION_GRANTED)
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}
                        , PERMISSIONS_REQUEST_RECORD_AUDIO);

        if(permissionCheck2 != PackageManager.PERMISSION_GRANTED)
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}
                        , PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE);


        addControls();
        addEvents();

        initializeTextToSpeech();  // Hàm xử lý voice
    }

    private void addEvents() {
        swVoice.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(swVoice.isChecked()){
                    new SetupTask(HomeActivity.this).execute();
                    saveToPreferences("switch", "1");
                    ControlFirstClassFragment.txtContentVoice.setTextColor(Color.WHITE);
                    ControlFirstClassFragment.txtContentVoice.setText(R.string.sayTut);
                }else {
                    if (recognizer != null) {
                        recognizer.cancel();
                        recognizer.shutdown();
                        saveToPreferences("switch", "0");
                        ControlFirstClassFragment.txtContentVoice.setTextColor(Color.parseColor("#757575"));
                        ControlFirstClassFragment.txtContentVoice.setText("Bộ điều khiển đã tắt");
                    }

                }
            }
        });

    }

    //Handling callback
    // Phuong thuc xử lý sau khi người dùng cho phép cấp quyền hay hủy
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSIONS_REQUEST_RECORD_AUDIO: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted, yay!

                    //Toast.makeText(HomeActivity.this,"Preparing the recognizer", BaseTransientBottomBar.LENGTH_LONG).show();

                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    Toast.makeText(this, "Permissions Denied to record audio", Toast.LENGTH_LONG).show();
                }
                return;
            }

            case PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted, yay!

                    Toast.makeText(HomeActivity.this,"Preparing the recognizer", Toast.LENGTH_LONG).show();
                    new SetupTask(this).execute();

                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    Toast.makeText(this, "Permissions Denied to WRITE_EXTERNAL_STORAGE", Toast.LENGTH_LONG).show();
                }
                return;
            }
        }
    }

    private void addControls() {
        drawerLayout= findViewById(R.id.drawer);
        nv= findViewById(R.id.nv);
        toolbar= findViewById(R.id.toolbar);
        pager = findViewById(R.id.view_pager);
        tabLayout = findViewById(R.id.tab_layout);

        // Khai báo ánh xạ header view trong drawer menu
        View hView =  nv.getHeaderView(0);
        swVoice= hView.findViewById(R.id.switchVoice);

        // Lấy dữ liệu sw tắt bật voice control
        if(getDataInReferences("switch").equals("1")){
            swVoice.setChecked(true);
        }else{
            swVoice.setChecked(false);
        }

        // Setup tablayout
        FragmentManager fragmentManager= getSupportFragmentManager();
        pagerAdapter= new vn.viethoang.truong.smartclass.Adapter.PagerAdapter(fragmentManager);
        pager.setAdapter(pagerAdapter);
        tabLayout.setupWithViewPager(pager);
        pager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
        tabLayout.setTabsFromPagerAdapter(pagerAdapter);//deprecated
        tabLayout.addOnTabSelectedListener(new TabLayout.ViewPagerOnTabSelectedListener(pager));


        // setup drawerLayout
        toolbar.setTitle("");
        setSupportActionBar(toolbar);
        toggle= new ActionBarDrawerToggle(this, drawerLayout, R.string.open, R.string.close);
        drawerLayout.addDrawerListener(toggle);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toggle.syncState();
        setupDrawerContent(nv);

    }


    // Task setup voice control
    // use static
    private static class SetupTask extends AsyncTask<Void, Void, Exception> {
        WeakReference<HomeActivity> activityReference;
        SetupTask(HomeActivity activity) {
            this.activityReference = new WeakReference<>(activity);
        }
        @Override
        protected Exception doInBackground(Void... params) {
            try {
                Assets assets = new Assets(activityReference.get());
                File assetDir = assets.syncAssets();
                activityReference.get().setupRecognizer(assetDir);
            } catch (IOException e) {
                return e;
            }
            return null;
        }
        @Override
        protected void onPostExecute(Exception result) {
            if (result != null) {
                Log.e(TAG, "Failed to init recognizer " + result);
            } else {
                Log.e(TAG, "TIEP TUC");
                activityReference.get().switchSearch(KWS_SEARCH);
            }
        }
    }

    // Chuyền đổi giữa nhận dạng key wakeup và key word menu chức năng
    private void switchSearch(String kwsSearch) {
        recognizer.stop();

        if (kwsSearch.equals(KWS_SEARCH)) {
            recognizer.startListening(kwsSearch);
           // Toast.makeText(HomeActivity.this, kwsSearch, Toast.LENGTH_LONG).show();
        }
        else
            recognizer.startListening(kwsSearch, 3000);

    }

    // Cài đặt file thư viện tiếng anh
    private void setupRecognizer(File assetsDir) throws IOException {
        recognizer = SpeechRecognizerSetup.defaultSetup()
                .setAcousticModel(new File(assetsDir, "en-us-ptm"))
                .setDictionary(new File(assetsDir, "cmudict-en-us.dict"))
                // Disable this line if you don't want recognizer to save raw
                // audio files to app's storage
                .setRawLogDir(assetsDir)
                .getRecognizer();
        recognizer.addListener(this);

        // Create keyword-activation search.
        recognizer.addKeyphraseSearch(KWS_SEARCH, KEYPHRASE);

        // Create your custom grammar-based search
        File menuGrammar = new File(assetsDir, "menu.gram");   // File menu chứa các key chức năng
        recognizer.addGrammarSearch(MENU_SEARCH, menuGrammar);

    }

    @Override
    protected void onStop() {
        super.onStop();
        // Dừng tiến trình ghi âm
        if (recognizer != null && swVoice.isChecked()) {
            recognizer.cancel();
            recognizer.shutdown();
            Log.e("ON STOP", "stopped recognizer");
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.e("ON RESUME", "I am here");
        // Tiếp tục ghi âm khi quay lại app
        // Xin quyền phòng trường hợp người dùng tắt
        // Check if user has given permission to record audio
        int permissionCheck = ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.RECORD_AUDIO);
        int permissionCheck2= ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if (permissionCheck != PackageManager.PERMISSION_GRANTED || permissionCheck2 != PackageManager.PERMISSION_GRANTED) {
            if (permissionCheck != PackageManager.PERMISSION_GRANTED)
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}
                        , PERMISSIONS_REQUEST_RECORD_AUDIO);
            if(permissionCheck2 != PackageManager.PERMISSION_GRANTED)
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}
                        , PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE);

        }
        if(swVoice.isChecked()) {
            new SetupTask(this).execute();
        }
    }

    // Hàm setup menu drawer
    private void setupDrawerContent(NavigationView nv) {
        nv.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                selectDrawerItem(item);
                return false;
            }
        });
    }

    private void selectDrawerItem(MenuItem item) {
        Intent intent;
        switch (item.getItemId()){
            case R.id.mnControl:
                drawerLayout.closeDrawers();
                break;
            case R.id.mnTut:
                intent= new Intent(HomeActivity.this, TutActivity.class);
                startActivity(intent);
                break;
            case R.id.mnAboutApp:
                intent= new Intent(HomeActivity.this, AboutAppActivity.class);
                startActivity(intent);
                break;

        }

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (toggle.onOptionsItemSelected(item)){
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    // Hàm xử lý khi bật thiết bị
    private void turnOnSensor(String name) {
        writeToFirebaseDB(name, 1);
        saveToPreferences(name, "1");
    }

    // Hàm xử lý khi tắt thiết bị
    private void turnOffSensor(String name) {
        writeToFirebaseDB(name, 0);
        saveToPreferences(name, "0");
    }


    // hàm ghi dữ liệu lên firebase
    public void writeToFirebaseDB(String reference, int value){
        // Write data to fbdb
        database = FirebaseDatabase.getInstance();
        myRef = database.getReference(reference);
        myRef.setValue(value);
    }


    // Hàm cài đặt Text To Speech
    private void initializeTextToSpeech() {

        mytts= new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status != TextToSpeech.SUCCESS){
                    Toast.makeText(HomeActivity.this,
                            "There is no TTS engine on your device", Toast.LENGTH_LONG).show();
                    finish();
                }else {
                    mytts.setLanguage(Locale.US);
                    Log.d(TAG, "TTS thanh cong ");
                    //speak("Hello sir !");
                }
            }
        });

    }

    // Hàm chuyển giọng nói
    private static void speak(String message) {
        if (Build.VERSION.SDK_INT >=21){
            mytts.speak(message, TextToSpeech.QUEUE_FLUSH, null, null);
        }else {
            mytts.speak(message, TextToSpeech.QUEUE_FLUSH, null);
        }
    }

    // Lấy dữ liệu thiết bị tắt mở trên references
    private String getDataInReferences(String name){
        preferences= getSharedPreferences(preferencesName,MODE_PRIVATE);
        String value= preferences.getString(name,"");
        return value;
    }

    // save dữ liệu và preferrences
    private void saveToPreferences(String name, String value) {
        preferences= HomeActivity.this.getSharedPreferences(HomeActivity.this.getApplicationContext());
        SharedPreferences.Editor editor= preferences.edit();
        editor.putString(name, value);
        editor.apply();
        Log.e("RE", "ok");
    }

    // Sử dụng hàm này khi save preferences cùng static
    public static SharedPreferences getSharedPreferences (Context ctxt) {
        return ctxt.getSharedPreferences(preferencesName, 0);
    }


    @Override
    public void onBeginningOfSpeech() {

    }

    // Khi tiến trình ghi âm đã hoàn thành, chuyển sang key wakeup
    @Override
    public void onEndOfSpeech() {
        if (!recognizer.getSearchName().equals(KWS_SEARCH))
            switchSearch(KWS_SEARCH);
    }

    // Khi lắng nghe được key wakeup thì thực hiện chuyển sang chế độ key menu chức năng
    @Override
    public void onPartialResult(Hypothesis hypothesis) {
        if (hypothesis == null) {
            return;
        }
        String text = hypothesis.getHypstr();
        if (text.equals(KEYPHRASE)) {
            switchSearch(MENU_SEARCH);
        }
    }

    // Xử lý khi đã lắng nghe
    @Override
    public void onResult(Hypothesis hypothesis) {
        if (hypothesis != null) {
            String action= hypothesis.getHypstr();  // chuyển giọng ghi âm thành string để xử lý
            // Nếu action chứa key : ok my phone thì nghe tiếp
            // nếu sai thì xét các trường hợp còn lại
            if(action.contains(KEYPHRASE)) {
                mediaPlayer= MediaPlayer.create(HomeActivity.this, R.raw.wakeup);
                mediaPlayer.start();
                ControlFirstClassFragment.txtContentVoice.setText("Đang nghe...");
            }else{
                mediaPlayer= MediaPlayer.create(HomeActivity.this, R.raw.f);
                mediaPlayer.start();

                if(action.contains("what") || action.contains("your")){
                    if(action.contains("name"))
                        speak("My name is Sunday");
                        replayTxtContentVoice();
                }else {

                    // Xét bật tắt quạt ?
                    if (action.contains("turn on the fan") |
                            action.contains("turn on the fan's")) {
                        turnOnSensorVoice("quat", "fan");
                    } else if (action.contains("turn off the fan")
                            | action.contains("turn off the fan's")) {
                        turnOffSensorVoice("quat", "fan");
                    } else if (action.contains("turn on the light")
                            | action.contains("turn on light's")
                            | action.contains("turn on lighted")
                            | action.contains("turn on lighter")) {
                        turnOnSensorVoice("den", "light");
                    } else if (action.contains("turn off the light")
                            | action.contains("turn off light's")
                            | action.contains("turn off lighted")
                            | action.contains("turn off lighter")) {
                        turnOffSensorVoice("den", "light");
                    } else {
                        //Hủy bỏ hành động
                        if (action.contains("cancel")) {
                            speak("Cancel action");
                            ControlFirstClassFragment.txtContentVoice.setText("Đã hủy hành động");
                            replayTxtContentVoice();
                        } else {
                            speak("I dont understand");
                            ControlFirstClassFragment.txtContentVoice.setText("Tôi không hiểu");
                            replayTxtContentVoice();
                        }
                    }
                }
            }
        }
    }

    // Chuyển text hiển thị giọng nói của thiết bị như ban đầu: "Nói ok my phone"
    private void replayTxtContentVoice() {
        Thread thread= new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }finally {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {

                            // Stuff that updates the UI
                            ControlFirstClassFragment.txtContentVoice.setText(R.string.sayTut);

                        }
                    });

                }
            }
        });

        thread.start();
    }

    // Tắt thiết bị bằng giọng nói
    private void turnOffSensorVoice(String vi, String el) {
        if(CheckConnectInternet.haveNetworkConnection(HomeActivity.this)) {

            if (getDataInReferences(vi).equals("0")) {
                speak("The" + el + " is turned off");
                if (vi.equals("den"))
                    ControlFirstClassFragment.txtContentVoice.setText("Quạt đã được tắt rồi");
                else
                    ControlFirstClassFragment.txtContentVoice.setText("Đèn đã được tắt rồi");
            } else {
                turnOffSensor(vi);
                speak("Ok. The" + el + "have been turned of");
                if (vi.equals("quat"))
                    ControlFirstClassFragment.txtContentVoice.setText("Đã tắt quạt");
                else
                    ControlFirstClassFragment.txtContentVoice.setText("Đã tắt đèn");
            }
        }else {
            ControlFirstClassFragment.txtContentVoice.setText("Hãy kiểm tra lại kết nối internet !");
            speak("Sorry. Check the connect internet");
        }

        replayTxtContentVoice();

    }

    // Bật thiết bị bằng giọng nói
    private void turnOnSensorVoice(String vi, String el) {
        if(CheckConnectInternet.haveNetworkConnection(HomeActivity.this)) {
            if(Integer.valueOf(getDataInReferences(vi))>0){
                speak("The"+ el+" is turned on");
                if(vi.equals("quat"))
                 ControlFirstClassFragment.txtContentVoice.setText("Quạt đã được bật rồi");
                else
                    ControlFirstClassFragment.txtContentVoice.setText("Đèn đã được bật rồi");
            }else {
                turnOnSensor(vi);
                speak("Ok. The "+el +" have been turned on");
                if(vi.equals("quat"))
                    ControlFirstClassFragment.txtContentVoice.setText("Đã bật quạt");
                else
                    ControlFirstClassFragment.txtContentVoice.setText("Đã bật đèn");
            }
        }else {
            ControlFirstClassFragment.txtContentVoice.setText("Hãy kiểm tra lại kết nối internet !");
            speak("Sorry. Check the connect internet");
        }

        replayTxtContentVoice();

    }

    @Override
    public void onError(Exception e) {
        System.out.println(e.getMessage());
    }

    // Sau thời gian lắng nghe tiếp tục trở lại key wakeup
    @Override
    public void onTimeout() {
        Log.e("TIMEOUT", "TIEP TUC");
        switchSearch(KWS_SEARCH);
        ControlFirstClassFragment.txtContentVoice.setText(R.string.sayTut);
    }

}
