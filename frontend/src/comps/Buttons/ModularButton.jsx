import { useEffect, useState } from "react";
import { useAuthStore } from "../../stores/authStore";
import { useMessagingStore } from "../../stores/messagingStore";

export default function ModularButton({ onClickFunction, text }) {
  const [bgColor, setBgColor] = useState("");
  const [hoverColor, setHoverColor] = useState("");

  useEffect(() => {
    switch (text) {
      case "Edit":
        setBgColor("bg-amber-600 hover:text-gray-800");
        break;
      case "In Cart":
        setBgColor("bg-green-600 hover:text-gray-800");
        break;

      default:
        setBgColor("bg-indigo-600 hover:text-gray-800");
        break;
    }
  }, [text]);

  return (
    <>
      <button
        onClick={() => onClickFunction()}
        className={`text-body mt-5 cursor-pointer rounded-xl bg-neutral-secondary-medium box-border border border-default-medium border-black hover:bg-neutral-tertiary-medium hover:text-heading focus:ring-4 focus:ring-neutral-tertiary shadow-xs font-medium leading-5  text-sm  py-2.5 focus:outline-none text-center px-5 whitespace-nowrap flex-1 min-w-[120px] ${bgColor} `}
        style={{
          fontSize: "clamp(0.65rem, 2.5vw, 0.875rem)",
        }}
      >
        {text}
      </button>
    </>
  );
}
