import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select";
import {
  DEFAULT_LISTING_SORT,
  LISTING_SORT_OPTIONS,
} from "@/utils/listingFilters";

export default function ListingSortSelect({
  value = DEFAULT_LISTING_SORT,
  onValueChange,
  disabled = false,
}) {
  return (
    <Select value={value} onValueChange={onValueChange} disabled={disabled}>
      <SelectTrigger
        aria-label="Sort listings"
        className="min-w-[11.5rem] border-surface-4 bg-surface-1"
      >
        <SelectValue placeholder="Sort by" />
      </SelectTrigger>
      <SelectContent align="end">
        {LISTING_SORT_OPTIONS.map((option) => (
          <SelectItem key={option.value} value={option.value}>
            {option.label}
          </SelectItem>
        ))}
      </SelectContent>
    </Select>
  );
}
