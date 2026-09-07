import {
  Card,
  CardContent,
  CardHeader,
  CardTitle,
} from "@/components/ui/card";
import { cn } from "@/lib/utils";

function formatAddress(address) {
  return [
    address.addressLine,
    address.district,
    address.city,
    address.postalCode,
    address.country,
  ]
    .filter(Boolean)
    .join(", ");
}

export default function CheckoutAddressOption({
  address,
  name,
  selected,
  onSelect,
}) {
  return (
    <label
      className={cn(
        "block cursor-pointer rounded-xl border border-surface-4 bg-surface-1 shadow-sm transition-colors",
        selected && "ring-2 ring-brand/40 border-brand/30",
      )}
    >
      <input
        type="radio"
        name={name}
        value={address.id}
        checked={selected}
        onChange={() => onSelect(address.id)}
        className="sr-only"
      />
      <Card className="border-0 bg-transparent shadow-none">
        <CardHeader className="border-b border-surface-4/70 pb-3">
          <CardTitle className="text-base">{address.label}</CardTitle>
        </CardHeader>
        <CardContent className="space-y-1 pt-4">
          <p className="text-sm font-medium text-on-surface">{address.fullName}</p>
          <p className="text-sm text-on-surface-muted">{formatAddress(address)}</p>
          <p className="text-sm text-on-surface-muted">{address.phone}</p>
        </CardContent>
      </Card>
    </label>
  );
}
