package com.stilloperational;

public record Manutencao(String descricao, boolean entradaA, boolean entradaB,
                         boolean resultadoEsperado, Operador operadorCorreto) {
    public boolean verificar(Operador escolhido) {
        return escolhido == operadorCorreto
                && escolhido.aplicar(entradaA, entradaB) == resultadoEsperado;
    }
}
