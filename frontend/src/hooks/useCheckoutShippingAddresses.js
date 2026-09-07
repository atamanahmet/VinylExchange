import { useEffect } from "react";
import { useAddressStore } from "../stores/addressStore";

export function useCheckoutShippingAddresses() {
  const shippingAddresses = useAddressStore((state) => state.addresses);
  const isFetching = useAddressStore((state) => state.isFetching);
  const isSaving = useAddressStore((state) => state.isSaving);
  const fetchAddresses = useAddressStore((state) => state.fetchAddresses);
  const createAddress = useAddressStore((state) => state.createAddress);

  useEffect(() => {
    fetchAddresses("SHIPPING");
  }, [fetchAddresses]);

  return {
    shippingAddresses,
    isFetching,
    isSaving,
    createAddress,
  };
}
