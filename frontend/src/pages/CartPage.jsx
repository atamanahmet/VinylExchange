import { useEffect } from "react";
import { Link, useNavigate } from "react-router-dom";
import { useAuthStore } from "../stores/authStore";
import { useCartStore } from "../stores/cartStore";
import { useListingStore } from "../stores/listingStore";
import CartItem from "@/components/cart/CartItem";
import PromotedItem from "@/components/cart/PromotedItem";

export default function CartPage() {
  const user = useAuthStore((state) => state.user);
  const navigate = useNavigate();

  const cart = useCartStore((state) => state.cart);
  const fetchCart = useCartStore((state) => state.fetchCart);

  const promotedListings = useListingStore((state) => state.promotedListings);
  const fetchPromotedListings = useListingStore(
    (state) => state.fetchPromotedListings,
  );

  useEffect(() => {
    fetchCart();
  }, []);

  useEffect(() => {
    if (!user) {
      navigate("/");
    }
  }, [user]);

  useEffect(() => {
    if (cart?.items?.length > 0) {
      fetchPromotedListings();
    }
  }, [cart]);

  const handleCheckout = () => {
    navigate("/checkout/shipping");
  };

  return (
    <section className="mx-auto min-h-screen max-w-7xl bg-surface-base py-5 text-on-surface antialiased">
      <div className="px-10">
        <h2 className="text-xl font-semibold text-on-surface sm:text-2xl">
          Shopping Cart
        </h2>
        <div className="mt-6 sm:mt-8 md:gap-6 lg:flex lg:items-start xl:gap-8">
          <div className="mx-auto flex-none lg:max-w-2xl xl:max-w-4xl">
            <div className="space-y-6">
              {cart?.items?.map((item) => (
                <CartItem key={item.id} item={item} />
              ))}
            </div>
            <div className="hidden xl:mt-8 xl:block">
              <h3 className="text-2xl font-semibold text-on-surface">
                People also bought
              </h3>
              <div className="mt-6 grid grid-cols-3 gap-4 sm:mt-8">
                {promotedListings.map((item) => (
                  <PromotedItem key={item.id} item={item} />
                ))}
              </div>
            </div>
          </div>

          <div className="mx-auto mt-6 max-w-4xl flex-1 space-y-6 lg:mt-0 lg:w-full">
            <div className="space-y-4 rounded-lg border border-surface-3 bg-surface-1 p-4 ring-1 ring-surface-3 sm:p-6">
              <p className="text-xl font-semibold text-on-surface">
                Order summary
              </p>
              <div className="space-y-4">
                <div className="space-y-2">
                  <dl className="flex items-center justify-between gap-4">
                    <dt className="text-base font-normal text-on-surface-muted">
                      Original price
                    </dt>
                    <dd className="text-base font-medium text-on-surface-bright">
                      {cart && cart.price.toLocaleString("tr-TR") + " ₺"}
                    </dd>
                  </dl>
                  <dl className="flex items-center justify-between gap-4">
                    <dt className="text-base font-normal text-on-surface-muted">
                      Savings
                    </dt>
                    <dd className="text-base font-medium text-success-fg">
                      {cart &&
                        (cart.discountedPrice - cart.price).toLocaleString(
                          "tr-TR",
                        ) + " ₺"}
                    </dd>
                  </dl>
                </div>
                <dl className="flex items-center justify-between gap-4 border-t border-surface-3 pt-2">
                  <dt className="text-base font-bold text-on-surface">Total</dt>
                  <dd className="text-base font-bold text-on-surface">
                    {cart?.discountedPrice?.toLocaleString("tr-TR") + " ₺"}
                  </dd>
                </dl>
              </div>
              <button
                type="button"
                onClick={handleCheckout}
                className="flex w-full items-center justify-center rounded-lg bg-brand px-5 py-2.5 text-sm font-medium text-on-surface transition-colors hover:bg-brand-hover focus:outline-none focus-visible:ring-4 focus-visible:ring-brand/40"
              >
                Proceed to Checkout
              </button>
              <div className="flex items-center justify-center gap-2">
                <span className="text-sm font-normal text-on-surface-muted">
                  or
                </span>
                <Link
                  to="/"
                  className="inline-flex items-center gap-2 text-sm font-medium text-brand-fg underline hover:no-underline"
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
                </Link>
              </div>
            </div>
            <div className="space-y-4 rounded-lg border border-surface-3 bg-surface-1 p-4 ring-1 ring-surface-3 sm:p-6">
              <div className="space-y-4">
                <div>
                  <label
                    htmlFor="voucher"
                    className="mb-2 block text-sm font-medium text-on-surface"
                  >
                    Do you have a voucher or gift card?
                  </label>
                  <input
                    type="text"
                    id="voucher"
                    className="block w-full rounded-lg border border-surface-3 bg-surface-2 p-2.5 text-sm text-on-surface placeholder:text-on-surface-muted focus:border-brand focus:ring-brand/40"
                  />
                </div>
                <button
                  type="button"
                  className="flex w-full items-center justify-center rounded-lg bg-brand px-5 py-2.5 text-sm font-medium text-on-surface transition-colors hover:bg-brand-hover focus:outline-none focus-visible:ring-4 focus-visible:ring-brand/40"
                >
                  Apply Code
                </button>
              </div>
            </div>
          </div>
        </div>
      </div>
    </section>
  );
}
