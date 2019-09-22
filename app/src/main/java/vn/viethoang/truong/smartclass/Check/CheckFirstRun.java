package vn.viethoang.truong.smartclass.Check;

import android.content.Context;

public class CheckFirstRun {
    Context context;
    String prerencseName;
    public CheckFirstRun(Context context, String prerencseName){
        this.context= context;
        this.prerencseName= prerencseName;
    }

    // Hàm kiểm tra lần mở app đầu tiên
    // Vì sao có hàm này?
    // Để sử dụng khởi tạo giá trị state=0 (như đã nói state là biến lưu trạng thái hẹn giờ)
    public boolean isFirstRun() {
        boolean firstrun = context.getSharedPreferences(prerencseName, context.MODE_PRIVATE)
                .getBoolean("firstrun", true);
        if (firstrun){
            // Save the state
            context.getSharedPreferences(prerencseName, context.MODE_PRIVATE)
                    .edit()
                    .putBoolean("firstrun", false)
                    .commit();
            return true;
        }
        return false;
    }

}
