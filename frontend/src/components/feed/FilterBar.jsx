import { CATEGORY_OPTIONS, TAG_OPTIONS } from "../../data/mockListings";

const ALLERGENS = ["Nuts", "Dairy", "Eggs", "Soy", "Wheat"];
const PRICE_OPTIONS = [
  { value: "free", label: "Free only" },
  { value: "under3", label: "Under $3" },
  { value: "3to10", label: "$3 – $10" },
  { value: "", label: "Any price" },
];

export function FilterBar({ filters, onChange }) {
  function toggleMulti(key, value) {
    const current = filters[key];
    const next = current.includes(value)
      ? current.filter((v) => v !== value)
      : [...current, value];
    onChange({ ...filters, [key]: next });
  }

  function set(key, value) {
    onChange({ ...filters, [key]: value });
  }

  function clearAll() {
    onChange({ category: "", tags: [], dietary: [], allergens: [], priceRange: "", radius: 5 });
  }

  const activeCount =
    (filters.category ? 1 : 0) +
    filters.tags.length +
    filters.dietary.length +
    filters.allergens.length +
    (filters.priceRange ? 1 : 0) +
    (filters.radius !== 5 ? 1 : 0);

  return (
    <div className="filter-bar">
      {/* Row 1: Category + Price + Radius */}
      <div className="filter-row">
        <span className="filter-label">Category</span>
        <select
          className="filter-select"
          value={filters.category}
          onChange={(e) => set("category", e.target.value)}
        >
          <option value="">All</option>
          {CATEGORY_OPTIONS.map((c) => (
            <option key={c} value={c}>{c}</option>
          ))}
        </select>

        <span className="filter-label">Price</span>
        {PRICE_OPTIONS.map((opt) => (
          <button
            key={opt.value}
            type="button"
            className={`filter-chip${filters.priceRange === opt.value ? " active" : ""}`}
            onClick={() => set("priceRange", opt.value)}
          >
            {opt.label}
          </button>
        ))}

        <div className="filter-slider-wrap">
          <span className="filter-label">Radius</span>
          <input
            type="range"
            min={1}
            max={20}
            value={filters.radius}
            onChange={(e) => set("radius", Number(e.target.value))}
          />
          <span className="filter-slider-label">{filters.radius} mi</span>
        </div>
      </div>

      <div className="filter-row">
        <span className="filter-label">Tags</span>
        {TAG_OPTIONS.map((tag) => (
          <button
            key={tag}
            type="button"
            className={`filter-chip${filters.tags.includes(tag) ? " active" : ""}`}
            onClick={() => toggleMulti("tags", tag)}
          >
            {tag}
          </button>
        ))}
      </div>

      {/* Row 2: Dietary */}
      <div className="filter-row">
        <span className="filter-label">Dietary</span>
        {TAG_OPTIONS.slice(0, 5).map((tag) => (
          <button
            key={tag}
            type="button"
            className={`filter-chip${filters.dietary.includes(tag) ? " active" : ""}`}
            onClick={() => toggleMulti("dietary", tag)}
          >
            {tag}
          </button>
        ))}
      </div>

      {/* Row 3: Allergens */}
      <div className="filter-row">
        <span className="filter-label">Avoid</span>
        {ALLERGENS.map((tag) => (
          <button
            key={tag}
            type="button"
            className={`filter-chip allergen${filters.allergens.includes(tag) ? " active" : ""}`}
            onClick={() => toggleMulti("allergens", tag)}
          >
            {tag}
          </button>
        ))}
      </div>

      {/* Active filter chips */}
      {activeCount > 0 && (
        <div className="active-filters">
          {filters.category && (
            <span className="active-chip">
              {filters.category}
              <button type="button" onClick={() => set("category", "")}>✕</button>
            </span>
          )}
          {filters.tags.map((tag) => (
            <span key={tag} className="active-chip">
              {tag}
              <button type="button" onClick={() => toggleMulti("tags", tag)}>✕</button>
            </span>
          ))}
          {filters.dietary.map((tag) => (
            <span key={tag} className="active-chip">
              {tag}
              <button type="button" onClick={() => toggleMulti("dietary", tag)}>✕</button>
            </span>
          ))}
          {filters.allergens.map((tag) => (
            <span key={tag} className="active-chip">
              No {tag}
              <button type="button" onClick={() => toggleMulti("allergens", tag)}>✕</button>
            </span>
          ))}
          {filters.priceRange && (
            <span className="active-chip">
              {PRICE_OPTIONS.find((o) => o.value === filters.priceRange)?.label}
              <button type="button" onClick={() => set("priceRange", "")}>✕</button>
            </span>
          )}
          {filters.radius !== 5 && (
            <span className="active-chip">
              {filters.radius} mi
              <button type="button" onClick={() => set("radius", 5)}>✕</button>
            </span>
          )}
          <button type="button" className="clear-filters" onClick={clearAll}>
            Clear all
          </button>
        </div>
      )}
    </div>
  );
}
