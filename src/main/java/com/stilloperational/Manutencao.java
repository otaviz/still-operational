package com.stilloperational;

/**
 * Representa um desafio de manutenção da instalação.
 *
 * @param descricao enunciado do desafio
 * @param entradaA primeira condição booleana
 * @param entradaB segunda condição booleana
 * @param resultadoEsperado resultado lógico esperado
 * @param operadorCorreto operador necessário para resolver o desafio
 */
public record Manutencao(String descricao, boolean entradaA, boolean entradaB,
                         boolean resultadoEsperado, Operador operadorCorreto) {

    /**
     * Verifica se o operador escolhido pelo jogador está correto
     * e produz o resultado esperado.
     *
     * @param escolhido operador selecionado pelo jogador
     * @return verdadeiro se a resposta estiver correta
     */
    public boolean verificar(Operador escolhido) {
        return escolhido == operadorCorreto
                && escolhido.aplicar(entradaA, entradaB) == resultadoEsperado;
    }
}