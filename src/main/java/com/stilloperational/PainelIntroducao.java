package com.stilloperational;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * Introdução narrativa com animação de digitação
 * e interface industrial.
 */
public class PainelIntroducao extends JPanel {

    private static final Color DOURADO =
            new Color(255, 190, 40);

    private static final Color METAL =
            new Color(65, 81, 95);

    private final String[] paginas = {

            """
            Em uma época muito distante, a inteligência artificial
            evoluiu a níveis impressionantes.

            Curaram doenças, revolucionaram a ciência, automatizaram
            indústrias inteiras e passaram a desempenhar inúmeras
            funções sem qualquer intervenção humana.

            E não, não dominaram o mundo.

            Nem tentaram.

            Pelo menos, não ainda.
            """,

            """
            Essas informações provavelmente fizeram você pensar
            que esta é uma história sobre o futuro da humanidade
            e como a inteligência artificial finalmente substituiu
            o homem.

            Mas essa não é a sua realidade.

            Você é um zelador.

            Seu trabalho é cuidar de uma instalação gigantesca,
            projetada para operar de maneira autônoma.
            """,

            """
            O problema?

            Seu chefe descobriu a inteligência artificial,
            mas nunca descobriu como utilizá-la.

            Depois de gastar todos os tokens disponíveis com
            instruções absurdamente específicas, redundantes
            e mal planejadas, ele conseguiu transformar uma
            das tecnologias mais avançadas da humanidade
            em um sistema incapaz de abrir uma porta.
            """,

            """
            Agora, aquele curso que você fez pela internet
            em 2025 é a última esperança dessa instalação.

            Você precisa de álgebra booleana.

            Boa sorte.

            Seu salário continua o mesmo.
            """
    };

    private final JLabel titulo = new JLabel(
            "RELATÓRIO OPERACIONAL",
            SwingConstants.CENTER
    );

    private final JTextArea historia = new JTextArea();

    private final JLabel instrucao = new JLabel(
            "CLIQUE PARA ACELERAR",
            SwingConstants.CENTER
    );

    private final BotaoPixel botao;
    private final BotaoPixel pular;

    private final Timer animacao;

    private int paginaAtual = 0;
    private int caracteres = 0;

    private long inicioTitulo;
    private long inicioDigitacao;
    private long inicioBotao = -1;

    private float opacidadeTitulo = 0f;
    private float opacidadeBotao = 0f;

    private boolean saindo = false;

    private static final int VELOCIDADE_DIGITACAO = 22;

    public PainelIntroducao(
            Runnable continuar,
            Runnable pularIntroducao) {

        setBackground(new Color(16, 19, 23));

        setLayout(null);

        // Título principal

        titulo.setFont(FonteJogo.tamanho(30));

        titulo.setForeground(
                new Color(255, 190, 40, 0)
        );

        add(titulo);

        // Texto narrativo

        historia.setFont(FonteJogo.tamanho(20));

        historia.setForeground(Color.WHITE);

        historia.setOpaque(false);
        historia.setEditable(false);
        historia.setFocusable(false);

        historia.setLineWrap(true);
        historia.setWrapStyleWord(true);

        historia.setBorder(
                BorderFactory.createEmptyBorder()
        );

        add(historia);

        // Instrução inferior

        instrucao.setFont(FonteJogo.tamanho(14));

        instrucao.setForeground(
                new Color(185, 199, 211)
        );

        add(instrucao);

        // Botão CONTINUAR animado

        botao = new BotaoPixel("CONTINUAR") {

            @Override
            public void paint(Graphics g) {

                Graphics2D g2 = (Graphics2D) g.create();

                g2.setComposite(
                        AlphaComposite.getInstance(
                                AlphaComposite.SRC_OVER,
                                opacidadeBotao
                        )
                );

                super.paint(g2);

                g2.dispose();
            }
        };

        botao.setVisible(false);

        botao.addActionListener(e -> {

            if (opacidadeBotao >= 1f && !saindo) {

                saindo = true;

                animacaoParar();

                continuar.run();
            }
        });

        add(botao);

        // Botão para pular toda a introdução

        pular = new BotaoPixel("PULAR");

        pular.setFont(FonteJogo.tamanho(22));

        pular.addActionListener(e -> {

            if (!saindo) {

                saindo = true;

                animacaoParar();

                pularIntroducao.run();
            }
        });

        add(pular);

        // Aceleração da narrativa

        MouseAdapter clique = new MouseAdapter() {

            @Override
            public void mouseClicked(MouseEvent e) {

                if (!saindo) {
                    acelerarTexto();
                }
            }
        };

        addMouseListener(clique);
        historia.addMouseListener(clique);
        titulo.addMouseListener(clique);
        instrucao.addMouseListener(clique);

        animacao = new Timer(
                16, e -> atualizarAnimacao()
        );
    }

    @Override
    public void addNotify() {

        super.addNotify();

        paginaAtual = 0;
        caracteres = 0;

        opacidadeTitulo = 0f;
        opacidadeBotao = 0f;

        inicioBotao = -1;

        saindo = false;

        historia.setText("");

        botao.setVisible(false);
        pular.setEnabled(true);

        titulo.setForeground(
                new Color(255, 190, 40, 0)
        );

        instrucao.setText("CLIQUE PARA ACELERAR");
        instrucao.setVisible(true);

        inicioTitulo = tempoAtual();

        inicioDigitacao = inicioTitulo + 1000;

        animacao.start();
    }

    @Override
    public void removeNotify() {

        animacao.stop();
        AudioJogo.pararDigitacao();

        super.removeNotify();
    }

    private long tempoAtual() {

        return System.nanoTime() / 1_000_000;
    }

    private void animacaoParar() {

        animacao.stop();
        AudioJogo.pararDigitacao();

        botao.setEnabled(false);
        pular.setEnabled(false);
    }

    /**
     * Atualiza a digitação e os efeitos de entrada.
     */
    private void atualizarAnimacao() {

        long agora = tempoAtual();

        // Fade-in do título

        opacidadeTitulo = fade(
                agora - inicioTitulo, 1000
        );

        titulo.setForeground(
                new Color(
                        255, 190, 40,
                        Math.round(opacidadeTitulo * 255)
                )
        );

        // Digitação gradual da narrativa

        if (agora >= inicioDigitacao) {

            String texto = paginas[paginaAtual];

            int quantidade = Math.min(
                    texto.length(),
                    (int) (
                            (agora - inicioDigitacao)
                                    / VELOCIDADE_DIGITACAO
                    )
            );

            if (quantidade > caracteres) {

                AudioJogo.tocarDigitacao(texto, caracteres, quantidade);
                caracteres = quantidade;

                historia.setText(
                        texto.substring(0, caracteres)
                );
            }

            if (caracteres >= texto.length()) {

                finalizarPagina();
            }
        }

        // Fade-in do botão final

        if (inicioBotao >= 0) {

            opacidadeBotao = fade(
                    agora - inicioBotao, 700
            );

            botao.setVisible(opacidadeBotao > 0f);

            botao.repaint();

            if (opacidadeBotao >= 1f
                    && opacidadeTitulo >= 1f) {

                animacao.stop();
            }
        }

        repaint();
    }

    /**
     * Revela imediatamente o texto ou avança
     * para a próxima página.
     */
    private void acelerarTexto() {

        String texto = paginas[paginaAtual];

        if (caracteres < texto.length()) {

            AudioJogo.pararDigitacao();
            caracteres = texto.length();

            historia.setText(texto);

            finalizarPagina();

        } else if (paginaAtual < paginas.length - 1) {

            paginaAtual++;

            caracteres = 0;

            historia.setText("");

            instrucao.setText(
                    "CLIQUE PARA ACELERAR"
            );

            instrucao.setVisible(true);

            inicioDigitacao = tempoAtual();

            animacao.start();
        }

        repaint();
    }

    private void finalizarPagina() {

        if (paginaAtual < paginas.length - 1) {

            instrucao.setText(
                    "CLIQUE PARA AVANÇAR"
            );

        } else {

            instrucao.setVisible(false);

            if (inicioBotao < 0) {

                inicioBotao = tempoAtual();
            }
        }
    }

    private float fade(long tempo, int duracao) {

        float progresso = Math.max(
                0f,
                Math.min(
                        1f,
                        tempo / (float) duracao
                )
        );

        return progresso * progresso
                * (3f - 2f * progresso);
    }

    @Override
    public void doLayout() {

        int w = getWidth();
        int h = getHeight();

        titulo.setBounds(
                210, 35,
                w - 420, 60
        );

        historia.setBounds(
                85, 132,
                w - 170,
                h - 250
        );

        botao.setBounds(
                (w - 280) / 2,
                h - 95,
                280, 60
        );

        pular.setBounds(
                w - 220,
                h - 95,
                180, 55
        );

        instrucao.setBounds(
                (w - 280) / 2,
                h - 85,
                280, 42
        );
    }

    @Override
    protected void paintComponent(Graphics g) {

        super.paintComponent(g);

        Graphics2D g2 = (Graphics2D) g.create();

        g2.setRenderingHint(
                RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_OFF
        );

        desenharFundo(g2);

        desenharMolduraExterna(g2);

        desenharCabecalho(g2);

        desenharCaixaNarrativa(g2);

        desenharRodape(g2);

        g2.dispose();
    }

    /**
     * Textura discreta do fundo industrial.
     */
    private void desenharFundo(Graphics2D g2) {

        int w = getWidth();
        int h = getHeight();

        g2.setColor(new Color(16, 19, 23));

        g2.fillRect(0, 0, w, h);

        g2.setColor(new Color(22, 26, 31));

        for (int y = 0; y < h; y += 6) {

            g2.fillRect(0, y, w, 1);
        }

        g2.setColor(new Color(27, 32, 38));

        g2.fillRect(0, 0, 12, h);
        g2.fillRect(w - 12, 0, 12, h);
    }

    /**
     * Estrutura metálica externa da interface.
     */
    private void desenharMolduraExterna(Graphics2D g2) {

        int w = getWidth();
        int h = getHeight();

        g2.setColor(new Color(5, 8, 11));

        g2.drawRect(
                17, 17,
                w - 35, h - 35
        );

        g2.setColor(METAL);

        g2.drawRect(
                20, 20,
                w - 41, h - 41
        );

        g2.setColor(new Color(36, 47, 57));

        g2.drawRect(
                23, 23,
                w - 47, h - 47
        );

        // Cantos reforçados

        desenharCanto(g2, 20, 20, 1, 1);

        desenharCanto(
                g2, w - 21, 20, -1, 1
        );

        desenharCanto(
                g2, 20, h - 21, 1, -1
        );

        desenharCanto(
                g2, w - 21, h - 21, -1, -1
        );
    }

    private void desenharCanto(
            Graphics2D g2,
            int x, int y,
            int direcaoX,
            int direcaoY) {

        Graphics2D canto = (Graphics2D) g2.create();

        canto.translate(x, y);

        canto.scale(direcaoX, direcaoY);

        canto.setColor(new Color(43, 54, 65));

        canto.fillRect(0, 0, 42, 7);
        canto.fillRect(0, 0, 7, 42);

        canto.setColor(new Color(197, 154, 56));

        canto.fillRect(0, 0, 35, 3);
        canto.fillRect(0, 0, 3, 35);

        canto.dispose();
    }

    /**
     * Placa metálica do título e indicador de página.
     */
    private void desenharCabecalho(Graphics2D g) {

        Graphics2D g2 = (Graphics2D) g.create();

        int w = getWidth();

        g2.setComposite(
                AlphaComposite.getInstance(
                        AlphaComposite.SRC_OVER,
                        opacidadeTitulo
                )
        );

        // Placa principal

        desenharPlaca(
                g2,
                195, 35,
                w - 390, 60,
                DOURADO
        );

        // Identificação do documento

        desenharPlaca(
                g2,
                45, 42,
                135, 46,
                METAL
        );

        g2.setColor(DOURADO);

        g2.setFont(FonteJogo.tamanho(12));

        centralizar(
                g2,
                "ARQUIVO",
                112, 59
        );

        g2.setColor(Color.WHITE);

        g2.setFont(FonteJogo.tamanho(13));

        centralizar(
                g2,
                "SO - 001",
                112, 76
        );

        // Indicador da página

        desenharPlaca(
                g2,
                w - 180, 42,
                135, 46,
                METAL
        );

        g2.setColor(DOURADO);

        g2.setFont(FonteJogo.tamanho(12));

        centralizar(
                g2,
                "PÁGINA",
                w - 113, 59
        );

        g2.setColor(Color.WHITE);

        g2.setFont(FonteJogo.tamanho(15));

        centralizar(
                g2,
                String.format(
                        "%02d / %02d",
                        paginaAtual + 1,
                        paginas.length
                ),
                w - 113, 78
        );

        g2.dispose();
    }

    /**
     * Caixa narrativa com estrutura industrial.
     */
    private void desenharCaixaNarrativa(Graphics2D g2) {

        int w = getWidth();
        int h = getHeight();

        int x = 60;
        int y = 110;

        int largura = w - 120;
        int altura = h - 220;

        desenharPlaca(
                g2,
                x, y,
                largura, altura,
                METAL
        );

        // Área interna de leitura

        g2.setColor(new Color(13, 18, 23));

        g2.fillRect(
                x + 15, y + 13,
                largura - 30,
                altura - 26
        );

        g2.setColor(new Color(32, 43, 53));

        g2.drawRect(
                x + 15, y + 13,
                largura - 31,
                altura - 27
        );

        // Detalhes dourados nas laterais

        g2.setColor(new Color(197, 154, 56));

        g2.fillRect(
                x + 5, y + 35,
                4, 35
        );

        g2.fillRect(
                x + largura - 9,
                y + 35,
                4, 35
        );

        // Pequenos encaixes metálicos

        g2.setColor(new Color(83, 103, 120));

        for (int i = 0; i < 3; i++) {

            g2.fillRect(
                    x + 5,
                    y + altura - 60 + i * 10,
                    4, 5
            );

            g2.fillRect(
                    x + largura - 9,
                    y + altura - 60 + i * 10,
                    4, 5
            );
        }
    }

    /**
     * Painel inferior e progresso da leitura.
     */
    private void desenharRodape(Graphics2D g2) {

        int w = getWidth();
        int h = getHeight();

        // Indicadores das páginas

        desenharPlaca(
                g2,
                40, h - 85,
                155, 42,
                METAL
        );

        for (int i = 0; i < paginas.length; i++) {

            int x = 56 + i * 32;
            int y = h - 70;

            g2.setColor(new Color(5, 9, 12));

            g2.fillRect(x, y, 25, 12);

            if (i < paginaAtual) {

                g2.setColor(
                        new Color(197, 154, 56)
                );

            } else if (i == paginaAtual) {

                g2.setColor(DOURADO);

            } else {

                g2.setColor(
                        new Color(48, 61, 72)
                );
            }

            g2.fillRect(
                    x + 2, y + 2,
                    21, 8
            );
        }

        // Caixa da instrução

        if (instrucao.isVisible()) {

            desenharPlaca(
                    g2,
                    (w - 280) / 2,
                    h - 85,
                    280, 42,
                    METAL
            );
        }
    }

    /**
     * Renderiza uma placa metálica reutilizável.
     */
    private void desenharPlaca(
            Graphics2D g2,
            int x, int y,
            int largura, int altura,
            Color detalhe) {

        // Sombra externa

        g2.setColor(new Color(4, 7, 10));

        g2.fillRect(
                x + 4, y + 4,
                largura, altura
        );

        // Contorno escuro

        g2.setColor(new Color(8, 12, 16));

        g2.fillRect(
                x, y,
                largura, altura
        );

        // Estrutura metálica

        g2.setColor(new Color(55, 70, 83));

        g2.fillRect(
                x + 2, y + 2,
                largura - 4,
                altura - 4
        );

        // Face interna

        g2.setColor(new Color(24, 32, 40));

        g2.fillRect(
                x + 7, y + 7,
                largura - 14,
                altura - 14
        );

        // Reflexo superior

        g2.setColor(new Color(98, 119, 136));

        g2.fillRect(
                x + 3, y + 3,
                largura - 6, 2
        );

        g2.fillRect(
                x + 3, y + 3,
                2, altura - 6
        );

        // Sombra inferior

        g2.setColor(new Color(15, 22, 28));

        g2.fillRect(
                x + 3,
                y + altura - 5,
                largura - 6, 3
        );

        // Detalhe central superior

        g2.setColor(detalhe);

        g2.fillRect(
                x + largura / 2 - 28,
                y + 2,
                56, 3
        );

        // Parafusos

        desenharParafuso(
                g2, x + 9, y + 9
        );

        desenharParafuso(
                g2, x + largura - 14, y + 9
        );

        desenharParafuso(
                g2, x + 9, y + altura - 14
        );

        desenharParafuso(
                g2,
                x + largura - 14,
                y + altura - 14
        );
    }

    private void desenharParafuso(
            Graphics2D g2,
            int x, int y) {

        g2.setColor(new Color(6, 10, 14));

        g2.fillRect(x - 1, y - 1, 8, 8);

        g2.setColor(new Color(99, 118, 133));

        g2.fillRect(x, y, 6, 6);

        g2.setColor(new Color(153, 169, 181));

        g2.fillRect(x, y, 6, 1);

        g2.setColor(new Color(28, 38, 47));

        g2.fillRect(x + 1, y + 2, 4, 2);
    }

    private void centralizar(
            Graphics2D g2,
            String texto,
            int centroX, int y) {

        int larguraTexto = g2
                .getFontMetrics()
                .stringWidth(texto);

        g2.drawString(
                texto,
                centroX - larguraTexto / 2,
                y
        );
    }
}
