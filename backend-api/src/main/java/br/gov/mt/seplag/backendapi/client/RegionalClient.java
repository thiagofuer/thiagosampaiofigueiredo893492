package br.gov.mt.seplag.backendapi.client;

import br.gov.mt.seplag.backendapi.dto.RegionalDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@FeignClient(name = "regional-service", url = "${api.external.regionais.url}")
public interface RegionalClient {
    @GetMapping("/regionais")
    List<RegionalDTO> buscarTodas();
}