import { Link, useNavigate } from "react-router-dom";

import { cn } from "@/lib/utils";
import { buildListingPath } from "@/utils/listingPath";
import {
  LIST_VIEW_ACTIONS_CELL,
  LIST_VIEW_CELL,
  LIST_VIEW_COVER_CELL,
  listViewRowClass,
} from "@/utils/listViewLayout";

import CardActionButtons from "./CardActionButtons";
import CardImage from "./CardImage";

export default function ListView({ item }) {
  const navigate = useNavigate();

  const linkToListing = !item.disableLink;
  const listingPath = buildListingPath(item);

  const navigateItemWithId = () => {
    if (linkToListing) {
      navigate(listingPath);
    }
  };

  const imageSrc = item.imageUrl || item.externalCoverUrl;
  const hasActions = Boolean(item.primaryAction || item.secondaryAction);
  const showPrice = item.price != null;
  const displayPrice = item.discount > 0 ? item.discountedPrice : item.price;

  const coverCell = linkToListing ? (
    <Link
      to={listingPath}
      className="size-24 shrink-0 overflow-hidden rounded-md"
    >
      <CardImage src={imageSrc} alt={item.title} />
    </Link>
  ) : (
    <div className="size-24 shrink-0 overflow-hidden rounded-md">
      <CardImage src={imageSrc} alt={item.title} />
    </div>
  );

  const mobileCoverCell = linkToListing ? (
    <Link
      to={listingPath}
      className="size-20 shrink-0 overflow-hidden rounded-md"
    >
      <CardImage src={imageSrc} alt={item.title} />
    </Link>
  ) : (
    <div className="size-20 shrink-0 overflow-hidden rounded-md">
      <CardImage src={imageSrc} alt={item.title} />
    </div>
  );

  const titleCell = linkToListing ? (
    <button
      type="button"
      onClick={navigateItemWithId}
      className="min-w-0 text-left"
    >
      <p className="line-clamp-2 font-medium text-on-surface">{item.title}</p>
    </button>
  ) : (
    <div className="min-w-0 text-left">
      <p className="line-clamp-2 font-medium text-on-surface">{item.title}</p>
    </div>
  );

  const mobileTitleCell = linkToListing ? (
    <button
      type="button"
      onClick={navigateItemWithId}
      className="line-clamp-2 text-left font-medium text-on-surface hover:text-brand-fg"
    >
      {item.title}
    </button>
  ) : (
    <p className="line-clamp-2 text-left font-medium text-on-surface">
      {item.title}
    </p>
  );

  return (
    <>
      <article className="border-b border-surface-3 bg-surface-1 p-4 lg:hidden">
        <div className="flex gap-3">
          {mobileCoverCell}

          <div className="min-w-0 flex-1 space-y-1 text-left">
            {mobileTitleCell}
            <p className="truncate text-sm text-accent-text">{item.artist}</p>
            <p className="text-xs text-on-surface-muted">
              {[item.year || item.date, item.format, item.country]
                .filter(Boolean)
                .join(" · ")}
            </p>
            {displayPrice != null && (
              <p className="text-sm font-semibold text-success-fg">
                {displayPrice.toLocaleString("tr-TR")} ₺
              </p>
            )}
          </div>
        </div>

        {hasActions && (
          <div className="mt-3 w-full min-w-0">
            <CardActionButtons
              layout="list"
              primaryAction={item.primaryAction}
              secondaryAction={item.secondaryAction}
              stackActions={item.stackActions}
            />
          </div>
        )}
      </article>

      <article
        className={listViewRowClass({ showPrice, showActions: hasActions })}
      >
        <div className={LIST_VIEW_COVER_CELL}>{coverCell}</div>

        <div className={LIST_VIEW_CELL}>{titleCell}</div>

        <p className={cn(LIST_VIEW_CELL, "truncate text-accent-text")}>
          {item.artist}
        </p>

        <p className={cn(LIST_VIEW_CELL, "text-on-surface-dim")}>
          {item.year || item.date}
        </p>

        <p className={cn(LIST_VIEW_CELL, "truncate text-on-surface-dim")}>
          {item.format || "—"}
        </p>

        <p className={cn(LIST_VIEW_CELL, "truncate text-on-surface-dim")}>
          {item.country || "—"}
        </p>

        {showPrice && (
          <div className={LIST_VIEW_CELL}>
            {item.discount > 0 && (
              <p className="text-sm text-on-surface-muted line-through">
                {item.price.toLocaleString("tr-TR")} ₺
              </p>
            )}
            {displayPrice != null && (
              <p className="font-semibold text-success-fg">
                {displayPrice.toLocaleString("tr-TR")} ₺
              </p>
            )}
          </div>
        )}

        {hasActions && (
          <div className={LIST_VIEW_ACTIONS_CELL}>
            <CardActionButtons
              layout="list"
              primaryAction={item.primaryAction}
              secondaryAction={item.secondaryAction}
              stackActions={item.stackActions}
            />
          </div>
        )}
      </article>
    </>
  );
}
