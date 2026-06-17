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

export function LoginForm({ onSwitchToRegister }) {
  const loginUser = useAuthStore((state) => state.loginUser);
  const resolveLogin = useUIStore((state) => state.resolveLogin);
  const setOpenLogin = useUIStore((state) => state.setOpenLogin);

  const [formData, setFormData] = useState({
    username: "",
    password: "",
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
    const success = await loginUser(formData);
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
          <FieldLabel htmlFor="password">Password</FieldLabel>
          <Input
            id="password"
            name="password"
            type="password"
            required
            value={formData.password}
            onChange={handleChange}
          />
        </Field>
        <FieldGroup>
          <Field>
            <Button type="submit" className="w-full">
              Login
            </Button>
            <FieldDescription className="text-center">
              Don&apos;t have an account?{" "}
              <button
                type="button"
                onClick={onSwitchToRegister}
                className="underline underline-offset-4 hover:text-brand"
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
