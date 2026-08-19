# Plataforma de Reserva de Laboratórios e Equipamentos

## Sobre o Projeto

A Plataforma de Reserva de Laboratórios e Equipamentos é uma solução desenvolvida para gerenciar o agendamento de espaços acadêmicos e equipamentos institucionais. O sistema permite que alunos, professores e pesquisadores realizem solicitações de reserva de forma prática, evitando conflitos de horários e melhorando a organização dos recursos disponíveis na instituição.

---

## 🚀 Ambiente de Produção (Render)

| Serviço | URL |
|---|---|
| **Frontend** | https://frontend-reservas.onrender.com |
| **Backend (API)** | https://backend-reservas-8gvn.onrender.com |

> ⚠️ O plano gratuito do Render hiberna após inatividade. A primeira requisição pode demorar até 50 segundos para "acordar" o serviço.

---

## Equipe

| Membro | GitHub |
|---|---|
| Joaci Laurindo | [@joacif](https://github.com/joacif) |
| Euclides Laurindo | [@euclideslaurindo](https://github.com/euclideslaurindo) |
| Luis Arthur | [@lu1s-4rthur](https://github.com/lu1s-4rthur) |
| Heitor Calado | [@heitorcalado](https://github.com/heitorcalado) |
| Arthur Ricardo | [@ArthurRLZ](https://github.com/ArthurRLZ) |

---

## Estrutura do Repositório

```
projeto-engenharia-software/
├── frontend/   # Aplicação web em Angular
└── backend/    # API REST em Java com Spring Boot
```

---

## Funcionalidades Implementadas

### Controle de Acesso

**Frontend (Angular)**
- Cadastro de usuário (nome, e-mail, senha)
- Login com autenticação via JWT
- Logout com limpeza do token
- Interceptor HTTP que anexa o token JWT a todas as requisições autenticadas
- AuthGuard — protege rotas que exigem login
- RoleGuard — protege rotas que exigem uma role específica
- Página de administração acessível apenas para usuários com role ADMIN

**Backend (Spring Boot + Spring Security)**
- Endpoints REST: `POST /api/auth/register`, `POST /api/auth/login`, `POST /api/auth/logout`
- Geração e validação de tokens JWT via `JwtUtil` e `JwtAuthFilter`
- SecurityFilterChain configurado com rotas públicas e protegidas por role
- Roles suportadas: `USER` e `ADMIN`
- Usuário cadastrado pelo sistema recebe automaticamente a role `USER`
- Credencial ADMIN criada automaticamente na inicialização da aplicação
- CORS configurado para integração com o frontend em `http://localhost:4200`

---

## Como Executar o Projeto

### Pré-requisitos

- [Node.js](https://nodejs.org/) (versão LTS) e Angular CLI:
  ```bash
  npm install -g @angular/cli
  ```
- [Java JDK 17+](https://www.oracle.com/java/technologies/downloads/)
- Maven (ou use o `mvnw` incluído no projeto)

### Frontend (Angular)

```bash
cd frontend
npm install
ng serve
```

Acesse em: http://localhost:4200

### Backend (Spring Boot)

```bash
cd backend
.\mvnw spring-boot:run       # Windows
./mvnw spring-boot:run       # Linux/macOS
```

A API estará disponível em: http://localhost:8080

---

## Credencial de Administrador

Na inicialização, o sistema cria automaticamente um usuário administrador padrão:

| Campo | Valor |
|---|---|
| E-mail | `admin@ufape.br` |
| Senha | `admin123` |

---

## Endpoints da API

| Método | Rota | Descrição | Acesso |
|---|---|---|---|
| `POST` | `/api/auth/register` | Cadastra um novo usuário (role USER) | Público |
| `POST` | `/api/auth/login` | Autentica e retorna o token JWT | Público |
| `POST` | `/api/auth/logout` | Encerra a sessão no servidor | Autenticado |
| `GET` | `/api/admin/**` | Rotas exclusivas para administradores | Role ADMIN |

