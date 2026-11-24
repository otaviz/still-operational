package com.stilloperational;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class IntegracaoTest {

    @Test
    void testarPartidaCompleta() {
        Jogo jogo = new Jogo();

        assertFalse(jogo.responder(Operador.OR));
        assertEquals(0, jogo.tokens());

        assertTrue(jogo.responder(Operador.AND));
        assertTrue(jogo.responder(Operador.OR));
        assertTrue(jogo.responder(Operador.NOT));

        assertEquals(3, jogo.tokens());
        assertTrue(jogo.terminou());
    }
}