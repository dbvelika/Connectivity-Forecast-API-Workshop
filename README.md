# Connectivity Forecast API

Reimplementação Java/Spring Boot da [API de referência](https://github.com/LiniiS/connectivity-forecast-api), usada como artefato educacional de APS II.

## Executar

Requisitos: Java 17 e Maven 3.9+.

```bash
mvn test
mvn spring-boot:run
```

Swagger UI: `http://localhost:8080/swagger-ui.html`  
OpenAPI: `http://localhost:8080/v3/api-docs`

## Escopo

API local com catálogo de modelos e previsões mockadas. Fixture igual à referência: 4 modelos ativos, 10 probes e 24 instantes (960 previsões); catálogo também contém modelo inativo. Dados fictícios. Não consulta RIPE Atlas, não treina nem executa modelos, não usa banco de dados e não exige deploy. Classificação e recomendações são regras experimentais, não padrões científicos.

## Estrutura

Separação em controllers, services, repositories, modelos de domínio/DTOs e configuração. API versionada em `/api/v1`, com recursos de health, modelos, localizações, previsões e atividades.

## Documentação

README e OpenAPI são pontos de partida. Completar em exercício: descrição dos endpoints, parâmetros e validações, exemplos de requisição/resposta, códigos de erro e origem dos campos. Referência à licença MIT da API de origem preservada neste projeto.

## Limitações conhecidas
- O catálogo e as previsões são carregados de fixtures estáticas (`data/models.json` e
 `data/predictions.csv`); não são dados de rede ao vivo.
- A API não consulta RIPE Atlas nem outra fonte externa em tempo real.
- O código não treina nem executa modelos de machine learning: os resultados retornados são
 baseados nos dados de exemplo fornecidos.
- A classificação de qualidade e as recomendações são regras experimentais, não padrões
 científicos nem garantias de desempenho real da conexão.
- Não há banco de dados. O projeto é um artefato educacional local e não deve ser
 interpretado como serviço de monitoramento de conectividade pronto para produção.
A fixture contém 4 modelos ativos, 10 probes e 24 instantes (960 previsões); o catálogo
 também contém um modelo inativo. Todos os dados são fictícios.
