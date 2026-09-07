import { Checkbox } from "@/components/ui/checkbox";
import {
  Field,
  FieldLabel,
} from "@/components/ui/field";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { cn } from "@/lib/utils";
import {
  MEDIA_FORMAT_OPTIONS,
  VINYL_SIZE_OPTIONS,
  VINYL_SPEED_OPTIONS,
  VINYL_SUBTYPE_OPTIONS,
} from "@/utils/mediaInfo";

const SELECT_CLASS =
  "h-8 w-full min-w-0 rounded-lg border border-accent-muted bg-surface-form px-2.5 py-1 text-base text-on-surface transition-colors outline-none focus-visible:border-brand-active focus-visible:ring-3 focus-visible:ring-brand-active/50 disabled:pointer-events-none disabled:cursor-not-allowed disabled:bg-surface-form/50 disabled:opacity-50 md:text-sm";

function RadioGroup({
  name,
  value,
  onChange,
  options,
  required = false,
  layout = "wrap",
}) {
  return (
    <div
      className={cn(
        layout === "column"
          ? "flex flex-col gap-2"
          : "flex flex-wrap gap-x-4 gap-y-2",
      )}
    >
      {options.map(({ value: optionValue, label }) => (
        <label
          key={optionValue}
          className="inline-flex cursor-pointer items-center gap-2 text-sm text-on-surface"
        >
          <input
            type="radio"
            name={name}
            value={optionValue}
            checked={value === optionValue}
            onChange={onChange}
            required={required && !value}
            className="size-4 accent-brand"
          />
          {label}
        </label>
      ))}
    </div>
  );
}

export default function MediaInfoFields({ mediaInfo, onChange }) {
  const updateMediaInfo = (patch) => {
    onChange({ ...mediaInfo, ...patch });
  };

  const handleFormatChange = (event) => {
    const format = event.target.value;
    onChange({
      format,
      vinylSubtype: format === "VINYL" ? mediaInfo.vinylSubtype || "LP" : "",
      speedRpm: format === "VINYL" ? mediaInfo.speedRpm || 33 : "",
      vinylSize: format === "VINYL" ? mediaInfo.vinylSize || '12"' : "",
      discCount:
        format === "VINYL" || format === "CD"
          ? mediaInfo.discCount || 1
          : 1,
      colored: format === "VINYL" ? mediaInfo.colored : false,
      pictureDisc: format === "VINYL" ? mediaInfo.pictureDisc : false,
      sourceFormatRaw: mediaInfo.sourceFormatRaw || "",
    });
  };

  return (
    <div className="space-y-4">
      <Field>
        <FieldLabel>Format</FieldLabel>
        <RadioGroup
          name="mediaFormat"
          value={mediaInfo.format}
          onChange={handleFormatChange}
          options={MEDIA_FORMAT_OPTIONS}
          layout="column"
          required
        />
      </Field>

      {mediaInfo.format === "VINYL" && (
        <div className="space-y-4 border-l-2 border-surface-4 pl-4">
          <Field>
            <FieldLabel>Vinyl type</FieldLabel>
            <RadioGroup
              name="vinylSubtype"
              value={mediaInfo.vinylSubtype}
              onChange={(event) =>
                updateMediaInfo({ vinylSubtype: event.target.value })
              }
              options={VINYL_SUBTYPE_OPTIONS}
              layout="column"
              required
            />
          </Field>

          <div className="grid gap-4 sm:grid-cols-2">
            <Field>
              <FieldLabel htmlFor="speedRpm">Speed</FieldLabel>
              <select
                id="speedRpm"
                name="speedRpm"
                value={mediaInfo.speedRpm || ""}
                onChange={(event) =>
                  updateMediaInfo({
                    speedRpm: event.target.value
                      ? Number(event.target.value)
                      : "",
                  })
                }
                required
                className={SELECT_CLASS}
              >
                <option value="">Select speed</option>
                {VINYL_SPEED_OPTIONS.map(({ value, label }) => (
                  <option key={value} value={value}>
                    {label}
                  </option>
                ))}
              </select>
            </Field>

            <Field>
              <FieldLabel htmlFor="vinylSize">Size</FieldLabel>
              <select
                id="vinylSize"
                name="vinylSize"
                value={mediaInfo.vinylSize || ""}
                onChange={(event) =>
                  updateMediaInfo({ vinylSize: event.target.value })
                }
                required
                className={SELECT_CLASS}
              >
                <option value="">Select size</option>
                {VINYL_SIZE_OPTIONS.map(({ value, label }) => (
                  <option key={value} value={value}>
                    {label}
                  </option>
                ))}
              </select>
            </Field>
          </div>

          <Field>
            <FieldLabel htmlFor="discCount">Disc count</FieldLabel>
            <Input
              id="discCount"
              type="number"
              name="discCount"
              min={1}
              value={mediaInfo.discCount ?? 1}
              onChange={(event) =>
                updateMediaInfo({
                  discCount: Number.parseInt(event.target.value, 10) || 1,
                })
              }
            />
          </Field>

          <div className="space-y-2">
            <div className="flex items-center gap-2">
              <Checkbox
                id="colored"
                checked={mediaInfo.colored === true}
                onCheckedChange={(checked) =>
                  updateMediaInfo({ colored: checked === true })
                }
              />
              <Label htmlFor="colored" className="font-normal">
                Colored vinyl
              </Label>
            </div>
            <div className="flex items-center gap-2">
              <Checkbox
                id="pictureDisc"
                checked={mediaInfo.pictureDisc === true}
                onCheckedChange={(checked) =>
                  updateMediaInfo({ pictureDisc: checked === true })
                }
              />
              <Label htmlFor="pictureDisc" className="font-normal">
                Picture disc
              </Label>
            </div>
          </div>
        </div>
      )}

      {mediaInfo.format === "CD" && (
        <Field>
          <FieldLabel htmlFor="cdDiscCount">Disc count</FieldLabel>
          <Input
            id="cdDiscCount"
            type="number"
            name="discCount"
            min={1}
            value={mediaInfo.discCount ?? 1}
            onChange={(event) =>
              updateMediaInfo({
                discCount: Number.parseInt(event.target.value, 10) || 1,
              })
            }
          />
        </Field>
      )}
    </div>
  );
}
