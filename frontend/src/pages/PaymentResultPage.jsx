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
    <section className="max-w-7xl mx-auto min-h-screen bg-surface-1 py-5">
      <div className="px-10 flex flex-col items-center justify-center py-20">
        {status === "success" ? (
          <>
            <div className="text-success-fg text-6xl mb-4">✓</div>
            <h2 className="text-2xl font-semibold text-on-surface mb-2">
              Payment Successful
            </h2>
            <p className="text-on-surface-muted mb-6">
              Your order has been confirmed.
            </p>
            <p className="text-sm text-on-surface-muted mt-2">
              You can find your payment reference in your order details.
            </p>
            <button
              onClick={() => navigate("/orders")}
              className="rounded-lg bg-brand px-5 py-2.5 text-sm font-medium text-on-brand hover:bg-brand-hover"
            >
              View Orders
            </button>
          </>
        ) : status === "refund-review" ? (
          <>
            <div className="text-brand-fg text-6xl mb-4">!</div>
            <h2 className="text-2xl font-semibold text-on-surface mb-2">
              Payment Under Review
            </h2>
            <p className="text-on-surface-muted mb-4 text-center max-w-md">
              We received your payment notification and our team is reviewing it.
              You have not been charged twice.
            </p>
            <p className="text-sm text-on-surface-muted mb-6 text-center max-w-md">
              No action is needed right now. If you have concerns, please contact
              support and we will follow up with you.
            </p>
            <div className="flex flex-col sm:flex-row gap-3">
              <button
                onClick={() => navigate("/orders")}
                className="rounded-lg bg-brand px-5 py-2.5 text-sm font-medium text-on-brand hover:bg-brand-hover"
              >
                View Orders
              </button>
              <button
                onClick={() => navigate("/contact")}
                className="rounded-lg border border-surface-4 px-5 py-2.5 text-sm font-medium text-on-surface-dim hover:bg-surface-2"
              >
                Contact Support
              </button>
            </div>
          </>
        ) : (
          <>
            <div className="text-danger-bright text-6xl mb-4">✕</div>
            <h2 className="text-2xl font-semibold text-on-surface mb-2">
              Payment Failed
            </h2>
            <p className="text-on-surface-muted mb-6">
              Something went wrong. Your cart has been restored.
            </p>
            <button
              onClick={() => navigate("/cart")}
              className="rounded-lg bg-brand px-5 py-2.5 text-sm font-medium text-on-brand hover:bg-brand-hover"
            >
              Return to Cart
            </button>
          </>
        )}
      </div>
    </section>
  );
}
