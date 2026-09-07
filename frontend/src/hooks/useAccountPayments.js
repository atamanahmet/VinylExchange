import { useEffect } from "react";
import { usePaymentStore } from "../stores/paymentStore";

export function useAccountPayments() {
  const payments = usePaymentStore((state) => state.payments);
  const isFetching = usePaymentStore((state) => state.isFetching);
  const fetchMyPayments = usePaymentStore((state) => state.fetchMyPayments);

  useEffect(() => {
    fetchMyPayments();
  }, [fetchMyPayments]);

  return {
    payments,
    isFetching,
    refresh: fetchMyPayments,
  };
}
