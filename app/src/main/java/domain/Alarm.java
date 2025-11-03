package domain;

public class Alarm {
    private String regtime;
    private String id;
    private String a_value;
    private String is_send;
    private String alarm_address;
    private String comment;
    private String hogi;
    private String startTime;
    private String endTime;

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public String getHogi() {
        return hogi;
    }

    public void setHogi(String hogi) {
        this.hogi = hogi;
    }

    public String getRegtime() {
        return regtime;
    }

    public void setRegtime(String regtime) {
        this.regtime = regtime;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getAlarm_address() {
        return alarm_address;
    }

    public void setAlarm_address(String alarm_address) {
        this.alarm_address = alarm_address;
    }

    public String getIs_send() {
        return is_send;
    }

    public void setIs_send(String is_send) {
        this.is_send = is_send;
    }

    public String getA_value() {
        return a_value;
    }

    public void setA_value(String a_value) {
        this.a_value = a_value;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
