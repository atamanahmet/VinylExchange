export default function Notification({
  notification,
  navigate,
  closeMenu,
  markAsRead,
}) {
  return (
    <li className=" border my-2 rounded hover:bg-surface-3">
      <button
        onClick={() => {
          navigate(`/listing/${notification.relatedListingId}`);
          closeMenu(false);
          markAsRead(notification.id);
        }}
        className={`w-full text-left block pl-2 py-1 text-sm font-light ${!notification.read ? "bg-brand text-on-brand hover:bg-brand-hover" : "text-on-surface"}`}
      >
        {notification.message}
      </button>
    </li>
  );
}
