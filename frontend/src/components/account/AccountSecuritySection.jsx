import { useMemo, useState } from "react";
import { Check, Circle } from "lucide-react";
import { toast } from "sonner";
import { useNavigate } from "react-router-dom";

import ConfirmDialog from "@/components/shared/ConfirmDialog";
import { Button } from "@/components/ui/button";
import {
  Field,
  FieldDescription,
  FieldGroup,
  FieldLabel,
} from "@/components/ui/field";
import { Input } from "@/components/ui/input";
import { cn } from "@/lib/utils";
import { useAuthStore } from "@/stores/authStore";
import {
  getPasswordStrengthChecks,
  isPasswordStrong,
  PASSWORD_STRENGTH_RULES,
} from "@/utils/passwordStrength";

const EMPTY_FORM = {
  currentPassword: "",
  newPassword: "",
  confirmPassword: "",
};

export default function AccountSecuritySection() {
  const navigate = useNavigate();
  const changePassword = useAuthStore((state) => state.changePassword);

  const [form, setForm] = useState(EMPTY_FORM);
  const [confirmOpen, setConfirmOpen] = useState(false);
  const [isPending, setIsPending] = useState(false);

  const strengthChecks = useMemo(
    () => getPasswordStrengthChecks(form.newPassword),
    [form.newPassword],
  );

  const passwordsMatch =
    form.newPassword.length > 0 &&
    form.confirmPassword.length > 0 &&
    form.newPassword === form.confirmPassword;

  const canSubmit =
    form.currentPassword.length > 0 &&
    isPasswordStrong(form.newPassword) &&
    passwordsMatch &&
    !isPending;

  const clearFields = () => {
    setForm(EMPTY_FORM);
  };

  const handleChange = (event) => {
    const { name, value } = event.target;
    setForm((prev) => ({ ...prev, [name]: value }));
  };

  const handleSubmit = (event) => {
    event.preventDefault();
    if (!canSubmit) {
      return;
    }
    setConfirmOpen(true);
  };

  const handleDialogOpenChange = (nextOpen) => {
    if (isPending) {
      return;
    }
    if (!nextOpen) {
      clearFields();
      setConfirmOpen(false);
    }
  };

  const handleConfirm = async () => {
    if (isPending) {
      return;
    }

    setIsPending(true);
    const toastId = toast.loading("Changing password...");

    try {
      const result = await changePassword({
        currentPassword: form.currentPassword,
        newPassword: form.newPassword,
      });

      if (result.success) {
        clearFields();
        setConfirmOpen(false);
        toast.success("Password changed, please log in again", { id: toastId });
        navigate("/login");
        return;
      }

      toast.error(result.message, { id: toastId });
    } finally {
      setIsPending(false);
    }
  };

  return (
    <section>
      <h2 className="text-lg font-semibold text-on-surface mb-4">
        Login &amp; security
      </h2>
      <p className="text-sm text-on-surface-muted mb-6">
        Change your password. You will need to sign in again after a successful
        change.
      </p>

      <form onSubmit={handleSubmit} className="max-w-md">
        <FieldGroup>
          <Field>
            <FieldLabel htmlFor="currentPassword">Current password</FieldLabel>
            <Input
              id="currentPassword"
              name="currentPassword"
              type="password"
              autoComplete="current-password"
              value={form.currentPassword}
              onChange={handleChange}
              disabled={isPending}
              required
            />
          </Field>

          <Field>
            <FieldLabel htmlFor="newPassword">New password</FieldLabel>
            <Input
              id="newPassword"
              name="newPassword"
              type="password"
              autoComplete="new-password"
              value={form.newPassword}
              onChange={handleChange}
              disabled={isPending}
              required
            />
            <ul className="mt-2 space-y-1" aria-live="polite">
              {PASSWORD_STRENGTH_RULES.map((rule) => {
                const passed = strengthChecks[rule.key];
                return (
                  <li
                    key={rule.key}
                    className={cn(
                      "flex items-center gap-2 text-xs",
                      passed ? "text-brand" : "text-on-surface-muted",
                    )}
                  >
                    {passed ? (
                      <Check className="size-3.5 shrink-0" aria-hidden />
                    ) : (
                      <Circle className="size-3.5 shrink-0" aria-hidden />
                    )}
                    <span>{rule.label}</span>
                  </li>
                );
              })}
            </ul>
          </Field>

          <Field>
            <FieldLabel htmlFor="confirmPassword">
              Confirm new password
            </FieldLabel>
            <Input
              id="confirmPassword"
              name="confirmPassword"
              type="password"
              autoComplete="new-password"
              value={form.confirmPassword}
              onChange={handleChange}
              disabled={isPending}
              required
            />
            {form.confirmPassword.length > 0 && !passwordsMatch && (
              <FieldDescription className="text-destructive">
                Passwords do not match.
              </FieldDescription>
            )}
          </Field>

          <Field>
            <Button type="submit" disabled={!canSubmit}>
              Change password
            </Button>
          </Field>
        </FieldGroup>
      </form>

      <ConfirmDialog
        open={confirmOpen}
        onOpenChange={handleDialogOpenChange}
        title="Change your password?"
        description="You will be signed out and must log in with the new password."
        confirmLabel="Change password"
        destructive
        isPending={isPending}
        onConfirm={handleConfirm}
      />
    </section>
  );
}
