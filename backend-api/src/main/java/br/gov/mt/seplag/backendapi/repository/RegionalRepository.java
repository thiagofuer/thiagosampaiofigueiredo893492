package br.gov.mt.seplag.backendapi.repository;

import br.gov.mt.seplag.backendapi.model.Regional;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RegionalRepository extends JpaRepository<Regional, Long> {
    Optional<Regional> findByIdExternoAndAtivoTrue(Long idExterno);
    List<Regional> findAllByAtivoTrue();
}
