import { useState } from "react";

import { Button } from "@/components/ui/button";
import { Field, FieldGroup, FieldLabel } from "@/components/ui/field";
import { Input } from "@/components/ui/input";

const EMPTY_FORM = {
  label: "",
  fullName: "",
  phone: "",
  addressLine: "",
  district: "",
  city: "",
  postalCode: "",
  country: "TR",
  addressType: "SHIPPING",
};

export default function CheckoutAddressForm({
  shippingAddressCount,
  isSaving,
  onSubmit,
  onCancel,
  showCancel,
}) {
  const [form, setForm] = useState(() => ({ ...EMPTY_FORM }));

  const updateField = (field, value) => {
    setForm((current) => ({ ...current, [field]: value }));
  };

  const handleSubmit = async (event) => {
    event.preventDefault();
    await onSubmit({
      ...form,
      country: form.country?.trim() || "TR",
      addressType: "SHIPPING",
      isDefault: shippingAddressCount === 0,
    });
  };

  return (
    <form
      onSubmit={handleSubmit}
      className="space-y-4 rounded-lg border border-surface-3 bg-surface-1 p-4 ring-1 ring-surface-3 sm:p-6"
    >
      <p className="text-sm font-medium text-on-surface">Add shipping address</p>

      <FieldGroup className="gap-4">
        <Field>
          <FieldLabel htmlFor="checkout-address-label">Label</FieldLabel>
          <Input
            id="checkout-address-label"
            value={form.label}
            onChange={(event) => updateField("label", event.target.value)}
            placeholder="Home, work..."
            required
          />
        </Field>

        <Field>
          <FieldLabel htmlFor="checkout-address-full-name">Full name</FieldLabel>
          <Input
            id="checkout-address-full-name"
            value={form.fullName}
            onChange={(event) => updateField("fullName", event.target.value)}
            required
          />
        </Field>

        <Field>
          <FieldLabel htmlFor="checkout-address-phone">Phone</FieldLabel>
          <Input
            id="checkout-address-phone"
            value={form.phone}
            onChange={(event) => updateField("phone", event.target.value)}
            required
          />
        </Field>

        <Field>
          <FieldLabel htmlFor="checkout-address-line">Address line</FieldLabel>
          <Input
            id="checkout-address-line"
            value={form.addressLine}
            onChange={(event) => updateField("addressLine", event.target.value)}
            required
          />
        </Field>

        <div className="grid gap-4 sm:grid-cols-2">
          <Field>
            <FieldLabel htmlFor="checkout-address-district">District</FieldLabel>
            <Input
              id="checkout-address-district"
              value={form.district}
              onChange={(event) => updateField("district", event.target.value)}
              required
            />
          </Field>

          <Field>
            <FieldLabel htmlFor="checkout-address-city">City</FieldLabel>
            <Input
              id="checkout-address-city"
              value={form.city}
              onChange={(event) => updateField("city", event.target.value)}
              required
            />
          </Field>
        </div>

        <div className="grid gap-4 sm:grid-cols-2">
          <Field>
            <FieldLabel htmlFor="checkout-address-postal">Postal code</FieldLabel>
            <Input
              id="checkout-address-postal"
              value={form.postalCode}
              onChange={(event) => updateField("postalCode", event.target.value)}
              required
            />
          </Field>

          <Field>
            <FieldLabel htmlFor="checkout-address-country">Country</FieldLabel>
            <Input
              id="checkout-address-country"
              value={form.country}
              onChange={(event) => updateField("country", event.target.value)}
            />
          </Field>
        </div>
      </FieldGroup>

      <div className="flex flex-wrap items-center gap-2">
        {showCancel && (
          <Button type="button" variant="outline" onClick={onCancel} disabled={isSaving}>
            Cancel
          </Button>
        )}
        <Button type="submit" disabled={isSaving}>
          {isSaving ? "Saving..." : "Save address"}
        </Button>
      </div>
    </form>
  );
}
