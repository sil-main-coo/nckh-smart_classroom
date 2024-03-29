package vn.viethoang.truong.smartclass.View.Alarm.Fragment;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import vn.viethoang.truong.smartclass.Adapter.PagerAlarmAdapter;
import vn.viethoang.truong.smartclass.Adapter.RCVAlarmAdapter;
import vn.viethoang.truong.smartclass.Check.CheckFirstRun;
import vn.viethoang.truong.smartclass.R;
import vn.viethoang.truong.smartclass.View.Alarm.Model.AlarmDevice;

import static android.content.Context.MODE_PRIVATE;
import static vn.viethoang.truong.smartclass.View.Home.Fragment.ControlFirstClassFragment.readDataFromFB;
import static vn.viethoang.truong.smartclass.View.Home.Fragment.ControlFirstClassFragment.saveToPreferences;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link AlarmTurnOnFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link AlarmTurnOnFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class AlarmTurnOnFragment extends Fragment {

    RecyclerView mRecyclerView;
    static RCVAlarmAdapter mRcvAdapter;
    static List<AlarmDevice> data;
    static Context thisContext;

    static AlarmDevice light= new AlarmDevice("Đèn");
    static AlarmDevice fan= new AlarmDevice("Quạt");

    private static final String TAG= "ALARM_ON_FRAG";
    private static String preferencesName= "alarm";
    private static SharedPreferences preferences;

    private static String nameParamLight= "den";
    private static String nameParamFan= "quat";

    private static final int action= 1;

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;


    private OnFragmentInteractionListener mListener;

    public AlarmTurnOnFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment AlarmTurnOnFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static AlarmTurnOnFragment newInstance(String param1, String param2) {
        AlarmTurnOnFragment fragment = new AlarmTurnOnFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
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
        View view= inflater.inflate(R.layout.fragment_alarm_turn_on, container, false);
        thisContext= container.getContext();

        addControls(view);


        return view;
    }

    private void addControls(View view) {
        mRecyclerView = view.findViewById(R.id.rcAlarmOn);

        data = new ArrayList<>();
        data.add(light);
        data.add(fan);
        mRcvAdapter = new RCVAlarmAdapter(data, getContext(), action);

        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);

        mRecyclerView.setLayoutManager(layoutManager);
        Log.e(TAG, "on Controls");
    }


    private static void setupInforDevices(String nameDevice, List<AlarmDevice> data, RCVAlarmAdapter mRcvAdapter) {
        if(nameDevice.equals(nameParamLight)) {
            data.get(0).setHour(getDataInReferences(nameDevice + action + "Hour"));
            data.get(0).setMinute(getDataInReferences(nameDevice + action + "Minute"));
            data.get(0).setDate(getDataInReferences(nameDevice + action + "Date"));
            data.get(0).setDayOfweek(getDataInReferences(nameDevice + action + "DayOfWeek"));
            data.get(0).setMonth(getDataInReferences(nameDevice + action + "Month"));
            data.get(0).setYear(getDataInReferences(nameDevice + action + "Year"));
            data.get(0).setSelect(Integer.parseInt(getDataInReferences(nameDevice + action + "Selected")));
            data.get(0).setsRepeats(getDataInReferences(nameDevice+action+"Repeat"));
        }else if(nameDevice.equals(nameParamFan)){
            data.get(1).setHour(getDataInReferences(nameDevice + action + "Hour"));
            data.get(1).setMinute(getDataInReferences(nameDevice + action + "Minute"));
            data.get(1).setDate(getDataInReferences(nameDevice + action + "Date"));
            data.get(1).setDayOfweek(getDataInReferences(nameDevice + action + "DayOfWeek"));
            data.get(1).setMonth(getDataInReferences(nameDevice + action + "Month"));
            data.get(1).setYear(getDataInReferences(nameDevice + action + "Year"));
            data.get(1).setSelect(Integer.parseInt(getDataInReferences(nameDevice + action + "Selected")));
            data.get(1).setsRepeats(getDataInReferences(nameDevice+action+"Repeat"));
        }

      //  Log.e(TAG, getDataInReferences(nameDevice + action + "Selected"));
        mRcvAdapter.notifyDataSetChanged();

    }

    // Lấy dữ liệu thiết bị tắt mở trên references
    private static String getDataInReferences(String name){
        preferences= thisContext.getSharedPreferences(preferencesName,MODE_PRIVATE);
        String value= preferences.getString(name,"00");
        return value;
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
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


    @Override
    public void onResume() {
        super.onResume();
        Log.e(TAG, "IN RESUME");

        // Hiển thị đèn và quạt
        setupInforDevices(nameParamLight, data, mRcvAdapter);
        setupInforDevices(nameParamFan, data, mRcvAdapter);
        mRecyclerView.setAdapter(mRcvAdapter);
    }
}
