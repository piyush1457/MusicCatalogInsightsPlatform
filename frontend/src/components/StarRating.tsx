interface Props {
  value: number | null;
  onChange: (rating: number) => void;
}

export default function StarRating({ value, onChange }: Props) {
  return (
    <span className="star-rating">
      {[1, 2, 3, 4, 5].map((star) => (
        <button
          key={star}
          type="button"
          className={`star ${star <= (value ?? 0) ? 'filled' : ''}`}
          onClick={() => onChange(star)}
          aria-label={`${star} star`}
        >
          {star <= (value ?? 0) ? '\u2605' : '\u2606'}
        </button>
      ))}
    </span>
  );
}
