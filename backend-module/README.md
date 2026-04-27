# Backend — Spring Boot 3.2.5 / Java 17

API REST. Expõe CRUD de benefícios e endpoint de transferência de valor entre eles. Esta é a única raiz Maven do projeto: o `pom.xml` aqui também compila os fontes Jakarta EE em `../ejb-module/src/main/java` via `build-helper-maven-plugin`.

## Pré-requisitos

- JDK 17+ (testado com OpenJDK 23/25)
- Maven 3.9+

## Como rodar

```bash
cd backend-module
mvn spring-boot:run
```

Sobe em `http://localhost:8080`.

| Recurso         | URL                                                 |
|-----------------|------------------------------------------------------|
| API base        | `http://localhost:8080/api/v1/beneficios`            |
| Swagger UI      | `http://localhost:8080/swagger-ui.html`              |
| OpenAPI JSON    | `http://localhost:8080/v3/api-docs`                  |
| H2 console      | `http://localhost:8080/h2-console`                   |
| H2 JDBC URL     | `jdbc:h2:mem:beneficios` (user `sa`, senha vazia)    |

Schema e seed são executados automaticamente na subida (`spring.sql.init` lendo `src/main/resources/db/schema.sql` + `seed.sql` — versões idempotentes das cópias canônicas em `../db/`).

## Build + testes

```bash
mvn clean verify                                       # 16 testes
mvn -Dtest=BeneficioServiceTest test                   # uma classe
mvn -Dtest=BeneficioControllerIT#transferUpdatesBalances test
mvn -Dtest=TransferConcurrencyIT test                  # concorrência real
```

| Suite                    | Tipo                                  | Qtd |
|--------------------------|---------------------------------------|-----|
| `BeneficioServiceTest`   | Unit (Mockito + stub manual)          | 7   |
| `BeneficioControllerIT`  | Integração (`@SpringBootTest` + MockMvc) | 7   |
| `TransferConcurrencyIT`  | Concorrência (200/100 transferências paralelas) | 2   |

`maven-surefire-plugin` foi configurado para também aceitar `*IT.java` além de `*Test.java`.

## Empacotamento

```bash
mvn clean package
java -jar target/backend-module-0.0.1-SNAPSHOT.jar
```

O `spring-boot-maven-plugin` repackages o jar como executável (fat-jar com Tomcat embarcado).

## Endpoints

| Método | Path                                  | HTTP sucesso | Descrição                                              |
|--------|---------------------------------------|--------------|---------------------------------------------------------|
| GET    | `/api/v1/beneficios`                  | 200          | Lista todos                                             |
| GET    | `/api/v1/beneficios/{id}`             | 200          | Busca por id                                            |
| POST   | `/api/v1/beneficios`                  | 201          | Cria                                                    |
| PUT    | `/api/v1/beneficios/{id}`             | 200          | Atualiza                                                |
| DELETE | `/api/v1/beneficios/{id}`             | 204          | Remove                                                  |
| POST   | `/api/v1/beneficios/transfer`         | 204          | Transfere `amount` (cada lado por id OU nome)           |

### Body do `POST /transfer`

```json
{
  "fromId":   1,
  "fromName": null,
  "toId":     null,
  "toName":   "Beneficio B",
  "amount":   100.00
}
```

Para cada lado preencha **`id` OU `name`** (se preencher os dois, o nome é ignorado).

### Mapeamento de erros (`RestExceptionHandler`)

| Exceção                                   | HTTP |
|-------------------------------------------|------|
| `BeneficioNotFoundException`              | 404  |
| `TransferException`                       | 422  |
| `MethodArgumentNotValidException`         | 400  |
| `OptimisticLock*` / `PessimisticLock*`    | 409  |

## Estrutura

Organizada em **package-by-feature** com camadas explícitas (`api → service → repository`) e separação clara entre código de feature, configuração e código compartilhado:

```
backend-module/
├── pom.xml                                            ÚNICO pom.xml do projeto
└── src/
    ├── main/java/com/example/backend/
    │   ├── BackendApplication.java                    entry point + @EntityScan
    │   ├── config/                                    configuração transversal
    │   │   ├── CorsConfig.java                        CORS configurável via app.cors.allowed-origin
    │   │   └── EjbConfig.java                         registra BeneficioEjbService como bean Spring
    │   ├── shared/exception/
    │   │   └── RestExceptionHandler.java              mapeia exceções para HTTP (404/422/400/409)
    │   └── beneficio/                                 feature module
    │       ├── BeneficioController.java               api REST /api/v1/beneficios
    │       ├── BeneficioService.java                  @Transactional; delega transfer ao EJB
    │       ├── BeneficioRepository.java               JpaRepository + findByNome
    │       ├── dto/
    │       │   ├── BeneficioRequest.java              input (POST/PUT) — sem id/version
    │       │   ├── BeneficioResponse.java             output (GET) — com id/version
    │       │   └── TransferRequest.java
    │       └── exception/
    │           └── BeneficioNotFoundException.java
    ├── main/resources/
    │   ├── application.yml                            H2 + sql.init + springdoc + app.cors
    │   └── db/
    │       ├── schema.sql                             idempotente (CREATE TABLE IF NOT EXISTS)
    │       └── seed.sql                               idempotente (INSERT WHERE NOT EXISTS)
    └── test/java/com/example/backend/beneficio/
        ├── BeneficioServiceTest.java                  unit (Mockito + stub do EJB)
        ├── BeneficioControllerIT.java                 integração (@SpringBootTest + MockMvc)
        └── TransferConcurrencyIT.java                 concorrência real (200/100 transfers paralelas)
```

### Princípios da estrutura

- **`config/`** — beans Spring de configuração (CORS, registro do EJB). Externalizados via `@Value` para permitir override por ambiente.
- **`shared/`** — código transversal usado por mais de uma feature (exception handler global). Cresce horizontalmente quando surgem mais features.
- **`beneficio/`** — feature módulo (alta coesão). Cada feature concentra controller + service + repository + dto + exception próprias. Para adicionar uma nova feature (ex.: `transferencia/` como entidade própria, `auditoria/`), cria-se outro pacote-irmão sem mexer nesse.
- **DTOs separados por direção** (`Request` para entrada, `Response` para saída) — evita campos opcionais ambíguos e blinda a API contra mudanças internas da entidade.

## Integração com o EJB

O `pom.xml` adiciona `../ejb-module/src/main/java` como diretório de fontes via `build-helper-maven-plugin`. As classes `Beneficio`, `BeneficioEjbService` e `TransferException` são compiladas junto com o backend e empacotadas no fat-jar.

O `BeneficioEjbService` (anotado com `@Stateless`) é registrado como bean Spring em `EjbConfig`. O `EntityManager` é injetado via `@PersistenceContext` por `PersistenceAnnotationBeanPostProcessor`. Não há container EE em runtime — a implementação corrigida do EJB é o que executa, e a anotação `@Stateless` documenta a intenção arquitetural.

`BeneficioService.transfer` delega 100% para o EJB depois de resolver `fromName`/`toName` em ids. Isso garante **fonte única da verdade** para a regra de negócio.

## Stack de dependências principais

- `spring-boot-starter-web` — REST + Tomcat embarcado
- `spring-boot-starter-data-jpa` + `h2` — persistência
- `spring-boot-starter-validation` — Bean Validation (`@Valid`, `@NotNull`, `@DecimalMin`)
- `springdoc-openapi-starter-webmvc-ui` 2.5.0 — Swagger UI
- `jakarta.ejb-api` 4.0.1 — anotações EE compilam
- `build-helper-maven-plugin` 3.6.0 — adiciona fontes do EJB
- `spring-boot-starter-test` — JUnit 5, Mockito, MockMvc, AssertJ
