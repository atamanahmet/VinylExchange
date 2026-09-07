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
  FORMAT_LABELS,
  FORMAT_OPTIONS,
  RPM_OPTIONS,
  SLIDER_BOUNDS,
  VINYL_SUBTYPE_OPTIONS,
} from "@/utils/listingFilters";
import { useCountryOptions } from "@/hooks/useCountryOptions";
import { useGenreOptions } from "@/hooks/useGenreOptions";

function FilterResetButton({ onClick, className }) {
  return (
    <Button
      variant="ghost"
      size="xs"
      className={cn("h-6 py-0", className)}
      onClick={onClick}
    >
      Reset
    </Button>
  );
}

function FilterActionBar({ onReset, onApply, className }) {
  return (
    <div className={cn("flex items-center justify-between gap-2 py-1", className)}>
      <FilterResetButton onClick={onReset} />
      <Button size="sm" onClick={onApply}>
        Apply
      </Button>
    </div>
  );
}

function FilterSection({ title, defaultOpen = true, children }) {
  const [open, setOpen] = useState(defaultOpen);

  return (
    <Collapsible open={open} onOpenChange={setOpen}>
      <CollapsibleTrigger className="flex w-full items-center justify-between py-2 text-left text-sm font-medium text-foreground hover:text-primary transition-colors">
        {title}
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
          <FilterActionBar onReset={onReset} onApply={onApply} />
          <Separator />
        </>
      )}

      <FilterSection title="Format">
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

      <FilterSection title="Condition">
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

      <FilterSection title="Country" defaultOpen={false}>
        <CountryFilterOptions
          options={countryOptions}
          selected={filters.countries ?? []}
          onToggle={(value) => toggleArrayValue("countries", value)}
          loading={countriesLoading}
        />
      </FilterSection>

      <Separator />

      <FilterSection title="Genre" defaultOpen={false}>
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

      <FilterSection title="Price (TL)">
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

      <FilterSection title="Release Year">
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

      <FilterSection title="Trade" defaultOpen={false}>
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

      <FilterActionBar onReset={onReset} onApply={onApply} />
    </div>
  );
}

export function MobileFilterSheet({
  filters,
  onFiltersChange,
  onApply,
  onReset,
  activeCount = 0,
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
        />
      </SheetContent>
    </Sheet>
  );
}

export default function FilterSidebar({
  filters,
  onFiltersChange,
  onApply,
  onReset,
  collapsed,
  onCollapsedChange,
}) {
  return (
    <aside
      className={cn(
        "hidden lg:flex shrink-0 flex-col self-start rounded-xl border border-border bg-card text-card-foreground overflow-hidden",
        "sticky top-20 max-h-[calc(100vh-5.5rem)]",
        "transition-[width] duration-100 ease-in-out",
        collapsed ? "w-12 min-h-[calc(100vh-5.5rem)]" : "w-64",
      )}
    >
      <div
        className={cn(
          "relative flex shrink-0 items-center border-b border-border p-2",
          collapsed ? "justify-center" : "justify-between px-3",
        )}
      >
        <span
          className={cn(
            "text-sm font-semibold text-foreground transition-opacity duration-100 ease-in-out",
            collapsed ? "pointer-events-none absolute opacity-0" : "opacity-100",
          )}
        >
          Filters
        </span>
        <Button
          variant="ghost"
          size="icon-sm"
          onClick={() => onCollapsedChange(!collapsed)}
          aria-label={collapsed ? "Expand filters" : "Collapse filters"}
        >
          {collapsed ? <ChevronRight /> : <ChevronLeft />}
        </Button>
      </div>

      <div
        className={cn(
          "flex min-h-0 flex-1 flex-col overflow-hidden transition-opacity duration-100 ease-in-out",
          collapsed ? "pointer-events-none opacity-0" : "opacity-100",
        )}
        aria-hidden={collapsed}
      >
        <div className="flex w-64 min-h-0 flex-1 flex-col">
          <div className="flex shrink-0 flex-col gap-1 px-4 pt-1">
            <FilterActionBar onReset={onReset} onApply={onApply} />
            <Separator />
          </div>
          <div className="min-h-0 flex-1 overflow-y-auto overflow-x-hidden overscroll-contain px-4 pb-4 pt-1">
            <FilterPanel
              filters={filters}
              onFiltersChange={onFiltersChange}
              onApply={onApply}
              onReset={onReset}
              showHeader={false}
            />
          </div>
        </div>
      </div>
    </aside>
  );
}
