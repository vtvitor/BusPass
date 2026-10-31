# Segurança e configuração do BusPass

O BusPass é um projeto educacional de demonstração, **não** um sistema autorizado para cobrança de tarifas ou dados pessoais reais.

## Mudanças na autenticação

- Novos usuários: senhas protegidas com **BCrypt**; o servidor nunca retorna senhas.
- Contas antigas: após um **login válido** a senha antiga em texto é substituída por um hash BCrypt. Use somente o banco de testes e faça backup antes da migração.
- Login/cadastro retorna `token`, válido por 12 horas. O front-end armazena a sessão somente durante a aba do navegador (sessionStorage).
- `GET /api/cartoes`, cadastro, recarga, bloqueio, histórico e exclusão exigem `Authorization: Bearer <token>` e verificam o dono.
- Bloqueio do cartão usa confirmação de senha no **backend**. O leitor RFID não pode debitar um cartão bloqueado.
- Recargas e débitos usam transações e bloqueio pessimista de linha para evitar perda de saldo por acessos simultâneos.
- Saldo inicial dos novos cartões é **R$ 0,00**; nenhum cliente pode definir saldo arbitrário ao cadastrar. Recarga é somente simulação, sem dinheiro real.

## Variáveis de ambiente do backend

Configure no Render (serviço Java) ou no seu ambiente local:

| Nome | Uso |
| --- | --- |
| `DB_URL` | JDBC MySQL, ex.: `jdbc:mysql://localhost:3306/busspay` |
| `DB_USERNAME` | Usuário do MySQL |
| `DB_PASSWORD` | Senha do MySQL — nunca publique |
| `BUSPASS_AUTH_SECRET` | Segredo aleatório com **mínimo 32 caracteres** para assinar sessões. Recomendado e obrigatório para sessões estáveis entre reinícios. |
| `BUSPASS_DEVICE_TOKEN` | Segredo **aleatório e diferente** do anterior, recomendado com 32+ caracteres; autoriza o ESP32 no endpoint `/api/cartoes/pagar`. |
| `BUSPASS_TARIFA_CENTAVOS` | Tarifa de demonstração fixada no backend; padrão: `500` |
| `DDL_AUTO` | `update` em desenvolvimento; prefira migração de banco versionada em produção. |

Se `BUSPASS_AUTH_SECRET` não for fornecido, o backend usa segredo aleatório por inicialização, **invalidando todas as sessões após reinício**.
O endpoint RFID é desabilitado enquanto `BUSPASS_DEVICE_TOKEN` não estiver configurado.

**Atenção à implantação:** os serviços existentes no Render podem apontar para os repositórios separados `backend-projeto` e `frontend-projeto`, não para `BusPass`. Publicar neste repositório principal não atualiza esses serviços automaticamente. Configure-os para usar `Back-end/` e `Front-end/` desta versão (ou faça um deploy correspondente) e atualize as variáveis acima. Não publique um front-end novo contra um backend antigo: a nova interface precisa de tokens.

## Firmware ESP32 e HTTPS

1. Copie `Codigo-IoT/ESP32/secrets.example.h` para `secrets.h` **no seu computador**.
2. Preencha a rede Wi-Fi, senha, `DEVICE_TOKEN` (igual à variável `BUSPASS_DEVICE_TOKEN` do backend) e `ROOT_CA`, com o certificado raiz PEM **confiável e válido** para a conexão TLS do serviço.
3. Carregue o firmware no ESP32. A implementação não usa mais `setInsecure()`.
4. **Troque imediatamente** qualquer senha Wi-Fi ou token que já tenha aparecido no histórico público do GitHub. Apagar de um commit recente não remove o histórico antigo.
5. Confira a fiação elétrica e os níveis lógicos entre ESP32 3,3 V e Arduino Mega 5 V antes de conectar os dispositivos.

## Histórico e migração do banco

A nova entidade `movimentacao` registra **recargas e débitos futuros**, com data, valor e saldo restante. Não recria automaticamente o histórico antigo. O campo `cartao.bloqueado` é persistente.

Em bancos existentes, faça backup e confirme as alterações do esquema em ambiente de testes. `DDL_AUTO=update` pode adicionar as estruturas ausentes, mas **não substitui uma migração controlada**. O arquivo `Banco-De-Dados/Creates.sql` é direcionado a novas instalações, não deve ser reexecutado sobre dados existentes sem revisão.

## Testes e revisão

```bash
cd Back-end
mvn test
```

Os testes automatizados adicionados validam regras críticas com mocks, mas ainda são necessários testes de integração MySQL, testes no navegador e no hardware físico. O sistema não deve ser considerado auditado ou pronto para produção.

Para contribuições de segurança, não envie senhas ou segredos em issues públicas.
