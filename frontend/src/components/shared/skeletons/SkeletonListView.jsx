import { cn } from "@/lib/utils";
import {
  LIST_VIEW_ACTIONS_CELL,
  LIST_VIEW_CELL,
  LIST_VIEW_COVER_CELL,
  listViewRowClass,
} from "@/utils/listViewLayout";

export default function SkeletonListView({ showPrice = true, showActions = true }) {
  return (
    <article
      className={listViewRowClass({ showPrice, showActions })}
      aria-hidden="true"
    >
      <div className={LIST_VIEW_COVER_CELL}>
        <div className="size-24 animate-pulse rounded-md bg-surface-3" />
      </div>

      <div className={LIST_VIEW_CELL}>
        <div className="h-5 w-3/4 animate-pulse rounded bg-surface-3" />
      </div>

      <div className={LIST_VIEW_CELL}>
        <div className="h-5 w-2/3 animate-pulse rounded bg-surface-3" />
      </div>

      <div className={LIST_VIEW_CELL}>
        <div className="h-4 w-12 animate-pulse rounded bg-surface-3" />
      </div>

      <div className={LIST_VIEW_CELL}>
        <div className="h-4 w-16 animate-pulse rounded bg-surface-3" />
      </div>

      <div className={LIST_VIEW_CELL}>
        <div className="h-4 w-12 animate-pulse rounded bg-surface-3" />
      </div>

      {showPrice && (
        <div className={LIST_VIEW_CELL}>
          <div className="h-5 w-20 animate-pulse rounded bg-surface-3" />
        </div>
      )}

      {showActions && (
        <div className={cn(LIST_VIEW_ACTIONS_CELL, "space-y-2")}>
          <div className="h-9 w-full animate-pulse rounded-xl bg-surface-3" />
          <div className="h-9 w-full animate-pulse rounded-xl bg-surface-3" />
        </div>
      )}
    </article>
  );
}
