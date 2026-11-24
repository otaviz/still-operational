package com.stilloperational;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class JogoTest {

    @Test
    void testarEstadoInicial() {
        Jogo jogo = new Jogo();

        assertEquals(1, jogo.faseAtual());
        assertEquals(0, jogo.tokens());
        assertFalse(jogo.terminou());
    }

    @Test
    void testarAvancoDeFase() {
        Jogo jogo = new Jogo();

        assertTrue(jogo.responder(Operador.AND));
        assertEquals(2, jogo.faseAtual());
        assertEquals(1, jogo.tokens());
    }

    @Test
    void testarErroSemAvanco() {
        Jogo jogo = new Jogo();

        assertFalse(jogo.responder(Operador.OR));
        assertEquals(1, jogo.faseAtual());
        assertEquals(0, jogo.tokens());
    }
}