import { Link } from "react-router-dom";

import {
  Card as ShadcnCard,
  CardContent,
} from "@/components/ui/card";
import { cn } from "@/lib/utils";

import AdaptiveCardTitle from "./AdaptiveCardTitle";
import CardActionButtons from "./CardActionButtons";
import CardImage from "./CardImage";

const META_TEXT_CLASS =
  "min-h-[1.25rem] truncate text-xs leading-normal text-on-surface-dim @md/card:min-h-[1.375rem] @md/card:text-sm";

function CardText({ children, className, title, accent = false, centered = false }) {
  return (
    <p
      title={title ?? (typeof children === "string" ? children : undefined)}
      className={cn(
        META_TEXT_CLASS,
        accent && "text-accent-text",
        centered && "text-center",
        className,
      )}
    >
      {children}
    </p>
  );
}

export default function Card({ item, onSelect, className }) {
  const imageSrc = item.imageUrl || item.externalCoverUrl;
  const hasActions = Boolean(item.primaryAction || item.secondaryAction);
  const linkToListing = !item.disableLink && !onSelect;
  const centered = Boolean(item.textCenter);

  const country = item.country?.toString().trim() || "Country unknown";

  return (
    <ShadcnCard
      className={cn(
        "@container/card flex h-full w-full min-w-0 flex-col gap-0 overflow-hidden rounded-xl border border-surface-3 bg-surface-1 p-2.5 shadow-xs ring-0",
        "transition-[transform,width,padding] duration-100 ease-in-out @sm/card:rounded-2xl @sm/card:p-3 @lg/card:p-4",
        "hover:-translate-y-0.5 @md/card:hover:-translate-y-1",
        className,
      )}
      onClick={onSelect ? () => onSelect(item) : undefined}
      style={{ cursor: onSelect ? "pointer" : "default" }}
    >
      <CardContent className="p-0">
        {linkToListing ? (
          <Link to={`/listing/${item.id}`} className="cursor-pointer">
            <CardImage src={imageSrc} alt={item.title} />
          </Link>
        ) : (
          <CardImage src={imageSrc} alt={item.title} />
        )}
      </CardContent>

      <div
        className={cn(
          "mt-3 flex min-h-0 flex-1 flex-col space-y-2 @sm/card:space-y-2.5 @lg/card:mt-3.5",
          centered && "items-center text-center",
        )}
      >
        <AdaptiveCardTitle
          title={item.title}
          linkable={linkToListing}
          centered={centered}
        />

        <CardText accent title={item.artist} centered={centered}>
          {item.artist}
        </CardText>

        <CardText centered={centered}>{item.format || "Unknown Format"}</CardText>
        <CardText centered={centered}>{item.year || "Unknown Year"}</CardText>
        <CardText centered={centered}>{country}</CardText>
        <CardText centered={centered}>{item.label || "Unknown Label"}</CardText>
        <CardText className="hidden @lg/card:block" centered={centered}>
          {item.barcode || "Unknown Barcode"}
        </CardText>
      </div>

      <div
        className={cn(
          "mt-3 shrink-0 space-y-2.5 pt-1 @sm/card:mt-3.5 @lg/card:space-y-3",
          centered && "w-full text-center",
        )}
      >
        {item.price != null && (
          <p className="min-h-[1.5rem] text-base font-semibold leading-normal text-success-fg @sm/card:text-lg @lg/card:text-xl">
            {(item.discountedPrice ?? item.price).toLocaleString("tr-TR")} ₺
          </p>
        )}

        {hasActions && (
          <CardActionButtons
            primaryAction={item.primaryAction}
            secondaryAction={item.secondaryAction}
            stackActions={item.stackActions}
          />
        )}
      </div>
    </ShadcnCard>
  );
}
