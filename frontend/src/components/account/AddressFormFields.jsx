import { Field, FieldGroup, FieldLabel } from "@/components/ui/field";
import { Input } from "@/components/ui/input";

const SELECT_CLASS =
  "h-9 w-full min-w-0 rounded-lg border border-accent-muted bg-surface-form px-2.5 py-1 text-sm text-on-surface transition-colors outline-none focus-visible:border-brand-active focus-visible:ring-3 focus-visible:ring-brand-active/50";

export const EMPTY_ADDRESS_FORM = {
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

export function toAddressForm(address) {
  if (!address) return { ...EMPTY_ADDRESS_FORM };
  return {
    label: address.label ?? "",
    fullName: address.fullName ?? "",
    phone: address.phone ?? "",
    addressLine: address.addressLine ?? "",
    district: address.district ?? "",
    city: address.city ?? "",
    postalCode: address.postalCode ?? "",
    country: address.country ?? "TR",
    addressType: address.addressType ?? "SHIPPING",
  };
}

export default function AddressFormFields({
  form,
  onFieldChange,
  idPrefix = "address-",
  showAddressType = true,
  lockAddressType,
}) {
  const addressType = lockAddressType ?? form.addressType;

  return (
    <FieldGroup className="gap-4">
      <Field>
        <FieldLabel htmlFor={`${idPrefix}label`}>Label</FieldLabel>
        <Input
          id={`${idPrefix}label`}
          value={form.label}
          onChange={(event) => onFieldChange("label", event.target.value)}
          placeholder="Home, work..."
          required
        />
      </Field>

      <Field>
        <FieldLabel htmlFor={`${idPrefix}full-name`}>Full name</FieldLabel>
        <Input
          id={`${idPrefix}full-name`}
          value={form.fullName}
          onChange={(event) => onFieldChange("fullName", event.target.value)}
          required
        />
      </Field>

      <Field>
        <FieldLabel htmlFor={`${idPrefix}phone`}>Phone</FieldLabel>
        <Input
          id={`${idPrefix}phone`}
          value={form.phone}
          onChange={(event) => onFieldChange("phone", event.target.value)}
          required
        />
      </Field>

      <Field>
        <FieldLabel htmlFor={`${idPrefix}line`}>Address line</FieldLabel>
        <Input
          id={`${idPrefix}line`}
          value={form.addressLine}
          onChange={(event) => onFieldChange("addressLine", event.target.value)}
          required
        />
      </Field>

      <div className="grid gap-4 sm:grid-cols-2">
        <Field>
          <FieldLabel htmlFor={`${idPrefix}district`}>District</FieldLabel>
          <Input
            id={`${idPrefix}district`}
            value={form.district}
            onChange={(event) => onFieldChange("district", event.target.value)}
            required
          />
        </Field>

        <Field>
          <FieldLabel htmlFor={`${idPrefix}city`}>City</FieldLabel>
          <Input
            id={`${idPrefix}city`}
            value={form.city}
            onChange={(event) => onFieldChange("city", event.target.value)}
            required
          />
        </Field>
      </div>

      <div className="grid gap-4 sm:grid-cols-2">
        <Field>
          <FieldLabel htmlFor={`${idPrefix}postal`}>Postal code</FieldLabel>
          <Input
            id={`${idPrefix}postal`}
            value={form.postalCode}
            onChange={(event) => onFieldChange("postalCode", event.target.value)}
            required
          />
        </Field>

        <Field>
          <FieldLabel htmlFor={`${idPrefix}country`}>Country</FieldLabel>
          <Input
            id={`${idPrefix}country`}
            value={form.country}
            onChange={(event) => onFieldChange("country", event.target.value)}
          />
        </Field>
      </div>

      {showAddressType && !lockAddressType && (
        <Field>
          <FieldLabel htmlFor={`${idPrefix}type`}>Address type</FieldLabel>
          <select
            id={`${idPrefix}type`}
            className={SELECT_CLASS}
            value={addressType}
            onChange={(event) => onFieldChange("addressType", event.target.value)}
          >
            <option value="SHIPPING">Shipping</option>
            <option value="BILLING">Billing</option>
          </select>
        </Field>
      )}

      {lockAddressType && (
        <input type="hidden" name="addressType" value={lockAddressType} />
      )}
    </FieldGroup>
  );
}
