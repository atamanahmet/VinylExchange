import { CardFooter } from "@/components/ui/card";
import { cn } from "@/lib/utils";

import CardActionButton from "./CardActionButton";

function renderAction(action, buttonClass, nowrap) {
  if (!action) {
    return null;
  }

  return (
    <CardActionButton
      data-card-action-btn
      label={action.label}
      onClick={action.onClick}
      isActive={action.isActive}
      disabled={action.disabled}
      className={buttonClass}
      nowrap={nowrap}
    />
  );
}

export default function CardActionButtons({
  primaryAction,
  secondaryAction,
  layout = "card",
  stackActions = false,
}) {
  const hasTwo = Boolean(primaryAction && secondaryAction);
  const isList = layout === "list";
  const stack = stackActions;

  const buttonClass = stack
    ? "mt-0 h-9 min-h-9 w-full min-w-0 px-3 text-xs sm:text-sm"
    : isList
      ? "mt-0 h-9 min-h-9 min-w-0 flex-1 basis-[calc(50%-0.25rem)] px-3 text-xs sm:text-sm"
      : cn(
          "mt-0 w-full min-h-[2rem] @sm/card:min-h-[2.25rem] @lg/card:min-h-[2.5rem]",
          hasTwo && !stack && "min-w-0 flex-1 basis-0",
        );

  const nowrap = (isList || hasTwo) && !stack;

  const actions = stack && secondaryAction
    ? [secondaryAction, primaryAction]
    : [primaryAction, secondaryAction];

  return (
    <CardFooter
      className={cn(
        "border-t-0 bg-transparent p-0",
        isList ? "pb-0" : "pb-2 @sm/card:pb-3 @lg/card:pb-4",
      )}
    >
      <div
        className={cn(
          "flex w-full min-w-0",
          stack
            ? "flex-col gap-2"
            : isList
              ? "flex-row flex-wrap gap-2"
              : cn(
                  "gap-2",
                  hasTwo
                    ? "flex-row @max-[11rem]/card:flex-col"
                    : "flex-col",
                ),
        )}
      >
        {actions.map(
          (action) =>
            action &&
            renderAction(action, buttonClass, nowrap),
        )}
      </div>
    </CardFooter>
  );
}
