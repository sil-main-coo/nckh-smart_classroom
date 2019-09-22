package vn.viethoang.truong.smartclass.View.Alarm;

import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Toast;

import vn.viethoang.truong.smartclass.Adapter.PagerAlarmAdapter;
import vn.viethoang.truong.smartclass.R;


public class ListAlarmDevice extends AppCompatActivity {
    TabLayout tl_alarm;
    ViewPager vpAlarmLayout;
    PagerAlarmAdapter pagerAdapter;
    FloatingActionButton flAddAlarm;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_alarm_layout);
        setupGoBackButton(); // Cài đặt hiển thị nút goback

        addControls();
        addEvents();

    }

    private void addEvents() {
        flAddAlarm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(ListAlarmDevice.this, "Tính năng thêm mới chưa được cập nhật !", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void addControls() {
        tl_alarm= findViewById(R.id.tl_alarm);
        flAddAlarm= findViewById(R.id.fabAdđDevice);
        vpAlarmLayout= findViewById(R.id.vpAlarmLayout);

        // Setup tablayout
        FragmentManager fragmentManager= getSupportFragmentManager();
        pagerAdapter= new PagerAlarmAdapter(fragmentManager);
        vpAlarmLayout.setAdapter(pagerAdapter);
        tl_alarm.setupWithViewPager(vpAlarmLayout);
        vpAlarmLayout.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tl_alarm));
        tl_alarm.setTabsFromPagerAdapter(pagerAdapter);//deprecated
        tl_alarm.addOnTabSelectedListener(new TabLayout.ViewPagerOnTabSelectedListener(vpAlarmLayout));

    }

    private void setupGoBackButton() {
        Toolbar toolbar = findViewById(R.id.toolbar_list_alarm);
        toolbar.setTitle("");
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }
}
