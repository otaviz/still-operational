package com.stilloperational;

public enum Operador {
    AND, OR, NOT;

    public boolean aplicar(boolean a, boolean b) {
        return switch (this) {
            case AND -> a && b;
            case OR -> a || b;
            case NOT -> !a;
        };
    }
}
