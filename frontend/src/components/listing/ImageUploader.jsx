import { useState, useCallback, useEffect } from "react";
import { useDropzone } from "react-dropzone";

import { cn } from "@/lib/utils";

const MAX_FILE_SIZE = 38 * 1024 * 1024; // 38MB in bytes

export default function ImageUploader({
  images,
  setImages,
  existingImages = [],
  compact = false,
}) {
  const [error, setError] = useState(null);

  // Seed existing listing images in edit mode when parent provides paths.
  useEffect(() => {
    if (!Array.isArray(existingImages) || existingImages.length === 0) {
      return;
    }

    setImages((prev) => {
      const newUploads = prev.filter((img) => !img.isExisting);
      const loadedExistingUrls = prev
        .filter((img) => img.isExisting)
        .map((img) => img.url);

      const alreadyLoaded =
        loadedExistingUrls.length === existingImages.length &&
        loadedExistingUrls.every((url, index) => url === existingImages[index]);

      if (alreadyLoaded) {
        return prev;
      }

      const existingImageObjects = existingImages.map((url, index) => ({
        preview: url,
        isExisting: true,
        url,
        name: `existing-image-${index}`,
      }));

      return [...existingImageObjects, ...newUploads];
    });
  }, [existingImages, setImages]);

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
    <div className={cn("w-full", compact ? "space-y-3" : "space-y-4")}>
      {error && (
        <div className="rounded-lg border border-danger bg-danger/10 px-4 py-3 text-danger-fg">
          {error}
        </div>
      )}

      <div
        {...getRootProps()}
        className={cn(
          "cursor-pointer rounded-xl border-2 border-dashed text-center transition-colors",
          compact ? "p-4" : "p-8",
          isDragActive
            ? "border-brand bg-surface-2"
            : "border-surface-4 bg-surface-2/50 hover:border-brand/60",
        )}
      >
        <input {...getInputProps()} />

        <p className={cn("text-on-surface", compact && "text-sm")}>
          {isDragActive
            ? "Drop images here…"
            : "Drag & drop or click to upload images"}
        </p>
        <p className="mt-1 text-xs text-on-surface-muted sm:text-sm">
          Max file size: 38MB per image
        </p>
      </div>

      {images.length > 0 && (
        <div
          className={cn(
            "grid grid-cols-2 gap-2",
            compact ? "sm:grid-cols-2" : "gap-4 sm:grid-cols-3 md:grid-cols-4",
          )}
        >
          {images.map((img, index) => (
            <div key={index} className="group relative">
              <img
                src={img.preview}
                alt={`preview ${index + 1}`}
                className={cn(
                  "w-full rounded-lg object-cover",
                  compact ? "h-20" : "h-30",
                )}
              />

              {img.isExisting && (
                <div className="absolute top-2 left-2 rounded bg-brand px-2 py-1 text-xs text-on-surface">
                  Existing
                </div>
              )}

              <button
                type="button"
                onClick={() => removeImage(index)}
                className="absolute top-1 right-1 flex size-8 items-center justify-center rounded-full bg-surface-3 text-on-surface opacity-80 transition hover:bg-danger hover:text-on-surface group-hover:opacity-100"
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
