package com.stilloperational;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;

/**
 * Controla a navegação, as transições entre telas
 * e a partida atual.
 */
public class JanelaPrincipal extends JFrame {

    private Jogo jogo;

    private static final int DURACAO_FADE_OUT = 300;
    private static final int DURACAO_FADE_IN = 400;

    private final JPanel camadaTransicao;
    private final Timer timerTransicao;

    private JPanel proximaTela;

    private boolean emTransicao = false;
    private boolean telaSubstituida = false;

    private float opacidadeTransicao = 0f;

    private long inicioTransicao;

    private final KeyEventDispatcher bloqueadorTeclado =
            evento -> emTransicao && isActive();

    public JanelaPrincipal() {

        setTitle("Still Operational");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setResizable(false);

        // Camada responsável pelas transições

        camadaTransicao = new JPanel() {

            @Override
            protected void paintComponent(Graphics g) {

                super.paintComponent(g);

                Graphics2D g2 = (Graphics2D) g.create();

                g2.setComposite(
                        AlphaComposite.getInstance(
                                AlphaComposite.SRC_OVER,
                                opacidadeTransicao
                        )
                );

                g2.setColor(Color.BLACK);

                g2.fillRect(
                        0, 0,
                        getWidth(),
                        getHeight()
                );

                g2.dispose();
            }
        };

        camadaTransicao.setOpaque(false);

        // Bloqueia interações durante as transições

        MouseAdapter bloqueadorMouse = new MouseAdapter() {

            @Override
            public void mousePressed(MouseEvent e) {
                e.consume();
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                e.consume();
            }

            @Override
            public void mouseClicked(MouseEvent e) {
                e.consume();
            }

            @Override
            public void mouseMoved(MouseEvent e) {
                e.consume();
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                e.consume();
            }

            @Override
            public void mouseWheelMoved(MouseWheelEvent e) {
                e.consume();
            }
        };

        camadaTransicao.addMouseListener(
                bloqueadorMouse
        );

        camadaTransicao.addMouseMotionListener(
                bloqueadorMouse
        );

        camadaTransicao.addMouseWheelListener(
                bloqueadorMouse
        );

        setGlassPane(camadaTransicao);

        camadaTransicao.setVisible(false);

        timerTransicao = new Timer(
                16, e -> atualizarTransicao()
        );

        // Primeira tela do jogo

        trocarTela(new PainelFlames(this::mostrarIntroducao));

        getContentPane().setPreferredSize(
                new Dimension(960, 540)
        );

        pack();
        setLocationRelativeTo(null);

        // Uma única trilha durante toda a execução.
        AudioJogo.iniciarMusica();
    }

    /**
     * Apresenta a introdução narrativa.
     * Permite continuar ou pular diretamente ao menu.
     */
    private void mostrarIntroducao() {

        trocarTela(new PainelIntroducao(
                this::mostrarToken,
                this::mostrarMenu
        ));
    }

    /**
     * Apresenta os tokens operacionais.
     */
    private void mostrarToken() {

        trocarTela(new PainelApresentacao(
                "TOKEN OPERACIONAL",

                new String[]{
                        "Estes cartões armazenam as permissões para reiniciar o servidor.",
                        "Cada manutenção concluída libera um token.",
                        "Sim, em pleno futuro, você ainda precisa inserir cartões manualmente.",
                        "Não questione o orçamento da empresa."
                },

                "/token.png",
                280,
                190,

                this::mostrarZelador,
                this::mostrarMenu
        ));
    }

    /**
     * Apresenta o personagem principal.
     */
    private void mostrarZelador() {

        trocarTela(new PainelApresentacao(
                "FUNCIONÁRIO DE MANUTENÇÃO",

                new String[]{
                        "Este é você.",
                        "Aparentemente, o único funcionário que entende lógica booleana.",
                        "Resolva as falhas, recupere os tokens e reinicie o servidor.",
                        "Nada disso estava na descrição da sua vaga."
                },

                "/zelador.png",
                180,
                210,

                this::mostrarMenu,
                this::mostrarMenu
        ));
    }

    /**
     * Apresenta o menu principal.
     */
    private void mostrarMenu() {

        trocarTela(new PainelMenu(
                this::iniciarJogo,
                this::dispose
        ));
    }

    /**
     * Cria uma nova partida.
     */
    private void iniciarJogo() {

        if (emTransicao) {
            return;
        }

        jogo = new Jogo();

        mostrarTreinamento();
    }

    /**
     * Apresenta o treinamento antes da primeira manutenção.
     * Tanto a conclusão quanto o botão PULAR levam ao primeiro setor.
     */
    private void mostrarTreinamento() {

        trocarTela(new PainelTreinamento(
                this::mostrarMonologo
        ));
    }

    /**
     * Apresenta o diálogo correspondente
     * à manutenção atual.
     */
    private void mostrarMonologo() {

        if (jogo.terminou()) {

            mostrarFinal();

            return;
        }

        String setor;
        String dialogo;

        switch (jogo.faseAtual()) {

            case 1 -> {

                setor = "SETOR DE SEGURANÇA";

                dialogo = """
                        Primeira parada: controle de acesso.

                        O crachá identifica o funcionário. A autorização
                        confirma se ele pode entrar. Um só não basta.

                        Vamos poupar uma caminhada até a recepção.
                        """;
            }

            case 2 -> {

                setor = "SETOR DE EMERGÊNCIA";

                dialogo = """
                        Agora, o sistema de emergência.

                        Fumaça já é motivo para disparar o alarme.
                        Temperatura excessiva também.

                        Prefiro um alarme falso a um extintor vazio.
                        """;
            }

            case 3 -> {

                setor = "SETOR DE ILUMINAÇÃO";

                dialogo = """
                        Última parada: iluminação.

                        Quando há claridade, as lâmpadas ficam apagadas.
                        Quando escurece, precisam acender.

                        Até que enfim, uma manutenção que faz sentido.
                        """;
            }

            default -> throw new IllegalStateException(
                    "Manutenção inexistente."
            );
        }

        trocarTela(new PainelMonologo(
                setor,
                dialogo,
                this::mostrarManutencao
        ));
    }

    /**
     * Apresenta o exercício sem reiniciar a partida.
     */
    private void mostrarManutencao() {

        trocarTela(new PainelJogo(
                jogo,
                this::mostrarMenu,
                this::mostrarMonologo
        ));
    }

    /**
     * Apresenta a reinicialização do servidor central.
     */
    private void mostrarFinal() {

        trocarTela(new PainelFinal(
                this::mostrarMenu
        ));
    }

    /**
     * Inicia uma transição entre duas telas.
     */
    private void trocarTela(JPanel painel) {

        // A primeira tela não precisa de fade-out

        if (!isShowing()) {

            setContentPane(painel);

            revalidate();
            repaint();

            return;
        }

        // Impede transições simultâneas

        if (emTransicao) {
            return;
        }

        proximaTela = painel;

        emTransicao = true;
        telaSubstituida = false;

        opacidadeTransicao = 0f;

        inicioTransicao = System.nanoTime();

        camadaTransicao.setVisible(true);

        KeyboardFocusManager
                .getCurrentKeyboardFocusManager()
                .addKeyEventDispatcher(
                        bloqueadorTeclado
                );

        timerTransicao.start();
    }

    /**
     * Executa o fade-out, substitui a tela
     * e executa o fade-in.
     */
    private void atualizarTransicao() {

        long tempo = (
                System.nanoTime() - inicioTransicao
        ) / 1_000_000;

        // Primeira etapa: fade-out

        if (!telaSubstituida) {

            float progresso = Math.min(
                    1f,
                    tempo / (float) DURACAO_FADE_OUT
            );

            opacidadeTransicao = suavizar(progresso);

            // Troca a tela quando estiver completamente preta

            if (progresso >= 1f) {

                opacidadeTransicao = 1f;

                setContentPane(proximaTela);

                revalidate();
                repaint();

                telaSubstituida = true;

                inicioTransicao = System.nanoTime();
            }

        } else {

            // Segunda etapa: fade-in

            float progresso = Math.min(
                    1f,
                    tempo / (float) DURACAO_FADE_IN
            );

            opacidadeTransicao =
                    1f - suavizar(progresso);

            if (progresso >= 1f) {

                finalizarTransicao();
            }
        }

        camadaTransicao.repaint();
    }

    /**
     * Encerra a animação e libera as interações.
     */
    private void finalizarTransicao() {

        timerTransicao.stop();

        opacidadeTransicao = 0f;

        camadaTransicao.setVisible(false);

        KeyboardFocusManager
                .getCurrentKeyboardFocusManager()
                .removeKeyEventDispatcher(
                        bloqueadorTeclado
                );

        emTransicao = false;
        telaSubstituida = false;

        proximaTela = null;

        revalidate();
        repaint();
    }

    /**
     * Aplica uma curva Smoothstep para suavizar
     * o início e o fim da animação.
     */
    private float suavizar(float progresso) {

        return progresso * progresso
                * (3f - 2f * progresso);
    }

    /**
     * Encerra os recursos da janela.
     */
    @Override
    public void dispose() {

        timerTransicao.stop();

        KeyboardFocusManager
                .getCurrentKeyboardFocusManager()
                .removeKeyEventDispatcher(
                        bloqueadorTeclado
                );

        AudioJogo.encerrar();
        super.dispose();
    }

    public static void main(String[] args) {

        SwingUtilities.invokeLater(() -> {
            new JanelaPrincipal().setVisible(true);
        });
    }
}
