/**
 * Builds page index list for numbered pagination.
 * currentPage is 0-based. Items are page numbers or "ellipsis".
 */
export function buildPaginationRange(currentPage, totalPages, siblingCount = 1) {
  if (totalPages <= 1) {
    return [];
  }

  const pages = new Set([0, totalPages - 1, currentPage]);

  for (let offset = 1; offset <= siblingCount; offset += 1) {
    if (currentPage - offset >= 0) {
      pages.add(currentPage - offset);
    }
    if (currentPage + offset < totalPages) {
      pages.add(currentPage + offset);
    }
  }

  const sorted = [...pages].sort((a, b) => a - b);
  const range = [];

  for (let index = 0; index < sorted.length; index += 1) {
    const page = sorted[index];
    if (index > 0 && page - sorted[index - 1] > 1) {
      range.push("ellipsis");
    }
    range.push(page);
  }

  return range;
}
