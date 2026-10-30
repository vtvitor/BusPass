# Segurança e limitações

Este repositório é um **protótipo educacional** e não deve receber dados reais de usuários, pagamentos ou operar catracas de produção sem revisão.

## Achados importantes (código atual)

- O serviço de login compara senhas diretamente; **usar hash com BCrypt/Argon2id**, nunca texto puro.
- Os endpoints de cartão e débito precisam de **autenticação e autorização**; um `usuarioId` enviado pelo cliente não prova propriedade.
- A API aceita débito pelo número/UID de cartão: **não usar UID isolado como prova de identidade**.
- O firmware ESP32 usa `client.setInsecure()` e **não valida o certificado TLS**. Configurar raiz de confiança e relógio correto antes de qualquer uso sensível.
- O saldo é atualizado em leitura/modificação/escrita sem bloqueio/controle de versão; testes com requisições simultâneas e transações são necessários.
- Considerar limites de requisições, auditoria, erros HTTP específicos e proteção contra repetição de pagamentos.
- `spring.jpa.hibernate.ddl-auto=update` é conveniente durante o desenvolvimento, mas migrar para Flyway/Liquibase em produção.

## Credenciais

`Codigo-IoT/ESP32/secrets.h` fica **fora do Git**. Copie `secrets.example.h` para `secrets.h` e insira os dados da sua rede. **Uma senha de Wi-Fi já esteve publicada no histórico do GitHub**: altere a senha da rede e faça higienização do histórico com apoio da equipe; remover do commit atual não elimina versões anteriores.

Não commitar arquivos `.env`, tokens, senhas, chaves e dados pessoais. Configurar segredos no serviço de hospedagem.

## Antes de publicar ou habilitar pagamentos

- Hash de senhas e autenticação autorizada nos endpoints.
- HTTPS com validação e identidade de dispositivo.
- Operações monetárias atômicas, idempotentes e auditáveis.
- Testes de concorrência e de falha de comunicação.
- Validação física, elétrica e de segurança do mecanismo.
