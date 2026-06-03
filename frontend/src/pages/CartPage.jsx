import axios from "axios";
import { useEffect, useState } from "react";
import { useAuthStore } from "../stores/authStore";
import { useCartStore } from "../stores/cartStore";

import CartItem from "../comps/CartItem";
import PromotedItem from "../comps/PromotedItem";

export default function CartPage() {
  const [promotedListings, setPromotedListings] = useState();
  const user = useAuthStore((state) => state.user);
  const setOpenLogin = useAuthStore((state) => state.setOpenLogin);
  const cart = useCartStore((state) => state.cart);
  const fetchCart = useCartStore((state) => state.fetchCart);
  const addtoCart = useCartStore((state) => state.addtoCart);
  const decreaseFromCart = useCartStore((state) => state.decreaseFromCart);
  const removeFromCart = useCartStore((state) => state.removeFromCart);

  useEffect(() => {
    fetchCart();
  }, []);

  useEffect(() => {
    if (user == null && !isLoading) {
      setOpenLogin;
    }
  }, []);

  async function fetchPromotedListings() {
    if (user && cart) {
      try {
        const res = await axios.get(
          "http://localhost:8080/api/listings/promote",
          {
            withCredentials: true,
          },
        );
        if (res.status === 200) {
          setPromotedListings(res.data);
          console.log(res.data);
        }
      } catch (e) {
        console.log(e);
      }
    }
  }

  useEffect(() => {
    fetchPromotedListings();
  }, [cart]);

  const testCheckout = async () => {
    console.log("checkout");
    try {
      const res = await axios.post(
        "http://localhost:8080/checkout",
        {},
        { withCredentials: true },
      );

      if (res.status == 200) {
        console.log(res.status);
        console.log(res);
      }
    } catch (error) {}
  };

  return (
    <>
      <section className="bg-white max-w-7xl mx-auto min-h-screen antialiased dark:bg-gray-900 py-5 ">
        <div className="  px-10 ">
          <h2 className="text-xl font-semibold text-gray-900 dark:text-white sm:text-2xl">
            Shopping Cart
          </h2>

          <div className="mt-6 sm:mt-8 md:gap-6 lg:flex lg:items-start xl:gap-8">
            <div className="mx-auto  flex-none lg:max-w-2xl xl:max-w-4xl">
              <div className="space-y-6">
                {/* {cart} */}
                {cart &&
                  cart.items &&
                  cart.items.map((item) => {
                    return <CartItem key={item.id} item={item}></CartItem>;
                  })}
              </div>

              <div className="hidden xl:mt-8 w-200 xl:block">
                <h3 className="text-2xl font-semibold text-gray-900 dark:text-white">
                  People also bought
                </h3>
                <div className="mt-6 grid grid-cols-3 gap-4 sm:mt-8">
                  {promotedListings &&
                    promotedListings.map((item) => {
                      return (
                        <PromotedItem key={item.id} item={item}></PromotedItem>
                      );
                    })}
                </div>
              </div>
            </div>

            <div className="mx-auto mt-6 max-w-4xl flex-1 space-y-6 lg:mt-0 lg:w-full">
              <div className="space-y-4 rounded-lg border border-gray-200 bg-white p-4 shadow-sm dark:border-gray-700 dark:bg-gray-800 sm:p-6">
                <p className="text-xl font-semibold text-gray-900 dark:text-white">
                  Order summary
                </p>

                <div className="space-y-4">
                  <div className="space-y-2">
                    <dl className="flex items-center justify-between gap-4">
                      <dt className="text-base font-normal text-gray-500 dark:text-gray-400">
                        Original price
                      </dt>
                      <dd className="text-base font-medium text-gray-900 dark:text-white">
                        {cart && cart.price.toLocaleString("tr-TR") + " ₺"}
                      </dd>
                    </dl>

                    <dl className="flex items-center justify-between gap-4">
                      <dt className="text-base font-normal text-gray-500 dark:text-gray-400">
                        Savings
                      </dt>
                      <dd className="text-base font-medium text-green-600">
                        {cart &&
                          (cart.discountedPrice - cart.price).toLocaleString(
                            "tr-TR",
                          ) + " ₺"}
                      </dd>
                    </dl>
                  </div>

                  <dl className="flex items-center justify-between gap-4 border-t border-gray-200 pt-2 dark:border-gray-700">
                    <dt className="text-base font-bold text-gray-900 dark:text-white">
                      Total
                    </dt>
                    <dd className="text-base font-bold text-gray-900 dark:text-white">
                      {cart &&
                        cart.discountedPrice &&
                        cart.discountedPrice.toLocaleString("tr-TR") + " ₺"}
                    </dd>
                  </dl>
                </div>

                <button
                  onClick={testCheckout}
                  className="flex w-full items-center justify-center rounded-lg bg-primary-700 px-5 py-2.5 text-sm font-medium text-white hover:bg-primary-800 focus:outline-none focus:ring-4 focus:ring-primary-300 dark:bg-primary-600 dark:hover:bg-primary-700 dark:focus:ring-primary-800"
                >
                  Proceed to Checkout
                </button>

                <div className="flex items-center justify-center gap-2">
                  <span className="text-sm font-normal text-gray-500 dark:text-gray-400">
                    {" "}
                    or{" "}
                  </span>
                  <a
                    href="/"
                    title=""
                    className="inline-flex items-center gap-2 text-sm font-medium text-primary-700 underline hover:no-underline dark:text-primary-500"
                  >
                    Continue Shopping
                    <svg
                      className="h-5 w-5"
                      aria-hidden="true"
                      xmlns="http://www.w3.org/2000/svg"
                      fill="none"
                      viewBox="0 0 24 24"
                    >
                      <path
                        stroke="currentColor"
                        strokeLinecap="round"
                        strokeLinejoin="round"
                        strokeWidth="2"
                        d="M19 12H5m14 0-4 4m4-4-4-4"
                      />
                    </svg>
                  </a>
                </div>
              </div>

              <div className="space-y-4 rounded-lg border border-gray-200 bg-white p-4 shadow-sm dark:border-gray-700 dark:bg-gray-800 sm:p-6">
                <form className="space-y-4">
                  <div>
                    <label className="mb-2 block text-sm font-medium text-gray-900 dark:text-white">
                      {" "}
                      Do you have a voucher or gift card?{" "}
                    </label>
                    <input
                      type="text"
                      id="voucher"
                      className="block w-full rounded-lg border border-gray-300 bg-gray-50 p-2.5 text-sm text-gray-900 focus:border-primary-500 focus:ring-primary-500 dark:border-gray-600 dark:bg-gray-700 dark:text-white dark:placeholder:text-gray-400 dark:focus:border-primary-500 dark:focus:ring-primary-500"
                      placeholder=""
                      required
                    />
                  </div>
                  <button
                    type="submit"
                    className="flex w-full items-center justify-center rounded-lg bg-primary-700 px-5 py-2.5 text-sm font-medium text-white hover:bg-primary-800 focus:outline-none focus:ring-4 focus:ring-primary-300 dark:bg-primary-600 dark:hover:bg-primary-700 dark:focus:ring-primary-800"
                  >
                    Apply Code
                  </button>
                </form>
              </div>
            </div>
          </div>
        </div>
      </section>
    </>
  );
}
