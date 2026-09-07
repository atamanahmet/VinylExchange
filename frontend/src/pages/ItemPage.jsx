import { useEffect, useMemo, useRef, useState } from "react";
import { createPortal } from "react-dom";
import { useNavigate, useParams } from "react-router-dom";
import {
  History,
  MessageSquare,
  Pencil,
  ShoppingCart,
  Trash2,
  X,
} from "lucide-react";
import dayjs from "dayjs";

import axios from "@/api/axiosInstance";

import ImageGallery from "@/components/shared/ImageGallery";
import { Button } from "@/components/ui/button";
import {
  Card,
  CardContent,
  CardFooter,
  CardHeader,
  CardTitle,
} from "@/components/ui/card";
import { cn } from "@/lib/utils";
import {
  GALLERY_THUMB_SKELETON_CLASS,
  MAIN_IMAGE_SKELETON_CLASS,
} from "@/utils/galleryLayout";
import { useAuthStore } from "../stores/authStore";
import { useCartStore } from "../stores/cartStore";
import { useListingStore } from "../stores/listingStore";
import {
  getListingFormatLabel,
  getListingLabelName,
} from "../utils/mediaInfo";

function DetailRow({ label, children, accent = false, className }) {
  return (
    <div className={cn("grid gap-1 sm:grid-cols-[7rem_1fr] sm:items-baseline sm:gap-3", className)}>
      <dt className="text-xs font-medium uppercase tracking-wider text-on-surface-muted">
        {label}
      </dt>
      <dd
        className={cn(
          "text-sm text-on-surface-bright",
          accent && "font-medium text-accent-text",
        )}
      >
        {children}
      </dd>
    </div>
  );
}

function formatPrice(value) {
  if (value == null) return null;
  return `${Number(value).toLocaleString("tr-TR")} ₺`;
}

function ItemPageSkeleton() {
  return (
    <div className="min-h-[60vh] bg-surface-base px-4 py-10 sm:px-6 lg:px-8">
      <div className="mx-auto flex max-w-6xl flex-col gap-8 lg:flex-row lg:gap-12">
        <div className="mx-auto w-full max-w-xl lg:mx-0 lg:max-w-none">
          <div className="space-y-3">
            <div className={MAIN_IMAGE_SKELETON_CLASS} aria-hidden="true" />
            <div className="flex gap-2">
              {Array.from({ length: 4 }, (_, index) => (
                <div key={index} className={GALLERY_THUMB_SKELETON_CLASS} />
              ))}
            </div>
          </div>
        </div>
        <div className="flex flex-1 animate-pulse flex-col gap-4">
          <div className="h-4 w-24 rounded bg-surface-3" />
          <div className="h-10 w-3/4 rounded bg-surface-3" />
          <div className="h-24 rounded-xl bg-surface-2" />
          <div className="h-12 w-40 rounded bg-surface-3" />
        </div>
      </div>
    </div>
  );
}

export default function ItemPage() {
  const user = useAuthStore((state) => state.user);
  const addToCart = useCartStore((state) => state.addToCart);
  const removeFromCart = useCartStore((state) => state.removeFromCart);
  const fetchCart = useCartStore((state) => state.fetchCart);
  const cart = useCartStore((state) => state.cart);
  const listing = useListingStore((state) => state.currentListing);
  const isFetching = useListingStore((state) => state.isFetchingCurrent);
  const fetchListing = useListingStore((state) => state.fetchListing);

  const { publicId } = useParams();
  const navigate = useNavigate();

  const [openModal, setOpenModal] = useState(false);
  const [openModalUrl, setOpenModalUrl] = useState("");
  const [cartBusy, setCartBusy] = useState(false);
  const [priceHistoryOpen, setPriceHistoryOpen] = useState(false);
  const [priceHistory, setPriceHistory] = useState([]);
  const [priceHistoryLoading, setPriceHistoryLoading] = useState(false);
  const [priceHistoryPosition, setPriceHistoryPosition] = useState({
    top: 0,
    left: 0,
  });
  const priceHistoryAnchorRef = useRef(null);
  const priceHistoryPanelRef = useRef(null);

  function updatePriceHistoryPosition() {
    const anchor = priceHistoryAnchorRef.current;
    if (!anchor) return;

    const rect = anchor.getBoundingClientRect();
    setPriceHistoryPosition({
      top: rect.bottom + 4,
      left: rect.left,
    });
  }

  const isOwner = useMemo(
    () => Boolean(user && listing && listing.ownerUsername === user.username),
    [user, listing],
  );

  const cartItem = useMemo(
    () =>
      cart?.items?.find(
        (item) => String(item.publicId) === String(listing?.publicId),
      ),
    [cart?.items, listing?.publicId],
  );

  const inCart = Boolean(cartItem);

  const hasDiscount = Boolean(listing?.discountPercent);
  const displayPrice = listing?.price;
  const originalPrice = hasDiscount ? listing?.originalPriceKurus : null;
  const formatLabel = getListingFormatLabel(listing);
  const labelName = getListingLabelName(listing);
  const listingReady = listing?.publicId === publicId;

  useEffect(() => {
    if (!publicId) return;
    fetchListing(publicId);
  }, [publicId, fetchListing]);

  useEffect(() => {
    if (user) {
      fetchCart();
    }
  }, [user, fetchCart]);

  useEffect(() => {
    setPriceHistoryOpen(false);
    setPriceHistory([]);
  }, [publicId]);

  useEffect(() => {
    if (!priceHistoryOpen) return;

    updatePriceHistoryPosition();

    function handleClickOutside(event) {
      const target = event.target;
      if (
        priceHistoryAnchorRef.current?.contains(target) ||
        priceHistoryPanelRef.current?.contains(target)
      ) {
        return;
      }
      setPriceHistoryOpen(false);
    }

    function handleReposition() {
      updatePriceHistoryPosition();
    }

    document.addEventListener("mousedown", handleClickOutside);
    window.addEventListener("resize", handleReposition);
    window.addEventListener("scroll", handleReposition, true);

    return () => {
      document.removeEventListener("mousedown", handleClickOutside);
      window.removeEventListener("resize", handleReposition);
      window.removeEventListener("scroll", handleReposition, true);
    };
  }, [priceHistoryOpen]);

  async function togglePriceHistory() {
    if (!priceHistoryOpen && priceHistory.length === 0 && !priceHistoryLoading) {
      setPriceHistoryLoading(true);
      try {
        const res = await axios.get(`/api/listings/${publicId}/price-history`);
        setPriceHistory(Array.isArray(res.data) ? res.data : []);
      } catch {
        setPriceHistory([]);
      } finally {
        setPriceHistoryLoading(false);
      }
    }
    setPriceHistoryOpen((open) => !open);
  }

  function openModalImage(url) {
    setOpenModalUrl(url);
    setOpenModal(true);
  }

  async function handleCartAction() {
    const listingPublicId = listing?.publicId;
    if (!listingPublicId || cartBusy) return;

    setCartBusy(true);
    try {
      if (inCart && cartItem?.id) {
        await removeFromCart(cartItem.id);
      } else {
        await addToCart(listingPublicId, 1);
      }
    } finally {
      setCartBusy(false);
    }
  }

  if (!listingReady) {
    if (isFetching) {
      return <ItemPageSkeleton />;
    }

    return (
      <div className="flex min-h-[50vh] items-center justify-center bg-surface-base px-4 text-on-surface-muted">
        Listing not found.
      </div>
    );
  }

  return (
    <div className="bg-surface-base text-left text-on-surface">
      {openModal && (
        <div
          className="fixed inset-0 z-50 flex items-center justify-center bg-surface-base/85 p-4 backdrop-blur-sm"
          onClick={() => setOpenModal(false)}
          role="dialog"
          aria-modal="true"
          aria-label="Image preview"
        >
          <Button
            type="button"
            variant="secondary"
            size="icon"
            className="absolute top-4 right-4 z-10"
            onClick={() => setOpenModal(false)}
            aria-label="Close preview"
          >
            <X />
          </Button>
          <img
            src={openModalUrl}
            alt=""
            className="max-h-[90vh] max-w-full rounded-2xl object-contain ring-1 ring-surface-4"
            onClick={(event) => event.stopPropagation()}
          />
        </div>
      )}

      <main className="mx-auto max-w-6xl px-4 pt-8 pb-6 sm:px-6 sm:pt-10 sm:pb-8 lg:px-8">
        <div className="grid items-start gap-6 lg:grid-cols-[minmax(0,33rem)_minmax(0,1fr)] lg:gap-8 xl:grid-cols-[minmax(0,36rem)_1fr]">
          <div className="mx-auto w-full max-w-xl lg:mx-0 lg:max-w-none">
            <ImageGallery
              key={publicId}
              imagePaths={listing.imagePaths}
              openModal={openModalImage}
            />
          </div>

          <Card className="gap-0 border-surface-3 bg-surface-1 py-0 shadow-none ring-1 ring-surface-3">
            <CardHeader className="gap-2 border-b border-surface-3 px-5 pt-6 pb-4 sm:px-6">
              {labelName && (
                <p className="text-xs font-semibold uppercase tracking-[0.2em] text-accent-text">
                  {labelName}
                </p>
              )}
              <CardTitle className="text-2xl leading-tight font-semibold text-on-surface sm:text-3xl">
                {listing.title}
              </CardTitle>
              {listing.artistName && (
                <p className="text-base font-medium text-accent-text sm:text-lg">
                  {listing.artistName}
                </p>
              )}
            </CardHeader>

            <CardContent className="space-y-4 px-5 py-4 sm:px-6">
              <dl className="space-y-2">
                {formatLabel && (
                  <DetailRow label="Format">{formatLabel}</DetailRow>
                )}
                {listing.genres?.length > 0 && (
                  <DetailRow label="Genre">
                    <span className="flex flex-wrap gap-1.5">
                      {listing.genres.map((genre) => (
                        <span
                          key={genre}
                          className="rounded-md bg-surface-3 px-2 py-0.5 text-xs font-medium text-on-surface-bright"
                        >
                          {genre}
                        </span>
                      ))}
                    </span>
                  </DetailRow>
                )}
                {listing.condition && (
                  <DetailRow label="Condition">{listing.condition}</DetailRow>
                )}
                {listing.year > 0 && (
                  <DetailRow label="Year">{listing.year}</DetailRow>
                )}
                {listing.country && (
                  <DetailRow label="Country">{listing.country}</DetailRow>
                )}
                <DetailRow label="Seller">
                  <button
                    type="button"
                    onClick={() => navigate(`/seller/${listing.ownerUsername}`)}
                    className="font-medium text-brand-fg hover:text-amber-400 transition-colors"
                  >
                    {listing.ownerUsername}
                  </button>
                </DetailRow>
                <DetailRow label="Stock">{listing.stockQuantity}</DetailRow>
                {listing.createdAt && (
                  <DetailRow label="Listed">
                    {dayjs(listing.createdAt).format("D MMM YYYY")}
                  </DetailRow>
                )}
              </dl>

              {listing.description && (
                <p className="border-t border-surface-3 pt-3 text-sm leading-relaxed text-on-surface-dim">
                  {listing.description}
                </p>
              )}

              <div className="flex flex-wrap items-end gap-2 border-t border-surface-3 pt-3">
                {displayPrice != null && (
                  <div className="flex items-center gap-1.5">
                    <p className="text-2xl font-bold tracking-tight text-success-fg sm:text-3xl">
                      {formatPrice(displayPrice)}
                    </p>
                    <div ref={priceHistoryAnchorRef}>
                      <Button
                        type="button"
                        variant="ghost"
                        size="icon-sm"
                        aria-label="Price history"
                        aria-expanded={priceHistoryOpen}
                        onClick={togglePriceHistory}
                      >
                        <History className="size-4 text-on-surface-muted" />
                      </Button>
                    </div>
                    {priceHistoryOpen &&
                      createPortal(
                        <div
                          ref={priceHistoryPanelRef}
                          className="fixed z-[200] w-[9.6rem] max-h-48 overflow-y-auto rounded-lg border border-surface-3 bg-surface-1 py-1 shadow-xl"
                          style={{
                            top: priceHistoryPosition.top,
                            left: priceHistoryPosition.left,
                          }}
                        >
                          {priceHistoryLoading ? (
                            <p className="px-2 py-1.5 text-sm text-on-surface-muted">
                              Loading…
                            </p>
                          ) : priceHistory.length === 0 ? (
                            <p className="px-2 py-1.5 text-sm text-on-surface-muted">
                              No price history.
                            </p>
                          ) : (
                            <ul className="divide-y divide-surface-3">
                              {priceHistory.map((entry, index) => (
                                <li
                                  key={`${entry.occurredAt}-${index}`}
                                  className="flex w-full items-center justify-between gap-2 px-2 py-1.5"
                                >
                                  <span className="text-sm font-medium tabular-nums text-success-fg">
                                    {formatPrice(entry.newPriceKurus)}
                                  </span>
                                  <span className="shrink-0 text-xs whitespace-nowrap text-on-surface-muted">
                                    {dayjs(entry.occurredAt).format(
                                      "D MMM YY",
                                    )}
                                  </span>
                                </li>
                              ))}
                            </ul>
                          )}
                        </div>,
                        document.body,
                      )}
                  </div>
                )}
                {hasDiscount && (
                  <span className="rounded-md bg-accent-soft px-2.5 py-1 text-sm font-semibold text-accent">
                    -{listing.discountPercent}%
                  </span>
                )}
                {originalPrice != null && (
                  <p className="text-base text-on-surface-muted line-through">
                    {formatPrice(originalPrice)}
                  </p>
                )}
              </div>
            </CardContent>

            <CardFooter className="flex-col items-stretch gap-3 border-t border-surface-3 bg-surface-2/40 px-5 py-4 sm:px-6">
              {!isOwner ? (
                <div className="flex flex-col gap-3 sm:flex-row">
                  <Button
                    type="button"
                    variant="outline"
                    className="h-11 min-w-0 flex-1 gap-2 px-4"
                    onClick={() => navigate(`/messaging/${listing.publicId}`)}
                  >
                    <MessageSquare className="size-4 shrink-0" />
                    Trade
                  </Button>
                  <Button
                    type="button"
                    variant={inCart ? "destructive" : "default"}
                    className={cn(
                      "h-11 min-w-0 flex-1 gap-2 px-4 transition-colors",
                      inCart
                        ? "bg-danger text-on-surface hover:bg-danger-hover focus-visible:border-danger focus-visible:ring-danger/40"
                        : "bg-brand text-on-surface hover:bg-brand-hover",
                    )}
                    onClick={handleCartAction}
                    disabled={cartBusy}
                    aria-pressed={inCart}
                  >
                    {inCart ? (
                      <Trash2 className="size-4" />
                    ) : (
                      <ShoppingCart className="size-4" />
                    )}
                    {cartBusy
                      ? inCart
                        ? "Removing…"
                        : "Adding…"
                      : inCart
                        ? "Remove"
                        : "Add to cart"}
                  </Button>
                </div>
              ) : (
                <Button
                  type="button"
                  className="h-11 w-full gap-2 bg-accent hover:bg-accent-hover"
                  onClick={() => navigate(`/edit/${listing.publicId}`)}
                >
                  <Pencil className="size-4" />
                  Edit listing
                </Button>
              )}
            </CardFooter>
          </Card>
        </div>
      </main>
    </div>
  );
}
