export function mbReleaseToWishlistItem(
  item,
  isInWishlistHolder,
  addToWishlistHolder,
) {
  const inWishlist = isInWishlistHolder(item);

  return {
    id: item.id,
    title: item.title,
    artist: item.artistCredit?.[0]?.name || "Unknown artist",
    format: item.media?.[0]?.format || "Unknown format",
    externalCoverUrl: item.externalCoverUrl,
    barcode: item.barcode || "Unknown Barcode",
    country: item.country || "Unknown Country",
    year: item.year || "Unknown Date",
    label: item.labelInfo?.[0].label?.name || "Unknown Label",

    primaryAction: {
      label: inWishlist ? "Added" : "Add to Wishlist",

      onClick: () => {
        addToWishlistHolder(item);
      },
      //   isActive: inWishlist, // To show active state in UI
    },
  };
}
