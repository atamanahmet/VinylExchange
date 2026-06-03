import { useEffect, useState } from "react";
import axios from "axios";
import { Link, useNavigate } from "react-router-dom";
import { useMessagingStore } from "../stores/messagingStore";
import { useCartStore } from "../stores/cartStore";
import { useAuthStore } from "../stores/authStore";
import CardImage from "./CardImage";
import ModularButton from "./Buttons/ModularButton";

export default function ListView({ item }) {
  const cart = useCartStore((state) => state.cart);
  const addToCart = useCartStore((state) => state.addtoCart);
  const decreaseFromCart = useCartStore((state) => state.decreaseFromCart);
  const removeFromCart = useCartStore((state) => state.removeFromCart);

  const user = useAuthStore((state) => state.user);
  const startConversation = useMessagingStore(
    (state) => state.startConversation,
  );

  const [image, setImage] = useState("");

  const [isOwnerUser, setIsOwnerUser] = useState();

  useEffect(() => {
    if (item && user && item.ownerUsername === user.username) {
      setIsOwnerUser(true);
    } else {
      setIsOwnerUser(false);
    }
  }, [item, user]);

  const navigate = useNavigate();

  const navigateItemWithId = () => {
    navigate(`/listing/${item.id}`);
  };
  const navigateEditItemWithId = () => {
    navigate(`/edit/${item.id}`);
  };
  const navigateMessagingWithItemId = () => {
    startConversation(item.id);
  };

  const inCart = cart
    ? cart.items.some((element) => element.listingId === item.id)
    : false;

  const imageSrc = item.imageUrl || item.externalCoverUrl;

  return (
    <>
      <div className="bg-neutral-primary-soft grid grid-cols-7 border-b   items-center">
        <div className="scale-65 -ml-6 -my-5">
          <Link to={`/listing/${item.id}`} className="cursor-pointer">
            <CardImage src={imageSrc} alt={item.title} />
          </Link>
        </div>
        <button
          onClick={() => {
            navigateItemWithId();
          }}
        >
          <p
            scope="row"
            className="px-6 py-4 font-medium text-heading flex flex-col overflow-auto "
          >
            {item.title}
          </p>
        </button>
        <button>
          <p
            scope="row"
            className="px-6 py-4 text-amber-400 font-medium text-heading flex flex-col overflow-auto "
          >
            {item.artist}
          </p>
        </button>

        <p className="px-6 py-4">{item.year || item.date} </p>
        <p className="">{item.format} </p>
        <div className="">
          {item.price && (
            <p
              className={`px-6 ${
                item.discount > 0
                  ? "text-base font-bold text-gray-900 dark:text-white line-through"
                  : "text-base font-bold text-gray-900 dark:text-white"
              }`}
            >
              {item.price + " ₺"}
            </p>
          )}
          <p
            className={`px-6 ${
              item.discount > 0
                ? "text-base font-bold text-green-900 dark:text-green-400"
                : "text-base font-bold text-green-900 dark:text-green-400"
            }`}
          >
            {item.discount > 0 ? item.discountedPrice + " ₺" : null}
          </p>
        </div>
        {/* <p className="px-6 py-4">{item.price}</p> */}

        <div className="px-6 py-4 text-right">
          {!isOwnerUser && (
            <div className="flex flex-col justify-center items-center -mt-5">
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
          )}
          {isOwnerUser && (
            <div className="flex flex-col justify-center items-center -mt-5">
              <a
                onClick={() => navigateEditItemWithId()}
                className={`mt-5 rounded-xl  border   focus:ring-4  shadow-xs font-medium leading-5  text-sm  py-2.5 focus:outline-none text-center px-4.5 cursor-pointer min-w-30  hover:text-red-700
             `}
              >
                Edit
              </a>
            </div>
          )}
        </div>
      </div>
    </>
  );
}
