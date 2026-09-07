import { cn } from "@/lib/utils";

/** Main gallery image frame — square, capped by viewport height (not thumb strip). */
export const MAIN_IMAGE_FRAME_CLASS = cn(
  "relative mx-auto aspect-square overflow-hidden rounded-2xl ring-1 ring-surface-3",
  "w-[min(100%,36rem,min(calc(100vw-2rem),calc(100dvh-13rem)))]",
  "lg:max-w-none lg:w-[min(100%,calc(100dvh-11rem))]",
);

export const MAIN_IMAGE_SKELETON_CLASS = cn(
  MAIN_IMAGE_FRAME_CLASS,
  "animate-pulse bg-surface-3",
);

export const GALLERY_THUMB_SKELETON_CLASS =
  "size-20 shrink-0 animate-pulse rounded-lg bg-surface-3 sm:size-24";
