package me.sirius.sschedules.model;

import java.text.Normalizer;
import java.util.Calendar;
import java.util.Locale;
import java.util.Map;

public class TimeFrame {

    // En ingles y en espanol, con o sin tilde. Antes habia dos copias de esta tabla:
    // la de los placeholders entendia "lunes" y la del planificador no, asi que un
    // horario de los lunes se mostraba bien pero se ejecutaba todos los dias.
    private static final Map<String, Integer> DAYS = Map.ofEntries(
            Map.entry("sunday", Calendar.SUNDAY), Map.entry("domingo", Calendar.SUNDAY),
            Map.entry("monday", Calendar.MONDAY), Map.entry("lunes", Calendar.MONDAY),
            Map.entry("tuesday", Calendar.TUESDAY), Map.entry("martes", Calendar.TUESDAY),
            Map.entry("wednesday", Calendar.WEDNESDAY), Map.entry("miercoles", Calendar.WEDNESDAY),
            Map.entry("thursday", Calendar.THURSDAY), Map.entry("jueves", Calendar.THURSDAY),
            Map.entry("friday", Calendar.FRIDAY), Map.entry("viernes", Calendar.FRIDAY),
            Map.entry("saturday", Calendar.SATURDAY), Map.entry("sabado", Calendar.SATURDAY));

    // Dia de la semana de Calendar, o -1 si el nombre no es un dia.
    public static int dayOfWeek(String name) {
        if (name == null) {
            return -1;
        }
        String plain = Normalizer.normalize(name.trim().toLowerCase(Locale.ROOT), Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "");
        return DAYS.getOrDefault(plain, -1);
    }

    // true si el instante del calendario cae en este tramo.
    public boolean matches(Calendar calendar) {
        if (month != null && calendar.get(Calendar.MONTH) + 1 != month) {
            return false;
        }
        Integer dayNumber = getDayAsNumber();
        if (dayNumber != null && calendar.get(Calendar.DAY_OF_MONTH) != dayNumber) {
            return false;
        }
        int weekday = dayOfWeek(getDayAsString());
        if (weekday != -1 && calendar.get(Calendar.DAY_OF_WEEK) != weekday) {
            return false;
        }
        if (hour != null && calendar.get(Calendar.HOUR_OF_DAY) != hour) {
            return false;
        }
        if (minute != null && calendar.get(Calendar.MINUTE) != minute) {
            return false;
        }
        return second == null || calendar.get(Calendar.SECOND) == second;
    }

    // Para los horarios continuos: cada cuanto se repiten, como minimo 1 s.
    public int intervalSeconds() {
        int seconds = 0;
        if (hour != null) seconds += hour * 3600;
        if (minute != null) seconds += minute * 60;
        if (second != null) seconds += second;
        return Math.max(1, seconds);
    }

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