import { useCallback, useEffect, useMemo, useState } from "react";
import { useNavigate } from "react-router-dom";

import ConfirmDialog from "@/components/shared/ConfirmDialog";
import PageContainer from "@/components/layout/PageContainer";
import ListView from "@/components/listing/ListView";
import ListViewHeader from "@/components/listing/ListViewHeader";
import SkeletonListView from "@/components/shared/skeletons/SkeletonListView";

import { useListingStore } from "../stores/listingStore";
import { useAuthStore } from "../stores/authStore";
import { mapListingsToCardItems } from "../adapters/mapListingToCardItems";

export default function ListingsPage() {
  const navigate = useNavigate();

  const user = useAuthStore((state) => state.user);

  const myListings = useListingStore((state) => state.myListings);
  const isFetchingMine = useListingStore((state) => state.isFetchingMine);

  const fetchMyActiveListings = useListingStore(
    (state) => state.fetchMyActiveListings,
  );

  const deleteListing = useListingStore((state) => state.deleteListing);

  const [pendingDeleteId, setPendingDeleteId] = useState(null);
  const [isDeleting, setIsDeleting] = useState(false);

  useEffect(() => {
    fetchMyActiveListings();
  }, [fetchMyActiveListings]);

  const myListingCards = useMemo(() => {
    return mapListingsToCardItems(myListings.items, {
      user,
      navigate,
      onDelete: setPendingDeleteId,
    });
  }, [myListings.items, user, navigate]);

  const pendingDeleteTitle = useMemo(
    () =>
      myListingCards.find((item) => item.id === pendingDeleteId)?.title ?? "",
    [myListingCards, pendingDeleteId],
  );

  const handleConfirmDelete = useCallback(async () => {
    if (!pendingDeleteId) return;

    setIsDeleting(true);
    try {
      await deleteListing(pendingDeleteId);
    } finally {
      setIsDeleting(false);
      setPendingDeleteId(null);
    }
  }, [deleteListing, pendingDeleteId]);

  const isEmpty = !isFetchingMine && myListingCards.length === 0;

  return (
    <PageContainer width="wide">
      <h1 className="text-left text-2xl font-semibold sm:text-3xl">
        My listings
      </h1>

      {isEmpty ? (
        <div className="rounded-xl border border-surface-3 bg-surface-1 px-6 py-12 text-center">
          <p className="text-lg font-medium text-on-surface">
            No active listings
          </p>
          <p className="mt-2 text-sm text-on-surface-muted">
            Listings you publish will show up here.
          </p>
        </div>
      ) : (
        <div className="overflow-hidden rounded-xl border border-surface-3">
          <ListViewHeader />

          <div>
            {isFetchingMine
              ? Array(5)
                  .fill(0)
                  .map((_, i) => <SkeletonListView key={i} />)
              : myListingCards.map((item) => (
                  <ListView key={item.id} item={item} />
                ))}
          </div>
        </div>
      )}

      <ConfirmDialog
        open={Boolean(pendingDeleteId)}
        onOpenChange={(next) => !next && setPendingDeleteId(null)}
        title="Delete this listing?"
        description={
          pendingDeleteTitle
            ? `"${pendingDeleteTitle}" will be removed permanently. This cannot be undone.`
            : "This listing will be removed permanently. This cannot be undone."
        }
        confirmLabel="Delete"
        destructive
        isPending={isDeleting}
        onConfirm={handleConfirmDelete}
      />
    </PageContainer>
  );
}
