import axios from "axios";
import { useState, useEffect } from "react";
import ListingItem from "../comps/ListingItem";
import AdminItem from "../comps/AdminItem";

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
    <>
      <div className="min-h-screen min-w-300 bg-black text-white mt-15 rounded-3xl">
        <main className="max-w-5xl mx-auto px-4 sm:px-6 lg:px-8 pt-4">
          <div className="">
            <h2 className="text-3xl font-semibold mb-5">All listings</h2>
          </div>
          <div className="bg-neutral-primary-soft border-b  border-default grid grid-cols-7 items-center">
            <p>Cover</p>
            <p>Title</p>
            <p>Release Date</p>
            <p>Format</p>
            <p>Price</p>
            <p>Promoted</p>
          </div>
          <div className="mt-6">
            {listings &&
              listings.map((item) => (
                <div>
                  <AdminItem
                    key={item.id}
                    item={item}
                    onDelete={deleteListing}
                    handlePromote={handlePromote}
                    handleFreeze={handleFreeze}
                  />
                </div>
              ))}
          </div>
        </main>
      </div>
    </>
  );
}
