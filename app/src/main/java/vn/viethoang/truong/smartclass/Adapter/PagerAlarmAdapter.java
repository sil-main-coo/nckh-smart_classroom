package vn.viethoang.truong.smartclass.Adapter;

import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import vn.viethoang.truong.smartclass.View.Alarm.Fragment.AlarmTurnOffFragment;
import vn.viethoang.truong.smartclass.View.Alarm.Fragment.AlarmTurnOnFragment;


public class PagerAlarmAdapter extends FragmentStatePagerAdapter {
    public PagerAlarmAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int i) {
        Fragment fragment= null;
        switch (i){
            case 0:
                fragment= new AlarmTurnOnFragment();
                break;
            case 1:
                fragment= new AlarmTurnOffFragment();
                break;
        }
        return fragment;
    }

    @Override
    public int getCount() {
        return 2;
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        String title="";
        switch (position){
            case 0:
                title= "Hẹn giờ bật";
                break;
            case 1:
                title= "Hẹn giờ tắt";
                break;
        }

        return title;
    }
}
