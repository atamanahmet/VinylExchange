import { useReferenceOptions } from "./useReferenceOptions";

export function useCountryOptions(lang = "en") {
  return useReferenceOptions("/api/reference/countries", { lang });
}
