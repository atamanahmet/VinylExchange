import { useEffect, useMemo, useState } from "react";
import { useOrderStore } from "../stores/orderStore";

const CURRENT_STATUSES = [
  "AWAITING_PAYMENT",
  "PAID",
  "AWAITING_SHIPMENT",
  "SHIPPED",
  "IN_TRANSIT",
  "OUT_FOR_DELIVERY",
  "DELIVERED",
  "DISPUTED",
];

const ORDER_TABS = [
  { id: "current", label: "Current" },
  { id: "unpaid", label: "Unpaid" },
  { id: "all", label: "All orders" },
];

export function useAccountOrders() {
  const purchases = useOrderStore((state) => state.purchases);
  const isFetching = useOrderStore((state) => state.isFetching);
  const fetchPurchases = useOrderStore((state) => state.fetchPurchases);
  const [tab, setTab] = useState("current");

  useEffect(() => {
    fetchPurchases();
  }, [fetchPurchases]);

  const filteredOrders = useMemo(() => {
    if (tab === "all") return purchases;
    if (tab === "unpaid") {
      return purchases.filter((order) => order.status === "AWAITING_PAYMENT");
    }
    return purchases.filter((order) => CURRENT_STATUSES.includes(order.status));
  }, [purchases, tab]);

  return {
    orders: filteredOrders,
    isFetching,
    tab,
    setTab,
    tabs: ORDER_TABS,
    refresh: fetchPurchases,
  };
}
