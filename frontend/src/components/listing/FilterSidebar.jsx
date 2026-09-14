import { useState } from "react";
import {
  ChevronDown,
  ChevronLeft,
  ChevronRight,
  SlidersHorizontal,
} from "lucide-react";

import CountryFilterOptions from "@/components/listing/CountryFilterOptions";
import GenreFilterOptions from "@/components/listing/GenreFilterOptions";
import { Button } from "@/components/ui/button";
import { Checkbox } from "@/components/ui/checkbox";
import {
  Collapsible,
  CollapsibleContent,
  CollapsibleTrigger,
} from "@/components/ui/collapsible";
import { Label } from "@/components/ui/label";
import { Separator } from "@/components/ui/separator";
import { Slider } from "@/components/ui/slider";
import {
  Sheet,
  SheetContent,
  SheetHeader,
  SheetTitle,
  SheetTrigger,
} from "@/components/ui/sheet";
import { cn } from "@/lib/utils";
import {
  CONDITION_OPTIONS,
  countSectionFilters,
  FORMAT_LABELS,
  FORMAT_OPTIONS,
  RPM_OPTIONS,
  SLIDER_BOUNDS,
  VINYL_SUBTYPE_OPTIONS,
} from "@/utils/listingFilters";
import { useCountryOptions } from "@/hooks/useCountryOptions";
import { useGenreOptions } from "@/hooks/useGenreOptions";

function FilterActionBar({ onReset, onApply, canReset, canApply, className }) {
  return (
    <div className={cn("flex items-center justify-between gap-2 py-1", className)}>
      <Button
        variant="ghost"
        size="xs"
        className="h-7 py-0"
        onClick={onReset}
        disabled={!canReset}
      >
        Reset
      </Button>
      <Button size="sm" onClick={onApply} disabled={!canApply}>
        {canApply ? "Apply filters" : "Applied"}
      </Button>
    </div>
  );
}

function FilterSection({ title, activeCount = 0, defaultOpen = true, children }) {
  const [open, setOpen] = useState(defaultOpen);

  return (
    <Collapsible open={open} onOpenChange={setOpen}>
      <CollapsibleTrigger className="flex w-full items-center justify-between gap-2 py-2 text-left text-sm font-medium text-foreground transition-colors hover:text-primary">
        <span className="flex min-w-0 items-center gap-2">
          <span className="truncate">{title}</span>
          {activeCount > 0 && (
            <span className="inline-flex h-5 min-w-5 shrink-0 items-center justify-center rounded-full bg-brand px-1.5 text-[10px] font-semibold text-on-brand">
              {activeCount}
            </span>
          )}
        </span>
        <ChevronDown
          className={cn(
            "size-4 shrink-0 text-muted-foreground transition-transform duration-100",
            open && "rotate-180",
          )}
        />
      </CollapsibleTrigger>
      <CollapsibleContent className="space-y-3 pb-4 pt-1">
        {children}
      </CollapsibleContent>
    </Collapsible>
  );
}

export function FilterPanel({
  filters,
  onFiltersChange,
  onApply,
  onReset,
  className,
  showHeader = true,
  canReset = true,
  canApply = true,
}) {
  const { options: countryOptions, loading: countriesLoading } = useCountryOptions("en");
  const [includeLocalGenres, setIncludeLocalGenres] = useState(true);
  const { options: genreOptions, loading: genresLoading } = useGenreOptions(
    "en",
    includeLocalGenres,
  );

  const toggleArrayValue = (key, value) => {
    const current = filters[key] ?? [];
    const next = current.includes(value)
      ? current.filter((item) => item !== value)
      : [...current, value];
    onFiltersChange({ ...filters, [key]: next });
  };

  const toggleFormat = (format) => {
    const current = filters.formats ?? [];
    const nextFormats = current.includes(format)
      ? current.filter((item) => item !== format)
      : [...current, format];

    const next = { ...filters, formats: nextFormats };
    if (!nextFormats.includes("VINYL")) {
      next.speedRpm = [];
      next.vinylSubtype = [];
    }
    onFiltersChange(next);
  };

  const vinylSelected = filters.formats.includes("VINYL");

  return (
    <div className={cn("flex flex-col gap-1", className)}>
      {showHeader && (
        <>
          <h2 className="pb-1 text-base font-semibold text-foreground">Filters</h2>
          <FilterActionBar
            onReset={onReset}
            onApply={onApply}
            canReset={canReset}
            canApply={canApply}
          />
          <Separator />
        </>
      )}

      <FilterSection
        title="Format"
        activeCount={countSectionFilters(filters, "format")}
      >
        <div className="space-y-2">
          {FORMAT_OPTIONS.map((format) => (
            <div key={format} className="flex items-center gap-2">
              <Checkbox
                id={`format-${format}`}
                checked={filters.formats.includes(format)}
                onCheckedChange={() => toggleFormat(format)}
              />
              <Label htmlFor={`format-${format}`} className="font-normal">
                {FORMAT_LABELS[format] ?? format}
              </Label>
            </div>
          ))}
        </div>

        {vinylSelected && (
          <>
            <div className="space-y-2 pt-2">
              <p className="text-xs font-medium text-muted-foreground">RPM</p>
              {RPM_OPTIONS.map((rpm) => (
                <div key={rpm} className="flex items-center gap-2">
                  <Checkbox
                    id={`rpm-${rpm}`}
                    checked={filters.speedRpm.includes(rpm)}
                    onCheckedChange={() => toggleArrayValue("speedRpm", rpm)}
                  />
                  <Label htmlFor={`rpm-${rpm}`} className="font-normal">
                    {rpm} RPM
                  </Label>
                </div>
              ))}
            </div>

            <div className="space-y-2 pt-2">
              <p className="text-xs font-medium text-muted-foreground">
                Vinyl Subtype
              </p>
              {VINYL_SUBTYPE_OPTIONS.map(({ value, label }) => (
                <div key={value} className="flex items-center gap-2">
                  <Checkbox
                    id={`vinyl-subtype-${value}`}
                    checked={filters.vinylSubtype.includes(value)}
                    onCheckedChange={() =>
                      toggleArrayValue("vinylSubtype", value)
                    }
                  />
                  <Label
                    htmlFor={`vinyl-subtype-${value}`}
                    className="font-normal"
                  >
                    {label}
                  </Label>
                </div>
              ))}
            </div>
          </>
        )}
      </FilterSection>

      <Separator />

      <FilterSection
        title="Condition"
        activeCount={countSectionFilters(filters, "condition")}
      >
        <div className="space-y-2">
          {CONDITION_OPTIONS.map(({ value, label }) => (
            <div key={value} className="flex items-center gap-2">
              <Checkbox
                id={`condition-${value}`}
                checked={filters.conditions.includes(value)}
                onCheckedChange={() => toggleArrayValue("conditions", value)}
              />
              <Label htmlFor={`condition-${value}`} className="font-normal">
                {label}
              </Label>
            </div>
          ))}
        </div>
      </FilterSection>

      <Separator />

      <FilterSection
        title="Country"
        activeCount={countSectionFilters(filters, "country")}
        defaultOpen={false}
      >
        <CountryFilterOptions
          options={countryOptions}
          selected={filters.countries ?? []}
          onToggle={(value) => toggleArrayValue("countries", value)}
          loading={countriesLoading}
        />
      </FilterSection>

      <Separator />

      <FilterSection
        title="Genre"
        activeCount={countSectionFilters(filters, "genre")}
        defaultOpen={false}
      >
        <GenreFilterOptions
          options={genreOptions}
          selected={filters.genreIds ?? []}
          onToggle={(value) => toggleArrayValue("genreIds", value)}
          loading={genresLoading}
          includeLocal={includeLocalGenres}
          onIncludeLocalChange={setIncludeLocalGenres}
        />
      </FilterSection>

      <Separator />

      <FilterSection
        title="Price (TL)"
        activeCount={countSectionFilters(filters, "price")}
      >
        <div className="space-y-3 px-1">
          <Slider
            min={SLIDER_BOUNDS.minPrice}
            max={SLIDER_BOUNDS.maxPrice}
            step={1}
            value={filters.priceRange}
            onValueChange={(priceRange) =>
              onFiltersChange({ ...filters, priceRange })
            }
          />
          <div className="flex justify-between text-xs text-muted-foreground">
            <span>{filters.priceRange[0]} TL</span>
            <span>{filters.priceRange[1]} TL</span>
          </div>
        </div>
      </FilterSection>

      <Separator />

      <FilterSection
        title="Release Year"
        activeCount={countSectionFilters(filters, "year")}
      >
        <div className="space-y-3 px-1">
          <Slider
            min={SLIDER_BOUNDS.minYear}
            max={SLIDER_BOUNDS.maxYear}
            step={1}
            value={filters.yearRange}
            onValueChange={(yearRange) =>
              onFiltersChange({ ...filters, yearRange })
            }
          />
          <div className="flex justify-between text-xs text-muted-foreground">
            <span>{filters.yearRange[0]}</span>
            <span>{filters.yearRange[1]}</span>
          </div>
        </div>
      </FilterSection>

      <Separator />

      <FilterSection
        title="Trade"
        activeCount={countSectionFilters(filters, "trade")}
        defaultOpen={false}
      >
        <div className="flex items-center gap-2">
          <Checkbox
            id="tradeable-only"
            checked={filters.tradeableOnly}
            onCheckedChange={(checked) =>
              onFiltersChange({ ...filters, tradeableOnly: checked === true })
            }
          />
          <Label htmlFor="tradeable-only" className="font-normal">
            Tradeable only
          </Label>
        </div>
      </FilterSection>

      <Separator />

      <FilterActionBar
        onReset={onReset}
        onApply={onApply}
        canReset={canReset}
        canApply={canApply}
      />
    </div>
  );
}

export function MobileFilterSheet({
  filters,
  onFiltersChange,
  onApply,
  onReset,
  activeCount = 0,
  canReset = true,
  canApply = true,
}) {
  const [open, setOpen] = useState(false);

  const handleApply = () => {
    onApply();
    setOpen(false);
  };

  const handleReset = () => {
    onReset();
    setOpen(false);
  };

  return (
    <Sheet open={open} onOpenChange={setOpen}>
      <SheetTrigger asChild>
        <Button variant="outline" size="sm" className="gap-2 lg:hidden">
          <SlidersHorizontal className="size-4" />
          Filters
          {activeCount > 0 && (
            <span className="rounded-full bg-primary px-1.5 py-0.5 text-[10px] font-medium text-primary-foreground">
              {activeCount}
            </span>
          )}
        </Button>
      </SheetTrigger>
      <SheetContent
        side="left"
        className="w-80 overflow-y-auto duration-100 ease-in-out data-open:duration-100 data-closed:duration-100"
      >
        <SheetHeader>
          <SheetTitle>Filters</SheetTitle>
        </SheetHeader>
        <FilterPanel
          filters={filters}
          onFiltersChange={onFiltersChange}
          onApply={handleApply}
          onReset={handleReset}
          className="px-1"
          canReset={canReset}
          canApply={canApply}
        />
      </SheetContent>
    </Sheet>
  );
}

/**
 * Collapsed rail. The whole strip is the expand control, labelled and
 * badged, so it reads as interactive instead of a bare chevron.
 */
function CollapsedFilterRail({ activeCount, onExpand }) {
  return (
    <aside className="sticky top-20 hidden max-h-[calc(100vh-5.5rem)] min-h-[calc(100vh-5.5rem)] w-12 shrink-0 self-start overflow-hidden rounded-xl border border-border bg-card text-card-foreground lg:flex">
      <button
        type="button"
        onClick={onExpand}
        aria-expanded="false"
        aria-label={
          activeCount > 0
            ? `Show filters, ${activeCount} active`
            : "Show filters"
        }
        className="group flex w-full cursor-pointer flex-col items-center gap-3 py-3 transition-colors hover:bg-surface-2"
      >
        <span className="flex size-8 items-center justify-center rounded-md border border-border bg-surface-2 text-on-surface-dim transition-colors group-hover:border-brand group-hover:text-brand-fg">
          <SlidersHorizontal className="size-4" />
        </span>

        {activeCount > 0 && (
          <span className="inline-flex h-5 min-w-5 items-center justify-center rounded-full bg-brand px-1.5 text-[10px] font-semibold text-on-brand">
            {activeCount}
          </span>
        )}

        <span className="text-xs font-medium uppercase tracking-widest text-on-surface-dim transition-colors [writing-mode:vertical-rl] group-hover:text-on-surface">
          Filters
        </span>

        <ChevronRight className="mt-auto size-4 text-on-surface-muted transition-colors group-hover:text-brand-fg" />
      </button>
    </aside>
  );
}

export default function FilterSidebar({
  filters,
  onFiltersChange,
  onApply,
  onReset,
  collapsed,
  onCollapsedChange,
  activeCount = 0,
  canReset = true,
  canApply = true,
}) {
  if (collapsed) {
    return (
      <CollapsedFilterRail
        activeCount={activeCount}
        onExpand={() => onCollapsedChange(false)}
      />
    );
  }

  return (
    <aside className="sticky top-20 hidden max-h-[calc(100vh-5.5rem)] w-64 shrink-0 flex-col self-start overflow-hidden rounded-xl border border-border bg-card text-card-foreground lg:flex">
      <div className="flex shrink-0 items-center justify-between border-b border-border p-2 px-3">
        <span className="flex items-center gap-2 text-sm font-semibold text-foreground">
          Filters
          {activeCount > 0 && (
            <span className="inline-flex h-5 min-w-5 items-center justify-center rounded-full bg-brand px-1.5 text-[10px] font-semibold text-on-brand">
              {activeCount}
            </span>
          )}
        </span>
        <Button
          variant="ghost"
          size="icon-sm"
          onClick={() => onCollapsedChange(true)}
          aria-expanded="true"
          aria-label="Hide filters"
        >
          <ChevronLeft />
        </Button>
      </div>

      <div className="flex min-h-0 flex-1 flex-col">
        <div className="flex shrink-0 flex-col gap-1 px-4 pt-1">
          <FilterActionBar
            onReset={onReset}
            onApply={onApply}
            canReset={canReset}
            canApply={canApply}
          />
          <Separator />
        </div>
        <div className="min-h-0 flex-1 overflow-y-auto overflow-x-hidden overscroll-contain px-4 pb-4 pt-1">
          <FilterPanel
            filters={filters}
            onFiltersChange={onFiltersChange}
            onApply={onApply}
            onReset={onReset}
            showHeader={false}
            canReset={canReset}
            canApply={canApply}
          />
        </div>
      </div>
    </aside>
  );
}
