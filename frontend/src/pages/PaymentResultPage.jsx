import { useEffect } from "react";
import { useNavigate, useSearchParams } from "react-router-dom";
import { useOrderStore } from "../stores/orderStore";

export default function PaymentResultPage() {
  const navigate = useNavigate();
  const [searchParams] = useSearchParams();
  const status = searchParams.get("status");
  const setPendingOrderIds = useOrderStore((state) => state.setPendingOrderIds);

  useEffect(() => {
    if (!status) navigate("/");
  }, [status, navigate]);

  useEffect(() => {
    if (status === "success") {
      setPendingOrderIds([]);
    }
  }, [status, setPendingOrderIds]);

  return (
    <section className="bg-white max-w-7xl mx-auto min-h-screen dark:bg-gray-900 py-5">
      <div className="px-10 flex flex-col items-center justify-center py-20">
        {status === "success" ? (
          <>
            <div className="text-green-500 text-6xl mb-4">✓</div>
            <h2 className="text-2xl font-semibold text-gray-900 dark:text-white mb-2">
              Payment Successful
            </h2>
            <p className="text-gray-500 dark:text-gray-400 mb-6">
              Your order has been confirmed.
            </p>
            <p className="text-sm text-gray-400 mt-2">
              You can find your payment reference in your order details.
            </p>
            <button
              onClick={() => navigate("/orders")}
              className="rounded-lg bg-primary-700 px-5 py-2.5 text-sm font-medium text-white hover:bg-primary-800"
            >
              View Orders
            </button>
          </>
        ) : status === "refund-review" ? (
          <>
            <div className="text-amber-500 text-6xl mb-4">!</div>
            <h2 className="text-2xl font-semibold text-gray-900 dark:text-white mb-2">
              Payment Under Review
            </h2>
            <p className="text-gray-500 dark:text-gray-400 mb-4 text-center max-w-md">
              We received your payment notification and our team is reviewing it.
              You have not been charged twice.
            </p>
            <p className="text-sm text-gray-400 mb-6 text-center max-w-md">
              No action is needed right now. If you have concerns, please contact
              support and we will follow up with you.
            </p>
            <div className="flex flex-col sm:flex-row gap-3">
              <button
                onClick={() => navigate("/orders")}
                className="rounded-lg bg-primary-700 px-5 py-2.5 text-sm font-medium text-white hover:bg-primary-800"
              >
                View Orders
              </button>
              <button
                onClick={() => navigate("/contact")}
                className="rounded-lg border border-gray-300 px-5 py-2.5 text-sm font-medium text-gray-700 hover:bg-gray-50 dark:border-gray-600 dark:text-gray-200 dark:hover:bg-gray-800"
              >
                Contact Support
              </button>
            </div>
          </>
        ) : (
          <>
            <div className="text-red-500 text-6xl mb-4">✕</div>
            <h2 className="text-2xl font-semibold text-gray-900 dark:text-white mb-2">
              Payment Failed
            </h2>
            <p className="text-gray-500 dark:text-gray-400 mb-6">
              Something went wrong. Your cart has been restored.
            </p>
            <button
              onClick={() => navigate("/cart")}
              className="rounded-lg bg-primary-700 px-5 py-2.5 text-sm font-medium text-white hover:bg-primary-800"
            >
              Return to Cart
            </button>
          </>
        )}
      </div>
    </section>
  );
}
