import { useEffect, useLayoutEffect, useMemo, useRef, useState } from "react";
import { ChevronLeft, ChevronRight } from "lucide-react";

import { Button } from "@/components/ui/button";
import { cn } from "@/lib/utils";
import {
  MAIN_IMAGE_FRAME_CLASS,
} from "@/utils/galleryLayout";
import {
  isImageElementReady,
  markImageLoaded,
  wasImageLoaded,
} from "@/utils/imageLoadCache";

function useImageLoaded(src) {
  const imgRef = useRef(null);
  const [loaded, setLoaded] = useState(() => wasImageLoaded(src));

  function markReady() {
    if (!src) {
      return;
    }
    markImageLoaded(src);
    setLoaded(true);
  }

  function syncFromElement(img) {
    if (isImageElementReady(img)) {
      markReady();
    }
  }

  useLayoutEffect(() => {
    if (!src) {
      setLoaded(false);
      return;
    }

    if (wasImageLoaded(src)) {
      setLoaded(true);
      return;
    }

    setLoaded(false);
    syncFromElement(imgRef.current);
  }, [src]);

  function handleImgRef(img) {
    imgRef.current = img;
    if (img) {
      syncFromElement(img);
    }
  }

  function handleLoad() {
    markReady();
  }

  return { loaded, handleLoad, handleImgRef };
}

function GallerySkeleton({ className }) {
  return (
    <div
      className={cn("absolute inset-0 z-10 animate-pulse bg-surface-3", className)}
      aria-hidden="true"
    />
  );
}

function GalleryThumb({ src, index, selected, onKeyDown }) {
  const { loaded, handleLoad, handleImgRef } = useImageLoaded(src);

  return (
    <button
      type="button"
      data-thumb-index={index}
      data-selected={selected ? "true" : "false"}
      onKeyDown={onKeyDown}
      className={cn(
        "relative size-20 shrink-0 overflow-hidden rounded-lg ring-2 transition-all select-none sm:size-24",
        selected ? "ring-brand" : "ring-surface-3 hover:ring-surface-4",
      )}
    >
      {!loaded && <GallerySkeleton />}

      <img
        ref={handleImgRef}
        src={src}
        alt=""
        draggable={false}
        loading="lazy"
        decoding="async"
        className={cn(
          "pointer-events-none size-full object-cover transition-opacity duration-200",
          loaded ? "opacity-100" : "opacity-0",
        )}
        onLoad={handleLoad}
      />
    </button>
  );
}

function getThumbIndex(target) {
  const el = target.closest("[data-thumb-index]");
  if (!el) {
    return null;
  }

  const index = Number(el.dataset.thumbIndex);
  return Number.isNaN(index) ? null : index;
}

export default function ImageGallery({ imagePaths, openModal }) {
  const [selectedIndex, setSelectedIndex] = useState(0);
  const [error, setError] = useState(false);
  const [scrollState, setScrollState] = useState({ left: false, right: false });
  const thumbStripRef = useRef(null);
  const dragRef = useRef({
    active: false,
    startX: 0,
    scrollLeft: 0,
    moved: false,
    pressedIndex: null,
  });

  const thumbnails = useMemo(
    () => (imagePaths?.length > 0 ? imagePaths : ["/placeholder.png"]),
    [imagePaths],
  );

  const pathsKey = thumbnails.join("\0");
  const mainImg = thumbnails[selectedIndex] ?? thumbnails[0];
  const { loaded: mainLoaded, handleLoad: handleMainLoad, handleImgRef } =
    useImageLoaded(mainImg);

  const showThumbArrows = scrollState.left || scrollState.right;

  useEffect(() => {
    setSelectedIndex(0);
    setError(false);
  }, [pathsKey]);

  function updateScrollState() {
    const strip = thumbStripRef.current;
    if (!strip) {
      return;
    }

    setScrollState({
      left: strip.scrollLeft > 1,
      right: strip.scrollLeft + strip.clientWidth < strip.scrollWidth - 1,
    });
  }

  useEffect(() => {
    const strip = thumbStripRef.current;
    if (!strip) {
      return;
    }

    updateScrollState();
    strip.addEventListener("scroll", updateScrollState, { passive: true });

    const observer = new ResizeObserver(updateScrollState);
    observer.observe(strip);

    return () => {
      strip.removeEventListener("scroll", updateScrollState);
      observer.disconnect();
    };
  }, [pathsKey, thumbnails.length]);

  useEffect(() => {
    const strip = thumbStripRef.current;
    if (!strip) return;

    const activeThumb = strip.querySelector('[data-selected="true"]');
    activeThumb?.scrollIntoView({
      behavior: "smooth",
      inline: "nearest",
      block: "nearest",
    });
  }, [selectedIndex]);

  function scrollThumbs(direction) {
    const strip = thumbStripRef.current;
    if (!strip) {
      return;
    }

    const thumb = strip.querySelector("[data-thumb-index]");
    const step = thumb ? thumb.offsetWidth + 8 : 88;
    strip.scrollBy({ left: direction * step, behavior: "smooth" });
  }

  function handleThumbKeyDown(event, index) {
    if (event.key === "Enter" || event.key === " ") {
      event.preventDefault();
      setSelectedIndex(index);
    }
  }

  function handlePointerDown(event) {
    const strip = thumbStripRef.current;
    if (!strip) return;

    dragRef.current = {
      active: true,
      startX: event.clientX,
      scrollLeft: strip.scrollLeft,
      moved: false,
      pressedIndex: getThumbIndex(event.target),
    };
    strip.setPointerCapture(event.pointerId);
  }

  function handlePointerMove(event) {
    const strip = thumbStripRef.current;
    if (!strip || !dragRef.current.active) return;

    const delta = event.clientX - dragRef.current.startX;
    if (Math.abs(delta) > 4) {
      dragRef.current.moved = true;
    }

    strip.scrollLeft = dragRef.current.scrollLeft - delta;
  }

  function handlePointerUp(event) {
    const strip = thumbStripRef.current;
    if (!strip || !dragRef.current.active) return;

    const { moved, pressedIndex } = dragRef.current;

    dragRef.current.active = false;
    strip.releasePointerCapture(event.pointerId);

    if (!moved && pressedIndex !== null) {
      setSelectedIndex(pressedIndex);
    }
  }

  const isPlaceholder = mainImg?.includes("/placeholders/");

  return (
    <div className="space-y-3">
      <div
        className={MAIN_IMAGE_FRAME_CLASS}
      >
        {!mainLoaded && !error && <GallerySkeleton />}

        <img
          ref={handleImgRef}
          src={mainImg ?? "/placeholder.png"}
          alt="Listing cover"
          loading="eager"
          decoding="async"
          className={cn(
            "h-full w-full cursor-zoom-in object-cover transition-opacity duration-200",
            mainLoaded ? "opacity-100" : "opacity-0",
          )}
          onClick={() => mainImg && openModal(mainImg)}
          onLoad={handleMainLoad}
          onError={() => setError(true)}
        />

        {isPlaceholder && !error && mainLoaded && (
          <div className="absolute top-4 left-3 z-20 rounded-md bg-accent px-2 py-0.5 text-[10px] font-semibold tracking-wide text-on-surface uppercase">
            Not seller photo
          </div>
        )}
      </div>

      <div className="flex items-center gap-1">
        {showThumbArrows && (
          <Button
            type="button"
            variant="secondary"
            size="icon"
            className="size-8 shrink-0 sm:size-9"
            disabled={!scrollState.left}
            onClick={() => scrollThumbs(-1)}
            aria-label="Scroll thumbnails left"
          >
            <ChevronLeft className="size-4" />
          </Button>
        )}

        <div
          ref={thumbStripRef}
          className={cn(
            "flex min-w-0 flex-1 cursor-grab gap-2 overflow-x-auto pb-1 active:cursor-grabbing",
            "[scrollbar-width:none] [-ms-overflow-style:none] [&::-webkit-scrollbar]:hidden",
            "touch-none",
          )}
          onPointerDown={handlePointerDown}
          onPointerMove={handlePointerMove}
          onPointerUp={handlePointerUp}
          onPointerCancel={handlePointerUp}
        >
          {thumbnails.map((img, index) => (
            <GalleryThumb
              key={`${img}-${index}`}
              src={img}
              index={index}
              selected={selectedIndex === index}
              onKeyDown={(event) => handleThumbKeyDown(event, index)}
            />
          ))}
        </div>

        {showThumbArrows && (
          <Button
            type="button"
            variant="secondary"
            size="icon"
            className="size-8 shrink-0 sm:size-9"
            disabled={!scrollState.right}
            onClick={() => scrollThumbs(1)}
            aria-label="Scroll thumbnails right"
          >
            <ChevronRight className="size-4" />
          </Button>
        )}
      </div>
    </div>
  );
}
