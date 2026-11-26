package com.stilloperational;

import javax.swing.*;
import java.awt.*;

/**
 * Menu principal com animações de entrada.
 */
public class PainelMenu extends JPanel {

    private final Image fundo = new ImageIcon(
            getClass().getResource("/fundo_menu.png")
    ).getImage();

    private final Image logo = new ImageIcon(
            getClass().getResource("/logo.png")
    ).getImage();

    private final BotaoAnimado iniciarBotao;
    private final BotaoAnimado sairBotao;

    private final Timer animacao;

    private long inicio;

    private float opacidadeFundo = 0f;
    private float opacidadeLogo = 0f;

    public PainelMenu(Runnable iniciar, Runnable sair) {

        setBackground(Color.BLACK);
        setLayout(null);

        iniciarBotao = new BotaoAnimado("INICIAR JOGO");
        iniciarBotao.addActionListener(e -> iniciar.run());

        sairBotao = new BotaoAnimado("SAIR");
        sairBotao.addActionListener(e -> sair.run());

        add(iniciarBotao);
        add(sairBotao);

        animacao = new Timer(16, e -> atualizarAnimacao());
    }

    @Override
    public void addNotify() {
        super.addNotify();

        inicio = System.nanoTime();

        opacidadeFundo = 0f;
        opacidadeLogo = 0f;

        iniciarBotao.setOpacidade(0f);
        sairBotao.setOpacidade(0f);

        animacao.start();
    }

    @Override
    public void removeNotify() {
        animacao.stop();
        super.removeNotify();
    }

    private void atualizarAnimacao() {

        long tempo = (System.nanoTime() - inicio) / 1_000_000;

        opacidadeFundo = calcularFade(tempo, 0, 1000);

        opacidadeLogo = calcularFade(tempo, 1000, 800);

        iniciarBotao.setOpacidade(
                calcularFade(tempo, 1800, 600)
        );

        sairBotao.setOpacidade(
                calcularFade(tempo, 2400, 600)
        );

        if (tempo >= 3000) {
            animacao.stop();
        }

        repaint();
    }

    private float calcularFade(long tempo, int atraso, int duracao) {

        float progresso = Math.max(0f, Math.min(1f,
                (tempo - atraso) / (float) duracao));

        // Suaviza o início e o fim da animação
        return progresso * progresso * (3f - 2f * progresso);
    }

    @Override
    public void doLayout() {

        int larguraBotao = 280;
        int alturaBotao = 60;

        int x = (getWidth() - larguraBotao) / 2;

        iniciarBotao.setBounds(
                x, (int) (getHeight() * 0.60),
                larguraBotao, alturaBotao
        );

        sairBotao.setBounds(
                x, (int) (getHeight() * 0.75),
                larguraBotao, alturaBotao
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

        // Fade-in do fundo
        g2.setComposite(AlphaComposite.getInstance(
                AlphaComposite.SRC_OVER, opacidadeFundo
        ));

        g2.drawImage(
                fundo, 0, 0,
                getWidth(), getHeight(), this
        );

        // Escurecimento do cenário
        g2.setComposite(AlphaComposite.getInstance(
                AlphaComposite.SRC_OVER, 0.28f * opacidadeFundo
        ));

        g2.setColor(Color.BLACK);
        g2.fillRect(0, 0, getWidth(), getHeight());

        // Fade-in da logo
        g2.setComposite(AlphaComposite.getInstance(
                AlphaComposite.SRC_OVER, opacidadeLogo
        ));

        int larguraOriginal = logo.getWidth(this);
        int alturaOriginal = logo.getHeight(this);

        if (larguraOriginal > 0 && alturaOriginal > 0) {

            int largura = 560;
            int altura = largura * alturaOriginal / larguraOriginal;

            int x = (getWidth() - largura) / 2;
            int y = 155 - altura / 2;

            g2.drawImage(
                    logo, x, y,
                    largura, altura, this
            );
        }

        g2.dispose();
    }

    /**
     * Botão pixelado com controle gradual de opacidade.
     */
    private static class BotaoAnimado extends BotaoPixel {

        private float opacidade = 0f;

        public BotaoAnimado(String texto) {
            super(texto);
        }

        public void setOpacidade(float valor) {

            opacidade = valor;

            setVisible(valor > 0f);

            repaint();
        }

        @Override
        public void paint(Graphics g) {

            Graphics2D g2 = (Graphics2D) g.create();

            g2.setComposite(AlphaComposite.getInstance(
                    AlphaComposite.SRC_OVER, opacidade
            ));

            super.paint(g2);

            g2.dispose();
        }
    }
}