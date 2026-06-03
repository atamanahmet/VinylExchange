import dayjs from "dayjs";

export function listingToCardData(listing) {
  const formattedDate = dayjs(listing.createdAt).format("DD-MM-YYYY");

  return {
    id: listing.id,
    title: listing.title,
    artist: listing.artistName,
    year: listing.year,
    format: listing.format,
    label: listing.labelName,
    imageUrl: listing.imagePaths?.[0] || "/placeholder.png",
    price: listing.price,
    country: listing.country,
    discountedPrice: listing.discountedPrice,
    ownerUsername: listing.ownerUsername,
    createdAt: formattedDate,
  };
}
