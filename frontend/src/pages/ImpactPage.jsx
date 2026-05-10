import { useEffect, useMemo, useState } from "react";
import axios from "axios";
import { Link, NavLink } from "react-router-dom";
import { useAuth } from "../auth/AuthProvider";

const PAGE_SIZE = 5;

const BADGE_CATALOG = [
  {
    id: "first_donation",
    name: "First Donation",
    description: "Donated food for the first time.",
    appliesTo: ["retail_user", "store", "diner"],
    criteria: { metric: "foodDonated", threshold: 1 },
    earned: true,
    earnedAt: "2026-04-21T10:00:00Z"
  },
  {
    id: "community_helper",
    name: "Community Helper",
    description: "Shared 10 or more food donations.",
    appliesTo: ["retail_user", "store", "diner"],
    criteria: { metric: "donationCount", threshold: 10 },
    earned: true,
    earnedAt: "2026-04-29T16:30:00Z"
  },
  {
    id: "waste_warrior",
    name: "Waste Warrior",
    description: "Helped save 50kg or more of CO2.",
    appliesTo: ["retail_user", "store", "diner"],
    criteria: { metric: "co2SavedKg", threshold: 50 },
    earned: true,
    earnedAt: "2026-05-02T12:15:00Z"
  },
  {
    id: "first_pickup",
    name: "First Pickup",
    description: "Reserved and received food for the first time.",
    appliesTo: ["retail_user"],
    criteria: { metric: "foodReceived", threshold: 1 },
    earned: false
  },
  {
    id: "campus_rescuer",
    name: "Campus Rescuer",
    description: "Received food 10 or more times.",
    appliesTo: ["retail_user"],
    criteria: { metric: "pickupCount", threshold: 10 },
    earned: false
  },
  {
    id: "reliable_donor",
    name: "Reliable Donor",
    description: "Listed or donated food every week for 4 weeks.",
    appliesTo: ["store", "diner"],
    criteria: { metric: "weeklyDonationStreak", threshold: 4 },
    earned: false
  },
  {
    id: "neighborhood_hero",
    name: "Neighborhood Hero",
    description: "Helped complete 50 or more student pickups.",
    appliesTo: ["store", "diner"],
    criteria: { metric: "completedPickups", threshold: 50 },
    earned: false
  },
  {
    id: "top_contributor",
    name: "Top Contributor",
    description: "Reached the top 10 in your leaderboard.",
    appliesTo: ["retail_user", "store", "diner"],
    criteria: { metric: "leaderboardPosition", threshold: 10, comparison: "lessThanOrEqual" },
    earned: false
  },
  {
    id: "greenloop_champion",
    name: "GreenLoop Champion",
    description: "Saved 500kg or more of CO2 through GreenLoop activity.",
    appliesTo: ["retail_user", "store", "diner"],
    criteria: { metric: "co2SavedKg", threshold: 500 },
    earned: false
  }
];

const DONOR_LEADERBOARD = [
  { rank: 1, userId: "store_1", userType: "store", name: "Maple Market", foodDonated: 148, donationCount: 58, completedPickups: 132, co2SavedKg: 612.4 },
  { rank: 2, userId: "diner_1", userType: "diner", name: "North Campus Diner", foodDonated: 121, donationCount: 44, completedPickups: 118, co2SavedKg: 503.8 },
  { rank: 3, userId: "store_2", userType: "store", name: "Fresh Basket", foodDonated: 95, donationCount: 37, completedPickups: 92, co2SavedKg: 394.2 },
  { rank: 4, userId: "diner_2", userType: "diner", name: "Elm Street Eats", foodDonated: 84, donationCount: 31, completedPickups: 77, co2SavedKg: 332.1 },
  { rank: 5, userId: "store_3", userType: "store", name: "Green Shelf Grocery", foodDonated: 72, donationCount: 26, completedPickups: 69, co2SavedKg: 289.6 },
  { rank: 6, userId: "diner_3", userType: "diner", name: "Commons Kitchen", foodDonated: 69, donationCount: 24, completedPickups: 65, co2SavedKg: 271.3 },
  { rank: 7, userId: "store_4", userType: "store", name: "Corner Pantry", foodDonated: 61, donationCount: 22, completedPickups: 56, co2SavedKg: 248.7 },
  { rank: 8, userId: "diner_4", userType: "diner", name: "Late Plate Cafe", foodDonated: 57, donationCount: 19, completedPickups: 48, co2SavedKg: 221.5 },
  { rank: 9, userId: "store_5", userType: "store", name: "Harvest Co-op", foodDonated: 49, donationCount: 17, completedPickups: 43, co2SavedKg: 197.9 },
  { rank: 10, userId: "diner_5", userType: "diner", name: "South Hall Grill", foodDonated: 43, donationCount: 15, completedPickups: 39, co2SavedKg: 173.4 },
  { rank: 11, userId: "store_6", userType: "store", name: "Oak Street Grocer", foodDonated: 38, donationCount: 13, completedPickups: 34, co2SavedKg: 151.8 },
  { rank: 12, userId: "diner_6", userType: "diner", name: "Union Kitchen", foodDonated: 34, donationCount: 11, completedPickups: 29, co2SavedKg: 136.2 }
];

const STUDENT_LEADERBOARD = [
  { rank: 1, userId: "retail_user_1", userType: "retail_user", name: "Maya R.", foodReceived: 44, pickupCount: 39, foodDonated: 9, donationCount: 6, co2SavedKg: 103.2 },
  { rank: 2, userId: "retail_user_2", userType: "retail_user", name: "Jordan P.", foodReceived: 39, pickupCount: 35, foodDonated: 7, donationCount: 4, co2SavedKg: 94.8 },
  { rank: 3, userId: "retail_user_3", userType: "retail_user", name: "Ari S.", foodReceived: 35, pickupCount: 32, foodDonated: 11, donationCount: 8, co2SavedKg: 91.4 },
  { rank: 4, userId: "retail_user_4", userType: "retail_user", name: "Sam K.", foodReceived: 30, pickupCount: 28, foodDonated: 5, donationCount: 3, co2SavedKg: 75.9 },
  { rank: 5, userId: "retail_user_5", userType: "retail_user", name: "Nina T.", foodReceived: 28, pickupCount: 25, foodDonated: 4, donationCount: 3, co2SavedKg: 68.1 },
  { rank: 6, userId: "retail_user_6", userType: "retail_user", name: "Dev A.", foodReceived: 24, pickupCount: 22, foodDonated: 6, donationCount: 4, co2SavedKg: 62.6 },
  { rank: 7, userId: "retail_user_7", userType: "retail_user", name: "Leah M.", foodReceived: 19, pickupCount: 18, foodDonated: 3, donationCount: 2, co2SavedKg: 51.3 },
  { rank: 8, userId: "retail_user_8", userType: "retail_user", name: "Chris V.", foodReceived: 17, pickupCount: 16, foodDonated: 2, donationCount: 2, co2SavedKg: 43.7 },
  { rank: 9, userId: "retail_user_9", userType: "retail_user", name: "Priya N.", foodReceived: 15, pickupCount: 14, foodDonated: 4, donationCount: 3, co2SavedKg: 39.5 },
  { rank: 10, userId: "retail_user_10", userType: "retail_user", name: "Eli W.", foodReceived: 13, pickupCount: 13, foodDonated: 1, donationCount: 1, co2SavedKg: 33.8 },
  { rank: 11, userId: "retail_user_11", userType: "retail_user", name: "Tara J.", foodReceived: 11, pickupCount: 10, foodDonated: 2, donationCount: 2, co2SavedKg: 28.6 },
  { rank: 12, userId: "retail_user_12", userType: "retail_user", name: "Owen C.", foodReceived: 9, pickupCount: 9, foodDonated: 1, donationCount: 1, co2SavedKg: 22.4 }
];

function createFallbackImpact(user) {
  const userType = user?.actor || "retail_user";
  const isStudent = userType === "retail_user";

  return {
    userType,
    foodReceived: isStudent ? 18 : null,
    foodDonated: userType === "store" ? 42 : userType === "diner" ? 36 : 8,
    donationCount: userType === "store" ? 15 : userType === "diner" ? 12 : 6,
    pickupCount: isStudent ? 16 : null,
    completedPickups: isStudent ? null : userType === "store" ? 38 : 31,
    co2SavedKg: userType === "store" ? 180.2 : userType === "diner" ? 151.7 : 54.5,
    badges: BADGE_CATALOG.filter((badge) => badge.earned && badge.appliesTo.includes(userType)),
    leaderboardPosition: userType === "retail_user" ? 14 : userType === "store" ? 3 : 5
  };
}

function formatUserType(userType) {
  if (userType === "retail_user") return "Student";
  if (userType === "store") return "Store";
  if (userType === "diner") return "Diner";
  return "Member";
}

function paginate(items, page) {
  const start = (page - 1) * PAGE_SIZE;
  return items.slice(start, start + PAGE_SIZE);
}

function Pagination({ page, total, onChange }) {
  const totalPages = Math.max(1, Math.ceil(total / PAGE_SIZE));

  return (
    <div className="impact-pagination">
      <button type="button" onClick={() => onChange(page - 1)} disabled={page === 1}>
        Previous
      </button>
      <span>
        Page {page} of {totalPages}
      </span>
      <button type="button" onClick={() => onChange(page + 1)} disabled={page === totalPages}>
        Next
      </button>
    </div>
  );
}

function LeaderboardTable({ title, subtitle, items, page, total, onPageChange, student }) {
  return (
    <section className="impact-panel">
      <div className="section-header">
        <div>
          <span className="eyebrow">Leaderboard</span>
          <h2>{title}</h2>
        </div>
      </div>

      <div className="impact-table-wrap">
        <table className="impact-table">
          <thead>
            <tr>
              <th>Rank</th>
              <th>Name</th>
              {student ? <th>Received</th> : null}
              <th>Donated</th>
              <th>CO2 saved</th>
            </tr>
          </thead>
          <tbody>
            {items.map((item) => (
              <tr key={item.userId}>
                <td>#{item.rank}</td>
                <td>{item.name}</td>
                {student ? <td>{item.foodReceived}</td> : null}
                <td>{item.foodDonated}</td>
                <td>{item.co2SavedKg} kg</td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>

      <Pagination page={page} total={total} onChange={onPageChange} />
    </section>
  );
}

export function ImpactPage() {
  const { user, logout } = useAuth();
  const [impact, setImpact] = useState(() => createFallbackImpact(user));
  const [badges, setBadges] = useState(BADGE_CATALOG);
  const [donorLeaderboard, setDonorLeaderboard] = useState({ items: DONOR_LEADERBOARD, total: DONOR_LEADERBOARD.length });
  const [studentLeaderboard, setStudentLeaderboard] = useState({ items: STUDENT_LEADERBOARD, total: STUDENT_LEADERBOARD.length });
  const [donorPage, setDonorPage] = useState(1);
  const [studentPage, setStudentPage] = useState(1);
  const [dataNote, setDataNote] = useState("");

  const isStudent = impact.userType === "retail_user";
  const visibleMetrics = useMemo(() => {
    const metrics = [];

    if (isStudent && impact.foodReceived != null) {
      metrics.push({ label: "Food received", value: impact.foodReceived });
    }

    metrics.push(
      { label: "Food donated", value: impact.foodDonated },
      { label: "CO2 saved", value: `${impact.co2SavedKg} kg` },
      { label: "Leaderboard position", value: `#${impact.leaderboardPosition}` }
    );

    return metrics;
  }, [impact, isStudent]);

  useEffect(() => {
    let cancelled = false;

    async function loadImpact() {
      try {
        const [impactResponse, badgesResponse] = await Promise.all([
          axios.get("/api/impact/me"),
          axios.get("/api/impact/badges")
        ]);

        if (cancelled) return;

        setImpact({ ...createFallbackImpact(user), ...impactResponse.data });
        setBadges(badgesResponse.data?.badges || BADGE_CATALOG);
        setDataNote("");
      } catch {
        if (!cancelled) {
          setImpact(createFallbackImpact(user));
          setBadges(BADGE_CATALOG);
          setDataNote("Showing sample impact data until the backend impact APIs are available.");
        }
      }
    }

    loadImpact();

    return () => {
      cancelled = true;
    };
  }, [user]);

  useEffect(() => {
    let cancelled = false;

    async function loadDonors() {
      try {
        const response = await axios.get("/api/impact/leaderboard/donors", {
          params: { page: donorPage, limit: PAGE_SIZE }
        });

        if (!cancelled) {
          setDonorLeaderboard({
            items: response.data?.items || [],
            total: response.data?.total || 0
          });
        }
      } catch {
        if (!cancelled) {
          setDonorLeaderboard({
            items: paginate(DONOR_LEADERBOARD, donorPage),
            total: DONOR_LEADERBOARD.length
          });
        }
      }
    }

    loadDonors();

    return () => {
      cancelled = true;
    };
  }, [donorPage]);

  useEffect(() => {
    let cancelled = false;

    async function loadStudents() {
      try {
        const response = await axios.get("/api/impact/leaderboard/students", {
          params: { page: studentPage, limit: PAGE_SIZE }
        });

        if (!cancelled) {
          setStudentLeaderboard({
            items: response.data?.items || [],
            total: response.data?.total || 0
          });
        }
      } catch {
        if (!cancelled) {
          setStudentLeaderboard({
            items: paginate(STUDENT_LEADERBOARD, studentPage),
            total: STUDENT_LEADERBOARD.length
          });
        }
      }
    }

    loadStudents();

    return () => {
      cancelled = true;
    };
  }, [studentPage]);

  return (
    <div className="impact-shell">
      <header className="feed-header">
        <div>
          <h1>GreenLoop Impact</h1>
          <p className="feed-subtitle">
            {formatUserType(impact.userType)} impact for {user?.displayName || user?.email}
          </p>
        </div>
        <div className="feed-header-right">
          <nav className="top-tabs" aria-label="Main navigation">
            <NavLink to="/" end className={({ isActive }) => `top-tab${isActive ? " active" : ""}`}>
              Feed
            </NavLink>
            <NavLink to="/impact" className={({ isActive }) => `top-tab${isActive ? " active" : ""}`}>
              Impact
            </NavLink>
          </nav>
          <Link className="feed-donate-button" to="/">
            Donate Food
          </Link>
          <button className="feed-logout-button" type="button" onClick={() => logout()}>
            Logout
          </button>
        </div>
      </header>

      <main className="impact-content">
        {dataNote ? <p className="impact-note">{dataNote}</p> : null}

        <div className="impact-layout">
          <div className="impact-left-column">
            <section className="impact-hero">
              <div>
                <span className="eyebrow">My Impact</span>
                <h2>Your food loop is working</h2>
              </div>
            </section>

            <section className="impact-metrics" aria-label="My impact metrics">
              {visibleMetrics.map((metric) => (
                <article className="impact-metric" key={metric.label}>
                  <span>{metric.label}</span>
                  <strong>{metric.value}</strong>
                </article>
              ))}
              <article className="impact-metric">
                <span>Badges earned</span>
                <strong>{impact.badges?.length || 0}</strong>
              </article>
            </section>

            <section className="impact-panel">
              <div className="section-header">
                <div>
                  <span className="eyebrow">My Badges</span>
                  <h2>Recognition</h2>
                </div>
              </div>

              <div className="badge-grid">
                {badges.map((badge) => (
                  <article className={`badge-card${badge.earned ? " earned" : ""}`} key={badge.id}>
                    <div className="badge-card-top">
                      {badge.earned ? null : <div className="badge-mark">Locked</div>}
                      <button
                        type="button"
                        className="badge-info-button"
                        aria-label={`${badge.name}: ${badge.description}`}
                      >
                        i
                        <span className="badge-tooltip" role="tooltip">
                          {badge.description}
                        </span>
                      </button>
                    </div>
                    <h3>{badge.name}</h3>
                    {badge.earnedAt ? (
                      <span>Earned {new Date(badge.earnedAt).toLocaleDateString()}</span>
                    ) : null}
                  </article>
                ))}
              </div>
            </section>
          </div>

          <aside className="impact-right-column" aria-label="Leaderboards">
            <LeaderboardTable
              title="Stores + Diners"
              subtitle="Ranked by donation impact and CO2 saved."
              items={donorLeaderboard.items}
              page={donorPage}
              total={donorLeaderboard.total}
              onPageChange={setDonorPage}
            />

            <LeaderboardTable
              title="Students"
              subtitle="Ranked by total impact from receiving and donating food."
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
