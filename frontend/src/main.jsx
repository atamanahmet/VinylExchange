// import React, { StrictMode } from "react";
import "./index.css";

import { BrowserRouter, Routes, Route } from "react-router";
import { createRoot } from "react-dom/client";

import App from "./App.jsx";

import { ThemeProvider } from "@material-tailwind/react";

createRoot(document.getElementById("root")).render(
  <BrowserRouter>
    <App />
  </BrowserRouter>,
);
