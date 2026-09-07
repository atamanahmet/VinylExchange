import { useCallback, useEffect, useMemo, useState } from "react";
import { useNavigate, useParams } from "react-router-dom";
import axios from "../api/axiosInstance";

import ListingBrowsePanel from "@/components/listing/ListingBrowsePanel";
import { useAuthStore } from "../stores/authStore";
import { useCartStore } from "../stores/cartStore";
import { useListingStore } from "../stores/listingStore";
import { mapListingsToCardItems } from "../adapters/mapListingToCardItems";
import {
  buildListingFilterParams,
  resetFilters,
} from "../utils/listingFilters";

export default function SellerProfilePage() {
  const PAGE_SIZE = 20;
  const { username } = useParams();
  const navigate = useNavigate();

  const [publicId, setPublicId] = useState(null);
  const [profileError, setProfileError] = useState(null);
  const [profileLoading, setProfileLoading] = useState(true);
  const [page, setPage] = useState(0);
  const [draftFilters, setDraftFilters] = useState(() => resetFilters());
  const [appliedFilters, setAppliedFilters] = useState(() => resetFilters());

  const user = useAuthStore((state) => state.user);
  const cart = useCartStore((state) => state.cart);
  const addToCart = useCartStore((state) => state.addToCart);
  const removeFromCart = useCartStore((state) => state.removeFromCart);

  const sellerListings = useListingStore((state) => state.sellerListings);
  const isFetchingSeller = useListingStore((state) => state.isFetchingSeller);
  const fetchSellerListings = useListingStore(
    (state) => state.fetchSellerListings,
  );

  useEffect(() => {
    if (!username) return;

    let cancelled = false;

    async function loadProfile() {
      setProfileLoading(true);
      setProfileError(null);
      try {
        const res = await axios.get(
          `/api/users/by-username/${encodeURIComponent(username)}`,
        );
        if (!cancelled) {
          setPublicId(res.data.publicId);
        }
      } catch {
        if (!cancelled) {
          setProfileError("Seller not found.");
          setPublicId(null);
        }
      } finally {
        if (!cancelled) {
          setProfileLoading(false);
        }
      }
    }

    loadProfile();
    return () => {
      cancelled = true;
    };
  }, [username]);

  useEffect(() => {
    if (!username || profileError) return;

    const params = buildListingFilterParams(appliedFilters, {
      page,
      size: PAGE_SIZE,
    });
    fetchSellerListings(username, params);
  }, [
    username,
    profileError,
    appliedFilters,
    page,
    fetchSellerListings,
  ]);

  const handleApplyFilters = useCallback(() => {
    setAppliedFilters({
      ...draftFilters,
      priceRange: [...draftFilters.priceRange],
      yearRange: [...draftFilters.yearRange],
      formats: [...draftFilters.formats],
      speedRpm: [...(draftFilters.speedRpm ?? [])],
      vinylSubtype: [...(draftFilters.vinylSubtype ?? [])],
      conditions: [...draftFilters.conditions],
      countries: [...(draftFilters.countries ?? [])],
      genreIds: [...(draftFilters.genreIds ?? [])],
    });
    setPage(0);
  }, [draftFilters]);

  const handleResetFilters = useCallback(() => {
    const defaults = resetFilters();
    setDraftFilters(defaults);
    setAppliedFilters(defaults);
    setPage(0);
  }, []);

  const listingItems = sellerListings.items ?? [];

  const cartItemByListingId = useMemo(() => {
    const map = new Map();
    cart?.items?.forEach((item) => {
      map.set(String(item.publicId), item.id);
    });
    return map;
  }, [cart?.items]);

  const items = useMemo(() => {
    if (!listingItems.length) return [];

    return mapListingsToCardItems(listingItems, {
      user,
      cartItemByListingId,
      addToCart,
      removeFromCart,
      navigate,
    });
  }, [
    listingItems,
    cartItemByListingId,
    user,
    addToCart,
    removeFromCart,
    navigate,
  ]);

  const isLoading =
    (profileLoading || isFetchingSeller) && listingItems.length === 0;

  if (profileError) {
    return (
      <div className="min-h-screen bg-surface-base px-4 py-16 text-center">
        <p className="text-sm text-on-surface-muted">{profileError}</p>
      </div>
    );
  }

  return (
    <ListingBrowsePanel
      header={
        <div className="mb-6">
          <h1 className="text-2xl font-semibold text-on-surface">@{username}</h1>
          <p className="mt-1 text-sm text-on-surface-muted">Seller listings</p>
        </div>
      }
      items={items}
      pagination={sellerListings.pagination}
      page={page}
      onPageChange={setPage}
      isLoading={isLoading}
      isFetching={isFetchingSeller}
      draftFilters={draftFilters}
      onDraftFiltersChange={setDraftFilters}
      onApplyFilters={handleApplyFilters}
      onResetFilters={handleResetFilters}
      emptyHint="This seller has no active listings matching your filters."
    />
  );
}
