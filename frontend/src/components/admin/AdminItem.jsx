import { useEffect, useState } from "react";
import axios from "axios";
import { useNavigate } from "react-router-dom";

export default function AdminItem({
  item,
  onDelete,
  handlePromote,
  handleFreeze,
}) {
  const [image, setImage] = useState("");

  const navigate = useNavigate();

  const navigateItemWithId = () => {
    navigate(`/listing/${item.id}`);
  };

  const navigateToEditWithId = () => {
    navigate("/edit", { state: { id: item.id } });
  };

  return (
    <>
      <div
        className={
          item.onHold
            ? "bg-red-500" +
              "ml-1 pb-5 gap-2  grid grid-cols-7 border-b mb-5 items-center"
            : "ml-1 pb-5 gap-2  grid grid-cols-7 border-b mb-5 items-center"
        }
      >
        <button
          onClick={() => {
            navigateItemWithId();
          }}
        >
          <img
            src={item.imagePaths[0]}
            onError={(e) => {
              e.target.src = "/placeholder.png";
            }}
            alt="listing main image"
            className="bg-black"
          />
        </button>
        <button
          onClick={() => {
            navigateItemWithId();
          }}
        >
          <p
            scope="row"
            className="px-6 py-4 font-medium text-heading flex flex-col overflow-auto "
          >
            {item.title}
          </p>
        </button>

        <p className="px-6 py-4">{item.date} </p>
        <p className={"px-6 py-4"}>{item.format} </p>
        {/* <p className="px-6 py-4">{item.trackCount}</p> */}
        <div className="">
          <p
            className={`px-6 ${
              item.discount > 0
                ? "text-base font-bold text-gray-900 dark:text-white line-through"
                : "text-base font-bold text-gray-900 dark:text-white"
            }`}
          >
            {item.price.toLocaleString("tr-TR") + " ₺"}
          </p>
          <p
            className={`px-6 ${
              item.discount > 0
                ? "text-base font-bold text-green-900 dark:text-green-400"
                : "text-base font-bold text-green-900 dark:text-green-400"
            }`}
          >
            {item.discount > 0
              ? item.discountedPrice.toLocaleString("tr-TR") + " ₺"
              : null}
          </p>
        </div>
        {/* <p className="px-6 py-4">{item.price}</p> */}
        <p
          className={`px-6 ${
            item.discount > 0
              ? "text-base font-bold text-green-900 dark:text-green-400"
              : "text-base font-bold text-green-900 dark:text-green-400"
          }`}
        >
          {item.promote ? "Promoted" : "Not Promoted"}
        </p>

        <div className="px-6 py-4 text-right">
          <div className="flex flex-col justify-center items-center -mt-5">
            <button
              onClick={() => handlePromote(item.id, !item.promote)}
              className="font-medium text-fg-brand hover:underline bg-amber-600 py-2 px-2 mb-2 rounded-md  cursor-pointer"
            >
              {item.promote ? "Unpromote" : "Promote"}
            </button>
            <button
              onClick={() => handleFreeze(item.id, !item.onHold)}
              className="font-medium text-fg-brand hover:underline bg-indigo-700 py-2 px-2 mb-2 rounded-md  cursor-pointer"
            >
              {item.onHold ? "Unfreeze" : "Freeze"}
            </button>
            {/* <a
              onClick={() => {
                navigateToEditWithId();
              }}
              className="font-medium text-fg-brand hover:underline bg-indigo-700 py-2 px-4.5 mb-2 rounded-md  cursor-pointer"
            >
              Edit
            </a> */}
            <a
              onClick={() => onDelete(item.id)}
              className="font-medium text-fg-brand bg-red-700 p-2 rounded-md hover:underline cursor-pointer"
            >
              Delete
            </a>
          </div>
        </div>
      </div>
    </>
  );
}
