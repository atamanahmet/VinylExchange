import { useEffect, useState } from "react";

import axios from "../api/axiosInstance";

/**
 * Reference lists are static per language, so the in-flight promise is shared
 * across callers and the result is kept for the session. Without this every
 * mount of the filter panel refetches countries and genres.
 */
const pending = new Map();
const resolved = new Map();

const EMPTY = [];

function initialState(key) {
  const cached = resolved.get(key);
  return cached
    ? { options: cached, loading: false, error: null }
    : { options: EMPTY, loading: true, error: null };
}

function loadOptions(key, path, params) {
  const inFlight = pending.get(key);
  if (inFlight) return inFlight;

  const request = axios
    .get(path, { params })
    .then((res) => {
      const options = Array.isArray(res.data) ? res.data : EMPTY;
      resolved.set(key, options);
      return options;
    })
    .catch((error) => {
      pending.delete(key);
      throw error;
    });

  pending.set(key, request);
  return request;
}

export function useReferenceOptions(path, params) {
  const paramsKey = JSON.stringify(params);
  const key = `${path}?${paramsKey}`;

  const [state, setState] = useState(() => initialState(key));
  const [trackedKey, setTrackedKey] = useState(key);

  // Reset during render instead of in an effect so a cached list never flashes loading.
  if (trackedKey !== key) {
    setTrackedKey(key);
    setState(initialState(key));
  }

  useEffect(() => {
    const currentKey = `${path}?${paramsKey}`;
    if (resolved.has(currentKey)) return undefined;

    let cancelled = false;

    loadOptions(currentKey, path, JSON.parse(paramsKey))
      .then((options) => {
        if (!cancelled) setState({ options, loading: false, error: null });
      })
      .catch((error) => {
        if (!cancelled) setState({ options: EMPTY, loading: false, error });
      });

    return () => {
      cancelled = true;
    };
  }, [path, paramsKey]);

  return state;
}
