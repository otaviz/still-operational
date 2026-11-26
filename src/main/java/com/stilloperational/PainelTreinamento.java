package com.stilloperational;

import javax.swing.*;
import java.awt.*;

/**
 * Treinamento interativo dos conceitos fundamentais
 * da lógica booleana.
 */
public class PainelTreinamento extends JPanel {

    private static final Color DOURADO = new Color(255, 190, 40);
    private static final Color VERDE = new Color(80, 220, 120);
    private static final Color METAL = new Color(65, 81, 95);
    private static final Color INATIVO = new Color(130, 145, 158);

    private final Image fundo = new ImageIcon(
            getClass().getResource("/fundo.png")
    ).getImage();

    private final String[] titulos = {
            "VERDADEIRO E FALSO",
            "OPERADOR AND (E)",
            "OPERADOR OR (OU)",
            "OPERADOR NOT (NÃO)"
    };

    private final String[][] explicacoes = {

            {
                    "Na lógica booleana, uma condição pode ser verdadeira ou falsa.",
                    "Representamos esses valores por TRUE (1) e FALSE (0).",
                    "Altere o estado da entrada e observe o resultado."
            },

            {
                    "AND significa E. As duas condições precisam ser verdadeiras.",
                    "Imagine uma porta que exige um crachá e uma autorização.",
                    "Experimente ligar as duas entradas."
            },

            {
                    "OR significa OU. Pelo menos uma condição deve ser verdadeira.",
                    "Um alarme pode detectar fumaça, calor ou ambos.",
                    "Experimente diferentes combinações de entradas."
            },

            {
                    "NOT significa NÃO. Ele inverte o valor de uma condição.",
                    "Se a entrada for verdadeira, o resultado será falso.",
                    "Se a entrada for falsa, o resultado será verdadeiro."
            }
    };

    private int etapa = 0;

    private boolean entradaA = false;
    private boolean entradaB = false;

    private final BotaoPixel botaoA;
    private final BotaoPixel botaoB;
    private final BotaoPixel anterior;
    private final BotaoPixel proximo;
    private final BotaoPixel pular;

    public PainelTreinamento(Runnable finalizar) {

        setBackground(new Color(16, 19, 23));
        setLayout(null);

        botaoA = criarBotao("A: 0", 20);
        botaoB = criarBotao("B: 0", 20);
        botaoA.setSomPadraoAtivo(false);
        botaoB.setSomPadraoAtivo(false);

        anterior = criarBotao("VOLTAR", 20);
        proximo = criarBotao("PRÓXIMO", 22);
        pular = criarBotao("PULAR", 22);

        botaoA.addActionListener(e -> {

            entradaA = !entradaA;
            AudioJogo.tocar("entrada.wav");
            atualizarInterface();
        });

        botaoB.addActionListener(e -> {

            entradaB = !entradaB;
            AudioJogo.tocar("entrada.wav");
            atualizarInterface();
        });

        anterior.addActionListener(e -> {

            if (etapa > 0) {

                etapa--;
                atualizarInterface();
            }
        });

        proximo.addActionListener(e -> {

            if (etapa < titulos.length - 1) {

                etapa++;
                atualizarInterface();

            } else {

                finalizar.run();
            }
        });

        pular.addActionListener(e -> finalizar.run());

        add(botaoA);
        add(botaoB);
        add(anterior);
        add(proximo);
        add(pular);

        atualizarInterface();
    }

    private BotaoPixel criarBotao(String texto, int tamanho) {

        BotaoPixel botao = new BotaoPixel(texto);

        botao.setFont(FonteJogo.tamanho(tamanho));

        return botao;
    }

    /**
     * Atualiza os controles de acordo com a etapa.
     */
    private void atualizarInterface() {

        botaoA.setText(
                entradaA ? "A: 1 - TRUE" : "A: 0 - FALSE"
        );

        botaoB.setText(
                entradaB ? "B: 1 - TRUE" : "B: 0 - FALSE"
        );

        botaoB.setVisible(usaDuasEntradas());

        anterior.setEnabled(etapa > 0);

        proximo.setText(
                etapa == titulos.length - 1
                        ? "INICIAR MANUTENÇÕES"
                        : "PRÓXIMO"
        );

        revalidate();
        repaint();
    }

    private boolean usaDuasEntradas() {

        return etapa == 1 || etapa == 2;
    }

    /**
     * Reutiliza os operadores do núcleo lógico do jogo.
     */
    private boolean calcularResultado() {

        return switch (etapa) {

            case 1 -> Operador.AND.aplicar(entradaA, entradaB);

            case 2 -> Operador.OR.aplicar(entradaA, entradaB);

            case 3 -> Operador.NOT.aplicar(entradaA, false);

            default -> entradaA;
        };
    }

    private String operadorAtual() {

        return switch (etapa) {

            case 1 -> "AND";
            case 2 -> "OR";
            case 3 -> "NOT";
            default -> "ESTADO";
        };
    }

    @Override
    public void doLayout() {

        int x = (getWidth() - 960) / 2;
        int y = (getHeight() - 540) / 2;

        botaoA.setBounds(
                x + 100,
                y + (usaDuasEntradas() ? 265 : 300),
                210, 52
        );

        botaoB.setBounds(
                x + 100, y + 335,
                210, 52
        );

        anterior.setBounds(
                x + 60, y + 450,
                160, 55
        );

        proximo.setBounds(
                x + 300, y + 445,
                360, 60
        );

        pular.setBounds(
                x + 680, y + 450,
                220, 55
        );
    }

    @Override
    protected void paintComponent(Graphics g) {

        super.paintComponent(g);

        Graphics2D g2 = (Graphics2D) g.create();

        g2.setRenderingHint(
                RenderingHints.KEY_INTERPOLATION,
                RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR
        );

        g2.setRenderingHint(
                RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_OFF
        );

        g2.setRenderingHint(
                RenderingHints.KEY_TEXT_ANTIALIASING,
                RenderingHints.VALUE_TEXT_ANTIALIAS_OFF
        );

        // Cenário da instalação

        g2.drawImage(
                fundo, 0, 0,
                getWidth(), getHeight(), this
        );

        g2.setColor(new Color(0, 0, 0, 190));

        g2.fillRect(
                0, 0,
                getWidth(), getHeight()
        );

        g2.translate(
                (getWidth() - 960) / 2,
                (getHeight() - 540) / 2
        );

        desenharMoldura(g2);
        desenharCabecalho(g2);
        desenharExplicacao(g2);
        desenharSimulador(g2);

        g2.dispose();
    }

    private void desenharMoldura(Graphics2D g2) {

        g2.setColor(METAL);
        g2.drawRect(20, 20, 919, 499);

        g2.setColor(DOURADO);

        for (int x : new int[]{20, 905}) {

            g2.fillRect(x, 20, 35, 3);
            g2.fillRect(x, 517, 35, 3);
        }

        g2.fillRect(20, 20, 3, 35);
        g2.fillRect(937, 20, 3, 35);
        g2.fillRect(20, 485, 3, 35);
        g2.fillRect(937, 485, 3, 35);
    }

    private void desenharCabecalho(Graphics2D g2) {

        desenharPlaca(
                g2, 60, 32,
                840, 60, DOURADO
        );

        textoCentralizado(
                g2,
                "TREINAMENTO OPERACIONAL",
                480, 56, 23,
                DOURADO, 780
        );

        textoCentralizado(
                g2,
                String.format(
                        "%02d / 04 - %s",
                        etapa + 1,
                        titulos[etapa]
                ),
                480, 80, 16,
                Color.WHITE, 780
        );
    }

    private void desenharExplicacao(Graphics2D g2) {

        desenharPlaca(
                g2, 60, 104,
                840, 122, METAL
        );

        // Identificação do personagem

        desenharPlaca(
                g2, 80, 113,
                145, 26, DOURADO
        );

        textoCentralizado(
                g2, "ZELADOR",
                152, 131, 15,
                DOURADO, 125
        );

        // Conteúdo educativo

        int y = 159;

        for (String linha : explicacoes[etapa]) {

            textoCentralizado(
                    g2, linha,
                    480, y, 17,
                    Color.WHITE, 790
            );

            y += 24;
        }
    }

    /**
     * Exibe as entradas, a operação e o resultado.
     */
    private void desenharSimulador(Graphics2D g2) {

        desenharPlaca(
                g2, 60, 238,
                840, 188, METAL
        );

        boolean resultado = calcularResultado();

        Color corA = entradaA ? VERDE : INATIVO;
        Color corB = entradaB ? VERDE : INATIVO;

        Color corResultado = resultado
                ? VERDE
                : INATIVO;

        // Conexão entre a entrada A e a operação

        int yA = usaDuasEntradas() ? 291 : 326;

        g2.setStroke(new BasicStroke(4));

        g2.setColor(corA);

        g2.drawLine(310, yA, 350, yA);
        g2.drawLine(350, yA, 350, 326);
        g2.drawLine(350, 326, 390, 326);

        // Conexão entre a entrada B e a operação

        if (usaDuasEntradas()) {

            g2.setColor(corB);

            g2.drawLine(310, 361, 370, 361);
            g2.drawLine(370, 361, 370, 347);
            g2.drawLine(370, 347, 390, 347);
        }

        // Conexão com o resultado

        g2.setColor(corResultado);

        g2.drawLine(550, 326, 640, 326);

        g2.setStroke(new BasicStroke(1));

        // Identificação do operador

        desenharPlaca(
                g2, 390, 283,
                160, 86, DOURADO
        );

        textoCentralizado(
                g2,
                operadorAtual(),
                470, 337, 30,
                DOURADO, 130
        );

        // Resultado da operação

        desenharPlaca(
                g2, 640, 265,
                220, 122, corResultado
        );

        textoCentralizado(
                g2, "RESULTADO",
                750, 290, 17,
                DOURADO, 185
        );

        // Indicador luminoso

        g2.setColor(new Color(5, 10, 13));

        g2.fillRect(733, 301, 34, 29);

        g2.setColor(
                resultado
                        ? VERDE
                        : new Color(50, 62, 72)
        );

        g2.fillRect(738, 306, 24, 19);

        if (resultado) {

            g2.setColor(new Color(175, 255, 195));

            g2.fillRect(740, 308, 20, 4);
        }

        textoCentralizado(
                g2,
                resultado ? "TRUE / 1" : "FALSE / 0",
                750, 353, 21,
                corResultado, 185
        );

        textoCentralizado(
                g2,
                resultado ? "VERDADEIRO" : "FALSO",
                750, 375, 14,
                Color.WHITE, 185
        );
    }

    /**
     * Renderiza placas metálicas com detalhes industriais.
     */
    private void desenharPlaca(
            Graphics2D g2,
            int x, int y,
            int largura, int altura,
            Color detalhe) {

        // Sombra

        g2.setColor(new Color(4, 7, 10));

        g2.fillRect(
                x + 4, y + 4,
                largura, altura
        );

        // Contorno

        g2.setColor(new Color(8, 12, 16));

        g2.fillRect(x, y, largura, altura);

        // Estrutura metálica

        g2.setColor(new Color(55, 70, 83));

        g2.fillRect(
                x + 2, y + 2,
                largura - 4, altura - 4
        );

        // Interior

        g2.setColor(new Color(13, 18, 23));

        g2.fillRect(
                x + 7, y + 7,
                largura - 14, altura - 14
        );

        // Relevo superior

        g2.setColor(new Color(98, 119, 136));

        g2.fillRect(
                x + 3, y + 3,
                largura - 6, 2
        );

        g2.fillRect(
                x + 3, y + 3,
                2, altura - 6
        );

        // Detalhe colorido

        g2.setColor(detalhe);

        g2.fillRect(
                x + largura / 2 - 28,
                y + 2,
                56, 3
        );

        // Parafusos

        desenharParafuso(g2, x + 9, y + 9);

        desenharParafuso(
                g2, x + largura - 15, y + 9
        );

        desenharParafuso(
                g2, x + 9, y + altura - 15
        );

        desenharParafuso(
                g2,
                x + largura - 15,
                y + altura - 15
        );
    }

    private void desenharParafuso(
            Graphics2D g2, int x, int y) {

        g2.setColor(new Color(6, 10, 14));

        g2.fillRect(x - 1, y - 1, 8, 8);

        g2.setColor(new Color(99, 118, 133));

        g2.fillRect(x, y, 6, 6);

        g2.setColor(new Color(28, 38, 47));

        g2.fillRect(x + 1, y + 2, 4, 2);
    }

    /**
     * Centraliza o texto e ajusta o tamanho da fonte
     * para respeitar os limites das placas.
     */
    private void textoCentralizado(
            Graphics2D g2,
            String texto,
            int x, int y,
            int tamanho,
            Color cor,
            int larguraMaxima) {

        g2.setFont(FonteJogo.tamanho(tamanho));

        while (g2.getFontMetrics().stringWidth(texto)
                > larguraMaxima && tamanho > 10) {

            tamanho--;

            g2.setFont(FonteJogo.tamanho(tamanho));
        }

        g2.setColor(cor);

        int largura = g2.getFontMetrics().stringWidth(texto);

        g2.drawString(
                texto,
                x - largura / 2,
                y
        );
    }
}
