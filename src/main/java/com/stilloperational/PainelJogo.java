package com.stilloperational;

import javax.swing.*;
import java.awt.*;
import java.util.EnumMap;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.IOException;

/** Painel de manutenção com sensores interativos e confirmação da resposta. */
public class PainelJogo extends JPanel {
    private static final Color DOURADO = new Color(255, 190, 40);
    private static final Color VERDE = new Color(80, 220, 120);
    private static final Color VERMELHO = new Color(255, 85, 85);
    private static final Color METAL = new Color(92, 113, 131);
    private static final Color INATIVO = new Color(125, 145, 160);
    private static final Color INTERIOR = new Color(13, 19, 25);
    private static final int TEMPO_DIAGNOSTICO = 1000;

    private final Image fundo = new ImageIcon(getClass().getResource("/fundo.png")).getImage();
    private final Image zelador = new ImageIcon(getClass().getResource("/zelador.png")).getImage();
    private final BufferedImage reacoes = carregarReacoes();
    private final Image token = new ImageIcon(getClass().getResource("/token.png")).getImage();
    private final Jogo jogo;
    private final Manutencao manutencao;
    private final int fase;
    private final Runnable proximo;
    private final BotaoPixel testar, menu, continuar;
    private final EnumMap<Operador, BotaoPixel> modulos = new EnumMap<>(Operador.class);
    private final Timer animacao;
    private final BotaoPixel entradaA, entradaB;
    // Os sensores são independentes da resposta selecionada.
    private boolean valorA, valorB;

    private Operador selecionado;
    private boolean testando, respondida, erro, avancando;
    private int tentativas;
    private long inicioTeste, inicioRecompensa, ultimoQuadro;
    private float abertura, opacidadeBotao;
    private boolean somTokenTocado;
    // A animação é independente da lógica e do contador de tokens.
    private long inicioReacao = -1L;
    private boolean reacaoPositiva;
    private static final int[] QUADROS_ACERTO = {0, 1, 2, 1, 2, 3, 0};
    private static final int[] QUADROS_ERRO = {4, 5, 4, 5, 5, 7, 0};
    private static final int[] DURACOES_ACERTO = {80, 130, 170, 130, 180, 150, 100};
    private static final int[] DURACOES_ERRO = {80, 180, 120, 200, 250, 180, 100};
    private String mensagem = "ESCOLHA O OPERADOR";
    private String dica = "Altere os sensores para visualizar as entradas.";

    public PainelJogo(Jogo jogo, Runnable voltarMenu, Runnable proximo) {
        this.jogo = jogo;
        this.manutencao = jogo.atual();
        this.fase = jogo.faseAtual();
        this.proximo = proximo;
        setBackground(Color.BLACK);
        setLayout(null);

        valorA = manutencao.entradaA();
        valorB = manutencao.usaDuasEntradas() && manutencao.entradaB();
        entradaA = botao("", 16, () -> {
            if (testando || respondida) return;
            valorA = !valorA;
            AudioJogo.tocar("entrada.wav");
            atualizarSimulador();
        });
        entradaB = botao("", 16, () -> {
            if (testando || respondida || !manutencao.usaDuasEntradas()) return;
            valorB = !valorB;
            AudioJogo.tocar("entrada.wav");
            atualizarSimulador();
        });
        entradaA.setSomPadraoAtivo(false);
        entradaB.setSomPadraoAtivo(false);
        entradaB.setVisible(manutencao.usaDuasEntradas());
        for (Operador op : Operador.values()) {
            BotaoPixel chip = botao(op.name(), 23, () -> selecionar(op));
            chip.setToolTipText("Marcar " + op.name() + " como resposta");
            modulos.put(op, chip);
        }
        testar = botao("CONFIRMAR", 21, this::iniciarTeste);
        menu = botao("MENU", 20, voltarMenu);
        continuar = new BotaoPixel("CONTINUAR") {
            @Override public void paint(Graphics g) {
                Graphics2D c = (Graphics2D) g.create();
                c.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, opacidadeBotao));
                super.paint(c);
                c.dispose();
            }
        };
        continuar.setFont(FonteJogo.tamanho(21));
        continuar.setVisible(false);
        continuar.setEnabled(false);
        continuar.addActionListener(e -> {
            if (respondida && !avancando && opacidadeBotao >= 1f) {
                avancando = true;
                continuar.setEnabled(false);
                this.proximo.run();
            }
        });
        add(continuar);
        animacao = new Timer(16, e -> atualizarAnimacao());
        atualizarControles();
    }

    private BotaoPixel botao(String texto, int tamanho, Runnable acao) {
        BotaoPixel botao = new BotaoPixel(texto);
        botao.setFont(FonteJogo.tamanho(tamanho));
        botao.addActionListener(e -> acao.run());
        add(botao);
        return botao;
    }

    private void selecionar(Operador op) {
        if (testando || respondida) return;
        selecionado = op;
        erro = false;
        mensagem = "RESPOSTA SELECIONADA";
        dica = "Confirme quando estiver pronto.";
        atualizarControles();
    }

    private void atualizarControles() {
        boolean livre = !testando && !respondida;
        for (BotaoPixel chip : modulos.values()) {
            chip.setEnabled(livre);
            chip.setVisible(!respondida);
        }
        testar.setVisible(!respondida);
        testar.setEnabled(livre && selecionado != null);
        testar.setText(testando ? "VERIFICANDO..." : respondida ? "APROVADO" : "CONFIRMAR");
        atualizarSimulador();
    }

    private void atualizarSimulador() {
        entradaA.setText(valorA ? "1 - SIM" : "0 - NÃO");
        entradaB.setText(valorB ? "1 - SIM" : "0 - NÃO");
        entradaA.setEnabled(!testando && !respondida);
        entradaB.setEnabled(!testando && !respondida);
        repaint();
    }

    /** O equipamento só funciona depois da aprovação da manutenção. */
    private boolean saida() {
        return respondida && manutencao.resultadoEsperado();
    }

    private void iniciarTeste() {
        if (selecionado == null || testando || respondida) return;
        testando = true;
        erro = false;
        inicioTeste = System.nanoTime();
        mensagem = "DIAGNÓSTICO EM ANDAMENTO";
        dica = "Verificando a configuração...";
        atualizarControles();
    }

    private void avancarTeste(long agora) {
        if ((agora - inicioTeste) / 1_000_000 < TEMPO_DIAGNOSTICO) return;
        testando = false;
        // Jogo.responder valida TODAS as combinações pela classe Manutencao.
        // O diagnóstico não depende dos sensores manipulados na tela.
        if (jogo.responder(selecionado)) {
            respondida = true;
            erro = false;
            mensagem = "MANUTENÇÃO CONCLUÍDA!";
            dica = switch (fase) {
                case 1 -> "As credenciais foram verificadas. A passagem está liberada.";
                case 2 -> "Sistema de emergência restaurado.";
                default -> "Iluminação restaurada. Agora podemos enxergar.";
            };
            // Demonstra o cenário de funcionamento após o acerto.
            valorA = manutencao.entradaA();
            valorB = manutencao.usaDuasEntradas() && manutencao.entradaB();
            abertura = 0f;
            inicioRecompensa = agora;
            iniciarReacao(true, agora);
            somTokenTocado = false;
            AudioJogo.tocar("sucesso.wav");
            // O equipamento entra em funcionamento no mesmo quadro da aprovação.
            if (fase == 2 && saida()) {
                AudioJogo.tocarEmLoop("alarme.wav");
            } else if (saida()) {
                AudioJogo.tocar(somEquipamento());
            }
            opacidadeBotao = 0f;
        } else {
            erro = true;
            tentativas++;
            iniciarReacao(false, agora);
            AudioJogo.tocar("erro.wav");
            mensagem = "CONFIGURAÇÃO INVÁLIDA";
            dica = obterDica();
            selecionado = null;
        }
        atualizarControles();
    }

    private String somEquipamento() {
        return switch (fase) {
            case 1 -> "porta.wav";
            case 2 -> "alarme.wav";
            default -> "lampada.wav";
        };
    }

    private String obterDica() {
        if (tentativas == 1) return switch (fase) {
            case 1 -> "Releia as exigências do controle de acesso.";
            case 2 -> "Reconsidere como os sensores devem trabalhar.";
            default -> "Pense na relação entre claridade e iluminação.";
        };
        if (tentativas == 2) return switch (fase) {
            case 1 -> "Imagine um funcionário com apenas uma credencial válida.";
            case 2 -> "Imagine que somente um sensor detectou perigo.";
            default -> "O que deve acontecer quando o ambiente escurece?";
        };
        return switch (fase) {
            case 1 -> "Uma credencial isolada não deve liberar a passagem.";
            case 2 -> "O perigo não precisa ser confirmado pelos dois sensores.";
            default -> "A lâmpada deve reagir de forma inversa ao sensor.";
        };
    }

    @Override public void addNotify() {
        super.addNotify();
        ultimoQuadro = System.nanoTime();
        animacao.start();
    }

    @Override public void removeNotify() {
        animacao.stop();
        // Impede que um alarme ou outro efeito longo invada a tela seguinte.
        if (respondida) {
            AudioJogo.pararEfeito(somEquipamento());
            AudioJogo.pararEfeito("sucesso.wav");
            AudioJogo.pararEfeito("token.wav");
        }
        AudioJogo.pararEfeito("erro.wav");
        super.removeNotify();
    }

    private void atualizarAnimacao() {
        long agora = System.nanoTime();
        float dt = Math.min(0.08f, (agora - ultimoQuadro) / 1_000_000_000f);
        ultimoQuadro = agora;
        if (testando) avancarTeste(agora);
        float alvo = saida() ? 1f : 0f;
        abertura += Math.signum(alvo - abertura) * Math.min(Math.abs(alvo - abertura), dt * 2.7f);
        if (respondida) {
            long tempo = (agora - inicioRecompensa) / 1_000_000;
            // Um único efeito quando o token termina de aparecer (fade de 250 ms).
            if (!somTokenTocado && tempo >= 250) {
                somTokenTocado = true;
                AudioJogo.tocar("token.wav");
            }
            opacidadeBotao = fade(tempo, 1250, 650);
            continuar.setVisible(opacidadeBotao > 0f);
            if (opacidadeBotao >= 1f && !avancando && !continuar.isEnabled()) continuar.setEnabled(true);
            continuar.repaint();
        }
        repaint();
    }

    private float progresso(long tempo, int atraso, int duracao) {
        return Math.max(0f, Math.min(1f, (tempo - atraso) / (float) duracao));
    }
    private float fade(long tempo, int atraso, int duracao) {
        float p = progresso(tempo, atraso, duracao);
        return p * p * (3f - 2f * p);
    }

    @Override public void doLayout() {
        int x = (getWidth() - 960) / 2, y = (getHeight() - 540) / 2;
        boolean duas = manutencao.usaDuasEntradas();
        entradaA.setBounds(x + 54, y + (duas ? 190 : 225), 180, 43);
        entradaB.setBounds(x + 54, y + 273, 180, 43);
        int i = 0;
        for (Operador op : Operador.values()) modulos.get(op).setBounds(x + 152 + i++ * 146, y + 359, 136, 43);
        testar.setBounds(x + 620, y + 357, 285, 45);
        menu.setBounds(x + 150, y + 485, 150, 48);
        continuar.setBounds(x + 625, y + 482, 280, 54);
    }

    @Override protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D c = (Graphics2D) g.create();
        c.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
        c.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
        c.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);
        c.drawImage(fundo, 0, 0, getWidth(), getHeight(), this);
        c.setColor(new Color(0, 0, 0, 165));
        c.fillRect(0, 0, getWidth(), getHeight());
        c.translate((getWidth() - 960) / 2, (getHeight() - 540) / 2);
        desenharCabecalho(c);
        desenharCircuito(c);
        desenharBancada(c);
        desenharFeedback(c);
        desenharZelador(c);
        if (respondida) desenharRecompensa(c);
        c.dispose();
    }

    private String setor() {
        return switch (fase) { case 1 -> "SEGURANÇA"; case 2 -> "EMERGÊNCIA"; default -> "ILUMINAÇÃO"; };
    }
    private String nomeA() {
        return switch (fase) { case 1 -> "A - CRACHÁ VÁLIDO"; case 2 -> "A - FUMAÇA"; default -> "A - HÁ CLARIDADE"; };
    }
    private String nomeB() { return fase == 1 ? "B - AUTORIZAÇÃO" : "B - TEMPERATURA ALTA"; }
    private String estadoDispositivo(boolean ativo) {
        return switch (fase) { case 1 -> ativo ? "ABERTA" : "FECHADA"; case 2 -> ativo ? "DISPARADO" : "SILENCIOSO"; default -> ativo ? "ACESA" : "APAGADA"; };
    }

    private void desenharCabecalho(Graphics2D c) {
        placa(c, 24, 15, 595, 50, DOURADO, INTERIOR);
        texto(c, String.format("%02d / 03 - SETOR DE %s", fase, setor()), 321, 40, 21, DOURADO, 554);
        placa(c, 630, 15, 306, 50, DOURADO, INTERIOR);
        texto(c, "TOKENS: " + jogo.tokens() + "/3", 738, 40, 20, DOURADO, 175);
        for (int i = 0; i < 3; i++) led(c, 831 + i * 26, 31, 20, 12, i < jogo.tokens() ? VERDE : new Color(48, 65, 78));
        placa(c, 24, 75, 912, 46, METAL, INTERIOR);
        texto(c, manutencao.descricao(), 480, 98, 19, Color.WHITE, 866);
    }

    private void desenharCircuito(Graphics2D c) {
        placa(c, 24, 130, 912, 204, METAL, new Color(15, 23, 30));
        // Trilhos técnicos atrás das conexões.
        c.setColor(new Color(32, 43, 52));
        for (int x = 32; x < 925; x += 16) c.drawLine(x, 139, x, 325);
        for (int y = 139; y < 325; y += 16) c.drawLine(32, y, 925, y);
        texto(c, respondida ? "EQUIPAMENTO RESTAURADO" : "ÁREA DE TESTE", 326, 146, 15,
                respondida ? VERDE : DOURADO, 570);
        int ya = manutencao.usaDuasEntradas() ? 211 : 246;
        int yb = 294;
        desenharEntrada(c, nomeA(), 44, ya - 42, valorA);
        if (manutencao.usaDuasEntradas()) {
            desenharEntrada(c, nomeB(), 44, yb - 42, valorB);
        }
        fio(c, 244, ya, 281, manutencao.usaDuasEntradas() ? 218 : 243, 320, valorA ? VERDE : INATIVO);
        if (manutencao.usaDuasEntradas()) fio(c, 244, yb, 300, 268, 320, valorB ? VERDE : INATIVO);
        fio(c, 590, 238, 615, 238, 645, saida() ? VERDE : INATIVO);

        placa(c, 320, 169, 270, 145, respondida ? VERDE : METAL, INTERIOR);
        texto(c, "CONTROLADOR", 455, 190, 16, DOURADO, 243);
        // O slot permanece neutro: não exibe o operador marcado.
        placa(c, 376, 211, 158, 65, respondida ? VERDE : METAL, new Color(9, 15, 20));
        texto(c, respondida ? "OK" : "?", 455, 243, 35,
                respondida ? VERDE : INATIVO, 125);
        texto(c, respondida ? "RESTAURADO" : "AGUARDANDO", 455, 295, 15,
                respondida ? VERDE : INATIVO, 243);

        placa(c, 645, 146, 271, 175, saida() ? VERDE : METAL, new Color(10, 16, 22));
        texto(c, switch(fase) {case 1 -> "PORTA INDUSTRIAL"; case 2 -> "SIRENE DE EMERGÊNCIA"; default -> "LÂMPADA INDUSTRIAL";}, 780, 161, 14, DOURADO, 245);
        switch (fase) {
            case 1 -> desenharPorta(c, 724, 174);
            case 2 -> desenharSirene(c, 780, 225);
            default -> desenharLampada(c, 780, 221);
        }
        texto(c, respondida ? estadoDispositivo(saida()) : "EM ESPERA",
                780, 307, 14, saida() ? VERDE : INATIVO, 246);
    }

    private void fio(Graphics2D c, int x1, int y1, int meio, int y2, int x2, Color cor) {
        int[] xs = {x1, meio, meio, x2}, ys = {y1, y1, y2, y2};
        Stroke anterior = c.getStroke();
        c.setStroke(new BasicStroke(8)); c.setColor(new Color(4, 8, 11)); c.drawPolyline(xs, ys, 4);
        c.setStroke(new BasicStroke(3)); c.setColor(cor); c.drawPolyline(xs, ys, 4);
        c.setStroke(anterior);
        led(c, x2 - 5, y2 - 5, 10, 10, cor);
    }

    /** Os sensores são manipuláveis, sem efeito na resposta. */
    private void desenharEntrada(Graphics2D c, String nome, int x, int y, boolean valor) {
        placa(c, x, y, 200, 70, valor ? VERDE : METAL, INTERIOR);
        texto(c, nome, x + 100, y + 13, 14, DOURADO, 175);
    }

    private void desenharPorta(Graphics2D c, int x, int y) {
        int w=112,h=118;
        c.setColor(new Color(5,9,13)); c.fillRect(x-6,y-3,w+12,h+7);
        c.setColor(new Color(65,84,98)); c.fillRect(x,y,w,h);
        c.setColor(new Color(112,134,149)); c.fillRect(x+3,y+3,w-6,5);
        c.setColor(new Color(4,11,17)); c.fillRect(x+10,y+12,w-20,h-19);
        c.setColor(new Color(24,48,58)); c.fillRect(x+15,y+22,w-30,4);
        int largura = Math.round(46 * (1f-abertura));
        Graphics2D p = (Graphics2D)c.create();
        p.clipRect(x+10,y+12,w-20,h-19);
        for(int i=0;i<2;i++) {
            int px = i==0 ? x+10+largura-46 : x+w-10-largura;
            p.setColor(new Color(57,77,91)); p.fillRect(px,y+12,46,h-19);
            p.setColor(new Color(101,124,140)); p.fillRect(px+2,y+14,42,3);
            p.setColor(new Color(26,39,50)); p.fillRect(px+5,y+20,36,h-39);
            p.setColor(new Color(74,96,110)); p.fillRect(px+8,y+23,30,h-45);
            for(int j=0;j<4;j++){p.setColor(new Color(39,54,65));p.fillRect(px+8,y+30+j*14,30,3);}
            p.setColor(DOURADO);p.fillRect(px+(i==0?38:3),y+53,5,16);
        }
        p.dispose();
        c.setColor(new Color(39,52,64)); c.fillRect(x+3,y+h-7,w-6,5);
        led(c,x+w+8,y+19,12,22,saida()?VERDE:INATIVO);
    }

    private void desenharSirene(Graphics2D c, int x, int y) {
        boolean pisca = saida() && (System.nanoTime()/350_000_000L)%2==0;
        if(pisca){c.setColor(new Color(255,60,45,32));c.fillRect(x-104,y-42,208,80);c.setColor(new Color(255,60,45,50));c.fillRect(x-80,y-32,160,64);}
        c.setColor(new Color(27,38,48));c.fillRect(x-51,y+27,102,20);
        c.setColor(new Color(95,114,128));c.fillRect(x-46,y+29,92,5);
        c.setColor(new Color(5,9,13));c.fillRect(x-40,y-37,80,68);
        c.setColor(pisca?new Color(255,75,60):new Color(86,29,33));c.fillRect(x-34,y-32,68,58);
        c.setColor(pisca?new Color(255,201,142):new Color(135,54,51));c.fillRect(x-29,y-27,12,44);
        c.setColor(new Color(30,36,42));for(int i=0;i<5;i++)c.fillRect(x-37,y-22+i*11,74,3);
        if(pisca){c.setColor(VERMELHO);c.fillRect(x-73,y-4,22,4);c.fillRect(x+51,y-4,22,4);c.fillRect(x-2,y-55,4,12);}
    }

    private void desenharLampada(Graphics2D c, int x, int y) {
        if(saida()){
            c.setColor(new Color(255,220,120,26)); c.fillPolygon(new int[]{x-40,x+40,x+105,x-105},new int[]{y+4,y+4,y+63,y+63},4);
            c.setColor(new Color(255,225,145,38)); c.fillPolygon(new int[]{x-30,x+30,x+73,x-73},new int[]{y+4,y+4,y+63,y+63},4);
        }
        c.setColor(new Color(80,100,116)); c.fillRect(x-6,y-46,12,28);
        c.setColor(new Color(4,9,13));c.fillRect(x-63,y-23,126,37);
        c.setColor(new Color(65,83,98));c.fillRect(x-59,y-19,118,29);
        c.setColor(new Color(119,140,155));c.fillRect(x-56,y-17,112,4);
        c.setColor(saida()?new Color(255,225,139):new Color(43,53,61));c.fillRect(x-50,y-6,100,11);
        if(saida()){c.setColor(new Color(255,251,213));c.fillRect(x-45,y-4,90,4);}
        c.setColor(new Color(34,47,57));for(int i=0;i<5;i++)c.fillRect(x-48+i*23,y-9,4,17);
    }

    private void desenharBancada(Graphics2D c) {
        placa(c, 140, 340, 796, 66, METAL, INTERIOR);
        if (respondida) {
            texto(c, "CIRCUITO RESTAURADO", 538, 373, 21, VERDE, 740);
            return;
        }
        texto(c, "RESPOSTA", 375, 349, 13, DOURADO, 435);
        int i=0;
        for(Operador op:Operador.values()) {
            if(op==selecionado){c.setColor(DOURADO);c.drawRect(149+i*146,357,141,46);c.drawRect(148+i*146,356,143,48);}
            i++;
        }
        if(testando) {
            c.setColor(DOURADO);c.fillRect(625,344,Math.round(275f*progresso((System.nanoTime()-inicioTeste)/1_000_000,0,TEMPO_DIAGNOSTICO)),3);
        }
    }

    private void desenharFeedback(Graphics2D c) {
        Color cor=respondida?VERDE:erro?VERMELHO:DOURADO;
        Color interior=respondida?new Color(12,35,24):erro?new Color(43,17,21):INTERIOR;
        placa(c,140,415,796,65,cor,interior);
        int centro=respondida?463:538;
        int limite=respondida?610:755;
        texto(c,mensagem,centro,432,18,cor,limite);
        texto(c,(testando?"":"ZELADOR: ")+dica,centro,451,15,Color.WHITE,limite);
        if (respondida) texto(c,"TOKEN RECUPERADO",463,468,12,VERDE,limite);

    }

    private BufferedImage carregarReacoes() {
        var recurso = getClass().getResource("/zelador_reacoes.png");
        if (recurso == null) return null;
        try {
            BufferedImage imagem = ImageIO.read(recurso);
            return imagem != null && imagem.getWidth() == 424
                    && imagem.getHeight() == 318 ? imagem : null;
        } catch (IOException e) {
            return null;
        }
    }

    private void iniciarReacao(boolean positiva, long agora) {
        reacaoPositiva = positiva;
        inicioReacao = agora;
    }

    private int quadroReacao(long agora) {
        if (inicioReacao < 0) return 0;
        long tempo = (agora - inicioReacao) / 1_000_000;
        int[] quadros = reacaoPositiva ? QUADROS_ACERTO : QUADROS_ERRO;
        int[] duracoes = reacaoPositiva ? DURACOES_ACERTO : DURACOES_ERRO;
        for (int i = 0; i < quadros.length; i++) {
            if (tempo < duracoes[i]) return quadros[i];
            tempo -= duracoes[i];
        }
        return 0;
    }

    private void desenharZelador(Graphics2D c) {
        placa(c,24,340,108,188,METAL,INTERIOR);
        if (reacoes != null) {
            int quadro = quadroReacao(System.nanoTime());
            int sx = (quadro % 4) * 106;
            int sy = (quadro / 4) * 159;
            // Todos os quadros têm a mesma área e os pés alinhados.
            c.drawImage(reacoes, 25, 344, 131, 503,
                    sx, sy, sx + 106, sy + 159, this);
        } else {
            int iw=zelador.getWidth(this),ih=zelador.getHeight(this);
            if(iw>0&&ih>0){int h=159,w=h*iw/ih;c.drawImage(zelador,78-w/2,344,w,h,this);}
        }
        texto(c,"ZELADOR",78,514,14,DOURADO,90);
    }

    private void desenharRecompensa(Graphics2D c) {
        long t=(System.nanoTime()-inicioRecompensa)/1_000_000;
        float alpha=fade(t,0,250), p=progresso(t,0,650);
        double escala=1+2.70158*Math.pow(p-1,3)+1.70158*Math.pow(p-1,2);
        int iw=token.getWidth(this),ih=token.getHeight(this);
        if(iw<=0||ih<=0)return;
        Graphics2D r=(Graphics2D)c.create();r.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER,alpha));
        double proporcao=Math.min(146.0/iw,56.0/ih)*Math.max(0,escala);
        int w=(int)(iw*proporcao),h=(int)(ih*proporcao);
        r.drawImage(token,838-w/2,447-h/2,w,h,this);r.dispose();
    }

    private void placa(Graphics2D c,int x,int y,int w,int h,Color detalhe,Color interior){
        c.setColor(new Color(4,7,10));c.fillRect(x+4,y+4,w,h);
        c.setColor(new Color(8,12,16));c.fillRect(x,y,w,h);
        c.setColor(new Color(55,70,83));c.fillRect(x+2,y+2,w-4,h-4);
        c.setColor(interior);c.fillRect(x+7,y+7,w-14,h-14);
        c.setColor(new Color(98,119,136));c.fillRect(x+3,y+3,w-6,2);c.fillRect(x+3,y+3,2,h-6);
        c.setColor(new Color(15,22,28));c.fillRect(x+3,y+h-5,w-6,3);
        c.setColor(detalhe);c.fillRect(x+w/2-28,y+2,56,3);
        for(int px:new int[]{x+9,x+w-15})for(int py:new int[]{y+9,y+h-15}){
            c.setColor(new Color(6,10,14));c.fillRect(px-1,py-1,8,8);
            c.setColor(new Color(99,118,133));c.fillRect(px,py,6,6);
            c.setColor(new Color(28,38,47));c.fillRect(px+1,py+2,4,2);
        }
    }
    private void led(Graphics2D c,int x,int y,int w,int h,Color cor){
        c.setColor(new Color(4,9,12));c.fillRect(x-2,y-2,w+4,h+4);
        c.setColor(cor.darker());c.fillRect(x,y,w,h);c.setColor(cor);c.fillRect(x+2,y+2,w-4,h-4);
    }
    private void texto(Graphics2D c,String s,int cx,int cy,int tamanho,Color cor,int max){
        c.setFont(FonteJogo.tamanho(tamanho));
        while(tamanho>9&&c.getFontMetrics().stringWidth(s)>max)c.setFont(FonteJogo.tamanho(--tamanho));
        FontMetrics fm=c.getFontMetrics();c.setColor(cor);
        c.drawString(s,cx-fm.stringWidth(s)/2,cy+(fm.getAscent()-fm.getDescent())/2);
    }
}

