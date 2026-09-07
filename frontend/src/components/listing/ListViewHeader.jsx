import { LIST_VIEW_HEADER_CELL, listViewHeaderClass } from "@/utils/listViewLayout";

export default function ListViewHeader({ showPrice = true, showActions = true }) {
  return (
    <div className={listViewHeaderClass({ showPrice, showActions })}>
      <p className={LIST_VIEW_HEADER_CELL}>Cover</p>
      <p className={LIST_VIEW_HEADER_CELL}>Title</p>
      <p className={LIST_VIEW_HEADER_CELL}>Artist</p>
      <p className={LIST_VIEW_HEADER_CELL}>Year</p>
      <p className={LIST_VIEW_HEADER_CELL}>Format</p>
      <p className={LIST_VIEW_HEADER_CELL}>Country</p>
      {showPrice && <p className={LIST_VIEW_HEADER_CELL}>Price</p>}
      {showActions && <div className={LIST_VIEW_HEADER_CELL} aria-hidden="true" />}
    </div>
  );
}
