export const FORMAT_OPTIONS = [
  "VINYL",
  "CASSETTE",
  "CD",
  "EIGHT_TRACK",
  "OTHER",
];

export const FORMAT_LABELS = {
  VINYL: "Vinyl",
  CASSETTE: "Cassette",
  CD: "CD",
  EIGHT_TRACK: "8-Track",
  OTHER: "Other",
};

export const RPM_OPTIONS = [33, 45, 78];

export const VINYL_SUBTYPE_OPTIONS = [
  { value: "LP", label: "LP" },
  { value: "EP", label: "EP" },
  { value: "SINGLE", label: "Single" },
  { value: "MAXI_SINGLE", label: "Maxi-Single" },
];

export const CONDITION_OPTIONS = [
  { value: "M", label: "Mint" },
  { value: "NM", label: "Near Mint" },
  { value: "E", label: "Excellent" },
  { value: "VG+", label: "Very Good+" },
  { value: "VG", label: "Very Good" },
  { value: "G", label: "Good" },
  { value: "P", label: "Poor" },
];

export const SLIDER_BOUNDS = {
  minPrice: 0,
  maxPrice: 5000,
  minYear: 1900,
  maxYear: new Date().getFullYear(),
};

export const DEFAULT_FILTERS = {
  formats: [],
  speedRpm: [],
  vinylSubtype: [],
  conditions: [],
  countries: [],
  genreIds: [],
  priceRange: [SLIDER_BOUNDS.minPrice, SLIDER_BOUNDS.maxPrice],
  yearRange: [SLIDER_BOUNDS.minYear, SLIDER_BOUNDS.maxYear],
  tradeableOnly: false,
};

/** Spring Pageable sort values — unfiltered browse uses Redis cache per sort key. */
export const DEFAULT_LISTING_SORT = "createdAt,desc";

export const LISTING_SORT_OPTIONS = [
  { value: "createdAt,desc", label: "Latest" },
  { value: "year,asc", label: "Year: old to new" },
  { value: "year,desc", label: "Year: new to old" },
  { value: "priceKurus,asc", label: "Price: low to high" },
  { value: "priceKurus,desc", label: "Price: high to low" },
];

export function resetFilters() {
  return {
    ...DEFAULT_FILTERS,
    priceRange: [...DEFAULT_FILTERS.priceRange],
    yearRange: [...DEFAULT_FILTERS.yearRange],
  };
}

export function countActiveFilters(filters) {
  let count = 0;
  if (filters.formats.length) count += 1;
  if (filters.speedRpm?.length) count += 1;
  if (filters.vinylSubtype?.length) count += 1;
  if (filters.conditions.length) count += 1;
  if (filters.countries?.length) count += 1;
  if (filters.genreIds?.length) count += 1;
  if (filters.tradeableOnly) count += 1;
  if (
    filters.priceRange &&
    (filters.priceRange[0] > SLIDER_BOUNDS.minPrice ||
      filters.priceRange[1] < SLIDER_BOUNDS.maxPrice)
  ) {
    count += 1;
  }
  if (
    filters.yearRange &&
    (filters.yearRange[0] > SLIDER_BOUNDS.minYear ||
      filters.yearRange[1] < SLIDER_BOUNDS.maxYear)
  ) {
    count += 1;
  }
  return count;
}

/** Active filter count for a single panel section, drives the section badges. */
export function countSectionFilters(filters, section) {
  switch (section) {
    case "format":
      return (
        (filters.formats?.length ? 1 : 0) +
        (filters.speedRpm?.length ? 1 : 0) +
        (filters.vinylSubtype?.length ? 1 : 0)
      );
    case "condition":
      return filters.conditions?.length ?? 0;
    case "country":
      return filters.countries?.length ?? 0;
    case "genre":
      return filters.genreIds?.length ?? 0;
    case "price":
      return filters.priceRange &&
        (filters.priceRange[0] > SLIDER_BOUNDS.minPrice ||
          filters.priceRange[1] < SLIDER_BOUNDS.maxPrice)
        ? 1
        : 0;
    case "year":
      return filters.yearRange &&
        (filters.yearRange[0] > SLIDER_BOUNDS.minYear ||
          filters.yearRange[1] < SLIDER_BOUNDS.maxYear)
        ? 1
        : 0;
    case "trade":
      return filters.tradeableOnly ? 1 : 0;
    default:
      return 0;
  }
}

/** True when the draft filters differ from what is currently applied. */
export function filtersEqual(a, b) {
  if (a === b) return true;
  if (!a || !b) return false;

  const sameList = (left = [], right = []) =>
    left.length === right.length &&
    left.every((value, index) => value === right[index]);

  return (
    sameList(a.formats, b.formats) &&
    sameList(a.speedRpm, b.speedRpm) &&
    sameList(a.vinylSubtype, b.vinylSubtype) &&
    sameList(a.conditions, b.conditions) &&
    sameList(a.countries, b.countries) &&
    sameList(a.genreIds, b.genreIds) &&
    sameList(a.priceRange, b.priceRange) &&
    sameList(a.yearRange, b.yearRange) &&
    Boolean(a.tradeableOnly) === Boolean(b.tradeableOnly)
  );
}

/**
 * Flattens active filters into removable chips.
 * Option lists supply human labels for id-based filters.
 */
export function describeActiveFilters(
  filters,
  { countryOptions = [], genreOptions = [] } = {},
) {
  const chips = [];
  const labelFor = (options, value, matchKey) =>
    options.find((option) => option[matchKey] === value)?.label ?? value;

  filters.formats?.forEach((format) => {
    chips.push({
      key: `formats:${format}`,
      label: FORMAT_LABELS[format] ?? format,
      remove: (current) => ({
        ...current,
        formats: current.formats.filter((item) => item !== format),
        ...(format === "VINYL" ? { speedRpm: [], vinylSubtype: [] } : {}),
      }),
    });
  });

  filters.speedRpm?.forEach((rpm) => {
    chips.push({
      key: `speedRpm:${rpm}`,
      label: `${rpm} RPM`,
      remove: (current) => ({
        ...current,
        speedRpm: current.speedRpm.filter((item) => item !== rpm),
      }),
    });
  });

  filters.vinylSubtype?.forEach((value) => {
    chips.push({
      key: `vinylSubtype:${value}`,
      label: labelFor(VINYL_SUBTYPE_OPTIONS, value, "value"),
      remove: (current) => ({
        ...current,
        vinylSubtype: current.vinylSubtype.filter((item) => item !== value),
      }),
    });
  });

  filters.conditions?.forEach((value) => {
    chips.push({
      key: `conditions:${value}`,
      label: labelFor(CONDITION_OPTIONS, value, "value"),
      remove: (current) => ({
        ...current,
        conditions: current.conditions.filter((item) => item !== value),
      }),
    });
  });

  filters.countries?.forEach((value) => {
    chips.push({
      key: `countries:${value}`,
      label: labelFor(countryOptions, value, "value"),
      remove: (current) => ({
        ...current,
        countries: current.countries.filter((item) => item !== value),
      }),
    });
  });

  filters.genreIds?.forEach((value) => {
    chips.push({
      key: `genreIds:${value}`,
      label: labelFor(genreOptions, value, "id"),
      remove: (current) => ({
        ...current,
        genreIds: current.genreIds.filter((item) => item !== value),
      }),
    });
  });

  if (countSectionFilters(filters, "price")) {
    chips.push({
      key: "priceRange",
      label: `${filters.priceRange[0]} - ${filters.priceRange[1]} TL`,
      remove: (current) => ({
        ...current,
        priceRange: [...DEFAULT_FILTERS.priceRange],
      }),
    });
  }

  if (countSectionFilters(filters, "year")) {
    chips.push({
      key: "yearRange",
      label: `${filters.yearRange[0]} - ${filters.yearRange[1]}`,
      remove: (current) => ({
        ...current,
        yearRange: [...DEFAULT_FILTERS.yearRange],
      }),
    });
  }

  if (filters.tradeableOnly) {
    chips.push({
      key: "tradeableOnly",
      label: "Tradeable only",
      remove: (current) => ({ ...current, tradeableOnly: false }),
    });
  }

  return chips;
}

export function buildListingFilterParams(filters, { page, size, sort, ownerUsername } = {}) {
  const params = {};

  if (filters.formats.length) params.format = filters.formats;
  if (filters.speedRpm?.length) params.speedRpm = filters.speedRpm;
  if (filters.vinylSubtype?.length) params.vinylSubtype = filters.vinylSubtype;
  if (filters.conditions.length) params.condition = filters.conditions;
  if (filters.countries?.length) params.country = filters.countries;
  if (filters.genreIds?.length) params.genreIds = filters.genreIds;

  const [minPrice, maxPrice] = filters.priceRange;
  if (minPrice > SLIDER_BOUNDS.minPrice) {
    params.priceFromKurus = Math.round(minPrice * 100);
  }
  if (maxPrice < SLIDER_BOUNDS.maxPrice) {
    params.priceToKurus = Math.round(maxPrice * 100);
  }

  const [minYear, maxYear] = filters.yearRange;
  if (minYear > SLIDER_BOUNDS.minYear) {
    params.yearFrom = minYear;
  }
  if (maxYear < SLIDER_BOUNDS.maxYear) {
    params.yearTo = maxYear;
  }

  if (filters.tradeableOnly) {
    params.tradeable = true;
  }

  if (page != null) params.page = page;
  if (size != null) params.size = size;
  if (sort) params.sort = sort;
  if (ownerUsername) params.ownerUsername = ownerUsername;

  return params;
}
