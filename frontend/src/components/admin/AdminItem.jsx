import { useNavigate } from "react-router-dom";

import { cn } from "@/lib/utils";

const ADMIN_GRID =
  "grid grid-cols-[5rem_minmax(0,1.4fr)_minmax(0,0.7fr)_minmax(0,0.7fr)_minmax(0,0.8fr)_minmax(0,0.7fr)_9rem] items-center";

export const ADMIN_HEADER_CELL = "min-w-0 px-3 py-3 text-center";

export function AdminListHeader() {
  return (
    <div
      className={cn(
        ADMIN_GRID,
        "hidden border-b border-surface-3 bg-surface-2 text-center text-xs font-medium uppercase tracking-wide text-on-surface-muted lg:grid",
      )}
    >
      <p className={ADMIN_HEADER_CELL}>Cover</p>
      <p className={ADMIN_HEADER_CELL}>Title</p>
      <p className={ADMIN_HEADER_CELL}>Release</p>
      <p className={ADMIN_HEADER_CELL}>Format</p>
      <p className={ADMIN_HEADER_CELL}>Price</p>
      <p className={ADMIN_HEADER_CELL}>Promoted</p>
      <div className={ADMIN_HEADER_CELL} aria-hidden="true" />
    </div>
  );
}

export default function AdminItem({
  item,
  onDelete,
  handlePromote,
  handleFreeze,
}) {
  const navigate = useNavigate();

  const navigateItemWithId = () => navigate(`/listing/${item.id}`);

  const handleImageError = (event) => {
    event.target.src = "/placeholder.png";
  };

  const price = item.price?.toLocaleString("tr-TR");
  const discountedPrice = item.discountedPrice?.toLocaleString("tr-TR");
  const hasDiscount = item.discount > 0;

  const cover = (
    <button
      type="button"
      onClick={navigateItemWithId}
      className="size-16 shrink-0 overflow-hidden rounded-md bg-surface-2"
    >
      <img
        src={item.imagePaths?.[0]}
        onError={handleImageError}
        alt={item.title || "listing main image"}
        className="size-full object-cover"
      />
    </button>
  );

  const priceCell = (
    <div className="min-w-0">
      <p
        className={cn(
          "text-sm font-semibold",
          hasDiscount
            ? "text-on-surface-muted line-through"
            : "text-on-surface",
        )}
      >
        {price != null ? `${price} ₺` : "—"}
      </p>
      {hasDiscount && discountedPrice != null && (
        <p className="text-sm font-semibold text-success-fg">
          {discountedPrice} ₺
        </p>
      )}
    </div>
  );

  const actions = (
    <div className="flex flex-wrap gap-2 lg:flex-col lg:flex-nowrap">
      <button
        type="button"
        onClick={() => handlePromote(item.id, !item.promote)}
        className="rounded-md bg-brand px-3 py-1.5 text-xs font-medium text-on-brand transition-colors hover:bg-brand-hover"
      >
        {item.promote ? "Unpromote" : "Promote"}
      </button>
      <button
        type="button"
        onClick={() => handleFreeze(item.id, !item.onHold)}
        className="rounded-md bg-surface-3 px-3 py-1.5 text-xs font-medium text-on-surface transition-colors hover:bg-surface-4"
      >
        {item.onHold ? "Unfreeze" : "Freeze"}
      </button>
      <button
        type="button"
        onClick={() => onDelete(item.id)}
        className="rounded-md bg-danger px-3 py-1.5 text-xs font-medium text-white transition-colors hover:bg-danger-hover"
      >
        Delete
      </button>
    </div>
  );

  const rowTint = item.onHold ? "bg-danger/10" : "bg-surface-1";

  return (
    <>
      <article
        className={cn("border-b border-surface-3 p-4 lg:hidden", rowTint)}
      >
        <div className="flex gap-3">
          {cover}

          <div className="min-w-0 flex-1 space-y-1 text-left">
            <button
              type="button"
              onClick={navigateItemWithId}
              className="line-clamp-2 text-left font-medium text-on-surface hover:text-brand-fg"
            >
              {item.title || "Untitled"}
            </button>
            <p className="text-xs text-on-surface-muted">
              {[item.date, item.format].filter(Boolean).join(" · ") || "—"}
            </p>
            {priceCell}
            <p className="text-xs text-on-surface-muted">
              {item.promote ? "Promoted" : "Not promoted"}
            </p>
          </div>
        </div>

        <div className="mt-3">{actions}</div>
      </article>

      <article
        className={cn(
          ADMIN_GRID,
          "hidden border-b border-surface-3 lg:grid",
          rowTint,
        )}
      >
        <div className="flex min-w-0 items-center justify-center px-3 py-3">
          {cover}
        </div>

        <button
          type="button"
          onClick={navigateItemWithId}
          className="min-w-0 px-3 py-3 text-left"
        >
          <p className="line-clamp-2 font-medium text-on-surface hover:text-brand-fg">
            {item.title || "Untitled"}
          </p>
        </button>

        <p className="min-w-0 truncate px-3 py-3 text-sm text-on-surface-dim">
          {item.date || "—"}
        </p>

        <p className="min-w-0 truncate px-3 py-3 text-sm text-on-surface-dim">
          {item.format || "—"}
        </p>

        <div className="min-w-0 px-3 py-3">{priceCell}</div>

        <p className="min-w-0 px-3 py-3 text-sm text-on-surface-dim">
          {item.promote ? "Promoted" : "—"}
        </p>

        <div className="min-w-0 px-3 py-3">{actions}</div>
      </article>
    </>
  );
}
