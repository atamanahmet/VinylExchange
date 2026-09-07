import AccountPaymentCard from "./AccountPaymentCard";
import { useAccountPayments } from "@/hooks/useAccountPayments";

export default function AccountPaymentsSection() {
  const { payments, isFetching } = useAccountPayments();

  return (
    <section>
      <h2 className="text-lg font-semibold text-on-surface mb-4">Payments</h2>
      <p className="text-sm text-on-surface-muted mb-6">
        Your payment and escrow history for purchases.
      </p>

      {isFetching ? (
        <div className="flex flex-col gap-3">
          {Array.from({ length: 3 }).map((_, index) => (
            <div
              key={index}
              className="h-36 rounded-xl border border-surface-4 bg-surface-1 animate-pulse"
            />
          ))}
        </div>
      ) : payments.length === 0 ? (
        <div className="rounded-xl border border-surface-4 bg-surface-1 px-4 py-12 text-center text-sm text-on-surface-muted">
          No payment history yet.
        </div>
      ) : (
        <div className="flex flex-col gap-4">
          {payments.map((payment) => (
            <AccountPaymentCard
              key={payment.paymentTransactionId}
              payment={payment}
            />
          ))}
        </div>
      )}
    </section>
  );
}
