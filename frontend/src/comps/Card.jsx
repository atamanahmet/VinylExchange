import { Link, useNavigate } from "react-router-dom";
import { useEffect, useState } from "react";
import TradeButton from "./Buttons/ModularButton";
import { useCartStore } from "../stores/cartStore";
import { useAuthStore } from "../stores/authStore";
import { useMessagingStore } from "../stores/messagingStore";

import ModularButton from "./Buttons/ModularButton";
import CardImage from "./CardImage";
import CardShell from "./CardShell";

export default function Card({ item, onSelect }) {
  const imageSrc = item.imageUrl || item.externalCoverUrl;

  return (
    <div
      className="hover:-translate-y-1 duration-200 ease-in-out border-2 p-4 rounded-2xl "
      onClick={onSelect ? () => onSelect(item) : null}
      style={{ cursor: onSelect ? "pointer" : "default" }}
    >
      {!onSelect && (
        <Link to={`/listing/${item.id}`} className="cursor-pointer">
          <CardImage src={imageSrc} alt={item.title} />
        </Link>
      )}
      {onSelect && <CardImage src={imageSrc} alt={item.title} />}

      <a href="#">
        <h5 className="mt-3 mb-2 text-2xl font-semibold tracking-tight text-heading ">
          {item.title}
        </h5>
      </a>
      <p className="mb-3 text-body text-amber-400">{item.artist}</p>
      <p className="mb-3 text-body ">{item.format || "Unknown Format"}</p>
      <p className="text-body mb-2 text-amber-400">
        {item.year || "Unknown Year"}
      </p>
      <p className="text-body mb-2">{item.country || "Unknown Country"}</p>
      <p className="text-body mb-2">{item.barcode || "Unknown Barcode"}</p>
      <p className="text-body">{item.label || "Unknown Label"}</p>

      {item.price != null && (
        <p className="text-body text-xl mt-5 text-green-400">
          {(item.discountedPrice ?? item.price).toLocaleString("tr-TR")} ₺
        </p>
      )}

      <div className="flex flex-row gap-3 justify-center">
        {item.primaryAction && (
          <ModularButton
            listingId={item.id}
            onClickFunction={item.primaryAction.onClick}
            text={item.primaryAction.label}
          />
        )}

        {item.secondaryAction && (
          <ModularButton
            listingId={item.id}
            onClickFunction={item.secondaryAction.onClick}
            text={item.secondaryAction.label}
          />
        )}
      </div>
    </div>
  );
}
