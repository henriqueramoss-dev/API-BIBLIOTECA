# Documentação — Biblioteca

Sistema de **gestão de acervo e empréstimos** de uma biblioteca, feito como projeto acadêmico (Sistemas para Internet, Senac).

```
 App mobile (Expo)  ──┐
                      ├──►  API REST (Spring Boot)  ──►  Banco H2 (em memória)
 Site (React/Vite)  ──┘      http://localhost:8080
```

| Parte | Onde está | Tecnologias |
|---|---|---|
| **API** | pasta `biblioteca-api` deste repositório | Java 21, Spring Boot 4.1.2, Spring Data JPA, H2, Springdoc OpenAPI, Spring HATEOAS |
| **Site** | pasta `biblioteca-web` deste repositório | React 18, Vite, TypeScript |
| **App** | repositório [REACT-NATIVE](https://github.com/henriqueramoss-dev/REACT-NATIVE) | React Native, Expo SDK 54, Expo Router |

## Por onde começar

| Quero... | Leia |
|---|---|
| Rodar a API, o site e o app | [Guia de instalação](guia-de-instalacao.md) |
| Chamar a API (endpoints, paginação, erros, exemplos) | [Guia da API](guia-da-api.md) |
| Entender as entidades e os campos | [Modelo de dados](modelo-de-dados.md) |
| Entender como o código está organizado | [Arquitetura](arquitetura.md) |
| Saber o que falta para usar de verdade | [Segurança e limitações](seguranca-e-limitacoes.md) |
| Ver as regras de uso e o tratamento de dados | [Termos de Serviço](../biblioteca-api/TERMS_OF_SERVICE.md) e [Política de Privacidade](../biblioteca-api/PRIVACY_POLICY.md) |

## Atalhos (com a API rodando)

| O quê | Endereço |
|---|---|
| Documentação interativa (Swagger) | <http://localhost:8080/swagger-ui.html> |
| Especificação OpenAPI (JSON) | <http://localhost:8080/v3/api-docs> |
| Site | <http://localhost:5173> |
| Coleção do Postman | `biblioteca-api/postman/Biblioteca-API.postman_collection.json` |

> **Atenção:** o sistema é um projeto acadêmico. Use **somente dados fictícios** e não o publique sem ler [Segurança e limitações](seguranca-e-limitacoes.md).
