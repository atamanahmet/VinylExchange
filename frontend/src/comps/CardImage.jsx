import { useLayoutEffect, useState } from "react";

import {
  isImageCachedByBrowser,
  markImageLoaded,
  wasImageLoaded,
} from "@/utils/imageLoadCache";

function getInitialLoadedState(src) {
  if (!src) {
    return false;
  }

  if (isImageCachedByBrowser(src) || wasImageLoaded(src)) {
    return true;
  }

  return false;
}

export default function CardImage({ src, alt }) {
  const [loaded, setLoaded] = useState(() => getInitialLoadedState(src));
  const [error, setError] = useState(false);

  const isPlaceholder = src?.includes("/placeholders/");

  useLayoutEffect(() => {
    if (!src) {
      setLoaded(false);
      setError(false);
      return;
    }

    setError(false);

    if (isImageCachedByBrowser(src) || wasImageLoaded(src)) {
      setLoaded(true);
      return;
    }

    setLoaded(false);
  }, [src]);

  function handleLoad() {
    markImageLoaded(src);
    setLoaded(true);
  }

  function handleError() {
    setError(true);
    setLoaded(true);
  }

  return (
    <div className="relative aspect-square w-full overflow-hidden rounded-md transition-[width,height] duration-100 ease-in-out">
      {!loaded && !error && (
        <div className="absolute inset-0 animate-pulse rounded-md bg-surface-3" />
      )}

      {src && !error && (
        <img
          src={src}
          alt={alt || "cover"}
          loading="lazy"
          decoding="async"
          className={`h-full w-full object-cover transition-[opacity,width,height] duration-100 ease-in-out ${
            loaded ? "opacity-100" : "opacity-0"
          }`}
          onLoad={handleLoad}
          onError={handleError}
        />
      )}

      {isPlaceholder && !error && loaded && (
        <div className="absolute top-6 -left-8.5 -rotate-45 rounded bg-accent-dim px-7 py-0.5 text-md font-semibold tracking-wider text-surface-base shadow-lg">
          Placeholder
        </div>
      )}

      {error && (
        <div className="flex h-full w-full items-center justify-center rounded-md bg-surface-2">
          <span className="text-sm text-on-surface-muted">No Image</span>
        </div>
      )}
    </div>
  );
}
