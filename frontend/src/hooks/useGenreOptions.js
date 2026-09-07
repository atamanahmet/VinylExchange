import { useEffect, useState } from "react";

import axios from "../api/axiosInstance";

export function useGenreOptions(lang = "en", includeLocal = true) {
  const [options, setOptions] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    let cancelled = false;

    setLoading(true);
    setError(null);

    axios
      .get("/api/reference/genres", { params: { lang, includeLocal } })
      .then((res) => {
        if (!cancelled) {
          setOptions(Array.isArray(res.data) ? res.data : []);
        }
      })
      .catch((err) => {
        if (!cancelled) {
          setError(err);
          setOptions([]);
        }
      })
      .finally(() => {
        if (!cancelled) {
          setLoading(false);
        }
      });

    return () => {
      cancelled = true;
    };
  }, [lang, includeLocal]);

  return { options, loading, error };
}
