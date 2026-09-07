/**
 * Extract a user-facing message from an axios error response.
 * Backend may return a plain string or a field -> message map (validation).
 */
export function getApiErrorMessage(error, fallback = "Something went wrong") {
  const data = error?.response?.data;

  if (data == null || data === "") {
    return error?.message || fallback;
  }

  if (typeof data === "string") {
    return data;
  }

  if (typeof data === "object") {
    const messages = Object.values(data).filter(
      (value) => typeof value === "string" && value.trim(),
    );
    if (messages.length > 0) {
      return messages.join(". ");
    }

    if (typeof data.message === "string" && data.message.trim()) {
      return data.message;
    }
  }

  return fallback;
}

export function isShippingAddressRequiredError(error) {
  if (error?.response?.status !== 409) {
    return false;
  }
  return /shipping address/i.test(getApiErrorMessage(error, ""));
}
