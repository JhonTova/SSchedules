package me.sirius.sschedules.model;

import org.junit.jupiter.api.Test;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.TimeZone;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TimeFrameTest {

    // 12 de octubre de 2026 fue lunes.
    private static Calendar lunes(int hour, int minute, int second) {
        Calendar c = new GregorianCalendar(TimeZone.getTimeZone("America/Bogota"));
        c.clear();
        c.set(2026, Calendar.OCTOBER, 12, hour, minute, second);
        return c;
    }

    @Test
    void entiendeLosDiasEnEspanolYEnIngles() {
        assertEquals(Calendar.MONDAY, TimeFrame.dayOfWeek("lunes"));
        assertEquals(Calendar.MONDAY, TimeFrame.dayOfWeek("MONDAY"));
        assertEquals(Calendar.WEDNESDAY, TimeFrame.dayOfWeek("miércoles"));
        assertEquals(Calendar.SATURDAY, TimeFrame.dayOfWeek(" Sábado "));
        assertEquals(-1, TimeFrame.dayOfWeek("festivo"));
        assertEquals(-1, TimeFrame.dayOfWeek(null));
    }

    // El fallo que habia: "lunes" no lo entendia el planificador y el horario se
    // disparaba todos los dias.
    @Test
    void unHorarioDeLosLunesNoSaltaElMartes() {
        TimeFrame frame = new TimeFrame();
        frame.setDay("lunes");
        frame.setHour(20);
        frame.setMinute(0);

        assertTrue(frame.matches(lunes(20, 0, 30)));
        Calendar martes = lunes(20, 0, 30);
        martes.add(Calendar.DAY_OF_MONTH, 1);
        assertFalse(frame.matches(martes));
    }

    @Test
    void comparaMesDiaDelMesYSegundos() {
        TimeFrame frame = new TimeFrame();
        frame.setMonth(10);
        frame.setDay(12);
        frame.setHour(8);
        frame.setMinute(30);
        frame.setSecond(15);

        assertTrue(frame.matches(lunes(8, 30, 15)));
        assertFalse(frame.matches(lunes(8, 30, 16)));
        assertFalse(frame.matches(lunes(9, 30, 15)));
    }

    @Test
    void elIntervaloNuncaEsCero() {
        TimeFrame frame = new TimeFrame();
        assertEquals(1, frame.intervalSeconds());
        frame.setHour(1);
        frame.setMinute(30);
        frame.setSecond(5);
        assertEquals(5405, frame.intervalSeconds());
    }
}
