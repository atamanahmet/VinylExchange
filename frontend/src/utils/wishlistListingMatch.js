export function findMatchingListing(wishlistItem, listings = []) {
  if (!wishlistItem?.title || !wishlistItem?.artist) {
    return null;
  }

  const title = wishlistItem.title.toLowerCase().trim();
  const artist = wishlistItem.artist.toLowerCase().trim();

  return (
    listings.find((listing) => {
      if ((listing.stockQuantity ?? 0) <= 0) {
        return false;
      }

      if (listing.status && listing.status !== "AVAILABLE") {
        return false;
      }

      return (
        listing.title?.toLowerCase().trim() === title &&
        listing.artistName?.toLowerCase().trim() === artist
      );
    }) ?? null
  );
}
