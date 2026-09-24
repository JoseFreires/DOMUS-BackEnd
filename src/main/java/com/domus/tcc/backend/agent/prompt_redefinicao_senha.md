# Prompt: Documentação Técnica para Redefinição de Senha

## Contexto

Precisamos implementar a funcionalidade de redefinição de senha com o seguinte ajuste de arquitetura e segurança:

- não haverá geração de token temporário;
- não haverá persistência de token de redefinição no banco;
- não haverá validação de token em endpoint dedicado;
- o e-mail só será enviado se o cadastro do usuário já existir no banco;
- a solicitação de redefinição deve responder de forma genérica para evitar enumeração de usuários;
- após a redefinição, o usuário não prosseguirá automaticamente para a aplicação;
- o usuário precisará realizar login normalmente usando a nova senha.

O backend continua em Java com Spring Boot, Spring Data JPA e organização em controller, service, repository, DTOs, eventos e listeners.

## Casos de uso

### Cenário 1: Primeiro acesso de um novo usuário

Quando o síndico cadastrar um novo usuário:

1. O sistema gera uma senha aleatória temporária apenas para atender ao campo obrigatório da senha.
2. O usuário é salvo no banco com a senha temporária em hash.
3. O backend verifica se o cadastro do usuário existe e, se existir, dispara o e-mail de redefinição.
4. Se o cadastro não existir, não envia e-mail e não revela a ausência do cadastro.
5. O usuário recebe instruções para acessar a tela de redefinição de senha.
6. O usuário informa a nova senha e confirma.
7. O backend atualiza a senha em hash no banco.
8. O usuário retorna à tela de login e autentica com a nova senha.

### Cenário 2: Usuário esqueceu a senha

Quando o usuário clicar em “Esqueci a senha”:

1. O sistema recebe o e-mail informado.
2. Se o e-mail estiver cadastrado, o backend envia o e-mail com instruções.
3. Se o e-mail não estiver cadastrado, responde com mensagem genérica.
4. O usuário acessa a tela de redefinição e informa a nova senha.
5. O backend valida a senha e atualiza o hash no banco.
6. O usuário não entra automaticamente no sistema; precisa fazer login normalmente.

## Regras que devem ser refletidas no documento

- O fluxo não usa token de redefinição.
- Não existe endpoint de validação de token.
- O e-mail deve ser enviado somente se o usuário existir no banco.
- Não será envolvido modelo de entidade para token.
- O payload de redefinição deve incluir o e-mail do usuário para identificar qual conta será alterada.
- A resposta de solicitação de redefinição deve ser neutra.
- A nova senha deve ser criptografada com `BCryptPasswordEncoder` antes de persistir.
- A troca de senha não deve redirecionar o usuário automaticamente para a aplicação.

## Nova estrutura esperada

- Controller:
  - `RedefinicaoSenhaController`
- Service:
  - `RedefinicaoSenhaService`
- DTOs:
  - `DadosSolicitacaoRedefinicaoSenhaDTO`
  - `DadosNovaSenhaDTO`
- Evento:
  - `RedefinicaoSenhaSolicitadaEvent`
- Listener:
  - `RedefinicaoSenhaEventListener`
- Template:
  - `email-recuperacao-senha.html`

## Objetivo da resposta

Gerar um documento técnico em Markdown explicando detalhadamente a funcionalidade de redefinição de senha com o fluxo ajustado:

1. Visão geral da solução.
2. Fluxo completo dos casos de uso.
3. Arquitetura sugerida.
4. Responsabilidade das classes.
5. DTOs e payloads necessários.
6. Endpoints para solicitação e efetivação da troca de senha.
7. Estratégia de validação e criptografia da senha.
8. Fluxo de envio de e-mail com mensagem genérica.
9. Exemplo de template HTML do e-mail.
10. Tratamento de exceções e códigos HTTP.
11. Requisitos de segurança e proteção contra enumeração.
12. Lista de etapas de implementação em ordem.

## Orientações técnicas

- Não reutilizar qualquer conceito de token temporário.
- Não incluir validação de token no backend.
- Não enviar e-mail quando o cadastro não existir no banco.
- Não automatizar tela de login após a redefinição.
- Usar resposta neutra para a solicitação de redefinição.
- Persistir a senha sempre em hash.
- Organizar o documento de forma clara e profissional em Markdown.
