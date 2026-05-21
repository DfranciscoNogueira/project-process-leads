export function MetricCard({title, value, subtitle}) {
    return <section className="metric-card"><span>{title}</span><strong>{value}</strong><small>{subtitle}</small></section>;
}
