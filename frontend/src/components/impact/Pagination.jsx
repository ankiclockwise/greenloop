import {
  IMPACT_COPY,
  PAGE_SIZE,
  PAGE_STEP
} from "../../constants/impactConstants";

export function Pagination({ page, total, onChange }) {
  const totalPages = Math.max(1, Math.ceil(total / PAGE_SIZE));

  return (
    <div className="impact-pagination">
      <button type="button" onClick={() => onChange(page - PAGE_STEP)} disabled={page === PAGE_STEP}>
        {IMPACT_COPY.PREVIOUS_PAGE}
      </button>
      <span>
        {IMPACT_COPY.PAGE_LABEL} {page} {IMPACT_COPY.PAGE_OF_LABEL} {totalPages}
      </span>
      <button type="button" onClick={() => onChange(page + PAGE_STEP)} disabled={page === totalPages}>
        {IMPACT_COPY.NEXT_PAGE}
      </button>
    </div>
  );
}
