import { useState } from "react";

export default function CardImage({ src, alt }) {
  const [loaded, setLoaded] = useState(false);
  const [error, setError] = useState(false);

  const isPlaceholder = src?.includes("/placeholders/");

  return (
    <div className="relative w-full aspect-square overflow-hidden rounded-md">
      {/* skeleton */}
      {!loaded && (
        <div className="absolute inset-0 bg-neutral-600 animate-pulse rounded-md" />
      )}

      {/* image */}
      {src && !error && (
        <img
          src={src}
          alt={alt || "cover"}
          className={`w-full h-full object-cover transition-opacity duration-300 ${
            loaded ? "opacity-100" : "opacity-0"
          }`}
          onLoad={() => setLoaded(true)}
          onError={() => {
            setError(true);
            setLoaded(true);
          }}
        />
      )}

      {/* placeholder badge */}
      {isPlaceholder && !error && (
        <div className="absolute top-6 -left-8.5 -rotate-45 bg-amber-500 text-black text-md font-semibold px-7 py-0.5 rounded shadow-lg tracking-wider">
          Placeholder
        </div>
      )}

      {/* error fallback */}
      {error && (
        <div className="w-full h-full bg-neutral-700 flex items-center justify-center rounded-md">
          <span className="text-white text-sm">No Image</span>
        </div>
      )}
    </div>
  );
}
