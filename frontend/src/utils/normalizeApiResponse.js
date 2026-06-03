export function normalizeApiResponse(res) {
  const payload = res?.data ?? res;

  if (!payload) {
    return { data: [], pagination: null };
  }
  //page
  if (payload && Array.isArray(payload.content)) {
    return {
      data: payload.content,
      pagination: {
        page: payload.number,
        size: payload.size,
        totalElements: payload.totalElements,
        totalPages: payload.totalPages,
        hasNext: !payload.last,
        hasPrev: !payload.first,
      },
    };
  }

  //array
  if (Array.isArray(payload)) {
    return {
      data: payload,
      pagination: null,
    };
  }

  //single data
  return {
    data: payload ? [payload] : [],
    pagination: null,
  };
}
