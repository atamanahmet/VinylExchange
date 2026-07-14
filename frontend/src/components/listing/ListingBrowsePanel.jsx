import { useCallback, useEffect, useMemo, useState } from "react";
import { LayoutGrid, List } from "lucide-react";

import Card from "@/components/listing/Card";
import FilterSidebar, { MobileFilterSheet } from "@/components/listing/FilterSidebar";
import ListView from "@/components/listing/ListView";
import ListViewHeader from "@/components/listing/ListViewHeader";
import ListingSortSelect from "@/components/listing/ListingSortSelect";
import SkeletonCardView from "@/components/shared/skeletons/SkeletonCardView";
import SkeletonListView from "@/components/shared/skeletons/SkeletonListView";

import { useUIStore } from "../../stores/uiStore";
import { cn } from "@/lib/utils";
import { countActiveFilters } from "../../utils/listingFilters";
import { buildPaginationRange } from "../../utils/paginationRange";

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

export default function ListingBrowsePanel({
  header,
  items,
  pagination,
  page,
  onPageChange,
  isLoading,
  isFetching,
  draftFilters,
  onDraftFiltersChange,
  onApplyFilters,
  onResetFilters,
  sort,
  onSortChange,
  showSort = false,
  showPagination = true,
  emptyTitle = "No listings found",
  emptyHint = "Try adjusting your filters or search terms.",
}) {
  const [sidebarCollapsed, setSidebarCollapsed] = useState(true);
  const isLargeScreen = useMinWidth(1024);

  const layout = useUIStore((state) => state.layout);
  const setLayout = useUIStore((state) => state.setLayout);

  const activeFilterCount = useMemo(
    () => countActiveFilters(draftFilters),
    [draftFilters],
  );

  const totalResults = pagination?.totalElements ?? items.length;
  const totalPages = pagination?.totalPages ?? 1;
  const currentPage = pagination?.page ?? page;
  const pageNumbers = useMemo(
    () => buildPaginationRange(currentPage, totalPages),
    [currentPage, totalPages],
  );

  const goToPage = useCallback(
    (nextPage) => {
      onPageChange(nextPage);
      window.scrollTo({ top: 0, behavior: "smooth" });
    },
    [onPageChange],
  );

  const showListLayout = layout === "list" && isLargeScreen;
  const showGridLayout = layout === "grid" || !isLargeScreen;
  const listViewShowsPrice = true;

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
        {header}

        <div className="flex flex-col gap-4 transition-[gap] duration-100 ease-in-out lg:flex-row lg:items-start lg:gap-6">
          <FilterSidebar
            filters={draftFilters}
            onFiltersChange={onDraftFiltersChange}
            onApply={onApplyFilters}
            onReset={onResetFilters}
            collapsed={sidebarCollapsed}
            onCollapsedChange={setSidebarCollapsed}
          />

          <main className="min-w-0 flex-1 transition-[flex-basis,width] duration-100 ease-in-out">
            <div className="mb-4 flex flex-wrap items-center justify-between gap-3 sm:mb-5">
              <div className="flex flex-wrap items-center gap-3">
                <MobileFilterSheet
                  filters={draftFilters}
                  onFiltersChange={onDraftFiltersChange}
                  onApply={onApplyFilters}
                  onReset={onResetFilters}
                  activeCount={activeFilterCount}
                />
                <p className="text-sm text-on-surface-muted">
                  {isLoading ? "Loading..." : `${totalResults} results`}
                </p>
              </div>

              <div className="flex items-center gap-2">
                {showSort && onSortChange && (
                  <ListingSortSelect
                    value={sort}
                    onValueChange={onSortChange}
                    disabled={isFetching}
                  />
                )}
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
            </div>

            {!isLoading && items.length === 0 && (
              <div className="rounded-xl border border-surface-3 bg-surface-1 px-6 py-12 text-center">
                <p className="text-lg font-medium text-on-surface">{emptyTitle}</p>
                <p className="mt-2 text-sm text-on-surface-muted">{emptyHint}</p>
              </div>
            )}

            {showListLayout && items.length > 0 && (
              <div className="overflow-hidden rounded-xl border border-surface-3">
                <ListViewHeader showPrice={listViewShowsPrice} />

                <div>
                  {isLoading
                    ? Array(5)
                        .fill(0)
                        .map((_, i) => (
                          <SkeletonListView
                            key={i}
                            showPrice={listViewShowsPrice}
                          />
                        ))
                    : items.map((item) => (
                        <ListView key={item.id} item={item} />
                      ))}
                </div>
              </div>
            )}

            {showGridLayout && items.length > 0 && (
              <div className="grid gap-3 transition-[grid-template-columns,gap] duration-100 ease-in-out [grid-template-columns:repeat(auto-fill,minmax(min(100%,8.5rem),1fr))] sm:gap-4 sm:[grid-template-columns:repeat(auto-fill,minmax(min(100%,11.5rem),1fr))] lg:[grid-template-columns:repeat(auto-fill,minmax(min(100%,12.5rem),1fr))] xl:[grid-template-columns:repeat(auto-fill,minmax(min(100%,13rem),1fr))]">
                {isLoading
                  ? Array(8)
                      .fill(0)
                      .map((_, i) => <SkeletonCardView key={i} />)
                  : items.map((item) => (
                      <Card key={item.id} item={item} />
                    ))}
              </div>
            )}

            {showPagination && totalPages > 1 && (
              <nav
                className="mt-6 flex flex-wrap items-center justify-center gap-3"
                aria-label="Listing pages"
              >
                <button
                  type="button"
                  disabled={currentPage <= 0 || isFetching}
                  onClick={() => goToPage(Math.max(0, currentPage - 1))}
                  className={cn(
                    "text-sm font-medium transition-colors",
                    currentPage <= 0 || isFetching
                      ? "cursor-not-allowed text-on-surface-muted/50"
                      : "text-on-surface-muted hover:text-brand-fg",
                  )}
                >
                  Previous
                </button>

                {pageNumbers.map((item, index) =>
                  item === "ellipsis" ? (
                    <span
                      key={`ellipsis-${index}`}
                      className="px-1 text-sm text-on-surface-muted"
                      aria-hidden="true"
                    >
                      …
                    </span>
                  ) : (
                    <button
                      key={item}
                      type="button"
                      disabled={isFetching}
                      onClick={() => goToPage(item)}
                      aria-current={item === currentPage ? "page" : undefined}
                      className={cn(
                        "min-w-6 text-sm font-medium transition-colors",
                        item === currentPage
                          ? "text-brand-fg"
                          : "text-on-surface-muted hover:text-brand-fg",
                        isFetching && "cursor-not-allowed opacity-50",
                      )}
                    >
                      {item + 1}
                    </button>
                  ),
                )}

                <button
                  type="button"
                  disabled={currentPage >= totalPages - 1 || isFetching}
                  onClick={() =>
                    goToPage(Math.min(totalPages - 1, currentPage + 1))
                  }
                  className={cn(
                    "text-sm font-medium transition-colors",
                    currentPage >= totalPages - 1 || isFetching
                      ? "cursor-not-allowed text-on-surface-muted/50"
                      : "text-on-surface-muted hover:text-brand-fg",
                  )}
                >
                  Next
                </button>
              </nav>
            )}
          </main>
        </div>
      </div>
    </div>
  );
}
