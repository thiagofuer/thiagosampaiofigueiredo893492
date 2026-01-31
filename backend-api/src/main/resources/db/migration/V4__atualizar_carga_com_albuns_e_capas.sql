-- 1. Limpeza da carga inicial simplificada da V1 (mas mantendo os artistas que estão corretos)
DELETE FROM artista_album;
DELETE FROM album;

-- 2. Inserção de Álbuns com as capas mapeadas (mapeando para os nomes de arquivos adicionados ao resources/data/images)
-- Serj Tankian
INSERT INTO album (titulo, imagem_capa) VALUES ('Harakiri', 'Harakiri.jpg');
INSERT INTO album (titulo, imagem_capa) VALUES ('Black Blooms', 'Black_Blooms.jpg');
INSERT INTO album (titulo, imagem_capa) VALUES ('The Rough Dog', 'The_Rough_Dog.png');

-- Mike Shinoda
INSERT INTO album (titulo, imagem_capa) VALUES ('The Rising Tied', 'The_Rising_Tied.jpg');
INSERT INTO album (titulo, imagem_capa) VALUES ('Post Traumatic', 'Post_Traumatic.jpg');
INSERT INTO album (titulo, imagem_capa) VALUES ('Post Traumatic EP', 'Post_Traumatic_EP.jpg');
INSERT INTO album (titulo, imagem_capa) VALUES ('Where dYou Go', 'Where_dYou_Go.jpg');

-- Michel Teló
INSERT INTO album (titulo, imagem_capa) VALUES ('Bem Sertanejo', 'Bem_Sertanejo.jpg');
INSERT INTO album (titulo, imagem_capa) VALUES ('Bem Sertanejo - O Show (Ao Vivo)', 'Bem_Sertanejo_O Show.jpeg');
INSERT INTO album (titulo, imagem_capa) VALUES ('Bem Sertanejo - (1ª Temporada) - EP', 'Bem_Sertanejo_1_Temporada.jpeg');

-- Guns N Roses
INSERT INTO album (titulo, imagem_capa) VALUES ('Use Your Illusion I', 'Use_Your_Illusion_1.jpg');
INSERT INTO album (titulo, imagem_capa) VALUES ('Use Your Illusion II', 'Use_Your_Illusion_2.jpg');
INSERT INTO album (titulo, imagem_capa) VALUES ('Greatest Hits', 'Greatest_Hits.jpg');

-- 3. Reconstruindo os relacionamentos N:N (Baseado nos IDs fixos da V1: 1-Serj, 2-Mike, 3-Michel, 4-Guns)

-- Serj Tankian (ID 1)
INSERT INTO artista_album (artista_id, album_id) SELECT 1, id FROM album WHERE titulo IN ('Harakiri', 'Black Blooms', 'The Rough Dog');

-- Mike Shinoda (ID 2)
INSERT INTO artista_album (artista_id, album_id) SELECT 2, id FROM album WHERE titulo IN ('The Rising Tied', 'Post Traumatic', 'Post Traumatic EP', 'Where dYou Go');

-- Michel Teló (ID 3)
INSERT INTO artista_album (artista_id, album_id) SELECT 3, id FROM album WHERE titulo IN ('Bem Sertanejo', 'Bem Sertanejo - O Show (Ao Vivo)', 'Bem Sertanejo - (1ª Temporada) - EP');

-- Guns N Roses (ID 4)
INSERT INTO artista_album (artista_id, album_id) SELECT 4, id FROM album WHERE titulo IN ('Use Your Illusion I', 'Use Your Illusion II', 'Greatest Hits');