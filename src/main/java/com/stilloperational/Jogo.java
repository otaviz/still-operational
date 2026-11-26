package com.stilloperational;

import java.util.List;

/** Gerencia as manutenções, a progressão e os tokens do jogador. */
public class Jogo {
    private final List<Manutencao> manutencoes = List.of(
            new Manutencao("A porta só pode abrir se as duas credenciais forem válidas.",
                    true, true, true, Operador.AND),
            new Manutencao("Qualquer um dos sensores de perigo deve disparar o alarme.",
                    false, true, true, Operador.OR),
            new Manutencao("As lâmpadas devem acender apenas na ausência de claridade.",
                    false, false, true, Operador.NOT)
    );
    private int fase = 0;
    private int tokens = 0;

    /** @return verdadeiro quando todas as manutenções estiverem concluídas */
    public boolean terminou() { return fase >= manutencoes.size(); }

    /** @return manutenção atual
     * @throws IllegalStateException se a partida estiver concluída
     */
    public Manutencao atual() {
        if (terminou()) throw new IllegalStateException("A partida já foi concluída.");
        return manutencoes.get(fase);
    }

    /** @return número da fase atual */
    public int faseAtual() { return fase + 1; }

    /** @return quantidade de tokens coletados */
    public int tokens() { return tokens; }

    /** Valida a configuração antes de conceder um token e avançar.
     * @param operador módulo escolhido
     * @return verdadeiro se a configuração estiver correta
     */
    public boolean responder(Operador operador) {
        if (terminou()) throw new IllegalStateException("Todas as manutenções foram concluídas.");
        if (!atual().verificar(operador)) return false;
        tokens++;
        fase++;
        return true;
    }
}
