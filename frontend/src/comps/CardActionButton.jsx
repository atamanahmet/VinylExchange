import {
  ArrowUpRight,
  Heart,
  MessageSquare,
  PackageX,
  Pencil,
  ShoppingCart,
  Trash2,
} from "lucide-react";

import { Button } from "@/components/ui/button";
import { cn } from "@/lib/utils";

const iconByLabel = {
  Trade: MessageSquare,
  Edit: Pencil,
  "Add to cart": ShoppingCart,
  Remove: Trash2,
  Delete: Trash2,
  "Add to Wishlist": Heart,
  "Remove from Wishlist": Heart,
  Added: Heart,
  "In stock": ArrowUpRight,
  "Not in stock": PackageX,
};

const toneByLabel = {
  Edit: "bg-accent hover:bg-accent-hover text-on-surface",
  Remove:
    "bg-danger hover:bg-danger-hover text-on-surface focus-visible:ring-danger/40",
  Delete:
    "bg-danger hover:bg-danger-hover text-on-surface focus-visible:ring-danger/40",
  "Remove from Wishlist":
    "bg-danger hover:bg-danger-hover text-on-surface focus-visible:ring-danger/40",
  "In stock":
    "bg-success hover:bg-success-hover text-on-surface focus-visible:ring-success/40",
  "Not in stock":
    "border-surface-4 bg-surface-2 text-on-surface-muted hover:bg-surface-2",
};

function getToneClass(label, isActive, disabled) {
  if (disabled) {
    return toneByLabel[label] ?? "border-surface-4 bg-surface-2 text-on-surface-muted";
  }

  if (isActive) {
    return "bg-accent hover:bg-accent-hover text-on-surface focus-visible:ring-accent/40";
  }

  return toneByLabel[label] ?? "bg-brand hover:bg-brand-hover text-on-surface";
}

export default function CardActionButton({
  label,
  onClick,
  isActive = false,
  disabled = false,
  className,
  nowrap = false,
  ...props
}) {
  const Icon = iconByLabel[label];

  return (
    <Button
      type="button"
      onClick={() => onClick?.()}
      disabled={disabled}
      aria-pressed={isActive || undefined}
      className={cn(
        "inline-flex h-auto min-w-0 max-w-full items-center justify-center gap-1.5 rounded-xl border border-surface-3 px-3 py-2 font-medium leading-none shadow-xs",
        "@sm/card:gap-2 @sm/card:px-3.5 @sm/card:py-2.5",
        "text-on-surface-dim hover:bg-surface-3 hover:text-on-surface focus-visible:ring-4 focus-visible:ring-surface-4",
        "text-[0.6875rem] @sm/card:text-xs @md/card:text-sm",
        nowrap ? "whitespace-nowrap" : "whitespace-normal text-center break-words",
        getToneClass(label, isActive, disabled),
        disabled && "pointer-events-none opacity-80",
        className,
      )}
      {...props}
    >
      {Icon && (
        <Icon
          className={cn(
            "size-3.5 shrink-0 @sm/card:size-4",
            isActive && (label.includes("Wishlist") || label === "Added") && "fill-current",
          )}
          aria-hidden="true"
        />
      )}
      <span className={cn("min-w-0", nowrap && "truncate")}>{label}</span>
    </Button>
  );
}
