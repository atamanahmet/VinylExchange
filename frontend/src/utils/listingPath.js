export function buildListingSlug(artist, title) {
  const slug = [artist, title]
    .filter(Boolean)
    .join("-")
    .toLowerCase()
    .replace(/[^a-z0-9]+/g, "-")
    .replace(/(^-|-$)/g, "");

  return slug || "listing";
}

export function buildListingPath(item) {
  const publicId = item.publicId ?? item.id;
  const slug = buildListingSlug(item.artist ?? item.artistName, item.title);
  return `/listing/${publicId}/${slug}`;
}
