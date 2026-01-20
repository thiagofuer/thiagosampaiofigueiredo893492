-- Tabela de Artistas com Tipo Numérico
CREATE TABLE artista (
                         id SERIAL PRIMARY KEY,
                         nome VARCHAR(255) NOT NULL,
                         tipo INTEGER NOT NULL -- 1 para CANTOR, 2 para BANDA, etc..
);

-- Tabela de Álbuns
CREATE TABLE album (
                       id SERIAL PRIMARY KEY,
                       titulo VARCHAR(255) NOT NULL,
                       data_cadastro TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Relacionamento N:N entre Artistas e Álbuns
CREATE TABLE artista_album (
                               artista_id INTEGER REFERENCES artista(id),
                               album_id INTEGER REFERENCES album(id),
                               PRIMARY KEY (artista_id, album_id)
);

-- Tabela de Regionais (Requisito para Sênior)
CREATE TABLE regional (
                          id INTEGER PRIMARY KEY,
                          nome VARCHAR(200) NOT NULL,
                          ativo BOOLEAN NOT NULL DEFAULT TRUE
);

-- Carga inicial (Artistas do exemplo do edital)
INSERT INTO artista (nome, tipo) VALUES ('Serj Tankian', 1); -- CANTOR
INSERT INTO artista (nome, tipo) VALUES ('Mike Shinoda', 1); -- CANTOR
INSERT INTO artista (nome, tipo) VALUES ('Michel Teló', 1); -- CANTOR
INSERT INTO artista (nome, tipo) VALUES ('Guns N Roses', 2); -- BANDA