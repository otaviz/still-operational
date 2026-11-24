package com.stilloperational;

/**
 * Representa os operadores booleanos disponíveis no jogo.
 */
public enum Operador {

    /** Retorna verdadeiro quando ambas as entradas são verdadeiras. */
    AND,

    /** Retorna verdadeiro quando pelo menos uma entrada é verdadeira. */
    OR,

    /** Inverte o valor lógico da primeira entrada. */
    NOT;

    /**
     * Aplica o operador lógico às entradas recebidas.
     *
     * @param a primeira condição booleana
     * @param b segunda condição booleana, ignorada pelo NOT
     * @return resultado da operação lógica
     */
    public boolean aplicar(boolean a, boolean b) {
        return switch (this) {
            case AND -> a && b;
            case OR -> a || b;
            case NOT -> !a;
        };
    }
}