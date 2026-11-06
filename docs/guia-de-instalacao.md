# Guia de instalação

## O que você precisa

| Para | Ferramenta | Versão |
|---|---|---|
| API | **Java** (JDK) | 21 |
| API | **Maven** | 3.9 ou mais novo |
| Site e app | **Node.js** (já inclui o `npm`) | 20 ou mais novo (testado com 22) |
| App no celular | **Expo Go** | versão compatível com o SDK 54 |

Para conferir o que já está instalado:

```bash
java -version
mvn -v
node -v
```

## 1. API

```bash
cd biblioteca-api
mvn spring-boot:run
```

Quando aparecer `Started BibliotecaApplication`, a API está no ar em **<http://localhost:8080>**. Para conferir:

```bash
curl http://localhost:8080/livros
```

Para gerar e rodar o arquivo `.jar`:

```bash
mvn package
java -jar target/biblioteca-0.0.1-SNAPSHOT.jar
```

> O banco é **H2 em memória**: ele começa vazio e **tudo é apagado quando a API é encerrada**. Para ter dados de exemplo, cadastre pelo Swagger, pelo site ou rode a coleção do Postman.

### Chave de administrador

Cadastrar, alterar, excluir e ver dados pessoais exige uma **chave de API** no cabeçalho `X-API-Key` (veja [Recursos avançados](recursos-avancados.md)). Ao iniciar, a API mostra no log uma chave de administrador:

```text
Chave de API de administrador gerada para esta execução:

    bib_3kF9xQ2mVb7Lp0sT1uWcYd4eHg5iJk6lMn8oPq9rSt0
```

Ela muda a cada início. Para fixar uma chave no seu computador, defina a variável de ambiente `BIBLIOTECA_CHAVE_ADMIN` (mínimo de 16 caracteres) antes de iniciar a API; no IntelliJ, em **Run → Edit Configurations → Environment variables**.

### Swagger

Abra <http://localhost:8080/swagger-ui.html>. Clique em **Authorize**, cole a chave de administrador e confirme. Depois, clique em um endpoint, em **Try it out** e **Execute**. As rotas com cadeado exigem a chave. No topo da página, o seletor **Versão da API** troca entre a documentação da versão 1 e da versão 2.

### Testes automatizados

```bash
cd biblioteca-api
mvn test
```

São 73 testes (68 de integração, em que a API sobe numa porta aleatória e não precisa estar rodando, e 5 de unidade). Demora cerca de 1 minuto.

### Postman

1. Importe `biblioteca-api/postman/Biblioteca-API.postman_collection.json`.
2. Clique na coleção, abra a aba **Variables** e cole a chave de administrador (mostrada no log da API) na variável `apiKey`.
3. Com a API rodando, use **Run collection** sem mudar a ordem das pastas.

A coleção tem 89 requisições, cada uma com seu teste, e pode ser executada várias vezes seguidas: a última pasta ("Exclusões") apaga o que foi criado.

## 2. Site

Com a API rodando:

```bash
cd biblioteca-web
npm install
npm run dev
```

Abra <http://localhost:5173>.

Para usar outro endereço da API, copie `.env.example` para `.env` e troque a linha:

```bash
VITE_API_URL=http://localhost:8080
```

Sem chave de API, o site só consegue consultar o catálogo (livros, autores e categorias). Para cadastrar, alterar e excluir, coloque uma chave no `.env`:

```bash
VITE_API_KEY=bib_cole_aqui_a_chave
```

Tudo que começa com `VITE_` fica visível no navegador: use isso só no seu computador.

Para gerar a versão final do site: `npm run build` (o resultado fica em `dist/`).

## 3. App mobile

O app está no repositório [REACT-NATIVE](https://github.com/henriqueramoss-dev/REACT-NATIVE). Com a API rodando:

> **Chave de API:** o catálogo, o login e o autocadastro continuam abertos, mas as telas que mostram empréstimos e dados de usuários usam rotas que agora exigem a chave `X-API-Key`. Para elas funcionarem, o app precisa, depois do login, gerar uma chave de LEITURA em `POST /chaves-api` (com o mesmo e-mail e senha) e enviá-la nas requisições. Veja [Recursos avançados](recursos-avancados.md#gerar-e-gerenciar-chaves).

```bash
git clone https://github.com/henriqueramoss-dev/REACT-NATIVE.git
cd REACT-NATIVE
npm install
npx expo start
```

Depois, escolha como abrir:

| Onde | Como |
|---|---|
| **Celular (Expo Go)** | Leia o QR code que aparece no terminal. O celular e o computador precisam estar **na mesma rede Wi-Fi** |
| **Navegador** | Aperte `w` no terminal (abre em <http://localhost:8081>) |
| **Emulador** | Aperte `a` (Android) ou `i` (iOS, só no macOS) |

### Como o app encontra a API

Em ordem de prioridade:

1. a variável `EXPO_PUBLIC_API_URL`, se existir um arquivo `.env` na raiz do app;
2. o endereço IP do computador que roda o Expo, na porta 8080 (é o que faz o Expo Go funcionar no celular);
3. `http://localhost:8080`.

Para forçar um endereço, crie o arquivo `.env`:

```bash
EXPO_PUBLIC_API_URL=http://192.168.0.10:8080
```

(troque pelo IP do seu computador) e reinicie o `npx expo start`.

## Problemas comuns

| Sintoma | Causa provável | O que fazer |
|---|---|---|
| `Port 8080 was already in use` | Outra API ou programa usa a porta | Encerre o outro programa ou rode com `mvn spring-boot:run -Dspring-boot.run.arguments=--server.port=8090` (e ajuste `VITE_API_URL` / `EXPO_PUBLIC_API_URL`) |
| `release version 21 not supported` | JDK mais antigo que o 21 | Instale o JDK 21 |
| Site ou app mostram "Não foi possível conectar à API" | API desligada ou endereço errado | Confira se `curl http://localhost:8080/livros` responde |
| O app no celular não acha a API | Rede diferente, firewall do computador ou IP errado | Use a mesma Wi-Fi, libere a porta 8080 no firewall ou defina `EXPO_PUBLIC_API_URL` |
| Navegador bloqueia as chamadas (erro de CORS) | A API só aceita `localhost`, `127.0.0.1` e `192.168.*` | Abra o site por um desses endereços. Redes `10.*` e `172.16.*` precisam ser incluídas em `CorsConfig.java` |
| Os dados sumiram | A API foi reiniciada (banco em memória) | Cadastre de novo ou rode a coleção do Postman |
| Erro 400 com "tamanho da página" | Foi pedido `size` acima de 100 | Use `size` entre 1 e 100 |
| `/h2-console` dá 404 | O console do H2 não vem no Spring Boot 4 | Veja a seção 13 do README principal |
