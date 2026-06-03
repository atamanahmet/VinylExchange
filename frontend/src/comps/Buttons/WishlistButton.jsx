import { Heart } from "lucide-react";
import { useWishlistStore } from "../store/wishlistStore";

export default function WishlistButton({ release }) {
  const { isInWishlist, toggleWishlist, isLoading } = useWishlistStore();

  const inWishlist = isInWishlist(release.title, release.artist);

  const handleToggle = async (e) => {
    e.preventDefault();
    e.stopPropagation();

    try {
      const result = await toggleWishlist(release);
      if (result === null) {
        alert("Failed to update wishlist. Please try again.");
      }
    } catch (error) {
      console.log("Error toggling wishlist:", error);
      alert("Failed to update wishlist. Please try again.");
    }
  };

  return (
    <button
      onClick={handleToggle}
      disabled={isLoading}
      className={`p-2 rounded-full transition-all duration-200 ${
        inWishlist
          ? "bg-red-500 text-white hover:bg-red-600"
          : "bg-gray-200 text-gray-600 hover:bg-gray-300"
      } disabled:opacity-50 disabled:cursor-not-allowed ${className}`}
      aria-label={inWishlist ? "Remove from wishlist" : "Add to wishlist"}
      title={inWishlist ? "Remove from wishlist" : "Add to wishlist"}
    >
      <Heart
        className={`w-5 h-5 transition-transform ${
          inWishlist ? "fill-current scale-110" : ""
        } ${isLoading ? "animate-pulse" : ""}`}
      />
    </button>
  );
}
