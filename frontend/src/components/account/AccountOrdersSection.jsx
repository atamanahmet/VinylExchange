import { cn } from "@/lib/utils";
import AccountOrderCard from "./AccountOrderCard";
import { useAccountOrders } from "@/hooks/useAccountOrders";

export default function AccountOrdersSection() {
  const { orders, isFetching, tab, setTab, tabs } = useAccountOrders();

  return (
    <section>
      <h2 className="text-lg font-semibold text-on-surface mb-4">My orders</h2>

      <div className="flex gap-1 mb-6 bg-surface-2 p-1 rounded-xl border border-accent-muted w-fit">
        {tabs.map((item) => (
          <button
            key={item.id}
            type="button"
            onClick={() => setTab(item.id)}
            className={cn(
              "px-4 py-2 rounded-lg text-sm font-medium transition-colors",
              tab === item.id
                ? "bg-brand text-white"
                : "text-on-surface-muted hover:text-on-surface",
            )}
          >
            {item.label}
          </button>
        ))}
      </div>

      {isFetching ? (
        <div className="flex flex-col gap-3">
          {Array.from({ length: 3 }).map((_, index) => (
            <div
              key={index}
              className="h-40 rounded-xl border border-accent-muted bg-surface-1 animate-pulse"
            />
          ))}
        </div>
      ) : orders.length === 0 ? (
        <div className="rounded-xl border border-accent-muted bg-surface-1 px-4 py-12 text-center text-sm text-on-surface-muted">
          No orders in this tab.
        </div>
      ) : (
        <div className="flex flex-col gap-4">
          {orders.map((order) => (
            <AccountOrderCard key={order.orderId} order={order} />
          ))}
        </div>
      )}
    </section>
  );
}
