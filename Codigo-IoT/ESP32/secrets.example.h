#pragma once
// Copie para secrets.h na mesma pasta e preencha ANTES de compilar.
// Não inclua secrets.h no GitHub.
const char* ssid = "NOME_DA_REDE_WIFI";
const char* password = "SENHA_DA_REDE_WIFI";
const char* DEVICE_TOKEN = "MESMO_VALOR_DA_VARIAVEL_BUSPASS_DEVICE_TOKEN_NO_SERVIDOR";
// Insira o certificado PEM da autoridade certificadora que assina o TLS do servidor.
// Confira a CA do seu domínio; não cole uma chave privada!
const char* ROOT_CA = R"BUSPASS(
-----BEGIN CERTIFICATE-----
COLE_AQUI_CERTIFICADO_CA_REAL_EM_PEM
-----END CERTIFICATE-----
)BUSPASS";
