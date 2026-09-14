import { useNavigate } from "react-router-dom";

import { cn } from "@/lib/utils";
import { useSearchStore } from "@/stores/searchStore";

/**
 * Artist / band name that searches the marketplace for that artist.
 * Search state lives in the store and MainPage renders it, matching the navbar.
 * Stops propagation because cards and list rows are themselves clickable.
 */
export default function ArtistLink({ artist, className }) {
  const navigate = useNavigate();
  const startListingSearch = useSearchStore((state) => state.startListingSearch);

  const name = artist?.toString().trim();
  if (!name) {
    return null;
  }

  const handleClick = (event) => {
    event.preventDefault();
    event.stopPropagation();
    startListingSearch(name);
    navigate("/");
  };

  return (
    <button
      type="button"
      onClick={handleClick}
      title={`Search listings by ${name}`}
      className={cn(
        "max-w-full cursor-pointer text-left underline-offset-4 transition-colors hover:text-brand-fg hover:underline",
        className,
      )}
    >
      {name}
    </button>
  );
}
