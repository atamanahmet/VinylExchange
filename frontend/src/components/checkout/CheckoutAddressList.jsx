import { Plus } from "lucide-react";

import { Button } from "@/components/ui/button";
import CheckoutAddressOption from "./CheckoutAddressOption";

export default function CheckoutAddressList({
  addresses,
  selectedAddressId,
  onSelect,
  onAddNew,
  showAddForm,
}) {
  return (
    <div className="space-y-4" role="radiogroup" aria-label="Shipping addresses">
      <div className="grid gap-4 sm:grid-cols-2">
        {addresses.map((address) => (
          <CheckoutAddressOption
            key={address.id}
            address={address}
            name="checkout-shipping-address"
            selected={selectedAddressId === address.id}
            onSelect={onSelect}
          />
        ))}
      </div>

      {!showAddForm && (
        <Button type="button" variant="outline" size="sm" onClick={onAddNew}>
          <Plus className="size-4" />
          Add new address
        </Button>
      )}
    </div>
  );
}
