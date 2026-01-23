DROP TABLE IF EXISTS regional;
CREATE TABLE regional (
                          id BIGSERIAL PRIMARY KEY,
                          id_externo BIGINT NOT NULL, -- ID que vem da API externa de Regionais
                          nome VARCHAR(100) NOT NULL,
                          ativo BOOLEAN DEFAULT TRUE
);

-- Index do id_externo para buscas rápidas durante a sincronização
CREATE INDEX idx_regional_id_externo ON regional(id_externo);