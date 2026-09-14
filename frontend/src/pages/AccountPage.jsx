import { useEffect } from "react";
import { useLocation, useNavigate } from "react-router-dom";

import AccountSidebar from "@/components/account/AccountSidebar";
import AccountHeader from "@/components/account/AccountHeader";
import AccountOrdersSection from "@/components/account/AccountOrdersSection";
import AccountAddressesSection from "@/components/account/AccountAddressesSection";
import AccountPaymentsSection from "@/components/account/AccountPaymentsSection";
import AccountSecuritySection from "@/components/account/AccountSecuritySection";
import { useAuthStore } from "@/stores/authStore";

function resolveAccountSection(pathname) {
  if (pathname.endsWith("/addresses")) return "addresses";
  if (pathname.endsWith("/payments")) return "payments";
  if (pathname.endsWith("/security")) return "security";
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
      navigate("/login", { replace: true });
    }
  }, [isLoading, user, navigate]);

  if (!user) {
    return null;
  }

  return (
    <div className="min-h-screen bg-surface-base text-on-surface">
      <div className="mx-auto w-full max-w-6xl px-4 py-4 text-left sm:px-6 sm:py-5 lg:px-8">
        <div className="flex flex-col md:flex-row md:items-start gap-8">
          <AccountSidebar />

          <div className="flex-1 min-w-0">
            <AccountHeader />
            {section === "addresses" && <AccountAddressesSection />}
            {section === "payments" && <AccountPaymentsSection />}
            {section === "security" && <AccountSecuritySection />}
            {section === "orders" && <AccountOrdersSection />}
          </div>
        </div>
      </div>
    </div>
  );
}
