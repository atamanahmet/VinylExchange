import { Button, Checkbox, Label, TextInput } from "flowbite-react";
import { useState } from "react";
import { useAuthStore } from "../stores/authStore";

export function Login() {
  const loginUser = useAuthStore((state) => state.loginUser);
  const registerUser = useAuthStore((state) => state.registerUser);

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
    loginUser(formData);
  };

  return (
    <form className="flex max-w-md flex-col gap-6 " onSubmit={handleSubmit}>
      <div>
        <div className="mb-2 block">
          <Label htmlFor="username1">Your username</Label>
        </div>
        <input
          id="username1"
          name="username"
          required
          className="ring-1 ring-indigo-800 rounded-md py-2 pl-2 w-70"
          onChange={handleChange}
        />
      </div>
      <div>
        <div className="mb-2 block">
          <Label htmlFor="password1">Your password</Label>
        </div>
        <input
          id="password1"
          type="password"
          name="password"
          required
          className="ring-1 ring-indigo-800 rounded-md py-2 pl-2 w-70"
          onChange={handleChange}
        />
      </div>
      <div className="flex items-center gap-2">
        <Checkbox id="remember" />
        <Label htmlFor="remember">Remember me</Label>
      </div>
      <Button
        type="submit"
        className="ring-1 ring-indigo-800 rounded-md py-2 pl-2 w-70 bg-slate-700"
      >
        Submit
      </Button>
    </form>
  );
}
