export default function CardShell({ children, onClick }) {
  return (
    <div
      onClick={onClick}
      className="
        bg-neutral-primary-soft
        border border-default
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
