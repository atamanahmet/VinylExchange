import { useState } from "react";
import { useCartStore } from "../stores/cartStore";

export default function CartItem({ item }) {
  const addtoCart = useCartStore((state) => state.addtoCart);
  const decreaseFromCart = useCartStore((state) => state.decreaseFromCart);
  const removeFromCart = useCartStore((state) => state.removeFromCart);

  const [favButtonColor, setFavButtonColor] = useState();

  function handleFav() {
    setFavButtonColor("#cc0000");
  }
  return (
    <div className="rounded-lg border border-gray-200 bg-white p-4 shadow-sm dark:border-gray-700 dark:bg-gray-800 md:p-6">
      <div className="space-y-4 md:flex md:items-center md:justify-between md:gap-6 md:space-y-0">
        <a href="#" className="shrink-0 md:order-1">
          <img
            className="h-25 w-25"
            //first imnage as main image
            src={item.mainImagePath || "/placeholder.png"}
            alt=""
          />
        </a>

        <label htmlFor="counter-input" className="sr-only">
          Choose quantity:
        </label>
        <div className="flex items-center justify-between md:order-3 md:justify-end">
          <div className="flex items-center">
            <button
              type="button"
              id="decrement-button-2"
              data-input-counter-decrement="counter-input-2"
              className="inline-flex h-5 w-5 shrink-0 items-center justify-center rounded-md border border-gray-300 bg-gray-100 hover:bg-gray-200 focus:outline-none focus:ring-2 focus:ring-gray-100 dark:border-gray-600 dark:bg-gray-700 dark:hover:bg-gray-600 dark:focus:ring-gray-700"
              onClick={() => decreaseFromCart(item.listingId)}
            >
              <svg
                className="h-2.5 w-2.5 text-gray-900 dark:text-white"
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
              className="w-10 shrink-0 border-0 bg-transparent text-center text-sm font-medium text-gray-900 focus:outline-none focus:ring-0 dark:text-white"
              value={item.quantity}
              required
              readOnly
              // onChange={()=>updateItemQuantity(item.id, item.quantity)}
            />
            <button
              type="button"
              id="increment-button-2"
              data-input-counter-increment="counter-input-2"
              className="inline-flex h-5 w-5 shrink-0 items-center justify-center rounded-md border border-gray-300 bg-gray-100 hover:bg-gray-200 focus:outline-none focus:ring-2 focus:ring-gray-100 dark:border-gray-600 dark:bg-gray-700 dark:hover:bg-gray-600 dark:focus:ring-gray-700"
              onClick={() => addtoCart(item.listingId)}
            >
              <svg
                className="h-2.5 w-2.5 text-gray-900 dark:text-white"
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

          {/* total price */}
          <div className="text-end md:order-4 md:w-32">
            <p
              className={
                item.discountPerUnit > 0
                  ? "text-base font-bold text-gray-900 dark:text-white line-through" +
                    " ₺"
                  : "text-base font-bold text-gray-900 dark:text-white "
              }
            >
              {item.totalPrice
                ? item.totalPrice.toLocaleString("tr-TR") + " ₺"
                : 0}
            </p>
            <p
              className={
                item.discountPerUnit > 0
                  ? "text-base font-bold text-green-900 dark:text-green-400 " +
                    " ₺"
                  : "text-base font-bold text-green-900 dark:text-green-900 "
              }
            >
              {item.discountPerUnit > 0
                ? item.discountedTotalPrice.toLocaleString("tr-TR") + " ₺"
                : null}
            </p>
          </div>
        </div>

        <div className="w-full min-w-0 flex flex-col justify-center gap-5 h-20 md:order-2 md:max-w-md text-left">
          <a className="cursor-pointer">
            {item.title + " - " + item.artistName}
          </a>

          <div className="flex gap-5 ">
            <button
              type="button"
              className="inline-flex text-sm font-medium text-gray-500 hover:text-gray-900 hover:underline dark:text-gray-400 dark:hover:text-white focus:outline-none focus-visible:ring-1 focus-visible:ring-gray-300 focus-visible:ring-offset-2 rounded"
              style={{ outline: "none", border: "none" }}
              onClick={handleFav}
            >
              <svg
                className="me-1.5 h-5 w-5"
                aria-hidden="true"
                xmlns="http://www.w3.org/2000/svg"
                width="24"
                height="24"
                fill={favButtonColor}
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
              style={{ outline: "none", border: "none" }}
              onClick={() => removeFromCart(item.id)}
              className="inline-flex items-center text-sm font-medium text-red-600 hover:underline dark:text-red-500 focus:outline-none focus-visible:ring-2 focus-visible:ring-gray-300 focus-visible:ring-offset-2 rounded"
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
