package com.stilloperational;

import java.util.List;

public class Jogo {
    private final List<Manutencao> manutencoes = List.of(
            new Manutencao("Liberar a porta somente com cracha E autorizacao.", true, true, true, Operador.AND),
            new Manutencao("Acionar o alarme com fumaca OU temperatura alta.", false, true, true, Operador.OR),
            new Manutencao("Ligar a luz quando NAO houver claridade.", false, false, true, Operador.NOT)
    );
    private int fase = 0;
    private int tokens = 0;

    public boolean terminou() { return fase >= manutencoes.size(); }
    public Manutencao atual() { return manutencoes.get(fase); }
    public int faseAtual() { return fase + 1; }
    public int tokens() { return tokens; }

    public boolean responder(Operador operador) {
        if (terminou()) throw new IllegalStateException("Todas as manutencoes foram concluidas.");
        if (!atual().verificar(operador)) return false;
        tokens++;
        fase++;
        return true;
    }
}
