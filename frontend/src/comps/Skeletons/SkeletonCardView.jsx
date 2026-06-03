import CardImage from "../CardImage";

export default function SkeletonCardView() {
  return (
    <div className="border-2 w-full p-4 rounded-2xl">
      <div className="w-full">
        <CardImage src={null} />
      </div>

      <div className="mt-4 mb-4.5 bg-neutral-700 rounded animate-pulse" />

      <div className="mb-3 h-5 w-2/5 bg-neutral-700 rounded animate-pulse" />
      <div className="mb-3 h-5 w-1/2 bg-neutral-700 rounded animate-pulse" />
      <div className="mb-5 h-5 w-1/3 bg-neutral-700 rounded animate-pulse" />
      <div className="mb-5 h-5 w-2/5 bg-neutral-700 rounded animate-pulse" />
      <div className="mb-3 h-5 w-3/5 bg-neutral-700 rounded animate-pulse" />
      <div className="mb-5 h-5 w-2/5 bg-neutral-700 rounded animate-pulse" />
      <div className="mb-4 h-5 w-2/5 bg-neutral-700 rounded animate-pulse" />

      <div className="mt-5 h-6 w-1/3 bg-neutral-700 rounded animate-pulse" />

      <div className=" flex flex-row gap-3 justify-center mt-4">
        <div className="h-10 w-28 rounded-xl bg-neutral-700 animate-pulse" />
        <div className="h-10 w-32 rounded-xl bg-neutral-700 animate-pulse" />
      </div>
    </div>
  );
}
