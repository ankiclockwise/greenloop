import { useMemo, useState } from "react";
import { CATEGORY_OPTIONS, TAG_OPTIONS } from "../../data/mockListings";

const EMPTY_FORM = {
  name: "",
  category: CATEGORY_OPTIONS[0],
  quantity: "1",
  price: "",
  pickupWindowStart: "",
  pickupWindowEnd: "",
  pickupLocation: "",
  pickupNotes: "",
  description: "",
  tags: []
};

function validateListing(values) {
  const errors = {};

  if (!values.name.trim()) {
    errors.name = "Listing name is required.";
  }

  if (!values.category) {
    errors.category = "Choose a category.";
  }

  if (!values.quantity || Number(values.quantity) < 1) {
    errors.quantity = "Quantity must be at least 1.";
  }

  if (values.price !== "" && Number(values.price) < 0) {
    errors.price = "Price cannot be negative.";
  }

  if (!values.pickupWindowStart) {
    errors.pickupWindowStart = "Pickup start time is required.";
  }

  if (!values.pickupWindowEnd) {
    errors.pickupWindowEnd = "Pickup end time is required.";
  }

  if (
    values.pickupWindowStart &&
    values.pickupWindowEnd &&
    new Date(values.pickupWindowEnd) <= new Date(values.pickupWindowStart)
  ) {
    errors.pickupWindowEnd = "Pickup end must be after pickup start.";
  }

  if (!values.pickupLocation.trim()) {
    errors.pickupLocation = "Pickup location is required.";
  }

  return errors;
}

export function ListingComposer({ onSubmit }) {
  const [formValues, setFormValues] = useState(EMPTY_FORM);
  const [errors, setErrors] = useState({});
  const [submitState, setSubmitState] = useState("idle");

  const tagSummary = useMemo(() => {
    if (formValues.tags.length === 0) {
      return "Add tags to help people filter and find this listing.";
    }

    return `${formValues.tags.length} tag${formValues.tags.length > 1 ? "s" : ""} selected`;
  }, [formValues.tags]);

  function setField(key, value) {
    setFormValues((current) => ({ ...current, [key]: value }));
    setErrors((current) => ({ ...current, [key]: "" }));
  }

  function toggleTag(tag) {
    setFormValues((current) => ({
      ...current,
      tags: current.tags.includes(tag)
        ? current.tags.filter((value) => value !== tag)
        : [...current.tags, tag]
    }));
  }

  function handleSubmit(event) {
    event.preventDefault();
    const nextErrors = validateListing(formValues);
    setErrors(nextErrors);

    if (Object.keys(nextErrors).length > 0) {
      setSubmitState("error");
      return;
    }

    onSubmit(formValues);
    setFormValues(EMPTY_FORM);
    setErrors({});
    setSubmitState("success");
  }

  return (
    <section className="composer-card">
      <form className="composer-form" onSubmit={handleSubmit}>
        <label>
          Listing name
          <input
            type="text"
            value={formValues.name}
            onChange={(event) => setField("name", event.target.value)}
            placeholder="Fresh fruit box"
          />
          {errors.name ? <span className="field-error">{errors.name}</span> : null}
        </label>

        <label>
          Category
          <select
            value={formValues.category}
            onChange={(event) => setField("category", event.target.value)}
          >
            {CATEGORY_OPTIONS.map((option) => (
              <option key={option} value={option}>
                {option}
              </option>
            ))}
          </select>
          {errors.category ? <span className="field-error">{errors.category}</span> : null}
        </label>

        <div className="composer-grid">
          <label>
            Quantity
            <input
              type="number"
              min="1"
              value={formValues.quantity}
              onChange={(event) => setField("quantity", event.target.value)}
            />
            {errors.quantity ? <span className="field-error">{errors.quantity}</span> : null}
          </label>

          <label>
            Price
            <input
              type="number"
              min="0"
              step="0.01"
              value={formValues.price}
              onChange={(event) => setField("price", event.target.value)}
              placeholder="0.00"
            />
            {errors.price ? <span className="field-error">{errors.price}</span> : null}
          </label>
        </div>

        <div className="composer-grid">
          <label>
            Pickup window start
            <input
              type="datetime-local"
              value={formValues.pickupWindowStart}
              onChange={(event) => setField("pickupWindowStart", event.target.value)}
            />
            {errors.pickupWindowStart ? (
              <span className="field-error">{errors.pickupWindowStart}</span>
            ) : null}
          </label>

          <label>
            Pickup window end
            <input
              type="datetime-local"
              value={formValues.pickupWindowEnd}
              onChange={(event) => setField("pickupWindowEnd", event.target.value)}
            />
            {errors.pickupWindowEnd ? (
              <span className="field-error">{errors.pickupWindowEnd}</span>
            ) : null}
          </label>
        </div>

        <label>
          Pickup location
          <input
            type="text"
            value={formValues.pickupLocation}
            onChange={(event) => setField("pickupLocation", event.target.value)}
            placeholder="Student Union North Entrance"
          />
          {errors.pickupLocation ? (
            <span className="field-error">{errors.pickupLocation}</span>
          ) : null}
        </label>

        <label>
          Pickup notes
          <textarea
            rows="3"
            value={formValues.pickupNotes}
            onChange={(event) => setField("pickupNotes", event.target.value)}
            placeholder="Ask for the GreenLoop shelf at checkout."
          />
        </label>

        <label>
          Description
          <textarea
            rows="4"
            value={formValues.description}
            onChange={(event) => setField("description", event.target.value)}
            placeholder="Tell people what they are reserving and anything they should bring."
          />
        </label>

        <div className="tag-picker">
          <div className="tag-picker-header">
            <strong>Tags</strong>
            <span>{tagSummary}</span>
          </div>
          <div className="tag-picker-options">
            {TAG_OPTIONS.map((tag) => (
              <button
                key={tag}
                type="button"
                className={`filter-chip${formValues.tags.includes(tag) ? " active" : ""}`}
                onClick={() => toggleTag(tag)}
              >
                {tag}
              </button>
            ))}
          </div>
        </div>

        {submitState === "error" ? (
          <p className="form-message error">Please fix the highlighted fields before posting.</p>
        ) : null}
        {submitState === "success" ? (
          <p className="form-message success">Listing drafted and added to the feed.</p>
        ) : null}

        <button type="submit" className="auth-button">
          Post listing
        </button>
      </form>
    </section>
  );
}
