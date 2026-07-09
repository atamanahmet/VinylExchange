import { useEffect, useMemo, useState } from "react";
import { useParams, useNavigate } from "react-router-dom";
import { toast } from "sonner";
import axios from "../api/axiosInstance";
import CheckoutAddressForm from "@/components/checkout/CheckoutAddressForm";
import { useOrderStore } from "../stores/orderStore";
import { useAppStore } from "../stores/appStore";
import { useAccountAddresses } from "../hooks/useAccountAddresses";

export default function ShipmentLabelPage() {
  const { orderId } = useParams();
  const navigate = useNavigate();

  const currentOrder = useOrderStore((state) => state.currentOrder);
  const isFetching = useOrderStore((state) => state.isFetchingCurrent);
  const fetchOrder = useOrderStore((state) => state.fetchOrder);
  const generateShipmentLabel = useOrderStore(
    (state) => state.generateShipmentLabel,
  );

  const {
    addresses,
    isFetching: isFetchingAddresses,
    isSaving,
    createAddress,
  } = useAccountAddresses();

  const [carriers, setCarriers] = useState([]);
  const [handlerCode, setHandlerCode] = useState("");
  const [sellerAddressId, setSellerAddressId] = useState("");
  const [showAddForm, setShowAddForm] = useState(false);
  const [isSubmitting, setIsSubmitting] = useState(false);

  const shippingAddresses = useMemo(
    () => addresses.filter((address) => address.addressType === "SHIPPING"),
    [addresses],
  );

  useEffect(() => {
    if (!currentOrder || currentOrder.orderId !== orderId) {
      fetchOrder(orderId);
    }
  }, [orderId, currentOrder, fetchOrder]);

  useEffect(() => {
    const loadCarriers = async () => {
      try {
        const res = await axios.get("/api/shipment/carriers");
        if (res.status === 200) {
          setCarriers(res.data);
        }
      } catch (err) {
        if (!err.response) useAppStore.getState().setBackendError(true);
      }
    };

    loadCarriers();
  }, []);

  useEffect(() => {
    if (isFetching || !currentOrder) return;

    const canAccess =
      currentOrder.viewerRole === "SELLER" && currentOrder.status === "PAID";

    if (!canAccess) {
      navigate(`/orders/${orderId}`, { replace: true });
    }
  }, [currentOrder, isFetching, navigate, orderId]);

  useEffect(() => {
    if (isFetchingAddresses) return;

    if (shippingAddresses.length === 0) {
      setShowAddForm(true);
      setSellerAddressId("");
      return;
    }

    setShowAddForm(false);
    setSellerAddressId((current) => {
      if (current && shippingAddresses.some((address) => address.id === current)) {
        return current;
      }
      const defaultAddress = shippingAddresses.find((address) => address.isDefault);
      return defaultAddress?.id ?? shippingAddresses[0].id;
    });
  }, [isFetchingAddresses, shippingAddresses]);

  const handleCreateAddress = async (payload) => {
    const result = await createAddress({
      ...payload,
      country: payload.country?.trim() || "TR",
      addressType: "SHIPPING",
      isDefault: shippingAddresses.length === 0,
    });

    if (!result.success) {
      toast.error(result.message || "Could not save address.");
      return;
    }

    toast.success("Sender address saved.");
    setSellerAddressId(result.data.id);
    setShowAddForm(false);
  };

  const handleSubmit = async (event) => {
    event.preventDefault();
    if (!handlerCode || !sellerAddressId || isSubmitting || showAddForm) return;

    setIsSubmitting(true);
    const ok = await generateShipmentLabel(
      orderId,
      handlerCode,
      sellerAddressId,
    );
    setIsSubmitting(false);

    if (ok) {
      navigate(`/orders/${orderId}`);
    }
  };

  if (isFetching || isFetchingAddresses) {
    return (
      <div className="max-w-3xl mx-auto px-4 py-8 flex flex-col gap-4">
        <div className="h-8 w-48 bg-neutral-900 rounded animate-pulse" />
        <div className="h-64 bg-neutral-900 rounded-xl animate-pulse" />
      </div>
    );
  }

  if (!currentOrder) {
    return (
      <div className="max-w-3xl mx-auto px-4 py-16 text-center">
        <p className="text-neutral-500 text-sm">Order not found.</p>
        <button
          onClick={() => navigate("/orders")}
          className="mt-4 text-sm text-amber-500 hover:text-amber-400"
        >
          Back to orders
        </button>
      </div>
    );
  }

  const canGenerateLabel = Boolean(handlerCode && sellerAddressId && !showAddForm);

  return (
    <div className="max-w-3xl mx-auto px-4 py-8">
      <button
        onClick={() => navigate(`/orders/${orderId}`)}
        className="flex items-center gap-1.5 text-sm text-neutral-500 hover:text-white mb-6 transition-colors"
      >
        <svg
          className="w-4 h-4"
          fill="none"
          stroke="currentColor"
          viewBox="0 0 24 24"
        >
          <path
            strokeLinecap="round"
            strokeLinejoin="round"
            strokeWidth={2}
            d="M15 19l-7-7 7-7"
          />
        </svg>
        Order
      </button>

      <h1 className="text-xl font-semibold text-white mb-2">
        Create shipping label{" "}
        <span className="font-mono text-neutral-400">
          #{currentOrder.orderNumber}
        </span>
      </h1>
      <p className="text-sm text-neutral-500 mb-6">
        Choose a carrier and sender address before generating your label.
      </p>

      <div className="space-y-4 mb-4">
        {shippingAddresses.length === 0 && (
          <div className="bg-neutral-900 border border-neutral-800 rounded-xl px-4 py-3">
            <p className="text-sm text-neutral-400">
              Add a shipping address to use as the sender on your label.
            </p>
          </div>
        )}

        {showAddForm && (
          <CheckoutAddressForm
            shippingAddressCount={shippingAddresses.length}
            isSaving={isSaving}
            onSubmit={handleCreateAddress}
            onCancel={() => setShowAddForm(false)}
            showCancel={shippingAddresses.length > 0}
          />
        )}

        {shippingAddresses.length > 0 && !showAddForm && (
          <div className="bg-neutral-900 border border-neutral-800 rounded-xl p-6">
            <div className="mb-4">
              <label className="text-xs text-neutral-500 mb-1 block">
                Sender address
              </label>
              <select
                value={sellerAddressId}
                onChange={(event) => setSellerAddressId(event.target.value)}
                className="w-full bg-neutral-800 border border-neutral-700 rounded-lg text-sm text-white p-2.5 focus:outline-none focus:border-neutral-500"
              >
                <option value="">Select address</option>
                {shippingAddresses.map((address) => (
                  <option key={address.id} value={address.id}>
                    {address.label} — {address.fullName}, {address.city}
                  </option>
                ))}
              </select>
            </div>
            <button
              type="button"
              onClick={() => setShowAddForm(true)}
              className="text-sm text-amber-500 hover:text-amber-400 transition-colors"
            >
              Add another sender address
            </button>
          </div>
        )}
      </div>

      {!showAddForm && sellerAddressId && (
        <form
          onSubmit={handleSubmit}
          className="bg-neutral-900 border border-neutral-800 rounded-xl p-6"
        >
          <div className="mb-6">
            <label className="text-xs text-neutral-500 mb-1 block">Carrier</label>
            <select
              value={handlerCode}
              onChange={(event) => setHandlerCode(event.target.value)}
              className="w-full bg-neutral-800 border border-neutral-700 rounded-lg text-sm text-white p-2.5 focus:outline-none focus:border-neutral-500"
            >
              <option value="">Select carrier</option>
              {carriers.map((carrier) => (
                <option key={carrier.code} value={carrier.code}>
                  {carrier.name}
                </option>
              ))}
            </select>
          </div>

          <div className="flex justify-end gap-2">
            <button
              type="button"
              onClick={() => navigate(`/orders/${orderId}`)}
              className="text-sm text-neutral-400 hover:text-white px-4 py-2 rounded-lg border border-neutral-700 hover:border-neutral-500 transition-colors"
            >
              Cancel
            </button>
            <button
              type="submit"
              disabled={!canGenerateLabel || isSubmitting}
              className="text-sm text-white bg-amber-600 hover:bg-amber-700 disabled:opacity-40 disabled:cursor-not-allowed px-4 py-2 rounded-lg transition-colors"
            >
              {isSubmitting ? "Generating..." : "Generate label"}
            </button>
          </div>
        </form>
      )}
    </div>
  );
}
