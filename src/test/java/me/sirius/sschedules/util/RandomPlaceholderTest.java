package me.sirius.sschedules.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RandomPlaceholderTest {

    private final Random random = new Random(42);

    @Test
    void quedaDentroDelRango() {
        for (int i = 0; i < 1000; i++) {
            long value = Long.parseLong(RandomPlaceholder.apply("%rand/5-10%", random));
            assertTrue(value >= 5 && value <= 10, "fuera de rango: " + value);
        }
    }

    @Test
    void sustituyeVariosEnElMismoTexto() {
        String out = RandomPlaceholder.apply("give %rand/1-1% x %rand/7-7%", random);
        assertEquals("give 1 x 7", out);
    }

    @Test
    void admiteNegativos() {
        long value = Long.parseLong(RandomPlaceholder.apply("%rand/-3--3%", random));
        assertEquals(-3, value);
    }

    // Antes un maximo menor que el minimo lanzaba una excepcion dentro de la tarea.
    @Test
    @Timeout(1)
    void losMalEscritosSeDejanYNoCuelgan() {
        assertEquals("%rand/10-5%", RandomPlaceholder.apply("%rand/10-5%", random));
        assertEquals("%rand/abc% y 3", RandomPlaceholder.apply("%rand/abc% y %rand/3-3%", random));
        assertEquals("%rand/1-2", RandomPlaceholder.apply("%rand/1-2", random));
        assertEquals("%rand/%", RandomPlaceholder.apply("%rand/%", random));
    }

    @Test
    void sinMarcadorDevuelveLoMismo() {
        String text = "broadcast hola";
        assertEquals(text, RandomPlaceholder.apply(text, random));
    }
}
