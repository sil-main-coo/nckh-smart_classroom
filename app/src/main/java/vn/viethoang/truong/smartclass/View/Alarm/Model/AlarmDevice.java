package vn.viethoang.truong.smartclass.View.Alarm.Model;

public class AlarmDevice {
    private String deviceName;
    private String hour;
    private String minute;
    private String date, month, year, dayOfweek;
    private String sRepeats;
    private int sttDevice;
    private int select;

    public AlarmDevice() {
    }

    public AlarmDevice(String deviceName) {
        this.deviceName = deviceName;
    }

    public String getDate() {
        return date;
    }

    public String getDayOfweek() {
        return dayOfweek;
    }

    public String getsRepeats() {
        return sRepeats;
    }

    public void setsRepeats(String sRepeats) {
        this.sRepeats = sRepeats;
    }

    public void setDayOfweek(String dayOfweek) {
        this.dayOfweek = dayOfweek;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getMonth() {
        return month;
    }

    public void setMonth(String month) {
        this.month = month;
    }

    public String getYear() {
        return year;
    }

    public void setYear(String year) {
        this.year = year;
    }

    public int getSttDevice() {
        return sttDevice;
    }

    public void setSttDevice(int sttDevice) {
        this.sttDevice = sttDevice;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    public String getHour() {
        return hour;
    }

    public void setHour(String hour) {
        this.hour = hour;
    }

    public String getMinute() {
        return minute;
    }

    public void setMinute(String minute) {
        this.minute = minute;
    }

    public int getSelect() {
        return select;
    }

    public void setSelect(int select) {
        this.select = select;
    }
}
