import { useNavigate } from "react-router-dom";

export default function ListingItem({ item }) {
  const navigate = useNavigate();

  return (
    <div className="bg-neutral-primary-soft ml-1 pb-5 gap-5 grid grid-cols-7 border-b mb-5 items-center">
      {/* Image */}
      <button onClick={() => item.primaryAction?.onClick?.()}>
        <img
          src={item.imageUrl}
          onError={(e) => {
            e.target.src = "/placeholder.png";
          }}
          alt={item.title || "listing main image"}
          className="bg-black object-cover h-35 w-35"
        />
      </button>

      {/* Title */}
      <button onClick={() => item.primaryAction?.onClick?.()}>
        <p className="px-6 py-4 font-medium text-heading flex flex-col overflow-auto">
          {item.title || "Untitled"}
        </p>
      </button>

      {/* Release Date */}
      <p className="px-6 py-4">{item.year || "Unknown"}</p>

      {/* Format */}
      <p className="px-6 py-4">{item.format || "N/A"}</p>

      {/* Price */}
      <div>
        <p
          className={`px-6 ${
            item.discount > 0
              ? "text-base font-bold text-gray-900 dark:text-white line-through"
              : "text-base font-bold text-gray-900 dark:text-white"
          }`}
        >
          {item.price != null ? item.price + " ₺" : "-"}
        </p>
        <p
          className={`px-6 ${
            item.discount > 0
              ? "text-base font-bold text-green-900 dark:text-green-400"
              : "text-base font-bold text-green-900 dark:text-green-400"
          }`}
        >
          {item.discount > 0 && item.discountedPrice != null
            ? item.discountedPrice + " ₺"
            : null}
        </p>
      </div>

      {/* Format */}
      <p className="px-6 py-4">{item.createdAt || "N/A"}</p>

      {/* Actions */}
      <div className="px-6 py-4 text-right">
        <div className="flex flex-col justify-center items-center -mt-5 gap-2">
          {item.primaryAction && (
            <button
              onClick={item.primaryAction.onClick}
              className="font-medium text-fg-brand hover:underline bg-indigo-700 py-2 px-4 rounded-md cursor-pointer"
            >
              {item.primaryAction.label}
            </button>
          )}
          {item.secondaryAction && (
            <button
              onClick={item.secondaryAction.onClick}
              className="font-medium text-fg-brand hover:underline bg-red-700 py-2 px-4 rounded-md cursor-pointer"
            >
              {item.secondaryAction.label}
            </button>
          )}
        </div>
      </div>
    </div>
  );
}
