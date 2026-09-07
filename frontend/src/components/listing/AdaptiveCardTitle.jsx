import { useLayoutEffect, useRef, useState } from "react";

import { CardTitle } from "@/components/ui/card";
import { cn } from "@/lib/utils";

const TITLE_MEASURE_FULL = "1.35rem";
const TITLE_MEASURE_COMPACT = "0.95rem";
const FULL_TITLE_SIZE = "clamp(0.8rem, 4.5cqi, 1.35rem)";
const COMPACT_TITLE_SIZE = "clamp(0.72rem, 3.8cqi, 0.95rem)";

function fitsOnOneLine(title, width) {
  if (!width || !title) return true;

  const measurer = document.createElement("span");
  measurer.style.cssText = [
    "position: absolute",
    "visibility: hidden",
    "pointer-events: none",
    "white-space: nowrap",
    `font-size: ${TITLE_MEASURE_FULL}`,
    "font-weight: 600",
    "letter-spacing: -0.025em",
  ].join(";");
  measurer.textContent = title;

  document.body.appendChild(measurer);
  const fits = measurer.scrollWidth <= width;
  measurer.remove();

  return fits;
}

export default function AdaptiveCardTitle({
  title,
  className,
  linkable = true,
  centered = false,
}) {
  const containerRef = useRef(null);
  const [compact, setCompact] = useState(false);

  useLayoutEffect(() => {
    const container = containerRef.current;
    if (!container) return;

    const measure = () => {
      setCompact(!fitsOnOneLine(title, container.clientWidth));
    };

    measure();

    const observer = new ResizeObserver(measure);
    observer.observe(container);

    return () => observer.disconnect();
  }, [title]);

  return (
    <div ref={containerRef} className={cn("min-w-0", centered && "text-center")}>
      <CardTitle
        className={cn(
          "line-clamp-2 break-words font-semibold tracking-tight text-on-surface leading-tight",
          centered && "text-center",
          className,
        )}
        style={{ fontSize: compact ? COMPACT_TITLE_SIZE : FULL_TITLE_SIZE }}
      >
        {linkable ? (
          <a href="#" className="hover:underline">
            {title}
          </a>
        ) : (
          title
        )}
      </CardTitle>
    </div>
  );
}
