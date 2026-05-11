import { ListingComposer } from "./ListingComposer";

export function DonateFoodModal({ onClose, onSubmit }) {
  return (
    <div className="confirmation-overlay" role="dialog" aria-modal="true">
      <div className="donate-modal-card">
        <div className="section-header">
          <div>
            <span className="eyebrow">Donate Food</span>
            <h2>Post a new listing</h2>
          </div>
          <button
            type="button"
            className="dismiss-button"
            onClick={onClose}
          >
            Close
          </button>
        </div>
        <ListingComposer onSubmit={onSubmit} />
      </div>
    </div>
  );
}
