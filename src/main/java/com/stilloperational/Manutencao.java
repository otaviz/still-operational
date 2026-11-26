package com.stilloperational;

import java.util.Objects;

/**
 * Representa uma ordem de manutenção e seu comportamento lógico esperado.
 * @param descricao enunciado do desafio
 * @param entradaA primeira condição do cenário inicial
 * @param entradaB segunda condição do cenário inicial
 * @param resultadoEsperado resultado do cenário inicial
 * @param operadorCorreto módulo que satisfaz a regra do dispositivo
 */
public record Manutencao(String descricao, boolean entradaA, boolean entradaB,
                         boolean resultadoEsperado, Operador operadorCorreto) {

    public Manutencao {
        Objects.requireNonNull(descricao, "Descrição obrigatória.");
        Objects.requireNonNull(operadorCorreto, "Operador obrigatório.");
        if (descricao.isBlank()) throw new IllegalArgumentException("Descrição vazia.");
        if (operadorCorreto.aplicar(entradaA, entradaB) != resultadoEsperado)
            throw new IllegalArgumentException("Resultado inicial incompatível com o operador.");
    }

    /** @return se a manutenção possui uma segunda entrada física */
    public boolean usaDuasEntradas() { return operadorCorreto != Operador.NOT; }

    /** Calcula a saída que o equipamento deveria produzir para estas entradas. */
    public boolean resultadoPara(boolean a, boolean b) {
        return operadorCorreto.aplicar(a, usaDuasEntradas() && b);
    }

    /** Verifica a configuração em todas as combinações possíveis, não só na inicial. */
    public boolean verificar(Operador escolhido) {
        if (escolhido == null) return false;
        int total = usaDuasEntradas() ? 4 : 2;
        for (int i = 0; i < total; i++) {
            boolean a = usaDuasEntradas() ? (i & 2) != 0 : i != 0;
            boolean b = usaDuasEntradas() && (i & 1) != 0;
            if (escolhido.aplicar(a, b) != resultadoPara(a, b)) return false;
        }
        return escolhido == operadorCorreto;
    }
}
