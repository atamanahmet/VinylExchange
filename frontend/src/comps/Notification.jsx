export default function Notification({
  notification,
  navigate,
  closeMenu,
  markAsRead,
}) {
  return (
    <li className=" border my-2 rounded hover:bg-gray-500">
      <button
        onClick={() => {
          navigate(`/listing/${notification.relatedListingId}`);
          closeMenu(false);
          markAsRead(notification.id);
        }}
        className={`w-full-white text-left block pl-2 py-1 text-sm font-light hover:text-heading ${!notification.read ? "bg-amber-600 hover:bg-amber-700" : ""}`}
      >
        {notification.message}
      </button>
    </li>
  );
}
