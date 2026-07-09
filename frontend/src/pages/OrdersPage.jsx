import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import { useOrderStore } from "../stores/orderStore";
import { useAuthStore } from "../stores/authStore";

/**
 * Status badge config — color + label per OrderStatus
 */
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

/**
 * Format kurus (smallest unit) to TL string
 */
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

/**
 * Single order card — shows items, status, totals, and relevant actions
 */
function OrderCard({
  order,
  isSeller,
  onPay,
  onConfirmDelivery,
  onCancel,
  onDispute,
  onOpenConversation,
}) {
  const [expanded, setExpanded] = useState(false);
  const navigate = useNavigate();
  const canPay = !isSeller && order.status === "AWAITING_PAYMENT";

  const canCreateShippingLabel =
    isSeller && order.status === "PAID" && !order.shipmentHandlerCode;
  const canConfirm = !isSeller && order.status === "SHIPPED";
  const canCancel =
    !isSeller && ["AWAITING_PAYMENT", "PAID"].includes(order.status);
  const canDispute = !isSeller && order.status === "SHIPPED";

  return (
    <div className="bg-neutral-900 border border-neutral-800 rounded-xl overflow-hidden">
      {/* header */}
      <div className="flex items-center justify-between px-4 py-3 border-b border-neutral-800">
        <div className="flex items-center gap-3">
          <button
            onClick={() => navigate(`/orders/${order.orderId}`)}
            className="text-xs text-neutral-500 font-mono hover:text-amber-400 transition-colors"
          >
            #{order.orderNumber}
          </button>
          <StatusBadge status={order.status} />
          {order.saleType === "TRADE" && (
            <span className="text-xs font-medium px-2.5 py-1 rounded-full bg-purple-500/10 text-purple-400 border border-purple-500/20">
              Trade
            </span>
          )}
        </div>
        <div className="flex items-center gap-3">
          <span className="text-xs text-neutral-500">
            {formatDate(order.createdAt)}
          </span>
          <button
            onClick={() => setExpanded(!expanded)}
            className="text-neutral-400 hover:text-white transition-colors"
            aria-label={expanded ? "Collapse order" : "Expand order"}
          >
            <svg
              className={`w-4 h-4 transition-transform ${expanded ? "rotate-180" : ""}`}
              fill="none"
              stroke="currentColor"
              viewBox="0 0 24 24"
            >
              <path
                strokeLinecap="round"
                strokeLinejoin="round"
                strokeWidth={2}
                d="M19 9l-7 7-7-7"
              />
            </svg>
          </button>
        </div>
      </div>

      {/* items preview — always visible */}
      <div className="px-4 py-3 flex flex-col gap-2">
        {order.items
          .slice(0, expanded ? order.items.length : 2)
          .map((item, i) => (
            <div key={i} className="flex items-center gap-3">
              {item.listingMainImageUrl ? (
                <img
                  src={item.listingMainImageUrl}
                  alt={item.listingTitle}
                  className="w-10 h-10 rounded-md object-cover bg-neutral-800 shrink-0"
                />
              ) : (
                <div className="w-10 h-10 rounded-md bg-neutral-800 shrink-0 flex items-center justify-center">
                  <svg
                    className="w-5 h-5 text-neutral-600"
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
                  className="text-sm text-white hover:text-amber-400 transition-colors truncate block text-left"
                >
                  {item.listingTitle}
                </button>
                <p className="text-xs text-neutral-500">
                  {item.quantity > 1 ? `${item.quantity}x ` : ""}
                  {formatPrice(item.unitPriceKurus)}
                </p>
              </div>
              <span className="text-sm text-neutral-300 shrink-0">
                {formatPrice(item.subTotalKurus)}
              </span>
            </div>
          ))}

        {!expanded && order.items.length > 2 && (
          <button
            onClick={() => setExpanded(true)}
            className="text-xs text-neutral-500 hover:text-neutral-300 transition-colors text-left"
          >
            +{order.items.length - 2} more item
            {order.items.length - 2 > 1 ? "s" : ""}
          </button>
        )}
      </div>

      {/* expanded details */}
      {expanded && (
        <div className="px-4 pb-3 border-t border-neutral-800 pt-3 flex flex-col gap-2">
          <div className="grid grid-cols-2 gap-x-4 gap-y-1 text-xs text-neutral-500">
            {order.shippingDeadline && (
              <>
                <span>Ship by</span>
                <span className="text-neutral-300">
                  {formatDate(order.shippingDeadline)}
                </span>
              </>
            )}
            {order.expectedDeliveryDate && (
              <>
                <span>Expected delivery</span>
                <span className="text-neutral-300">
                  {formatDate(order.expectedDeliveryDate)}
                </span>
              </>
            )}
            {order.deliveredAt && (
              <>
                <span>Delivered</span>
                <span className="text-neutral-300">
                  {formatDate(order.deliveredAt)}
                </span>
              </>
            )}
          </div>
        </div>
      )}

      {/* footer — total + actions */}
      <div className="flex items-center justify-between px-4 py-3 border-t border-neutral-800 bg-neutral-950/50">
        <span className="text-sm font-medium text-white">
          Total: {formatPrice(order.totalPriceKurus)}
        </span>
        <div className="flex items-center gap-2">
          <button
            onClick={() => onOpenConversation(order)}
            className="text-xs text-neutral-400 hover:text-white border border-neutral-700 hover:border-neutral-500 px-3 py-1.5 rounded-lg transition-colors"
          >
            Message
          </button>

          {canCreateShippingLabel && (
            <button
              onClick={() =>
                navigate(`/orders/${order.orderId}/shipment/label`)
              }
              className="text-xs text-white bg-amber-600 hover:bg-amber-700 px-3 py-1.5 rounded-lg transition-colors"
            >
              Create shipping label
            </button>
          )}

          {canConfirm && (
            <button
              onClick={() => onConfirmDelivery(order.orderId)}
              className="text-xs text-white bg-green-700 hover:bg-green-800 px-3 py-1.5 rounded-lg transition-colors"
            >
              Confirm delivery
            </button>
          )}

          {canDispute && (
            <button
              onClick={() => onDispute(order.orderId)}
              className="text-xs text-orange-400 hover:text-orange-300 border border-orange-500/30 hover:border-orange-500/60 px-3 py-1.5 rounded-lg transition-colors"
            >
              Open dispute
            </button>
          )}

          {canPay && (
            <button
              onClick={() => onPay(order.orderId)}
              className="text-xs text-white bg-amber-600 hover:bg-amber-700 px-3 py-1.5 rounded-lg transition-colors"
            >
              Complete Payment
            </button>
          )}

          {canCancel && (
            <button
              onClick={() => onCancel(order.orderId)}
              className="text-xs text-red-400 hover:text-red-300 border border-red-500/30 hover:border-red-500/60 px-3 py-1.5 rounded-lg transition-colors"
            >
              Cancel
            </button>
          )}
        </div>
      </div>
    </div>
  );
}

/**
 * Groups orders by counterpart username for display
 * Purchases grouped by seller, sales by buyer
 */
function OrderGroup({
  label,
  orders,
  isSeller,
  onPay,
  onConfirmDelivery,
  onCancel,
  onDispute,
  onOpenConversation,
}) {
  const navigate = useNavigate();

  return (
    <div className="flex flex-col gap-3">
      <div className="flex items-center gap-2">
        <span className="text-xs font-medium text-neutral-500 uppercase tracking-wider">
          {isSeller ? "Buyer" : "Seller"}
        </span>
        <button
          onClick={() => navigate(`/seller/${label}`)}
          className="text-sm font-medium text-white hover:text-amber-400 transition-colors"
        >
          {label}
        </button>
        <span className="text-xs text-neutral-600 ml-auto">
          {orders.length} order{orders.length !== 1 ? "s" : ""}
        </span>
      </div>
      {orders.map((order) => (
        <OrderCard
          key={order.orderId}
          order={order}
          isSeller={isSeller}
          onConfirmDelivery={onConfirmDelivery}
          onCancel={onCancel}
          onDispute={onDispute}
          onPay={onPay}
          onOpenConversation={onOpenConversation}
        />
      ))}
    </div>
  );
}

/**
 * Filter bar — filter by status
 */
const STATUS_FILTERS = [
  { label: "All", value: null },
  {
    label: "Active",
    value: ["AWAITING_PAYMENT", "PAID", "SHIPPED", "DELIVERED", "DISPUTED"],
  },
  { label: "Completed", value: ["COMPLETED"] },
  { label: "Cancelled / Refunded", value: ["CANCELLED", "REFUNDED"] },
];

export default function OrdersPage() {
  const navigate = useNavigate();
  const user = useAuthStore((state) => state.user);

  const purchases = useOrderStore((state) => state.purchases);
  const sales = useOrderStore((state) => state.sales);
  const isFetching = useOrderStore((state) => state.isFetching);
  const fetchPurchases = useOrderStore((state) => state.fetchPurchases);
  const fetchSales = useOrderStore((state) => state.fetchSales);
  const confirmDelivery = useOrderStore((state) => state.confirmDelivery);
  const cancelOrder = useOrderStore((state) => state.cancelOrder);
  const openDispute = useOrderStore((state) => state.openDispute);

  const [tab, setTab] = useState("purchases");
  const [statusFilter, setFilter] = useState(null);
  const [cancelModal, setCancelModal] = useState(null);
  const [cancelReason, setCancelReason] = useState("");

  const initiatePayment = useOrderStore((state) => state.initiatePayment);
  const setPendingOrderIds = useOrderStore((state) => state.setPendingOrderIds);

  const handlePay = (orderId) => {
    setPendingOrderIds([orderId]);
    navigate("/payment");
  };

  useEffect(() => {
    fetchPurchases();
    fetchSales();
  }, []);

  const activeOrders = tab === "purchases" ? purchases : sales;
  const isSeller = tab === "sales";

  const filtered = statusFilter
    ? activeOrders.filter((o) => statusFilter.includes(o.status))
    : activeOrders;

  /**
   * Groups orders by counterpart username for display
   * Purchases: group by sellerUsername, sales: group by buyerUsername
   */
  const grouped = filtered.reduce((acc, order) => {
    const key = isSeller ? order.buyerUsername : order.sellerUsername;
    if (!acc[key]) acc[key] = [];
    acc[key].push(order);
    return acc;
  }, {});

  const handleOpenConversation = (order) => {
    const listingPublicId = order.items?.[0]?.publicId;
    if (!listingPublicId) return;
    navigate(`/messaging/${listingPublicId}`);
  };

  const handleCancelConfirm = async () => {
    if (!cancelModal || !cancelReason.trim()) return;
    await cancelOrder(cancelModal, cancelReason.trim());
    setCancelModal(null);
    setCancelReason("");
    fetchPurchases();
  };

  const handleConfirmDelivery = async (orderId) => {
    await confirmDelivery(orderId);
    fetchPurchases();
  };

  const handleDispute = async (orderId) => {
    /* TODO: open dispute reason modal same as cancel */
    await openDispute(orderId, "ITEM_NOT_AS_DESCRIBED", "");
    fetchPurchases();
  };

  const tabs = [
    { id: "purchases", label: "Purchases", count: purchases.length },
    { id: "sales", label: "Sales", count: sales.length },
  ];

  return (
    <div className="max-w-3xl mx-auto px-4 py-8">
      <h1 className="text-xl font-semibold text-white mb-6">Orders</h1>

      {/* tabs */}
      <div className="flex gap-1 mb-6 bg-neutral-900 p-1 rounded-xl border border-neutral-800 w-fit">
        {tabs.map((t) => (
          <button
            key={t.id}
            onClick={() => setTab(t.id)}
            className={`flex items-center gap-2 px-4 py-2 rounded-lg text-sm font-medium transition-colors ${
              tab === t.id
                ? "bg-amber-600 text-white"
                : "text-neutral-400 hover:text-white"
            }`}
          >
            {t.label}
            {t.count > 0 && (
              <span
                className={`text-xs px-1.5 py-0.5 rounded-full ${
                  tab === t.id
                    ? "bg-amber-700 text-amber-100"
                    : "bg-neutral-800 text-neutral-400"
                }`}
              >
                {t.count}
              </span>
            )}
          </button>
        ))}
      </div>

      {/* status filter */}
      <div className="flex gap-2 mb-6 flex-wrap">
        {STATUS_FILTERS.map((f) => (
          <button
            key={f.label}
            onClick={() => setFilter(f.value)}
            className={`text-xs px-3 py-1.5 rounded-full border transition-colors ${
              statusFilter === f.value
                ? "border-amber-500 text-amber-400 bg-amber-500/10"
                : "border-neutral-700 text-neutral-400 hover:border-neutral-500 hover:text-neutral-300"
            }`}
          >
            {f.label}
          </button>
        ))}
      </div>

      {/* content */}
      {isFetching ? (
        <div className="flex flex-col gap-3">
          {Array(3)
            .fill(0)
            .map((_, i) => (
              <div
                key={i}
                className="h-28 bg-neutral-900 rounded-xl border border-neutral-800 animate-pulse"
              />
            ))}
        </div>
      ) : Object.keys(grouped).length === 0 ? (
        <div className="text-center py-16 text-neutral-500">
          <svg
            className="w-10 h-10 mx-auto mb-3 text-neutral-700"
            fill="none"
            stroke="currentColor"
            viewBox="0 0 24 24"
          >
            <path
              strokeLinecap="round"
              strokeLinejoin="round"
              strokeWidth={1.5}
              d="M20 7l-8-4-8 4m16 0l-8 4m8-4v10l-8 4m0-10L4 7m8 4v10M4 7v10l8 4"
            />
          </svg>
          <p className="text-sm">No {tab} found</p>
        </div>
      ) : (
        <div className="flex flex-col gap-8">
          {Object.entries(grouped).map(([counterpartUsername, orders]) => (
            <OrderGroup
              key={counterpartUsername}
              label={counterpartUsername}
              orders={orders}
              isSeller={isSeller}
              onConfirmDelivery={handleConfirmDelivery}
              onCancel={(orderId) => setCancelModal(orderId)}
              onDispute={handleDispute}
              onPay={handlePay}
              onOpenConversation={handleOpenConversation}
            />
          ))}
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
              Please provide a reason for cancellation.
            </p>
            <textarea
              value={cancelReason}
              onChange={(e) => setCancelReason(e.target.value)}
              placeholder="Reason for cancellation..."
              rows={3}
              className="w-full bg-neutral-800 border border-neutral-700 rounded-lg text-sm text-white placeholder-neutral-500 p-3 resize-none focus:outline-none focus:border-neutral-500"
            />
            <div className="flex justify-end gap-2 mt-4">
              <button
                onClick={() => {
                  setCancelModal(null);
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
    </div>
  );
}
