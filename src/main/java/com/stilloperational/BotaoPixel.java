package com.stilloperational;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.font.TextLayout;
import java.awt.geom.Rectangle2D;

/**
 * Botão industrial pixelado com moldura proporcional e texto ajustável.
 */
public class BotaoPixel extends JButton {

    private boolean somPadraoAtivo = true;

    /** Sensores possuem efeito próprio; os demais botões mantêm hover e clique. */
    public void setSomPadraoAtivo(boolean ativo) {
        somPadraoAtivo = ativo;
    }

    public BotaoPixel(String texto) {
        super(texto);
        setFont(FonteJogo.tamanho(24));
        setPreferredSize(new Dimension(280, 60));
        setContentAreaFilled(false);
        setBorderPainted(false);
        setFocusPainted(false);
        setOpaque(false);
        setRolloverEnabled(true);
        setCursor(new Cursor(Cursor.HAND_CURSOR));
        getModel().addChangeListener(e -> repaint());

        // Um efeito por entrada do mouse; botões desabilitados ficam silenciosos.
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                if (isEnabled() && somPadraoAtivo) {
                    AudioJogo.tocar("hover.wav");
                }
            }
        });
    }

    @Override
    protected void fireActionPerformed(ActionEvent evento) {
        // Toca somente em uma ação efetiva, também pelo teclado.
        if (isEnabled() && somPadraoAtivo) {
            AudioJogo.tocar("clique.wav");
        }
        super.fireActionPerformed(evento);
    }

    @Override
    protected void paintComponent(Graphics g) {
        int w = getWidth();
        int h = getHeight();
        if (w < 46 || h < 16) return;

        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_OFF);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);

        boolean hover = getModel().isRollover();
        boolean pressionado = getModel().isPressed() && getModel().isArmed();

        if (pressionado) {
            g2.translate(w / 2.0, h / 2.0);
            g2.scale(0.94, 0.94);
            g2.translate(-w / 2.0, -h / 2.0);
        }

        Color dourado = new Color(197, 154, 56);
        Color brilho = new Color(241, 202, 102);
        Color sombra = new Color(136, 96, 33);
        Color texto = new Color(33, 25, 14);

        if (hover) {
            dourado = new Color(240, 203, 114);
            brilho = new Color(255, 240, 187);
            sombra = new Color(179, 139, 54);
            texto = new Color(89, 64, 24);
        }
        if (pressionado) {
            dourado = new Color(158, 118, 43);
            brilho = new Color(202, 163, 77);
            sombra = new Color(108, 75, 29);
            texto = new Color(24, 19, 12);
        }
        if (!isEnabled()) {
            dourado = new Color(90, 94, 98);
            brilho = new Color(125, 130, 135);
            sombra = new Color(55, 60, 65);
            texto = Color.LIGHT_GRAY;
        }

        // A moldura foi desenhada originalmente para 60 px de altura.
        // Em botões compactos, comprimimos APENAS a moldura na vertical.
        // O texto é renderizado depois, sem essa deformação.
        int alturaModelo = Math.max(60, h);
        double escalaY = h / (double) alturaModelo;
        Graphics2D moldura = (Graphics2D) g2.create();
        moldura.scale(1.0, escalaY);
        desenharMoldura(moldura, w, alturaModelo, dourado, brilho, sombra);
        moldura.dispose();

        // Área útil real, já convertida para a altura atual do botão.
        // Excluímos os relevos superior/inferior e as marcas laterais.
        double margemX = Math.min(40, (w - 10) / 2.0);
        double topo = 15 * escalaY;
        double base = (alturaModelo - 21) * escalaY;
        desenharTexto(g2, margemX, topo, w - margemX, base, texto, brilho);
        g2.dispose();
    }

    private void desenharTexto(Graphics2D g2, double esquerda, double topo,
                               double direita, double base, Color texto, Color brilho) {
        String legenda = getText();
        if (legenda == null || legenda.isBlank()) return;

        // Reserva espaço para arredondamento dos pixels e relevo do texto.
        double largura = direita - esquerda - 2;
        double altura = base - topo - 3;
        if (largura <= 0 || altura <= 0) return;

        Font fonteBase = getFont();
        float tamanho = fonteBase.getSize2D();
        TextLayout layout = null;
        Rectangle2D limites = null;

        // Usa a área realmente ocupada pelos caracteres, inclusive acentos,
        // em vez da altura total de linha da fonte.
        while (tamanho >= 1f) {
            Font fonte = fonteBase.deriveFont(tamanho);
            TextLayout candidato = new TextLayout(legenda, fonte,
                    g2.getFontRenderContext());
            Rectangle2D caixa = candidato.getBounds();
            if (caixa.getWidth() <= largura && caixa.getHeight() <= altura) {
                layout = candidato;
                limites = caixa;
                break;
            }
            tamanho -= 0.5f;
        }

        // Nunca volta à fonte grande se ela não couber.
        if (layout == null || limites == null) return;

        float x = (float) Math.round((esquerda + direita) / 2.0
                - limites.getCenterX());
        float y = (float) Math.round((topo + base - 1) / 2.0
                - limites.getCenterY());

        // Sem clipping artificial: os caracteres já foram dimensionados
        // para caber inteiros na superfície dourada.
        g2.setColor(brilho);
        layout.draw(g2, x, y + 1);
        g2.setColor(texto);
        layout.draw(g2, x, y);
    }

    private void desenharMoldura(Graphics2D g2, int w, int h,
                                 Color dourado, Color brilho, Color sombra) {
        placa(g2, 2, 6, w - 4, h - 6, 7, new Color(8, 12, 16));
        placa(g2, 0, 0, w, h - 6, 8, new Color(9, 14, 19));
        placa(g2, 3, 3, w - 6, h - 12, 6, new Color(53, 70, 83));

        g2.setColor(new Color(128, 148, 162));
        g2.fillRect(10, 4, w - 20, 3);
        g2.fillRect(4, 10, 3, h - 26);
        g2.setColor(new Color(91, 112, 126));
        g2.fillRect(10, 8, w - 20, 2);

        g2.setColor(new Color(23, 35, 45));
        g2.fillRect(9, h - 16, w - 18, 5);
        g2.fillRect(w - 7, 10, 3, h - 26);

        placa(g2, 17, 8, w - 34, h - 22, 4, new Color(14, 20, 25));
        placa(g2, 21, 11, w - 42, h - 28, 3, dourado);

        g2.setColor(brilho);
        g2.fillRect(25, 12, w - 50, 3);
        g2.setColor(sombra);
        g2.fillRect(25, h - 21, w - 50, 4);

        if (w >= 90) {
            for (int i = 0; i < 3; i++) {
                int x = 26 + i * 5;
                g2.fillPolygon(new int[]{x, x + 3, x - 1, x - 4},
                        new int[]{16, 16, 23, 23}, 4);
                int direita = w - 26 - i * 5;
                g2.fillPolygon(new int[]{direita, direita + 3, direita - 1, direita - 4},
                        new int[]{h - 31, h - 31, h - 24, h - 24}, 4);
            }
        }

        parafuso(g2, 7, 11);
        parafuso(g2, w - 15, 11);
        parafuso(g2, 7, h - 27);
        parafuso(g2, w - 15, h - 27);

        g2.setColor(new Color(157, 176, 187));
        g2.fillRect(18, 5, 11, 2);
        g2.fillRect(w - 34, h - 13, 8, 2);
        g2.setColor(new Color(20, 30, 39));
        g2.fillRect(w / 3, 5, 7, 2);
        g2.fillRect(w * 2 / 3, h - 12, 9, 2);
    }

    private void placa(Graphics2D g2, int x, int y,
                       int w, int h, int corte, Color cor) {
        if (w <= 0 || h <= 0) return;
        corte = Math.min(corte, Math.min(w, h) / 2);
        g2.setColor(cor);
        int[] pontosX = {x + corte, x + w - corte,
                x + w - corte, x + w, x + w, x + w - corte,
                x + w - corte, x + corte, x + corte, x, x, x + corte};
        int[] pontosY = {y, y, y + corte, y + corte,
                y + h - corte, y + h - corte, y + h, y + h,
                y + h - corte, y + h - corte, y + corte, y + corte};
        g2.fillPolygon(pontosX, pontosY, 12);
    }

    private void parafuso(Graphics2D g2, int x, int y) {
        g2.setColor(new Color(15, 23, 30));
        g2.fillRect(x, y, 8, 8);
        g2.setColor(new Color(138, 157, 169));
        g2.fillRect(x + 1, y + 1, 6, 6);
        g2.setColor(new Color(194, 207, 215));
        g2.fillRect(x + 1, y + 1, 4, 2);
        g2.setColor(new Color(53, 67, 77));
        g2.fillRect(x + 2, y + 3, 4, 2);
    }
}
