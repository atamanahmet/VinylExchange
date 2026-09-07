import { useState } from "react";
import { ChevronDown, SlidersHorizontal } from "lucide-react";

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
import { cn } from "@/lib/utils";
import {
  countActiveMbFilters,
  resetMbFilters,
} from "@/utils/mbReleaseFilters";

function optionId(prefix, value) {
  return `${prefix}-${value.replace(/[^a-zA-Z0-9]+/g, "-")}`;
}

function FilterGroup({ title, children, className }) {
  return (
    <div className={cn("min-w-0 space-y-2", className)}>
      <p className="text-xs font-medium uppercase tracking-wide text-on-surface-muted">
        {title}
      </p>
      {children}
    </div>
  );
}

function CheckboxList({ options, selected, onToggle, idPrefix, maxHeight }) {
  return (
    <div
      className={cn("space-y-2 overflow-y-auto overscroll-contain pr-1", maxHeight)}
    >
      {options.map((option) => (
        <div key={option} className="flex items-start gap-2">
          <Checkbox
            id={optionId(idPrefix, option)}
            checked={selected.includes(option)}
            onCheckedChange={() => onToggle(option)}
            className="mt-0.5"
          />
          <Label
            htmlFor={optionId(idPrefix, option)}
            className="font-normal leading-snug text-on-surface"
          >
            {option}
          </Label>
        </div>
      ))}
    </div>
  );
}

export default function MbReleaseFilterBar({
  filters,
  bounds,
  onFiltersChange,
  resultCount,
  totalCount,
  hasMoreMbResults = false,
  className,
}) {
  const [open, setOpen] = useState(false);
  const activeCount = countActiveMbFilters(filters, bounds);

  const toggleArrayValue = (key, value) => {
    const current = filters[key] ?? [];
    const next = current.includes(value)
      ? current.filter((item) => item !== value)
      : [...current, value];
    onFiltersChange({ ...filters, [key]: next });
  };

  const handleReset = () => {
    onFiltersChange(resetMbFilters(bounds));
  };

  return (
    <Collapsible
      open={open}
      onOpenChange={setOpen}
      className={cn("border-b border-surface-3 bg-surface-2/30", className)}
    >
      <div className="flex flex-wrap items-center justify-between gap-2 px-5 py-3 sm:px-6">
        <CollapsibleTrigger className="flex min-w-0 flex-1 items-center gap-2 text-left text-sm font-medium text-on-surface transition-colors hover:text-brand-fg">
          <SlidersHorizontal className="size-4 shrink-0 text-on-surface-muted" />
          <span>Filters</span>
          {activeCount > 0 && (
            <span className="rounded-full bg-brand px-1.5 py-0.5 text-[10px] font-medium text-on-surface">
              {activeCount}
            </span>
          )}
          <ChevronDown
            className={cn(
              "size-4 shrink-0 text-on-surface-muted transition-transform duration-100",
              open && "rotate-180",
            )}
          />
        </CollapsibleTrigger>

        <div className="flex items-center gap-3">
          <p className="text-xs text-on-surface-muted sm:text-sm">
            {resultCount} of {totalCount}
            {hasMoreMbResults ? "+" : ""} loaded
          </p>
          {activeCount > 0 && (
            <Button type="button" variant="ghost" size="sm" onClick={handleReset}>
              Reset
            </Button>
          )}
        </div>
      </div>

      <CollapsibleContent className="px-5 pb-4 sm:px-6">
        <Separator className="mb-4" />

        <div className="grid gap-5 md:grid-cols-2 lg:grid-cols-3 xl:grid-cols-5">
          {bounds.artists.length > 0 && (
            <FilterGroup title="Artist">
              <CheckboxList
                options={bounds.artists}
                selected={filters.artists}
                onToggle={(value) => toggleArrayValue("artists", value)}
                idPrefix="mb-artist"
                maxHeight="max-h-36"
              />
            </FilterGroup>
          )}

          {bounds.formats.length > 0 && (
            <FilterGroup title="Format">
              <CheckboxList
                options={bounds.formats}
                selected={filters.formats}
                onToggle={(value) => toggleArrayValue("formats", value)}
                idPrefix="mb-format"
                maxHeight="max-h-36"
              />
            </FilterGroup>
          )}

          {bounds.countries.length > 0 && (
            <FilterGroup title="Country">
              <CheckboxList
                options={bounds.countries}
                selected={filters.countries}
                onToggle={(value) => toggleArrayValue("countries", value)}
                idPrefix="mb-country"
                maxHeight="max-h-36"
              />
            </FilterGroup>
          )}

          {bounds.labels.length > 0 && (
            <FilterGroup title="Label">
              <CheckboxList
                options={bounds.labels}
                selected={filters.labels}
                onToggle={(value) => toggleArrayValue("labels", value)}
                idPrefix="mb-label"
                maxHeight="max-h-36"
              />
            </FilterGroup>
          )}

          <FilterGroup title="Year" className="lg:col-span-1 xl:col-span-1">
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
              <div className="flex justify-between text-xs text-on-surface-muted">
                <span>{filters.yearRange?.[0]}</span>
                <span>{filters.yearRange?.[1]}</span>
              </div>
            </div>
          </FilterGroup>
        </div>

        <div className="mt-4 flex items-center gap-2 border-t border-surface-3 pt-4">
          <Checkbox
            id="mb-has-barcode"
            checked={filters.hasBarcode}
            onCheckedChange={(checked) =>
              onFiltersChange({ ...filters, hasBarcode: checked === true })
            }
          />
          <Label htmlFor="mb-has-barcode" className="font-normal text-on-surface">
            Has barcode only
          </Label>
        </div>
      </CollapsibleContent>
    </Collapsible>
  );
}
