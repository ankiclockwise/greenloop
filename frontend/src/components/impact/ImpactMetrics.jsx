import { IMPACT_COPY, METRIC_LABELS } from "../../constants/impactConstants";

export function ImpactMetrics({ metrics, badgesEarned }) {
  return (
    <section className="impact-metrics" aria-label={IMPACT_COPY.IMPACT_METRICS_ARIA_LABEL}>
      {metrics.map((metric) => (
        <article className="impact-metric" key={metric.label}>
          <span>{metric.label}</span>
          <strong>{metric.value}</strong>
        </article>
      ))}
      <article className="impact-metric">
        <span>{METRIC_LABELS.BADGES_EARNED}</span>
        <strong>{badgesEarned}</strong>
      </article>
    </section>
  );
}
