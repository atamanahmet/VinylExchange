export function mbReleaseToCardItem(release, isInWishlist, toggleToWishlist) {
  const inWishlist = isInWishlist(release);

  return {
    id: release.id,
    title: release.title,
    artist: release.artistCredit?.[0]?.name || "Unknown artist",
    format: release.media?.[0]?.format || "Unknown format",
    barcode: release.barcode || "Unkown Barcode",
    externalCoverUrl: release.externalCoverUrl,
    year: release.year || "Unknown Date",
    label: release.labelInfo?.[0].label?.name || "Unknown Label",

    primaryAction: {
      label: inWishlist ? "Remove from Wishlist" : "Add to Wishlist",

      onClick: async () => {
        const result = await toggleToWishlist(release);

        if (result === null) {
          console.log("Failed to toggle wishlist for:", release.title);
        } else if (result === "added") {
          console.log("Added to wishlist:", release.title);
        } else if (result === "removed") {
          console.log("Removed from wishlist:", release.title);
        }
      },
      isActive: inWishlist, // To show active state in UI
    },
  };
}
