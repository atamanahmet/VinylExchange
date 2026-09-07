/** In-memory session cache. Cleared when tab closes. */
const MAX_ENTRIES = 500;
const order = [];
const seen = new Set();

function normalizeSrc(src) {
  if (!src || typeof src !== "string") {
    return null;
  }

  return src.trim();
}

export function wasImageLoaded(src) {
  const key = normalizeSrc(src);
  return key ? seen.has(key) : false;
}

export function markImageLoaded(src) {
  const key = normalizeSrc(src);
  if (!key || seen.has(key)) {
    return;
  }

  seen.add(key);
  order.push(key);

  while (order.length > MAX_ENTRIES) {
    const oldest = order.shift();
    seen.delete(oldest);
  }
}

/** True when an img element already finished loading (covers browser cache hits). */
export function isImageElementReady(img) {
  return Boolean(img?.complete && img.naturalWidth > 0);
}
