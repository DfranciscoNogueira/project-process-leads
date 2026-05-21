import React, {useState} from 'react';
import {createRoot} from 'react-dom/client';
import {Dashboard} from './pages/Dashboard';
import {UploadCsv} from './pages/UploadCsv';
import {LeadList} from './pages/LeadList';
import './styles/main.css';

function App() {
    const [refreshKey, setRefreshKey] = useState(0);

    function refreshPageData() {
        setRefreshKey(current => current + 1);
    }

    return <main className="app">
        <header><span>Diego Francisco</span><h1>Importação assíncrona de Leads</h1></header>
        <Dashboard refreshKey={refreshKey}/>
        <UploadCsv onProcessingFinished={refreshPageData}/>
        <LeadList refreshKey={refreshKey}/>
    </main>;
}

createRoot(document.getElementById('root')).render(<React.StrictMode><App/></React.StrictMode>);
