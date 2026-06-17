export function wishlistItemToCardItem(
  item,
  isInWishlist,
  removeFromWishlist,
  matchingListing,
  navigate,
) {
  const inWishlist = isInWishlist(item);
  const inStock = Boolean(matchingListing);

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
    disableLink: true,
    textCenter: true,
    stackActions: true,

    primaryAction: {
      label: "Remove from Wishlist",
      onClick: async () => {
        const result = await removeFromWishlist(item.id);

        if (result) {
          console.log("Removed from wishlist:", item.title);
        } else {
          console.log("Failed to remove", item.title);
        }
      },
      isActive: inWishlist,
    },

    secondaryAction: inStock
      ? {
          label: "In stock",
          onClick: () => navigate(`/listing/${matchingListing.id}`),
        }
      : {
          label: "Not in stock",
          disabled: true,
        },
  };
}
