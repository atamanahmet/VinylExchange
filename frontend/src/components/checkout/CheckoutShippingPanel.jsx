import { useEffect, useState } from "react";
import { toast } from "sonner";

import { useCheckoutShippingAddresses } from "@/hooks/useCheckoutShippingAddresses";
import { useCartStore } from "@/stores/cartStore";
import CheckoutAddressForm from "./CheckoutAddressForm";
import CheckoutAddressList from "./CheckoutAddressList";

export default function CheckoutShippingPanel() {
  const { shippingAddresses, isFetching, isSaving, createAddress } =
    useCheckoutShippingAddresses();
  const checkout = useCartStore((state) => state.checkout);

  const [selectedAddressId, setSelectedAddressId] = useState(null);
  const [showAddForm, setShowAddForm] = useState(false);
  const [isCheckingOut, setIsCheckingOut] = useState(false);

  useEffect(() => {
    if (isFetching) return;

    if (shippingAddresses.length === 0) {
      setShowAddForm(true);
      setSelectedAddressId(null);
      return;
    }

    const defaultAddress = shippingAddresses.find((address) => address.isDefault);
    setSelectedAddressId(defaultAddress?.id ?? null);
  }, [isFetching, shippingAddresses]);

  const handleSelect = (addressId) => {
    setSelectedAddressId(addressId);
    setShowAddForm(false);
  };

  const handleAddNew = () => {
    setShowAddForm(true);
    setSelectedAddressId(null);
  };

  const handleCreateAddress = async (payload) => {
    const result = await createAddress(payload);

    if (!result.success) {
      toast.error(result.message || "Could not save address.");
      return;
    }

    toast.success("Address added.");
    setSelectedAddressId(result.data.id);
    setShowAddForm(false);
  };

  const handleContinue = async () => {
    if (!selectedAddressId) return;

    setIsCheckingOut(true);
    const success = await checkout(selectedAddressId);
    setIsCheckingOut(false);

    if (!success) {
      toast.error("Checkout failed. Please try again.");
    }
  };

  return (
    <div className="mx-auto max-w-3xl space-y-6">
      {isFetching ? (
        <div className="flex flex-col gap-3">
          {Array.from({ length: 2 }).map((_, index) => (
            <div
              key={index}
              className="h-36 rounded-xl border border-surface-4 bg-surface-2 animate-pulse"
            />
          ))}
        </div>
      ) : (
        <>
          {shippingAddresses.length > 0 && (
            <CheckoutAddressList
              addresses={shippingAddresses}
              selectedAddressId={selectedAddressId}
              onSelect={handleSelect}
              onAddNew={handleAddNew}
              showAddForm={showAddForm}
            />
          )}

          {showAddForm && (
            <CheckoutAddressForm
              shippingAddressCount={shippingAddresses.length}
              isSaving={isSaving}
              onSubmit={handleCreateAddress}
              onCancel={() => setShowAddForm(false)}
              showCancel={shippingAddresses.length > 0}
            />
          )}
        </>
      )}

      <div className="rounded-lg border border-surface-3 bg-surface-1 p-4 ring-1 ring-surface-3 sm:p-6">
        <button
          type="button"
          onClick={handleContinue}
          disabled={!selectedAddressId || isCheckingOut || isFetching}
          className="flex w-full items-center justify-center rounded-lg bg-brand px-5 py-2.5 text-sm font-medium text-on-surface transition-colors hover:bg-brand-hover focus:outline-none focus-visible:ring-4 focus-visible:ring-brand/40 disabled:pointer-events-none disabled:opacity-50"
        >
          {isCheckingOut ? "Processing..." : "Continue to payment"}
        </button>
      </div>
    </div>
  );
}
