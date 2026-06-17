import { useState, useEffect, useMemo } from "react";
import "../App.css";

import Card from "../comps/Card";
import FilterSidebar, { MobileFilterSheet } from "../comps/FilterSidebar";
import ListView from "../comps/ListView";
import SkeletonCardView from "../comps/Skeletons/SkeletonCardView";
import SkeletonListView from "../comps/Skeletons/SkeletonListView";

import { useListingStore } from "../stores/listingStore";
import { useAuthStore } from "../stores/authStore";
import { useUIStore } from "../stores/uiStore";
import { useCartStore } from "../stores/cartStore";
import { useMessagingStore } from "../stores/messagingStore";
import useWishlistStore from "../stores/wishlistStore";
import { useSearchStore } from "../stores/searchStore";

import { mbReleaseToCardItem } from "../adapters/mbReleaseToCardItem";
import { mapListingsToCardItems } from "../adapters/mapListingToCardItems";
import { LayoutGrid, List } from "lucide-react";
import { useNavigate } from "react-router-dom";
import { cn } from "@/lib/utils";
import {
  applyListingFilters,
  countActiveFilters,
  createInitialFilters,
  getListingBounds,
} from "../utils/listingFilters";

function useMinWidth(minWidth) {
  const [matches, setMatches] = useState(() =>
    typeof window !== "undefined"
      ? window.matchMedia(`(min-width: ${minWidth}px)`).matches
      : false,
  );

  useEffect(() => {
    const media = window.matchMedia(`(min-width: ${minWidth}px)`);
    const onChange = (event) => setMatches(event.matches);

    setMatches(media.matches);
    media.addEventListener("change", onChange);
    return () => media.removeEventListener("change", onChange);
  }, [minWidth]);

  return matches;
}

export default function MainPage() {
  const PAGE_SIZE = 20;
  const [page, setPage] = useState(1);
  const [visibleItems, setVisibleItems] = useState([]);
  const [filters, setFilters] = useState(createInitialFilters());
  const [sidebarCollapsed, setSidebarCollapsed] = useState(true);
  const isLargeScreen = useMinWidth(1024);

  const navigate = useNavigate();

  const searchResult = useSearchStore((state) => state.searchResult);

  const user = useAuthStore((state) => state.user);

  const isInWishlist = useWishlistStore((state) => state.isInWishlist);
  const toggleToWishlist = useWishlistStore((state) => state.toggleToWishlist);

  const isFetchingPublic = useListingStore((state) => state.isFetchingPublic);
  const fetchPublicListings = useListingStore(
    (state) => state.fetchPublicListings,
  );
  const publicListings = useListingStore((state) => state.publicListings);

  const layout = useUIStore((state) => state.layout);
  const setLayout = useUIStore((state) => state.setLayout);

  const cart = useCartStore((state) => state.cart);
  const addToCart = useCartStore((state) => state.addToCart);
  const removeFromCart = useCartStore((state) => state.removeFromCart);

  const startConversation = useMessagingStore(
    (state) => state.startConversation,
  );

  useEffect(() => {
    if (!searchResult.items?.length) {
      fetchPublicListings();
    }
  }, []);

  const currentData = useMemo(() => {
    if (searchResult.items?.length > 0) return searchResult;
    return { dataType: "listing", items: publicListings.items };
  }, [searchResult, publicListings]);

  const bounds = useMemo(
    () => getListingBounds(currentData.items),
    [currentData.items],
  );

  useEffect(() => {
    if (!currentData.items?.length) return;
    setFilters((prev) => {
      if (prev.priceRange && prev.yearRange) return prev;
      return createInitialFilters(currentData.items);
    });
  }, [currentData.items]);

  const filteredListings = useMemo(() => {
    if (currentData.dataType !== "listing") return currentData.items;
    return applyListingFilters(currentData.items, filters, bounds);
  }, [currentData, filters, bounds]);

  const activeFilterCount = useMemo(
    () => countActiveFilters(filters, bounds),
    [filters, bounds],
  );

  const cartItemByListingId = useMemo(() => {
    const map = new Map();
    cart?.items?.forEach((item) => {
      map.set(String(item.listingId), item.id);
    });
    return map;
  }, [cart?.items]);

  const items = useMemo(() => {
    if (!filteredListings?.length) return [];

    if (currentData.dataType === "listing") {
      return mapListingsToCardItems(filteredListings, {
        user,
        cartItemByListingId,
        addToCart,
        removeFromCart,
        navigate,
        startConversation,
      });
    }

    if (currentData.dataType === "mb") {
      return filteredListings.map((release) =>
        mbReleaseToCardItem(release, isInWishlist, toggleToWishlist),
      );
    }

    return [];
  }, [
    filteredListings,
    currentData.dataType,
    cartItemByListingId,
    user,
    addToCart,
    removeFromCart,
    navigate,
    startConversation,
  ]);

  useEffect(() => {
    if (!currentData?.items) return;
    setPage(1);
    setVisibleItems(items.slice(0, PAGE_SIZE));
  }, [items]);

  useEffect(() => {
    const handleScroll = () => {
      const scrollTop = window.scrollY;
      const windowHeight = window.innerHeight;
      const fullHeight = document.documentElement.scrollHeight;
      if (scrollTop + windowHeight >= fullHeight * 0.75) {
        loadNextPage();
      }
    };
    window.addEventListener("scroll", handleScroll);
    return () => window.removeEventListener("scroll", handleScroll);
  }, [visibleItems.length, items, page]);

  const loadNextPage = () => {
    const nextPage = page + 1;
    const nextItems = items.slice(0, nextPage * PAGE_SIZE);
    if (nextItems.length > visibleItems.length) {
      setVisibleItems(nextItems);
      setPage(nextPage);
    }
  };

  const showListLayout = layout === "list" && isLargeScreen;
  const showGridLayout = layout === "grid" || !isLargeScreen;
  const isLoading = isFetchingPublic && !searchResult.items?.length;

  const viewToggleClass = (active) =>
    cn(
      "inline-flex size-9 shrink-0 items-center justify-center rounded-md border p-1.5 transition-colors",
      active
        ? "border-brand-active bg-brand text-on-surface"
        : "border-surface-4 bg-surface-2 text-brand-fg hover:bg-surface-3",
    );

  return (
    <div className="min-h-screen w-full bg-surface-base text-on-surface">
      <div className="mx-auto w-full max-w-7xl px-4 py-4 sm:px-6 sm:py-5 lg:px-8">
        <div className="flex flex-col gap-4 transition-[gap] duration-100 ease-in-out lg:flex-row lg:items-start lg:gap-6">
          <FilterSidebar
            filters={filters}
            bounds={bounds}
            onFiltersChange={setFilters}
            collapsed={sidebarCollapsed}
            onCollapsedChange={setSidebarCollapsed}
          />

          <main className="min-w-0 flex-1 transition-[flex-basis,width] duration-100 ease-in-out">
            <div className="mb-4 flex flex-wrap items-center justify-between gap-3 sm:mb-5">
              <div className="flex flex-wrap items-center gap-3">
                <MobileFilterSheet
                  filters={filters}
                  bounds={bounds}
                  onFiltersChange={setFilters}
                  activeCount={activeFilterCount}
                />
                <p className="text-sm text-on-surface-muted">
                  {isLoading ? "Loading..." : `${items.length} results`}
                </p>
              </div>

              <div
                className="flex items-center gap-1 rounded-lg border border-surface-4 bg-surface-1 p-1"
                role="group"
                aria-label="Change listing view"
              >
                <button
                  type="button"
                  onClick={() => setLayout("list")}
                  className={viewToggleClass(layout === "list")}
                  aria-label="List view"
                  aria-pressed={layout === "list"}
                >
                  <List className="size-4" strokeWidth={2.5} />
                </button>
                <button
                  type="button"
                  onClick={() => setLayout("grid")}
                  className={viewToggleClass(layout === "grid")}
                  aria-label="Grid view"
                  aria-pressed={layout === "grid"}
                >
                  <LayoutGrid className="size-4" strokeWidth={2.5} />
                </button>
              </div>
            </div>

            {!isLoading && visibleItems.length === 0 && (
              <div className="rounded-xl border border-surface-3 bg-surface-1 px-6 py-12 text-center">
                <p className="text-lg font-medium text-on-surface">
                  No listings found
                </p>
                <p className="mt-2 text-sm text-on-surface-muted">
                  Try adjusting your filters or search terms.
                </p>
              </div>
            )}

            {showListLayout && visibleItems.length > 0 && (
              <div className="overflow-hidden rounded-xl border border-surface-3">
                <div className="grid grid-cols-7 border-b border-surface-3 bg-surface-2 px-2 py-3 text-left text-xs font-medium uppercase tracking-wide text-on-surface-muted sm:text-sm">
                  <p className="text-center">Cover</p>
                  <p>Title</p>
                  <p>Artist</p>
                  <p>Year</p>
                  <p>Format</p>
                  <p>Price</p>
                  <p className="text-center">Actions</p>
                </div>

                <div>
                  {isLoading
                    ? Array(5)
                        .fill(0)
                        .map((_, i) => <SkeletonListView key={i} />)
                    : visibleItems.map((item) => (
                        <ListView key={item.id} item={item} />
                      ))}
                </div>
              </div>
            )}

            {showGridLayout && visibleItems.length > 0 && (
              <div className="grid gap-3 transition-[grid-template-columns,gap] duration-100 ease-in-out [grid-template-columns:repeat(auto-fill,minmax(min(100%,8.5rem),1fr))] sm:gap-4 sm:[grid-template-columns:repeat(auto-fill,minmax(min(100%,11.5rem),1fr))] lg:[grid-template-columns:repeat(auto-fill,minmax(min(100%,12.5rem),1fr))] xl:[grid-template-columns:repeat(auto-fill,minmax(min(100%,13rem),1fr))]">
                {isLoading
                  ? Array(8)
                      .fill(0)
                      .map((_, i) => <SkeletonCardView key={i} />)
                  : visibleItems.map((item) => (
                      <Card key={item.id} item={item} />
                    ))}
              </div>
            )}
          </main>
        </div>
      </div>
    </div>
  );
}
