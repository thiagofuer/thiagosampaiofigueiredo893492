package br.gov.mt.seplag.backendapi.service;

import br.gov.mt.seplag.backendapi.client.RegionalClient;
import br.gov.mt.seplag.backendapi.dto.RegionalDTO;
import br.gov.mt.seplag.backendapi.model.Regional;
import br.gov.mt.seplag.backendapi.repository.RegionalRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class RegionalSyncService {

    private final RegionalClient client;
    private final RegionalRepository repository;
    private final NotificationService notificationService;

    @Scheduled(cron = "0 0 * * * *")
    @Transactional
    public void sincronizar() {
        /**
         * 	e) Endpoint de regionais (https://integrador-argus-api.geia.vip/v1/regionais):
         * 		i) Importar a lista para tabela interna;
         * 		ii) Adicionar atributo “ativo” (regional (id integer, nome varchar(200), ativo boolean));
         * 		iii) Sincronizar com menor complexidade:
         * 			1) Novo no endpoint → inserir;
         * 			2) Ausente no endpoint → inativar;
         * 			3) Atributo alterado → inativar antigo e criar novo registro.
         */

        List<RegionalDTO> externas = client.buscarTodas();
        List<Long> idsExternos = externas.stream().map(RegionalDTO::getId).toList();

        for (RegionalDTO externaAtual : externas) {
            Optional<Regional> regionalLocalOpt = repository.findByIdExternoAndAtivoTrue(externaAtual.getId());

            if (regionalLocalOpt.isEmpty()) {
                //1) Novo no endpoint → inserir;
                salvarNovaRegional(externaAtual);
            } else {
                Regional regionalLocal = regionalLocalOpt.get();
                //3) Atributo alterado → inativar antigo e criar novo registro.
                if (!regionalLocal.getNome().equals(externaAtual.getNome())) {
                    log.info("Regional {} alterada. Inativando ID {} e criando nova versão.", externaAtual.getId(), regionalLocal.getId());
                    //Inativa o antigo
                    regionalLocal.setAtivo(false);
                    repository.save(regionalLocal);
                    //Cria novo registro
                    salvarNovaRegional(externaAtual);
                }
            }
        }

        // Inativar regionais locais que não existem mais no sistema externo
        //2) Ausente no endpoint → inativar;
        repository.findAllByAtivoTrue().stream()
                .filter(local -> !idsExternos.contains(local.getIdExterno()))
                .forEach(local -> {
                    local.setAtivo(false);
                    repository.save(local);
                });
        notificationService.enviarNotificacao("Sincronização de Regionais concluída com sucesso.");
    }

    private void salvarNovaRegional(RegionalDTO dto) {
        Regional nova = new Regional();
        nova.setIdExterno(dto.getId()); // ID da api externa de Regionais
        nova.setNome(dto.getNome());
        nova.setAtivo(true);
        repository.save(nova);
    }
}