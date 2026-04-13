function buildPattern(value) {
  const seed = value
    .split("")
    .reduce((total, character, index) => total + character.charCodeAt(0) * (index + 1), 0);

  return Array.from({ length: 121 }, (_, index) => ((seed + index * 17) % 7) < 3);
}

export function QRCodeDisplay({ value }) {
  const pattern = buildPattern(value || "greenloop");

  return (
    <div className="qr-card">
      <div className="qr-grid" aria-label={`QR code for ${value}`}>
        {pattern.map((filled, index) => (
          <span
            key={`${value}-${index}`}
            className={`qr-cell${filled ? " filled" : ""}`}
          />
        ))}
      </div>
      <p>{value}</p>
    </div>
  );
}
