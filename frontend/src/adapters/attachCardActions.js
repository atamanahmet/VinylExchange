export function attachCardActions(cardData, context) {
  const {
    user,
    cartIds = new Set(),
    addToCart,
    navigate,
    startConversation,
    onDelete,
  } = context;

  const isOwner = user?.username === cardData.ownerUsername;
  const inCart = cartIds.has(cardData.id);

  const actions = {};

  // Primary button
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

  // Secondary button
  if (!isOwner && addToCart) {
    actions.secondaryAction = {
      label: inCart ? "In Cart" : "Add to Cart",
      onClick: () => addToCart(cardData.id),
    };
  } else if (isOwner && onDelete) {
    actions.secondaryAction = {
      label: "Delete",
      onClick: () => onDelete(cardData.id),
    };
  }

  return { ...cardData, ...actions };
}
