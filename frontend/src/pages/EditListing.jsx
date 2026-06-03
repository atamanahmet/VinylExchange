// import { useEffect, useState } from "react";
// import axios from "axios";
// import ImageUploader from "../comps/ImageUploader";
// import { useLocation, useNavigate, useParams } from "react-router-dom";

// export default function EditListing() {
//   const navigate = useNavigate();

//   const { listingId } = useParams();

//   const location = useLocation();

//   const [previewPrice, setPreviewPrice] = useState("");

//   const [images, setImages] = useState([]);

//   const [listing, setListing] = useState({
//     id: "",
//     title: "",
//     status: "",
//     date: "",
//     country: "",
//     condition: "",
//     barcode: "",
//     packaging: "",
//     format: "",
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

//   async function getListing() {
//     try {
//       const res = await axios.get(
//         `http://localhost:8080/api/listings/${listingId}`,
//         {
//           withCredentials: true,
//         },
//       );
//       if (res.status == 200) {
//         setListing(res.data);
//       }
//     } catch (error) {
//       console.log(error);
//     }
//   }

//   useEffect(() => {
//     getListing();
//   }, []);

//   const fetchPricePreview = async (price, discount) => {
//     try {
//       const res = await axios.post(
//         "http://localhost:8080/api/listings/price/preview",
//         { priceTL: price, discountPercent: discount },
//         { withCredentials: true },
//       );
//       setPreviewPrice(res.data); // BigDecimal
//     } catch (err) {
//       console.error(err);
//     }
//   };

//   const handleChange = (e) => {
//     const { name, value, type, checked } = e.target;

//     setListing((prev) => {
//       const updated = {
//         ...prev,
//         [name]: type === "checkbox" ? checked : value,
//       };

//       // for price preview after discount
//       if (name === "price") {
//         setPreviewPrice(null);
//         updated.price = value;
//         updated.discount = 0;
//       }
//       if (name === "discount") {
//         if (value == 0) {
//           setPreviewPrice(null);
//         } else {
//           fetchPricePreview(updated.price, updated.discount);
//         }
//       }

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

//   const setDiscountPercent = (percent) => {
//     if (percent == 0) {
//       setPreviewPrice(null);
//     } else {
//       const newPrice = listing.price; // current price
//       const newDiscount = percent; // new discount

//       fetchPricePreview(newPrice, newDiscount);
//     }

//     setListing((prev) => ({
//       ...prev,
//       discount: percent, // update
//     }));
//   };

//   const handleSubmit = async (e) => {
//     e.preventDefault();

//     const existingImageUrls = images
//       .filter((img) => img.isExisting)
//       .map((img) => img.url);

//     const newImageFiles = images.filter((img) => !img.isExisting);

//     const payload = {
//       ...listing,
//       imagePaths: existingImageUrls,
//       tradePreferences: listing.tradeable ? listing.tradePreferences : null,
//     };

//     const formData = new FormData();

//     formData.append(
//       "listing",
//       new Blob([JSON.stringify(payload)], {
//         type: "application/json",
//       }),
//     );

//     newImageFiles.forEach((img) => {
//       formData.append("images", img);
//     });

//     try {
//       const res = await axios.put(
//         `http://localhost:8080/api/listings/${listingId}`,
//         formData,
//         {
//           withCredentials: true,
//           headers: { "Content-Type": "multipart/form-data" },
//         },
//       );

//       if (res.status === 200) {
//         console.log("Listing updated:", res.data);
//         navigate("/listings");
//       }
//     } catch (err) {
//       console.log("error caught", err);
//       if (err.status === 403) {
//         navigate("/");
//       }
//     }
//   };
//   return (
//     <div className="mt-5 max-w-7xl mx-auto">
//       <h2 className="text-3xl font-bold text-left ml-4">Edit Listing</h2>
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
//               <label className="block mb-1">Release Date</label>
//               <input
//                 type="date"
//                 name="date"
//                 value={listing.date}
//                 onChange={handleChange}
//                 className="input w-75 input-bordered  border-2 border-amber-50 ring-1 ring-indigo-800 rounded-md pl-2 py-1"
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
//               Submit
//             </button>
//           </div>
//           <div className="formItem max-w-50 mt-14 ml-4">
//             <label className="block mb-1 ">Format</label>

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
//             <h3 className="text-2xl font-bold">Trade Information</h3>
//             <div className="mt-5">
//               <div className="formItem mt-5 relative">
//                 <label className="block mb-1">Direct Sell price</label>
//                 <input
//                   type="number"
//                   name="price"
//                   value={previewPrice ? previewPrice : listing.price}
//                   onChange={handleChange}
//                   className="input w-75 input-bordered  border-2 text-green-400 font-bold border-amber-50 ring-1 ring-indigo-800 rounded-md pl-2 py-1"
//                 />
//                 {previewPrice && (
//                   <span className="line-through absolute right-16 top-8.5 text-red-500">
//                     {listing.price}
//                   </span>
//                 )}

//                 <span className="absolute text-xl right-12 top-7.5"> ₺</span>
//               </div>
//               <label className="block mb-1">
//                 Discount
//                 <a
//                   className="px-1.5 py-0.5 rounded-full bg-red-600 text-white ml-2"
//                   onClick={() => setDiscountPercent(0)}
//                 >
//                   ↺
//                 </a>
//               </label>

//               <div className="space-x-2">
//                 <button
//                   type="button"
//                   className="px-2 py-1 rounded-md border bg-indigo-700 "
//                   onClick={() => setDiscountPercent(10)}
//                 >
//                   10%
//                 </button>
//                 <button
//                   type="button"
//                   className="px-2 py-1 rounded-md border bg-indigo-700 "
//                   onClick={() => setDiscountPercent(20)}
//                 >
//                   20%
//                 </button>
//                 <button
//                   type="button"
//                   className="px-2 py-1 rounded-md border bg-indigo-700 "
//                   onClick={() => setDiscountPercent(30)}
//                 >
//                   30%
//                 </button>
//                 <button
//                   type="button"
//                   className="px-2 py-1 rounded-md border bg-indigo-700 "
//                   onClick={() => setDiscountPercent(40)}
//                 >
//                   40%
//                 </button>
//                 <input
//                   type="number"
//                   name="discount"
//                   value={listing.discount}
//                   placeholder={listing.discount == 0 ? "0.0" : listing.discount}
//                   onChange={handleChange}
//                   className="input w-19 input-bordered border-2   border-amber-50 ring-1 ring-indigo-800 rounded-md pl-2 py-1"
//                 />
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
//                               price: 0,
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
//                   color:
//                     listing.description?.length <= 255 ||
//                     listing.description == null
//                       ? "green"
//                       : "red",
//                 }}
//               >
//                 {listing.description?.length || 0}/255
//               </p>
//             </div>
//             <div>
//               <h3 className="text-3xl font-bold mb-5 mt-5">Upload images</h3>

//               <ImageUploader
//                 images={images}
//                 setImages={setImages}
//                 existingImages={listing.imagePaths}
//               />
//             </div>
//           </div>
//         </div>
//       </form>
//     </div>
//   );
// }
