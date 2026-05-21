import {useRef, useState} from 'react';
import {UploadCloud} from 'lucide-react';
import {API_URL, uploadCsv} from '../api/http';

const FINAL_STATUSES = ['FINALIZADO', 'FINALIZADO_COM_ERROS', 'FALHOU'];

export function UploadCsv({onProcessingFinished}) {
    const [file, setFile] = useState(null);
    const [status, setStatus] = useState(null);
    const [progress, setProgress] = useState(0);
    const [processing, setProcessing] = useState(false);
    const inputRef = useRef(null);

    async function submit() {
        if (!file || processing) return;

        setProcessing(true);
        setProgress(0);

        try {
            const response = await uploadCsv(file);
            setStatus(response);

            const source = new EventSource(`${API_URL}/api/lotes/${response.loteId}/stream`);

            source.addEventListener('lote-status', event => {
                const data = JSON.parse(event.data);
                const statusAtual = data.status;

                setProgress(Math.round(data.progressoPercentual));

                if (FINAL_STATUSES.includes(statusAtual)) {
                    source.close();
                    setProcessing(false);
                    onProcessingFinished?.();
                }
            });

            source.onerror = () => {
                source.close();
                setProcessing(false);
            };
        } catch (error) {
            console.error(error);
            setProcessing(false);
        }
    }

    return <section className="card">
        <h2>Importar CSV</h2>
        <div className="dropzone" onClick={() => inputRef.current?.click()} onDrop={e => {
            e.preventDefault();
            setFile(e.dataTransfer.files[0]);
        }} onDragOver={e => e.preventDefault()}>
            <UploadCloud size={36}/>
            <p>Arraste o CSV aqui ou clique para selecionar</p>
            <small>Colunas: nome, email, telefone, origem, data_cadastro</small>
            <input ref={inputRef} type="file" accept=".csv" hidden onChange={e => setFile(e.target.files[0])}/>
        </div>
        {file && <p className="selected">Arquivo selecionado: {file.name}</p>}
        <button onClick={submit}
                disabled={!file || processing}>{processing ? 'Processando...' : 'Enviar e processar'}</button>
        {status && <div className="progress-wrapper"><p>Lote: {status.loteId}</p>
            <div className="progress"><span style={{width: `${progress}%`}}/></div>
            <strong>{progress}%</strong></div>}
    </section>;
}
