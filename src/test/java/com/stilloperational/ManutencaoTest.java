package com.stilloperational;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ManutencaoTest {

    @Test
    void testarRespostaCorreta() {
        Manutencao desafio = new Manutencao(
                "Teste de segurança",
                true, true, true, Operador.AND
        );

        assertTrue(desafio.verificar(Operador.AND));
    }

    @Test
    void testarRespostaIncorreta() {
        Manutencao desafio = new Manutencao(
                "Teste de segurança",
                true, true, true, Operador.AND
        );

        assertFalse(desafio.verificar(Operador.OR));
    }
}