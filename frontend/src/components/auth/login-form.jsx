import { useState } from "react";
import { toast } from "sonner";

import { Button } from "@/components/ui/button";
import {
  Field,
  FieldDescription,
  FieldGroup,
  FieldLabel,
} from "@/components/ui/field";
import { Input } from "@/components/ui/input";
import { useAuthStore } from "@/stores/authStore";
import { useUIStore } from "@/stores/uiStore";

export function LoginForm({ onSwitchToRegister }) {
  const loginUser = useAuthStore((state) => state.loginUser);
  const resolveLogin = useUIStore((state) => state.resolveLogin);
  const setOpenLogin = useUIStore((state) => state.setOpenLogin);

  const [formData, setFormData] = useState({
    username: "",
    password: "",
  });
  const [isSubmitting, setIsSubmitting] = useState(false);

  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData((prevData) => ({
      ...prevData,
      [name]: value,
    }));
  };

  const handleSubmit = async (e) => {
    e.preventDefault();

    if (isSubmitting) {
      return;
    }

    setIsSubmitting(true);
    const toastId = toast.loading("Signing in...");

    try {
      const result = await loginUser(formData);

      if (result.success) {
        toast.success("Logged in successfully", { id: toastId });
        const waitingForLogin = Boolean(useUIStore.getState().loginResolver);
        resolveLogin(true);
        setOpenLogin(false);
        if (!waitingForLogin) {
          window.location.reload();
        }
        return;
      }

      toast.error(result.message, { id: toastId });
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <form onSubmit={handleSubmit}>
      <FieldGroup>
        <Field>
          <FieldLabel htmlFor="username">Username</FieldLabel>
          <Input
            id="username"
            name="username"
            type="text"
            placeholder="johndoe"
            required
            value={formData.username}
            onChange={handleChange}
            disabled={isSubmitting}
          />
        </Field>
        <Field>
          <FieldLabel htmlFor="password">Password</FieldLabel>
          <Input
            id="password"
            name="password"
            type="password"
            required
            value={formData.password}
            onChange={handleChange}
            disabled={isSubmitting}
          />
        </Field>
        <FieldGroup>
          <Field>
            <Button type="submit" className="w-full" disabled={isSubmitting}>
              Login
            </Button>
            <FieldDescription className="text-center">
              Don&apos;t have an account?{" "}
              <button
                type="button"
                onClick={onSwitchToRegister}
                className="underline underline-offset-4 hover:text-brand"
                disabled={isSubmitting}
              >
                Sign up
              </button>
            </FieldDescription>
          </Field>
        </FieldGroup>
      </FieldGroup>
    </form>
  );
}
