import { useEffect, useMemo, useState } from "react";

import { Checkbox } from "@/components/ui/checkbox";
import { Label } from "@/components/ui/label";
import { cn } from "@/lib/utils";

const VISIBLE_COUNTRY_COUNT = 6;

function countryFilterId(country) {
  return `country-${country.replace(/[^a-zA-Z0-9]+/g, "-")}`;
}

function CountryCheckbox({ option, selected, onToggle, idPrefix = "country" }) {
  const id = countryFilterId(`${idPrefix}-${option.value}`);

  return (
    <div className="flex items-start gap-2">
      <Checkbox
        id={id}
        className="mt-0.5"
        checked={selected.includes(option.value)}
        onCheckedChange={() => onToggle(option.value)}
      />
      <Label htmlFor={id} className="min-w-0 flex-1 items-start text-left font-normal leading-snug">
        {option.label}
      </Label>
    </div>
  );
}

function ToggleButton({ expanded, onClick, className }) {
  return (
    <button
      type="button"
      onClick={onClick}
      className={cn(
        "shrink-0 self-start py-0 text-sm leading-snug text-muted-foreground transition-colors hover:text-foreground",
        className,
      )}
    >
      {expanded ? "…less" : "…more"}
    </button>
  );
}

export default function CountryFilterOptions({
  options,
  selected = [],
  onToggle,
  loading = false,
  idPrefix = "country",
  className,
}) {
  const { visible, hidden } = useMemo(
    () => ({
      visible: options.slice(0, VISIBLE_COUNTRY_COUNT),
      hidden: options.slice(VISIBLE_COUNTRY_COUNT),
    }),
    [options],
  );

  const hasSelectedHidden = useMemo(
    () => hidden.some((option) => selected.includes(option.value)),
    [hidden, selected],
  );

  const [showMore, setShowMore] = useState(hasSelectedHidden);

  useEffect(() => {
    if (hasSelectedHidden) {
      setShowMore(true);
    }
  }, [hasSelectedHidden]);

  if (loading) {
    return <p className="text-sm text-muted-foreground">Loading countries...</p>;
  }

  if (!options.length) {
    return null;
  }

  const expandedOptions = showMore ? [...visible, ...hidden] : visible;

  return (
    <div className={cn("flex flex-col gap-2", className)}>
      <div
        className={cn(
          "space-y-2",
          showMore &&
            hidden.length > 0 &&
            "max-h-64 overflow-y-auto overscroll-contain pr-1",
        )}
      >
        {expandedOptions.map((option) => (
          <CountryCheckbox
            key={option.value}
            option={option}
            selected={selected}
            onToggle={onToggle}
            idPrefix={idPrefix}
          />
        ))}
      </div>

      {hidden.length > 0 && (
        <ToggleButton
          expanded={showMore}
          onClick={() => setShowMore((open) => !open)}
        />
      )}
    </div>
  );
}
