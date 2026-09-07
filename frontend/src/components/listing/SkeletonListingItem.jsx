export default function SkeletonListingItem() {
  return (
    <div
      className="mb-5 grid grid-cols-7 items-center gap-5 border-b bg-neutral-primary-soft pb-5 ml-1"
      aria-hidden="true"
    >
      <div className="h-35 w-35 animate-pulse bg-surface-3" />
      <div className="h-5 w-3/4 animate-pulse rounded bg-surface-3 px-6" />
      <div className="h-5 w-12 animate-pulse rounded bg-surface-3 px-6" />
      <div className="h-5 w-16 animate-pulse rounded bg-surface-3 px-6" />
      <div className="h-5 w-14 animate-pulse rounded bg-surface-3 px-6" />
      <div className="h-5 w-20 animate-pulse rounded bg-surface-3 px-6" />
      <div className="flex flex-col items-center gap-2 px-6">
        <div className="h-9 w-24 animate-pulse rounded-md bg-surface-3" />
        <div className="h-9 w-24 animate-pulse rounded-md bg-surface-3" />
      </div>
    </div>
  );
}
