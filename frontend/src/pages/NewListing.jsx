// import { useEffect, useState } from "react";
// import axios from "axios";
// import ImageUploader from "../comps/ImageUploader";
// import { useNavigate } from "react-router-dom";
// import { useSearchStore } from "../stores/searchStore";
// import Card from "../comps/Card";
// import { mbReleaseToListingMap } from "../adapters/mbReleaseToListingMap";
// import { useAuthStore } from "../stores/authStore";
// import {
//   Button,
//   Modal,
//   ModalBody,
//   ModalFooter,
//   ModalHeader,
// } from "flowbite-react";
// import SkeletonCardView from "../comps/Skeletons/SkeletonCardView";
// import CardShell from "../comps/CardShell";

// export default function NewListing() {
//   const [images, setImages] = useState([]);
//   const user = useAuthStore((state) => state.user);
//   const checkAuth = useAuthStore((state) => state.checkAuth);
//   const setOpenLogin = useAuthStore((state) => state.setOpenLogin);
//   const searchMusicBrainz = useSearchStore((state) => state.searchMusicBrainz);
//   const isLoadingSearch = useSearchStore((state) => state.isLoadingSearch);
//   const searchResult = useSearchStore((state) => state.searchResult);

//   const [mbResults, setMbResults] = useState([]);
//   const [isModalOpen, setIsModalOpen] = useState(false);
//   const [loadingMb, setLoadingMb] = useState(false);

//   const [items, setItems] = useState([]);

//   const navigate = useNavigate();

//   //user check
//   useEffect(() => {
//     checkAuth();
//   }, []);

//   const [listing, setListing] = useState({
//     title: "",
//     // status: "",
//     date: "",
//     country: "",
//     barcode: "",
//     condition: "",
//     packaging: "",
//     labelName: "",
//     format: "",
//     imageUrl: "",
//     mbId: "",
//     trackCount: 1,
//     stockQuantity: 1,
//     artistName: "",
//     tradeValue: 0,
//     description: "",

//     tradeable: false,
//     price: 0,
//     discount: 0,
//     tradePreferences: [],
//   });

//   useEffect(() => {
//     if (!searchResult?.items) {
//       setItems([]);
//       return;
//     }

//     setItems(
//       searchResult?.items.map((release) => mbReleaseToListingMap(release)),
//     );
//   }, [searchResult]);

//   const checkRelease = async () => {
//     if (!listing.title) return alert("Enter album title first");
//     setIsModalOpen(true);
//     await searchMusicBrainz(listing.title);
//   };

//   const selectMbRelease = (item) => {
//     console.log("selected item", item);
//     setListing((prev) => ({
//       ...prev,
//       title: item.title,
//       artistName: item.artist,
//       year: item.year || "",
//       labelName: item.label || "",
//       mbId: item.id || "",
//       barcode: item.barcode || "",
//       format: item.format || "",
//       country: item.country || "",
//       trackCount: item.trackCount || 1,
//     }));
//     setIsModalOpen(false);
//   };

//   const handleChange = (e) => {
//     const { name, value, type, checked } = e.target;

//     setListing((prev) => {
//       const updated = {
//         ...prev,
//         [name]: type === "checkbox" ? checked : value,
//       };

//       // tradeable disabled, clear prefs
//       if (name === "tradeable" && checked === false) {
//         updated.tradePreferences = [];
//       }

//       return updated;
//     });
//   };

//   const handleTradePrefChange = (index, field, value) => {
//     setListing((prev) => {
//       const prefs = [...prev.tradePreferences];
//       prefs[index] = {
//         ...prefs[index],
//         [field]: value,
//       };

//       return { ...prev, tradePreferences: prefs };
//     });
//   };

//   const addPreference = () => {
//     setListing((prev) => ({
//       ...prev,
//       tradePreferences: [
//         ...prev.tradePreferences,
//         {
//           desiredItem: "",
//           paymentDirection: "NO_EXTRA",
//           extraAmount: 0,
//         },
//       ],
//     }));
//   };

//   const removePreference = (index) => {
//     setListing((prev) => ({
//       ...prev,
//       tradePreferences: prev.tradePreferences.filter((_, i) => i !== index),
//     }));
//   };

//   const handleSubmit = async (e) => {
//     e.preventDefault();

//     const payload = {
//       ...listing,
//       tradePreferences: listing.tradeable ? listing.tradePreferences : null,
//     };

//     const formData = new FormData();

//     // upload images
//     images.forEach((img) => {
//       formData.append("images", img);
//     });

//     formData.append(
//       "listing",
//       new Blob([JSON.stringify(payload)], {
//         type: "application/json",
//       }),
//     );

//     for (let pair of formData.entries()) {
//       console.log(pair[0], pair[1]);
//     }

//     try {
//       const res = await axios.post(
//         "http://localhost:8080/api/listings",
//         formData,
//         {
//           withCredentials: true,
//           headers: { "Content-Type": "multipart/form-data" },
//         },
//       );

//       alert("Listing added successfully!");
//       navigate("/listings");
//     } catch (err) {
//       console.error(err);
//       alert("Error adding vinyl");
//     }
//   };

//   return (
//     <div className="mt-5 max-w-7xl mx-auto">
//       <Modal
//         show={isModalOpen}
//         onClose={() => setIsModalOpen(false)}
//         dismissible
//         className="backdrop-blur-sm "
//       >
//         <div className="flex justify-center w-full">
//           <div className="bg-black text-center rounded-md px-5 w-full sm:w-auto min-w-3xl max-w-5xl max-h-[95vh] overflow-y-auto relative">
//             {/* close button */}
//             <button
//               onClick={() => setIsModalOpen(false)}
//               className="absolute top-3 right-6 text-white text-2xl hover:text-red-500"
//               aria-label="Close"
//             >
//               ×
//             </button>
//             <ModalHeader className="bg-black sticky top-0 z-10">
//               Select Release
//             </ModalHeader>

//             {isLoadingSearch && <p>Loading...</p>}

//             {!isLoadingSearch && !searchResult.items.length && (
//               <p>No results found.</p>
//             )}

//             <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-4 justify-items-center py-5">
//               {isLoadingSearch ? (
//                 Array(6)
//                   .fill(0)
//                   .map((_, i) => <SkeletonCardView key={i} />)
//               ) : items.length === 0 ? (
//                 <p className="text-white col-span-full text-center">
//                   No results found.
//                 </p>
//               ) : (
//                 items.map((item) => (
//                   <Card
//                     key={item.id}
//                     item={item}
//                     onSelect={() => selectMbRelease(item)}
//                   />
//                 ))
//               )}
//             </div>

//             <button
//               className="btn btn-sm btn-outline mt-4"
//               onClick={() => setIsModalOpen(false)}
//             >
//               Close
//             </button>
//           </div>
//         </div>
//       </Modal>

//       <h2 className="text-3xl font-bold text-left ml-4">Create New Listing</h2>
//       <form onSubmit={handleSubmit} className=" p-4 space-y-4 text-left">
//         <div className="grid grid-cols-[0.9fr_0.5fr_1fr_1fr]">
//           <div className="">
//             <div className="formItem">
//               <h3 className="text-2xl font-bold mb-5">Listing information</h3>

//               <label className="block mb-1">Album</label>
//               <input
//                 type="text"
//                 name="title"
//                 value={listing.title}
//                 onChange={handleChange}
//                 className="input w-75 input-bordered border-2 border-amber-50 ring-1 ring-indigo-800 rounded-md pl-2 py-1"
//                 required
//               />
//               <button
//                 type="button"
//                 onClick={checkRelease}
//                 className="bg-indigo-600 rounded mt-2 px-2"
//               >
//                 Check Release Info
//               </button>
//             </div>
//             <div className="formItem">
//               <label className="block mb-1">Artist / Band</label>
//               <input
//                 type="text"
//                 name="artistName"
//                 value={listing.artistName}
//                 onChange={handleChange}
//                 className="input w-75 input-bordered border-2 border-amber-50 ring-1 ring-indigo-800 rounded-md pl-2 py-1"
//               />
//             </div>

//             {/* packaging */}
//             <div className="formItem my-3 ">
//               <h2 className="mb-2 text-white">Packaging</h2>
//               <label className="inline-flex items-center gap-2 ">
//                 <input
//                   type="radio"
//                   name="packaging"
//                   value="SEALED"
//                   checked={listing.packaging === "SEALED"}
//                   onChange={handleChange}
//                   className="radio radio-primary border-2 border-amber-50 ring-1 ring-indigo-800 rounded-md pl-2 py-1"
//                   required
//                 />
//                 <span>Sealed</span>
//                 <input
//                   type="radio"
//                   name="packaging"
//                   value="OPENED"
//                   checked={listing.packaging === "OPENED"}
//                   onChange={handleChange}
//                   className="ml-5 radio radio-primary border-2 border-amber-50 ring-1 ring-indigo-800 rounded-md pl-2 py-1"
//                 />
//                 <span>Opened</span>
//               </label>
//               <label className="inline-flex items-center gap-2 ">
//                 <input
//                   type="radio"
//                   name="packaging"
//                   value="RESEALED"
//                   checked={listing.packaging === "RESEALED"}
//                   onChange={handleChange}
//                   className="ml-5 radio radio-primary border-2 border-amber-50 ring-1 ring-indigo-800 rounded-md pl-2 py-1"
//                 />
//                 <span>Resealed</span>
//               </label>
//             </div>
//             <div className="formItem mt-3">
//               <label className="block mb-1">Release Year</label>
//               <input
//                 type="number"
//                 name="year"
//                 value={listing.year || ""}
//                 onChange={(e) =>
//                   handleChange({
//                     target: {
//                       name: "year",
//                       value: Number(e.target.value),
//                     },
//                   })
//                 }
//                 min={1900}
//                 max={new Date().getFullYear()}
//                 className="input w-75 input-bordered border-2 border-amber-50 ring-1 ring-indigo-800 rounded-md pl-2 py-1"
//               />
//             </div>
//             <div className="formItem">
//               <label className="block mb-1">Country</label>
//               <input
//                 type="text"
//                 name="country"
//                 value={listing.country}
//                 onChange={handleChange}
//                 className="input w-75 input-bordered border-2 border-amber-50 ring-1 ring-indigo-800 rounded-md pl-2 py-1"
//               />
//             </div>
//             <div className="formItem">
//               <label className="block mb-1">Label</label>
//               <input
//                 type="text"
//                 name="labelName"
//                 value={listing.labelName}
//                 onChange={handleChange}
//                 className="input w-75 input-bordered border-2 border-amber-50 ring-1 ring-indigo-800 rounded-md pl-2 py-1"
//               />
//             </div>
//             <div className="formItem">
//               <label className="block mb-1">Barcode / Catalog No.</label>
//               <input
//                 type="text"
//                 name="barcode"
//                 value={listing.barcode}
//                 onChange={handleChange}
//                 className="input w-75 input-bordered  border-2 border-amber-50 ring-1 ring-indigo-800 rounded-md pl-2 py-1"
//               />
//             </div>
//             <div className="formItem">
//               <label className="block mb-1">Stock</label>
//               <input
//                 type="number"
//                 name="stockQuantity"
//                 value={listing.stockQuantity}
//                 min={1}
//                 onChange={handleChange}
//                 className="input w-75 input-bordered  border-2 border-amber-50 ring-1 ring-indigo-800 rounded-md pl-2 py-1"
//               />
//             </div>
//             {/* <div className="formItem">
//               <label className="block mb-1">Packaging</label>
//               <input
//                 type="text"
//                 name="packaging"
//                 value={listing.packaging}
//                 onChange={handleChange}
//                 className="input w-75 input-bordered  border-2 border-amber-50 ring-1 ring-indigo-800 rounded-md pl-2 py-1"
//               />
//             </div> */}
//             <div className="formItem">
//               <label className="block mb-1">Track Count</label>
//               <input
//                 type="number"
//                 name="trackCount"
//                 value={listing.trackCount}
//                 min={1}
//                 onChange={handleChange}
//                 className="input w-75 input-bordered  border-2 border-amber-50 ring-1 ring-indigo-800 rounded-md pl-2 py-1"
//               />
//             </div>
//             <button
//               type="submit"
//               className="btn btn-primary border-2 bg-indigo-800 text-white border-amber-50 ring-1 py-2 ring-indigo-800 rounded-md px-2 mt-2"
//             >
//               Create
//             </button>
//           </div>

//           <div className="formItem max-w-50 mt-14 ml-1">
//             <label className="block mb-1 font-bold ">Format</label>

//             <div className="flex flex-col gap-2">
//               {/* listing LP */}
//               <label className="inline-flex items-center gap-2 ">
//                 <input
//                   type="radio"
//                   name="format"
//                   value="33"
//                   checked={listing.format === "33"}
//                   onChange={handleChange}
//                   className="radio radio-primary border-2 border-amber-50 ring-1 ring-indigo-800 rounded-md pl-2 py-1"
//                   required
//                 />
//                 <span>LP 12"/ 33 </span>
//               </label>

//               {/* 7" Single */}
//               <label className="inline-flex items-center gap-2">
//                 <input
//                   type="radio"
//                   name="format"
//                   value="45"
//                   checked={listing.format === "45"}
//                   onChange={handleChange}
//                   className="radio radio-primary border-2 border-amber-50 ring-1 ring-indigo-800 rounded-md pl-2 py-1"
//                 />
//                 <span>Single 7" / 45 </span>
//               </label>

//               {/* EP */}
//               <label className="inline-flex items-center gap-2">
//                 <input
//                   type="radio"
//                   name="format"
//                   value="EP"
//                   checked={listing.format === "EP"}
//                   onChange={handleChange}
//                   className="radio radio-primary border-2  border-amber-50 ring-1 ring-indigo-800 rounded-md pl-2 py-1"
//                 />
//                 <span>EP</span>
//               </label>

//               {/* Cassette */}
//               <label className="inline-flex items-center gap-2">
//                 <input
//                   type="radio"
//                   name="format"
//                   value="Cassette"
//                   checked={listing.format === "Cassette"}
//                   onChange={handleChange}
//                   className="radio radio-primary border-2 border-amber-50 ring-1 ring-indigo-800 rounded-md pl-2 py-1"
//                 />
//                 <span>Cassette</span>
//               </label>

//               {/* CD */}
//               <label className="inline-flex items-center gap-2">
//                 <input
//                   type="radio"
//                   name="format"
//                   value="CD"
//                   checked={listing.format === "CD"}
//                   onChange={handleChange}
//                   className="radio radio-primary border-2 border-amber-50 ring-1 ring-indigo-800 rounded-md pl-2 py-1"
//                 />
//                 <span>CD</span>
//               </label>
//               <label className="inline-flex items-center gap-2">
//                 <input
//                   type="radio"
//                   name="format"
//                   value="other"
//                   checked={listing.format === "Other"}
//                   onChange={handleChange}
//                   className="radio radio-primary border-2 border-amber-50 ring-1 ring-indigo-800 rounded-md pl-2 py-1"
//                 />
//                 <span>Other</span>
//               </label>
//             </div>
//             {/* condition */}
//             <label className="block mb-1 mt-5 font-bold ">Condition</label>

//             <div className="flex flex-col gap-2">
//               <label className="inline-flex items-center gap-2 ">
//                 <input
//                   type="radio"
//                   name="condition"
//                   value="P"
//                   checked={listing.condition === "P"}
//                   onChange={handleChange}
//                   className="radio radio-primary border-2 border-amber-50 ring-1 ring-indigo-800 rounded-md pl-2 py-1"
//                   required
//                 />
//                 <span>(P) (F) Poor / Fair</span>
//               </label>

//               <label className="inline-flex items-center gap-2">
//                 <input
//                   type="radio"
//                   name="condition"
//                   value="G"
//                   checked={listing.condition === "G"}
//                   onChange={handleChange}
//                   className="radio radio-primary border-2 border-amber-50 ring-1 ring-indigo-800 rounded-md pl-2 py-1"
//                 />
//                 <span>(G) Good</span>
//               </label>

//               <label className="inline-flex items-center gap-2">
//                 <input
//                   type="radio"
//                   name="condition"
//                   value="VG"
//                   checked={listing.condition === "VG"}
//                   onChange={handleChange}
//                   className="radio radio-primary border-2  border-amber-50 ring-1 ring-indigo-800 rounded-md pl-2 py-1"
//                 />
//                 <span>(VG) Very Good</span>
//               </label>

//               <label className="inline-flex items-center gap-2">
//                 <input
//                   type="radio"
//                   name="condition"
//                   value="VG+"
//                   checked={listing.condition === "VG+"}
//                   onChange={handleChange}
//                   className="radio radio-primary border-2 border-amber-50 ring-1 ring-indigo-800 rounded-md pl-2 py-1"
//                 />
//                 <span>(VG+) Very Good+</span>
//               </label>

//               {/* CD */}
//               <label className="inline-flex items-center gap-2">
//                 <input
//                   type="radio"
//                   name="condition"
//                   value="E"
//                   checked={listing.condition === "E"}
//                   onChange={handleChange}
//                   className="radio radio-primary border-2 border-amber-50 ring-1 ring-indigo-800 rounded-md pl-2 py-1"
//                 />
//                 <span>(E) Excellent</span>
//               </label>
//               <label className="inline-flex items-center gap-2">
//                 <input
//                   type="radio"
//                   name="condition"
//                   value="NM"
//                   checked={listing.condition === "NM"}
//                   onChange={handleChange}
//                   className="radio radio-primary border-2 border-amber-50 ring-1 ring-indigo-800 rounded-md pl-2 py-1"
//                 />
//                 <span>(NM) Near Mint</span>
//               </label>
//               <label className="inline-flex items-center gap-2">
//                 <input
//                   type="radio"
//                   name="condition"
//                   value="M"
//                   checked={listing.condition === "M"}
//                   onChange={handleChange}
//                   className="radio radio-primary border-2 border-amber-50 ring-1 ring-indigo-800 rounded-md pl-2 py-1"
//                 />
//                 <span>(M) Mint</span>
//               </label>
//             </div>
//           </div>

//           <div className="ml-5">
//             <h3 className="text-2xl font-bold ">Trade Information</h3>
//             <div className="mt-5">
//               <div className="formItem mt-5 relative">
//                 <label className="block mb-1">Direct Sell price</label>
//                 <input
//                   type="number"
//                   name="price"
//                   step="0.01"
//                   min={1}
//                   value={listing.price}
//                   onChange={handleChange}
//                   className="input w-75 input-bordered  border-2 border-amber-50 ring-1 ring-indigo-800 rounded-md pl-2 py-1"
//                   required
//                 />
//                 <span className="absolute text-xl right-12 top-7.5"> ₺</span>
//               </div>
//               <label className="inline-flex items-center gap-2 mt-5">
//                 <input
//                   type="checkbox"
//                   name="tradeable"
//                   checked={listing.tradeable}
//                   onChange={handleChange}
//                   className="border-2 border-amber-50 ring-1 ring-indigo-800 rounded-md pl-2 py-1"
//                 />
//                 <span>Open to trade</span>
//                 <div className="vl -mr-0.5"></div>
//                 <label
//                   className={`block
//       ${!listing.tradeable ? "opacity-50 cursor-not-allowed" : ""}`}
//                 >
//                   Trade value
//                 </label>
//                 <input
//                   type="number"
//                   name="tradeValue"
//                   value={listing.tradeValue}
//                   onChange={handleChange}
//                   disabled={!listing.tradeable}
//                   className={`input w-19 input-bordered border-2 border-amber-50 ring-1 ring-indigo-800 rounded-md pl-2 py-1
//       ${!listing.tradeable ? "opacity-50 cursor-not-allowed" : ""}`}
//                 />
//               </label>
//               {/* <div className="formItem mt-5">
//                 <label className="block mb-1">Discount</label>
//                 <input
//                   type="number"
//                   name="discount"
//                   value={listing.discount}
//                   onChange={handleChange}
//                   className="input w-75 input-bordered  border-2 border-amber-50 ring-1 ring-indigo-800 rounded-md pl-2 py-1"
//                 />
//               </div> */}
//               {listing.tradeable && (
//                 <div className="mt-5">
//                   <div className=" items-center justify-between mb-2 relative">
//                     <label className="font-bold">Trade Preferences</label>

//                     <button
//                       type="button"
//                       onClick={() =>
//                         setListing((prev) => ({
//                           ...prev,
//                           tradePreferences: [
//                             ...prev.tradePreferences,
//                             {
//                               desiredItem: "",
//                               extraAmount: 0,
//                               paymentDirection: "NO_EXTRA",
//                             },
//                           ],
//                         }))
//                       }
//                       className="btn btn-sm border border-amber-200 absolute right-11 bottom-0.5"
//                     >
//                       +
//                     </button>
//                   </div>

//                   {listing.tradePreferences.map((pref, index) => (
//                     <div
//                       key={index}
//                       className="mb-3 items-center relative border rounded-md p-1 py-2 -ml-1.5 w-78"
//                     >
//                       {/* Remove */}
//                       <button
//                         type="button"
//                         onClick={() =>
//                           setListing((prev) => ({
//                             ...prev,
//                             tradePreferences: prev.tradePreferences.filter(
//                               (_, i) => i !== index,
//                             ),
//                           }))
//                         }
//                         className="text-red-500 text-xl text-center absolute right-2 top-1 rounded-full p-0.5 w-7"
//                       >
//                         x
//                       </button>
//                       {/* Desired Item */}
//                       <input
//                         type="text"
//                         placeholder="Record title"
//                         value={pref.desiredItem}
//                         onChange={(e) =>
//                           handleTradePrefChange(
//                             index,
//                             "desiredItem",
//                             e.target.value,
//                           )
//                         }
//                         className="input w-75 input-bordered border-2 border-amber-50 ring-1 ring-indigo-800 rounded-md pl-2 py-1 mb-3"
//                       />

//                       <div className=" mb-1">
//                         <label className="font-bold">Value difference</label>
//                       </div>

//                       <div className="flex gap-2">
//                         <button
//                           type="button"
//                           onClick={() =>
//                             handleTradePrefChange(
//                               index,
//                               "paymentDirection",
//                               "NO_EXTRA",
//                             )
//                           }
//                           className={`px-2 py-1 rounded-md border ${
//                             pref.paymentDirection === "NO_EXTRA"
//                               ? "bg-green-600 text-white"
//                               : "bg-indigo-700"
//                           }`}
//                         >
//                           0
//                         </button>

//                         <button
//                           type="button"
//                           onClick={() =>
//                             handleTradePrefChange(
//                               index,
//                               "paymentDirection",
//                               "PAY",
//                             )
//                           }
//                           className={`px-2 py-1 rounded-md border ${
//                             pref.paymentDirection === "PAY"
//                               ? "bg-red-600 text-white"
//                               : "bg-indigo-700"
//                           }`}
//                         >
//                           Pay
//                         </button>

//                         <button
//                           type="button"
//                           onClick={() =>
//                             handleTradePrefChange(
//                               index,
//                               "paymentDirection",
//                               "RECEIVE",
//                             )
//                           }
//                           className={`px-2 py-1 rounded-md border ${
//                             pref.paymentDirection === "RECEIVE"
//                               ? "bg-green-600 text-white"
//                               : "bg-indigo-700"
//                           }`}
//                         >
//                           Receive
//                         </button>
//                         {/* Extra Amount */}
//                         <input
//                           type="number"
//                           placeholder="Amount"
//                           value={pref.extraAmount}
//                           onChange={(e) =>
//                             handleTradePrefChange(
//                               index,
//                               "extraAmount",
//                               e.target.value,
//                             )
//                           }
//                           disabled={pref.paymentDirection === "NO_EXTRA"}
//                           className={`input input-bordered border-3 rounded-md w-33.5 border-white pl-1.5 ${
//                             pref.paymentDirection === "NO_EXTRA"
//                               ? "opacity-50 cursor-not-allowed"
//                               : ""
//                           }`}
//                         />
//                       </div>
//                     </div>
//                   ))}
//                 </div>
//               )}
//             </div>
//           </div>
//           <div>
//             <div className="formItem ">
//               <label className="block text-2xl font-bold mb-2">
//                 Description
//               </label>
//               <textarea
//                 type="text"
//                 name="description"
//                 rows="5"
//                 value={listing.description}
//                 onChange={handleChange}
//                 maxLength="255"
//                 className="input w-87 input-bordered border-2 mt-5 border-amber-50 ring-1 ring-indigo-800 rounded-md pl-2 py-1"
//               />
//               <p
//                 className="text-right"
//                 style={{
//                   color: listing.description.length <= 255 ? "green" : "red",
//                 }}
//               >
//                 {listing.description.length}/255
//               </p>
//             </div>
//             <div>
//               <h3 className="text-2xl font-bold mb-5 mt-5">Upload images</h3>

//               <ImageUploader images={images} setImages={setImages} />
//             </div>
//           </div>
//         </div>
//       </form>
//     </div>
//   );
// }
