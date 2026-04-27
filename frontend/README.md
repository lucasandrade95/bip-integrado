# Frontend Angular

Aplicação Angular 17 (standalone components) que consome o backend em `http://localhost:8080`.

## Pré-requisitos

- **Node.js v21.7.3** (versão usada no desenvolvimento). Compatível também com Node 18.13+ ou 20.9+ (LTS recomendadas pelo Angular 17).
- **npm** (vem com o Node).
- **Angular CLI 17.3.17** (`npm install -g @angular/cli@17`).

## Comandos

```bash
npm install        # instalar dependências
npm start          # ng serve em http://localhost:4200
npm run build      # build de produção em dist/
npm test           # testes unitários (Karma + Jasmine)
```

## Estrutura

Organizada em **package-by-feature** com **smart vs presentational components** e isolamento das responsabilidades transversais (notificação, confirmação) em serviços/componentes globais reutilizáveis.

```
src/app/
├── app.component.ts/.html/.css                  shell + outlets globais (toast + modal)
├── app.config.ts                                provideHttpClient + provideRouter + ngx-mask
├── app.routes.ts                                lazy load da feature beneficio
├── core/                                         singletons (services + UI globais)
│   ├── notification/
│   │   ├── notification.model.ts                Notification interface
│   │   ├── notification.service.ts              signal + notify('success'|'error', text)
│   │   └── toast.component.ts/.html/.css        renderiza o toast a partir do service
│   └── confirmation/
│       ├── confirmation.model.ts                PendingAction interface
│       ├── confirmation.service.ts              signal + ask/confirm/cancel
│       └── confirmation-modal.component.ts/.html/.css
├── shared/                                       código reusável entre features
│   └── pipes/
│       ├── brl.pipe.ts                          pipe BrlPipe para templates
│       └── format-brl.ts                        função formatBrl(n) para TS
└── features/
    └── beneficio/
        ├── beneficio.routes.ts                   roteamento da feature
        ├── models/
        │   ├── beneficio.model.ts
        │   └── transfer-request.model.ts
        ├── services/
        │   └── beneficio.service.ts             HttpClient para /api/v1/beneficios
        └── components/
            ├── beneficio-list-page/             SMART container (orquestra estado + HTTP)
            ├── beneficio-form/                   PRESENTATIONAL (formulário CRUD)
            ├── beneficio-table/                  PRESENTATIONAL (lista + ações)
            └── transfer-form/                    PRESENTATIONAL (transferência id/nome)
```

### Princípios da estrutura

- **`core/`** — services singleton + componentes globais (toast, modal). Qualquer componente pode injetar `NotificationService` e `ConfirmationService` sem importar UI específica.
- **`shared/`** — utilitários puros (pipes, helpers de formato). Sem dependência de feature ou estado.
- **`features/<nome>/`** — uma pasta por bounded context. Cresce horizontalmente (adicionar `transferencia/`, `auditoria/` é só criar outro pacote-irmão).
- **Smart vs presentational components**:
  - `BeneficioListPageComponent` (smart) — orquestra estado, HTTP, diálogos. Não tem markup próprio além de compor os filhos.
  - `BeneficioFormComponent`, `BeneficioTableComponent`, `TransferFormComponent` (presentational) — recebem `@Input`, emitem `@Output`. Sem injeção de service. Reutilizáveis e fáceis de testar isoladamente.
- **Lazy loading** via `loadChildren` em `app.routes.ts` — a feature `beneficio` é um chunk separado (~30 kB) carregado sob demanda; melhora o tempo do bundle inicial.
- **Outlets globais** no `AppComponent` — `<app-toast>` e `<app-confirmation-modal>` ficam no shell e funcionam para qualquer rota.
- **Signals** (`signal`, `computed`, `asReadonly`) em vez de `BehaviorSubject` para o estado local.

## Stack

- Angular 17 standalone components
- `ngx-mask` 17.1.8 (máscara monetária `R$ 1.234,56`)
- HttpClient + Forms (template-driven com `ngModel`)
- Signals para estado reativo
- Lazy routing (`loadChildren`)

## CORS

O backend libera `http://localhost:4200` em `BackendApplication`/`CorsConfig` (configurável via `app.cors.allowed-origin` no `application.yml`).
