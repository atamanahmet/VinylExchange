import { useEffect, useState } from "react";
import { useParams, useNavigate } from "react-router-dom";
import { useOrderStore } from "../stores/orderStore";

const STATUS_CONFIG = {
  AWAITING_PAYMENT: {
    label: "Awaiting payment",
    color: "bg-brand/10 text-brand-fg border border-brand/20",
  },
  PAID: {
    label: "Paid",
    color: "bg-signal-fg/10 text-signal-fg border border-signal-fg/20",
  },
  SHIPPED: {
    label: "Shipped",
    color: "bg-promo-fg/10 text-promo-fg border border-promo-fg/20",
  },
  DELIVERED: {
    label: "Delivered",
    color: "bg-info/10 text-info-fg border border-info/20",
  },
  DISPUTED: {
    label: "Disputed",
    color: "bg-warning/10 text-warning-fg border border-warning/20",
  },
  COMPLETED: {
    label: "Completed",
    color: "bg-success/10 text-success-fg border border-success/20",
  },
  REFUNDED: {
    label: "Refunded",
    color: "bg-on-surface-muted/10 text-on-surface-muted border border-on-surface-muted/20",
  },
  CANCELLED: {
    label: "Cancelled",
    color: "bg-danger/10 text-danger-fg border border-danger/20",
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
    color: "bg-on-surface-muted/10 text-on-surface-muted",
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
    <div className="flex justify-between items-center py-2 border-b border-surface-2 last:border-0">
      <span className="text-sm text-on-surface-muted">{label}</span>
      <span className="text-sm text-on-surface-bright">{value}</span>
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
        <div className="h-8 w-48 bg-surface-1 rounded animate-pulse" />
        <div className="h-64 bg-surface-1 rounded-xl animate-pulse" />
        <div className="h-40 bg-surface-1 rounded-xl animate-pulse" />
      </div>
    );
  }

  if (!currentOrder) {
    return (
      <div className="max-w-3xl mx-auto px-4 py-16 text-center">
        <p className="text-on-surface-muted text-sm">Order not found.</p>
        <button
          onClick={() => navigate("/orders")}
          className="mt-4 text-sm text-brand-fg hover:text-brand-fg"
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
        className="flex items-center gap-1.5 text-sm text-on-surface-muted hover:text-on-surface mb-6 transition-colors"
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
          <h1 className="text-xl font-semibold text-on-surface">
            Order{" "}
            <span className="font-mono text-on-surface-muted">
              #{currentOrder.orderNumber}
            </span>
          </h1>
          <StatusBadge status={currentOrder.status} />
          {currentOrder.saleType === "TRADE" && (
            <span className="text-xs font-medium px-2.5 py-1 rounded-full bg-promo-fg/10 text-promo-fg border border-promo-fg/20">
              Trade
            </span>
          )}
        </div>
        <span className="text-sm text-on-surface-muted">
          {formatDate(currentOrder.createdAt)}
        </span>
      </div>

      {/* items */}
      <div className="bg-surface-1 border border-surface-2 rounded-xl overflow-hidden mb-4">
        <div className="px-4 py-3 border-b border-surface-2">
          <span className="text-xs font-medium text-on-surface-muted uppercase tracking-wider">
            Items
          </span>
        </div>

        {currentOrder.items.map((item, i) => (
          <div
            key={i}
            className={`flex items-center gap-4 px-4 py-4 ${
              i < currentOrder.items.length - 1
                ? "border-b border-surface-2"
                : ""
            }`}
          >
            {item.listingMainImageUrl ? (
              <img
                src={item.listingMainImageUrl}
                alt={item.listingTitle}
                className="w-14 h-14 rounded-lg object-cover bg-surface-2 shrink-0"
              />
            ) : (
              <div className="w-14 h-14 rounded-lg bg-surface-2 shrink-0 flex items-center justify-center">
                <svg
                  className="w-6 h-6 text-on-surface-faint"
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
                className="text-sm font-medium text-on-surface hover:text-brand-fg transition-colors text-left truncate block"
              >
                {item.listingTitle}
              </button>
              <p className="text-xs text-on-surface-muted mt-0.5">
                {formatPrice(item.unitPriceKurus)} × {item.quantity}
              </p>
            </div>

            <span className="text-sm font-medium text-on-surface-bright shrink-0">
              {formatPrice(item.subTotalKurus)}
            </span>
          </div>
        ))}

        {/* total row */}
        <div className="flex justify-between items-center px-4 py-3 bg-surface-base/50 border-t border-surface-2">
          <span className="text-sm text-on-surface-muted">Order total</span>
          <span className="text-base font-semibold text-on-surface">
            {formatPrice(currentOrder.totalPriceKurus)}
          </span>
        </div>
      </div>

      {/* order info */}
      <div className="bg-surface-1 border border-surface-2 rounded-xl px-4 py-2 mb-4">
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
        <div className="bg-surface-1 border border-surface-2 rounded-xl px-4 py-2 mb-4">
          <InfoRow label="Carrier" value={currentOrder.shipmentHandlerCode} />
          <InfoRow label="Tracking barcode" value={currentOrder.shipmentBarcode} />
          <div className="flex justify-between items-center py-2 border-b border-surface-2 last:border-0">
            <span className="text-sm text-on-surface-muted">Shipping label</span>
            {currentOrder.shipmentLabelUrl ? (
              <a
                href={currentOrder.shipmentLabelUrl}
                target="_blank"
                rel="noopener noreferrer"
                className="text-sm text-brand-fg hover:text-brand-fg transition-colors"
              >
                View label
              </a>
            ) : (
              <span className="text-sm text-on-surface-bright">—</span>
            )}
          </div>
          <InfoRow
            label="Label date"
            value={formatDate(currentOrder.shipmentLabelGeneratedAt)}
          />
        </div>
      )}

      {showGenerateLabelPrompt && (
        <div className="bg-surface-1 border border-surface-2 rounded-xl px-4 py-4 mb-4 flex items-center justify-between gap-4">
          <p className="text-sm text-on-surface-muted">Label not generated yet</p>
          <button
            onClick={() =>
              navigate(`/orders/${currentOrder.orderId}/shipment/label`)
            }
            className="text-sm text-on-brand bg-brand hover:bg-brand-hover px-4 py-2 rounded-lg transition-colors shrink-0"
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
              className="text-sm text-on-brand bg-success hover:bg-success-hover px-4 py-2 rounded-lg transition-colors"
            >
              Confirm delivery
            </button>
          )}
          {canDispute && (
            <button
              onClick={() => setDisputeModal(true)}
              className="text-sm text-warning-fg hover:text-accent-dim border border-warning/30 hover:border-warning/60 px-4 py-2 rounded-lg transition-colors"
            >
              Open dispute
            </button>
          )}
          {canCancel && (
            <button
              onClick={() => setCancelModal(true)}
              className="text-sm text-danger-fg hover:text-danger-bright border border-danger/30 hover:border-danger/60 px-4 py-2 rounded-lg transition-colors"
            >
              Cancel order
            </button>
          )}
        </div>
      )}

      {/* cancel modal */}
      {cancelModal && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/70">
          <div className="bg-surface-1 border border-surface-3 rounded-xl p-6 w-full max-w-md mx-4">
            <h2 className="text-base font-medium text-on-surface mb-1">
              Cancel order
            </h2>
            <p className="text-sm text-on-surface-muted mb-4">
              Provide a reason for cancellation.
            </p>
            <textarea
              value={cancelReason}
              onChange={(e) => setCancelReason(e.target.value)}
              placeholder="Reason..."
              rows={3}
              className="w-full bg-surface-2 border border-surface-3 rounded-lg text-sm text-on-surface placeholder-on-surface-muted p-3 resize-none focus:outline-none focus:border-surface-4"
            />
            <div className="flex justify-end gap-2 mt-4">
              <button
                onClick={() => {
                  setCancelModal(false);
                  setCancelReason("");
                }}
                className="text-sm text-on-surface-muted hover:text-on-surface px-4 py-2 rounded-lg border border-surface-3 hover:border-surface-4 transition-colors"
              >
                Go back
              </button>
              <button
                onClick={handleCancelConfirm}
                disabled={!cancelReason.trim()}
                className="text-sm text-white bg-danger hover:bg-danger-hover disabled:opacity-40 disabled:cursor-not-allowed px-4 py-2 rounded-lg transition-colors"
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
          <div className="bg-surface-1 border border-surface-3 rounded-xl p-6 w-full max-w-md mx-4">
            <h2 className="text-base font-medium text-on-surface mb-1">
              Open dispute
            </h2>
            <p className="text-sm text-on-surface-muted mb-4">
              Describe the issue with this order.
            </p>

            <label className="text-xs text-on-surface-muted mb-1 block">
              Reason
            </label>
            <select
              value={disputeReason}
              onChange={(e) => setDisputeReason(e.target.value)}
              className="w-full bg-surface-2 border border-surface-3 rounded-lg text-sm text-on-surface p-2.5 mb-3 focus:outline-none focus:border-surface-4"
            >
              <option value="ITEM_NOT_AS_DESCRIBED">
                Item not as described
              </option>
              <option value="ITEM_NOT_RECEIVED">Item not received</option>
              <option value="WRONG_ITEM_SENT">Wrong item sent</option>
              <option value="DAMAGED_ITEM">Item arrived damaged</option>
            </select>

            <label className="text-xs text-on-surface-muted mb-1 block">
              Details
            </label>
            <textarea
              value={disputeNote}
              onChange={(e) => setDisputeNote(e.target.value)}
              placeholder="Describe the problem..."
              rows={3}
              className="w-full bg-surface-2 border border-surface-3 rounded-lg text-sm text-on-surface placeholder-on-surface-muted p-3 resize-none focus:outline-none focus:border-surface-4"
            />
            <div className="flex justify-end gap-2 mt-4">
              <button
                onClick={() => {
                  setDisputeModal(false);
                  setDisputeNote("");
                }}
                className="text-sm text-on-surface-muted hover:text-on-surface px-4 py-2 rounded-lg border border-surface-3 hover:border-surface-4 transition-colors"
              >
                Go back
              </button>
              <button
                onClick={handleDisputeConfirm}
                disabled={!disputeNote.trim()}
                className="text-sm text-white bg-warning hover:bg-warning-hover disabled:opacity-40 disabled:cursor-not-allowed px-4 py-2 rounded-lg transition-colors"
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
