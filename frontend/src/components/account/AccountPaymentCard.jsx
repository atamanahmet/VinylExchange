import { useState } from "react";
import { useNavigate } from "react-router-dom";
import { ChevronDown } from "lucide-react";

import { Button } from "@/components/ui/button";
import { cn } from "@/lib/utils";

const STATUS_CONFIG = {
  PENDING_PAYMENT: {
    label: "Pending payment",
    color: "bg-yellow-500/10 text-yellow-600 border-yellow-500/20",
  },
  HELD: {
    label: "Held in escrow",
    color: "bg-blue-500/10 text-blue-600 border-blue-500/20",
  },
  RELEASED: {
    label: "Released to seller",
    color: "bg-indigo-500/10 text-indigo-600 border-indigo-500/20",
  },
  COMPLETED: {
    label: "Completed",
    color: "bg-green-500/10 text-green-600 border-green-500/20",
  },
  REFUNDED: {
    label: "Refunded",
    color: "bg-gray-500/10 text-gray-600 border-gray-500/20",
  },
  CANCELLED: {
    label: "Cancelled",
    color: "bg-red-500/10 text-red-600 border-red-500/20",
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

function formatStatus(status) {
  return STATUS_CONFIG[status]?.label ?? status?.replaceAll("_", " ").toLowerCase();
}

function StatusBadge({ status }) {
  const config = STATUS_CONFIG[status] || {
    label: status,
    color: "bg-gray-500/10 text-gray-600 border-gray-500/20",
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

export default function AccountPaymentCard({ payment }) {
  const navigate = useNavigate();
  const [expanded, setExpanded] = useState(false);
  const hasEvents = payment.events?.length > 0;

  return (
    <article className="rounded-xl border border-surface-4 bg-surface-1 overflow-hidden">
      <header className="flex flex-wrap items-start justify-between gap-3 px-4 py-3 border-b border-surface-4/70">
        <div className="space-y-1">
          <button
            type="button"
            onClick={() => navigate(`/orders/${payment.orderId}`)}
            className="text-sm font-medium text-on-surface hover:text-brand transition-colors"
          >
            Order #{payment.orderNumber}
          </button>
          <p className="text-xs text-on-surface-muted">
            Seller {payment.sellerUsername || "—"}
          </p>
        </div>
        <div className="flex flex-col items-end gap-2">
          <StatusBadge status={payment.status} />
          {payment.refundReviewRequired && (
            <span className="text-xs px-2 py-0.5 rounded-full bg-orange-500/10 text-orange-600 border border-orange-500/20">
              Under review
            </span>
          )}
        </div>
      </header>

      <div className="grid gap-3 px-4 py-3 sm:grid-cols-3 text-sm">
        <div>
          <p className="text-on-surface-muted">Amount</p>
          <p className="text-on-surface font-medium">{formatPrice(payment.amountKurus)}</p>
        </div>
        <div>
          <p className="text-on-surface-muted">Started</p>
          <p className="text-on-surface">{formatDateTime(payment.createdAt)}</p>
        </div>
        <div>
          <p className="text-on-surface-muted">Captured</p>
          <p className="text-on-surface">{formatDateTime(payment.capturedAt)}</p>
        </div>
      </div>

      {hasEvents && (
        <div className="border-t border-surface-4/70">
          <button
            type="button"
            onClick={() => setExpanded((open) => !open)}
            className="flex w-full items-center justify-between px-4 py-3 text-sm text-on-surface-muted hover:text-on-surface hover:bg-surface-2/50 transition-colors"
          >
            <span>Payment history ({payment.events.length})</span>
            <ChevronDown
              className={cn("size-4 transition-transform", expanded && "rotate-180")}
            />
          </button>

          {expanded && (
            <ol className="px-4 pb-4 space-y-3">
              {payment.events.map((event, index) => (
                <li
                  key={`${event.occurredAt}-${index}`}
                  className="rounded-lg border border-surface-4/70 bg-surface-2/40 px-3 py-2"
                >
                  <div className="flex flex-wrap items-center justify-between gap-2">
                    <p className="text-sm text-on-surface">
                      {event.fromStatus === event.toStatus
                        ? formatStatus(event.toStatus)
                        : `${formatStatus(event.fromStatus)} → ${formatStatus(event.toStatus)}`}
                    </p>
                    <p className="text-xs text-on-surface-muted">
                      {formatDateTime(event.occurredAt)}
                    </p>
                  </div>
                  {event.note && (
                    <p className="text-xs text-on-surface-muted mt-1">{event.note}</p>
                  )}
                </li>
              ))}
            </ol>
          )}
        </div>
      )}

      <footer className="flex justify-end px-4 py-3 border-t border-surface-4/70 bg-surface-2/30">
        <Button
          type="button"
          variant="outline"
          size="sm"
          onClick={() => navigate(`/orders/${payment.orderId}`)}
        >
          View order
        </Button>
      </footer>
    </article>
  );
}
