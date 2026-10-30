# Protótipo físico — montagem e testes

## Componentes identificados no código

| Peça | Função |
| --- | --- |
| ESP32 | Wi-Fi, leitor RFID e requisições HTTPS |
| RFID RC522 | Leitura de cartão por SPI |
| Arduino Mega | Recebe mensagens do ESP32 pela Serial1 |
| Display TFT compatível com MCUFRIEND_kbv | Mensagens e saldo |
| 28BYJ-48 com **driver ULN2003** | Motor da catraca |
| Peças STL em `Modelagem-fisica/` | Estrutura física |

## Pinos atualmente usados

**ESP32 ↔ RC522 (conforme firmware):** SS/SDA GPIO 5, RST GPIO 2, SCK GPIO 18, MISO GPIO 19, MOSI GPIO 13. Verifique os nomes dos pinos impressos na sua placa antes de ligar.

**ESP32 ↔ Mega (UART):** ESP32 TX2 GPIO 17 → Mega RX1 pino 19; Mega TX1 pino 18 → ESP32 RX2 GPIO 16 **somente com conversor de nível 5 V → 3,3 V**; terra (GND) em comum. A entrada do ESP32 não é tolerante a 5 V.

**Mega ↔ ULN2003:** entradas IN1, IN2, IN3, IN4 em D30, D31, D32, D33. Conector do motor vai na placa ULN2003, **não nos pinos do ESP32/Mega**.

### Alimentação

**Não conecte o fio vermelho do 28BYJ-48 diretamente ao VIN/GND de um ESP32.** Use um driver ULN2003 adequado ao motor e uma alimentação externa de tensão/corrente compatíveis, com GND comum. Não ligue as bobinas diretamente aos microcontroladores. Não conecte sinal de 5 V diretamente a GPIO do ESP32.

### Testes recomendados

1. Confirme a tensão das placas e o GND comum com multímetro, sem motor conectado.
2. Teste o display TFT isoladamente no Mega.
3. Teste a leitura RC522 no ESP32.
4. Confirme a conexão Wi-Fi e a resposta da API sem acionar motor.
5. Teste o motor com ULN2003 e fonte correta, separado da lógica de pagamento.
6. Integre os dois microcontroladores, primeiro usando apenas mensagens de simulação.
7. Verifique débitos repetidos, saldo insuficiente, falha de rede e desligamento inesperado.

**Atenção:** não considere sucesso HTTP como prova de liberação mecânica. O código ainda precisa de confirmação do Mega e recuperação de falhas.
