import { useEffect } from "react";
import { useAddressStore } from "../stores/addressStore";

export function useAccountAddresses() {
  const addresses = useAddressStore((state) => state.addresses);
  const isFetching = useAddressStore((state) => state.isFetching);
  const isSaving = useAddressStore((state) => state.isSaving);
  const fetchAddresses = useAddressStore((state) => state.fetchAddresses);
  const createAddress = useAddressStore((state) => state.createAddress);
  const updateAddress = useAddressStore((state) => state.updateAddress);
  const deleteAddress = useAddressStore((state) => state.deleteAddress);

  useEffect(() => {
    fetchAddresses();
  }, [fetchAddresses]);

  return {
    addresses,
    isFetching,
    isSaving,
    refresh: fetchAddresses,
    createAddress,
    updateAddress,
    deleteAddress,
  };
}
