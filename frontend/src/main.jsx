// import React, { StrictMode } from "react";
import "./index.css";

import { BrowserRouter, Routes, Route } from "react-router";
import { createRoot } from "react-dom/client";

import { ThemeProvider } from "next-themes";
import App from "./App.jsx";

createRoot(document.getElementById("root")).render(
  <BrowserRouter>
    <ThemeProvider attribute="class" defaultTheme="system" enableSystem>
      <App />
    </ThemeProvider>
  </BrowserRouter>,
);
