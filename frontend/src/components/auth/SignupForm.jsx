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

export function SignupForm({ onSwitchToLogin }) {
  const registerUser = useAuthStore((state) => state.registerUser);
  const resolveLogin = useUIStore((state) => state.resolveLogin);
  const setOpenLogin = useUIStore((state) => state.setOpenLogin);

  const [formData, setFormData] = useState({
    username: "",
    email: "",
    password: "",
    repeatPassword: "",
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

    if (formData.password !== formData.repeatPassword) {
      toast.warning("Passwords do not match");
      return;
    }

    setIsSubmitting(true);
    const toastId = toast.loading("Creating account...");

    try {
      const result = await registerUser(formData);

      if (result.success) {
        toast.success("Account created successfully", { id: toastId });
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
          <FieldLabel htmlFor="email">Email</FieldLabel>
          <Input
            id="email"
            name="email"
            type="email"
            placeholder="m@example.com"
            required
            value={formData.email}
            onChange={handleChange}
            disabled={isSubmitting}
          />
          <FieldDescription>
            We&apos;ll use this to contact you. We will not share your email
            with anyone else.
          </FieldDescription>
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
          <FieldDescription>
            Must be at least 8 characters long.
          </FieldDescription>
        </Field>
        <Field>
          <FieldLabel htmlFor="repeatPassword">Confirm Password</FieldLabel>
          <Input
            id="repeatPassword"
            name="repeatPassword"
            type="password"
            required
            value={formData.repeatPassword}
            onChange={handleChange}
            disabled={isSubmitting}
          />
          <FieldDescription>Please confirm your password.</FieldDescription>
        </Field>
        <FieldGroup>
          <Field>
            <Button type="submit" className="w-full" disabled={isSubmitting}>
              Create Account
            </Button>
            <FieldDescription className="text-center">
              Already have an account?{" "}
              <button
                type="button"
                onClick={onSwitchToLogin}
                className="underline underline-offset-4 hover:text-brand"
                disabled={isSubmitting}
              >
                Sign in
              </button>
            </FieldDescription>
          </Field>
        </FieldGroup>
      </FieldGroup>
    </form>
  );
}
