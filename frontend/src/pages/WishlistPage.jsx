import { useEffect, useMemo, useRef, useState } from "react";
import { LayoutGrid, List, Search } from "lucide-react";
import { useNavigate } from "react-router-dom";

import { Button } from "@/components/ui/button";
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from "@/components/ui/dialog";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Tabs, TabsList, TabsTrigger } from "@/components/ui/tabs";
import { cn } from "@/lib/utils";

import Card from "@/components/listing/Card";
import ListView from "@/components/listing/ListView";
import ListViewHeader from "@/components/listing/ListViewHeader";
import MbReleaseFilterBar from "@/components/listing/MbReleaseFilterBar";
import SkeletonCardView from "@/components/shared/skeletons/SkeletonCardView";
import SkeletonListView from "@/components/shared/skeletons/SkeletonListView";
import { mbReleaseToWishlistItem } from "../adapters/mbReleaseToWishlistItem";
import { wishlistItemToCardItem } from "../adapters/wishlistItemToCardItem";
import { MB_SEARCH_SCOPES, useSearchStore } from "../stores/searchStore";
import { useMbReleaseFilters } from "../hooks/useMbReleaseFilters";
import { useMbScrollLoadMore } from "../hooks/useMbScrollLoadMore";
import { useListingStore } from "../stores/listingStore";
import useWishlistStore from "../stores/wishlistStore";
import { useUIStore } from "../stores/uiStore";
import { CARD_GRID_CLASS } from "../utils/cardLayout";
import { findMatchingListing } from "../utils/wishlistListingMatch";

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

export default function WishlistPage() {
  const navigate = useNavigate();
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [wishlistHolder, setWishlistHolder] = useState([]);
  const [query, setQuery] = useState("");
  const [searchScope, setSearchScope] = useState(MB_SEARCH_SCOPES.TITLE);

  const isLargeScreen = useMinWidth(1024);

  const wishlist = useWishlistStore((state) => state.wishlist);
  const isLoading = useWishlistStore((state) => state.isLoading);
  const addToWishlistBulk = useWishlistStore(
    (state) => state.addToWishlistBulk,
  );
  const isInWishlist = useWishlistStore((state) => state.isInWishlist);
  const fetchWishlist = useWishlistStore((state) => state.fetchWishlist);
  const removeFromWishlist = useWishlistStore(
    (state) => state.removeFromWishlist,
  );

  const searchMusicBrainz = useSearchStore((state) => state.searchMusicBrainz);
  const mbSearchResult = useSearchStore((state) => state.mbSearchResult);
  const mbSearchContext = useSearchStore((state) => state.mbSearchContext);
  const isLoadingMbSearch = useSearchStore((state) => state.isLoadingMbSearch);
  const isLoadingMoreMb = useSearchStore((state) => state.isLoadingMoreMb);
  const hasMoreMbResults = useSearchStore((state) => state.hasMoreMbResults);
  const mbModalScrollRef = useRef(null);
  useMbScrollLoadMore(mbModalScrollRef);
  const fetchPublicListings = useListingStore((state) => state.fetchPublicListings);
  const publicListings = useListingStore((state) => state.publicListings.items);

  const layout = useUIStore((state) => state.layout);
  const setLayout = useUIStore((state) => state.setLayout);

  useEffect(() => {
    fetchWishlist();
    fetchPublicListings({ page: 0, size: 200 });
  }, [fetchWishlist, fetchPublicListings]);

  const isInWishlistHolder = (item) => {
    if (!item || item.id == null) return false;

    return wishlistHolder.some((w) => w?.id != null && w.id === item.id);
  };

  const addToWishlistHolder = (item) => {
    if (!isInWishlistHolder(item)) {
      setWishlistHolder((prev) => [...prev, item]);
    }
  };

  const removeFromWishlistHolder = (item) => {
    setWishlistHolder((prev) => prev.filter((entry) => entry?.id !== item.id));
  };

  const handleModalClose = async () => {
    if (wishlistHolder.length > 0) {
      await addToWishlistBulk(wishlistHolder);
    }

    setWishlistHolder([]);
    setIsModalOpen(false);
  };

  const handleOpenChange = (open) => {
    if (open) {
      setIsModalOpen(true);
      return;
    }

    void handleModalClose();
  };

  const handleSearch = async (e) => {
    e?.preventDefault();
    if (!query.trim()) {
      return;
    }

    await searchMusicBrainz(query.trim(), searchScope);
    setIsModalOpen(true);
  };

  const items = useMemo(
    () =>
      wishlist?.map((item) =>
        wishlistItemToCardItem(
          item,
          isInWishlist,
          removeFromWishlist,
          findMatchingListing(item, publicListings),
          navigate,
        ),
      ) ?? [],
    [wishlist, isInWishlist, removeFromWishlist, publicListings, navigate],
  );

  const mbReleases = mbSearchResult?.items;

  const {
    filters: mbFilters,
    setFilters: setMbFilters,
    bounds: mbFilterBounds,
    filteredReleases: filteredMbReleases,
    hasReleases: hasMbReleases,
    releaseCount: mbReleaseCount,
  } = useMbReleaseFilters(mbReleases, {
    isLoadingSearch: isLoadingMbSearch,
    searchContext: mbSearchContext,
  });

  const searchItems = useMemo(
    () =>
      filteredMbReleases.map((item) =>
        mbReleaseToWishlistItem(
          item,
          isInWishlistHolder,
          addToWishlistHolder,
          removeFromWishlistHolder,
        ),
      ),
    [filteredMbReleases, wishlistHolder],
  );

  const showListLayout = layout === "list" && isLargeScreen;
  const showGridLayout = layout === "grid" || !isLargeScreen;
  const selectedCount = wishlistHolder.length;

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
        <div className="mb-5 flex flex-col gap-4 sm:mb-6">
          <h1 className="text-2xl font-semibold tracking-tight text-on-surface sm:text-3xl">
            Wishlist
          </h1>

          <form
            className="mx-auto flex w-full max-w-xl flex-col gap-3"
            onSubmit={handleSearch}
          >
            <div className="flex flex-col gap-2">
              <Label htmlFor="wishlist-mb-search" className="text-on-surface-dim">
                Search in
              </Label>
              <Tabs
                value={searchScope}
                onValueChange={setSearchScope}
                className="gap-0"
              >
                <TabsList className="w-full border border-surface-3 bg-surface-2">
                  <TabsTrigger value={MB_SEARCH_SCOPES.TITLE} className="flex-1">
                    Title
                  </TabsTrigger>
                  <TabsTrigger value={MB_SEARCH_SCOPES.ARTIST} className="flex-1">
                    Artist
                  </TabsTrigger>
                  <TabsTrigger value={MB_SEARCH_SCOPES.BOTH} className="flex-1">
                    Both
                  </TabsTrigger>
                </TabsList>
              </Tabs>
            </div>

            <div className="flex items-center gap-2">
              <Input
                id="wishlist-mb-search"
                type="search"
                placeholder="Find albums on MusicBrainz..."
                value={query}
                onChange={(e) => setQuery(e.target.value)}
                className="min-w-0 flex-1 border-surface-3 bg-surface-form text-on-surface placeholder:text-on-surface-muted"
              />
              <Button
                type="submit"
                size="icon"
                disabled={!query.trim() || isLoadingMbSearch}
                aria-label="Search MusicBrainz"
              >
                <Search />
              </Button>
            </div>
          </form>
        </div>

        <div className="mb-4 flex flex-wrap items-center justify-between gap-3 sm:mb-5">
          <p className="text-sm text-on-surface-muted">
            {isLoading ? "Loading..." : `${items.length} saved albums`}
          </p>

          <div
            className="flex items-center gap-1 rounded-lg border border-surface-4 bg-surface-1 p-1"
            role="group"
            aria-label="Change wishlist view"
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

        {!isLoading && items.length === 0 && (
          <div className="rounded-xl border border-surface-3 bg-surface-1 px-6 py-12 text-center">
            <p className="text-lg font-medium text-on-surface">Wishlist is empty</p>
            <p className="mt-2 text-sm text-on-surface-muted">
              Search MusicBrainz above to add albums you want to track.
            </p>
          </div>
        )}

        {showListLayout && items.length > 0 && (
          <div className="overflow-hidden rounded-xl border border-surface-3">
            <ListViewHeader showPrice={false} />

            <div>
              {isLoading
                ? Array(5)
                    .fill(0)
                    .map((_, i) => (
                      <SkeletonListView key={i} showPrice={false} />
                    ))
                : items.map((item) => <ListView key={item.id} item={item} />)}
            </div>
          </div>
        )}

        {showGridLayout && items.length > 0 && (
          <div className={CARD_GRID_CLASS}>
            {isLoading
              ? Array(8)
                  .fill(0)
                  .map((_, i) => <SkeletonCardView key={i} />)
              : items.map((item) => <Card key={item.id} item={item} />)}
          </div>
        )}
      </div>

      <Dialog open={isModalOpen} onOpenChange={handleOpenChange}>
        <DialogContent
          showCloseButton
          className="flex max-h-[85vh] flex-col gap-0 overflow-hidden border-surface-3 bg-surface-1 p-0 text-on-surface ring-surface-4 sm:max-w-6xl"
        >
          <DialogHeader className="border-b border-surface-3 px-5 py-4 sm:px-6">
            <DialogTitle className="text-on-surface">Search results</DialogTitle>
            <DialogDescription className="text-on-surface-muted">
              Add albums to your wishlist. Selected items save when you close.
            </DialogDescription>
          </DialogHeader>

          {!isLoadingMbSearch && hasMbReleases && (
            <MbReleaseFilterBar
              filters={mbFilters}
              bounds={mbFilterBounds}
              onFiltersChange={setMbFilters}
              resultCount={searchItems.length}
              totalCount={mbReleaseCount}
              hasMoreMbResults={hasMoreMbResults}
            />
          )}

          <div
            ref={mbModalScrollRef}
            className="flex-1 overflow-y-auto px-5 py-4 sm:px-6"
          >
            {isLoadingMbSearch && (
              <div className={CARD_GRID_CLASS}>
                {Array(6)
                  .fill(0)
                  .map((_, i) => (
                    <SkeletonCardView key={i} />
                  ))}
              </div>
            )}

            {!isLoadingMbSearch && !hasMbReleases && (
              <div className="rounded-xl border border-surface-3 bg-surface-2 px-6 py-10 text-center">
                <p className="font-medium text-on-surface">No results found</p>
                <p className="mt-2 text-sm text-on-surface-muted">
                  Try another query or search scope.
                </p>
              </div>
            )}

            {!isLoadingMbSearch && hasMbReleases && searchItems.length === 0 && (
              <div className="rounded-xl border border-surface-3 bg-surface-2 px-6 py-10 text-center">
                <p className="font-medium text-on-surface">
                  No releases match your filters
                </p>
                <p className="mt-2 text-sm text-on-surface-muted">
                  Adjust or reset filters to see more results.
                </p>
              </div>
            )}

            {!isLoadingMbSearch && searchItems.length > 0 && (
              <div className={CARD_GRID_CLASS}>
                {searchItems.map((item) => (
                  <Card key={item.id} item={item} />
                ))}
              </div>
            )}

            {isLoadingMoreMb && (
              <div className={cn(CARD_GRID_CLASS, "mt-4")}>
                {Array(3)
                  .fill(0)
                  .map((_, i) => (
                    <SkeletonCardView key={`mb-load-more-${i}`} />
                  ))}
              </div>
            )}
          </div>

          <DialogFooter className="mx-0 mb-0 flex flex-row items-center justify-end gap-3 border-t border-surface-3 bg-surface-2/40 px-5 py-4 sm:px-6">
            {selectedCount > 0 && (
              <p className="mr-auto text-sm text-on-surface-dim">
                {selectedCount} selected
              </p>
            )}
            <Button type="button" onClick={() => void handleModalClose()}>
              {selectedCount > 0 ? "Save and close" : "Close"}
            </Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>
    </div>
  );
}
