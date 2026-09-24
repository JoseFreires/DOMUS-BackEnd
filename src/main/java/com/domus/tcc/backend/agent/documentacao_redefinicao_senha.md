# Documentação Técnica: Redefinição de Senha

## 1. Visão geral da solução

A funcionalidade de redefinição de senha foi ajustada para um fluxo mais simples e seguro, sem geração de token temporário e sem validação de token em endpoint dedicado.

O comportamento atual fica assim:

- o sistema somente envia e-mail de redefinição se o cadastro do usuário já existir no banco;
- a solicitação de redefinição aceita apenas o e-mail do usuário;
- o backend verifica se esse e-mail corresponde a um usuário cadastrado;
- se existir, dispara o evento para envio de e-mail com instruções;
- se não existir, responde de forma genérica, sem revelar a ausência do cadastro;
- a troca de senha é feita por um endpoint público sem validação de token;
- após a alteração, o usuário não é redirecionado automaticamente para a aplicação;
- o usuário precisa efetuar login normalmente usando a nova senha.

A solução continua respeitando as boas práticas de segurança:

- senha armazenada em hash com `BCryptPasswordEncoder`;
- resposta genérica ao cliente para evitar enumeração de usuários;
- validação da nova senha antes do update;
- boas práticas de rate limiting e auditoria;
- transação no service para garantir consistência.

---

## 2. Fluxo completo dos casos de uso

### Caso de uso 1: Primeiro acesso de um novo usuário

1. O síndico cadastra um novo usuário.
2. O sistema gera uma senha temporária aleatória, apenas para cumprir o campo obrigatório da senha.
3. O usuário é salvo com a senha temporária em hash.
4. O backend verifica se o usuário já existe e, se houver cadastro prévio, dispara o evento de e-mail de redefinição.
5. O e-mail enviado informa que o usuário deve redefinir a senha e orienta a ação.
6. O usuário acessa a tela de redefinição, informa a nova senha e confirma.
7. A API atualiza a senha do usuário no banco.
8. O usuário sai da tela de redefinição e precisa realizar login manualmente na aplicação com a nova senha.

Observação importante:

- se o cadastro ainda não existir no banco, o sistema não envia e-mail de redefinição;
- a criação da conta e a troca de senha seguem o mesmo padrão de segurança, sem token temporário.

### Caso de uso 2: Usuário esqueceu a senha

1. O usuário acessa a tela de login e seleciona “Esqueci a senha”.
2. O sistema recebe o e-mail informado.
3. Se o e-mail estiver cadastrado, o backend envia o e-mail com instruções para redefinição.
4. Se o e-mail não estiver cadastrado, responde com mensagem genérica, sem confirmar ou negar a existência do cadastro.
5. O usuário acessa a tela de redefinição de senha.
6. O usuário informa a nova senha e confirma.
7. O backend aplica o hash e salva a nova senha.
8. O usuário deve fazer login manualmente com a nova senha.

---

## 3. Arquitetura sugerida

A estrutura continua seguindo o padrão do projeto: controller, service, repository, DTOs, eventos e listener para envio de e-mail.

### Camadas novas

- `controller/RedefinicaoSenhaController`
- `services/RedefinicaoSenhaService`
- `repository/UsuarioRepository` (uso de busca por e-mail/username)
- `dto/request/DadosSolicitacaoRedefinicaoSenhaDTO`
- `dto/request/DadosNovaSenhaDTO`
- `util/RedefinicaoSenhaSolicitadaEvent`
- `util/RedefinicaoSenhaEventListener`
- `templates/email-recuperacao-senha.html`

### Arquivos existentes que provavelmente serão alterados

- `repository/UsuarioRepository` — busca de usuário por e-mail ou username;
- `services/EmailService` e `services/EmailServiceImpl` — envio do e-mail;
- `security/SecurityConfig` — abertura de endpoints públicos;
- `controller/AutenticacaoController` — possibilidade de incluir o endpoint de redefinição, se preferirem centralizar;
- `templates/` — inclusão do template HTML do e-mail.


---

## 4. Responsabilidade das classes

### `RedefinicaoSenhaController`

Responsável por expor endpoints públicos para:

- solicitar redefinição de senha;
- confirmar troca da senha;
- responder com status neutro para evitar vazamento de informação.

Exemplo de estrutura:

```java
@RestController
@RequestMapping("/auth")
public class RedefinicaoSenhaController {

    @PostMapping("/esqueci-minha-senha")
    public ResponseEntity<Map<String, String>> solicitarRedefinicao(@RequestBody @Valid DadosSolicitacaoRedefinicaoSenhaDTO dados) {
        return ResponseEntity.accepted().body(Map.of(
            "message", "Se o e-mail estiver cadastrado, você receberá as instruções para redefinir sua senha."
        ));
    }

    @PutMapping("/redefinir-senha")
    public ResponseEntity<Void> redefinirSenha(@RequestBody @Valid DadosNovaSenhaDTO dados) {
        return ResponseEntity.noContent().build();
    }
}
```

### `RedefinicaoSenhaService`

Centraliza a regra de negócio:

- busca usuário por e-mail ou username;
- valida existência do cadastro;
- dispara evento de e-mail apenas quando o usuário existe;
- valida nova senha;
- aplica hash com `PasswordEncoder`;
- persiste a senha atualizada.

### `DadosSolicitacaoRedefinicaoSenhaDTO`

```java
public record DadosSolicitacaoRedefinicaoSenhaDTO(
        @NotBlank @Email String email
) {}
```

### `DadosNovaSenhaDTO`

Como não há token, o email precisa ser informado novamente no payload para identificar o usuário:

```java
public record DadosNovaSenhaDTO(
        @NotBlank @Email String email,
        @NotBlank String novaSenha,
        @NotBlank String confirmarSenha
) {}
```

### `RedefinicaoSenhaSolicitadaEvent`

Transporta os dados necessários para o envio do e-mail.

```java
public record RedefinicaoSenhaSolicitadaEvent(
        String emailDestino,
        String nomeUsuario,
        String nomeSistema,
        String linkRedefinicao
) {}
```

### `RedefinicaoSenhaEventListener`

Recebe o evento e envia o e-mail com instruções de redefinição.

```java
@Component
public class RedefinicaoSenhaEventListener {

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onRedefinicaoSolicitada(RedefinicaoSenhaSolicitadaEvent evento) {
        Context ctx = new Context();
        ctx.setVariable("nomeUsuario", evento.nomeUsuario());
        ctx.setVariable("nomeSistema", evento.nomeSistema());
        ctx.setVariable("linkRedefinicao", evento.linkRedefinicao());

        String html = templateEngine.process("email-recuperacao-senha", ctx);
        emailService.enviarEmail(evento.emailDestino(), "Redefina sua senha", html);
    }
}
```

---

## 5. Estratégia para atualização e criptografia da senha

### Criptografia

- utilizar `BCryptPasswordEncoder`;
- never store plain text password;
- persistir somente o hash no banco;
- manter a coluna `senha` em hash.

```java
@Bean
public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
}
```

### Atualização da senha

No service:

```java
String senhaCriptografada = passwordEncoder.encode(dados.novaSenha());
usuario.setSenha(senhaCriptografada);
usuarioRepository.save(usuario);
```

### Validação da nova senha

Regras recomendadas:

- mínimo de 8 caracteres;
- no mínimo 1 letra maiúscula;
- no mínimo 1 letra minúscula;
- no mínimo 1 número;
- senha não pode ser igual à temporária anterior;
- confirmar senha deve bater com a nova senha;
- rejeitar senha muito comum ou conhecida.

Exemplo:

```java
if (!dados.novaSenha().equals(dados.confirmarSenha())) {
    throw new SenhaInvalidaException("As senhas informadas não conferem.");
}

if (dados.novaSenha().length() < 8) {
    throw new SenhaInvalidaException("A senha deve ter pelo menos 8 caracteres.");
}
```

---

## 6. Fluxo de envio do e-mail

O padrão continua seguindo a organização do aviso condominial, mas sem token temporário.

### Processo

1. `RedefinicaoSenhaService.solicitarRedefinicao(email)` valida o usuário;
2. busca no banco o cadastro pelo e-mail;
3. se o usuário existir, publica `RedefinicaoSenhaSolicitadaEvent`;
4. listener executa após o commit;
5. template HTML é processado;
6. `EmailService` envia o e-mail;
7. a API responde com mensagem genérica;
8. se o usuário não existir, a API não dispara evento e responde com mensagem neutra.

### Exemplo de publicação de evento

```java
applicationEventPublisher.publishEvent(new RedefinicaoSenhaSolicitadaEvent(
    usuario.getPessoa().getEmail(),
    usuario.getPessoa().getNomeCompleto(),
    "https://front.domus.com/redefinir-senha",
    "Domus"
));
```

### Resposta neutra da solicitação

```json
{
  "message": "Se o e-mail estiver cadastrado, você receberá as instruções para redefinir sua senha."
}
```

---

## 7. E-mail de redefinição

### Regras

- o e-mail só é enviado se o cadastro existir no banco;
- o link pode ser simples e abrir a tela de redefinição no frontend;
- não deve existir token de validação no backend;
- o usuário deve realizar login manualmente após definir a nova senha.

### Exemplo de template HTML

Arquivo sugerido: `src/main/resources/templates/email-recuperacao-senha.html`

```html
<!DOCTYPE html>
<html lang="pt-BR">
<head>
    <meta charset="UTF-8" />
    <title>Redefinição de senha</title>
</head>
<body style="font-family: Arial, sans-serif; background-color: #f5f5f5; padding: 24px;">
    <div style="max-width: 600px; margin: 0 auto; background: white; border-radius: 8px; padding: 24px;">
        <h2 style="color: #1f2937;">Redefinição de senha</h2>
        <p>Olá, <strong th:text="${nomeUsuario}">Usuário</strong>,</p>
        <p>Recebemos sua solicitação para redefinir a senha da sua conta.</p>
        <p>Para continuar, acesse a tela de redefinição de senha no sistema:</p>

        <p style="text-align: center; margin: 32px 0;">
            <a th:href="${linkRedefinicao}"
               style="display: inline-block; background-color: #2563eb; color: white; text-decoration: none; padding: 12px 24px; border-radius: 6px;">
                Redefinir senha
            </a>
        </p>

        <p>Se você não solicitou essa alteração, ignore este e-mail.</p>
        <p>Atenciosamente,<br />Equipe Domus</p>
    </div>
</body>
</html>
```

---

## 8. Endpoints necessários

### 8.1 Solicitação de redefinição

Endpoint sugerido:

```http
POST /auth/esqueci-minha-senha
Content-Type: application/json
```

Corpo:

```json
{
  "email": "usuario@domus.com"
}
```

Resposta recomendada:

```http
HTTP/1.1 202 Accepted
Content-Type: application/json
```

```json
{
  "message": "Se o e-mail estiver cadastrado, você receberá as instruções para redefinir sua senha."
}
```

### 8.2 Efetivação da troca de senha

Endpoint sugerido:

```http
PUT /auth/redefinir-senha
Content-Type: application/json
```

Corpo:

```json
{
  "email": "usuario@domus.com",
  "novaSenha": "SenhaForte@123",
  "confirmarSenha": "SenhaForte@123"
}
```

Resposta:

```http
HTTP/1.1 204 No Content
```

Em caso de erro de validação:

```http
HTTP/1.1 400 Bad Request
```

```json
{
  "message": "A senha deve conter pelo menos 8 caracteres, incluindo letras maiúsculas, minúsculas e números."
}
```

Observação:

- não existe validação de token nesta rota;
- o usuário deve retornar à tela de login e autenticar com a nova senha;
- não há redirecionamento obrigatório para a aplicação após a troca.

---

## 9. Tratamento de exceções e códigos HTTP

### Exceções esperadas

- `UsuarioNaoEncontradoException`
- `SenhaInvalidaException`
- `EmailInvalidoException` (opcional)
- `SenhaIgualAnteriorException` (opcional)

### Códigos sugeridos

- `202 Accepted` — solicitação de redefinição aceita;
- `204 No Content` — redefinição concluída com sucesso;
- `400 Bad Request` — payload inválido ou regras de senha violadas;
- `429 Too Many Requests` — abuso do endpoint;
- `500 Internal Server Error` — falha inesperada.

---

## 10. Proteções contra enumeração e abuso

### Proteção contra enumeração

- não informar se o e-mail existe ou não;
- responder sempre com mensagem neutra;
- registrar auditoria interna sem expor dados ao cliente.

### Proteção contra abuso

- limitar requisições por IP ou usuário;
- aplicar rate limiting (ex.: 3 tentativas por 15 minutos);
- não expor detalhes do fluxo em logs públicos;
- manter a validação da senha e do hash em um único ponto da aplicação.

---

## 11. Sugestões de nomes de classes e objetos

### Controller

- `RedefinicaoSenhaController`
- `AutenticacaoController` (pode concentrar os endpoints de autenticação)

### Service

- `RedefinicaoSenhaService`

### Eventos

- `RedefinicaoSenhaSolicitadaEvent`

### Listeners

- `RedefinicaoSenhaEventListener`

### DTOs

- `DadosSolicitacaoRedefinicaoSenhaDTO`
- `DadosNovaSenhaDTO`

### Templates

- `email-recuperacao-senha.html`

---

## 12. Etapas de implementação em ordem recomendada

1. Ajustar `UsuarioRepository` para buscar usuário por e-mail/username.
2. Criar `DadosSolicitacaoRedefinicaoSenhaDTO`.
3. Criar `DadosNovaSenhaDTO` com email + novaSenha + confirmarSenha.
4. Implementar `RedefinicaoSenhaService` com validação de existência e regra de senha.
5. Criar `RedefinicaoSenhaController` com endpoints públicos.
6. Ajustar `SecurityConfig` para permitir acesso público aos endpoints.
7. Implementar evento e listener para envio de e-mail.
8. Criar o template HTML do e-mail.
9. Aplicar `PasswordEncoder` antes de salvar.
10. Implementar tratativa global de exceções.
11. Adicionar rate limiting e proteção contra abuso.
12. Validar fluxo completo com e-mail mockado ou real.

---

## 13. Observações finais

Essa abordagem foi ajustada para remover a geração de token provisório e a validação por token, mantendo a segurança com a autenticação tradicional do sistema.

O ponto central é que:

- o e-mail só é enviado quando o usuário realmente existe no banco;
- a troca de senha não depende de token temporário;
- o usuário precisa entrar na aplicação após a alteração e logar com a nova senha;
- a segurança continua preservada com criptografia, validação e resposta genérica.
