import { useState } from "react";
import { Link } from "react-router-dom";
import { cn } from "@/lib/utils";
import { useCartStore } from "@/stores/cartStore";
import { buildListingPath } from "@/utils/listingPath";

export default function CartItem({ item }) {
  const addToCart = useCartStore((state) => state.addToCart);
  const decreaseFromCart = useCartStore((state) => state.decreaseFromCart);
  const removeFromCart = useCartStore((state) => state.removeFromCart);

  const [isFavorite, setIsFavorite] = useState(false);
  const listingPath = buildListingPath(item);

  return (
    <div className="rounded-lg border border-surface-3 bg-surface-1 p-4 ring-1 ring-surface-3 md:p-6">
      <div className="space-y-4 md:flex md:items-center md:justify-between md:gap-6 md:space-y-0">
        <Link to={listingPath} className="shrink-0 md:order-1">
          <img
            className="h-25 w-25"
            src={item.mainImagePath || "/placeholder.png"}
            alt=""
          />
        </Link>

        <label htmlFor="counter-input" className="sr-only">
          Choose quantity:
        </label>
        <div className="flex items-center justify-between md:order-3 md:justify-end">
          <div className="flex items-center">
            <button
              type="button"
              id="decrement-button-2"
              data-input-counter-decrement="counter-input-2"
              className="inline-flex h-5 w-5 shrink-0 items-center justify-center rounded-md border border-surface-3 bg-surface-2 text-on-surface hover:bg-surface-3 focus:outline-none focus-visible:ring-2 focus-visible:ring-surface-4"
              onClick={() => decreaseFromCart(item.publicId)}
            >
              <svg
                className="h-2.5 w-2.5"
                aria-hidden="true"
                xmlns="http://www.w3.org/2000/svg"
                fill="none"
                viewBox="0 0 18 2"
              >
                <path
                  stroke="currentColor"
                  strokeLinecap="round"
                  strokeLinejoin="round"
                  strokeWidth="2"
                  d="M1 1h16"
                />
              </svg>
            </button>
            <input
              type="text"
              id="counter-input-2"
              data-input-counter
              className="w-10 shrink-0 border-0 bg-transparent text-center text-sm font-medium text-on-surface focus:outline-none focus:ring-0"
              value={item.quantity}
              required
              readOnly
            />
            <button
              type="button"
              id="increment-button-2"
              data-input-counter-increment="counter-input-2"
              className="inline-flex h-5 w-5 shrink-0 items-center justify-center rounded-md border border-surface-3 bg-surface-2 text-on-surface hover:bg-surface-3 focus:outline-none focus-visible:ring-2 focus-visible:ring-surface-4"
              onClick={() => addToCart(item.publicId)}
            >
              <svg
                className="h-2.5 w-2.5"
                aria-hidden="true"
                xmlns="http://www.w3.org/2000/svg"
                fill="none"
                viewBox="0 0 18 18"
              >
                <path
                  stroke="currentColor"
                  strokeLinecap="round"
                  strokeLinejoin="round"
                  strokeWidth="2"
                  d="M9 1v16M1 9h16"
                />
              </svg>
            </button>
          </div>

          <div className="text-end md:order-4 md:w-32">
            <p
              className={cn(
                "text-base font-bold text-on-surface-bright",
                item.discountPerUnit > 0 && "line-through",
              )}
            >
              {item.totalPrice
                ? item.totalPrice.toLocaleString("tr-TR") + " ₺"
                : 0}
            </p>
            {item.discountPerUnit > 0 && (
              <p className="text-base font-bold text-success-fg">
                {item.discountedTotalPrice.toLocaleString("tr-TR") + " ₺"}
              </p>
            )}
          </div>
        </div>

        <div className="flex h-20 w-full min-w-0 flex-col justify-center gap-5 text-left md:order-2 md:max-w-md">
          <Link
            to={listingPath}
            className="cursor-pointer text-on-surface-bright hover:underline"
          >
            {item.title + " - " + item.artistName}
          </Link>

          <div className="flex gap-5">
            <button
              type="button"
              className={cn(
                "inline-flex rounded text-sm font-medium focus:outline-none focus-visible:ring-1 focus-visible:ring-surface-4 focus-visible:ring-offset-2 focus-visible:ring-offset-surface-1",
                isFavorite
                  ? "text-danger-fg hover:text-danger-bright"
                  : "text-on-surface-muted hover:text-on-surface hover:underline",
              )}
              onClick={() => setIsFavorite(true)}
            >
              <svg
                className="me-1.5 h-5 w-5"
                aria-hidden="true"
                xmlns="http://www.w3.org/2000/svg"
                width="24"
                height="24"
                fill={isFavorite ? "currentColor" : "none"}
                viewBox="0 0 24 24"
              >
                <path
                  stroke="currentColor"
                  strokeLinecap="round"
                  strokeLinejoin="round"
                  strokeWidth="2"
                  d="M12.01 6.001C6.5 1 1 8 5.782 13.001L12.011 20l6.23-7C23 8 17.5 1 12.01 6.002Z"
                />
              </svg>
              Add to Favorites
            </button>

            <button
              type="button"
              onClick={() => removeFromCart(item.id)}
              className="inline-flex items-center rounded text-sm font-medium text-danger-fg hover:text-danger-bright hover:underline focus:outline-none focus-visible:ring-2 focus-visible:ring-surface-4 focus-visible:ring-offset-2 focus-visible:ring-offset-surface-1"
            >
              <svg
                className="me-1.5 h-5 w-5"
                aria-hidden="true"
                xmlns="http://www.w3.org/2000/svg"
                width="24"
                height="24"
                fill="none"
                viewBox="0 0 24 24"
              >
                <path
                  stroke="currentColor"
                  strokeLinecap="round"
                  strokeLinejoin="round"
                  strokeWidth="2"
                  d="M6 18 17.94 6M18 18 6.06 6"
                />
              </svg>
              Remove
            </button>
          </div>
        </div>
      </div>
    </div>
  );
}
