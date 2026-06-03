"use client";

import {
  Button,
  Modal,
  ModalBody,
  ModalFooter,
  ModalHeader,
} from "flowbite-react";
import { useState } from "react";
import { Login } from "./Login";
import { Register } from "./Register";
import { useAuthStore } from "../stores/authStore";

export function AuthModal({ openModal, setOpenModal }) {
  const authResponse = useAuthStore((state) => state.authResponse);

  const [authType, setAuthType] = useState("Login");

  const changeAuthType = () => {
    authType == "Login" ? setAuthType("Register") : setAuthType("Login");
  };

  return (
    <>
      <Modal
        dismissible
        show={openModal}
        onClose={() => setOpenModal()}
        className="backdrop-blur-sm"
      >
        <div className="bg-gray-900 rounded-lg overflow-hidden">
          <ModalHeader className="bg-gray-900 py-5 px-6">
            {authResponse ? authResponse : authType}
          </ModalHeader>
          <ModalBody className="space-y-6 bg-gray-900">
            {authType == "Login" && <Login></Login>}
            {authType == "Register" && <Register></Register>}
            <a
              onClick={changeAuthType}
              className="ring-1 ring-indigo-800 rounded-md pt-2 pb-3 p-5 bg-slate-700 cursor-pointer"
            >
              {authType == "Login" ? "Register" : "Login"}
            </a>
          </ModalBody>
          <ModalFooter className=" bg-gray-900"></ModalFooter>
        </div>
      </Modal>
    </>
  );
}
