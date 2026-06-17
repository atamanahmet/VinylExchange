export const FORMAT_OPTIONS = ["33", "45", "EP", "Cassette", "CD", "Other"];

export const CONDITION_OPTIONS = [
  { value: "M", label: "Mint" },
  { value: "NM", label: "Near Mint" },
  { value: "E", label: "Excellent" },
  { value: "VG+", label: "Very Good+" },
  { value: "VG", label: "Very Good" },
  { value: "G", label: "Good" },
  { value: "P", label: "Poor" },
];

export const DEFAULT_FILTERS = {
  formats: [],
  conditions: [],
  countries: [],
  priceRange: null,
  yearRange: null,
  tradeableOnly: false,
};

export function getCountryOptions(listings = []) {
  const countries = new Set();

  for (const listing of listings) {
    const country = listing.country?.toString().trim();
    if (country) {
      countries.add(country);
    }
  }

  return [...countries].sort((a, b) => a.localeCompare(b));
}

export function getListingBounds(listings = []) {
  if (!listings.length) {
    return {
      minPrice: 0,
      maxPrice: 1000,
      minYear: 1950,
      maxYear: new Date().getFullYear(),
      countries: [],
    };
  }

  let minPrice = Infinity;
  let maxPrice = 0;
  let minYear = Infinity;
  let maxYear = 0;

  for (const listing of listings) {
    const price = Number(listing.price) || 0;
    minPrice = Math.min(minPrice, price);
    maxPrice = Math.max(maxPrice, price);
    minYear = Math.min(minYear, listing.year || minYear);
    maxYear = Math.max(maxYear, listing.year || maxYear);
  }

  return {
    minPrice: Math.floor(minPrice),
    maxPrice: Math.ceil(maxPrice),
    minYear: minYear === Infinity ? 1950 : minYear,
    maxYear: maxYear === 0 ? new Date().getFullYear() : maxYear,
    countries: getCountryOptions(listings),
  };
}

export function resetFilters(bounds) {
  return {
    ...DEFAULT_FILTERS,
    priceRange: [bounds.minPrice, bounds.maxPrice],
    yearRange: [bounds.minYear, bounds.maxYear],
  };
}

export function createInitialFilters(listings = []) {
  const bounds = getListingBounds(listings);
  return resetFilters(bounds);
}

export function countActiveFilters(filters, bounds) {
  let count = 0;
  if (filters.formats.length) count += 1;
  if (filters.conditions.length) count += 1;
  if (filters.countries?.length) count += 1;
  if (filters.tradeableOnly) count += 1;
  if (
    filters.priceRange &&
    (filters.priceRange[0] > bounds.minPrice ||
      filters.priceRange[1] < bounds.maxPrice)
  ) {
    count += 1;
  }
  if (
    filters.yearRange &&
    (filters.yearRange[0] > bounds.minYear ||
      filters.yearRange[1] < bounds.maxYear)
  ) {
    count += 1;
  }
  return count;
}

export function applyListingFilters(listings = [], filters, bounds) {
  if (!listings.length) return [];

  const [minPrice, maxPrice] = filters.priceRange ?? [
    bounds.minPrice,
    bounds.maxPrice,
  ];
  const [minYear, maxYear] = filters.yearRange ?? [
    bounds.minYear,
    bounds.maxYear,
  ];

  return listings.filter((listing) => {
    const price = Number(listing.price) || 0;
    if (price < minPrice || price > maxPrice) return false;
    if (listing.year < minYear || listing.year > maxYear) return false;
    if (filters.tradeableOnly && !listing.tradeable) return false;
    if (
      filters.formats.length &&
      !filters.formats.includes(listing.format)
    ) {
      return false;
    }
    if (
      filters.conditions.length &&
      !filters.conditions.includes(listing.condition)
    ) {
      return false;
    }
    if (filters.countries?.length) {
      const country = listing.country?.toString().trim();
      if (!country || !filters.countries.includes(country)) {
        return false;
      }
    }
    return true;
  });
}
