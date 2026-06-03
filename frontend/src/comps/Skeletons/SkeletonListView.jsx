import { useAuthStore } from "../../stores/authStore";

export default function SkeletonListView() {
  const user = useAuthStore((state) => state.user);

  return (
    <div className="bg-neutral-primary-soft ml-1 pb-5 gap-3 grid grid-cols-7 border-b max-w-[1160px] mb-3 items-center">
      <div className="bg-neutral-700 w-full aspect-square rounded animate-pulse min-h-35 max-w-35 ml-3"></div>

      <div className="px-12 ">
        <div className="bg-neutral-700 h-5 rounded w-3/4 animate-pulse"></div>
      </div>
      <div className="px-12 ">
        <div className="bg-neutral-700 h-5 rounded w-3/4 animate-pulse"></div>
      </div>

      <div className="px-12 ">
        <div className="bg-neutral-700 h-4 rounded w-20 animate-pulse"></div>
      </div>

      <div className="px-12 ">
        <div className="bg-neutral-700 h-4 rounded w-16 animate-pulse"></div>
      </div>

      <div className="px-12 ">
        <div className="bg-neutral-700 h-5 rounded w-24 mb-2 animate-pulse"></div>
      </div>

      <div className="px-10  text-right flex flex-col justify-center items-center -mt-5">
        <div className="bg-neutral-700 h-10 rounded-xl w-full mt-5 animate-pulse"></div>
        {!user && (
          <div className="bg-neutral-700 h-10 rounded-xl w-full mt-5 animate-pulse"></div>
        )}
      </div>
    </div>
  );
}
