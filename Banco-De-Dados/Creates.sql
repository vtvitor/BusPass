-- ============================================
-- BUSSPAY - Banco de Dados
-- Projeto: Ônibus São Carlos - SP
-- Para usar com Spring Boot no Java
-- ============================================

-- Cria o banco
CREATE DATABASE IF NOT EXISTS busspay;
USE busspay;

-- ============================================
-- Tabela de usuários
-- ============================================
CREATE TABLE usuario (
    id    INT AUTO_INCREMENT PRIMARY KEY,
    nome  VARCHAR(100) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    cpf   VARCHAR(14)  NOT NULL UNIQUE,
    senha VARCHAR(255) NOT NULL,
    data_cadastro DATETIME DEFAULT CURRENT_TIMESTAMP
);

-- ============================================
-- Tabela de operadoras (empresa de ônibus)
-- ============================================
CREATE TABLE operadora (
    id   INT AUTO_INCREMENT PRIMARY KEY,
    nome VARCHAR(100) NOT NULL
);

-- ============================================
-- Tabela de linhas de ônibus
-- ============================================
CREATE TABLE linha (
    id           INT AUTO_INCREMENT PRIMARY KEY,
    numero       VARCHAR(10)  NOT NULL,
    nome         VARCHAR(100) NOT NULL,
    cor          VARCHAR(20),
    operadora_id INT NOT NULL,
    FOREIGN KEY (operadora_id) REFERENCES operadora(id)
);

-- ============================================
-- Tabela de pontos (paradas)
-- ============================================
CREATE TABLE ponto (
    id       INT AUTO_INCREMENT PRIMARY KEY,
    nome     VARCHAR(100) NOT NULL,
    endereco VARCHAR(200) NOT NULL,
    lat      DECIMAL(10,7) NOT NULL,
    lng      DECIMAL(10,7) NOT NULL,
    tipo     VARCHAR(20)  DEFAULT 'regular'  -- 'regular' ou 'terminal'
);

-- ============================================
-- Quais linhas passam em quais pontos (N:N)
-- ============================================
CREATE TABLE ponto_linha (
    ponto_id INT NOT NULL,
    linha_id INT NOT NULL,
    PRIMARY KEY (ponto_id, linha_id),
    FOREIGN KEY (ponto_id) REFERENCES ponto(id),
    FOREIGN KEY (linha_id) REFERENCES linha(id)
);

-- ============================================
-- Rota da linha (sequência de pontos)
-- ============================================
CREATE TABLE rota (
    id       INT AUTO_INCREMENT PRIMARY KEY,
    linha_id INT NOT NULL,
    sentido  VARCHAR(10) DEFAULT 'ida',  -- 'ida' ou 'volta'
    FOREIGN KEY (linha_id) REFERENCES linha(id)
);

-- ============================================
-- Pontos da rota em ordem
-- ============================================
CREATE TABLE rota_ponto (
    rota_id  INT NOT NULL,
    posicao  INT NOT NULL,   -- ordem do ponto (0, 1, 2...)
    ponto_id INT NOT NULL,
    PRIMARY KEY (rota_id, posicao),
    FOREIGN KEY (rota_id)  REFERENCES rota(id),
    FOREIGN KEY (ponto_id) REFERENCES ponto(id)
);

-- ============================================
-- Horários das linhas
-- ============================================
CREATE TABLE horario (
    id       INT AUTO_INCREMENT PRIMARY KEY,
    linha_id INT NOT NULL,
    dia      VARCHAR(15) NOT NULL,  -- 'weekday', 'saturday', 'sunday'
    hora     TIME NOT NULL,
    FOREIGN KEY (linha_id) REFERENCES linha(id)
);

-- ============================================
-- Cartão BUSSPAY do usuário
-- Guardei o saldo em centavos (inteiro) pra
-- não dar problema com número decimal
-- ============================================
CREATE TABLE cartao (
    id            INT AUTO_INCREMENT PRIMARY KEY,
    usuario_id    INT NOT NULL,
    numero        VARCHAR(30) NOT NULL UNIQUE,
    saldo_centavos INT DEFAULT 0,
    bloqueado     BOOLEAN NOT NULL DEFAULT FALSE,
    FOREIGN KEY (usuario_id) REFERENCES usuario(id)
);

-- ============================================
-- Histórico de recargas
-- ============================================
CREATE TABLE recarga (
    id             INT AUTO_INCREMENT PRIMARY KEY,
    cartao_id      INT NOT NULL,
    valor_centavos INT NOT NULL,
    data_recarga   DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (cartao_id) REFERENCES cartao(id)
);

-- ============================================
-- Movimentações de créditos demonstrativos e passagem
-- ============================================
CREATE TABLE IF NOT EXISTS movimentacao (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    cartao_id BIGINT NOT NULL,
    tipo VARCHAR(20) NOT NULL,
    valor_centavos INT NOT NULL,
    saldo_apos_centavos INT NOT NULL,
    data_hora DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_movimentacao_cartao_data (cartao_id, data_hora)
);

-- ============================================
-- Histórico de uso do cartão (passagens)
-- ============================================
CREATE TABLE passagem (
    id             INT AUTO_INCREMENT PRIMARY KEY,
    cartao_id      INT NOT NULL,
    linha_id       INT NOT NULL,
    valor_centavos INT NOT NULL,
    data_uso       DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (cartao_id) REFERENCES cartao(id),
    FOREIGN KEY (linha_id)  REFERENCES linha(id)
);

-- ============================================
-- Dados iniciais (seed)
-- ============================================
INSERT INTO operadora (nome) VALUES ('Serttel');

INSERT INTO linha (numero, nome, cor, operadora_id) VALUES
    ('01', 'Cidade Aracy',      '#1e88e5', 1),
    ('02', 'Santa Felícia',     'secondary', 1),
    ('03', 'CECAP',             'tertiary', 1),
    ('04', 'Vila Prado',        'direct', 1),
    ('05', 'Centro / Terminal', '#1e88e5', 1),
    ('06', 'Jardim Botânico',   'secondary', 1);
