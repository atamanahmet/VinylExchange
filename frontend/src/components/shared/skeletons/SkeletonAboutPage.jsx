export default function SkeletonAboutPage() {
  return (
    <div
      className="relative flex justify-center overflow-hidden"
      style={{ height: "calc(100vh - 64px)" }}
    >
      <div className="relative z-10 mx-auto w-full max-w-7xl px-4 py-12 text-left sm:px-6 lg:px-8 lg:py-24">
        <div className="max-w-4xl">
          <div className="mb-8 h-12 max-w-2xl animate-pulse rounded-lg bg-surface-3 md:h-16" />

          <div className="mb-8 h-1 w-24 animate-pulse rounded bg-surface-3" />

          <div className="mb-12 max-w-2xl space-y-3">
            <div className="h-6 animate-pulse rounded bg-surface-3" />
            <div className="h-6 animate-pulse rounded bg-surface-3" />
            <div className="h-6 w-5/6 animate-pulse rounded bg-surface-3" />
            <div className="h-6 w-4/6 animate-pulse rounded bg-surface-3" />
          </div>

          <div className="flex flex-col gap-4 sm:flex-row">
            <div className="h-14 w-full animate-pulse rounded-lg bg-surface-3 sm:w-48" />
            <div className="h-14 w-full animate-pulse rounded-lg bg-surface-3 sm:w-48" />
          </div>

          <div className="mt-12 grid grid-cols-1 gap-6 md:grid-cols-3">
            <div className="h-40 animate-pulse rounded-lg bg-surface-3" />
            <div className="h-40 animate-pulse rounded-lg bg-surface-3" />
            <div className="h-40 animate-pulse rounded-lg bg-surface-3" />
          </div>
        </div>
      </div>
    </div>
  );
}
