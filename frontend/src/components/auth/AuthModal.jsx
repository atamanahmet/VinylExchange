import { useState } from "react";
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogHeader,
  DialogTitle,
} from "@/components/ui/dialog";
import { LoginForm } from "./login-form";
import { SignupForm } from "./signup-form";
import { useUIStore } from "@/stores/uiStore";

export function AuthModal() {
  const openLogin = useUIStore((state) => state.openLogin);
  const resolveLogin = useUIStore((state) => state.resolveLogin);
  const setOpenLogin = useUIStore((state) => state.setOpenLogin);

  const [authType, setAuthType] = useState("Login");

  function handleOpenChange(open) {
    if (open) {
      setOpenLogin(true);
      return;
    }

    resolveLogin(false);
    setOpenLogin(false);
    setAuthType("Login");
  }

  const isLogin = authType === "Login";

  return (
    <Dialog open={openLogin} onOpenChange={handleOpenChange}>
      <DialogContent className="bg-surface-1 text-on-surface sm:max-w-md">
        <DialogHeader>
          <DialogTitle>
            {isLogin ? "Login to your account" : "Create an account"}
          </DialogTitle>
          <DialogDescription className="text-on-surface-muted">
            {isLogin
              ? "Enter your username and password to sign in"
              : "Enter your information below to create your account"}
          </DialogDescription>
        </DialogHeader>

        {isLogin ? (
          <LoginForm onSwitchToRegister={() => setAuthType("Register")} />
        ) : (
          <SignupForm onSwitchToLogin={() => setAuthType("Login")} />
        )}
      </DialogContent>
    </Dialog>
  );
}
