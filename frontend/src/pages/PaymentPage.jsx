import { useEffect, useRef, useState } from "react";
import { useNavigate } from "react-router-dom";
import { useOrderStore } from "../stores/orderStore";

export default function PaymentPage() {
  const navigate = useNavigate();
  const formContainerRef = useRef(null);

  const pendingOrderIds = useOrderStore((state) => state.pendingOrderIds);
  const initiatePayment = useOrderStore((state) => state.initiatePayment);

  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    if (!pendingOrderIds || pendingOrderIds.length === 0) {
      navigate("/");
      return;
    }

    /*
     * For now handle first order only
     * Multi-order payment wip
     */
    const orderId = pendingOrderIds[0];
    initPayment(orderId);
  }, []);

  const initPayment = async (orderId) => {
    setLoading(true);
    setError(null);

    const response = await initiatePayment(orderId);

    if (!response || !response.success) {
      setError("Payment could not be started. Please try again.");
      setLoading(false);
      return;
    }

    console.log("response success:", response?.success);
    console.log("checkoutFormContent exists:", !!response?.checkoutFormContent);
    console.log(
      "checkoutFormContent first 100:",
      response?.checkoutFormContent?.substring(0, 100),
    );
    console.log("formContainerRef.current:", formContainerRef.current);

    if (formContainerRef.current && response.checkoutFormContent) {
      let htmlContent = response.checkoutFormContent;

      try {
        htmlContent = atob(response.checkoutFormContent);
      } catch (e) {
        // already plain HTML
      }

      /*
       * Iyzico needs this div to render the form into
       * Must exist before iyziInit script runs
       */
      const targetDiv = document.createElement("div");
      targetDiv.id = "iyzipay-checkout-form";
      targetDiv.className = "responsive";
      formContainerRef.current.appendChild(targetDiv);

      /*
       * Parse the iyziInit script from response HTML
       * and execute it after target div exists in DOM
       */
      const tempDiv = document.createElement("div");
      tempDiv.innerHTML = htmlContent;
      const initScript = tempDiv.querySelector("script");

      if (initScript) {
        const newScript = document.createElement("script");
        newScript.textContent = initScript.textContent;

        /*
         * Wait for Iyzico bundle to load then check if form rendered
         */
        newScript.onload = () => {
          console.log("iyziInit script loaded");
        };

        document.body.appendChild(newScript);

        /*
         * Give bundle time to render after script executes
         * Then check if form appeared
         */
        setTimeout(() => {
          console.log(
            "iyzipay div contents after 2s:",
            document
              .getElementById("iyzipay-checkout-form")
              ?.innerHTML?.substring(0, 200),
          );
        }, 2000);
      }
    }

    setLoading(false);
  };

  return (
    <section className="bg-white max-w-7xl mx-auto min-h-screen dark:bg-gray-900 py-5">
      <div className="px-10">
        <h2 className="text-xl font-semibold text-gray-900 dark:text-white sm:text-2xl mb-6">
          Complete Payment
        </h2>

        {loading && (
          <div className="flex items-center justify-center py-20">
            <p className="text-gray-500 dark:text-gray-400">
              Loading payment form...
            </p>
          </div>
        )}

        {error && (
          <div className="rounded-lg border border-red-200 bg-red-50 p-4">
            <p className="text-sm text-red-700">{error}</p>
            <button
              onClick={() => navigate("/orders")}
              className="mt-3 text-sm font-medium text-red-700 underline"
            >
              Return to orders
            </button>
          </div>
        )}

        {/* Always mounted so ref is available before initPayment runs */}
        <div
          ref={formContainerRef}
          style={{ display: loading ? "none" : "block" }}
        />
      </div>
    </section>
  );
}
