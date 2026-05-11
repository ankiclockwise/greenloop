import {
  IMPACT_COPY,
  LEADERBOARD_COLUMNS,
  UNITS
} from "../../constants/impactConstants";
import { Pagination } from "./Pagination";

export function LeaderboardTable({ title, items, page, total, onPageChange, student = false }) {
  return (
    <section className="impact-panel">
      <div className="section-header">
        <div>
          <span className="eyebrow">{IMPACT_COPY.LEADERBOARD_EYEBROW}</span>
          <h2>{title}</h2>
        </div>
      </div>

      <div className="impact-table-wrap">
        <table className="impact-table">
          <thead>
            <tr>
              <th>{LEADERBOARD_COLUMNS.RANK}</th>
              <th>{LEADERBOARD_COLUMNS.NAME}</th>
              {student ? <th>{LEADERBOARD_COLUMNS.RECEIVED}</th> : null}
              <th>{LEADERBOARD_COLUMNS.DONATED}</th>
              <th>{LEADERBOARD_COLUMNS.CO2_SAVED}</th>
            </tr>
          </thead>
          <tbody>
            {items.map((item) => (
              <tr key={item.userId}>
                <td>#{item.rank}</td>
                <td>{item.name}</td>
                {student ? <td>{item.foodReceived}</td> : null}
                <td>{item.foodDonated}</td>
                <td>
                  {item.co2SavedKg} {UNITS.KG}
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>

      <Pagination page={page} total={total} onChange={onPageChange} />
    </section>
  );
}
