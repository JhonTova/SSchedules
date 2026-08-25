package me.sirius.sschedules.model;

public class TimeFrame {
    
    private Integer month;
    private Object day;
    private Integer hour;
    private Integer minute;
    private Integer second;
    
    public Integer getMonth() {
        return month;
    }
    
    public void setMonth(Integer month) {
        this.month = month;
    }
    
    public Object getDay() {
        return day;
    }
    
    public void setDay(Object day) {
        this.day = day;
    }
    
    public Integer getDayAsNumber() {
        if (day instanceof Integer) {
            return (Integer) day;
        }
        return null;
    }
    
    public String getDayAsString() {
        if (day instanceof String) {
            return (String) day;
        }
        return null;
    }
    
    public Integer getHour() {
        return hour;
    }
    
    public void setHour(Integer hour) {
        this.hour = hour;
    }
    
    public Integer getMinute() {
        return minute;
    }
    
    public void setMinute(Integer minute) {
        this.minute = minute;
    }
    
    public Integer getSecond() {
        return second;
    }
    
    public void setSecond(Integer second) {
        this.second = second;
    }
}