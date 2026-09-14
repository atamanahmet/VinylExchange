import { useState } from "react";
import { useNavigate } from "react-router-dom";
import { MoreVertical } from "lucide-react";
import { toast } from "sonner";

import { Button } from "@/components/ui/button";
import { cn } from "@/lib/utils";
import { buildListingPath } from "@/utils/listingPath";

const STATUS_CONFIG = {
  AWAITING_PAYMENT: {
    label: "Awaiting payment",
    color: "bg-brand/10 text-brand-fg border-brand/20",
  },
  PAID: {
    label: "Paid",
    color: "bg-signal-fg/10 text-signal-fg border-signal-fg/20",
  },
  AWAITING_SHIPMENT: {
    label: "Awaiting shipment",
    color: "bg-signal-fg/10 text-signal-fg border-signal-fg/20",
  },
  SHIPPED: {
    label: "Shipped",
    color: "bg-promo-fg/10 text-promo-fg border-promo-fg/20",
  },
  IN_TRANSIT: {
    label: "In transit",
    color: "bg-promo-fg/10 text-promo-fg border-promo-fg/20",
  },
  OUT_FOR_DELIVERY: {
    label: "Out for delivery",
    color: "bg-promo-fg/10 text-promo-fg border-promo-fg/20",
  },
  DELIVERED: {
    label: "Delivered",
    color: "bg-info/10 text-info-fg border-info/20",
  },
  DISPUTED: {
    label: "Disputed",
    color: "bg-warning/10 text-warning-fg border-warning/20",
  },
  COMPLETED: {
    label: "Completed",
    color: "bg-success/10 text-success-fg border-success/20",
  },
  RETURNING: {
    label: "Returning",
    color: "bg-brand/10 text-brand border-brand/20",
  },
  RETURNED: {
    label: "Returned",
    color: "bg-brand/10 text-brand border-brand/20",
  },
  REFUNDED: {
    label: "Refunded",
    color: "bg-on-surface-muted/10 text-on-surface-muted border-on-surface-muted/20",
  },
  CANCELLED: {
    label: "Cancelled",
    color: "bg-danger/10 text-danger-fg border-danger/20",
  },
  LOST: {
    label: "Lost",
    color: "bg-danger/10 text-danger-fg border-danger/20",
  },
};

function formatPrice(kurus) {
  if (kurus == null) return "—";
  return `₺${(kurus / 100).toFixed(2)}`;
}

function formatDateTime(isoString) {
  if (!isoString) return "—";
  return new Date(isoString).toLocaleString("tr-TR", {
    day: "2-digit",
    month: "short",
    year: "numeric",
    hour: "2-digit",
    minute: "2-digit",
  });
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
      className={cn(
        "text-xs font-medium px-2.5 py-1 rounded-full border",
        config.color,
      )}
    >
      {config.label}
    </span>
  );
}

export default function AccountOrderCard({ order }) {
  const navigate = useNavigate();
  const [menuOpen, setMenuOpen] = useState(false);

  const productCount = order.items?.reduce(
    (sum, item) => sum + (item.quantity || 0),
    0,
  );
  const deliveryDate = order.deliveredAt || order.expectedDeliveryDate;

  const handleDownloadInvoice = () => {
    // TODO: wire to invoice generation endpoint when backend supports it
    toast.info("Invoice download is not available yet.");
  };

  const handleStubMenuAction = () => {
    setMenuOpen(false);
    toast.info("Order actions are not available yet.");
  };

  return (
    <article className="rounded-xl border border-accent-muted bg-surface-1 overflow-hidden">
      <header className="flex flex-wrap items-start justify-between gap-3 px-4 py-3 border-b border-accent-muted">
        <div className="space-y-1">
          <button
            type="button"
            onClick={() => navigate(`/orders/${order.orderId}`)}
            className="text-sm font-medium text-on-surface hover:text-brand transition-colors"
          >
            Order #{order.orderNumber}
          </button>
          <p className="text-xs text-on-surface-muted">
            {productCount} item{productCount === 1 ? "" : "s"} · Ordered by{" "}
            {order.buyerUsername}
          </p>
        </div>

        <div className="flex items-center gap-2">
          <StatusBadge status={order.status} />
          <div className="relative">
            <Button
              type="button"
              variant="ghost"
              size="icon-sm"
              aria-label="Order actions"
              onClick={() => setMenuOpen((open) => !open)}
            >
              <MoreVertical className="size-4" />
            </Button>
            {menuOpen && (
              <div className="absolute right-0 top-full z-10 mt-1 min-w-40 rounded-lg border border-accent-muted bg-surface-1 py-1 shadow-lg">
                <button
                  type="button"
                  onClick={handleStubMenuAction}
                  className="w-full px-3 py-2 text-left text-sm text-on-surface-muted hover:bg-surface-2 hover:text-on-surface"
                >
                  View details
                </button>
                <button
                  type="button"
                  onClick={handleStubMenuAction}
                  className="w-full px-3 py-2 text-left text-sm text-on-surface-muted hover:bg-surface-2 hover:text-on-surface"
                >
                  Contact seller
                </button>
              </div>
            )}
          </div>
        </div>
      </header>

      <div className="grid gap-3 px-4 py-3 sm:grid-cols-2 text-sm">
        <div>
          <p className="text-on-surface-muted">Order date</p>
          <p className="text-on-surface">{formatDateTime(order.createdAt)}</p>
        </div>
        <div>
          <p className="text-on-surface-muted">Delivery date</p>
          <p className="text-on-surface">{formatDate(deliveryDate)}</p>
        </div>
        <div className="sm:col-span-2">
          <p className="text-on-surface-muted">Delivery address</p>
          <p className="text-on-surface">
            {order.shippingAddressSummary || "—"}
          </p>
        </div>
      </div>

      <div className="px-4 py-3 border-t border-accent-muted flex flex-col gap-3">
        {order.items?.map((item) => (
          <div key={item.publicId || item.listingTitle} className="flex items-center gap-3">
            {item.listingMainImageUrl ? (
              <img
                src={item.listingMainImageUrl}
                alt={item.listingTitle}
                className="size-12 rounded-md object-cover bg-surface-2 shrink-0"
              />
            ) : (
              <div className="size-12 rounded-md bg-surface-2 shrink-0" />
            )}
            <div className="flex-1 min-w-0">
              <button
                type="button"
                onClick={() => {
                  if (!item.publicId) return;
                  navigate(buildListingPath({ publicId: item.publicId, title: item.listingTitle }));
                }}
                className="text-sm font-medium text-on-surface hover:text-brand truncate block text-left"
              >
                {item.listingTitle}
              </button>
              <p className="text-xs text-on-surface-muted">
                Qty {item.quantity} · {formatPrice(item.unitPriceKurus)} each
              </p>
            </div>
            <p className="text-sm text-on-surface shrink-0">
              {formatPrice(item.subTotalKurus)}
            </p>
          </div>
        ))}
      </div>

      <footer className="flex flex-wrap items-center justify-between gap-3 px-4 py-3 border-t border-accent-muted bg-surface-2/40">
        <p className="text-sm font-semibold text-on-surface">
          Total {formatPrice(order.totalPriceKurus)}
        </p>
        <Button type="button" variant="outline" size="sm" onClick={handleDownloadInvoice}>
          Download invoice
        </Button>
      </footer>
    </article>
  );
}
