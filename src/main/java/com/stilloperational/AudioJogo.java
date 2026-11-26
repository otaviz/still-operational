package com.stilloperational;

import javax.sound.sampled.*;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/** Gerencia a música e os efeitos sonoros do jogo. */
public final class AudioJogo {

    private static final String MUSICA = "musica_fundo.wav";
    private static final String DIGITACAO = "digitacao.wav";
    private static final float VOLUME_MUSICA = -18f;
    private static final float VOLUME_EFEITOS = -8f;
    // Limita a frequência dos disparos, sem mudar a velocidade do texto.
    private static final long INTERVALO_DIGITACAO = 70_000_000L;

    private static final Map<String, Clip> sons = new HashMap<>();
    private static final Set<String> indisponiveis = new HashSet<>();
    private static final ExecutorService audio =
            Executors.newSingleThreadExecutor(tarefa -> {
                Thread thread = new Thread(tarefa, "AudioJogo");
                thread.setDaemon(true);
                return thread;
            });

    private static boolean musicaAtiva;
    // Canal exclusivo para interações: somente o evento mais recente é válido.
    private static final String[] SONS_INTERFACE = {
            "hover.wav", "clique.wav", "entrada.wav"
    };
    private static volatile long revisaoInterface;
    // Acesso protegido pelos métodos synchronized.
    private static boolean digitacaoSolicitada;
    private static long ultimoSomDigitacao;

    private AudioJogo() {}

    /** Reproduz um efeito sem interromper a música. */
    public static synchronized void tocar(String arquivo) {
        if (audio.isShutdown() || MUSICA.equals(arquivo)) return;
        if (somDeInterface(arquivo)) {
            tocarInterface(arquivo);
            return;
        }
        audio.execute(() -> {
            Clip clip = carregar(arquivo);
            if (clip == null) return;
            reiniciar(clip);
            ajustarVolume(clip, VOLUME_EFEITOS);
            clip.start();
        });
    }

    private static boolean somDeInterface(String arquivo) {
        for (String som : SONS_INTERFACE) {
            if (som.equals(arquivo)) return true;
        }
        return false;
    }

    /** Sem cooldown: o último hover, clique ou acionamento de sensor vence. */
    private static void tocarInterface(String arquivo) {
        long revisao = ++revisaoInterface;
        audio.execute(() -> {
            if (revisao != revisaoInterface) return;
            // Corta o anterior, mesmo quando o próximo usa outro arquivo.
            for (String som : SONS_INTERFACE) {
                Clip anterior = sons.get(som);
                if (anterior != null) reiniciar(anterior);
            }
            Clip clip = carregar(arquivo);
            // Uma interação mais nova pode ter ocorrido durante o carregamento.
            if (clip == null || revisao != revisaoInterface) return;
            reiniciar(clip);
            ajustarVolume(clip, VOLUME_EFEITOS);
            clip.loop(0); // Uma reprodução completa, iniciada do primeiro frame.
        });
    }

    private static void reiniciar(Clip clip) {
        clip.stop();
        clip.flush();
        clip.setFramePosition(0);
    }

    /** Reproduz um efeito em loop até pararEfeito ser chamado. */
    public static synchronized void tocarEmLoop(String arquivo) {
        if (audio.isShutdown() || MUSICA.equals(arquivo) || somDeInterface(arquivo)) return;
        audio.execute(() -> {
            Clip clip = carregar(arquivo);
            if (clip == null) return;
            reiniciar(clip);
            ajustarVolume(clip, VOLUME_EFEITOS);
            clip.loop(Clip.LOOP_CONTINUOUSLY);
        });
    }

    /** Toca somente durante a revelação gradual de caracteres visíveis. */
    public static synchronized void tocarDigitacao(String texto, int antes, int depois) {
        if (audio.isShutdown() || depois <= antes) return;
        if (depois >= texto.length()) {
            pararDigitacao();
            return;
        }
        boolean caractereVisivel = false;
        for (int i = antes; i < depois; i++) {
            if (!Character.isWhitespace(texto.charAt(i))) {
                caractereVisivel = true;
                break;
            }
        }
        if (!caractereVisivel) return;
        long agora = System.nanoTime();
        if (digitacaoSolicitada && agora - ultimoSomDigitacao < INTERVALO_DIGITACAO) return;
        digitacaoSolicitada = true;
        ultimoSomDigitacao = agora;
        tocar(DIGITACAO);
    }

    /** Interrompe somente a digitação, inclusive quando o texto é pulado. */
    public static synchronized void pararDigitacao() {
        if (!digitacaoSolicitada) return;
        digitacaoSolicitada = false;
        if (audio.isShutdown()) return;
        audio.execute(() -> {
            Clip clip = sons.get(DIGITACAO);
            if (clip != null) {
                reiniciar(clip);
            }
        });
    }

    /** Interrompe um efeito específico sem interferir na música. */
    public static synchronized void pararEfeito(String arquivo) {
        if (audio.isShutdown() || MUSICA.equals(arquivo)) return;
        if (DIGITACAO.equals(arquivo)) {
            pararDigitacao();
            return;
        }
        audio.execute(() -> {
            Clip clip = sons.get(arquivo);
            if (clip != null) {
                reiniciar(clip);
            }
        });
    }

    /** Inicia a música uma única vez, em repetição contínua. */
    public static synchronized void iniciarMusica() {
        if (audio.isShutdown()) return;
        audio.execute(() -> {
            if (musicaAtiva) return;
            Clip clip = carregar(MUSICA);
            if (clip == null) return;
            ajustarVolume(clip, VOLUME_MUSICA);
            clip.setFramePosition(0);
            clip.loop(Clip.LOOP_CONTINUOUSLY);
            musicaAtiva = true;
            for (String som : SONS_INTERFACE) carregar(som);
        });
    }

    /** Interrompe somente a música de fundo. */
    public static synchronized void pararMusica() {
        if (audio.isShutdown()) return;
        audio.execute(() -> {
            Clip clip = sons.get(MUSICA);
            if (clip != null) clip.stop();
            musicaAtiva = false;
        });
    }

    /** Carrega e armazena os arquivos de áudio. */
    private static Clip carregar(String arquivo) {
        if (indisponiveis.contains(arquivo)) return null;
        if (sons.containsKey(arquivo)) return sons.get(arquivo);
        Clip clip = null;
        try (InputStream recurso = AudioJogo.class.getResourceAsStream("/audio/" + arquivo)) {
            if (recurso == null) throw new IOException("Arquivo não encontrado.");
            try (AudioInputStream entrada = AudioSystem.getAudioInputStream(new BufferedInputStream(recurso))) {
                clip = AudioSystem.getClip();
                clip.open(entrada);
            }
            sons.put(arquivo, clip);
            return clip;
        } catch (Exception e) {
            if (clip != null) clip.close();
            indisponiveis.add(arquivo);
            System.err.println("Áudio: " + arquivo + " — " + e.getMessage());
            return null;
        }
    }

    /** Ajusta o volume em decibéis. */
    private static void ajustarVolume(Clip clip, float decibeis) {
        if (!clip.isControlSupported(FloatControl.Type.MASTER_GAIN)) return;
        FloatControl controle = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
        controle.setValue(Math.max(controle.getMinimum(), Math.min(controle.getMaximum(), decibeis)));
    }

    /** Libera os recursos ao encerrar o jogo. */
    public static synchronized void encerrar() {
        if (audio.isShutdown()) return;
        digitacaoSolicitada = false;
        revisaoInterface++;
        audio.execute(() -> {
            for (Clip clip : sons.values()) {
                clip.stop();
                clip.close();
            }
            sons.clear();
            musicaAtiva = false;
        });
        audio.shutdown();
    }
}
