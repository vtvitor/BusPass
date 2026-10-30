#include <SPI.h>
#include <MFRC522.h>
#include <WiFi.h>
#include <WiFiClientSecure.h>
#include <HTTPClient.h>

// ===== RC522 =====
#define SS_PIN    5    // SDA
#define RST_PIN   2    // RST
#define SCK_PIN   18
#define MISO_PIN  19
#define MOSI_PIN  13
MFRC522 rfid(SS_PIN, RST_PIN);

// ===== WIFI =====
#include "secrets.h"  // Defina WIFI_SSID e WIFI_PASSWORD localmente
const char* ssid = WIFI_SSID;
const char* password = WIFI_PASSWORD;

// ===== SERVIDOR JAVA (RENDER) =====
// Troque pelo endereco do seu servico no Render (sempre https, sem porta)
const char* serverUrl = "https://backend-projeto-tkvz.onrender.com/api/cartoes/pagar";

// ===== ESP32 -> MEGA (TX2 = GPIO17, RX2 = GPIO16) =====
HardwareSerial MegaSerial(2);

const int valorCentavos = 500; // R$ 5,00
const int TEMPO_CATRACA_MS = 4000; // tempo em que o Mega gira o motor (evita ler outro cartao)

void conectarWiFi() {
  if (WiFi.status() == WL_CONNECTED) return;
  Serial.print("Conectando ao WiFi");
  WiFi.mode(WIFI_STA);
  WiFi.begin(ssid, password);
  unsigned long inicio = millis();
  while (WiFi.status() != WL_CONNECTED && millis() - inicio < 15000) {
    delay(500);
    Serial.print(".");
  }
  if (WiFi.status() == WL_CONNECTED) {
    Serial.println("\nWiFi conectado! IP: " + WiFi.localIP().toString());
  } else {
    Serial.println("\nFalha ao conectar no WiFi.");
  }
}

void setup() {
  Serial.begin(115200);

  // Comunicacao com o Mega (RX=16, TX=17)
  MegaSerial.begin(9600, SERIAL_8N1, 16, 17);

  // SPI.begin(SCK, MISO, MOSI, SS)
  SPI.begin(SCK_PIN, MISO_PIN, MOSI_PIN, SS_PIN);
  rfid.PCD_Init();

  Serial.println("\n==============================");
  Serial.println("ESP32 - CATRACA RFID INICIADA");
  Serial.println("==============================");

  // Versao do firmware do RC522 (0x91 ou 0x92 = ligacao OK)
  rfid.PCD_DumpVersionToSerial();

  conectarWiFi();
}

void loop() {
  if (!rfid.PICC_IsNewCardPresent()) return;
  if (!rfid.PICC_ReadCardSerial()) return;

  String uid = "";
  for (byte i = 0; i < rfid.uid.size; i++) {
    if (rfid.uid.uidByte[i] < 0x10) uid += "0";
    uid += String(rfid.uid.uidByte[i], HEX);
  }
  uid.toUpperCase();

  Serial.println("\n------------------------------");
  Serial.print("CARTAO DETECTADO: "); Serial.println(uid);

  rfid.PICC_HaltA();
  rfid.PCD_StopCrypto1();

  if (WiFi.status() != WL_CONNECTED) {
    Serial.println("WiFi desconectado, tentando reconectar...");
    conectarWiFi();
  }
  if (WiFi.status() != WL_CONNECTED) {
    Serial.println("Erro: WiFi desconectado!");
    MegaSerial.println("ERRO");
    return;
  }

  // HTTPS (Render): aceita o certificado sem validar
  WiFiClientSecure client;
  client.setInsecure();

  HTTPClient http;
  http.setTimeout(30000);              // Render gratis pode demorar ate ~60 s para acordar
  http.setFollowRedirects(HTTPC_STRICT_FOLLOW_REDIRECTS);
  if (!http.begin(client, serverUrl)) {
    Serial.println("Erro ao iniciar HTTP!");
    MegaSerial.println("ERRO");
    return;
  }

  http.addHeader("Content-Type", "application/json");
  String json = "{\"numero\":\"" + uid + "\",\"valorCentavos\":" + String(valorCentavos) + "}";

  Serial.println("Enviando para servidor: " + json);
  int httpCode = http.POST(json);

  bool liberarCatraca = false;

  if (httpCode >= 200 && httpCode < 300) {
    String resposta = http.getString();
    Serial.println("Resposta: " + resposta);

    String respSemEspaco = resposta;
    respSemEspaco.replace(" ", "");

    if (respSemEspaco.indexOf("\"sucesso\":true") >= 0) {
      int inicio = respSemEspaco.indexOf("\"novoSaldoCentavos\":");
      if (inicio >= 0) {
        inicio += strlen("\"novoSaldoCentavos\":");
        int fim = respSemEspaco.indexOf(",", inicio);
        if (fim < 0) fim = respSemEspaco.indexOf("}", inicio);

        int saldoRestante = respSemEspaco.substring(inicio, fim).toInt();
        int saldoAntes = saldoRestante + valorCentavos;

        String msg = "SALDO_ANTES:" + String(saldoAntes) +
                     ";DESCONTO:" + String(valorCentavos) +
                     ";SALDO_RESTANTE:" + String(saldoRestante);

        Serial.println("Enviando para Mega: " + msg);
        MegaSerial.println(msg);
        MegaSerial.flush();

        liberarCatraca = true;
      } else {
        Serial.println("Resposta sem novoSaldoCentavos");
        MegaSerial.println("ERRO");
      }
    } else {
      Serial.println("PAGAMENTO NEGADO");
      MegaSerial.println("NEGADO");
    }
  } else {
    Serial.print("Erro HTTP Code: "); Serial.println(httpCode);
    MegaSerial.println("ERRO");
  }

  http.end();

  // Pagamento aprovado: o MEGA gira a catraca. Aqui so aguardamos a volta terminar.
  if (liberarCatraca) {
    Serial.println("Aguardando catraca (Mega)...");
    delay(TEMPO_CATRACA_MS);
  }

  delay(1500);
}
