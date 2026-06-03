import { useState, useCallback, useEffect } from "react";
import { useDropzone } from "react-dropzone";

const MAX_FILE_SIZE = 38 * 1024 * 1024; // 38MB in bytes

export default function ImageUploader({
  images,
  setImages,
  existingImages = [],
}) {
  const [error, setError] = useState(null);

  //for edit only
  useEffect(() => {
    if (existingImages.length > 0 && images.length === 0) {
      const existingImageObjects = existingImages.map((url, index) => ({
        preview: url,
        isExisting: true,
        url: url,
        name: `existing-image-${index}`,
      }));
      setImages(existingImageObjects);
    }
  }, [existingImages, images.length, setImages]);

  useEffect(() => {
    return () => {
      images.forEach((img) => {
        if (img.preview && !img.isExisting) {
          URL.revokeObjectURL(img.preview);
        }
      });
    };
  }, [images]);

  const onDrop = useCallback(
    (acceptedFiles, rejectedFiles) => {
      console.log("Accepted:", acceptedFiles);
      console.log("Rejected:", rejectedFiles);

      setError(null);

      if (rejectedFiles.length > 0) {
        const oversizedCount = rejectedFiles.filter((rejection) =>
          rejection.errors.some((err) => err.code === "file-too-large"),
        ).length;

        if (oversizedCount > 0) {
          setError(`${oversizedCount} file(s) exceed the 38MB limit`);
          setTimeout(() => setError(null), 5000);
        }
      }

      if (acceptedFiles.length > 0) {
        const newFiles = acceptedFiles.map((file) =>
          Object.assign(file, {
            preview: URL.createObjectURL(file),
            isExisting: false,
          }),
        );
        setImages((prev) => [...prev, ...newFiles]);
      }
    },
    [setImages],
  );

  const { getRootProps, getInputProps, isDragActive } = useDropzone({
    onDrop,
    accept: { "image/*": [] },
    multiple: true,
    maxSize: MAX_FILE_SIZE,
  });

  const removeImage = (index) => {
    const imageToRemove = images[index];

    if (imageToRemove.preview && !imageToRemove.isExisting) {
      URL.revokeObjectURL(imageToRemove.preview);
    }

    setImages((prev) => prev.filter((_, i) => i !== index));
  };

  return (
    <div className="space-y-4 max-w-100">
      {error && (
        <div className="bg-red-900/50 border border-red-500 text-red-200 px-4 py-3 rounded">
          {error}
        </div>
      )}

      {/* DROPZONE */}
      <div
        {...getRootProps()}
        className={`
          border-2 border-dashed rounded-xl p-8 text-center cursor-pointer 
          transition
          ${isDragActive ? "bg-neutral-800 border-white" : "border-neutral-700"}
        `}
      >
        <input {...getInputProps()} />

        <p className="text-gray-300">
          {isDragActive
            ? "Drop images here…"
            : "Drag & drop or click to upload images"}
        </p>
        <p className="text-gray-500 text-sm mt-2">
          Max file size: 38MB per image
        </p>
      </div>

      {images.length > 0 && (
        <div className="grid grid-cols-2 sm:grid-cols-3 md:grid-cols-4 gap-4">
          {images.map((img, index) => (
            <div key={index} className="relative group">
              <img
                src={img.preview}
                alt={`preview ${index + 1}`}
                className="w-full h-30 object-cover rounded-lg"
              />

              {img.isExisting && (
                <div className="absolute top-2 left-2 bg-blue-600 text-white text-xs px-2 py-1 rounded">
                  Existing
                </div>
              )}

              {/* REMOVE BUTTON */}
              <button
                type="button"
                onClick={() => removeImage(index)}
                className="
                  absolute top-0.5 right-0.5 
                  w-10 h-10
                  bg-indigo-600 hover:bg-red-700 
                  text-white p-0 rounded-full
                  opacity-50 group-hover:opacity-100 transition
                  flex items-center justify-center
                "
                aria-label={`Remove image ${index + 1}`}
              >
                ×
              </button>
            </div>
          ))}
        </div>
      )}
    </div>
  );
}
