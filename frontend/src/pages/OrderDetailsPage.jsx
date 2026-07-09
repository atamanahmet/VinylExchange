import { useEffect, useState } from "react";
import { useParams, useNavigate } from "react-router-dom";
import { useOrderStore } from "../stores/orderStore";

const STATUS_CONFIG = {
  AWAITING_PAYMENT: {
    label: "Awaiting payment",
    color: "bg-yellow-500/10 text-yellow-400 border border-yellow-500/20",
  },
  PAID: {
    label: "Paid",
    color: "bg-blue-500/10 text-blue-400 border border-blue-500/20",
  },
  SHIPPED: {
    label: "Shipped",
    color: "bg-indigo-500/10 text-indigo-400 border border-indigo-500/20",
  },
  DELIVERED: {
    label: "Delivered",
    color: "bg-teal-500/10 text-teal-400 border border-teal-500/20",
  },
  DISPUTED: {
    label: "Disputed",
    color: "bg-orange-500/10 text-orange-400 border border-orange-500/20",
  },
  COMPLETED: {
    label: "Completed",
    color: "bg-green-500/10 text-green-400 border border-green-500/20",
  },
  REFUNDED: {
    label: "Refunded",
    color: "bg-gray-500/10 text-gray-400 border border-gray-500/20",
  },
  CANCELLED: {
    label: "Cancelled",
    color: "bg-red-500/10 text-red-400 border border-red-500/20",
  },
};

function formatPrice(kurus) {
  if (kurus == null) return "—";
  return `₺${(kurus / 100).toFixed(2)}`;
}

function formatDate(isoString) {
  if (!isoString) return "—";
  return new Date(isoString).toLocaleDateString("tr-TR", {
    day: "2-digit",
    month: "short",
    year: "numeric",
  });
}

function StatusBadge({ status }) {
  const config = STATUS_CONFIG[status] || {
    label: status,
    color: "bg-gray-500/10 text-gray-400",
  };
  return (
    <span
      className={`text-xs font-medium px-2.5 py-1 rounded-full ${config.color}`}
    >
      {config.label}
    </span>
  );
}

function InfoRow({ label, value }) {
  if (!value) return null;
  return (
    <div className="flex justify-between items-center py-2 border-b border-neutral-800 last:border-0">
      <span className="text-sm text-neutral-500">{label}</span>
      <span className="text-sm text-neutral-200">{value}</span>
    </div>
  );
}

export default function OrderDetailPage() {
  const { orderId } = useParams();
  const navigate = useNavigate();

  const currentOrder = useOrderStore((state) => state.currentOrder);
  const isFetching = useOrderStore((state) => state.isFetchingCurrent);
  const fetchOrder = useOrderStore((state) => state.fetchOrder);
  const confirmDelivery = useOrderStore((state) => state.confirmDelivery);
  const cancelOrder = useOrderStore((state) => state.cancelOrder);
  const openDispute = useOrderStore((state) => state.openDispute);

  const [cancelModal, setCancelModal] = useState(false);
  const [disputeModal, setDisputeModal] = useState(false);
  const [cancelReason, setCancelReason] = useState("");
  const [disputeReason, setDisputeReason] = useState("ITEM_NOT_AS_DESCRIBED");
  const [disputeNote, setDisputeNote] = useState("");

  useEffect(() => {
    fetchOrder(orderId);
  }, [orderId]);

  if (isFetching) {
    return (
      <div className="max-w-3xl mx-auto px-4 py-8 flex flex-col gap-4">
        <div className="h-8 w-48 bg-neutral-900 rounded animate-pulse" />
        <div className="h-64 bg-neutral-900 rounded-xl animate-pulse" />
        <div className="h-40 bg-neutral-900 rounded-xl animate-pulse" />
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

  const isSeller = currentOrder.viewerRole === "SELLER";

  const canConfirm = !isSeller && currentOrder.status === "SHIPPED";
  const canCancel =
    !isSeller && ["AWAITING_PAYMENT", "PAID"].includes(currentOrder.status);
  const canDispute = !isSeller && currentOrder.status === "SHIPPED";
  const showGenerateLabelPrompt =
    isSeller &&
    currentOrder.status === "PAID" &&
    !currentOrder.shipmentHandlerCode;

  const handleConfirm = async () => {
    await confirmDelivery(currentOrder.orderId);
  };

  const handleCancelConfirm = async () => {
    if (!cancelReason.trim()) return;
    const ok = await cancelOrder(currentOrder.orderId, cancelReason.trim());
    if (ok) {
      setCancelModal(false);
      setCancelReason("");
      fetchOrder(orderId);
    }
  };

  const handleDisputeConfirm = async () => {
    if (!disputeNote.trim()) return;
    const ok = await openDispute(
      currentOrder.orderId,
      disputeReason,
      disputeNote.trim(),
    );
    if (ok) {
      setDisputeModal(false);
      setDisputeNote("");
      fetchOrder(orderId);
    }
  };

  return (
    <div className="max-w-3xl mx-auto px-4 py-8">
      {/* back */}
      <button
        onClick={() => navigate("/orders")}
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
        Orders
      </button>

      {/* header */}
      <div className="flex items-center justify-between mb-6">
        <div className="flex items-center gap-3">
          <h1 className="text-xl font-semibold text-white">
            Order{" "}
            <span className="font-mono text-neutral-400">
              #{currentOrder.orderNumber}
            </span>
          </h1>
          <StatusBadge status={currentOrder.status} />
          {currentOrder.saleType === "TRADE" && (
            <span className="text-xs font-medium px-2.5 py-1 rounded-full bg-purple-500/10 text-purple-400 border border-purple-500/20">
              Trade
            </span>
          )}
        </div>
        <span className="text-sm text-neutral-500">
          {formatDate(currentOrder.createdAt)}
        </span>
      </div>

      {/* items */}
      <div className="bg-neutral-900 border border-neutral-800 rounded-xl overflow-hidden mb-4">
        <div className="px-4 py-3 border-b border-neutral-800">
          <span className="text-xs font-medium text-neutral-500 uppercase tracking-wider">
            Items
          </span>
        </div>

        {currentOrder.items.map((item, i) => (
          <div
            key={i}
            className={`flex items-center gap-4 px-4 py-4 ${
              i < currentOrder.items.length - 1
                ? "border-b border-neutral-800"
                : ""
            }`}
          >
            {item.listingMainImageUrl ? (
              <img
                src={item.listingMainImageUrl}
                alt={item.listingTitle}
                className="w-14 h-14 rounded-lg object-cover bg-neutral-800 shrink-0"
              />
            ) : (
              <div className="w-14 h-14 rounded-lg bg-neutral-800 shrink-0 flex items-center justify-center">
                <svg
                  className="w-6 h-6 text-neutral-600"
                  fill="none"
                  stroke="currentColor"
                  viewBox="0 0 24 24"
                >
                  <path
                    strokeLinecap="round"
                    strokeLinejoin="round"
                    strokeWidth={1.5}
                    d="M9 19V6l12-3v13M9 19c0 1.105-.895 2-2 2s-2-.895-2-2 .895-2 2-2 2 .895 2 2zm12-3c0 1.105-.895 2-2 2s-2-.895-2-2 .895-2 2-2 2 .895 2 2z"
                  />
                </svg>
              </div>
            )}

            <div className="flex-1 min-w-0">
              <button
                onClick={() => navigate(`/item/${item.listingId}`)}
                className="text-sm font-medium text-white hover:text-amber-400 transition-colors text-left truncate block"
              >
                {item.listingTitle}
              </button>
              <p className="text-xs text-neutral-500 mt-0.5">
                {formatPrice(item.unitPriceKurus)} × {item.quantity}
              </p>
            </div>

            <span className="text-sm font-medium text-neutral-200 shrink-0">
              {formatPrice(item.subTotalKurus)}
            </span>
          </div>
        ))}

        {/* total row */}
        <div className="flex justify-between items-center px-4 py-3 bg-neutral-950/50 border-t border-neutral-800">
          <span className="text-sm text-neutral-500">Order total</span>
          <span className="text-base font-semibold text-white">
            {formatPrice(currentOrder.totalPriceKurus)}
          </span>
        </div>
      </div>

      {/* order info */}
      <div className="bg-neutral-900 border border-neutral-800 rounded-xl px-4 py-2 mb-4">
        <InfoRow
          label="Order placed"
          value={formatDate(currentOrder.createdAt)}
        />
        <InfoRow
          label="Ship by"
          value={formatDate(currentOrder.shippingDeadline)}
        />
        <InfoRow
          label="Expected delivery"
          value={formatDate(currentOrder.expectedDeliveryDate)}
        />
        <InfoRow
          label="Delivered"
          value={formatDate(currentOrder.deliveredAt)}
        />
        <InfoRow
          label="Sale type"
          value={currentOrder.saleType === "TRADE" ? "Trade" : "Purchase"}
        />
        <InfoRow label="Payment date" value={formatDate(currentOrder.paidAt)} />
      </div>

      {currentOrder.shipmentHandlerCode && (
        <div className="bg-neutral-900 border border-neutral-800 rounded-xl px-4 py-2 mb-4">
          <InfoRow label="Carrier" value={currentOrder.shipmentHandlerCode} />
          <InfoRow label="Tracking barcode" value={currentOrder.shipmentBarcode} />
          <div className="flex justify-between items-center py-2 border-b border-neutral-800 last:border-0">
            <span className="text-sm text-neutral-500">Shipping label</span>
            {currentOrder.shipmentLabelUrl ? (
              <a
                href={currentOrder.shipmentLabelUrl}
                target="_blank"
                rel="noopener noreferrer"
                className="text-sm text-amber-500 hover:text-amber-400 transition-colors"
              >
                View label
              </a>
            ) : (
              <span className="text-sm text-neutral-200">—</span>
            )}
          </div>
          <InfoRow
            label="Label date"
            value={formatDate(currentOrder.shipmentLabelGeneratedAt)}
          />
        </div>
      )}

      {showGenerateLabelPrompt && (
        <div className="bg-neutral-900 border border-neutral-800 rounded-xl px-4 py-4 mb-4 flex items-center justify-between gap-4">
          <p className="text-sm text-neutral-400">Label not generated yet</p>
          <button
            onClick={() =>
              navigate(`/orders/${currentOrder.orderId}/shipment/label`)
            }
            className="text-sm text-white bg-amber-600 hover:bg-amber-700 px-4 py-2 rounded-lg transition-colors shrink-0"
          >
            Generate shipping label
          </button>
        </div>
      )}

      {/* actions */}
      {(canConfirm || canCancel || canDispute) && (
        <div className="flex flex-wrap gap-2 justify-end mt-6">
          {canConfirm && (
            <button
              onClick={handleConfirm}
              className="text-sm text-white bg-green-700 hover:bg-green-800 px-4 py-2 rounded-lg transition-colors"
            >
              Confirm delivery
            </button>
          )}
          {canDispute && (
            <button
              onClick={() => setDisputeModal(true)}
              className="text-sm text-orange-400 hover:text-orange-300 border border-orange-500/30 hover:border-orange-500/60 px-4 py-2 rounded-lg transition-colors"
            >
              Open dispute
            </button>
          )}
          {canCancel && (
            <button
              onClick={() => setCancelModal(true)}
              className="text-sm text-red-400 hover:text-red-300 border border-red-500/30 hover:border-red-500/60 px-4 py-2 rounded-lg transition-colors"
            >
              Cancel order
            </button>
          )}
        </div>
      )}

      {/* cancel modal */}
      {cancelModal && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/70">
          <div className="bg-neutral-900 border border-neutral-700 rounded-xl p-6 w-full max-w-md mx-4">
            <h2 className="text-base font-medium text-white mb-1">
              Cancel order
            </h2>
            <p className="text-sm text-neutral-400 mb-4">
              Provide a reason for cancellation.
            </p>
            <textarea
              value={cancelReason}
              onChange={(e) => setCancelReason(e.target.value)}
              placeholder="Reason..."
              rows={3}
              className="w-full bg-neutral-800 border border-neutral-700 rounded-lg text-sm text-white placeholder-neutral-500 p-3 resize-none focus:outline-none focus:border-neutral-500"
            />
            <div className="flex justify-end gap-2 mt-4">
              <button
                onClick={() => {
                  setCancelModal(false);
                  setCancelReason("");
                }}
                className="text-sm text-neutral-400 hover:text-white px-4 py-2 rounded-lg border border-neutral-700 hover:border-neutral-500 transition-colors"
              >
                Go back
              </button>
              <button
                onClick={handleCancelConfirm}
                disabled={!cancelReason.trim()}
                className="text-sm text-white bg-red-700 hover:bg-red-800 disabled:opacity-40 disabled:cursor-not-allowed px-4 py-2 rounded-lg transition-colors"
              >
                Confirm cancel
              </button>
            </div>
          </div>
        </div>
      )}

      {/* dispute modal */}
      {disputeModal && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/70">
          <div className="bg-neutral-900 border border-neutral-700 rounded-xl p-6 w-full max-w-md mx-4">
            <h2 className="text-base font-medium text-white mb-1">
              Open dispute
            </h2>
            <p className="text-sm text-neutral-400 mb-4">
              Describe the issue with this order.
            </p>

            <label className="text-xs text-neutral-500 mb-1 block">
              Reason
            </label>
            <select
              value={disputeReason}
              onChange={(e) => setDisputeReason(e.target.value)}
              className="w-full bg-neutral-800 border border-neutral-700 rounded-lg text-sm text-white p-2.5 mb-3 focus:outline-none focus:border-neutral-500"
            >
              <option value="ITEM_NOT_AS_DESCRIBED">
                Item not as described
              </option>
              <option value="ITEM_NOT_RECEIVED">Item not received</option>
              <option value="WRONG_ITEM_SENT">Wrong item sent</option>
              <option value="DAMAGED_ITEM">Item arrived damaged</option>
            </select>

            <label className="text-xs text-neutral-500 mb-1 block">
              Details
            </label>
            <textarea
              value={disputeNote}
              onChange={(e) => setDisputeNote(e.target.value)}
              placeholder="Describe the problem..."
              rows={3}
              className="w-full bg-neutral-800 border border-neutral-700 rounded-lg text-sm text-white placeholder-neutral-500 p-3 resize-none focus:outline-none focus:border-neutral-500"
            />
            <div className="flex justify-end gap-2 mt-4">
              <button
                onClick={() => {
                  setDisputeModal(false);
                  setDisputeNote("");
                }}
                className="text-sm text-neutral-400 hover:text-white px-4 py-2 rounded-lg border border-neutral-700 hover:border-neutral-500 transition-colors"
              >
                Go back
              </button>
              <button
                onClick={handleDisputeConfirm}
                disabled={!disputeNote.trim()}
                className="text-sm text-white bg-orange-700 hover:bg-orange-800 disabled:opacity-40 disabled:cursor-not-allowed px-4 py-2 rounded-lg transition-colors"
              >
                Submit dispute
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
