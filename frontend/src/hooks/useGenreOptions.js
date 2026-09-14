import { useReferenceOptions } from "./useReferenceOptions";

export function useGenreOptions(lang = "en", includeLocal = true) {
  return useReferenceOptions("/api/reference/genres", { lang, includeLocal });
}
