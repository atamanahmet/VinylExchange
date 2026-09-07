export default function CardShell({ children, onClick }) {
  return (
    <div
      onClick={onClick}
      className="
        bg-surface-1
        border border-surface-3
        rounded-2xl
        shadow-xs
        flex flex-col
        items-stretch
        py-5 px-5
      "
    >
      {children}
    </div>
  );
}
