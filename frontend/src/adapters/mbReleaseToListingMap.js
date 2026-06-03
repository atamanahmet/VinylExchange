export function mbReleaseToListingMap(release) {
  return {
    id: release.id,
    title: release.title,
    artist: release.artistCredit?.[0]?.name || "Unknown artist",
    format: release.media?.[0]?.format || "Unknown format",
    externalCoverUrl: release.externalCoverUrl,
    country: release.country,
    barcode: release.barcode,
    year: release.year || "Unknown Date",
    label: release.labelInfo?.[0].label?.name || "Unknown Label",
  };
}
