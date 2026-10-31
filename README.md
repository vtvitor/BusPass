# 🚌 BusPass — Mobilidade urbana e validação inteligente de transporte

<p align="center">
  <strong>Uma plataforma de transporte público que conecta aplicação web, API Java e prototipagem IoT.</strong>
</p>

<p align="center">
  <img src="assets/buspass-prototipo.svg" alt="Ilustração conceitual do terminal BusPass com tela, RFID, cartão, ESP32 e Arduino" width="100%" />
</p>

<p align="center"><sub>Ilustração conceitual do protótipo. A montagem física e a integração ponta a ponta ainda precisam de validação.</sub></p>

<p align="center">
  <a href="https://landing-page-wt6o.onrender.com">Conhecer o projeto</a> ·
  <a href="https://github.com/vtvitor/BusPass/tree/main/Front-end">Front-end</a> ·
  <a href="https://github.com/vtvitor/BusPass/tree/main/Back-end">Back-end</a> ·
  <a href="https://github.com/vtvitor/BusPass/tree/main/Codigo-IoT">Firmware IoT</a>
</p>

> **Status:** projeto em desenvolvimento, de caráter demonstrativo/educacional. Existem componentes de software e firmware implementados, mas o conjunto físico integrado não está documentado como homologado. As recargas do sistema registram créditos, **não processam pagamentos bancários reais**.

## Sobre o projeto

Desenvolvido pela equipe **Four Sync**, o BusPass reúne em um mesmo repositório uma aplicação web para o passageiro, uma API REST em Java/Spring Boot, scripts de banco de dados, firmware de eletrônica embarcada e arquivos de modelagem 3D.

O objetivo é simplificar o acompanhamento de cartões e saldos, a consulta a linhas e pontos de ônibus e a interação com um protótipo de terminal de validação por RFID.

### Principais recursos

| Área | O que existe no código |
| --- | --- |
| **Acesso** | Cadastro de usuários e endpoint de login |
| **Cartões** | Cadastro, listagem por usuário, exclusão e consulta de saldo |
| **Recargas** | Adição de créditos ao saldo registrado no banco, sem gateway de pagamento |
| **Passagens** | Endpoint de débito por número de cartão, com verificação de saldo |
| **Mobilidade** | Endpoints para consulta de linhas e pontos e interface web com recursos de mapa |
| **IoT** | Firmware do ESP32 para leitura RC522 e requisição HTTP; firmware do Arduino Mega para display TFT e motor |

**Importante:** a existência dessas rotinas no código não significa que o sistema completo esteja testado em ambiente de produção.

## Tecnologias

| Camada | Tecnologias |
| --- | --- |
| Interface web | HTML5, CSS3, JavaScript |
| API | Java 21, Spring Boot 3.5.6, REST/JSON |
| Persistência | MySQL, Spring Data JPA / Hibernate |
| Build e implantação | Maven, Dockerfile e configuração para hospedagem |
| Eletrônica | ESP32, Arduino Mega, RFID-RC522 e display TFT |
| Prototipagem | Modelos STL e impressão 3D |

## Protótipo físico

A proposta é aproximar um cartão do **RFID-RC522**, identificar o cartão pelo **ESP32** e consultar a API para verificar a possibilidade de débito de passagem. O firmware do ESP32 já contém uma chamada ao endpoint `POST /api/cartoes/pagar`. O código do Arduino Mega inclui mensagens no **display TFT** e acionamento do **motor de passos** da catraca.

```text
Cartão RFID → RC522 → ESP32 → API REST (Spring Boot) → MySQL
                         |
                         └── comunicação serial → Arduino Mega
                                                    ├── display TFT
                                                    └── motor da catraca
```

O fluxo acima descreve a integração **proposta/implementada em partes**. O teste de funcionamento conjunto, a montagem elétrica definitiva e o comportamento mecânico da catraca devem ser confirmados em bancada.

Os arquivos em [`Modelagem-fisica/`](Modelagem-fisica/) incluem modelos STL de componentes mecânicos. A ilustração desta página é uma **visualização do conceito**, não uma fotografia do produto final nem um substituto para medidas de fabricação.

## Organização do repositório

Este é um **repositório único**, com os módulos organizados em pastas:

```text
BusPass/
├── Back-end/                 # API Spring Boot, entidades e serviços
│   ├── pom.xml
│   ├── Dockerfile
│   └── src/main/
├── Front-end/                # Aplicação web HTML/CSS/JavaScript
│   ├── index.html
│   ├── style.css
│   ├── script.js
│   └── api.js
├── Banco-De-Dados/
│   └── Creates.sql
├── Codigo-IoT/
│   ├── ESP32/esp32.ino
│   └── Arduino_TFT/Arduino_TFT.ino
├── Modelagem-fisica/         # Peças e modelos STL
├── Landing-page/             # Apresentação do projeto
├── assets/
│   └── buspass-prototipo.svg
└── README.md
```

## Como executar localmente

**Pré-requisitos:** JDK 21, Maven, MySQL e navegador moderno. Para o hardware, são necessários os componentes e bibliotecas específicas dos sketches.

1. Clone o repositório:

   ```bash
   git clone https://github.com/vtvitor/BusPass.git
   cd BusPass
   ```

2. Prepare um banco MySQL e consulte os scripts em `Banco-De-Dados/Creates.sql`. Confira se o esquema corresponde às entidades atuais antes de importar dados.

3. Configure a conexão do MySQL em `Back-end/src/main/resources/application.properties` usando **variáveis de ambiente** para dados sensíveis, sem publicar senhas ou credenciais reais.

4. Inicie a API (na pasta `Back-end`):

   ```bash
   cd Back-end
   mvn spring-boot:run
   ```

5. Configure a URL da API em `Front-end/api.js` e sirva a pasta `Front-end` em um servidor HTTP local (por exemplo, a extensão Live Server do VS Code).

6. Para a eletrônica, configure a rede e os pinos para seu hardware, instale as bibliotecas Arduino necessárias e grave os sketches em `Codigo-IoT/`. Faça o teste elétrico antes de ligar o motor ou a comunicação serial entre placas.

### Principais endpoints disponíveis

| Método | Rota | Finalidade |
| --- | --- | --- |
| `POST` | `/api/auth/register` | Cadastrar usuário |
| `POST` | `/api/auth/login` | Login |
| `GET` | `/api/cartoes?usuarioId=...` | Consultar cartões |
| `POST` | `/api/cartoes` | Cadastrar cartão |
| `POST` | `/api/cartoes/{id}/recarga` | Adicionar créditos |
| `POST` | `/api/cartoes/pagar` | Debitar saldo por cartão |
| `GET` | `/api/linhas` | Listar linhas |
| `GET` | `/api/pontos` | Listar pontos |

## Limitações e próximos passos

O projeto **não deve ser utilizado para cobrança real ou com dados pessoais reais sem reforçar a segurança**. Antes de qualquer publicação para uso real, recomendamos:

- Implementar hash seguro de senhas e autenticação/autorização por sessão ou token; o fluxo atual não possui proteção adequada.
- Remover credenciais de rede do firmware e de qualquer histórico público, rotacionando credenciais que já tenham sido expostas.
- Validar certificados HTTPS no ESP32, substituindo o uso de `setInsecure()`.
- Adicionar validação de entradas, regras de acesso, transações atômicas de débito/recarga e tratamento padronizado de erros.
- Criar testes automatizados da API e testes ponta a ponta do fluxo RFID → débito → display/motor.
- Revisar segurança elétrica e níveis de tensão na comunicação entre **Arduino Mega (5 V)** e **ESP32 (3,3 V)**.
- Validar impressão, tolerâncias, fixação dos componentes e montagem física final.
- Estudar integração futura de pagamentos reais, notificações de saldo e dados de transporte atualizados.

## Equipe — Four Sync

| Integrante | Responsabilidade |
| --- | --- |
| [Vitor Derencio](https://github.com/vtvitor) | Desenvolvimento full-stack |
| [Leticia S. Meira](https://github.com/leticiameira-star) | Desenvolvimento full-stack |
| [Maria C. Feitosa](https://github.com/konomi-naruko) | Desenvolvimento front-end |
| [Kauan Fernandes S.](https://github.com/kauan-cmd-alt) | UI/UX |

---

<p align="center">Desenvolvido pela equipe <strong>Four Sync</strong> · BusPass</p>
