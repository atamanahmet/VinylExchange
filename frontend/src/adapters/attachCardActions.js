export function attachCardActions(cardData, context) {
  const {
    user,
    cartItemByListingId = new Map(),
    addToCart,
    removeFromCart,
    navigate,
    onDelete,
  } = context;

  const isOwner = Boolean(
    user?.username &&
      cardData.ownerUsername &&
      user.username === cardData.ownerUsername,
  );
  const listingKey = String(cardData.id);
  const cartItemId = cartItemByListingId.get(listingKey);
  const inCart = cartItemByListingId.has(listingKey);

  const actions = {};

  if (!isOwner && navigate) {
    actions.primaryAction = {
      label: "Trade",
      onClick: () => navigate(`/messaging/${cardData.id}`),
    };
  } else if (isOwner && navigate) {
    actions.primaryAction = {
      label: "Edit",
      onClick: () => {
        if (!user) return;
        navigate(`/edit/${cardData.id}`);
      },
    };
  }

  if (!isOwner && addToCart && removeFromCart) {
    actions.secondaryAction = {
      label: inCart ? "Remove" : "Add",
      onClick: () =>
        inCart && cartItemId
          ? removeFromCart(cartItemId)
          : addToCart(cardData.id, 1),
    };
  } else if (isOwner && onDelete) {
    actions.secondaryAction = {
      label: "Delete",
      onClick: () => onDelete(cardData.id),
    };
  }

  return { ...cardData, ...actions };
}
