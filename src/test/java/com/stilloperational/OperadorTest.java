package com.stilloperational;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class OperadorTest {

    @Test
    void testarAND() {
        assertTrue(Operador.AND.aplicar(true, true));
        assertFalse(Operador.AND.aplicar(true, false));
        assertFalse(Operador.AND.aplicar(false, true));
        assertFalse(Operador.AND.aplicar(false, false));
    }
    @Test
    void testarOR() {
        assertTrue(Operador.OR.aplicar(true, true));
        assertTrue(Operador.OR.aplicar(true, false));
        assertTrue(Operador.OR.aplicar(false, true));
        assertFalse(Operador.OR.aplicar(false, false));
    }

    @Test
    void testarNOT() {
        assertFalse(Operador.NOT.aplicar(true, false));
        assertFalse(Operador.NOT.aplicar(true, true));
        assertTrue(Operador.NOT.aplicar(false, false));
        assertTrue(Operador.NOT.aplicar(false, true));
    }

}