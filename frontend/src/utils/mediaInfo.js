export const MEDIA_FORMAT_OPTIONS = [
  { value: "VINYL", label: "Vinyl" },
  { value: "CASSETTE", label: "Cassette" },
  { value: "CD", label: "CD" },
  { value: "EIGHT_TRACK", label: "8-Track" },
  { value: "OTHER", label: "Other" },
];

export const VINYL_SUBTYPE_OPTIONS = [
  { value: "LP", label: "LP" },
  { value: "EP", label: "EP" },
  { value: "SINGLE", label: "Single" },
  { value: "MAXI_SINGLE", label: "Maxi Single" },
];

export const VINYL_SPEED_OPTIONS = [
  { value: 33, label: "33 RPM" },
  { value: 45, label: "45 RPM" },
  { value: 78, label: "78 RPM" },
];

export const VINYL_SIZE_OPTIONS = [
  { value: '7"', label: '7"' },
  { value: '10"', label: '10"' },
  { value: '12"', label: '12"' },
];

export function emptyMediaInfo() {
  return {
    format: "",
    vinylSubtype: "",
    speedRpm: "",
    vinylSize: "",
    discCount: 1,
    colored: false,
    pictureDisc: false,
    sourceFormatRaw: "",
  };
}

export function normalizeMediaInfoFromApi(mediaInfo) {
  if (!mediaInfo?.format) {
    return emptyMediaInfo();
  }

  return {
    format: mediaInfo.format ?? "",
    vinylSubtype: mediaInfo.vinylSubtype ?? "",
    speedRpm: mediaInfo.speedRpm ?? "",
    vinylSize: mediaInfo.vinylSize ?? "",
    discCount: mediaInfo.discCount ?? 1,
    colored: mediaInfo.colored === true,
    pictureDisc: mediaInfo.pictureDisc === true,
    sourceFormatRaw: mediaInfo.sourceFormatRaw ?? "",
  };
}

export function sanitizeMediaInfoForSubmit(mediaInfo) {
  if (!mediaInfo?.format) {
    return null;
  }

  const next = {
    format: mediaInfo.format,
    sourceFormatRaw: mediaInfo.sourceFormatRaw || null,
  };

  if (mediaInfo.format === "VINYL") {
    next.vinylSubtype = mediaInfo.vinylSubtype || null;
    next.speedRpm = mediaInfo.speedRpm ? Number(mediaInfo.speedRpm) : null;
    next.vinylSize = mediaInfo.vinylSize || null;
    next.discCount = mediaInfo.discCount ? Number(mediaInfo.discCount) : 1;
    next.colored = mediaInfo.colored === true ? true : null;
    next.pictureDisc = mediaInfo.pictureDisc === true ? true : null;
    return next;
  }

  if (mediaInfo.format === "CD") {
    next.discCount = mediaInfo.discCount ? Number(mediaInfo.discCount) : 1;
    return next;
  }

  return next;
}

export function getListingFormatLabel(listing) {
  if (!listing) {
    return "";
  }

  if (listing.mediaInfo?.format) {
    const fromMediaInfo = formatMediaInfoLabel(
      normalizeMediaInfoFromApi(listing.mediaInfo),
    );
    if (fromMediaInfo) {
      return fromMediaInfo;
    }
  }

  const fallback = listing.format?.trim();
  if (!fallback) {
    return "";
  }

  const enumLabel = MEDIA_FORMAT_OPTIONS.find(
    (option) => option.value === fallback,
  )?.label;
  return enumLabel ?? fallback;
}

export function getListingLabelName(listing) {
  if (!listing) {
    return "";
  }

  return (listing.labelName ?? listing.label ?? "").trim();
}

export function formatMediaInfoLabel(mediaInfo) {
  if (!mediaInfo?.format) {
    return "";
  }

  const discPrefix =
    mediaInfo.discCount && Number(mediaInfo.discCount) > 1
      ? `${mediaInfo.discCount}x`
      : "";

  switch (mediaInfo.format) {
    case "VINYL": {
      const subtypeLabels = {
        LP: "LP",
        EP: "EP",
        SINGLE: "Single",
        MAXI_SINGLE: "Maxi Single",
      };
      const subtype = subtypeLabels[mediaInfo.vinylSubtype] || "Vinyl";
      let label = `${discPrefix}${subtype}`;
      if (mediaInfo.vinylSize) {
        label += ` ${mediaInfo.vinylSize}`;
      }
      if (mediaInfo.speedRpm) {
        label += ` / ${mediaInfo.speedRpm}`;
      }
      if (mediaInfo.colored) {
        label += " (Colored)";
      }
      if (mediaInfo.pictureDisc) {
        label += " (Picture Disc)";
      }
      return label.trim();
    }
    case "CD":
      return `${discPrefix}CD`.trim();
    case "CASSETTE":
      return "Cassette";
    case "EIGHT_TRACK":
      return "8-Track";
    case "OTHER":
      return mediaInfo.sourceFormatRaw || "Other";
    default:
      return mediaInfo.format;
  }
}

const DISC_COUNT = /(\d+)\s*[x×]/i;
const SIZE = /(\d+)\s*"/;
const RPM = /(33|45|78)\s*(?:rpm)?/i;

export function parseMusicBrainzFormat(raw) {
  if (!raw?.trim()) {
    return emptyMediaInfo();
  }

  const normalized = raw.trim();
  const upper = normalized.toUpperCase();

  if (upper.includes("CD")) {
    return {
      ...emptyMediaInfo(),
      format: "CD",
      discCount: parseDiscCount(normalized, 1),
      sourceFormatRaw: normalized,
    };
  }

  if (upper.includes("CASSETTE")) {
    return {
      ...emptyMediaInfo(),
      format: "CASSETTE",
      sourceFormatRaw: normalized,
    };
  }

  if (upper.includes("8-TRACK") || upper.includes("8 TRACK") || upper.includes("8TRACK")) {
    return {
      ...emptyMediaInfo(),
      format: "EIGHT_TRACK",
      sourceFormatRaw: normalized,
    };
  }

  if (upper.includes("VINYL") || upper.includes("LP") || upper.includes("EP") || SIZE.test(normalized)) {
    let vinylSubtype = "LP";
    if (upper.includes("MAXI")) {
      vinylSubtype = "MAXI_SINGLE";
    } else if (/\bEP\b/.test(upper)) {
      vinylSubtype = "EP";
    } else if (upper.includes("SINGLE") || normalized.includes('7"')) {
      vinylSubtype = "SINGLE";
    }

    let speedRpm = parseRpm(normalized);
    if (!speedRpm && vinylSubtype === "SINGLE") speedRpm = 45;
    if (!speedRpm && (vinylSubtype === "LP" || vinylSubtype === "EP")) speedRpm = 33;

    let vinylSize = parseSize(normalized);
    if (!vinylSize && vinylSubtype === "SINGLE") vinylSize = '7"';
    if (!vinylSize && (vinylSubtype === "LP" || vinylSubtype === "EP")) vinylSize = '12"';

    return {
      ...emptyMediaInfo(),
      format: "VINYL",
      vinylSubtype,
      speedRpm,
      vinylSize,
      discCount: parseDiscCount(normalized, 1),
      colored: upper.includes("COLOU") || upper.includes("COLOR"),
      pictureDisc: upper.includes("PICTURE DISC"),
      sourceFormatRaw: normalized,
    };
  }

  return {
    ...emptyMediaInfo(),
    format: "OTHER",
    sourceFormatRaw: normalized,
  };
}

function parseDiscCount(raw, defaultValue) {
  const match = raw.match(DISC_COUNT);
  return match ? Number.parseInt(match[1], 10) : defaultValue;
}

function parseRpm(raw) {
  const match = raw.match(RPM);
  return match ? Number.parseInt(match[1], 10) : "";
}

function parseSize(raw) {
  const match = raw.match(SIZE);
  return match ? `${match[1]}"` : "";
}

export function mediaInfoFromRelease(release) {
  if (release?.suggestedMediaInfo?.format) {
    return normalizeMediaInfoFromApi(release.suggestedMediaInfo);
  }

  const raw = release?.media?.[0]?.format;
  return parseMusicBrainzFormat(raw);
}
