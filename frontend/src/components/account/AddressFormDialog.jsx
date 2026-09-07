import { useEffect, useState } from "react";

import { Button } from "@/components/ui/button";
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from "@/components/ui/dialog";
import AddressFormFields, { toAddressForm } from "./AddressFormFields";

export default function AddressFormDialog({
  open,
  onOpenChange,
  initialAddress,
  isSaving,
  onSubmit,
  title,
  description,
  lockAddressType,
  submitLabel,
}) {
  const [form, setForm] = useState(() => toAddressForm(initialAddress));

  useEffect(() => {
    if (open) {
      setForm(toAddressForm(initialAddress));
    }
  }, [open, initialAddress]);

  const updateField = (field, value) => {
    setForm((current) => ({ ...current, [field]: value }));
  };

  const handleSubmit = async (event) => {
    event.preventDefault();
    await onSubmit({
      ...form,
      addressType: lockAddressType ?? form.addressType,
    });
  };

  const dialogTitle =
    title ?? (initialAddress ? "Edit address" : "Add address");
  const dialogDescription =
    description ?? "Saved addresses can be used at checkout.";
  const saveLabel =
    submitLabel ?? (initialAddress ? "Save changes" : "Add address");

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent className="bg-surface-1 text-on-surface sm:max-w-lg max-h-[90vh] overflow-y-auto">
        <DialogHeader>
          <DialogTitle>{dialogTitle}</DialogTitle>
          <DialogDescription className="text-on-surface-muted">
            {dialogDescription}
          </DialogDescription>
        </DialogHeader>

        <form onSubmit={handleSubmit}>
          <AddressFormFields
            form={form}
            onFieldChange={updateField}
            showAddressType={!lockAddressType}
            lockAddressType={lockAddressType}
          />

          <DialogFooter className="mt-6">
            <Button
              type="button"
              variant="outline"
              onClick={() => onOpenChange(false)}
              disabled={isSaving}
            >
              Cancel
            </Button>
            <Button type="submit" disabled={isSaving}>
              {isSaving ? "Saving..." : saveLabel}
            </Button>
          </DialogFooter>
        </form>
      </DialogContent>
    </Dialog>
  );
}
