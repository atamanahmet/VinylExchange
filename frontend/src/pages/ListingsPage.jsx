import { useEffect, useMemo } from "react";
import React from "react";
import "../App.css";
import ListingItem from "@/components/listing/ListingItem";
import SkeletonListingItem from "@/components/listing/SkeletonListingItem";
import { useListingStore } from "../stores/listingStore";
import { useNavigate } from "react-router-dom";
import { mapListingsToCardItems } from "../adapters/mapListingToCardItems";
import { useAuthStore } from "../stores/authStore";

export default function ListingsPage() {
  const navigate = useNavigate();

  const user = useAuthStore((state) => state.user);

  const myListings = useListingStore((state) => state.myListings);
  const isFetchingMine = useListingStore((state) => state.isFetchingMine);

  const fetchMyActiveListings = useListingStore(
    (state) => state.fetchMyActiveListings,
  );

  const deleteListing = useListingStore((state) => state.deleteListing);

  useEffect(() => {
    fetchMyActiveListings();
  }, [fetchMyActiveListings]);

  const myListingCards = useMemo(() => {
    return mapListingsToCardItems(myListings.items, {
      user,
      navigate,
      onDelete: deleteListing,
    });
  }, [myListings.items, user, navigate, deleteListing]);

  return (
    <>
      <div className="min-h-screen max-w-7xl mx-auto min-w-300 bg-surface-base text-on-surface">
        <main className="max-w-6xl mx-auto px-4 sm:px-6 lg:px-8 pt-4">
          <div className="">
            <h2 className="text-3xl font-semibold mb-5">My listings</h2>
          </div>
          <div className="bg-neutral-primary-soft border-b  border-default grid grid-cols-7 items-center">
            <p>Cover</p>
            <p>Title</p>
            <p>Release Date</p>
            <p>Format</p>
            <p>Price</p>
            <p>Created At</p>
          </div>
          <div className="mt-6">
            {isFetchingMine ? (
              Array(5)
                .fill(0)
                .map((_, i) => <SkeletonListingItem key={i} />)
            ) : myListingCards.length === 0 ? (
              <p className="text-on-surface-muted">
                You have no active listings.
              </p>
            ) : (
              myListingCards.map((item) => (
                <ListingItem key={item.id} item={item} />
              ))
            )}
          </div>
        </main>
      </div>
    </>
  );
}
