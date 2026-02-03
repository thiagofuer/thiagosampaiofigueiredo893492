# 📋 Identificação do Candidato e Vaga

* **Candidato:** Thiago Sampaio Figueiredo
* **Projeto:** PROJETO PRÁTICO - IMPLEMENTAÇÃO BACK END JAVA SÊNIOR
* **Processo Seletivo:** Edital nº 001/2026/SEPLAG-MT
* **Vaga:** Engenheiro da Computação / SÊNIOR. (Foco em Backend Java)
* **Órgão:** Secretaria de Estado de Planejamento e Gestão de Mato Grosso (SEPLAG-MT)

# Projeto: Backend API - Gestão de Álbuns e Sincronização de Regionais

Este projeto é uma API RESTful robusta desenvolvida para o gerenciamento de álbuns musicais, com integração a serviços de armazenamento de objetos (S3/MinIO), notificações em tempo real e um sistema inteligente de sincronização de dados externos.

## 🏛️ Justificativa da Arquitetura

A solução foi estruturada para garantir escalabilidade, segurança e integridade referencial, seguindo as melhores práticas de desenvolvimento de sistemas modernos:

* **Sincronização com Versionamento (Auditabilidade)**: Em vez de um simples processo de atualização (Update), implementamos uma lógica de sincronização para Regionais que inativa registros antigos (`ativo = false`) e insere novas versões. Esta abordagem preserva o histórico de dados e a rastreabilidade, requisitos fundamentais para sistemas de gestão pública.
* **Armazenamento Seguro com S3/MinIO**: As imagens de capa dos álbuns são geridas via **URLs Pré-assinadas**. O backend gera links temporários com expiração de 30 minutos, permitindo o acesso seguro aos ficheiros diretamente pelo cliente, sem expor o bucket publicamente ou sobrecarregar a largura de banda da API.
* **Comunicação Real-Time**: A utilização do protocolo **STOMP sobre SockJS** providencia uma interface reativa, notificando os utilizadores instantaneamente sobre a conclusão de processos pesados (como a sincronização) ou novos registos, melhorando significativamente a experiência do utilizador.
* **Segurança Stateless**: A arquitetura baseia-se em **JWT (JSON Web Token)** para autenticação, eliminando a necessidade de estado no servidor (Sessionless) e facilitando a escalabilidade horizontal da aplicação.
* **Observabilidade**: A implementação de **Health Checks** customizados permite uma monitorização ativa da saúde do sistema, garantindo que dependências críticas como o PostgreSQL e o MinIO estão operacionais.

## 🚀 Como Executar o Projeto

Siga os passos abaixo para configurar e rodar a aplicação em seu ambiente local:

### Pré-requisitos
* **Docker e Docker Compose**: Para orquestração da infraestrutura.
* **Java 21 (JDK)**: Versão necessária para compilação e execução.
* **Maven 3.9+**: Para gerenciamento de dependências e build.

### 1. Subir a Infraestrutura
Na raiz do projeto, execute o comando para subir o Banco de Dados (PostgreSQL) e o Storage (MinIO):
```bash
docker-compose up -d
```

### 2. Executar Testes e Relatório de Cobertura
Para validar as regras de negócio e gerar o relatório de cobertura do **JaCoCo**, utilize o comando:

```bash
mvn clean test
```

Após a execução, o relatório detalhado estará disponível em: `target/site/jacoco/index.html`. Ele apresenta a porcentagem de instruções e linhas de código cobertas pelos testes unitários.

### 3. Iniciar a Aplicação
Com a infraestrutura ativa e os testes validados, execute a aplicação via Maven:

```bash
mvn spring-boot:run
```

A API estará acessível em `http://localhost:8080` e a documentação interativa do Swagger poderá ser consultada no endereço `/swagger-ui/index.html`.

### ✅ Acessando o Health Check

1. Garanta que a aplicação esteja em execução localmente (`mvn spring-boot:run`) e acessível em `http://localhost:8080`.
2. Utilize o Actuator para verificar o estado geral do serviço em `http://localhost:8080/actuator/health`.
3. Você pode realizar a requisição via navegador ou com `curl`:

```bash
curl -X GET http://localhost:8080/actuator/health
```

Uma resposta `{"status":"UP"}` indica que todos os componentes monitorados estão saudáveis. Em caso de falhas, o payload exibirá quais dependências exigem atenção.


## 📡 Teste de Notificações em Tempo Real

Para validar o funcionamento das mensagens reativas sem a necessidade de um frontend externo complexo:

1. **Acesse a Página de Teste**: Abra o navegador no endereço `http://localhost:8080/index.html`.
2. **Verifique a Conexão**: O status na tela deve mudar para **"Conectado!"**. Esta página utiliza codificação UTF-8 para garantir a renderização correta de caracteres especiais.
3. **Dispare um Evento**: Utilize o Postman ou `curl` para realizar uma sincronização de regionais (`POST /v1/admin/regionais/sincronizar`) ou cadastrar um novo álbum.
4. **Valide o Recebimento**: A notificação enviada pelo servidor via protocolo **STOMP** aparecerá instantaneamente na lista da página, confirmando que o handshake do WebSocket e o Broker de mensagens estão operacionais.

## 🛠️ Tecnologias Utilizadas

O projeto foi construído utilizando um stack tecnológico moderno para garantir alta performance e integração contínua:

* **Spring Boot 3.4/3.5**: Framework base para construção da API RESTful e gerenciamento de dependências.
* **Spring Security & JWT**: Implementação de segurança stateless para autenticação e autorização de usuários.
* **Spring Data JPA**: Abstração da camada de persistência utilizando o Hibernate como provedor.
* **Spring Cloud OpenFeign**: Cliente HTTP declarativo para simplificar o consumo da API externa de regionais.
* **PostgreSQL**: Banco de dados relacional para armazenamento persistente dos dados de álbuns, artistas e regionais.
* **Flyway**: Ferramenta de versionamento de banco de dados para garantir a evolução controlada do schema.
* **MinIO**: Servidor de armazenamento de objetos compatível com a API S3 da AWS, utilizado para persistência de imagens.
* **JUnit 5 & Mockito**: Ferramentas de testes unitários para garantir a qualidade do código e cobertura de lógica de negócio.
* **JaCoCo**: Plugin para geração de relatórios de cobertura de código durante a fase de testes do Maven.
* **Docker & Docker Compose**: Orquestração de containers para facilitar o setup do ambiente de desenvolvimento.

## 👨‍💻 Decisões Técnicas de Destaque

Como parte da estratégia para garantir a robustez e a conformidade do projeto com os requisitos de nível sênior, foram tomadas as seguintes decisões:

* **Padronização de Encoding (UTF-8)**: Configuração rigorosa de encoding em toda a cadeia de processamento (Maven, JVM e cabeçalhos HTTP). Isso foi essencial para suportar caracteres especiais em arquivos de propriedades e respostas do servidor, evitando erros de leitura pelo Maven e de renderização no frontend.
* **Resiliência na Sincronização**: A lógica de sincronização com o serviço de Regionais foi desenhada para ser idempotente. Em caso de falha, a integridade do banco é mantida, e o sistema é capaz de retomar o estado consistente na próxima execução através do versionamento (inativação/inserção).
* **Filtro de Rate Limit Customizado**: Implementação de um filtro de segurança que limita o número de requisições por IP, protegendo a API contra ataques de força bruta e garantindo a disponibilidade para usuários legítimos.
* **Testes com ArgumentCaptor**: Nos testes unitários do serviço S3, utilizamos capturadores de argumentos para validar se as instruções enviadas ao SDK da Amazon (como tempo de expiração de 30 minutos e nome do arquivo) estão corretas, garantindo a segurança operacional do storage.
* **Health Checks Personalizados**: Extensão do Spring Boot Actuator para incluir verificações de prontidão (Readiness) e sobrevivência (Liveness) específicas para o banco de dados e para o servidor de arquivos MinIO, facilitando o monitoramento em ambientes de container.