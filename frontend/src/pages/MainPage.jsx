import { useState, useEffect, useMemo } from "react";
import "../App.css";
import { ThemeProvider } from "@material-tailwind/react";

import Card from "../comps/Card";
import ListView from "../comps/ListView";

import SkeletonCardView from "../comps/Skeletons/SkeletonCardView";
import SkeletonListView from "../comps/Skeletons/SkeletonListView";

import { useDataStore } from "../stores/dataStore";
import { useAuthStore } from "../stores/authStore";
import { useUIStore } from "../stores/uiStore";
import { useCartStore } from "../stores/cartStore";

import { mbReleaseToCardItem } from "../adapters/mbReleaseToCardItem";
import { useNavigate } from "react-router-dom";
import { useMessagingStore } from "../stores/messagingStore";
import useWishlistStore from "../stores/wishlistStore";
import { useSearchStore } from "../stores/searchStore";
import { mapListingsToCardItems } from "../adapters/mapListingToCardItems";

export default function MainPage() {
  const PAGE_SIZE = 20;

  const [page, setPage] = useState(1);

  const [visibleItems, setVisibleItems] = useState([]);

  const navigate = useNavigate();

  const searchResult = useSearchStore((state) => state.searchResult);
  const clearSearch = useSearchStore((state) => state.clearSearch);

  const user = useAuthStore((state) => state.user);

  //wishlist store
  const addToWishlist = useWishlistStore((state) => state.addToWishlist);
  const isInWishlist = useWishlistStore((state) => state.isInWishlist);
  const toggleToWishlist = useWishlistStore((state) => state.toggleToWishlist);
  const removeFromWishlist = useWishlistStore(
    (state) => state.removeFromWishlist,
  );

  //data store
  const isFetching = useDataStore((state) => state.isFetching);
  const fetchAllListings = useDataStore((state) => state.fetchAllListings);
  const data = useDataStore((state) => state.data);

  //UI store
  const layout = useUIStore((state) => state.layout);
  const setLayout = useUIStore((state) => state.setLayout);

  //cart store
  const cart = useCartStore((state) => state.cart);
  const addToCart = useCartStore((state) => state.addToCart);
  const removeFromCart = useCartStore((state) => state.removeFromCart);

  const startConversation = useMessagingStore(
    (state) => state.startConversation,
  );

  useEffect(() => {
    clearSearch();
    fetchAllListings();
  }, []);

  const currentData = useMemo(() => {
    return searchResult.items?.length > 0 ? searchResult : data;
  }, [searchResult, data]);

  const items = useMemo(() => {
    if (!currentData?.items) return [];

    if (currentData.dataType === "listing") {
      const cartIds = new Set(cart?.items?.map((i) => i.listingId) || []);

      return mapListingsToCardItems(currentData.items, {
        user,
        cartIds,
        addToCart,
        navigate,
        startConversation,
      });
    }
    //TODO:remove, devonly
    if (currentData.dataType === "mb") {
      return currentData.items.map((release) =>
        mbReleaseToCardItem(release, isInWishlist, toggleToWishlist),
      );
    }

    return [];
  }, [currentData, cart, user]);

  //pagination for infinite scroll
  useEffect(() => {
    if (!currentData?.items) return;
    setPage(1);
    setVisibleItems(items.slice(0, PAGE_SIZE));
  }, [items]);

  useEffect(() => {
    const handleScroll = () => {
      const scrollTop = window.scrollY;
      const windowHeight = window.innerHeight;
      const fullHeight = document.documentElement.scrollHeight;

      if (scrollTop + windowHeight >= fullHeight * 0.75) {
        loadNextPage();
      }
    };

    window.addEventListener("scroll", handleScroll);
    return () => window.removeEventListener("scroll", handleScroll);
  }, [visibleItems.length, items, page]);

  const loadNextPage = () => {
    const nextPage = page + 1;
    const nextItems = items.slice(0, nextPage * PAGE_SIZE);

    if (nextItems.length > visibleItems.length) {
      setVisibleItems(nextItems);
      setPage(nextPage);
    }
  };

  return (
    <>
      <div className=" flex flex-row gap-2 justify-end mt-5 max-w-7xl mx-auto px-10 ">
        <button className="" onClick={() => setLayout("list")}>
          <svg
            className="border border-indigo-500 w-8 h-8 p-1 rounded-md bg-black"
            viewBox="0 0 24 24"
            fill="#FFFFFF"
            xmlns="http://www.w3.org/2000/svg"
          >
            <path
              d="M8 6.00067L21 6.00139M8 12.0007L21 12.0015M8 18.0007L21 18.0015M3.5 6H3.51M3.5 12H3.51M3.5 18H3.51M4 6C4 6.27614 3.77614 6.5 3.5 6.5C3.22386 6.5 3 6.27614 3 6C3 5.72386 3.22386 5.5 3.5 5.5C3.77614 5.5 4 5.72386 4 6ZM4 12C4 12.2761 3.77614 12.5 3.5 12.5C3.22386 12.5 3 12.2761 3 12C3 11.7239 3.22386 11.5 3.5 11.5C3.77614 11.5 4 11.7239 4 12ZM4 18C4 18.2761 3.77614 18.5 3.5 18.5C3.22386 18.5 3 18.2761 3 18C3 17.7239 3.22386 17.5 3.5 17.5C3.77614 17.5 4 17.7239 4 18Z"
              stroke="oklch(0.585 0.233 277.117)"
              fill="#FFFFFF"
              strokeWidth="3"
              strokeLinecap="round"
              strokeLinejoin="round"
            />
          </svg>
        </button>
        <button className="" onClick={() => setLayout("grid")}>
          <svg
            className="border border-indigo-500 w-8 h-8 p-1 rounded-md bg-black"
            viewBox="0 -0.5 21 21"
            version="1.1"
            xmlns="http://www.w3.org/2000/svg"
            xmlnsXlink="http://www.w3.org/1999/xlink"
          >
            <g id="Page-1" stroke="#FFFFFF" strokeWidth="0" fill="#FFFFFF">
              <g
                id="Dribbble-Light-Preview"
                transform="translate(-219.000000, -200.000000)"
                fill="oklch(0.585 0.233 277.117)"
              >
                <g id="icons" transform="translate(56.000000, 160.000000)">
                  <path
                    d="M181.9,54 L179.8,54 C178.63975,54 177.7,54.895 177.7,56 L177.7,58 C177.7,59.105 178.63975,60 179.8,60 L181.9,60 C183.06025,60 184,59.105 184,58 L184,56 C184,54.895 183.06025,54 181.9,54 M174.55,54 L172.45,54 C171.28975,54 170.35,54.895 170.35,56 L170.35,58 C170.35,59.105 171.28975,60 172.45,60 L174.55,60 C175.71025,60 176.65,59.105 176.65,58 L176.65,56 C176.65,54.895 175.71025,54 174.55,54 M167.2,54 L165.1,54 C163.93975,54 163,54.895 163,56 L163,58 C163,59.105 163.93975,60 165.1,60 L167.2,60 C168.36025,60 169.3,59.105 169.3,58 L169.3,56 C169.3,54.895 168.36025,54 167.2,54 M181.9,47 L179.8,47 C178.63975,47 177.7,47.895 177.7,49 L177.7,51 C177.7,52.105 178.63975,53 179.8,53 L181.9,53 C183.06025,53 184,52.105 184,51 L184,49 C184,47.895 183.06025,47 181.9,47 M174.55,47 L172.45,47 C171.28975,47 170.35,47.895 170.35,49 L170.35,51 C170.35,52.105 171.28975,53 172.45,53 L174.55,53 C175.71025,53 176.65,52.105 176.65,51 L176.65,49 C176.65,47.895 175.71025,47 174.55,47 M167.2,47 L165.1,47 C163.93975,47 163,47.895 163,49 L163,51 C163,52.105 163.93975,53 165.1,53 L167.2,53 C168.36025,53 169.3,52.105 169.3,51 L169.3,49 C169.3,47.895 168.36025,47 167.2,47 M181.9,40 L179.8,40 C178.63975,40 177.7,40.895 177.7,42 L177.7,44 C177.7,45.105 178.63975,46 179.8,46 L181.9,46 C183.06025,46 184,45.105 184,44 L184,42 C184,40.895 183.06025,40 181.9,40 M174.55,40 L172.45,40 C171.28975,40 170.35,40.895 170.35,42 L170.35,44 C170.35,45.105 171.28975,46 172.45,46 L174.55,46 C175.71025,46 176.65,45.105 176.65,44 L176.65,42 C176.65,40.895 175.71025,40 174.55,40 M169.3,42 L169.3,44 C169.3,45.105 168.36025,46 167.2,46 L165.1,46 C163.93975,46 163,45.105 163,44 L163,42 C163,40.895 163.93975,40 165.1,40 L167.2,40 C168.36025,40 169.3,40.895 169.3,42"
                    id="grid-[#1526]"
                  ></path>
                </g>
              </g>
            </g>
          </svg>
        </button>
      </div>
      {layout == "list" && (
        <div>
          <div className="bg-neutral-primary-soft border-b border-default grid grid-cols-7 items-center text-white justify-center max-w-7xl mx-auto">
            <p>Cover</p>
            <p>Title</p>
            <p>Band/Artist</p>
            <p>Release Date</p>
            <p>Format</p>
            <p>Price</p>
          </div>
          <div className="max-w-7xl mx-auto">
            {isFetching || !visibleItems || visibleItems.length === 0
              ? Array(5)
                  .fill(0)
                  .map((_, i) => <SkeletonListView key={i} />)
              : visibleItems?.map((item) => (
                  <ListView key={item.id} item={item} />
                ))}
          </div>
        </div>
      )}
      {layout == "grid" && (
        <div className="grid min-[850px]:grid-cols-3 min-[1100px]:grid-cols-4 min-[670px]:grid-cols-2 mt-5 gap-5 sm:px-6 lg:px-8 max-w-7xl mx-auto">
          {isFetching || !visibleItems || visibleItems.length === 0
            ? Array(8)
                .fill(0)
                .map((_, i) => <SkeletonCardView key={i} />)
            : visibleItems?.map((item) => <Card key={item.id} item={item} />)}
        </div>
      )}
    </>
  );
}
