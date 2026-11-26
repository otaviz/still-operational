package com.stilloperational;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.IOException;

/** Apresentação inicial da FLAMES, sem bloquear a navegação. */
public class PainelFlames extends JPanel {
    private static final int FADE_IN = 220;
    private static final int DURACAO = 1850;
    private static final int LARGURA_LOGO = 360;
    private final Runnable continuar;
    private final BufferedImage logo;
    private final Timer animacao;
    private long inicio = -1;
    private boolean saindo;
    private float opacidade;

    public PainelFlames(Runnable continuar) {
        this.continuar = continuar;
        setBackground(Color.BLACK);
        setOpaque(true);
        logo = carregarLogo();
        animacao = new Timer(16, e -> atualizar());
        addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                if (SwingUtilities.isLeftMouseButton(e)) finalizar();
            }
        });
    }

    private BufferedImage carregarLogo() {
        var recurso = getClass().getResource("/flames.png");
        if (recurso == null) return null;
        try {
            BufferedImage original = ImageIO.read(recurso);
            if (original == null) return null;
            // Um pixel de origem ocupa dois pixels na tela.
            // Preserva os traços finos sem redesenhar o símbolo ou a tipografia.
            int w = LARGURA_LOGO / 2;
            int h = Math.max(1, Math.round(w * original.getHeight() / (float) original.getWidth()));
            BufferedImage pequena = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = pequena.createGraphics();
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                    RenderingHints.VALUE_INTERPOLATION_BICUBIC);
            g.drawImage(original, 0, 0, w, h, null);
            g.dispose();
            return pequena;
        } catch (IOException e) {
            System.err.println("Logo FLAMES: " + e.getMessage());
            return null;
        }
    }

    @Override public void addNotify() {
        super.addNotify();
        inicio = -1;
        saindo = false;
        opacidade = 0;
        animacao.start();
    }

    @Override public void removeNotify() {
        animacao.stop();
        super.removeNotify();
    }

    private void atualizar() {
        if (saindo || !isShowing()) return;
        if (inicio < 0) inicio = System.nanoTime();
        long tempo = (System.nanoTime() - inicio) / 1_000_000;
        float p = Math.min(1f, tempo / (float) FADE_IN);
        opacidade = p * p * (3f - 2f * p);
        repaint();
        if (tempo >= DURACAO) finalizar();
    }

    private void finalizar() {
        if (saindo || !isShowing()) return;
        saindo = true;
        animacao.stop();
        // A JanelaPrincipal já executa o fade-out e o fade-in entre telas.
        continuar.run();
    }

    @Override protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, opacidade));
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);
        g2.setColor(Color.WHITE);
        g2.setFont(FonteJogo.tamanho(22));
        int largura = logo == null ? LARGURA_LOGO : logo.getWidth() * 2;
        int altura = logo == null ? 300 : logo.getHeight() * 2;
        int topo = (getHeight() - altura - 52) / 2;
        String titulo = "DEVELOPED BY";
        FontMetrics fm = g2.getFontMetrics();
        g2.drawString(titulo, (getWidth() - fm.stringWidth(titulo)) / 2,
                topo + fm.getAscent());
        if (logo != null) {
            g2.drawImage(logo, (getWidth() - largura) / 2, topo + 52,
                    largura, altura, this);
        } else {
            g2.setFont(FonteJogo.tamanho(42));
            String nome = "FLAMES";
            g2.drawString(nome, (getWidth() - g2.getFontMetrics().stringWidth(nome)) / 2,
                    topo + 52 + altura / 2);
        }
        g2.dispose();
    }
}
