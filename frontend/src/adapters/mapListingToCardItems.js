import { attachCardActions } from "./attachCardActions";
import { listingToCardData } from "./listingToCardData";

export function mapListingsToCardItems(listings, context) {
  return listings.map((listing) =>
    attachCardActions(listingToCardData(listing), context),
  );
}
