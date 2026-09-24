# Documento Técnico - Ajuste do Fluxo de Redefinição de Senha

## Objetivo

Eliminar a necessidade de o usuário informar novamente seu e-mail durante o processo de redefinição de senha, utilizando um token enviado por e-mail para identificar o usuário de forma segura.

---

## Cenário Atual

### Fluxo atual

```text
Esqueci minha senha
        ↓
Informa e-mail
        ↓
Recebe e-mail com link
        ↓
Acessa o link
        ↓
Informa e-mail novamente
        ↓
Informa nova senha
        ↓
Senha atualizada
```

### Problemas identificados

- Solicitação redundante do e-mail.
- Experiência do usuário inferior ao padrão de mercado.
- Possibilidade de erro ao digitar o e-mail na etapa final.
- Dependência de uma informação que já foi validada no início do processo.

---

## Proposta de Solução

Utilizar um token de recuperação de senha enviado ao usuário por e-mail.

O token será responsável por identificar o usuário que realizou a solicitação de redefinição.

### Exemplo de link enviado por e-mail

```text
https://sistema.com.br/redefinir-senha?token=abc123xyz
```

Ao acessar o link, o usuário deverá informar apenas:

```text
Nova senha
Confirmar senha
```

---

## Fluxo Proposto

```text
Esqueci minha senha
        ↓
Informa e-mail
        ↓
Sistema gera token
        ↓
Sistema envia e-mail
        ↓
Usuário acessa o link
        ↓
Frontend captura token
        ↓
Usuário informa nova senha
        ↓
Backend valida token
        ↓
Senha atualizada
```

---

## Alterações no Backend

### 1. Solicitação de redefinição

Responsável por iniciar o processo de recuperação.

#### Fluxo

1. Receber e-mail informado pelo usuário.
2. Localizar usuário associado ao e-mail.
3. Gerar token único de redefinição.
4. Associar token ao usuário.
5. Definir data de expiração.
6. Enviar e-mail contendo o link de recuperação.

---

### 2. Persistência do token

#### Opção Recomendada

Criar uma entidade específica para recuperação de senha.

### Entidade PasswordResetToken

```java
@Entity
public class PasswordResetToken {

    @Id
    private Long id;

    private String token;

    private LocalDateTime dataExpiracao;

    private Boolean utilizado;

    @ManyToOne
    private Usuario usuario;
}
```

### Campos sugeridos

| Campo | Descrição |
|---------|---------|
| id | Identificador do registro |
| token | Token único de recuperação |
| usuario | Usuário associado |
| dataExpiracao | Data limite de utilização |
| utilizado | Indica se o token já foi utilizado |

---

### Alternativa Simplificada

Adicionar colunas na tabela Usuario.

```java
private String resetToken;
private LocalDateTime resetTokenExpiracao;
```

Observação:

Esta abordagem reduz o número de tabelas, porém possui menor flexibilidade para futuras evoluções.

---

### 3. Endpoint de redefinição

#### Request

```json
{
  "token": "abc123xyz",
  "novaSenha": "NovaSenha123"
}
```

#### Validações obrigatórias

- Token existe.
- Token pertence a um usuário válido.
- Token não expirou.
- Token não foi utilizado.
- Nova senha atende às regras de segurança.

#### Processamento

1. Localizar token.
2. Validar token.
3. Identificar usuário associado.
4. Atualizar senha.
5. Invalidar token.
6. Retornar sucesso.

---

## Alterações no Frontend

### Tela de recuperação

Ao acessar:

```text
/redefinir-senha?token=abc123xyz
```

O frontend deverá:

1. Capturar o token presente na URL.
2. Armazená-lo temporariamente.
3. Exibir apenas os campos necessários para alteração da senha.

### Campos exibidos

```text
Nova senha
Confirmar senha
```

### Campos removidos

```text
E-mail
```

---

### Envio da nova senha

Payload esperado:

```json
{
  "token": "abc123xyz",
  "novaSenha": "NovaSenha123"
}
```

---

## Regras de Segurança

### Expiração do token

Sugestão:

```text
15 a 30 minutos
```

### Uso único

Após redefinição bem-sucedida:

```text
Token deve ser invalidado.
```

### Armazenamento da senha

A senha deve continuar sendo armazenada de forma criptografada utilizando o mecanismo já adotado pela aplicação.

Exemplo:

```java
BCryptPasswordEncoder
```

### Tratamento de erros

Retornar mensagens apropriadas para:

- Token inválido.
- Token expirado.
- Token já utilizado.
- Usuário não encontrado.

---

## Benefícios Esperados

- Melhor experiência do usuário.
- Remoção de informação redundante.
- Processo alinhado com padrões de mercado.
- Menor chance de erro operacional.
- Maior segurança através do uso de tokens temporários.
- Facilidade de evolução futura do mecanismo de recuperação de senha.

---

## Resultado Final Esperado

```text
Esqueci minha senha
        ↓
Informa e-mail
        ↓
Recebe e-mail com link
        ↓
Acessa o link
        ↓
Informa apenas a nova senha
        ↓
Sistema valida token
        ↓
Senha atualizada
```

Este fluxo elimina a necessidade de solicitar novamente o e-mail ao usuário, tornando o processo mais simples, seguro e aderente às boas práticas de desenvolvimento de software.
