export default function SkeletonNavbar() {
  return (
    <nav
      className="border-b border-surface-3 bg-surface-base"
      aria-hidden="true"
    >
      <div className="mx-auto max-w-7xl px-3 py-3">
        <div className="flex items-center justify-between">
          <div className="h-10 w-32 animate-pulse rounded bg-surface-3" />

          <div className="mx-8 hidden max-w-md flex-1 md:block">
            <div className="h-10 animate-pulse rounded bg-surface-3" />
          </div>

          <div className="flex items-center gap-2">
            <div className="h-10 w-10 animate-pulse rounded bg-surface-3 md:hidden" />
            <div className="h-10 w-20 animate-pulse rounded bg-surface-3" />
            <div className="h-10 w-10 animate-pulse rounded bg-surface-3" />
            <div className="h-10 w-10 animate-pulse rounded bg-surface-3" />
            <div className="h-10 w-10 animate-pulse rounded-full bg-surface-3" />
            <div className="h-10 w-10 animate-pulse rounded bg-surface-3 lg:hidden" />
          </div>
        </div>
      </div>
    </nav>
  );
}
