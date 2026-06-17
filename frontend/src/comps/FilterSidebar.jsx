import { useState } from "react";
import {
  ChevronDown,
  ChevronLeft,
  ChevronRight,
  SlidersHorizontal,
} from "lucide-react";

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
  FORMAT_OPTIONS,
  createInitialFilters,
  resetFilters,
} from "@/utils/listingFilters";

function countryFilterId(country) {
  return `country-${country.replace(/[^a-zA-Z0-9]+/g, "-")}`;
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
  bounds,
  onFiltersChange,
  onReset,
  className,
  showHeader = true,
}) {
  const toggleArrayValue = (key, value) => {
    const current = filters[key] ?? [];
    const next = current.includes(value)
      ? current.filter((item) => item !== value)
      : [...current, value];
    onFiltersChange({ ...filters, [key]: next });
  };

  return (
    <div className={cn("flex flex-col gap-1", className)}>
      {showHeader && (
        <>
          <div className="flex items-center justify-between pb-2">
            <h2 className="text-base font-semibold text-foreground">Filters</h2>
            <Button variant="ghost" size="sm" onClick={onReset}>
              Reset
            </Button>
          </div>
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
                onCheckedChange={() => toggleArrayValue("formats", format)}
              />
              <Label htmlFor={`format-${format}`} className="font-normal">
                {format === "33" ? '12" (33 RPM)' : format === "45" ? '7" (45 RPM)' : format}
              </Label>
            </div>
          ))}
        </div>
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

      {bounds.countries?.length > 0 && (
        <>
          <FilterSection title="Country" defaultOpen={false}>
            <div className="max-h-48 space-y-2 overflow-y-auto overscroll-contain pr-1">
              {bounds.countries.map((country) => (
                <div key={country} className="flex items-center gap-2">
                  <Checkbox
                    id={countryFilterId(country)}
                    checked={(filters.countries ?? []).includes(country)}
                    onCheckedChange={() => toggleArrayValue("countries", country)}
                  />
                  <Label htmlFor={countryFilterId(country)} className="font-normal">
                    {country}
                  </Label>
                </div>
              ))}
            </div>
          </FilterSection>
          <Separator />
        </>
      )}

      <FilterSection title="Price (TL)">
        <div className="space-y-3 px-1">
          <Slider
            min={bounds.minPrice}
            max={bounds.maxPrice}
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
            min={bounds.minYear}
            max={bounds.maxYear}
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
    </div>
  );
}

export function MobileFilterSheet({
  filters,
  bounds,
  onFiltersChange,
  activeCount = 0,
}) {
  const handleReset = () => onFiltersChange(resetFilters(bounds));

  return (
    <Sheet>
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
          bounds={bounds}
          onFiltersChange={onFiltersChange}
          onReset={handleReset}
          className="px-1"
        />
      </SheetContent>
    </Sheet>
  );
}

export default function FilterSidebar({
  filters,
  bounds,
  onFiltersChange,
  collapsed,
  onCollapsedChange,
}) {
  const handleReset = () => onFiltersChange(resetFilters(bounds));

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
          <div className="flex shrink-0 justify-end px-4 pt-4 pb-2">
            <Button variant="ghost" size="sm" onClick={handleReset}>
              Reset
            </Button>
          </div>
          <div className="min-h-0 flex-1 overflow-y-auto overflow-x-hidden overscroll-contain px-4 pb-4">
            <FilterPanel
              filters={filters}
              bounds={bounds}
              onFiltersChange={onFiltersChange}
              onReset={handleReset}
              showHeader={false}
            />
          </div>
        </div>
      </div>
    </aside>
  );
}
