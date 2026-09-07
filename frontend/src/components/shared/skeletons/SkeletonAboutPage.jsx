export default function SkeletonAboutPage() {
  return (
    <div className="text-left -ml-25 -mt-50">
      <section>
        <div className="grid max-w-7xl px-4 py-8 mx-auto lg:gap-8 xl:gap-0 lg:py-16 lg:grid-cols-12">
          <div className="mr-auto lg:col-span-10 pr-50 pl-20 py-20 mt-20">
            {/* header */}
            <div className="max-w-2xl mb-4 h-16 bg-gray-700 animate-pulse rounded-lg my-10"></div>

            {/* text content */}
            <div className="max-w-2xl space-y-3 mb-6">
              <div className="h-6 bg-gray-700 animate-pulse rounded"></div>
              <div className="h-6 bg-gray-700 animate-pulse rounded"></div>
              <div className="h-6 bg-gray-700 animate-pulse rounded w-5/6"></div>
              <div className="h-6 bg-gray-700 animate-pulse rounded"></div>
              <div className="h-6 bg-gray-700 animate-pulse rounded w-4/6"></div>
            </div>

            {/* buttons */}
            <div className="flex gap-3 mt-6">
              <div className="w-40 h-12 bg-gray-700 animate-pulse rounded-lg"></div>
              <div className="w-40 h-12 bg-gray-700 animate-pulse rounded-lg"></div>
            </div>

            <div className="bg-gray-900 mr-auto lg:col-span-10 pr-50 py-20 mt-32 opacity-80 absolute top-0 w-200 h-150 -ml-20 -z-1"></div>
          </div>

          {/* image */}
          <div className="rotate-30 absolute -z-2 w-300 h-300 -top-25 left-100 bg-gray-700 animate-pulse rounded-lg"></div>
        </div>
      </section>
    </div>
  );
}
