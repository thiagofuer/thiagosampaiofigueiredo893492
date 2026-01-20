-- Tabela de Artistas com Tipo Numérico
CREATE TABLE artista (
                         id BIGSERIAL PRIMARY KEY,
                         nome VARCHAR(255) NOT NULL,
                         tipo INTEGER NOT NULL -- 1 para CANTOR, 2 para BANDA, etc..
);

-- Tabela de Álbuns
CREATE TABLE album (
                       id BIGSERIAL PRIMARY KEY,
                       titulo VARCHAR(255) NOT NULL,
                       data_cadastro TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Relacionamento N:N entre Artistas e Álbuns
CREATE TABLE artista_album (
                               artista_id BIGINT REFERENCES artista(id),
                               album_id BIGINT REFERENCES album(id),
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

-- Carga inicial (Exemplo de álbuns do edital)
INSERT INTO album (titulo) VALUES ('Harakiri');
INSERT INTO album (titulo) VALUES ('Post Traumatic');
INSERT INTO album (titulo) VALUES ('Use Your Illusion I');

-- Carga inicial (Construindo N:N entre artistas e álbuns)
INSERT INTO artista_album (artista_id, album_id) VALUES (1, 1);
INSERT INTO artista_album (artista_id, album_id) VALUES (2, 2);
INSERT INTO artista_album (artista_id, album_id) VALUES (4, 3);