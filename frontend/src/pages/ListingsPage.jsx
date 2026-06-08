import { useState, useEffect, useMemo } from "react";
import React from "react";
import "../App.css";
import { ThemeProvider } from "@material-tailwind/react";
import Card from "../comps/Card";
import axios from "axios";
import ListingItem from "../comps/ListingItem";
import { useListingStore } from "../stores/listingStore";
import { useNavigate } from "react-router-dom";
import { mapListingsToCardItems } from "../adapters/mapListingToCardItems";
import { useAuthStore } from "../stores/authStore";

export default function ListingsPage() {
  const navigate = useNavigate();

  const user = useAuthStore((state) => state.user);

  const publicListings = useListingStore((state) => state.publicListings);
  const isFetching = useListingStore((state) => state.isFetching);

  const fetchMyActiveListings = useListingStore(
    (state) => state.fetchMyActiveListings,
  );

  const fetchListingsByUser = useListingStore(
    (state) => state.fetchListingsByUser,
  );

  const deleteListing = useListingStore((state) => state.deleteListing);

  useEffect(() => {
    fetchMyActiveListings();
  }, []);

  const myListingCards = useMemo(() => {
    return mapListingsToCardItems(publicListings.items, {
      user,
      navigate,
      onDelete: deleteListing,
    });
  }, [publicListings, user]);

  return (
    <>
      <div className="min-h-screen max-w-7xl mx-auto min-w-300 bg-black text-white">
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
            {publicListings.isFetching ? (
              Array(5)
                .fill(0)
                .map((_, i) => <SkeletonListingItem key={i} />)
            ) : myListingCards.length === 0 ? (
              <p className="text-gray-400">You have no active listings.</p>
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
