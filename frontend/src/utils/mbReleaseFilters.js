export const DEFAULT_MB_FILTERS = {
  artists: [],
  formats: [],
  countries: [],
  labels: [],
  yearRange: null,
  hasBarcode: false,
};

function getReleaseArtist(release) {
  return release.artistCredit?.[0]?.name?.trim() || "Unknown artist";
}

function getReleaseFormat(release) {
  return release.media?.[0]?.format?.trim() || "Unknown format";
}

function getReleaseLabel(release) {
  return release.labelInfo?.[0]?.label?.name?.trim() || "Unknown Label";
}

function getReleaseCountry(release) {
  return release.country?.toString().trim() || "";
}

function getReleaseYear(release) {
  const year = release.year;
  if (typeof year === "number" && !Number.isNaN(year)) {
    return year;
  }
  return null;
}

export function getMbFilterBounds(releases = []) {
  const artists = new Set();
  const formats = new Set();
  const countries = new Set();
  const labels = new Set();
  let minYear = Infinity;
  let maxYear = -Infinity;

  for (const release of releases) {
    artists.add(getReleaseArtist(release));
    formats.add(getReleaseFormat(release));
    const country = getReleaseCountry(release);
    if (country) {
      countries.add(country);
    }
    labels.add(getReleaseLabel(release));
    const year = getReleaseYear(release);
    if (year != null) {
      minYear = Math.min(minYear, year);
      maxYear = Math.max(maxYear, year);
    }
  }

  const currentYear = new Date().getFullYear();

  return {
    artists: [...artists].sort((a, b) => a.localeCompare(b)),
    formats: [...formats].sort((a, b) => a.localeCompare(b)),
    countries: [...countries].sort((a, b) => a.localeCompare(b)),
    labels: [...labels].sort((a, b) => a.localeCompare(b)),
    minYear: minYear === Infinity ? 1950 : minYear,
    maxYear: maxYear === -Infinity ? currentYear : maxYear,
  };
}

export function resetMbFilters(bounds) {
  return {
    ...DEFAULT_MB_FILTERS,
    yearRange: [bounds.minYear, bounds.maxYear],
  };
}

export function createInitialMbFilters(releases = []) {
  return resetMbFilters(getMbFilterBounds(releases));
}

function isFullYearRange(yearRange, bounds) {
  if (!yearRange || !bounds) {
    return true;
  }

  return yearRange[0] === bounds.minYear && yearRange[1] === bounds.maxYear;
}

export function mergeMbFiltersWithBounds(filters, bounds, previousBounds = null) {
  const next = { ...filters };

  if (isFullYearRange(filters.yearRange, previousBounds)) {
    next.yearRange = [bounds.minYear, bounds.maxYear];
  } else if (filters.yearRange) {
    next.yearRange = [
      Math.max(bounds.minYear, filters.yearRange[0]),
      Math.min(bounds.maxYear, filters.yearRange[1]),
    ];
  } else {
    next.yearRange = [bounds.minYear, bounds.maxYear];
  }

  return next;
}

export function countActiveMbFilters(filters, bounds) {
  let count = 0;
  if (filters.artists?.length) count += 1;
  if (filters.formats?.length) count += 1;
  if (filters.countries?.length) count += 1;
  if (filters.labels?.length) count += 1;
  if (filters.hasBarcode) count += 1;
  if (
    filters.yearRange &&
    (filters.yearRange[0] > bounds.minYear ||
      filters.yearRange[1] < bounds.maxYear)
  ) {
    count += 1;
  }
  return count;
}

export function applyMbReleaseFilters(releases = [], filters, bounds) {
  if (!releases.length) {
    return [];
  }

  const [minYear, maxYear] = filters.yearRange ?? [
    bounds.minYear,
    bounds.maxYear,
  ];
  const yearFilterActive =
    minYear > bounds.minYear || maxYear < bounds.maxYear;

  return releases.filter((release) => {
    const artist = getReleaseArtist(release);
    if (filters.artists?.length && !filters.artists.includes(artist)) {
      return false;
    }

    const format = getReleaseFormat(release);
    if (filters.formats?.length && !filters.formats.includes(format)) {
      return false;
    }

    const country = getReleaseCountry(release);
    if (filters.countries?.length) {
      if (!country || !filters.countries.includes(country)) {
        return false;
      }
    }

    const label = getReleaseLabel(release);
    if (filters.labels?.length && !filters.labels.includes(label)) {
      return false;
    }

    if (filters.hasBarcode) {
      const barcode = release.barcode?.toString().trim();
      if (!barcode) {
        return false;
      }
    }

    if (yearFilterActive) {
      const year = getReleaseYear(release);
      if (year == null || year < minYear || year > maxYear) {
        return false;
      }
    }

    return true;
  });
}
