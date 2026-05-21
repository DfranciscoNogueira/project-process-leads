import {useEffect, useState} from 'react';
import {getJson} from '../api/http';

export function LeadList({refreshKey}) {

    const [filters, setFilters] = useState({nome: '', email: '', origem: ''});
    const [page, setPage] = useState({content: [], number: 0, totalPages: 0});

    async function load(pageNumber = 0) {
        const params = new URLSearchParams({page: pageNumber, size: 10});
        Object.entries(filters).forEach(([key, value]) => value && params.append(key, value));
        setPage(await getJson(`/api/leads?${params}`));
    }

    useEffect(() => {
        load(0).catch(console.error);
    }, [refreshKey]);

    return <section className="card">
        <h2>Leads importados</h2>
        <div className="filters">
            {['nome', 'email', 'origem'].map(field => <input key={field} placeholder={field} value={filters[field]}
                                                             onChange={e => setFilters({
                                                                 ...filters,
                                                                 [field]: e.target.value
                                                             })}/>)}
            <button onClick={() => load(0)}>Buscar</button>
        </div>
        <table>
            <thead>
            <tr>
                <th>Nome</th>
                <th>Email</th>
                <th>Telefone</th>
                <th>Origem</th>
                <th>Cadastro</th>
            </tr>
            </thead>
            <tbody>{page.content.map(lead => <tr key={lead.id}>
                <td>{lead.nome}</td>
                <td>{lead.email}</td>
                <td>{lead.telefone}</td>
                <td>{lead.origem}</td>
                <td>{lead.dataCadastro}</td>
            </tr>)}</tbody>
        </table>
        <div className="pagination">
            <button disabled={page.number === 0} onClick={() => load(page.number - 1)}>Anterior</button>
            <span>Página {page.number + 1} de {Math.max(page.totalPages, 1)}</span>
            <button disabled={page.number + 1 >= page.totalPages} onClick={() => load(page.number + 1)}>Próxima</button>
        </div>
    </section>;
}
