import { useCallback, useEffect, useMemo, useRef, useState } from "react";
import { Plus, X } from "lucide-react";
import { useNavigate, useParams } from "react-router-dom";
import { toast } from "sonner";

import { Button } from "@/components/ui/button";
import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from "@/components/ui/card";
import { Checkbox } from "@/components/ui/checkbox";
import {
  Field,
  FieldDescription,
  FieldGroup,
  FieldLabel,
} from "@/components/ui/field";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Separator } from "@/components/ui/separator";
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from "@/components/ui/dialog";
import { Textarea } from "@/components/ui/textarea";
import { cn } from "@/lib/utils";

import { mbReleaseToListingMap } from "../adapters/mbReleaseToListingMap";
import MediaInfoFields from "@/components/listing/MediaInfoFields";
import MbReleaseFilterBar from "@/components/listing/MbReleaseFilterBar";
import ReleaseCard from "@/components/listing/Card";
import ImageUploader from "@/components/listing/ImageUploader";
import AddressFormDialog from "@/components/account/AddressFormDialog";
import SkeletonCardView from "@/components/shared/skeletons/SkeletonCardView";
import { useAuthStore } from "../stores/authStore";
import { useAddressStore } from "../stores/addressStore";
import { useSearchStore } from "../stores/searchStore";
import { useMbReleaseFilters } from "../hooks/useMbReleaseFilters";
import { useMbScrollLoadMore } from "../hooks/useMbScrollLoadMore";
import axios from "../api/axiosInstance";
import { CARD_GRID_CLASS } from "../utils/cardLayout";
import { buildListingPath } from "../utils/listingPath";
import {
  emptyMediaInfo,
  normalizeMediaInfoFromApi,
  sanitizeMediaInfoForSubmit,
} from "../utils/mediaInfo";
import { getApiErrorMessage, isShippingAddressRequiredError } from "../utils/apiErrorMessage";
import {
  ensureUserLoggedIn,
  isAuthRequiredError,
  promptReauthentication,
} from "../utils/authSession";

const PACKAGING_OPTIONS = [
  { value: "SEALED", label: "Sealed" },
  { value: "OPENED", label: "Opened" },
  { value: "RESEALED", label: "Resealed" },
];

const SELECT_CLASS =
  "h-8 w-full min-w-0 rounded-lg border border-accent-muted bg-surface-form px-2.5 py-1 text-base text-on-surface transition-colors outline-none focus-visible:border-brand-active focus-visible:ring-3 focus-visible:ring-brand-active/50 disabled:pointer-events-none disabled:cursor-not-allowed disabled:bg-surface-form/50 disabled:opacity-50 md:text-sm";

const CONDITION_OPTIONS = [
  { value: "P", label: "(P) (F) Poor / Fair" },
  { value: "G", label: "(G) Good" },
  { value: "VG", label: "(VG) Very Good" },
  { value: "VG+", label: "(VG+) Very Good+" },
  { value: "E", label: "(E) Excellent" },
  { value: "NM", label: "(NM) Near Mint" },
  { value: "M", label: "(M) Mint" },
];

const PAYMENT_DIRECTIONS = [
  { value: "NO_EXTRA", label: "0" },
  { value: "PAY", label: "Pay" },
  { value: "RECEIVE", label: "Receive" },
];

function mapImagePathsToUploaderImages(imagePaths = []) {
  return imagePaths.map((url, index) => ({
    preview: url,
    isExisting: true,
    url,
    name: `existing-image-${index}`,
  }));
}

function sanitizeListingPayload(payload) {
  const next = { ...payload };

  if (!next.mbId) {
    delete next.mbId;
  }

  if (typeof next.year === "string" && next.year.trim()) {
    next.year = Number.parseInt(next.year, 10);
  } else if (next.year === "" || Number.isNaN(next.year)) {
    delete next.year;
  }

  next.mediaInfo = sanitizeMediaInfoForSubmit(next.mediaInfo);

  return next;
}

function paymentDirectionButtonClass(direction, isActive) {
  if (!isActive) {
    return "border-accent-muted bg-surface-form text-on-surface-dim hover:bg-surface-3 hover:text-on-surface";
  }

  if (direction === "PAY") {
    return "border-danger bg-danger text-on-surface hover:bg-danger-hover";
  }

  if (direction === "RECEIVE") {
    return "border-success bg-success text-on-surface hover:bg-success-hover";
  }

  return "border-brand bg-brand text-on-surface hover:bg-brand-hover";
}

function FormSection({ title, description, children, className, contentClassName }) {
  return (
    <Card
      className={cn(
        "flex h-full min-h-0 flex-col border-surface-3 bg-surface-2 shadow-xs ring-1 ring-surface-4",
        className,
      )}
    >
      <CardHeader className="shrink-0 space-y-1 px-4 pb-2 pt-4 sm:px-6">
        <CardTitle className="text-base font-semibold text-on-surface sm:text-lg">
          {title}
        </CardTitle>
        {description && (
          <CardDescription className="text-xs text-on-surface-muted sm:text-sm">
            {description}
          </CardDescription>
        )}
      </CardHeader>
      <CardContent
        className={cn(
          "min-h-0 flex-1 overflow-y-auto px-4 pb-4 sm:px-6",
          contentClassName,
        )}
      >
        <FieldGroup className="gap-3">{children}</FieldGroup>
      </CardContent>
    </Card>
  );
}

function RadioGroup({
  name,
  value,
  onChange,
  options,
  required = false,
  layout = "wrap",
}) {
  return (
    <div
      className={cn(
        layout === "column"
          ? "flex flex-col gap-2"
          : "flex flex-wrap gap-x-4 gap-y-2",
      )}
    >
      {options.map((option) => {
        const isChecked =
          value === option.value ||
          (option.value === "other" && value === "Other");

        return (
          <label
            key={option.value}
            className={cn(
              "flex cursor-pointer items-center gap-2 rounded-md border px-2 py-1.5 text-sm transition-colors",
              isChecked
                ? "border-brand/40 bg-brand/10 text-on-surface"
                : "border-transparent text-on-surface-dim hover:border-surface-4 hover:bg-surface-3/50 hover:text-on-surface",
            )}
          >
            <input
              type="radio"
              name={name}
              value={option.value}
              checked={isChecked}
              onChange={onChange}
              required={required}
              className="size-4 accent-brand"
            />
            <span>{option.label}</span>
          </label>
        );
      })}
    </div>
  );
}

export default function ListingForm() {
  const { listingId } = useParams();
  const isEditMode = !!listingId;

  const navigate = useNavigate();

  const createAddress = useAddressStore((state) => state.createAddress);
  const fetchAddresses = useAddressStore((state) => state.fetchAddresses);
  const isSavingAddress = useAddressStore((state) => state.isSaving);
  const searchMusicBrainz = useSearchStore((state) => state.searchMusicBrainz);
  const isLoadingMbSearch = useSearchStore((state) => state.isLoadingMbSearch);
  const isLoadingMoreMb = useSearchStore((state) => state.isLoadingMoreMb);
  const hasMoreMbResults = useSearchStore((state) => state.hasMoreMbResults);
  const mbSearchContext = useSearchStore((state) => state.mbSearchContext);
  const mbSearchResult = useSearchStore((state) => state.mbSearchResult);

  const mbModalScrollRef = useRef(null);
  useMbScrollLoadMore(mbModalScrollRef);

  const [images, setImages] = useState([]);
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [addressDialogOpen, setAddressDialogOpen] = useState(false);
  const [retryListingAfterAddress, setRetryListingAfterAddress] = useState(false);
  const [loading, setLoading] = useState(false);
  const [isSubmitting, setIsSubmitting] = useState(false);

  const [countryOptions, setCountryOptions] = useState([]);
  const [countriesLoading, setCountriesLoading] = useState(true);

  const emptyListing = {
    title: "",
    country: "",
    barcode: "",
    condition: "",
    packaging: "",
    labelName: "",
    mediaInfo: emptyMediaInfo(),
    mbId: "",
    trackCount: 1,
    stockQuantity: 1,
    artistName: "",
    tradeValue: 0,
    description: "",
    year: "",
    tradeable: false,
    price: 0,
    discount: 0,
    tradePreferences: [],
  };

  const [listing, setListing] = useState(emptyListing);

  const mapListingToForm = (data) => ({
    title: data.title ?? "",
    country: data.country ?? "",
    barcode: data.barcode ?? "",
    condition: data.condition ?? "",
    packaging: data.packaging ?? "",
    labelName: data.labelName ?? "",
    mediaInfo: normalizeMediaInfoFromApi(data.mediaInfo),
    mbId: data.mbId ?? "",
    trackCount: data.trackCount ?? 1,
    stockQuantity: data.stockQuantity ?? 1,
    artistName: data.artistName ?? "",
    tradeValue: data.tradeValue ?? 0,
    description: data.description ?? "",
    year: data.year ?? "",
    tradeable: data.tradeable ?? false,
    price: data.price ?? 0,
    discount: data.discount ?? 0,
    tradePreferences: data.tradePreferences ?? [],
    imagePaths: Array.isArray(data.imagePaths) ? data.imagePaths : [],
  });

  useEffect(() => {
    if (isEditMode) {
      setImages([]);
      loadListing();
    } else {
      setImages([]);
      setListing(emptyListing);
    }
  }, [listingId]);

  useEffect(() => {
    let cancelled = false;

    axios
      .get("/api/reference/countries", { params: { lang: "en" } })
      .then((res) => {
        if (!cancelled) {
          setCountryOptions(Array.isArray(res.data) ? res.data : []);
        }
      })
      .catch((error) => {
        console.error("Failed to load countries:", error);
        if (!cancelled) {
          toast.error("Failed to load country list");
        }
      })
      .finally(() => {
        if (!cancelled) {
          setCountriesLoading(false);
        }
      });

    return () => {
      cancelled = true;
    };
  }, []);

  const loadListing = async () => {
    try {
      setLoading(true);

      const authenticated = await ensureUserLoggedIn({
        promptMessage: "Sign in to edit this listing.",
      });
      if (!authenticated) {
        navigate("/");
        return;
      }

      const res = await axios.get(`/api/listings/${listingId}`);

      if (res.status === 200) {
        const currentUser = useAuthStore.getState().user;
        if (
          res.data.ownerUsername &&
          currentUser?.username !== res.data.ownerUsername
        ) {
          toast.error("You can only edit your own listings");
          navigate("/");
          return;
        }

        setListing(mapListingToForm(res.data));
        setImages(mapImagePathsToUploaderImages(res.data.imagePaths));
      }
    } catch (error) {
      console.error("Failed to load listing:", error);
      if (error.response?.status === 404) {
        toast.error("Listing not found");
        navigate("/listings");
      } else if (isAuthRequiredError(error)) {
        await promptReauthentication(error);
      } else {
        toast.error(getApiErrorMessage(error, "Failed to load listing"));
      }
    } finally {
      setLoading(false);
    }
  };

  const selectMbRelease = useCallback((item) => {
    setListing((prev) => ({
      ...prev,
      title: item.title,
      artistName: item.artist,
      year: item.year || "",
      labelName: item.label || "",
      mbId: item.id || "",
      barcode: item.barcode || "",
      mediaInfo: item.mediaInfo || emptyMediaInfo(),
      country: item.country || "",
      trackCount: item.trackCount || 1,
    }));
    setIsModalOpen(false);
  }, []);

  const mbReleases = mbSearchResult?.items;

  const {
    filters: mbFilters,
    setFilters: setMbFilters,
    bounds: mbFilterBounds,
    filteredReleases: filteredMbReleases,
    hasReleases: hasMbReleases,
    releaseCount: mbReleaseCount,
  } = useMbReleaseFilters(mbReleases, {
    isLoadingSearch: isLoadingMbSearch,
    searchContext: mbSearchContext,
  });

  const searchItems = useMemo(
    () =>
      filteredMbReleases.map((release) =>
        mbReleaseToListingMap(release, selectMbRelease),
      ),
    [filteredMbReleases, selectMbRelease],
  );

  const checkRelease = async () => {
    if (!listing.title) {
      toast.warning("Enter album title first");
      return;
    }
    setIsModalOpen(true);
    await searchMusicBrainz(listing.title);
  };

  const handleChange = (e) => {
    const { name, value, type, checked } = e.target;

    setListing((prev) => {
      const updated = {
        ...prev,
        [name]: type === "checkbox" ? checked : value,
      };

      if (name === "tradeable" && !checked) {
        updated.tradePreferences = [];
      }

      return updated;
    });
  };

  const handleTradePrefChange = (index, field, value) => {
    setListing((prev) => {
      const prefs = [...prev.tradePreferences];
      prefs[index] = {
        ...prefs[index],
        [field]: value,
      };
      return { ...prev, tradePreferences: prefs };
    });
  };

  const addPreference = () => {
    setListing((prev) => ({
      ...prev,
      tradePreferences: [
        ...prev.tradePreferences,
        {
          desiredItem: "",
          paymentDirection: "NO_EXTRA",
          extraAmount: 0,
        },
      ],
    }));
  };

  const removePreference = (index) => {
    setListing((prev) => ({
      ...prev,
      tradePreferences: prev.tradePreferences.filter((_, i) => i !== index),
    }));
  };

  const buildListingFormData = () => {
    const formData = new FormData();

    if (isEditMode) {
      const existingImageUrls = images
        .filter((img) => img.isExisting)
        .map((img) => img.url);

      const newImageFiles = images.filter((img) => !img.isExisting);
      const { id, ownerUsername, discountedPrice, ...cleanListing } = listing;

      formData.append(
        "listing",
        new Blob(
          [
            JSON.stringify(
              sanitizeListingPayload({
                ...cleanListing,
                imagePaths: existingImageUrls,
                tradePreferences: listing.tradeable
                  ? listing.tradePreferences
                  : null,
              }),
            ),
          ],
          { type: "application/json" },
        ),
      );

      newImageFiles.forEach((img) => {
        formData.append("images", img);
      });
    } else {
      const { id, ownerUsername, discountedPrice, ...cleanListing } = listing;

      images.forEach((img) => {
        formData.append("images", img);
      });

      formData.append(
        "listing",
        new Blob(
          [
            JSON.stringify(
              sanitizeListingPayload({
                ...cleanListing,
                tradePreferences: listing.tradeable
                  ? listing.tradePreferences
                  : null,
              }),
            ),
          ],
          { type: "application/json" },
        ),
      );
    }

    return formData;
  };

  const openShippingAddressDialog = (retryAfterSave = false) => {
    setRetryListingAfterAddress(retryAfterSave);
    setAddressDialogOpen(true);
  };

  const ensureShippingAddressForCreate = async () => {
    await fetchAddresses("SHIPPING");
    const shippingAddresses = useAddressStore.getState().addresses;
    if (shippingAddresses.length > 0) {
      return true;
    }

    toast.warning("Add a shipping address before creating a listing.");
    openShippingAddressDialog(false);
    return false;
  };

  const handleAddressDialogSubmit = async (form) => {
    const shippingAddressCount = useAddressStore.getState().addresses.length;
    const payload = {
      ...form,
      country: form.country?.trim() || "TR",
      addressType: "SHIPPING",
      isDefault: shippingAddressCount === 0,
    };

    const result = await createAddress(payload);
    if (!result.success) {
      toast.error(result.message || "Could not save address.");
      return;
    }

    toast.success("Shipping address saved.");
    setAddressDialogOpen(false);

    if (retryListingAfterAddress) {
      setRetryListingAfterAddress(false);
      await saveListing({ retryAfterLogin: true });
    }
  };

  const submitListingRequest = async () => {
    const formData = buildListingFormData();
    const url = isEditMode ? `/api/listings/${listingId}` : "/api/listings";
    const method = isEditMode ? "patch" : "post";

    return axios[method](url, formData, {
      headers: { "Content-Type": "multipart/form-data" },
    });
  };

  const saveListing = async ({ retryAfterLogin = false } = {}) => {
    if (!retryAfterLogin) {
      const authenticated = await ensureUserLoggedIn({
        promptMessage:
          "Sign in to save your listing. Your draft stays on this page.",
      });

      if (!authenticated) {
        toast.warning(
          "Sign in to save your listing. Your draft is still here.",
        );
        return false;
      }
    }

    const loadingMessage = isEditMode ? "Saving changes..." : "Creating listing...";
    const successMessage = isEditMode
      ? "Listing updated successfully"
      : "Listing created successfully";
    const errorFallback = isEditMode
      ? "Failed to update listing"
      : "Failed to create listing";

    setIsSubmitting(true);
    const toastId = toast.loading(loadingMessage);

    try {
      if (!isEditMode) {
        const hasShippingAddress = await ensureShippingAddressForCreate();
        if (!hasShippingAddress) {
          toast.dismiss(toastId);
          setIsSubmitting(false);
          return false;
        }
      }

      const res = await submitListingRequest();
      toast.success(successMessage, { id: toastId });

      const savedListing = res?.data;
      const savedListingId =
        savedListing?.publicId ?? (isEditMode ? listingId : null);

      if (savedListingId) {
        navigate(
          buildListingPath({
            ...listing,
            ...savedListing,
            publicId: savedListingId,
          }),
          { replace: true },
        );
        return true;
      }

      toast.error("Listing saved but could not open the item page.");
      return false;
    } catch (err) {
      if (isAuthRequiredError(err)) {
        toast.dismiss(toastId);
        const loggedIn = await promptReauthentication(err);

        if (loggedIn) {
          setIsSubmitting(false);
          return saveListing({ retryAfterLogin: true });
        }

        return false;
      }

      if (!isEditMode && isShippingAddressRequiredError(err)) {
        toast.dismiss(toastId);
        toast.warning(
          getApiErrorMessage(
            err,
            "You must add a shipping address before creating a listing.",
          ),
        );
        openShippingAddressDialog(true);
        return false;
      }

      toast.error(getApiErrorMessage(err, errorFallback), { id: toastId });
      return false;
    } finally {
      setIsSubmitting(false);
    }
  };

  const handleSubmit = async (e) => {
    e.preventDefault();

    if (isSubmitting) {
      return;
    }

    await saveListing();
  };

  const descriptionLength = listing.description?.length || 0;

  if (isEditMode && loading) {
    return (
      <div className="min-h-screen bg-surface-base px-4 py-12 text-on-surface sm:px-6">
        <Card className="mx-auto max-w-6xl border-surface-3 bg-surface-2 ring-1 ring-surface-4">
          <CardContent className="py-12 text-center text-on-surface-muted">
            Loading listing...
          </CardContent>
        </Card>
      </div>
    );
  }

  return (
    <div className="mx-auto flex min-h-screen w-full max-w-[100rem] flex-col bg-surface-base px-4 py-4 text-on-surface sm:px-6 lg:max-h-[calc(100dvh-4.5rem)] lg:min-h-0 lg:py-5">
      <Dialog open={isModalOpen} onOpenChange={setIsModalOpen}>
        <DialogContent
          showCloseButton
          className="flex max-h-[85vh] flex-col gap-0 overflow-hidden border-surface-3 bg-surface-1 p-0 text-on-surface ring-surface-4 sm:max-w-6xl"
        >
          <DialogHeader className="border-b border-surface-3 px-5 py-4 sm:px-6">
            <DialogTitle className="text-on-surface">Select release</DialogTitle>
            <DialogDescription className="text-on-surface-muted">
              Choose a MusicBrainz release to autofill your listing.
            </DialogDescription>
          </DialogHeader>

          {!isLoadingMbSearch && hasMbReleases && (
            <MbReleaseFilterBar
              filters={mbFilters}
              bounds={mbFilterBounds}
              onFiltersChange={setMbFilters}
              resultCount={searchItems.length}
              totalCount={mbReleaseCount}
              hasMoreMbResults={hasMoreMbResults}
            />
          )}

          <div
            ref={mbModalScrollRef}
            className="flex-1 overflow-y-auto px-5 py-4 sm:px-6"
          >
            {isLoadingMbSearch && (
              <div className={CARD_GRID_CLASS}>
                {Array(6)
                  .fill(0)
                  .map((_, i) => (
                    <SkeletonCardView key={i} />
                  ))}
              </div>
            )}

            {!isLoadingMbSearch && !hasMbReleases && (
              <div className="rounded-xl border border-surface-3 bg-surface-2 px-6 py-10 text-center">
                <p className="font-medium text-on-surface">No results found</p>
                <p className="mt-2 text-sm text-on-surface-muted">
                  Try a different album title.
                </p>
              </div>
            )}

            {!isLoadingMbSearch && hasMbReleases && searchItems.length === 0 && (
                <div className="rounded-xl border border-surface-3 bg-surface-2 px-6 py-10 text-center">
                  <p className="font-medium text-on-surface">
                    No releases match your filters
                  </p>
                  <p className="mt-2 text-sm text-on-surface-muted">
                    Adjust or reset filters to see more results.
                  </p>
                </div>
              )}

            {!isLoadingMbSearch && searchItems.length > 0 && (
              <div className={CARD_GRID_CLASS}>
                {searchItems.map((item) => (
                  <ReleaseCard key={item.id} item={item} />
                ))}
              </div>
            )}

            {isLoadingMoreMb && (
              <div className={cn(CARD_GRID_CLASS, "mt-4")}>
                {Array(3)
                  .fill(0)
                  .map((_, i) => (
                    <SkeletonCardView key={`mb-load-more-${i}`} />
                  ))}
              </div>
            )}
          </div>

          <DialogFooter className="mx-0 mb-0 flex flex-row items-center justify-end gap-3 border-t border-surface-3 bg-surface-2/40 px-5 py-4 sm:px-6">
            <Button type="button" onClick={() => setIsModalOpen(false)}>
              Close
            </Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>

      <AddressFormDialog
        open={addressDialogOpen}
        onOpenChange={setAddressDialogOpen}
        initialAddress={null}
        isSaving={isSavingAddress}
        onSubmit={handleAddressDialogSubmit}
        title="Add shipping address"
        description="Sellers need a shipping address so buyers can receive orders."
        lockAddressType="SHIPPING"
        submitLabel="Save address"
      />

      <div className="mb-4 shrink-0 text-left lg:mb-5">
        <h1 className="text-2xl font-bold tracking-tight text-on-surface sm:text-3xl">
          {isEditMode ? "Edit Listing" : "Create New Listing"}
        </h1>
        <p className="mt-1 text-sm text-on-surface-dim">
          Add release details, pricing, and photos for your vinyl listing.
        </p>
      </div>

      <form
        onSubmit={handleSubmit}
        className="flex min-h-0 flex-1 flex-col text-left"
      >
        <div className="grid min-h-0 flex-1 grid-cols-1 gap-4 lg:grid-cols-3 lg:gap-5 lg:items-stretch">
          <FormSection
            title="Listing information"
            description="Release details, format, and condition."
          >
            <Field>
              <FieldLabel htmlFor="title">Album</FieldLabel>
              <Input
                id="title"
                type="text"
                name="title"
                value={listing.title}
                onChange={handleChange}
                required
              />
              {!isEditMode && (
                <Button
                  type="button"
                  variant="outline"
                  size="sm"
                  className="mt-2 w-fit"
                  onClick={checkRelease}
                >
                  Check Release Info
                </Button>
              )}
            </Field>

            <Field>
              <FieldLabel htmlFor="artistName">Artist / Band</FieldLabel>
              <Input
                id="artistName"
                type="text"
                name="artistName"
                value={listing.artistName}
                onChange={handleChange}
              />
            </Field>

            <Field>
              <FieldLabel>Packaging</FieldLabel>
              <RadioGroup
                name="packaging"
                value={listing.packaging}
                onChange={handleChange}
                options={PACKAGING_OPTIONS}
                required
              />
            </Field>

            <div className="grid gap-4 sm:grid-cols-2">
              <Field>
                <FieldLabel htmlFor="year">Release Year</FieldLabel>
                <Input
                  id="year"
                  type="number"
                  name="year"
                  value={listing.year || ""}
                  min={1900}
                  max={new Date().getFullYear()}
                  onChange={(e) =>
                    handleChange({
                      target: {
                        name: "year",
                        value: Number(e.target.value),
                      },
                    })
                  }
                />
              </Field>

              <Field>
                <FieldLabel htmlFor="country">Country</FieldLabel>
                <select
                  id="country"
                  name="country"
                  value={listing.country}
                  onChange={handleChange}
                  required
                  disabled={countriesLoading}
                  className={SELECT_CLASS}
                >
                  <option value="">
                    {countriesLoading ? "Loading countries..." : "Select country"}
                  </option>
                  {countryOptions.map(({ value, label }) => (
                    <option key={value} value={value}>
                      {label}
                    </option>
                  ))}
                </select>
              </Field>
            </div>

            <Field>
              <FieldLabel htmlFor="labelName">Label</FieldLabel>
              <Input
                id="labelName"
                type="text"
                name="labelName"
                value={listing.labelName}
                onChange={handleChange}
              />
            </Field>

            <Field>
              <FieldLabel htmlFor="barcode">Barcode / Catalog No.</FieldLabel>
              <Input
                id="barcode"
                type="text"
                name="barcode"
                value={listing.barcode}
                onChange={handleChange}
              />
            </Field>

            <div className="grid gap-3 sm:grid-cols-2">
              <Field>
                <FieldLabel htmlFor="stockQuantity">Stock</FieldLabel>
                <Input
                  id="stockQuantity"
                  type="number"
                  name="stockQuantity"
                  value={listing.stockQuantity}
                  min={1}
                  onChange={handleChange}
                />
              </Field>

              <Field>
                <FieldLabel htmlFor="trackCount">Track Count</FieldLabel>
                <Input
                  id="trackCount"
                  type="number"
                  name="trackCount"
                  value={listing.trackCount}
                  min={1}
                  onChange={handleChange}
                />
              </Field>
            </div>

            <Separator className="bg-surface-4" />

            <MediaInfoFields
              mediaInfo={listing.mediaInfo}
              onChange={(mediaInfo) =>
                setListing((prev) => ({ ...prev, mediaInfo }))
              }
            />

            <Field>
              <FieldLabel>Condition</FieldLabel>
              <RadioGroup
                name="condition"
                value={listing.condition}
                onChange={handleChange}
                options={CONDITION_OPTIONS}
                layout="column"
                required
              />
            </Field>
          </FormSection>

          <FormSection
            title="Trade information"
            description="Pricing and optional trade preferences."
          >
            <Field>
              <FieldLabel htmlFor="price">Direct sell price</FieldLabel>
              <div className="relative">
                <Input
                  id="price"
                  type="number"
                  name="price"
                  step="0.01"
                  min={1}
                  value={listing.price}
                  onChange={handleChange}
                  className="pr-10"
                  required
                />
                <span className="pointer-events-none absolute top-1/2 right-3 -translate-y-1/2 text-on-surface-muted">
                  ₺
                </span>
              </div>
            </Field>

            <Field orientation="horizontal">
              <Checkbox
                id="tradeable"
                checked={listing.tradeable}
                onCheckedChange={(checked) =>
                  setListing((prev) => ({
                    ...prev,
                    tradeable: checked === true,
                    tradePreferences: checked ? prev.tradePreferences : [],
                  }))
                }
              />
              <div className="space-y-1">
                <Label htmlFor="tradeable">Open to trade</Label>
                <FieldDescription>
                  Enable trade value and preferred swap records.
                </FieldDescription>
              </div>
            </Field>

            <Field>
              <FieldLabel htmlFor="tradeValue">Trade value</FieldLabel>
              <Input
                id="tradeValue"
                type="number"
                name="tradeValue"
                value={listing.tradeValue}
                onChange={handleChange}
                disabled={!listing.tradeable}
              />
            </Field>

            {listing.tradeable && (
              <>
                <Separator className="bg-surface-4" />

                <div className="space-y-3">
                  <div className="flex items-center justify-between gap-3">
                    <FieldLabel>Trade preferences</FieldLabel>
                    <Button
                      type="button"
                      variant="outline"
                      size="icon-sm"
                      onClick={addPreference}
                      aria-label="Add trade preference"
                    >
                      <Plus />
                    </Button>
                  </div>

                  {listing.tradePreferences.length === 0 && (
                    <p className="text-sm text-on-surface-muted">
                      Add records you would accept in a trade.
                    </p>
                  )}

                  {listing.tradePreferences.map((pref, index) => (
                    <div
                      key={index}
                      className="relative space-y-3 rounded-lg border border-surface-3 bg-surface-2 p-3"
                    >
                      <Button
                        type="button"
                        variant="ghost"
                        size="icon-sm"
                        className="absolute top-2 right-2 text-danger-fg hover:bg-danger/10 hover:text-danger-fg"
                        onClick={() => removePreference(index)}
                        aria-label="Remove trade preference"
                      >
                        <X />
                      </Button>

                      <Field>
                        <FieldLabel htmlFor={`desiredItem-${index}`}>
                          Desired record
                        </FieldLabel>
                        <Input
                          id={`desiredItem-${index}`}
                          type="text"
                          placeholder="Record title"
                          value={pref.desiredItem}
                          onChange={(e) =>
                            handleTradePrefChange(
                              index,
                              "desiredItem",
                              e.target.value,
                            )
                          }
                        />
                      </Field>

                      <Field>
                        <FieldLabel>Value difference</FieldLabel>
                        <div className="flex flex-wrap items-center gap-2">
                          {PAYMENT_DIRECTIONS.map((direction) => (
                            <Button
                              key={direction.value}
                              type="button"
                              size="sm"
                              variant="outline"
                              className={paymentDirectionButtonClass(
                                direction.value,
                                pref.paymentDirection === direction.value,
                              )}
                              onClick={() =>
                                handleTradePrefChange(
                                  index,
                                  "paymentDirection",
                                  direction.value,
                                )
                              }
                            >
                              {direction.label}
                            </Button>
                          ))}

                          <Input
                            type="number"
                            placeholder="Amount"
                            value={pref.extraAmount}
                            onChange={(e) =>
                              handleTradePrefChange(
                                index,
                                "extraAmount",
                                e.target.value,
                              )
                            }
                            disabled={pref.paymentDirection === "NO_EXTRA"}
                            className="w-28"
                          />
                        </div>
                      </Field>
                    </div>
                  ))}
                </div>
              </>
            )}
          </FormSection>

          <FormSection
            title="Description & images"
            description="Notes for buyers and listing photos."
          >
            <Field>
              <FieldLabel htmlFor="description">Description</FieldLabel>
              <Textarea
                id="description"
                name="description"
                rows={3}
                value={listing.description || ""}
                onChange={handleChange}
                maxLength={255}
                className="min-h-[5.5rem] resize-none lg:max-h-28"
              />
              <FieldDescription
                className={cn(
                  "text-right",
                  descriptionLength > 255
                    ? "text-danger-fg"
                    : "text-success-fg",
                )}
              >
                {descriptionLength}/255
              </FieldDescription>
            </Field>

            <Field>
              <FieldLabel>Upload images</FieldLabel>
              <ImageUploader
                compact
                images={images}
                setImages={setImages}
                existingImages={isEditMode ? listing.imagePaths : undefined}
              />
            </Field>
          </FormSection>
        </div>

        <div className="mt-4 flex shrink-0 flex-col-reverse gap-3 border-t border-surface-4 bg-surface-1/40 pt-4 sm:flex-row sm:justify-end lg:mt-5">
          <Button
            type="button"
            variant="outline"
            onClick={() => navigate("/listings")}
          >
            Cancel
          </Button>
          <Button type="submit" disabled={isSubmitting}>
            {isEditMode ? "Save changes" : "Create listing"}
          </Button>
        </div>
      </form>
    </div>
  );
}
