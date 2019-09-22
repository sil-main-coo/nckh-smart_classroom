package vn.viethoang.truong.smartclass.Check;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public class CheckConnectInternet {
    public static boolean haveNetworkConnection(Context context){
        boolean haveConnectWifi = false;
        boolean haveConnectMobile = false;

        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo[] netInfo = cm.getAllNetworkInfo();
        for (NetworkInfo ni: netInfo){
            if (ni.getTypeName().equalsIgnoreCase("WIFI")){
                if(ni.isConnected()){
                    haveConnectWifi = true;
                }
            }
            if (ni.getTypeName().equalsIgnoreCase("MOBILE")){
                if(ni.isConnected()){
                    haveConnectMobile = true;
                }
            }
        }
        return haveConnectWifi || haveConnectMobile;
    }

}