export function wishlistItemToCardItem(item, isInWishlist, removeFromWishlist) {
  const inWishlist = isInWishlist(item);

  return {
    id: item.id,
    title: item.title,
    artist: item.artist || "Unknown artist",
    format: item.format || "Unknown format",
    externalCoverUrl: item.externalCoverUrl,
    barcode: item.barcode || "Unknown Barcode",
    country: item.country || "Unknown Country",
    year: item.year || "Unknown Date",
    label: item.label || "Unknown Label",

    primaryAction: {
      label: inWishlist ? "Remove" : "Add to Wishlist",

      onClick: async () => {
        const result = await removeFromWishlist(item.id);

        if (result) {
          console.log("Removed to wishlist:", item.title);
        } else {
          console.log("Failed to remove", item.title);
        }
      },
      //   isActive: inWishlist, // To show active state in UI
    },
  };
}
