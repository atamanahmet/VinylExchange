import { useState, useEffect } from "react";
import {
  Button,
  Modal,
  ModalBody,
  ModalFooter,
  ModalHeader,
} from "flowbite-react";

export default function ImageGallery({ imagePaths, openModal }) {
  const [mainImg, setMainImg] = useState();

  const [error, setError] = useState(false);

  const handleClick = (clickedImg) => {
    setMainImg(clickedImg);
  };

  useEffect(() => {
    if (imagePaths && imagePaths.length > 0) {
      setMainImg(imagePaths[0]);
    }
  }, [imagePaths]);

  function handleModal(url) {
    openModal(url);
  }

  const isPlaceholder = mainImg?.includes("/placeholders/");

  return (
    <div>
      <div className="relative overflow-hidden">
        <img
          src={mainImg ? mainImg : "/placeholder.png"}
          alt="main"
          className="w-130 h-130 object-cover rounded-md mb-4 "
          onClick={() => mainImg && handleModal(mainImg)}
        />
        {/* placeholder badge */}
        {isPlaceholder && !error && (
          <div className="absolute top-6 -left-8.5 -rotate-45 bg-amber-500 text-black text-md font-semibold px-7 py-0.5 rounded shadow-lg tracking-wider">
            Placeholder
          </div>
        )}
      </div>

      <div className="flex gap-4.5 flex-wrap">
        {imagePaths &&
          imagePaths.map((img, i) => (
            <img
              key={i}
              src={img}
              onClick={() => handleClick(img)}
              className="w-20 h-20 object-cover cursor-pointer rounded-md border"
            />
          ))}
      </div>
    </div>
  );
}
