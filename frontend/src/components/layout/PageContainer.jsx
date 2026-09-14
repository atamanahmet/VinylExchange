import { cn } from "@/lib/utils";

/**
 * Shared responsive page shell. Single source of truth for page
 * max-width and horizontal padding across every route.
 */
const WIDTHS = {
  narrow: "max-w-3xl",
  form: "max-w-4xl",
  content: "max-w-6xl",
  wide: "max-w-7xl",
  full: "max-w-none",
};

export default function PageContainer({
  width = "wide",
  className,
  innerClassName,
  children,
}) {
  return (
    <div className="min-h-screen w-full bg-surface-base text-on-surface">
      <div
        className={cn(
          "mx-auto w-full px-4 py-4 sm:px-6 sm:py-5 lg:px-8",
          WIDTHS[width] ?? WIDTHS.wide,
          className,
          innerClassName,
        )}
      >
        {children}
      </div>
    </div>
  );
}
