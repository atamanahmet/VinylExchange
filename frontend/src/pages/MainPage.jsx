import { useState, useEffect, useMemo, useCallback } from "react";

import "../App.css";

import ListingBrowsePanel from "@/components/listing/ListingBrowsePanel";

import { useListingStore } from "../stores/listingStore";
import { useAuthStore } from "../stores/authStore";
import { useCartStore } from "../stores/cartStore";
import { useSearchStore } from "../stores/searchStore";

import { mapListingsToCardItems } from "../adapters/mapListingToCardItems";
import { useNavigate } from "react-router-dom";
import {
  buildListingFilterParams,
  DEFAULT_LISTING_SORT,
  resetFilters,
} from "../utils/listingFilters";

export default function MainPage() {
  const PAGE_SIZE = 20;

  const [page, setPage] = useState(0);
  const [sort, setSort] = useState(DEFAULT_LISTING_SORT);
  const [draftFilters, setDraftFilters] = useState(() => resetFilters());
  const [appliedFilters, setAppliedFilters] = useState(() => resetFilters());

  const navigate = useNavigate();

  const listingSearchResult = useSearchStore(
    (state) => state.listingSearchResult,
  );
  const lastListingQuery = useSearchStore((state) => state.lastListingQuery);
  const isLoadingListingSearch = useSearchStore(
    (state) => state.isLoadingListingSearch,
  );
  const searchProducts = useSearchStore((state) => state.searchProducts);

  const user = useAuthStore((state) => state.user);

  const isFetchingPublic = useListingStore((state) => state.isFetchingPublic);
  const fetchPublicListings = useListingStore(
    (state) => state.fetchPublicListings,
  );
  const publicListings = useListingStore((state) => state.publicListings);

  const cart = useCartStore((state) => state.cart);
  const addToCart = useCartStore((state) => state.addToCart);
  const removeFromCart = useCartStore((state) => state.removeFromCart);

  const isSearchActive = Boolean(lastListingQuery);

  useEffect(() => {
    if (isSearchActive) return;

    // sort alone → Redis top-60 per Sort; filters → DB
    const params = buildListingFilterParams(appliedFilters, {
      page,
      size: PAGE_SIZE,
      sort,
    });

    fetchPublicListings(params);
  }, [appliedFilters, page, sort, isSearchActive, fetchPublicListings]);

  useEffect(() => {
    if (!lastListingQuery) return;
    // text search IDs from FTS/OS; sort applied after hydrate (not Redis browse cache)
    searchProducts(lastListingQuery, { sort });
  }, [lastListingQuery, sort, searchProducts]);

  const handleSortChange = useCallback((nextSort) => {
    setSort(nextSort);
    setPage(0);
  }, []);

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

  const listingItems = useMemo(() => {
    if (isSearchActive) {
      return listingSearchResult.items;
    }
    return publicListings.items ?? [];
  }, [isSearchActive, listingSearchResult.items, publicListings.items]);

  const pagination = isSearchActive ? null : publicListings.pagination;

  const cartItemByListingId = useMemo(() => {
    const map = new Map();
    cart?.items?.forEach((item) => {
      map.set(String(item.publicId), item.id);
    });
    return map;
  }, [cart?.items]);

  const items = useMemo(() => {
    if (!listingItems?.length) return [];

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
    (isFetchingPublic || isLoadingListingSearch) && listingItems.length === 0;

  return (
    <ListingBrowsePanel
      items={items}
      pagination={pagination}
      page={page}
      onPageChange={setPage}
      isLoading={isLoading}
      isFetching={isFetchingPublic || isLoadingListingSearch}
      draftFilters={draftFilters}
      onDraftFiltersChange={setDraftFilters}
      onApplyFilters={handleApplyFilters}
      onResetFilters={handleResetFilters}
      sort={sort}
      onSortChange={handleSortChange}
      showSort
      showPagination={!isSearchActive}
    />
  );
}
