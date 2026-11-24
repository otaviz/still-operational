package com.stilloperational;

import java.util.Scanner;

/**
 * Classe principal do Still Operational.
 * Controla a interação entre o jogador e o jogo pelo console.
 */
public class Main {

    /**
     * Inicia a partida, recebe as respostas do jogador
     * e apresenta os resultados das manutenções.
     *
     * @param args argumentos de execução do programa
     */
    public static void main(String[] args) {
        Jogo jogo = new Jogo();
        Scanner scanner = new Scanner(System.in);

        System.out.println("STILL OPERATIONAL - Prototipo funcional");
        System.out.println("A instalacao automatizada precisa de um zelador que saiba logica booleana.");

        while (!jogo.terminou()) {
            System.out.println("\nManutencao " + jogo.faseAtual() + ": " + jogo.atual().descricao());
            System.out.print("Escolha AND, OR ou NOT (ou SAIR): ");

            String texto = scanner.nextLine().trim().toUpperCase();

            if (texto.equals("SAIR")) break;

            try {
                Operador escolhido = Operador.valueOf(texto);
                System.out.println(jogo.responder(escolhido) ? "Correto! Token coletado." : "Incorreto. Tente novamente.");
            } catch (IllegalArgumentException e) {
                System.out.println("Operador invalido. Use AND, OR ou NOT.");
            }
        }

        System.out.println("Tokens: " + jogo.tokens());

        if (jogo.terminou()) System.out.println("Instalacao estabilizada. Todas as manutencoes concluidas!");
    }
}