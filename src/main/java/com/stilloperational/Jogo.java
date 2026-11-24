package com.stilloperational;

import java.util.List;

/**
 * Gerencia as manutenções, a progressão e os tokens do jogador.
 */
public class Jogo {

    private final List<Manutencao> manutencoes = List.of(
            new Manutencao("Liberar a porta somente com cracha E autorizacao.", true, true, true, Operador.AND),
            new Manutencao("Acionar o alarme com fumaca OU temperatura alta.", false, true, true, Operador.OR),
            new Manutencao("Ligar a luz quando NAO houver claridade.", false, false, true, Operador.NOT)
    );

    private int fase = 0;
    private int tokens = 0;

    /**
     * @return verdadeiro quando todas as manutenções forem concluídas
     */
    public boolean terminou() {
        return fase >= manutencoes.size();
    }

    /**
     * Retorna a manutenção atual.
     *
     * @return manutenção que o jogador precisa resolver
     * @throws IllegalStateException se a partida estiver concluída
     */
    public Manutencao atual() {
        if (terminou()) {
            throw new IllegalStateException("A partida ja foi concluida.");
        }

        return manutencoes.get(fase);
    }

    /**
     * @return número da fase atual durante a partida
     */
    public int faseAtual() {
        return fase + 1;
    }

    /**
     * @return quantidade de tokens coletados
     */
    public int tokens() {
        return tokens;
    }

    /**
     * Valida a resposta do jogador. Caso esteja correta,
     * concede um token e avança para a próxima fase.
     *
     * @param operador operador escolhido pelo jogador
     * @return verdadeiro se a resposta estiver correta
     * @throws IllegalStateException se o jogo já estiver concluído
     */
    public boolean responder(Operador operador) {
        if (terminou()) throw new IllegalStateException("Todas as manutencoes foram concluidas.");
        if (!atual().verificar(operador)) return false;

        tokens++;
        fase++;
        return true;
    }
}