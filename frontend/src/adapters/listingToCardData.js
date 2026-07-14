import dayjs from "dayjs";

import {
  getListingFormatLabel,
  getListingLabelName,
} from "../utils/mediaInfo";

export function listingToCardData(listing) {
  const formattedDate = dayjs(listing.createdAt).format("DD-MM-YYYY");

  return {
    id: listing.publicId,
    publicId: listing.publicId,
    title: listing.title,
    artist: listing.artistName,
    year: listing.year,
    format: getListingFormatLabel(listing),
    label: getListingLabelName(listing),
    imageUrl: listing.imageUrl || listing.imagePaths?.[0] || listing.mainImageUrl || "/placeholder.png",
    price: listing.price,
    country: listing.country,
    discountedPrice: listing.discountedPrice,
    ownerUsername: listing.ownerUsername,
    createdAt: formattedDate,
  };
}
