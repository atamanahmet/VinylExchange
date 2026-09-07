import { useEffect } from "react";
import { useLocation, useNavigate } from "react-router-dom";

import AccountSidebar from "@/components/account/AccountSidebar";
import AccountHeader from "@/components/account/AccountHeader";
import AccountOrdersSection from "@/components/account/AccountOrdersSection";
import AccountAddressesSection from "@/components/account/AccountAddressesSection";
import AccountPaymentsSection from "@/components/account/AccountPaymentsSection";
import { useAuthStore } from "@/stores/authStore";

function resolveAccountSection(pathname) {
  if (pathname.endsWith("/addresses")) return "addresses";
  if (pathname.endsWith("/payments")) return "payments";
  return "orders";
}

export default function AccountPage() {
  const navigate = useNavigate();
  const location = useLocation();
  const user = useAuthStore((state) => state.user);
  const isLoading = useAuthStore((state) => state.isLoading);

  const section = resolveAccountSection(location.pathname);

  useEffect(() => {
    if (!isLoading && !user) {
      navigate("/");
    }
  }, [isLoading, user, navigate]);

  if (!user) {
    return null;
  }

  return (
    <div className="min-h-screen bg-surface-base text-on-surface">
      <div className="w-full max-w-6xl mx-auto px-4 md:px-8 lg:px-10 py-8 text-left">
        <div className="flex flex-col md:flex-row md:items-start gap-8">
          <AccountSidebar />

          <div className="flex-1 min-w-0">
            <AccountHeader />
            {section === "addresses" && <AccountAddressesSection />}
            {section === "payments" && <AccountPaymentsSection />}
            {section === "orders" && <AccountOrdersSection />}
          </div>
        </div>
      </div>
    </div>
  );
}
