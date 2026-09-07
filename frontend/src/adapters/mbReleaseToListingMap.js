import { formatMediaInfoLabel, mediaInfoFromRelease } from "../utils/mediaInfo";

export function mbReleaseToListingMap(release, onSelect) {
  const mediaInfo = mediaInfoFromRelease(release);

  const item = {
    id: release.id,
    title: release.title,
    artist: release.artistCredit?.[0]?.name || "Unknown artist",
    format: formatMediaInfoLabel(mediaInfo) || "Unknown format",
    mediaInfo,
    externalCoverUrl: release.externalCoverUrl,
    country: release.country || "",
    barcode: release.barcode || "Unknown Barcode",
    year: release.year || "Unknown Date",
    label: release.labelInfo?.[0]?.label?.name || "Unknown Label",
    disableLink: true,
    textCenter: true,
  };

  if (onSelect) {
    item.primaryAction = {
      label: "Use this release",
      onClick: () => onSelect(item),
    };
  }

  return item;
}
