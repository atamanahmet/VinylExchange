import { useState } from "react";
import { Button } from "@/components/ui/button";
import {
  Field,
  FieldDescription,
  FieldGroup,
  FieldLabel,
} from "@/components/ui/field";
import { Input } from "@/components/ui/input";
import { useAuthStore } from "../stores/authStore";
import { useUIStore } from "../stores/uiStore";

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

  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData((prevData) => ({
      ...prevData,
      [name]: value,
    }));
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (formData.password !== formData.repeatPassword) {
      alert("Passwords do not match!");
      return;
    }
    const success = await registerUser(formData);
    if (success) {
      resolveLogin(true);
      setOpenLogin(false);
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
          />
          <FieldDescription>Please confirm your password.</FieldDescription>
        </Field>
        <FieldGroup>
          <Field>
            <Button type="submit" className="w-full">
              Create Account
            </Button>
            <FieldDescription className="text-center">
              Already have an account?{" "}
              <button
                type="button"
                onClick={onSwitchToLogin}
                className="underline underline-offset-4 hover:text-brand"
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
