import { Link, NavLink } from "react-router-dom";
import { useAuth } from "../auth/AuthProvider";
import { BadgePalette } from "../components/impact/BadgePalette";
import { ImpactMetrics } from "../components/impact/ImpactMetrics";
import { LeaderboardTable } from "../components/impact/LeaderboardTable";
import {
  IMPACT_COPY,
  IMPACT_ROUTES
} from "../constants/impactConstants";
import { useImpactData } from "../hooks/useImpactData";
import { formatUserType } from "../utils/impactUtils";

export function ImpactPage() {
  const { user, logout } = useAuth();
  const {
    badges,
    dataNote,
    donorLeaderboard,
    donorPage,
    impact,
    setDonorPage,
    setStudentPage,
    studentLeaderboard,
    studentPage,
    visibleMetrics
  } = useImpactData(user);

  return (
    <div className="impact-shell">
      <header className="feed-header">
        <div>
          <h1>{IMPACT_COPY.PAGE_TITLE}</h1>
          <p className="feed-subtitle">
            {formatUserType(impact.userType)} impact for {user?.displayName || user?.email}
          </p>
        </div>
        <div className="feed-header-right">
          <nav className="top-tabs" aria-label={IMPACT_COPY.NAV_LABEL}>
            <NavLink to={IMPACT_ROUTES.FEED} end className={({ isActive }) => `top-tab${isActive ? " active" : ""}`}>
              {IMPACT_COPY.FEED_TAB}
            </NavLink>
            <NavLink to={IMPACT_ROUTES.IMPACT} className={({ isActive }) => `top-tab${isActive ? " active" : ""}`}>
              {IMPACT_COPY.IMPACT_TAB}
            </NavLink>
          </nav>
          <Link className="feed-donate-button" to={IMPACT_ROUTES.FEED}>
            {IMPACT_COPY.DONATE_FOOD}
          </Link>
          <button className="feed-logout-button" type="button" onClick={() => logout()}>
            {IMPACT_COPY.LOGOUT}
          </button>
        </div>
      </header>

      <main className="impact-content">
        {dataNote ? <p className="impact-note">{dataNote}</p> : null}

        <div className="impact-layout">
          <div className="impact-left-column">
            <section className="impact-hero">
              <div>
                <span className="eyebrow">{IMPACT_COPY.MY_IMPACT_EYEBROW}</span>
                <h2>{IMPACT_COPY.MY_IMPACT_TITLE}</h2>
              </div>
            </section>

            <ImpactMetrics
              metrics={visibleMetrics}
              badgesEarned={impact.badges?.length || 0}
            />

            <BadgePalette badges={badges} />
          </div>

          <aside className="impact-right-column" aria-label={IMPACT_COPY.LEADERBOARDS_ARIA_LABEL}>
            <LeaderboardTable
              title={IMPACT_COPY.DONOR_LEADERBOARD_TITLE}
              items={donorLeaderboard.items}
              page={donorPage}
              total={donorLeaderboard.total}
              onPageChange={setDonorPage}
            />

            <LeaderboardTable
              title={IMPACT_COPY.STUDENT_LEADERBOARD_TITLE}
              items={studentLeaderboard.items}
              page={studentPage}
              total={studentLeaderboard.total}
              onPageChange={setStudentPage}
              student
            />
          </aside>
        </div>
      </main>
    </div>
  );
}
