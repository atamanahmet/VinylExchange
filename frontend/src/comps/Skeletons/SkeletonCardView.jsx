import CardImage from "../CardImage";

export default function SkeletonCardView() {
  return (
    <div className="@container/card w-full rounded-xl border border-surface-3 bg-surface-1 p-2.5 @sm/card:rounded-2xl @sm/card:p-3 @lg/card:p-4">
      <div className="w-full">
        <CardImage src={null} />
      </div>

      <div className="mt-2 h-4 w-full rounded bg-surface-3 animate-pulse @sm/card:mt-2.5 @lg/card:mt-3" />
      <div className="mt-1.5 h-3 w-3/5 rounded bg-surface-3 animate-pulse @sm/card:mt-2" />
      <div className="mt-1.5 h-3 w-4/5 rounded bg-surface-3 animate-pulse" />
      <div className="mt-1.5 hidden h-3 w-2/3 rounded bg-surface-3 animate-pulse @md/card:block" />

      <div className="mt-2 h-5 w-1/3 rounded bg-surface-3 animate-pulse @sm/card:mt-2.5 @lg/card:mt-3" />

      <div className="mt-2 flex gap-1.5 @sm/card:mt-3 @sm/card:gap-2">
        <div className="h-8 min-h-[2rem] flex-1 rounded-xl bg-surface-3 animate-pulse @lg/card:h-10" />
        <div className="h-8 min-h-[2rem] flex-1 rounded-xl bg-surface-3 animate-pulse @lg/card:h-10" />
      </div>
    </div>
  );
}
