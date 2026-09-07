import { useLayoutEffect, useRef, useState } from "react";

import { markImageLoaded, wasImageLoaded } from "@/utils/imageLoadCache";

export default function CardImage({ src, alt }) {
  const imgRef = useRef(null);
  const [ready, setReady] = useState(() => Boolean(src && wasImageLoaded(src)));
  const [error, setError] = useState(false);

  const isPlaceholder = src?.includes("/placeholders/");

  function setImageReady() {
    if (!src) {
      return;
    }
    markImageLoaded(src);
    setReady(true);
  }

  function checkAlreadyLoaded() {
    const img = imgRef.current;
    if (img?.complete && img.naturalWidth > 0) {
      setImageReady();
    }
  }

  useLayoutEffect(() => {
    if (!src) {
      setReady(false);
      setError(false);
      return;
    }

    setError(false);

    if (wasImageLoaded(src)) {
      setReady(true);
      return;
    }

    setReady(false);
    checkAlreadyLoaded();
    const frameId = requestAnimationFrame(checkAlreadyLoaded);
    return () => cancelAnimationFrame(frameId);
  }, [src]);

  function handleRef(node) {
    imgRef.current = node;
    if (node) {
      checkAlreadyLoaded();
    }
  }

  function handleLoad() {
    setImageReady();
  }

  function handleError() {
    setError(true);
    setReady(true);
  }

  return (
    <div className="relative aspect-square w-full overflow-hidden rounded-md bg-surface-2">
      {src && !error && (
        <img
          ref={handleRef}
          src={src}
          alt={alt || "cover"}
          loading="eager"
          className="h-full w-full object-cover"
          onLoad={handleLoad}
          onError={handleError}
        />
      )}

      {!ready && !error && (
        <div
          className="absolute inset-0 z-10 animate-pulse bg-surface-3"
          aria-hidden="true"
        />
      )}

      {isPlaceholder && !error && ready && (
        <div className="absolute top-6 -left-8.5 z-20 -rotate-45 rounded bg-accent-dim px-7 py-0.5 text-md font-semibold tracking-wider text-surface-base shadow-lg">
          Not seller photo
        </div>
      )}

      {error && (
        <div className="flex h-full w-full items-center justify-center bg-surface-2">
          <span className="text-sm text-on-surface-muted">No Image</span>
        </div>
      )}
    </div>
  );
}
