package com.stilloperational;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Area;
import java.awt.image.BufferedImage;

/**
 * Controla a reinicialização do servidor central
 * e o encerramento da partida.
 */
public class PainelFinal extends JPanel {

    private final Image servidor =
            new ImageIcon(getClass().getResource("/servidor.png")).getImage();

    private final Image token =
            new ImageIcon(getClass().getResource("/token.png")).getImage();

    private final Image fundo =
            new ImageIcon(getClass().getResource("/fundo.png")).getImage();

    // A mesma arte usada no menu. As camadas são separadas uma única vez.
    private final BufferedImage logoOriginal;
    private final BufferedImage logoBase;
    private final BufferedImage logoFinal;
    private final BufferedImage logoStill;
    private final BufferedImage logoSemPlacaOnline;
    private final BufferedImage placaOnline;
    private final BufferedImage brilhoLogo;

    private static final int INICIO_FINAL = 8500;
    private static final int INICIO_QUEDA = 10500;
    private static final int FIM_QUEDA = 11800;
    private static final int INICIO_SUBIDA = FIM_QUEDA + 180;
    private static final int FIM_SUBIDA = INICIO_SUBIDA + 1250;
    private static final int INICIO_RELATORIO = FIM_SUBIDA + 850;
    private static final int INICIO_MENU = INICIO_RELATORIO + 1200;

    private final Runnable voltarMenu;

    private final BotaoPixel botao;
    private final Timer animacao;

    private long inicio;
    private long inicioReinicio;

    private boolean reiniciando = false;
    private boolean encerrado = false;

    private float opacidadeCena = 0f;
    private float opacidadeBotao = 0f;
    private float opacidadeFinal = 0f;

    private long tempoReinicio = 0;
    // Mantém os efeitos sincronizados mesmo se um quadro for atrasado.
    private long ultimoTempoAudio = -1;
    private static final int TEMPO_CONTATO_CARTAO = 750;
    private static final int TEMPO_CONTATO_PLACA = INICIO_SUBIDA + 1050;

    private static final int TEMPO_INSERCAO = 1000;
    private static final int TEMPO_PROCESSAMENTO = 3200;

    private static final Color DOURADO =
            new Color(255, 190, 40);

    private static final Color VERDE =
            new Color(80, 220, 120);

    public PainelFinal(Runnable voltarMenu) {

        this.voltarMenu = voltarMenu;
        logoOriginal = carregarLogo("/logo.png");
        logoFinal = carregarLogo("/logo_final.png");
        logoBase = separarLogo(false);
        logoStill = separarLogo(true);
        logoSemPlacaOnline = separarPlacaOnline(false);
        placaOnline = separarPlacaOnline(true);
        brilhoLogo = criarBrilho();

        setBackground(Color.BLACK);
        setLayout(null);

        botao = new BotaoPixel("REINICIAR SERVIDOR") {

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

        botao.setFont(FonteJogo.tamanho(22));
        botao.setVisible(false);

        botao.addActionListener(e -> {

            if (opacidadeBotao < 1f) {
                return;
            }

            if (encerrado) {

                botao.setEnabled(false);
                this.voltarMenu.run();

            } else if (!reiniciando) {

                iniciarReinicio();
            }
        });

        add(botao);

        animacao = new Timer(
                16, e -> atualizarAnimacao()
        );
    }

    @Override
    public void addNotify() {

        super.addNotify();

        inicio = System.nanoTime();

        animacao.start();
    }

    @Override
    public void removeNotify() {

        animacao.stop();
        if (reiniciando) {
            AudioJogo.pararEfeito("inserir_token.wav");
            AudioJogo.pararEfeito("reiniciar.wav");
            AudioJogo.pararEfeito("metal_queda.wav");
            AudioJogo.pararEfeito("metal_encaixe.wav");
        }

        super.removeNotify();
    }

    /**
     * Inicia a inserção dos cartões operacionais.
     */
    private void iniciarReinicio() {

        reiniciando = true;

        inicioReinicio = System.nanoTime();
        ultimoTempoAudio = -1;
        tempoReinicio = 0;

        opacidadeBotao = 0f;

        botao.setVisible(false);

        animacao.start();
    }

    /**
     * Controla a sequência cinematográfica.
     */
    private void atualizarAnimacao() {

        long tempo = (
                System.nanoTime() - inicio
        ) / 1_000_000;

        if (!reiniciando) {

            opacidadeCena = fade(
                    tempo, 0, 1000
            );

            opacidadeBotao = fade(
                    tempo, 1000, 650
            );

            botao.setVisible(opacidadeBotao > 0f);

            if (tempo >= 1650) {
                animacao.stop();
            }

        } else {

            tempoReinicio = (
                    System.nanoTime() - inicioReinicio
            ) / 1_000_000;

            atualizarAudio(tempoReinicio);

            // Escurecimento após a reinicialização

            opacidadeCena = 1f - fade(
                    tempoReinicio, 7700, 800
            );

            // Entrada da tela de encerramento

            opacidadeFinal = fade(
                    tempoReinicio, INICIO_FINAL, 900
            );

            if (tempoReinicio >= INICIO_FINAL && !encerrado) {

                encerrado = true;

                botao.setText("VOLTAR AO MENU");
            }

            if (encerrado) {

                opacidadeBotao = fade(
                        tempoReinicio, INICIO_MENU, 700
                );

                botao.setVisible(opacidadeBotao > 0f);
            }

            if (tempoReinicio >= INICIO_MENU + 700) {

                animacao.stop();
            }
        }

        botao.repaint();
        repaint();
    }

    /** Dispara cada efeito uma única vez ao cruzar seu marco na animação. */
    private void atualizarAudio(long tempo) {
        long anterior = ultimoTempoAudio;
        if (tempo <= anterior) return;
        ultimoTempoAudio = tempo;

        for (int i = 0; i < 3; i++) {
            long contato = (long) i * TEMPO_INSERCAO + TEMPO_CONTATO_CARTAO;
            if (anterior < contato && tempo >= contato) {
                AudioJogo.tocar("inserir_token.wav");
            }
        }

        long processamento = 3L * TEMPO_INSERCAO;
        if (anterior < processamento && tempo >= processamento) {
            AudioJogo.tocar("reiniciar.wav");
        }

        // Não deixa um arquivo de reinicialização longo invadir o encerramento.
        if (anterior < 7700 && tempo >= 7700) {
            AudioJogo.pararEfeito("reiniciar.wav");
        }

        // Impacto fora da tela, após a queda da placa STILL.
        if (anterior < FIM_QUEDA && tempo >= FIM_QUEDA) {
            AudioJogo.tocar("metal_queda.wav");
        }

        // O contato dos tubos precede os últimos 200 ms de vibração.
        if (anterior < TEMPO_CONTATO_PLACA && tempo >= TEMPO_CONTATO_PLACA) {
            AudioJogo.tocar("metal_encaixe.wav");
        }
    }

    @Override
    public void doLayout() {

        botao.setBounds(
                (getWidth() - 360) / 2,
                getHeight() - 80,
                360,
                55
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

        if (opacidadeCena > 0f) {

            desenharServidor(g2);
        }

        if (encerrado) {

            desenharEncerramento(g2);
        }

        g2.dispose();
    }

    /**
     * Renderiza o cenário e o servidor central.
     */
    private void desenharServidor(Graphics2D g) {

        Graphics2D g2 = (Graphics2D) g.create();

        int w = getWidth();
        int h = getHeight();

        g2.setComposite(
                AlphaComposite.getInstance(
                        AlphaComposite.SRC_OVER,
                        opacidadeCena
                )
        );

        // Cenário da instalação

        g2.drawImage(
                fundo,
                0, 0, w, h,
                this
        );

        g2.setColor(new Color(0, 0, 0, 195));

        g2.fillRect(0, 0, w, h);

        // Cabeçalho industrial
        desenharPlaca(g2, w / 2 - 260, 8, 520, 46, DOURADO);
        textoNaPlaca(g2, "SERVIDOR CENTRAL", w / 2 - 260, 8,
                520, 46, 30, DOURADO);

        // Dimensões proporcionais da arte

        int originalW = servidor.getWidth(this);
        int originalH = servidor.getHeight(this);

        if (originalW <= 0 || originalH <= 0) {

            g2.dispose();

            return;
        }

        double proporcao = Math.min(
                520.0 / originalW,
                375.0 / originalH
        );

        int largura = (int) (
                originalW * proporcao
        );

        int altura = (int) (
                originalH * proporcao
        );

        int x = (w - largura) / 2;
        int y = 60;

        // Arte principal

        g2.drawImage(
                servidor,
                x, y,
                largura, altura,
                this
        );

        // Componentes animados do servidor

        desenharMonitor(
                g2, x, y, largura, altura
        );

        desenharIndicadores(
                g2, x, y, largura, altura
        );

        desenharTokens(
                g2, x, y, largura, altura
        );

        // Painel de status, sem interferir nas entradas dos cartões.
        String mensagem;
        Color cor;
        int alturaStatus;
        int yStatus;

        if (!reiniciando) {
            mensagem = "TODOS OS TOKENS RECUPERADOS";
            cor = VERDE;
            alturaStatus = 25;
            yStatus = h - 106;
        } else {
            cor = sistemaOnline() ? VERDE : DOURADO;
            alturaStatus = 42;
            yStatus = h - 88;

            if (sistemaOnline()) {
                mensagem = "INSTALAÇÃO RESTAURADA";
            } else if (tempoReinicio < 3000) {
                mensagem = "INSERINDO TOKENS OPERACIONAIS...";
            } else {
                mensagem = "REINICIALIZANDO SISTEMA...";
            }
        }

        int larguraStatus = 530;
        int xStatus = (w - larguraStatus) / 2;
        desenharPlaca(g2, xStatus, yStatus, larguraStatus, alturaStatus, cor);
        textoNaPlaca(g2, mensagem, xStatus, yStatus,
                larguraStatus, alturaStatus, reiniciando ? 19 : 15, cor);

        g2.dispose();
    }

    /**
     * Anima os três cartões em direção às entradas.
     */
    private void desenharTokens(
            Graphics2D g2,
            int x, int y,
            int largura, int altura) {

        double[] posicoesY = {
                0.565,
                0.658,
                0.752
        };

        for (int i = 0; i < 3; i++) {

            long tempoToken = reiniciando
                    ? tempoReinicio - (long) i * TEMPO_INSERCAO
                    : -1;

            if (tempoToken >= TEMPO_INSERCAO) {
                continue;
            }

            float movimento = progresso(
                    tempoToken, 0, 750
            );

            movimento = movimento * movimento
                    * (3f - 2f * movimento);

            float insercao = progresso(
                    tempoToken, 750, 250
            );

            int destinoX = x + (int) (
                    largura * 0.497
            );

            int destinoY = y + (int) (
                    altura * posicoesY[i]
            );

            int origemX = x - 65;

            int posX = (int) (
                    origemX
                            + (destinoX - origemX) * movimento
            );

            int tokenOriginalW = token.getWidth(this);
            int tokenOriginalH = token.getHeight(this);

            if (tokenOriginalW <= 0 || tokenOriginalH <= 0) {
                continue;
            }

            int larguraToken = 76;

            int alturaToken = Math.max(
                    1,
                    (int) (
                            larguraToken
                                    * tokenOriginalH
                                    / (double) tokenOriginalW
                                    * (1f - insercao)
                    )
            );

            Graphics2D cartao = (Graphics2D) g2.create();

            cartao.setComposite(
                    AlphaComposite.getInstance(
                            AlphaComposite.SRC_OVER,
                            opacidadeCena * (1f - insercao)
                    )
            );

            cartao.drawImage(
                    token,
                    posX - larguraToken / 2,
                    destinoY - alturaToken / 2,
                    larguraToken,
                    alturaToken,
                    this
            );

            cartao.dispose();
        }
    }

    /**
     * Controla as luzes do servidor e dos leitores.
     */
    private void desenharIndicadores(
            Graphics2D g2,
            int x, int y,
            int largura, int altura) {

        double[] posicoesY = {
                0.565,
                0.658,
                0.752
        };

        // Indicadores dos três leitores

        for (int i = 0; i < 3; i++) {

            boolean inserido = reiniciando
                    && tempoReinicio >= (i + 1L) * TEMPO_INSERCAO;

            desenharLed(
                    g2,
                    x + (int) (largura * 0.613),
                    y + (int) (altura * posicoesY[i]),
                    15,
                    10,
                    inserido ? VERDE : DOURADO
            );
        }

        // Indicadores principais

        boolean online = sistemaOnline();

        boolean processando = reiniciando
                && !online;

        int ledX = x + (int) (largura * 0.744);

        desenharLed(
                g2,
                ledX,
                y + (int) (altura * 0.287),
                14, 12,
                !reiniciando
                        ? new Color(255, 60, 45)
                        : new Color(65, 25, 25)
        );

        boolean piscar = (tempoReinicio / 250) % 2 == 0;

        desenharLed(
                g2,
                ledX,
                y + (int) (altura * 0.347),
                14, 12,
                processando && piscar
                        ? DOURADO
                        : new Color(65, 50, 20)
        );

        desenharLed(
                g2,
                ledX,
                y + (int) (altura * 0.408),
                14, 12,
                online
                        ? VERDE
                        : new Color(20, 55, 30)
        );
    }

    private void desenharLed(
            Graphics2D g2,
            int x, int y,
            int largura, int altura,
            Color cor) {

        g2.setColor(new Color(12, 16, 20));

        g2.fillRect(
                x - largura / 2 - 2,
                y - altura / 2 - 2,
                largura + 4,
                altura + 4
        );

        g2.setColor(cor.darker());

        g2.fillRect(
                x - largura / 2,
                y - altura / 2,
                largura,
                altura
        );

        g2.setColor(cor);

        g2.fillRect(
                x - largura / 2 + 2,
                y - altura / 2 + 2,
                largura - 4,
                altura - 4
        );
    }

    /**
     * Exibe mensagens dentro do monitor do servidor.
     */
    private void desenharMonitor(
            Graphics2D g2,
            int x, int y,
            int largura, int altura) {

        Graphics2D tela = (Graphics2D) g2.create();

        int monitorX = x + (int) (largura * 0.355);
        int monitorY = y + (int) (altura * 0.205);

        int monitorW = (int) (largura * 0.29);
        int monitorH = (int) (altura * 0.225);

        tela.clipRect(
                monitorX, monitorY,
                monitorW, monitorH
        );

        int centroX = monitorX + monitorW / 2;

        tela.setColor(new Color(8, 23, 30, 230));

        tela.fillRect(
                monitorX, monitorY,
                monitorW, monitorH
        );

        // Linhas horizontais do monitor

        tela.setColor(new Color(120, 220, 220, 15));

        for (int linha = 0; linha < monitorH; linha += 4) {

            tela.fillRect(
                    monitorX,
                    monitorY + linha,
                    monitorW,
                    1
            );
        }

        // Estado inicial

        if (!reiniciando) {

            tela.setColor(new Color(255, 90, 75));

            textoMonitor(
                    tela,
                    "SYSTEM OFFLINE",
                    centroX,
                    monitorY + 30,
                    15,
                    monitorW
            );

            tela.setColor(Color.WHITE);

            textoMonitor(
                    tela,
                    "AGUARDANDO TOKENS",
                    centroX,
                    monitorY + 55,
                    11,
                    monitorW
            );

            // Inserção dos cartões

        } else if (tempoReinicio < 3000) {

            int quantidade = (int) (
                    tempoReinicio / TEMPO_INSERCAO
            );

            tela.setColor(DOURADO);

            textoMonitor(
                    tela,
                    "AUTENTICANDO",
                    centroX,
                    monitorY + 28,
                    14,
                    monitorW
            );

            tela.setColor(Color.WHITE);

            textoMonitor(
                    tela,
                    "TOKENS: " + quantidade + "/3",
                    centroX,
                    monitorY + 50,
                    12,
                    monitorW
            );

            desenharBarra(
                    tela,
                    monitorX + 12,
                    monitorY + monitorH - 18,
                    monitorW - 24,
                    8,
                    quantidade / 3f
            );

            // Processamento

        } else if (!sistemaOnline()) {

            long tempoProcessamento =
                    tempoReinicio - 3000;

            float progresso = Math.min(
                    1f,
                    tempoProcessamento
                            / (float) TEMPO_PROCESSAMENTO
            );

            String mensagem1;
            String mensagem2;

            if (progresso < 0.33f) {

                mensagem1 = "RECUPERANDO";
                mensagem2 = "PERMISSÕES";

            } else if (progresso < 0.66f) {

                mensagem1 = "REMOVENDO";
                mensagem2 = "INSTRUÇÕES REDUNDANTES";

            } else {

                mensagem1 = "RESTAURANDO";
                mensagem2 = "OPERAÇÕES BOOLEANAS";
            }

            tela.setColor(DOURADO);

            textoMonitor(
                    tela, mensagem1,
                    centroX,
                    monitorY + 20,
                    13,
                    monitorW
            );

            tela.setColor(Color.WHITE);

            textoMonitor(
                    tela, mensagem2,
                    centroX,
                    monitorY + 37,
                    11,
                    monitorW
            );

            textoMonitor(
                    tela,
                    (int) (progresso * 100) + "%",
                    centroX,
                    monitorY + 54,
                    12,
                    monitorW
            );

            desenharBarra(
                    tela,
                    monitorX + 12,
                    monitorY + monitorH - 15,
                    monitorW - 24,
                    8,
                    progresso
            );

            // Sistema restaurado

        } else {

            tela.setColor(VERDE);

            textoMonitor(
                    tela,
                    "SYSTEM ONLINE",
                    centroX,
                    monitorY + 32,
                    17,
                    monitorW
            );

            textoMonitor(
                    tela,
                    "REINÍCIO CONCLUÍDO",
                    centroX,
                    monitorY + 53,
                    11,
                    monitorW
            );
        }

        tela.dispose();
    }

    private void desenharBarra(
            Graphics2D g2,
            int x, int y,
            int largura, int altura,
            float progresso) {

        g2.setColor(new Color(45, 65, 70));

        g2.fillRect(
                x, y,
                largura, altura
        );

        g2.setColor(VERDE);

        g2.fillRect(
                x, y,
                (int) (largura * progresso),
                altura
        );
    }

    private boolean sistemaOnline() {

        return reiniciando
                && tempoReinicio >= 3000 + TEMPO_PROCESSAMENTO;
    }

    /**
     * Apresenta a conclusão da história.
     */
    private void desenharEncerramento(Graphics2D g) {
        Graphics2D g2 = (Graphics2D) g.create();
        int centroX = getWidth() / 2;
        g2.setComposite(AlphaComposite.getInstance(
                AlphaComposite.SRC_OVER, opacidadeFinal));

        if (logoOriginal != null || logoFinal != null) {
            desenharLogoFinal(g2, centroX);
        } else {
            // O encerramento permanece utilizável se a arte estiver ausente.
            desenharPlaca(g2, centroX - 330, 100, 660, 95, DOURADO);
            textoNaPlaca(g2, tempoReinicio < FIM_QUEDA
                            ? "STILL OPERATIONAL" : "OPERATIONAL",
                    centroX - 330, 100, 660, 95, 44, DOURADO);
        }

        float relatorio = fade(tempoReinicio, INICIO_RELATORIO, 850);
        if (relatorio > 0f) {
            g2.setComposite(AlphaComposite.getInstance(
                    AlphaComposite.SRC_OVER, relatorio));
            // A arte final já contém SISTEMA ONLINE. Não duplicar o texto.
            if (logoFinal == null) {
                desenharPlaca(g2, centroX - 210, 284, 420, 42, VERDE);
                textoNaPlaca(g2, "SISTEMA ONLINE", centroX - 210,
                        284, 420, 42, 22, VERDE);
            }

            desenharPlaca(g2, centroX - 380, 350, 760, 100, VERDE);
            textoNaPlaca(g2, "A instalação voltou a funcionar.",
                    centroX - 370, 355, 740, 29, 21, Color.WHITE);
            textoNaPlaca(g2, "Contra todas as expectativas.",
                    centroX - 370, 382, 740, 27, 19, Color.WHITE);
            g2.setColor(new Color(65, 81, 95));
            g2.fillRect(centroX - 260, 409, 520, 2);
            textoNaPlaca(g2, "Seu turno termina em seis horas.",
                    centroX - 370, 414, 740, 30, 19,
                    new Color(185, 199, 211));
        }
        g2.dispose();
    }

    /** Normaliza a arte para coordenadas fixas, sem alterar o PNG do menu. */
    private BufferedImage carregarLogo(String caminho) {
        java.net.URL recurso = getClass().getResource(caminho);
        if (recurso == null) return null;
        ImageIcon imagem = new ImageIcon(recurso);
        if (imagem.getIconWidth() <= 0 || imagem.getIconHeight() <= 0) return null;
        BufferedImage normalizada = new BufferedImage(724, 543, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = normalizada.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
        g.drawImage(imagem.getImage(), 0, 0, 724, 543, null);
        g.dispose();
        return normalizada;
    }

    /** Contorno da placa STILL na arte original, incluindo os pingos vermelhos. */
    private Polygon contornoStill() {
        return new Polygon(
                new int[]{40,44,49,67,70,103,105,132,136,168,171,188,190,
                        224,230,234,266,272,280,279,286,286,264,264,258,
                        257,199,193,190,106,103,103,99,97,97,78,66,51,47,43},
                new int[]{172,164,169,166,159,153,149,145,142,138,135,138,132,
                        130,125,131,125,130,144,155,170,217,222,240,240,
                        224,235,242,236,248,259,259,246,249,253,250,253,244,228,209},
                40);
    }

    /** Isola a placa e desenha o metal que ficava escondido atrás dela. */
    private BufferedImage separarLogo(boolean apenasStill) {
        if (logoOriginal == null) return null;
        BufferedImage camada = new BufferedImage(724, 543, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = camada.createGraphics();
        Polygon placa = contornoStill();
        if (apenasStill) {
            g.setClip(placa);
            g.drawImage(logoOriginal, 0, 0, null);
        } else {
            // A parte encoberta não existe no PNG: reconstrução gráfica do trilho.
            Area recorte = new Area(placa);
            recorte.add(new Area(new BasicStroke(5f).createStrokedShape(placa)));
            g.setClip(recorte);
            Polygon fundoMetal = new Polygon(
                    new int[]{18,38,56,276,300,300,18},
                    new int[]{230,210,197,197,211,270,270}, 7);
            g.setColor(new Color(12, 20, 29));
            g.fillPolygon(fundoMetal);
            g.setClip(recorte);
            g.clip(fundoMetal);
            g.setPaint(new GradientPaint(0, 199, new Color(120, 147, 167),
                    0, 247, new Color(28, 44, 60)));
            g.fillRect(20, 197, 285, 73);
            g.setColor(new Color(173, 193, 204));
            g.fillRect(55, 198, 240, 3);
            g.setColor(new Color(19, 31, 42));
            g.fillRect(55, 203, 240, 3);
            g.fillRect(46, 238, 254, 4);
            g.setColor(new Color(66, 90, 109));
            g.fillRect(60, 225, 220, 4);
            Area restante = new Area(new Rectangle(0, 0, 724, 543));
            restante.subtract(recorte);
            g.setClip(restante);
            g.drawImage(logoOriginal, 0, 0, null);
        }
        g.dispose();
        return camada;
    }

    /** Separa a placa inferior e seus tubos, mantendo os suportes no corpo. */
    private BufferedImage separarPlacaOnline(boolean apenasPlaca) {
        if (logoFinal == null) return null;
        BufferedImage camada = new BufferedImage(724, 543, BufferedImage.TYPE_INT_ARGB);
        // Recorte na junção dos tubos e no espaço entre as duas placas.
        Polygon contorno = new Polygon(
                new int[]{130, 164, 164, 176, 185, 215, 228, 273,
                        284, 437, 451, 495, 507, 526, 539, 557,
                        557, 594, 610, 610, 115, 115},
                new int[]{434, 434, 444, 444, 438, 438, 430, 430,
                        438, 438, 430, 430, 438, 438, 447, 447,
                        434, 434, 447, 543, 543, 447}, 22);
        Area recorte = new Area(contorno);
        if (!apenasPlaca) {
            Area restante = new Area(new Rectangle(0, 0, 724, 543));
            restante.subtract(recorte);
            recorte = restante;
        }
        Graphics2D g = camada.createGraphics();
        g.setClip(recorte);
        g.drawImage(logoFinal, 0, 0, null);
        g.dispose();
        return camada;
    }

    /** Máscara luminosa: somente as faces claras das letras OPERATIONAL. */
    private BufferedImage criarBrilho() {
        if (logoFinal == null) return null;
        BufferedImage mascara = new BufferedImage(724, 543, BufferedImage.TYPE_INT_ARGB);
        // Limites das letras na arte normalizada. Exclui moldura e placa inferior.
        for (int y = 244; y <= 369; y++) {
            for (int x = 39; x <= 702; x++) {
                int pixel = logoFinal.getRGB(x, y);
                int a = pixel >>> 24;
                int r = (pixel >>> 16) & 255;
                int verde = (pixel >>> 8) & 255;
                int azul = pixel & 255;
                // Seleciona os reflexos claros e azulados do metal das letras.
                if (a < 128 || azul < 145 || verde < 135 || r < 90) continue;
                int intensidade = Math.min(210, (verde - 130) * 3);
                int alpha = intensidade * a / 255;
                mascara.setRGB(x, y, (alpha << 24) | 0xA9F3FF);
            }
        }
        return mascara;
    }

    /** Pisca brevemente como um painel energizado e depois estabiliza. */
    private float intensidadeLed(long decorrido) {
        if (decorrido < 100) return 1f;
        if (decorrido < 180) return 0.10f;
        if (decorrido < 310) return 0.95f;
        if (decorrido < 410) return 0.15f;
        if (decorrido < 570) return 1f;
        if (decorrido < 680) return 0.35f;
        return 0.78f;
    }

    /** A placa se desprende e a versão restaurada assume seu lugar. */
    private void desenharLogoFinal(Graphics2D g, int centroX) {
        Graphics2D arte = (Graphics2D) g.create();
        double escala = Math.min(580.0, getWidth() - 50.0) / 724.0;
        arte.translate(centroX - 362 * escala, 23 - 125 * escala);
        arte.scale(escala, escala);
        long t = tempoReinicio;

        if (t < INICIO_QUEDA && logoOriginal != null) {
            arte.drawImage(logoOriginal, 0, 0, null);
        } else if (t < FIM_QUEDA && logoOriginal != null) {
            // Usa a textura da arte restaurada sob a placa em movimento.
            // O indicador SISTEMA ONLINE só é revelado no fim da queda.
            if (logoFinal != null) {
                arte.drawImage(logoSemPlacaOnline, 0, 0, null);
            } else {
                arte.drawImage(logoBase, 0, 0, null);
            }
            Graphics2D still = (Graphics2D) arte.create();
            long decorrido = t - INICIO_QUEDA;
            if (decorrido < 260) {
                still.translate(Math.sin(decorrido * 0.09) * 3, 0);
            } else if (decorrido < 650) {
                float giro = fade(decorrido, 260, 390);
                still.rotate(giro * 0.65, 60, 185);
            } else {
                double queda = (decorrido - 650) / 650.0;
                still.translate(70 * queda, 850 * queda * queda);
                still.rotate(0.65 + queda * 1.6, 60, 185);
            }
            still.drawImage(logoStill, 0, 0, null);
            still.dispose();
        } else if (logoFinal != null) {
            if (t < FIM_SUBIDA) {
                // A placa STILL já caiu. O corpo restaurado fica imóvel.
                arte.drawImage(logoSemPlacaOnline, 0, 0, null);
                if (t >= INICIO_SUBIDA) {
                    long decorrido = t - INICIO_SUBIDA;
                    // A placa inferior sobe de fora da tela, desacelerando.
                    double p = progresso(decorrido, 0, 1050);
                    double deslocamento = 380 * Math.pow(1 - p, 3);
                    // Pequeno impacto mecânico ao encaixar os tubos.
                    if (decorrido >= 1050) {
                        double q = (decorrido - 1050) / 200.0;
                        deslocamento = 3 * Math.sin(q * Math.PI * 2) * (1 - q);
                    }
                    Graphics2D online = (Graphics2D) arte.create();
                    online.translate(0, Math.round(deslocamento));
                    online.drawImage(placaOnline, 0, 0, null);
                    online.dispose();
                }
            } else {
                // No encaixe, usa o PNG completo para eliminar a emenda.
                arte.drawImage(logoFinal, 0, 0, null);
                if (brilhoLogo != null) {
                    arte.setComposite(AlphaComposite.getInstance(
                            AlphaComposite.SRC_OVER,
                            opacidadeFinal * intensidadeLed(t - FIM_SUBIDA)));
                    arte.drawImage(brilhoLogo, 0, 0, null);
                }
            }
        } else {
            // Compatibilidade caso o novo PNG ainda não esteja nos recursos.
            arte.drawImage(logoBase, 0, 0, null);
        }
        arte.dispose();
    }

    /**
     * Placa metálica com contorno, relevo, parafusos e detalhe de estado.
     */
    private void desenharPlaca(Graphics2D g2, int x, int y,
                               int largura, int altura, Color detalhe) {

        g2.setColor(new Color(4, 7, 10));
        g2.fillRect(x + 3, y + 3, largura, altura);
        g2.setColor(new Color(8, 12, 16));
        g2.fillRect(x, y, largura, altura);
        g2.setColor(new Color(55, 70, 83));
        g2.fillRect(x + 2, y + 2, largura - 4, altura - 4);
        g2.setColor(new Color(13, 18, 23));
        g2.fillRect(x + 7, y + 6, largura - 14, altura - 12);
        g2.setColor(new Color(98, 119, 136));
        g2.fillRect(x + 3, y + 3, largura - 6, 2);
        g2.fillRect(x + 3, y + 3, 2, altura - 6);
        g2.setColor(new Color(15, 22, 28));
        g2.fillRect(x + 3, y + altura - 5, largura - 6, 3);
        g2.setColor(detalhe);
        g2.fillRect(x + largura / 2 - 28, y + 2, 56, 3);
        g2.fillRect(x + 5, y + 9, 2, altura - 18);
        g2.fillRect(x + largura - 7, y + 9, 2, altura - 18);

        desenharParafuso(g2, x + 9, y + 7);
        desenharParafuso(g2, x + largura - 15, y + 7);
        if (altura >= 36) {
            desenharParafuso(g2, x + 9, y + altura - 13);
            desenharParafuso(g2, x + largura - 15, y + altura - 13);
        }
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

    /** Centraliza e ajusta a fonte ao espaço real da placa. */
    private void textoNaPlaca(Graphics2D g2, String texto,
                              int x, int y, int largura, int altura,
                              int tamanho, Color cor) {
        g2.setFont(FonteJogo.tamanho(tamanho));
        FontMetrics fm = g2.getFontMetrics();
        while (tamanho > 7 && (fm.stringWidth(texto) > largura - 42
                || fm.getHeight() > altura - 8)) {
            g2.setFont(FonteJogo.tamanho(--tamanho));
            fm = g2.getFontMetrics();
        }
        g2.setColor(cor);
        centralizar(g2, texto, x + largura / 2,
                y + (altura - fm.getHeight()) / 2 + fm.getAscent());
    }

    /**
     * Ajusta o texto ao espaço disponível no monitor.
     */
    private void textoMonitor(
            Graphics2D g2,
            String texto,
            int centroX,
            int y,
            int tamanho,
            int larguraMaxima) {

        int tamanhoAtual = tamanho;

        g2.setFont(
                FonteJogo.tamanho(tamanhoAtual)
        );

        while (g2.getFontMetrics().stringWidth(texto)
                > larguraMaxima - 8
                && tamanhoAtual > 7) {

            tamanhoAtual--;

            g2.setFont(
                    FonteJogo.tamanho(tamanhoAtual)
            );
        }

        centralizar(
                g2, texto, centroX, y
        );
    }

    private void centralizar(
            Graphics2D g2,
            String texto,
            int centroX,
            int y) {

        int larguraTexto = g2
                .getFontMetrics()
                .stringWidth(texto);

        g2.drawString(
                texto,
                centroX - larguraTexto / 2,
                y
        );
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
}
