package com.stilloperational;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * Tela reutilizável de apresentação de personagens
 * e equipamentos com interface industrial.
 */
public class PainelApresentacao extends JPanel {

    private static final Color DOURADO =
            new Color(255, 190, 40);

    private static final Color METAL =
            new Color(65, 81, 95);

    private final String titulo;
    private final String[] texto;
    private final Image imagem;

    private final int larguraMaxima;
    private final int alturaMaxima;

    private final BotaoPixel continuar;
    private final BotaoPixel pular;

    private final Timer animacao;

    private long inicio;

    private boolean saindo = false;

    private float opacidadeTitulo = 0f;
    private float opacidadeImagem = 0f;
    private float opacidadeTexto = 0f;
    private float opacidadeBotao = 0f;

    private double escalaImagem = 0;

    public PainelApresentacao(
            String titulo,
            String[] texto,
            String arquivo,
            int larguraMaxima,
            int alturaMaxima,
            Runnable proximo,
            Runnable pularIntroducao) {

        this.titulo = titulo;
        this.texto = texto;
        this.larguraMaxima = larguraMaxima;
        this.alturaMaxima = alturaMaxima;

        imagem = new ImageIcon(
                getClass().getResource(arquivo)
        ).getImage();

        setBackground(new Color(16, 19, 23));
        setLayout(null);

        // Botão CONTINUAR com fade-in

        continuar = new BotaoPixel("CONTINUAR") {

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

        continuar.setVisible(false);

        continuar.addActionListener(e -> {

            if (opacidadeBotao >= 1f && !saindo) {

                sairDaTela(proximo);
            }
        });

        add(continuar);

        // Botão PULAR

        pular = new BotaoPixel("PULAR");

        pular.setFont(FonteJogo.tamanho(22));

        pular.addActionListener(e -> {

            if (!saindo) {

                sairDaTela(pularIntroducao);
            }
        });

        add(pular);

        // Aceleração das animações por clique.
        // Nenhuma instrução será exibida na interface.

        addMouseListener(new MouseAdapter() {

            @Override
            public void mouseClicked(MouseEvent e) {

                if (!saindo
                        && SwingUtilities.isLeftMouseButton(e)) {

                    acelerarAnimacao();
                }
            }
        });

        animacao = new Timer(
                16, e -> atualizar()
        );
    }

    @Override
    public void addNotify() {

        super.addNotify();

        inicio = System.nanoTime();

        saindo = false;

        opacidadeTitulo = 0f;
        opacidadeImagem = 0f;
        opacidadeTexto = 0f;
        opacidadeBotao = 0f;

        escalaImagem = 0;

        continuar.setEnabled(false);
        continuar.setVisible(false);

        pular.setEnabled(true);

        animacao.start();
    }

    @Override
    public void removeNotify() {

        animacao.stop();

        super.removeNotify();
    }

    /**
     * Encerra as animações antes de trocar de tela.
     */
    private void sairDaTela(Runnable destino) {

        saindo = true;

        animacao.stop();

        continuar.setEnabled(false);
        pular.setEnabled(false);

        destino.run();
    }

    /**
     * Finaliza imediatamente todas as animações.
     */
    private void acelerarAnimacao() {

        opacidadeTitulo = 1f;
        opacidadeImagem = 1f;
        opacidadeTexto = 1f;
        opacidadeBotao = 1f;

        escalaImagem = 1;

        continuar.setVisible(true);
        continuar.setEnabled(true);

        animacao.stop();

        revalidate();
        repaint();
    }

    /**
     * Controla a sequência de apresentação.
     */
    private void atualizar() {

        long tempo = (
                System.nanoTime() - inicio
        ) / 1_000_000;

        // Fade-in do cabeçalho

        opacidadeTitulo = fade(
                tempo, 0, 700
        );

        // Entrada da imagem

        opacidadeImagem = fade(
                tempo, 700, 250
        );

        // Efeito POP

        double p = progresso(
                tempo, 700, 650
        );

        double intensidade = 1.70158;

        escalaImagem = 1
                + (intensidade + 1)
                * Math.pow(p - 1, 3)
                + intensidade
                * Math.pow(p - 1, 2);

        // Entrada da caixa de texto

        opacidadeTexto = fade(
                tempo, 1400, 800
        );

        // Entrada do botão

        opacidadeBotao = fade(
                tempo, 2200, 600
        );

        continuar.setVisible(
                opacidadeBotao > 0f
        );

        continuar.setEnabled(
                opacidadeBotao >= 1f
        );

        continuar.repaint();

        if (tempo >= 2800) {

            animacao.stop();
        }

        repaint();
    }

    private float progresso(
            long tempo, int atraso, int duracao) {

        return Math.max(
                0f,
                Math.min(
                        1f,
                        (tempo - atraso) / (float) duracao
                )
        );
    }

    private float fade(
            long tempo, int atraso, int duracao) {

        float p = progresso(
                tempo, atraso, duracao
        );

        return p * p * (3f - 2f * p);
    }

    @Override
    public void doLayout() {

        int w = getWidth();
        int h = getHeight();

        continuar.setBounds(
                (w - 280) / 2,
                h - 95,
                280, 60
        );

        pular.setBounds(
                w - 220,
                h - 95,
                180, 55
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

        g2.setRenderingHint(
                RenderingHints.KEY_INTERPOLATION,
                RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR
        );

        g2.setRenderingHint(
                RenderingHints.KEY_TEXT_ANTIALIASING,
                RenderingHints.VALUE_TEXT_ANTIALIAS_OFF
        );

        desenharFundo(g2);

        desenharMolduraExterna(g2);

        desenharCabecalho(g2);

        desenharCompartimento(g2);

        desenharImagem(g2);

        desenharDescricao(g2);

        g2.dispose();
    }

    /**
     * Textura industrial do fundo.
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
     * Moldura metálica externa.
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
     * Placa metálica que identifica a apresentação.
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

        int largura = w - 240;

        desenharPlaca(
                g2,
                120, 35,
                largura, 55,
                DOURADO
        );

        g2.setColor(new Color(7, 10, 14));

        g2.fillRect(
                136, 46,
                largura - 32, 33
        );

        g2.setFont(
                FonteJogo.tamanho(30)
        );

        ajustarFonte(
                g2,
                titulo,
                30,
                largura - 60
        );

        // Sombra do título

        g2.setColor(new Color(91, 62, 15));

        centralizar(
                g2,
                titulo,
                w / 2 + 2,
                73
        );

        // Título dourado

        g2.setColor(DOURADO);

        centralizar(
                g2,
                titulo,
                w / 2,
                71
        );

        g2.dispose();
    }

    /**
     * Compartimento visual do personagem ou equipamento.
     */
    private void desenharCompartimento(Graphics2D g) {

        Graphics2D g2 = (Graphics2D) g.create();

        int centroX = getWidth() / 2;

        int largura = 430;

        int x = centroX - largura / 2;
        int y = 103;
        int altura = 207;

        g2.setComposite(
                AlphaComposite.getInstance(
                        AlphaComposite.SRC_OVER,
                        opacidadeTitulo
                )
        );

        desenharPlaca(
                g2,
                x, y,
                largura, altura,
                METAL
        );

        // Interior escuro

        g2.setColor(new Color(12, 17, 22));

        g2.fillRect(
                x + 15, y + 13,
                largura - 30,
                altura - 26
        );

        // Grade de inspeção

        g2.setColor(new Color(21, 29, 36));

        for (int linhaX = x + 20;
             linhaX < x + largura - 15;
             linhaX += 24) {

            g2.fillRect(
                    linhaX,
                    y + 13,
                    1,
                    altura - 26
            );
        }

        for (int linhaY = y + 18;
             linhaY < y + altura - 13;
             linhaY += 24) {

            g2.fillRect(
                    x + 15,
                    linhaY,
                    largura - 30,
                    1
            );
        }

        // Marcações laterais

        for (int i = 0; i < 7; i++) {

            g2.setColor(
                    i == 3
                            ? DOURADO
                            : new Color(65, 81, 95)
            );

            int tamanho = i == 3 ? 12 : 6;

            g2.fillRect(
                    x + 20,
                    y + 37 + i * 22,
                    tamanho, 2
            );

            g2.fillRect(
                    x + largura - 20 - tamanho,
                    y + 37 + i * 22,
                    tamanho, 2
            );
        }

        // Detalhes dourados nos cantos internos

        g2.setColor(new Color(197, 154, 56));

        g2.fillRect(
                x + 15, y + 13,
                24, 3
        );

        g2.fillRect(
                x + 15, y + 13,
                3, 24
        );

        g2.fillRect(
                x + largura - 39,
                y + altura - 16,
                24, 3
        );

        g2.fillRect(
                x + largura - 18,
                y + altura - 37,
                3, 24
        );

        g2.dispose();
    }

    /**
     * Exibe a imagem com efeito POP.
     */
    private void desenharImagem(Graphics2D g) {

        Graphics2D g2 = (Graphics2D) g.create();

        g2.setComposite(
                AlphaComposite.getInstance(
                        AlphaComposite.SRC_OVER,
                        opacidadeImagem
                )
        );

        int originalW = imagem.getWidth(this);
        int originalH = imagem.getHeight(this);

        if (originalW > 0 && originalH > 0) {

            // Limita a altura para respeitar a moldura
            // durante a expansão do efeito POP.

            double alturaPermitida = Math.min(
                    alturaMaxima, 165
            );

            double proporcao = Math.min(
                    larguraMaxima / (double) originalW,
                    alturaPermitida / originalH
            );

            int imagemW = (int) (
                    originalW * proporcao * escalaImagem
            );

            int imagemH = (int) (
                    originalH * proporcao * escalaImagem
            );

            int x = (getWidth() - imagemW) / 2;

            int y = 206 - imagemH / 2;

            g2.drawImage(
                    imagem,
                    x, y,
                    imagemW, imagemH,
                    this
            );
        }

        g2.dispose();
    }

    /**
     * Caixa metálica de descrição.
     */
    private void desenharDescricao(Graphics2D g) {

        Graphics2D g2 = (Graphics2D) g.create();

        int w = getWidth();

        int x = 60;
        int y = 318;

        int largura = w - 120;
        int altura = 114;

        g2.setComposite(
                AlphaComposite.getInstance(
                        AlphaComposite.SRC_OVER,
                        opacidadeTexto
                )
        );

        desenharPlaca(
                g2,
                x, y,
                largura, altura,
                DOURADO
        );

        // Fundo interno da caixa

        g2.setColor(new Color(13, 18, 23));

        g2.fillRect(
                x + 15, y + 12,
                largura - 30,
                altura - 24
        );

        // Detalhes laterais dourados

        g2.setColor(new Color(197, 154, 56));

        g2.fillRect(
                x + 5, y + 30,
                4, 32
        );

        g2.fillRect(
                x + largura - 9,
                y + 30,
                4, 32
        );

        // Ajuste automático do tamanho da fonte

        int tamanhoFonte = 20;

        while (tamanhoFonte > 12) {

            g2.setFont(
                    FonteJogo.tamanho(tamanhoFonte)
            );

            FontMetrics fm = g2.getFontMetrics();

            boolean cabe = true;

            for (String linha : texto) {

                if (fm.stringWidth(linha)
                        > largura - 65) {

                    cabe = false;
                    break;
                }
            }

            if (cabe && texto.length * fm.getHeight()
                    <= altura - 30) {

                break;
            }

            tamanhoFonte--;
        }

        g2.setFont(
                FonteJogo.tamanho(tamanhoFonte)
        );

        g2.setColor(Color.WHITE);

        FontMetrics fm = g2.getFontMetrics();

        int espacamento = fm.getHeight();

        int alturaTexto = texto.length * espacamento;

        int posY = y
                + (altura - alturaTexto) / 2
                + fm.getAscent();

        for (String linha : texto) {

            centralizar(
                    g2,
                    linha,
                    w / 2,
                    posY
            );

            posY += espacamento;
        }

        g2.dispose();
    }

    /**
     * Placa industrial com relevo, sombras e parafusos.
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

        // Interior

        g2.setColor(new Color(24, 32, 40));

        g2.fillRect(
                x + 7, y + 7,
                largura - 14,
                altura - 14
        );

        // Reflexos do metal

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

        // Detalhe central

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

        g2.fillRect(
                x - 1, y - 1, 8, 8
        );

        g2.setColor(new Color(99, 118, 133));

        g2.fillRect(x, y, 6, 6);

        g2.setColor(new Color(153, 169, 181));

        g2.fillRect(x, y, 6, 1);

        g2.setColor(new Color(28, 38, 47));

        g2.fillRect(
                x + 1, y + 2, 4, 2
        );
    }

    private void ajustarFonte(
            Graphics2D g2,
            String texto,
            int tamanho,
            int larguraMaxima) {

        g2.setFont(
                FonteJogo.tamanho(tamanho)
        );

        while (g2.getFontMetrics().stringWidth(texto)
                > larguraMaxima
                && tamanho > 12) {

            tamanho--;

            g2.setFont(
                    FonteJogo.tamanho(tamanho)
            );
        }
    }

    private void centralizar(
            Graphics2D g2,
            String texto,
            int centroX,
            int y) {

        FontMetrics fm = g2.getFontMetrics();

        int x = centroX
                - fm.stringWidth(texto) / 2;

        g2.drawString(texto, x, y);
    }
}