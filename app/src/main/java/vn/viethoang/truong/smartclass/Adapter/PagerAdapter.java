package vn.viethoang.truong.smartclass.Adapter;

import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import vn.viethoang.truong.smartclass.View.Home.Fragment.ControlSecondClassFragment;
import vn.viethoang.truong.smartclass.View.Home.Fragment.ControlFirstClassFragment;

public class PagerAdapter extends FragmentStatePagerAdapter {

    public PagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int i) {
        Fragment fragment= null;

        switch (i){
            case 0:
                fragment= new ControlFirstClassFragment();
                break;
            case 1:
                fragment= new ControlSecondClassFragment();
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
                title= "Phòng 103";
                break;
            case 1:
                title= "Phòng 104";
                break;
        }

        return title;
    }
}
