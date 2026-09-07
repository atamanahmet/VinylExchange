import { useState } from "react";
import { Pencil, Plus, Star, Trash2 } from "lucide-react";
import { toast } from "sonner";

import { Button } from "@/components/ui/button";
import {
  Card,
  CardContent,
  CardFooter,
  CardHeader,
  CardTitle,
} from "@/components/ui/card";
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from "@/components/ui/dialog";
import { cn } from "@/lib/utils";
import AddressFormDialog from "./AddressFormDialog";
import { useAccountAddresses } from "@/hooks/useAccountAddresses";

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

function toPayload(address, overrides = {}) {
  return {
    label: address.label,
    fullName: address.fullName,
    phone: address.phone,
    addressLine: address.addressLine,
    district: address.district,
    city: address.city,
    postalCode: address.postalCode,
    country: address.country?.trim() || "TR",
    addressType: address.addressType,
    isDefault: address.isDefault,
    ...overrides,
  };
}

export default function AccountAddressesSection() {
  const {
    addresses,
    isFetching,
    isSaving,
    createAddress,
    updateAddress,
    deleteAddress,
  } = useAccountAddresses();

  const [formOpen, setFormOpen] = useState(false);
  const [editingAddress, setEditingAddress] = useState(null);
  const [deleteTarget, setDeleteTarget] = useState(null);
  const [settingDefaultId, setSettingDefaultId] = useState(null);

  const openCreate = () => {
    setEditingAddress(null);
    setFormOpen(true);
  };

  const openEdit = (address) => {
    setEditingAddress(address);
    setFormOpen(true);
  };

  const handleSubmit = async (form) => {
    const payload = {
      ...form,
      country: form.country?.trim() || "TR",
      isDefault: editingAddress?.isDefault ?? false,
    };

    const result = editingAddress
      ? await updateAddress(editingAddress.id, payload)
      : await createAddress(payload);

    if (!result.success) {
      toast.error(result.message || "Could not save address.");
      return;
    }

    toast.success(editingAddress ? "Address updated." : "Address added.");
    setFormOpen(false);
    setEditingAddress(null);
  };

  const handleSetDefault = async (address) => {
    if (address.isDefault) return;

    setSettingDefaultId(address.id);
    const result = await updateAddress(address.id, toPayload(address, { isDefault: true }));
    setSettingDefaultId(null);

    if (!result.success) {
      toast.error(result.message || "Could not set default address.");
      return;
    }

    toast.success("Default address updated.");
  };

  const handleDeleteConfirm = async () => {
    if (!deleteTarget) return;

    const result = await deleteAddress(deleteTarget.id);
    if (!result.success) {
      toast.error(result.message || "Could not delete address.");
      return;
    }

    toast.success("Address removed.");
    setDeleteTarget(null);
  };

  return (
    <section>
      <div className="flex items-center justify-between gap-3 mb-4">
        <h2 className="text-lg font-semibold text-on-surface">Your addresses</h2>
        <Button type="button" size="sm" onClick={openCreate}>
          <Plus className="size-4" />
          Add address
        </Button>
      </div>

      {isFetching ? (
        <div className="flex flex-col gap-3">
          {Array.from({ length: 2 }).map((_, index) => (
            <div
              key={index}
              className="h-36 rounded-xl border border-surface-4 bg-surface-2 animate-pulse"
            />
          ))}
        </div>
      ) : addresses.length === 0 ? (
        <Card className="border-surface-4">
          <CardContent className="py-12 text-center text-sm text-on-surface-muted">
            No saved addresses yet.
          </CardContent>
        </Card>
      ) : (
        <div className="grid gap-4 sm:grid-cols-2">
          {addresses.map((address) => (
            <Card
              key={address.id}
              className={cn(
                "border-surface-4 shadow-sm",
                address.isDefault && "ring-2 ring-brand/40 border-brand/30",
              )}
            >
              <CardHeader className="border-b border-surface-4/70 pb-3">
                <div className="flex items-start justify-between gap-3">
                  <div>
                    <CardTitle className="text-base">{address.label}</CardTitle>
                    <p className="text-xs text-on-surface-muted mt-1">
                      {address.addressType === "BILLING" ? "Billing" : "Shipping"}
                    </p>
                  </div>
                  {address.isDefault && (
                    <span className="inline-flex items-center gap-1 text-xs px-2 py-1 rounded-full bg-brand/20 text-brand border border-brand/30">
                      <Star className="size-3 fill-current" />
                      Default
                    </span>
                  )}
                </div>
              </CardHeader>

              <CardContent className="space-y-1 pt-4">
                <p className="text-sm font-medium text-on-surface">{address.fullName}</p>
                <p className="text-sm text-on-surface-muted">{formatAddress(address)}</p>
                <p className="text-sm text-on-surface-muted">{address.phone}</p>
              </CardContent>

              <CardFooter className="flex flex-wrap items-center gap-2 border-t border-surface-4/70">
                {address.isDefault ? (
                  <span className="text-xs font-medium text-brand">Default address</span>
                ) : (
                  <Button
                    type="button"
                    variant="outline"
                    size="sm"
                    disabled={isSaving && settingDefaultId === address.id}
                    onClick={() => handleSetDefault(address)}
                  >
                    {isSaving && settingDefaultId === address.id
                      ? "Saving..."
                      : "Set as default"}
                  </Button>
                )}

                <div className="flex items-center gap-1 ml-auto">
                  <Button
                    type="button"
                    variant="ghost"
                    size="icon-sm"
                    aria-label="Edit address"
                    onClick={() => openEdit(address)}
                  >
                    <Pencil className="size-4" />
                  </Button>
                  <Button
                    type="button"
                    variant="ghost"
                    size="icon-sm"
                    aria-label="Delete address"
                    onClick={() => setDeleteTarget(address)}
                  >
                    <Trash2 className="size-4" />
                  </Button>
                </div>
              </CardFooter>
            </Card>
          ))}
        </div>
      )}

      <AddressFormDialog
        open={formOpen}
        onOpenChange={setFormOpen}
        initialAddress={editingAddress}
        isSaving={isSaving}
        onSubmit={handleSubmit}
      />

      <Dialog open={Boolean(deleteTarget)} onOpenChange={() => setDeleteTarget(null)}>
        <DialogContent className="bg-surface-1 text-on-surface sm:max-w-md">
          <DialogHeader>
            <DialogTitle>Remove address</DialogTitle>
            <DialogDescription className="text-on-surface-muted">
              Delete {deleteTarget?.label}? This cannot be undone.
            </DialogDescription>
          </DialogHeader>
          <DialogFooter>
            <Button type="button" variant="outline" onClick={() => setDeleteTarget(null)}>
              Cancel
            </Button>
            <Button type="button" variant="destructive" onClick={handleDeleteConfirm}>
              Delete
            </Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>
    </section>
  );
}
