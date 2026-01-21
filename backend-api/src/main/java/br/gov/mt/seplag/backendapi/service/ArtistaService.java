package br.gov.mt.seplag.backendapi.service;

import br.gov.mt.seplag.backendapi.dto.ArtistaDTO;
import br.gov.mt.seplag.backendapi.model.Artista;
import br.gov.mt.seplag.backendapi.repository.ArtistaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ArtistaService {

    private final ArtistaRepository repository;

    @Transactional
    public ArtistaDTO salvar(ArtistaDTO dto) {
        Artista artista = toEntity(dto);
        artista = repository.save(artista);
        return toDTO(artista);
    }

    @Transactional(readOnly = true)
    public ArtistaDTO buscarPorId(Long id) {
        return repository.findById(id)
                .map(this::toDTO)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Artista não encontrado"));
    }

    @Transactional
    @SuppressWarnings("unused")
    public ArtistaDTO atualizar(Long id, ArtistaDTO dto) {
        Artista artistaExistente = repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Artista não encontrado"));

        artistaExistente.setNome(dto.getNome());
        artistaExistente.setTipo(dto.getTipo());

        return toDTO(repository.save(artistaExistente));
    }

    @Transactional
    public void excluir(Long id) {
        Artista artista = repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Artista não encontrado"));

        //Como o relacionamento é N:N, precisamos remover as associações antes de excluir o artista
        artista.getAlbuns().clear();
        repository.delete(artista);
    }

    @Transactional(readOnly = true)
    public List<ArtistaDTO> listarComFiltro(String nome, String direcao) {
        // ordena por padrão ASC se não for informado DESC

        Sort sort = Sort.by("nome");
        sort = "desc".equalsIgnoreCase(direcao) ? sort.descending() : sort.ascending();

        // Lógica de busca: se houver nome, filtra; se não, traz todos ordenados
        List<Artista> artistas;
        if (nome != null && !nome.isBlank()) {
            artistas = repository.findByNomeContainingIgnoreCase(nome, sort);
        } else {
            artistas = repository.findAll(sort);
        }

        return artistas.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    // Métodos auxiliares de conversão
    private ArtistaDTO toDTO(Artista artista) {
        ArtistaDTO dto = new ArtistaDTO();
        dto.setId(artista.getId());
        dto.setNome(artista.getNome());
        dto.setTipo(artista.getTipo());
        return dto;
    }

    private Artista toEntity(ArtistaDTO dto) {
        Artista artista = new Artista();
        artista.setId(dto.getId());
        artista.setNome(dto.getNome());
        artista.setTipo(dto.getTipo());
        return artista;
    }


}