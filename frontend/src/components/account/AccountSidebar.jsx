import { NavLink, useNavigate } from "react-router-dom";
import { toast } from "sonner";
import { cn } from "@/lib/utils";
import { useAuthStore } from "@/stores/authStore";

const NAV_ITEM_CLASS =
  "block w-full text-left px-3 py-2.5 rounded-lg text-sm transition-colors bg-surface-2/90 border border-surface-4/60";

const NAV_ITEMS = [
  { id: "orders", label: "My orders", to: "/account" },
  { id: "addresses", label: "Your addresses", to: "/account/addresses" },
  { id: "security", label: "Login & security", stub: true },
  { id: "payments", label: "Payments", to: "/account/payments" },
  { id: "archived", label: "Archived orders", stub: true },
  { id: "saved", label: "Saved items", to: "/wishlist" },
  { id: "support", label: "Customer support", to: "/contact" },
];

function stubClick(label) {
  toast.info(`${label} is not available yet.`);
}

export default function AccountSidebar() {
  const navigate = useNavigate();
  const logOut = useAuthStore((state) => state.logOut);

  const handleLogout = async () => {
    await logOut();
    navigate("/");
  };

  return (
    <nav className="w-full md:w-56 shrink-0 self-start">
      <ul className="flex flex-col items-stretch gap-1.5">
        {NAV_ITEMS.map((item) => {
          if (item.stub) {
            return (
              <li key={item.id} className="w-full">
                <button
                  type="button"
                  onClick={() => stubClick(item.label)}
                  className={cn(
                    NAV_ITEM_CLASS,
                    "text-on-surface-muted hover:bg-surface-3 hover:text-on-surface hover:border-surface-4",
                  )}
                >
                  {item.label}
                </button>
              </li>
            );
          }

          if (item.to.startsWith("/account")) {
            return (
              <li key={item.id} className="w-full">
                <NavLink
                  to={item.to}
                  end={item.to === "/account"}
                  className={({ isActive }) =>
                    cn(
                      NAV_ITEM_CLASS,
                      isActive
                        ? "bg-brand/20 border-brand/40 text-brand font-medium"
                        : "text-on-surface-muted hover:bg-surface-3 hover:text-on-surface hover:border-surface-4",
                    )
                  }
                >
                  {item.label}
                </NavLink>
              </li>
            );
          }

          return (
            <li key={item.id} className="w-full">
              <NavLink
                to={item.to}
                className={({ isActive }) =>
                  cn(
                    NAV_ITEM_CLASS,
                    isActive
                      ? "bg-brand/20 border-brand/40 text-brand font-medium"
                      : "text-on-surface-muted hover:bg-surface-3 hover:text-on-surface hover:border-surface-4",
                  )
                }
              >
                {item.label}
              </NavLink>
            </li>
          );
        })}

        <li className="w-full mt-2 pt-2 border-t border-surface-4/60">
          <button
            type="button"
            onClick={handleLogout}
            className={cn(
              NAV_ITEM_CLASS,
              "text-on-surface-muted hover:bg-surface-3 hover:text-on-surface hover:border-surface-4",
            )}
          >
            Log out
          </button>
        </li>
      </ul>
    </nav>
  );
}
