import {useEffect, useState} from 'react';
import {getJson} from '../api/http';
import {MetricCard} from '../components/MetricCard';

export function Dashboard({refreshKey}) {

    const [data, setData] = useState({totalLeads: 0, lotesProcessados: 0, taxaErroPercentual: 0});

    useEffect(() => {
        getJson('/api/dashboard').then(setData).catch(console.error);
    }, [refreshKey]);

    return <div className="grid three">
        <MetricCard title="Total de leads" value={data.totalLeads} subtitle="Registros persistidos"/>
        <MetricCard title="Lotes processados" value={data.lotesProcessados} subtitle="Finalizados"/>
        <MetricCard title="Taxa de erro" value={`${data.taxaErroPercentual.toFixed(2)}%`} subtitle="Linhas inválidas ou duplicadas"/>
    </div>;
}
