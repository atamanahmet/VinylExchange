import { useEffect, useState } from "react";
import { useNavigate, useLocation } from "react-router";
import { AuthModal } from "./AuthModal";
import { useCartStore } from "../stores/cartStore";
import { useAuthStore } from "../stores/authStore";
import { useUIStore } from "../stores/uiStore";
import { useMessagingStore } from "../stores/messagingStore";
import { useNotificationStore } from "../stores/notificationStore";
import Notification from "./Notification";
import { useSearchStore } from "../stores/searchStore";
import { useListingStore } from "../stores/listingStore";

const SkeletonNavbar = () => (
  <nav className="border-surface-3 border-b bg-surface-base">
    <div className="max-w-7xl mx-auto px-3 py-3">
      {/* logo */}
      <div className="flex items-center justify-between">
        <div className="h-10 w-32 bg-surface-3 rounded animate-pulse"></div>

        <div className="hidden md:block flex-1 max-w-md mx-8">
          <div className="h-10 bg-surface-3 rounded animate-pulse"></div>
        </div>

        <div className="flex items-center gap-2">
          {/* mobile search button */}
          <div className="h-10 w-10 bg-surface-3 rounded md:hidden animate-pulse"></div>

          {/* sign in bvutton */}
          <div className="h-10 w-20 bg-surface-3 rounded animate-pulse"></div>

          {/* cart icon */}
          <div className="h-10 w-10 bg-surface-3 rounded animate-pulse"></div>

          {/* message icon */}
          <div className="h-10 w-10 bg-surface-3 rounded animate-pulse"></div>

          {/* avatar */}
          <div className="h-10 w-10 bg-surface-3 rounded-full animate-pulse"></div>

          {/* mobile menu*/}
          <div className="h-10 w-10 bg-surface-3 rounded lg:hidden animate-pulse"></div>
        </div>
      </div>
    </div>
  </nav>
);

export default function Navbar() {
  const location = useLocation();

  const navigate = useNavigate();

  const cartItemCount = useCartStore((state) => state.cartItemCount);

  const user = useAuthStore((state) => state.user);
  const logOut = useAuthStore((state) => state.logOut);
  const isLoading = useAuthStore((state) => state.isLoading);

  const clearSearch = useSearchStore((state) => state.clearSearch);
  const searchProducts = useSearchStore((state) => state.searchProducts);

  const fetchPublicListings = useListingStore(
    (state) => state.fetchPublicListings,
  );

  const openLogin = useUIStore((state) => state.openLogin);
  const navbarActive = useUIStore((state) => state.navbarActive);
  const setOpenLogin = useUIStore((state) => state.setOpenLogin);

  const fetchMessaggeUnreadCount = useMessagingStore(
    (state) => state.fetchUnreadCount,
  );
  const messageUnreadCount = useMessagingStore((state) => state.unreadCount);

  const fetchDropdownNotifications = useNotificationStore(
    (state) => state.fetchDropdownNotifications,
  );
  const notifications = useNotificationStore((state) => state.notifications);
  const notificationUnreadCount = useNotificationStore(
    (state) => state.unreadCount,
  );
  const markAsRead = useNotificationStore((state) => state.markAsRead);

  const [query, setQuery] = useState("");
  const [isMobileMenuOpen, setIsMobileMenuOpen] = useState(false);

  const [isUserMenuOpen, setIsUserMenuOpen] = useState(false);

  const [isNotificationMenuOpen, setIsNotificationMenuOpen] = useState(false);
  const [isMobileSearchOpen, setIsMobileSearchOpen] = useState(false);

  const handleSearch = (e) => {
    if (e) e.preventDefault();
    searchProducts(query);
    navigate("/");
  };

  const handleKeyPress = (e) => {
    if (e.key === "Enter") {
      handleSearch(e);
    }
  };

  useEffect(() => {
    if (user && !isLoading) {
      fetchMessaggeUnreadCount();
      fetchDropdownNotifications();
    }
  }, [user]);

  const handleLogOut = () => logOut();

  const setModalActive = () => {
    setOpenLogin(!openLogin);
  };

  const handleLogoClick = () => {
    clearSearch();
    if (location.pathname === "/") {
      fetchPublicListings();
    } else {
      navigate("/");
    }
  };

  if (isLoading) {
    return <SkeletonNavbar />;
  }

  if (!navbarActive) {
    return null;
  }

  return (
    <nav className="full-width-break border-surface-3 border-b sticky top-0 z-50 bg-surface-base">
      <div className=" max-w-7xl mx-auto  px-4 sm:px-6 lg:px-8 ">
        <div className="flex items-center justify-between h-16">
          {/* logo */}
          <button
            onClick={() => handleLogoClick()}
            className="flex items-center space-x-3 shrink-0"
          >
            <svg
              className="h-10 w-8 text-accent-dim"
              fill="currentColor"
              viewBox="0 0 20 20"
            >
              <path d="M10 2a8 8 0 100 16 8 8 0 000-16zM8 10a2 2 0 114 0 2 2 0 01-4 0z" />
            </svg>
            <span className="self-center text-xl font-semibold whitespace-nowrap text-on-surface hidden sm:block">
              VexChange
            </span>
          </button>

          {/* lg search */}
          <div className="hidden md:flex flex-1 max-w-md mx-4 lg:mx-8 ">
            <div className="relative w-full">
              <div className="absolute inset-y-0 left-0 flex items-center pl-3 pointer-events-none">
                <svg
                  className="w-4 h-4 text-on-surface-dim"
                  fill="none"
                  stroke="currentColor"
                  viewBox="0 0 24 24"
                >
                  <path
                    strokeLinecap="round"
                    strokeLinejoin="round"
                    strokeWidth={2}
                    d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z"
                  />
                </svg>
              </div>
              <input
                type="search"
                className="block w-full p-2.5 pl-10 text-sm text-on-surface border-2 rounded-base bg-surface-1 focus:ring-accent focus:border-accent placeholder:text-on-surface-muted rounded-xl"
                placeholder="Search marketplace..."
                value={query}
                onChange={(e) => setQuery(e.target.value)}
                onKeyDown={handleKeyPress}
              />
            </div>
          </div>

          <div className="flex items-center gap-2">
            {/* mobile search */}
            <button
              onClick={() => setIsMobileSearchOpen(!isMobileSearchOpen)}
              className="flex items-center justify-center md:hidden text-on-surface-dim hover:text-on-surface bg-transparent border border-transparent hover:bg-surface-2 focus:ring-2 focus:ring-surface-3 font-medium rounded-base text-sm w-10 h-10 focus:outline-none"
              aria-label="Search"
            >
              <svg
                className="w-5 h-5"
                fill="none"
                stroke="currentColor"
                viewBox="0 0 24 24"
              >
                <path
                  strokeLinecap="round"
                  strokeLinejoin="round"
                  strokeWidth={2}
                  d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z"
                />
              </svg>
            </button>

            {/* sign in/listing */}
            {user == null ? (
              <div>
                <button
                  onClick={setModalActive}
                  className="hidden sm:flex items-center text-on-surface hover:text-on-surface hover:bg-accent-hover focus:ring-2 focus:ring-surface-3 font-medium rounded-base text-sm px-4 py-2.5 focus:outline-none bg-accent rounded-xl"
                >
                  Sign in
                </button>
              </div>
            ) : (
              <div className="flex flex-row gap-4">
                <button
                  onClick={() => navigate("/newlisting")}
                  className="hidden sm:flex items-center text-on-surface bg-accent rounded-xl hover:bg-accent-hover focus:ring-2 focus:ring-accent font-medium rounded-base text-sm px-4 py-2.5 focus:outline-none"
                >
                  New Listing
                </button>
                <button
                  onClick={() => navigate("/orders")}
                  className="hidden sm:flex items-center text-on-surface bg-accent rounded-xl hover:bg-accent-hover focus:ring-2 focus:ring-accent font-medium rounded-base text-sm px-4 py-2.5 focus:outline-none"
                >
                  Orders
                </button>
              </div>
            )}

            <div className="flex gap-2">
              {/* message */}
              {user != null && (
                <button
                  onClick={() => navigate("/messaging")}
                  className="relative flex items-center justify-center text-on-surface-dim hover:text-on-surface bg-transparent border border-transparent hover:bg-surface-2 focus:ring-2 focus:ring-surface-3 font-medium rounded-base text-sm w-10 h-10 ml-2 hover:-translate-y-0.5 ease-in-out"
                  aria-label="Messages"
                >
                  <div className="border-2 border-gray-800 p-2 rounded-full">
                    <svg
                      className="w-5 h-5"
                      fill="none"
                      stroke="currentColor"
                      viewBox="0 0 24 24"
                    >
                      <path
                        strokeLinecap="round"
                        strokeLinejoin="round"
                        strokeWidth={2}
                        d="M8 10h.01M12 10h.01M16 10h.01M9 16H5a2 2 0 01-2-2V6a2 2 0 012-2h14a2 2 0 012 2v8a2 2 0 01-2 2h-5l-5 5v-5z"
                      />
                    </svg>
                  </div>

                  <span className="absolute -top-1 -right-1 flex items-center justify-center w-5 h-5 text-xs font-bold text-on-surface bg-accent rounded-full">
                    {messageUnreadCount}
                  </span>
                </button>
              )}

              {/* cart */}
              {user != null && (
                <button
                  onClick={() => navigate("/cart")}
                  className="relative flex items-center justify-center text-on-surface-dim hover:text-on-surface bg-transparent hover:bg-surface-2 focus:ring-2 focus:ring-surface-3 font-medium rounded-base text-sm w-10 h-10 focus:outline-none "
                  aria-label="Cart"
                >
                  <div className="border-2 border-gray-800 p-2 rounded-full">
                    <svg
                      className="w-5 h-5 "
                      fill="none"
                      stroke="currentColor"
                      viewBox="0 0 24 24"
                    >
                      <path
                        strokeLinecap="round"
                        strokeLinejoin="round"
                        strokeWidth={2}
                        d="M3 3h2l.4 2M7 13h10l4-8H5.4M7 13L5.4 5M7 13l-2.293 2.293c-.63.63-.184 1.707.707 1.707H17m0 0a2 2 0 100 4 2 2 0 000-4zm-8 2a2 2 0 11-4 0 2 2 0 014 0z"
                      />
                    </svg>
                  </div>
                  {cartItemCount > 0 && (
                    <span className="absolute -top-1 -right-1 flex items-center justify-center w-5 h-5 text-xs font-bold text-on-surface bg-accent rounded-full">
                      {cartItemCount}
                    </span>
                  )}
                </button>
              )}
            </div>
            {/* notification */}

            {user != null && (
              <div className="relative">
                <button
                  onClick={() =>
                    setIsNotificationMenuOpen(!isNotificationMenuOpen)
                  }
                  className="flex items-center justify-center text-sm bg-surface-2 rounded-full w-10 h-10 focus:ring-4 focus:ring-surface-3"
                  aria-label="Open user menu"
                >
                  <div className="border-2 border-gray-800 p-2 rounded-full">
                    <svg
                      className="w-5 h-5"
                      viewBox="0 0 24 24"
                      xmlns="http://www.w3.org/2000/svg"
                    >
                      <g>
                        <path d="M0 0h24v24H0z" />
                        <path
                          d="M20 17h2v2H2v-2h2v-7a8 8 0 1 1 16 0v7zm-2 0v-7a6 6 0 1 0-12 0v7h12zm-9 4h6v2H9v-2z"
                          fill="#FFFFFF"
                        />
                      </g>
                    </svg>
                  </div>
                  {notificationUnreadCount > 0 && (
                    <span className="absolute -top-1 -right-1 flex items-center justify-center w-5 h-5 text-xs font-bold text-on-surface bg-accent rounded-full">
                      {notificationUnreadCount}
                    </span>
                  )}
                </button>

                {isNotificationMenuOpen && (
                  <div
                    className="fixed inset-0 z-30"
                    onClick={() => setIsNotificationMenuOpen(false)}
                  />
                )}

                {/* notification dropdown */}
                {isNotificationMenuOpen && (
                  <div className="absolute bg-surface-base right-0 z-40 mt-2 w-65 divide-y divide-surface-3 rounded-base shadow-lg border border-surface-3">
                    <div className=" py-3">
                      <span className="block text-sm text-on-surface font-semibold">
                        Notifications
                      </span>
                    </div>
                    <ul className=" px-2">
                      {notifications && notifications.length > 0 ? (
                        notifications.map((notification) => (
                          <Notification
                            key={notification.id}
                            notification={notification}
                            navigate={navigate}
                            closeMenu={setIsNotificationMenuOpen}
                            markAsRead={markAsRead}
                          />
                        ))
                      ) : (
                        <li className=" py-2 text-sm text-gray-400 text-center">
                          No notifications
                        </li>
                      )}
                    </ul>
                    <div className="py-2">
                      <button
                        onClick={() => {
                          navigate("/notifications");
                        }}
                        className="w-full text-left block px-4 py-2 text-sm text-on-surface-dim hover:bg-surface-2 hover:text-on-surface"
                      >
                        All notifications
                      </button>
                    </div>
                  </div>
                )}
              </div>
            )}

            {/* user menu */}
            {user != null && (
              <div className="relative">
                <button
                  onClick={() => setIsUserMenuOpen(!isUserMenuOpen)}
                  className="flex items-center justify-center text-sm bg-surface-2 rounded-full w-10 h-10 focus:ring-4 focus:ring-surface-3"
                  aria-label="Open user menu"
                >
                  <span className="text-on-surface font-extrabold text-center  border-3 border-white py-2 px-3.5 rounded-full">
                    {user?.username?.charAt(0).toUpperCase() || "U"}
                  </span>
                </button>

                {isUserMenuOpen && (
                  <div
                    className="fixed inset-0 z-30"
                    onClick={() => setIsUserMenuOpen(false)}
                  />
                )}

                {/* user menu drop */}
                {isUserMenuOpen && (
                  <div className="absolute bg-surface-base right-0 z-40 mt-2 w-56 divide-y divide-surface-3 rounded-base shadow-lg border border-surface-3">
                    <div className="px-4 py-3">
                      <span className="block text-sm text-on-surface font-semibold">
                        {user?.username || ""}
                      </span>
                      <span className="block text-sm text-on-surface-dim truncate">
                        {user.email}
                      </span>
                    </div>
                    <ul className="py-2">
                      <li>
                        <button
                          onClick={() => {
                            navigate("/listings");
                            setIsUserMenuOpen(false);
                          }}
                          className="w-full text-left block px-4 py-2 text-sm text-on-surface-dim hover:bg-surface-2 hover:text-on-surface"
                        >
                          My listings
                        </button>
                      </li>
                      <li>
                        <button
                          onClick={() => {
                            navigate("/profile");
                            setIsUserMenuOpen(false);
                          }}
                          className="w-full text-left block px-4 py-2 text-sm text-on-surface-dim hover:bg-surface-2 hover:text-on-surface"
                        >
                          Profile
                        </button>
                      </li>
                      <li>
                        <button
                          onClick={() => {
                            navigate("/wishlist");
                            setIsUserMenuOpen(false);
                          }}
                          className="w-full text-left block px-4 py-2 text-sm text-on-surface-dim hover:bg-surface-2 hover:text-on-surface"
                        >
                          Wishlist
                        </button>
                      </li>
                      <li>
                        <button
                          onClick={() => {
                            navigate("/earnings");
                            setIsUserMenuOpen(false);
                          }}
                          className="w-full text-left block px-4 py-2 text-sm text-on-surface-dim hover:bg-surface-2 hover:text-on-surface"
                        >
                          Earnings
                        </button>
                      </li>
                    </ul>
                    <div className="py-2">
                      <button
                        onClick={() => {
                          handleLogOut();
                          setIsUserMenuOpen(false);
                        }}
                        className="w-full text-left block px-4 py-2 text-sm text-on-surface-dim hover:bg-surface-2 hover:text-on-surface"
                      >
                        Sign out
                      </button>
                    </div>
                  </div>
                )}
              </div>
            )}

            {/* mmobile menu button */}
            <button
              onClick={() => setIsMobileMenuOpen(!isMobileMenuOpen)}
              className="inline-flex items-center p-2 w-10 h-10 justify-center text-sm text-on-surface-dim rounded-base lg:hidden hover:bg-surface-2 hover:text-on-surface focus:outline-none focus:ring-2 focus:ring-surface-3"
              aria-label="Open main menu"
            >
              <svg
                className="w-5 h-5"
                fill="none"
                stroke="currentColor"
                viewBox="0 0 24 24"
              >
                <path
                  strokeLinecap="round"
                  strokeLinejoin="round"
                  strokeWidth={2}
                  d="M4 6h16M4 12h16M4 18h16"
                />
              </svg>
            </button>
          </div>
        </div>

        {/* mobile search */}
        {isMobileSearchOpen && (
          <div className="md:hidden pb-4">
            <div className="relative">
              <div className="absolute inset-y-0 left-0 flex items-center pl-3 pointer-events-none">
                <svg
                  className="w-4 h-4 text-on-surface-dim"
                  fill="none"
                  stroke="currentColor"
                  viewBox="0 0 24 24"
                >
                  <path
                    strokeLinecap="round"
                    strokeLinejoin="round"
                    strokeWidth={2}
                    d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z"
                  />
                </svg>
              </div>
              <input
                type="search"
                className="block w-full p-2.5 pl-10 text-sm text-on-surface border border-surface-3 rounded-base bg-surface-1 focus:ring-accent focus:border-accent placeholder:text-on-surface-muted"
                placeholder="Search products..."
                value={query}
                onChange={(e) => setQuery(e.target.value)}
                onKeyDown={handleKeyPress}
              />
            </div>
          </div>
        )}

        {/* navigation */}
        {/* <div
          className={`${
            isMobileMenuOpen ? "block" : "hidden"
          } lg:flex lg:items-center lg:justify-center lg:space-x-8 border-t border-surface-3 lg:border-t-0 py-4 lg:py-3`}
        >
          <ul className="flex flex-col lg:flex-row lg:items-center gap-4 lg:gap-8">
            <li>
              <button
                onClick={() => {
                  navigate("/");
                  setIsMobileMenuOpen(false);
                }}
                className="block py-2 text-on-surface-dim hover:text-on-surface transition-colors"
              >
                Home
              </button>
            </li>
            <li>
              <button
                onClick={() => {
                  navigate("/about");
                  setIsMobileMenuOpen(false);
                }}
                className="block py-2 text-on-surface-dim hover:text-on-surface transition-colors"
              >
                About
              </button>
            </li>
            <li>
              <button
                onClick={() => {
                  navigate("/services");
                  setIsMobileMenuOpen(false);
                }}
                className="block py-2 text-on-surface-dim hover:text-on-surface transition-colors"
              >
                Services
              </button>
            </li>
            <li>
              <button
                onClick={() => {
                  navigate("/orders");
                  setIsMobileMenuOpen(false);
                }}
                className="block py-2 text-on-surface-dim hover:text-on-surface transition-colors"
              >
                Orders
              </button>
            </li>
            {user?.role === "admin" && (
              <li>
                <button
                  onClick={() => {
                    navigate("/admin");
                    setIsMobileMenuOpen(false);
                  }}
                  className="block py-2 text-on-surface-dim hover:text-on-surface transition-colors"
                >
                  AdminDash
                </button>
              </li>
            )}
          </ul>
        </div> */}
      </div>

      <AuthModal />
    </nav>
  );
}
