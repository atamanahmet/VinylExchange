import { useEffect, useMemo, useState } from "react";
import { ChevronDown } from "lucide-react";

import { Checkbox } from "@/components/ui/checkbox";
import {
  Collapsible,
  CollapsibleContent,
  CollapsibleTrigger,
} from "@/components/ui/collapsible";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { cn } from "@/lib/utils";

function genreFilterId(id) {
  return `genre-${id}`;
}

function GenreCheckbox({ option, selected, onToggle }) {
  const id = genreFilterId(option.id);

  return (
    <div className="flex items-start gap-2">
      <Checkbox
        id={id}
        className="mt-0.5"
        checked={selected.includes(option.id)}
        onCheckedChange={() => onToggle(option.id)}
      />
      <Label htmlFor={id} className="min-w-0 flex-1 items-start text-left font-normal leading-snug">
        {option.label}
      </Label>
    </div>
  );
}

function ParentGenreRow({
  parent,
  childOptions,
  selected,
  onToggle,
  open,
  onOpenChange,
}) {
  const hasChildren = childOptions.length > 0;

  return (
    <Collapsible open={open} onOpenChange={onOpenChange}>
      <div className="flex items-start gap-1">
        <div className="min-w-0 flex-1">
          <GenreCheckbox option={parent} selected={selected} onToggle={onToggle} />
        </div>
        {hasChildren && (
          <CollapsibleTrigger
            className="mt-0.5 inline-flex size-7 shrink-0 items-center justify-center rounded-md text-muted-foreground transition-colors hover:bg-muted hover:text-foreground"
            aria-label={`Expand ${parent.label}`}
          >
            <ChevronDown
              className={cn(
                "size-4 transition-transform duration-100",
                open && "rotate-180",
              )}
            />
          </CollapsibleTrigger>
        )}
      </div>
      {hasChildren && (
        <CollapsibleContent className="space-y-2 pl-6 pt-2">
          {childOptions.map((child) => (
            <GenreCheckbox
              key={child.id}
              option={child}
              selected={selected}
              onToggle={onToggle}
            />
          ))}
        </CollapsibleContent>
      )}
    </Collapsible>
  );
}

export default function GenreFilterOptions({
  options,
  selected = [],
  onToggle,
  loading = false,
  includeLocal = true,
  onIncludeLocalChange,
  className,
}) {
  const [search, setSearch] = useState("");
  const [expandedParents, setExpandedParents] = useState(() => new Set());

  const normalizedSearch = search.trim().toLowerCase();

  const { parents, childrenByParentId } = useMemo(() => {
    const parentOptions = options.filter((option) => option.parentId == null);
    const childMap = new Map();

    for (const option of options) {
      if (option.parentId == null) continue;
      const siblings = childMap.get(option.parentId) ?? [];
      siblings.push(option);
      childMap.set(option.parentId, siblings);
    }

    return {
      parents: parentOptions,
      childrenByParentId: childMap,
    };
  }, [options]);

  const visibleParents = useMemo(() => {
    if (!normalizedSearch) return parents;

    return parents.filter((parent) => {
      const children = childrenByParentId.get(parent.id) ?? [];
      const parentMatches = parent.label.toLowerCase().includes(normalizedSearch);
      const childMatches = children.some((child) =>
        child.label.toLowerCase().includes(normalizedSearch),
      );
      return parentMatches || childMatches;
    });
  }, [parents, childrenByParentId, normalizedSearch]);

  const getVisibleChildren = (parent) => {
    const children = childrenByParentId.get(parent.id) ?? [];
    if (!normalizedSearch) return children;

    const parentMatches = parent.label.toLowerCase().includes(normalizedSearch);
    if (parentMatches) return children;

    return children.filter((child) =>
      child.label.toLowerCase().includes(normalizedSearch),
    );
  };

  useEffect(() => {
    if (!normalizedSearch) return;

    setExpandedParents((prev) => {
      const next = new Set(prev);
      for (const parent of parents) {
        const children = childrenByParentId.get(parent.id) ?? [];
        const parentMatches = parent.label.toLowerCase().includes(normalizedSearch);
        const childMatches = children.some((child) =>
          child.label.toLowerCase().includes(normalizedSearch),
        );
        if (childMatches && !parentMatches) {
          next.add(parent.id);
        }
      }
      return next;
    });
  }, [normalizedSearch, parents, childrenByParentId]);

  if (loading) {
    return <p className="text-sm text-muted-foreground">Loading genres...</p>;
  }

  if (!options.length) {
    return null;
  }

  return (
    <div className={cn("flex flex-col gap-3", className)}>
      <div className="flex items-start gap-2">
        <Checkbox
          id="include-local-genres"
          className="mt-0.5"
          checked={includeLocal}
          onCheckedChange={(checked) =>
            onIncludeLocalChange?.(checked === true)
          }
        />
        <Label htmlFor="include-local-genres" className="min-w-0 flex-1 items-start text-left font-normal leading-snug">
          Show regional genres
        </Label>
      </div>

      <Input
        type="search"
        value={search}
        onChange={(event) => setSearch(event.target.value)}
        placeholder="Search genres..."
        aria-label="Search genres"
      />

      <div className="max-h-64 space-y-2 overflow-y-auto overscroll-contain pr-1">
        {visibleParents.map((parent) => {
          const visibleChildren = getVisibleChildren(parent);
          const isOpen = expandedParents.has(parent.id);

          return (
            <ParentGenreRow
              key={parent.id}
              parent={parent}
              childOptions={visibleChildren}
              selected={selected}
              onToggle={onToggle}
              open={isOpen}
              onOpenChange={(open) => {
                setExpandedParents((prev) => {
                  const next = new Set(prev);
                  if (open) {
                    next.add(parent.id);
                  } else {
                    next.delete(parent.id);
                  }
                  return next;
                });
              }}
            />
          );
        })}
      </div>
    </div>
  );
}
