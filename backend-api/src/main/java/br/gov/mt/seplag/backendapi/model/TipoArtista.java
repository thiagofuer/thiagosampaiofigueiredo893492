package br.gov.mt.seplag.backendapi.model;

public enum TipoArtista {
    CANTOR(1),
    BANDA(2);

    private final int codigo;

    TipoArtista(int codigo) {
        this.codigo = codigo;
    }

    public int getCodigo() {
        return codigo;
    }
}