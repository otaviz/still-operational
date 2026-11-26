package com.stilloperational;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * Apresenta os diálogos do zelador antes das manutenções.
 */
public class PainelMonologo extends JPanel {

    private static final Color DOURADO = new Color(255, 190, 40);
    private static final Color METAL = new Color(65, 81, 95);

    // Ponta do cigarro em coordenadas proporcionais ao PNG original.
    // Ajuste estes valores caso o sprite tenha sido recortado.
    private static final double CIGARRO_X = 0.610;
    private static final double CIGARRO_Y = 0.290;

    private final String setor;
    private final String dialogo;
    private final Image zelador = new ImageIcon(
            getClass().getResource("/zelador.png")
    ).getImage();
    private final Image fundo = new ImageIcon(
            getClass().getResource("/fundo.png")
    ).getImage();

    private final JTextArea texto = new JTextArea();
    private final BotaoPixel continuar;
    private final Timer animacao;

    private long inicio;
    private long inicioBotao = -1;
    private int caracteres = 0;
    private boolean saindo = false;

    private float opacidadeFundo = 0f;
    private float opacidadeTitulo = 0f;
    private float opacidadeZelador = 0f;
    private float opacidadeBotao = 0f;
    private double escalaZelador = 0;

    public PainelMonologo(String setor, String dialogo, Runnable proximo) {
        this.setor = setor;
        this.dialogo = dialogo;

        setBackground(Color.BLACK);
        setLayout(null);

        texto.setFont(FonteJogo.tamanho(20));
        texto.setForeground(Color.WHITE);
        texto.setOpaque(false);
        texto.setEditable(false);
        texto.setFocusable(false);
        texto.setLineWrap(true);
        texto.setWrapStyleWord(true);
        texto.setBorder(BorderFactory.createEmptyBorder());
        add(texto);

        continuar = new BotaoPixel("CONTINUAR") {
            @Override
            public void paint(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setComposite(AlphaComposite.getInstance(
                        AlphaComposite.SRC_OVER, opacidadeBotao));
                super.paint(g2);
                g2.dispose();
            }
        };
        continuar.setVisible(false);
        continuar.addActionListener(e -> {
            if (opacidadeBotao >= 1f && !saindo) {
                saindo = true;
                AudioJogo.pararDigitacao();
                continuar.setEnabled(false);
                proximo.run();
            }
        });
        add(continuar);

        MouseAdapter clique = new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (!saindo && SwingUtilities.isLeftMouseButton(e)) {
                    acelerarTexto();
                }
            }
        };
        addMouseListener(clique);
        texto.addMouseListener(clique);

        animacao = new Timer(16, e -> atualizar());
    }

    @Override
    public void addNotify() {
        super.addNotify();
        inicio = System.nanoTime();
        inicioBotao = -1;
        caracteres = 0;
        saindo = false;
        texto.setText("");
        opacidadeFundo = 0f;
        opacidadeTitulo = 0f;
        opacidadeZelador = 0f;
        opacidadeBotao = 0f;
        escalaZelador = 0;
        continuar.setEnabled(true);
        continuar.setVisible(false);
        animacao.start();
    }

    @Override
    public void removeNotify() {
        animacao.stop();
        AudioJogo.pararDigitacao();
        super.removeNotify();
    }

    private void atualizar() {
        long tempo = tempoAtual();
        opacidadeFundo = fade(tempo, 0, 700);
        opacidadeTitulo = fade(tempo, 700, 600);
        opacidadeZelador = fade(tempo, 1300, 250);

        double p = progresso(tempo, 1300, 600);
        escalaZelador = 1 + 2.70158 * Math.pow(p - 1, 3)
                + 1.70158 * Math.pow(p - 1, 2);

        if (tempo >= 2000 && caracteres < dialogo.length()) {
            int quantidade = Math.min(dialogo.length(),
                    (int) ((tempo - 2000) / 24));
            if (quantidade > caracteres) {
                AudioJogo.tocarDigitacao(dialogo, caracteres, quantidade);
                caracteres = quantidade;
                texto.setText(dialogo.substring(0, caracteres));
            }
        }

        if (caracteres >= dialogo.length() && inicioBotao < 0) {
            inicioBotao = Math.max(2000, tempo);
        }

        if (inicioBotao >= 0) {
            opacidadeBotao = fade(tempo, inicioBotao, 650);
            continuar.setVisible(opacidadeBotao > 0f);
            continuar.repaint();
        }

        // O timer permanece ativo para animar a fumaça.
        // É interrompido automaticamente quando a tela é removida.
        repaint();
    }

    private void acelerarTexto() {
        if (caracteres >= dialogo.length()) {
            return;
        }
        // Se o clique ocorrer durante a entrada, conclui a entrada também.
        if (tempoAtual() < 2000) {
            inicio = System.nanoTime() - 2_000_000_000L;
        }
        AudioJogo.pararDigitacao();
        caracteres = dialogo.length();
        texto.setText(dialogo);
        inicioBotao = tempoAtual();
        atualizar();
    }

    private long tempoAtual() {
        return (System.nanoTime() - inicio) / 1_000_000;
    }

    private float progresso(long tempo, long atraso, int duracao) {
        return Math.max(0f, Math.min(1f, (tempo - atraso) / (float) duracao));
    }

    private float fade(long tempo, long atraso, int duracao) {
        float p = progresso(tempo, atraso, duracao);
        return p * p * (3f - 2f * p);
    }

    @Override
    public void doLayout() {
        int w = getWidth();
        int h = getHeight();
        texto.setBounds(100, 126, w - 200, 154);
        continuar.setBounds((w - 280) / 2, h - 85, 280, 60);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_OFF);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);

        int w = getWidth();
        int h = getHeight();
        g2.setComposite(AlphaComposite.getInstance(
                AlphaComposite.SRC_OVER, opacidadeFundo));
        g2.drawImage(fundo, 0, 0, w, h, this);
        g2.setColor(new Color(0, 0, 0, 165));
        g2.fillRect(0, 0, w, h);

        desenharMoldura(g2);
        desenharCabecalho(g2);
        desenharCaixaDialogo(g2);
        desenharZelador(g2);
        desenharInstrucao(g2);
        g2.dispose();
    }

    private void desenharMoldura(Graphics2D g2) {
        int w = getWidth();
        int h = getHeight();
        g2.setColor(new Color(8, 12, 16));
        g2.drawRect(17, 17, w - 35, h - 35);
        g2.setColor(METAL);
        g2.drawRect(20, 20, w - 41, h - 41);
        g2.setColor(new Color(36, 47, 57));
        g2.drawRect(23, 23, w - 47, h - 47);
        desenharCanto(g2, 20, 20, 1, 1);
        desenharCanto(g2, w - 21, 20, -1, 1);
        desenharCanto(g2, 20, h - 21, 1, -1);
        desenharCanto(g2, w - 21, h - 21, -1, -1);
    }

    private void desenharCanto(Graphics2D g2, int x, int y, int dx, int dy) {
        Graphics2D canto = (Graphics2D) g2.create();
        canto.translate(x, y);
        canto.scale(dx, dy);
        canto.setColor(new Color(43, 54, 65));
        canto.fillRect(0, 0, 42, 7);
        canto.fillRect(0, 0, 7, 42);
        canto.setColor(new Color(197, 154, 56));
        canto.fillRect(0, 0, 35, 3);
        canto.fillRect(0, 0, 3, 35);
        canto.dispose();
    }

    private void desenharCabecalho(Graphics2D g) {
        Graphics2D g2 = (Graphics2D) g.create();
        int w = getWidth();
        g2.setComposite(AlphaComposite.getInstance(
                AlphaComposite.SRC_OVER, opacidadeTitulo));
        desenharPlaca(g2, 120, 30, w - 240, 53, DOURADO);
        g2.setColor(new Color(7, 10, 14));
        g2.fillRect(137, 41, w - 274, 31);
        int tamanho = 30;
        g2.setFont(FonteJogo.tamanho(tamanho));
        while (g2.getFontMetrics().stringWidth(setor) > w - 300 && tamanho > 12) {
            g2.setFont(FonteJogo.tamanho(--tamanho));
        }
        g2.setColor(new Color(91, 62, 15));
        centralizar(g2, setor, w / 2 + 2, 67);
        g2.setColor(DOURADO);
        centralizar(g2, setor, w / 2, 65);
        g2.dispose();
    }

    private void desenharCaixaDialogo(Graphics2D g) {
        Graphics2D g2 = (Graphics2D) g.create();
        int w = getWidth();
        g2.setComposite(AlphaComposite.getInstance(
                AlphaComposite.SRC_OVER, opacidadeTitulo));
        desenharPlaca(g2, 75, 103, w - 150, 184, METAL);
        g2.setColor(new Color(13, 18, 23));
        g2.fillRect(90, 119, w - 180, 154);
        g2.setColor(new Color(197, 154, 56));
        g2.fillRect(80, 139, 4, 33);
        g2.fillRect(w - 84, 139, 4, 33);

        // Placa de identificação de quem está falando.
        desenharPlaca(g2, 90, 91, 180, 30, DOURADO);
        g2.setFont(FonteJogo.tamanho(17));
        g2.setColor(DOURADO);
        centralizar(g2, "ZELADOR", 180, 111);

        // Pequeno bico pixelado, ligando a caixa ao personagem.
        int cx = w / 2;
        g2.setColor(new Color(8, 12, 16));
        g2.fillRect(cx - 17, 285, 34, 5);
        g2.fillRect(cx - 12, 290, 24, 5);
        g2.fillRect(cx - 6, 295, 12, 4);
        g2.setColor(METAL);
        g2.fillRect(cx - 13, 285, 26, 5);
        g2.fillRect(cx - 8, 290, 16, 4);
        g2.fillRect(cx - 3, 294, 6, 3);
        g2.dispose();
    }

    private void desenharZelador(Graphics2D g) {
        int originalW = zelador.getWidth(this);
        int originalH = zelador.getHeight(this);
        if (originalW <= 0 || originalH <= 0 || opacidadeZelador <= 0f) {
            return;
        }
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setComposite(AlphaComposite.getInstance(
                AlphaComposite.SRC_OVER, opacidadeZelador));
        int altura = Math.max(1, (int) (170 * escalaZelador));
        int largura = Math.max(1, altura * originalW / originalH);
        int x = (getWidth() - largura) / 2;
        int y = 365 - altura / 2;
        g2.drawImage(zelador, x, y, largura, altura, this);
        if (tempoAtual() >= 1900) {
            desenharFumaca(g2, x, y, largura, altura);
        }
        g2.dispose();
    }

    /** Fumaça pixelada: partículas sobem e desaparecem em um loop suave. */
    private void desenharFumaca(Graphics2D g, int x, int y, int largura, int altura) {
        Graphics2D g2 = (Graphics2D) g.create();
        int origemX = x + (int) Math.round(largura * CIGARRO_X);
        int origemY = y + (int) Math.round(altura * CIGARRO_Y);
        long tempo = tempoAtual() - 1900;
        int pixel = Math.max(2, Math.round(altura / 85f));
        for (int i = 0; i < 5; i++) {
            long idade = tempo - i * 320L;
            if (idade < 0) continue;
            double p = (idade % 1600L) / 1600.0;
            double desvio = 3 * p + Math.sin(p * 5 + i * 0.65) * 5 * p;
            int px = origemX + (int) Math.round(desvio / pixel) * pixel;
            int py = origemY - (int) Math.round((p * 29) / pixel) * pixel;
            int alpha = (int) Math.round(125 * (1 - p) * opacidadeZelador);
            g2.setComposite(AlphaComposite.SrcOver);
            g2.setColor(new Color(177, 187, 196, alpha));
            g2.fillRect(px, py, pixel, pixel);
            if (p > 0.20) {
                g2.fillRect(px - pixel, py - pixel, pixel, pixel);
            }
            if (p > 0.45) {
                g2.setColor(new Color(145, 155, 165, alpha / 2));
                g2.fillRect(px + pixel, py - pixel, pixel, pixel);
            }
        }
        g2.dispose();
    }

    private void desenharInstrucao(Graphics2D g) {
        if (caracteres >= dialogo.length()) return;
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setComposite(AlphaComposite.getInstance(
                AlphaComposite.SRC_OVER, opacidadeTitulo));
        int w = getWidth();
        int h = getHeight();
        desenharPlaca(g2, (w - 280) / 2, h - 75, 280, 42, METAL);
        g2.setFont(FonteJogo.tamanho(14));
        g2.setColor(new Color(185, 199, 211));
        centralizar(g2, "CLIQUE PARA ACELERAR", w / 2, h - 49);
        g2.dispose();
    }

    private void desenharPlaca(Graphics2D g2, int x, int y,
                               int largura, int altura, Color detalhe) {
        g2.setColor(new Color(4, 7, 10));
        g2.fillRect(x + 4, y + 4, largura, altura);
        g2.setColor(new Color(8, 12, 16));
        g2.fillRect(x, y, largura, altura);
        g2.setColor(new Color(55, 70, 83));
        g2.fillRect(x + 2, y + 2, largura - 4, altura - 4);
        g2.setColor(new Color(24, 32, 40));
        g2.fillRect(x + 7, y + 7, largura - 14, altura - 14);
        g2.setColor(new Color(98, 119, 136));
        g2.fillRect(x + 3, y + 3, largura - 6, 2);
        g2.fillRect(x + 3, y + 3, 2, altura - 6);
        g2.setColor(new Color(15, 22, 28));
        g2.fillRect(x + 3, y + altura - 5, largura - 6, 3);
        g2.setColor(detalhe);
        g2.fillRect(x + largura / 2 - 28, y + 2, 56, 3);
        desenharParafuso(g2, x + 9, y + 9);
        desenharParafuso(g2, x + largura - 14, y + 9);
        desenharParafuso(g2, x + 9, y + altura - 14);
        desenharParafuso(g2, x + largura - 14, y + altura - 14);
    }

    private void desenharParafuso(Graphics2D g2, int x, int y) {
        g2.setColor(new Color(6, 10, 14));
        g2.fillRect(x - 1, y - 1, 8, 8);
        g2.setColor(new Color(99, 118, 133));
        g2.fillRect(x, y, 6, 6);
        g2.setColor(new Color(153, 169, 181));
        g2.fillRect(x, y, 6, 1);
        g2.setColor(new Color(28, 38, 47));
        g2.fillRect(x + 1, y + 2, 4, 2);
    }

    private void centralizar(Graphics2D g2, String frase, int centroX, int y) {
        FontMetrics fm = g2.getFontMetrics();
        g2.drawString(frase, centroX - fm.stringWidth(frase) / 2, y);
    }
}
