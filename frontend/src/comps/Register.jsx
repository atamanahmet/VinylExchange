import { Button, Checkbox, Label, TextInput } from "flowbite-react";
import { useState } from "react";
import { useAuthStore } from "../stores/authStore";

export function Register() {
  const { registerUser, user } = useAuthStore();

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
    }
    registerUser(formData);
  };
  return (
    <form className="flex max-w-md flex-col gap-6" onSubmit={handleSubmit}>
      <div>
        <div className="mb-2 block">
          <Label htmlFor="username1">Your username</Label>
        </div>
        <input
          id="username1"
          name="username"
          placeholder=""
          required
          className="ring-1 ring-indigo-800 rounded-md py-2 pl-2 w-70"
          onChange={handleChange}
          value={formData.username}
        />
      </div>
      <div>
        <div className="mb-2 block">
          <Label htmlFor="email2">Your email</Label>
        </div>
        <input
          id="email2"
          type="email"
          name="email"
          placeholder="name@mail.com"
          required
          className="ring-1 ring-indigo-800 rounded-md py-2 pl-2 w-70"
          onChange={handleChange}
          value={formData.email}
        />
      </div>
      <div>
        <div className="mb-2 block">
          <Label htmlFor="password2">Your password</Label>
        </div>
        <input
          id="password2"
          type="password"
          name="password"
          required
          className="ring-1 ring-indigo-800 rounded-md py-2 pl-2 w-70"
          onChange={handleChange}
        />
      </div>
      <div>
        <div className="mb-2 block">
          <Label htmlFor="repeatPassword">Repeat password</Label>
        </div>
        <input
          id="repeatPassword"
          type="password"
          name="repeatPassword"
          required
          className="ring-1 ring-indigo-800 rounded-md py-2 pl-2 w-70"
          onChange={handleChange}
        />
      </div>
      <div className="flex items-center gap-2">
        <Checkbox id="agree" />
        <Label htmlFor="agree" className="flex">
          I agree with the&nbsp;{" "}
          <a
            href="/terms"
            className="text-cyan-600 hover:underline dark:text-cyan-500"
            required
          >
            Terms and Contitions
          </a>
        </Label>
      </div>
      <Button
        type="submit"
        className="ring-1 ring-indigo-950 rounded-md py-2 pl-2 w-70 bg-slate-700"
      >
        Register new account
      </Button>
    </form>
  );
}
