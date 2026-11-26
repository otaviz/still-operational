package com.stilloperational;

import java.awt.Font;

public class FonteJogo {

    private static final Font FONTE = carregar();

    private static Font carregar() {
        try (var arquivo = FonteJogo.class.getResourceAsStream(
                "/DiaryOfAn8BitMage-lYDD.ttf")) {

            return Font.createFont(Font.TRUETYPE_FONT, arquivo);

        } catch (Exception e) {
            throw new IllegalStateException("Erro ao carregar a fonte.", e);
        }
    }

    public static Font tamanho(float tamanho) {
        return FONTE.deriveFont(tamanho);
    }
}