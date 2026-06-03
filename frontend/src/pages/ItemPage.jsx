import { useNavigate, useLocation, useParams } from "react-router-dom";
import { useState, useEffect } from "react";
import axios from "axios";
import ImageGallery from "../comps/ImageGallery";

import {
  Button,
  Modal,
  ModalBody,
  ModalFooter,
  ModalHeader,
} from "flowbite-react";
import { useAuthStore } from "../stores/authStore";
import { useMessagingStore } from "../stores/messagingStore";
import { useCartStore } from "../stores/cartStore";
import { useDataStore } from "../stores/dataStore";

export default function ItemPage() {
  const user = useAuthStore((state) => state.user);

  const addToCart = useCartStore((state) => state.addToCart);
  const removeFromCart = useCartStore((state) => state.removeFromCart);
  const data = useDataStore((state) => state.data);
  const fetchListing = useDataStore((state) => state.fetchListing);
  const fetchListingsByUser = useDataStore(
    (state) => state.fetchListingsByUser,
  );

  const startConversation = useMessagingStore(
    (state) => state.startConversation,
  );

  const [loading, setLoading] = useState(true);

  const { listingId } = useParams();
  const [listing, setListing] = useState();
  const [mainImage, setMainImage] = useState();
  const [openModal, setOpenModal] = useState(false);
  const [openModalUrl, setOpenModalUrl] = useState();
  const [isOwnerUser, setIsOwnerUser] = useState();

  function openModalImage(url) {
    console.log(url);
    setOpenModal((curr) => !curr);
    setOpenModalUrl(url);
  }

  let label;
  let format;

  const navigate = useNavigate();

  const navigateItemWithId = () => {
    navigate(`/listing/${listing.id}`);
  };
  const navigateEditItemWithId = () => {
    navigate(`/edit/${listing.id}`);
  };
  const navigateMessagingWithItemId = () => {
    startConversation(listing.id);
  };

  function addCart() {
    console.log("added to cart");
    addToCart(listing.id);
  }

  // async function getListing() {
  //   try {
  //     const res = await axios.get(
  //       `http://localhost:8080/api/listings/${listingId}`,
  //       {
  //         withCredentials: true,
  //       },
  //     );
  //     if (res.status == 200) {
  //       console.log(res.data);
  //       setListing(res.data);
  //     }
  //   } catch (error) {
  //     console.log(error);
  //   } finally {
  //     setLoading(false);
  //   }
  // }

  useEffect(() => {
    if (listingId == null) return;
    fetchListing(listingId);
  }, [listingId]);

  useEffect(() => {
    if (data.items && user && data.items[0].ownerUsername === user.username) {
      setIsOwnerUser(true);
    } else {
      setIsOwnerUser(false);
    }
  }, [data.items, user]);

  // const handleUserListingSelection = () => {};

  return (
    <>
      {data.items[0] && (
        <div className="w-full px-4 sm:px-6 lg:px-8 py-4 ">
          <div>
            <Modal
              dismissible
              show={openModal}
              onClose={() => setOpenModal(false)}
              className="backdrop-blur-sm"
            >
              <div className="bg-gray-900 rounded-lg overflow-hidden">
                <ModalHeader className="bg-gray-900 py-5 px-6"></ModalHeader>
                <ModalBody className="space-y-6 bg-gray-900">
                  <img src={openModalUrl} alt="" className="h-full w-full" />
                </ModalBody>
                <ModalFooter className=" bg-gray-900"></ModalFooter>
              </div>
            </Modal>
          </div>
          <div className="flex justify-center gap-20 text-left mt-20">
            <ImageGallery
              imagePaths={data.items[0].imagePaths}
              openModal={openModalImage}
            />
            <div>
              <h2 className="company uppercase text-amber-600 font-bold text-sm sm:text-md tracking-wider pb-3 sm:pb-5">
                {data.items[0].labelName}
              </h2>
              <h3
                // ref={data.items[0].title}
                className="product capitalize text-very-dark-blue font-bold text-3xl sm:text-4xl sm:leading-none pb-3"
              >
                {data.items[0].title}{" "}
                <span className="block  mt-5 text-2xl text-amber-600">
                  {/* {size} */}
                  {data.items[0].artistName}
                </span>
                <span className="block  mt-5 text-2xl">
                  {/* {size} */}
                  {label}
                </span>
                <span className="block  mt-5 text-2xl">
                  {data.items[0].format}
                </span>
                <span className="block  mt-5 text-2xl">
                  <span className="italic font-normal text-amber-200">
                    Condition:{" "}
                  </span>
                  {data.items[0].condition}
                </span>
                <span className="block  mt-5 text-2xl">
                  {data.items[0].date}
                </span>
              </h3>

              <p className="mt-5">
                Seller:{" "}
                <button onClick={() => handleUserListingSelection()}>
                  {data.items[0].ownerUsername}
                </button>
              </p>

              <p className="mt-5">Stock: {data.items[0].stockQuantity}</p>

              <p className="text-dark-grayish-blue pb-6 lg:py-7 lg:leading-6">
                {data.items[0].description}
              </p>
              <div className="amount font-bold flex items-center justify-between lg:flex-col lg:items-start mb-6">
                <div className="discount-price items-center flex">
                  <div className="price -mt-5 text-3xl ">
                    <span className="text-green-400">
                      {data.items[0].discount == 0
                        ? data.items[0].price.toLocaleString("tr-TR") + " ₺"
                        : data.items[0].discountedPrice.toLocaleString(
                            "tr-TR",
                          ) + " ₺"}
                    </span>
                  </div>
                  <div className="discount text-green bg-pale-orange w-max px-2 rounded mx-5 h-6">
                    {data.items[0].discount != 0
                      ? data.items[0].discount + "%"
                      : null}
                  </div>
                </div>
                <div className="original-price text-grayish-blue line-through lg:mt-2">
                  {data.items[0].discount != 0
                    ? data.items[0].price.toLocaleString("tr-TR") + "₺"
                    : null}
                </div>
              </div>
              <div className="sm:flex lg:mt-8 w-full">
                {!isOwnerUser && (
                  <div className="quantity-container w-full bg-light-grayish-blue rounded-lg h-14 mb-4 flex items-center justify-between px-6 lg:px-3 font-bold sm:mr-3 lg:mr-5 lg:w-1/3">
                    <button
                      // onClick={data.items[0].format}
                      className="text-orange text-2xl leading-none font-bold mb-1 lg:mb-2 lg:text-3xl hover:opacity-60"
                    >
                      -
                    </button>
                    <input
                      min={0}
                      max={100}
                      // onChange={quantity}
                      className="quantity focus:outline-none text-dark-blue bg-light-grayish-blue font-bold flex text-center w-full"
                      type="number"
                      name="quantity"
                      // value={quantityCount}
                      aria-label="quantity number"
                    />
                    <button
                      // onClick={quantity}
                      className="text-orange text-2xl leading-none font-bold mb-1 lg:mb-2 lg:text-3xl hover:opacity-60"
                    >
                      +
                    </button>
                  </div>
                )}
                {!isOwnerUser && (
                  <div className="inline-flex justify-center w-full gap-3 items-center -mt-5">
                    <button
                      onClick={() => {
                        navigateMessagingWithItemId();
                      }}
                      className=" w-full h-14 bg-orange rounded-lg lg:rounded-xl mb-2 hover:shadow-lg hover:shadow-indigo-600 text-white flex bg-indigo-600 hover:bg-indigo-800 hover:text-slate-200 items-center justify-center lg:w-3/5 "
                      // className="rounded-lg py-2 px-3 text-amber-50  "
                    >
                      <i className="cursor-pointer text-white text-xl text-center ">
                        <ion-icon name="cart-outline"></ion-icon>
                      </i>
                      Trade
                    </button>
                    <button
                      onClick={addCart}
                      className=" w-full h-14 bg-orange rounded-lg lg:rounded-xl mb-2 hover:shadow-lg hover:shadow-indigo-600 text-white flex bg-indigo-600 hover:bg-indigo-800 hover:text-slate-200 items-center justify-center lg:w-3/5 "
                      // className="rounded-lg py-2 px-3 text-amber-50  "
                    >
                      <i className="cursor-pointer text-white text-xl text-center ">
                        <ion-icon name="cart-outline"></ion-icon>
                      </i>
                      Add to cart
                    </button>
                  </div>
                )}
                {isOwnerUser && (
                  <div className="inline-flex  w-full gap-3 ">
                    <button
                      onClick={() => {
                        navigateEditItemWithId();
                      }}
                      className=" w-full h-14 bg-orange rounded-lg lg:rounded-xl mb-2 hover:shadow-lg hover:shadow-indigo-600 text-white flex bg-indigo-600 hover:bg-indigo-800 hover:text-slate-200 items-center justify-center lg:w-3/5 "
                      // className="rounded-lg py-2 px-3 text-amber-50  "
                    >
                      <i className="cursor-pointer text-white text-xl text-center ">
                        <ion-icon name="cart-outline"></ion-icon>
                      </i>
                      Edit
                    </button>
                  </div>
                )}
              </div>
            </div>
          </div>
        </div>
      )}
    </>
  );
}
