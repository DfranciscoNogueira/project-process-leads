CREATE TABLE lote (
    id UUID PRIMARY KEY,
    nome_arquivo VARCHAR(255) NOT NULL,
    caminho_arquivo VARCHAR(500) NOT NULL,
    status VARCHAR(30) NOT NULL,
    total_linhas INTEGER NOT NULL DEFAULT 0,
    total_sucesso INTEGER NOT NULL DEFAULT 0,
    total_erros INTEGER NOT NULL DEFAULT 0,
    criado_em TIMESTAMP NOT NULL,
    iniciado_em TIMESTAMP,
    finalizado_em TIMESTAMP
);

CREATE TABLE lead (
    id UUID PRIMARY KEY,
    nome VARCHAR(180) NOT NULL,
    email VARCHAR(220) NOT NULL,
    telefone VARCHAR(40) NOT NULL,
    origem VARCHAR(120) NOT NULL,
    data_cadastro DATE NOT NULL,
    lote_id UUID NOT NULL REFERENCES lote(id),
    criado_em TIMESTAMP NOT NULL,
    CONSTRAINT uk_lead_email_origem UNIQUE (email, origem)
);

CREATE TABLE lote_processamento (
    id UUID PRIMARY KEY,
    lote_id UUID NOT NULL REFERENCES lote(id),
    chunk_index INTEGER NOT NULL,
    total_linhas INTEGER NOT NULL,
    total_sucesso INTEGER NOT NULL,
    total_erros INTEGER NOT NULL,
    tempo_ms BIGINT NOT NULL,
    processado_em TIMESTAMP NOT NULL,
    CONSTRAINT uk_lote_chunk UNIQUE (lote_id, chunk_index)
);

CREATE INDEX idx_lead_nome ON lead(nome);
CREATE INDEX idx_lead_email ON lead(email);
CREATE INDEX idx_lead_origem ON lead(origem);
CREATE INDEX idx_lote_status ON lote(status);
