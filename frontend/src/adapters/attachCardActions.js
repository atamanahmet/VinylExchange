export function attachCardActions(cardData, context) {
  const {
    user,
    cartItemByListingId = new Map(),
    addToCart,
    removeFromCart,
    navigate,
    startConversation,
    onDelete,
  } = context;

  const isOwner = user?.username === cardData.ownerUsername;
  const listingKey = String(cardData.id);
  const cartItemId = cartItemByListingId.get(listingKey);
  const inCart = cartItemByListingId.has(listingKey);

  const actions = {};

  if (!isOwner && startConversation) {
    actions.primaryAction = {
      label: "Trade",
      onClick: () => startConversation(cardData.id),
    };
  } else if (isOwner && navigate) {
    actions.primaryAction = {
      label: "Edit",
      onClick: () => navigate(`/edit/${cardData.id}`),
    };
  }

  if (!isOwner && addToCart && removeFromCart) {
    actions.secondaryAction = {
      label: inCart ? "Remove" : "Add to cart",
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
