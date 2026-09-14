import axios from "axios";
import { useState, useEffect } from "react";
import PageContainer from "@/components/layout/PageContainer";
import AdminItem, { AdminListHeader } from "@/components/admin/AdminItem";

export default function AdminDashboard() {
  const [listings, setListings] = useState([]);

  async function fetchListings() {
    try {
      const res = await axios.get("http://localhost:8080/api/listings/all", {
        withCredentials: true,
      });
      setListings(res.data);
    } catch (error) {
      console.log(error);
    }
  }

  const deleteListing = async (listingId) => {
    try {
      const res = await axios.delete(
        `http://localhost:8080/api/listings/${listingId}`,
        {
          withCredentials: true,
        }
      );

      if (res.status == 204) {
        fetchListings();
      }
    } catch (error) {
      console.log(error);
    }
  };

  useEffect(() => {
    fetchListings();
  }, []);

  async function handlePromote(listingId, action) {
    try {
      const res = await axios.patch(
        `http://localhost:8080/api/listings/promote/${listingId}`,
        { action: action },
        { withCredentials: true }
      );
      if (res.status == 200) {
        fetchListings();
      }
    } catch (error) {
      console.log(error);
    }
  }
  async function handleFreeze(listingId, action) {
    try {
      const res = await axios.patch(
        `http://localhost:8080/api/listings/freeze/${listingId}`,
        { action: action },
        { withCredentials: true }
      );
      if (res.status == 200) {
        fetchListings();
      }
    } catch (error) {
      console.log(error);
    }
  }
  return (
    <PageContainer width="wide">
      <h1 className="text-left text-2xl font-semibold sm:text-3xl">
        All listings
      </h1>

      <div className="overflow-hidden rounded-xl border border-surface-3">
        <AdminListHeader />

        <div>
          {listings?.map((item) => (
            <AdminItem
              key={item.id}
              item={item}
              onDelete={deleteListing}
              handlePromote={handlePromote}
              handleFreeze={handleFreeze}
            />
          ))}
        </div>
      </div>
    </PageContainer>
  );
}
