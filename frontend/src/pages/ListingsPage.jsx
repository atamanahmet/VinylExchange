import { useState, useEffect, useMemo } from "react";
import React from "react";
import "../App.css";
import { ThemeProvider } from "@material-tailwind/react";
import Card from "../comps/Card";
import axios from "axios";
import ListingItem from "../comps/ListingItem";
import { useDataStore } from "../stores/dataStore";
import { useNavigate } from "react-router-dom";
import { mapListingsToCardItems } from "../adapters/mapListingToCardItems";
import { useAuthStore } from "../stores/authStore";

export default function ListingsPage() {
  const navigate = useNavigate();

  const user = useAuthStore((state) => state.user);

  const data = useDataStore((state) => state.data);
  const isFetching = useDataStore((state) => state.isFetching);

  const fetchMyActiveListings = useDataStore(
    (state) => state.fetchMyActiveListings,
  );

  const fetchListingsByUser = useDataStore(
    (state) => state.fetchListingsByUser,
  );

  const deleteListing = useDataStore((state) => state.deleteListing);

  useEffect(() => {
    fetchMyActiveListings();
  }, []);

  const myListingCards = useMemo(() => {
    return mapListingsToCardItems(data.items, {
      user,
      navigate,
      onDelete: deleteListing,
    });
  }, [data, user]);

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
            {data.isFetching ? (
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
