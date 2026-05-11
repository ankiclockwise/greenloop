import { IMPACT_COPY } from "../../constants/impactConstants";

export function BadgePalette({ badges }) {
  return (
    <section className="impact-panel">
      <div className="section-header">
        <div>
          <span className="eyebrow">{IMPACT_COPY.BADGES_EYEBROW}</span>
          <h2>{IMPACT_COPY.BADGES_TITLE}</h2>
        </div>
      </div>

      <div className="badge-grid">
        {badges.map((badge) => (
          <article className={`badge-card${badge.earned ? " earned" : ""}`} key={badge.id}>
            <div className="badge-card-top">
              {badge.earned ? null : <div className="badge-mark">{IMPACT_COPY.LOCKED_BADGE}</div>}
              <button
                type="button"
                className="badge-info-button"
                aria-label={`${badge.name}: ${badge.description}`}
              >
                {IMPACT_COPY.BADGE_INFO_SYMBOL}
                <span className="badge-tooltip" role="tooltip">
                  {badge.description}
                </span>
              </button>
            </div>
            <h3>{badge.name}</h3>
            {badge.earnedAt ? (
              <span>
                {IMPACT_COPY.EARNED_BADGE_DATE_PREFIX} {new Date(badge.earnedAt).toLocaleDateString()}
              </span>
            ) : null}
          </article>
        ))}
      </div>
    </section>
  );
}
