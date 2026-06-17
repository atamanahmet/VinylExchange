const MAX_ENTRIES = 300;
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

/** True when browser already has this URL decoded (HTTP/memory cache hit). */
export function isImageCachedByBrowser(src) {
  const key = normalizeSrc(src);
  if (!key) {
    return false;
  }

  const probe = new Image();
  probe.src = key;
  return probe.complete && probe.naturalWidth > 0;
}
