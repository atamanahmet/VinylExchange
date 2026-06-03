import { useLocation } from "react-router-dom";

export default function OrderItemsPage() {
  const location = useLocation();
  const items = location.state;

  const formatDate = (dateString) => {
    const date = new Date(dateString);
    const day = String(date.getDate()).padStart(2, "0");
    const month = String(date.getMonth() + 1).padStart(2, "0");
    const year = date.getFullYear();
    return `${day}.${month}.${year}`;
  };

  // Calculate totals
  const itemsSubtotal =
    items?.reduce((sum, item) => sum + item.subTotal, 0) || 0;
  const shipping = 50; // You can make this dynamic
  const orderTotal = itemsSubtotal + shipping;

  return (
    <div className="max-w-7xl mx-auto px-4 py-8">
      {/* Order Items Table */}
      <div className="bg-gray-900 border border-gray-700 rounded-lg overflow-hidden">
        {/* Table Header */}
        <div className="grid grid-cols-12 gap-4 px-6 py-4 bg-gray-800 border-b border-gray-700">
          <div className="col-span-5 text-sm font-medium text-gray-400">
            Item
          </div>
          <div className="col-span-2 text-sm font-medium text-gray-400 text-right">
            Cost
          </div>
          <div className="col-span-2 text-sm font-medium text-gray-400 text-center">
            Qty
          </div>
          <div className="col-span-2 text-sm font-medium text-gray-400 text-right">
            Total
          </div>
          <div className="col-span-1 text-sm font-medium text-gray-400 text-right">
            Status
          </div>
        </div>

        {/* Order Items */}
        {items &&
          items.map((item, index) => (
            <div key={index}>
              <div className="grid grid-cols-12 gap-4 px-6 py-6 items-center hover:bg-gray-800/50 transition-colors">
                {/* Item Info */}
                <div className="col-span-5 flex items-center gap-4">
                  <div className="shrink-0 w-16 h-16 bg-gray-800 rounded-md overflow-hidden">
                    <img
                      src={item.listingMainImageUrl || "/placeholder.png"}
                      alt={item.listingTitle}
                      className="w-full h-full object-cover"
                    />
                  </div>
                  <div className="flex-1 min-w-0">
                    <h3 className="text-indigo-400 font-medium text-base truncate hover:text-indigo-300 cursor-pointer">
                      {item.listingTitle}
                    </h3>
                    {item.artistName && (
                      <p className="text-gray-400 text-sm mt-1">
                        {item.artistName}
                      </p>
                    )}
                  </div>
                </div>

                {/* Cost */}
                <div className="col-span-2 text-right">
                  <p className="text-gray-300 font-medium">
                    {item.unitPrice.toLocaleString("tr-TR")} ₺
                  </p>
                </div>

                {/* Quantity */}
                <div className="col-span-2 text-center">
                  <p className="text-gray-300 font-medium">× {item.quantity}</p>
                </div>

                {/* Total */}
                <div className="col-span-2 text-right">
                  <p className="text-green-500 font-semibold">
                    {item.subTotal.toLocaleString("tr-TR")} ₺
                  </p>
                </div>

                {/* Status/Delivery */}
                <div className="col-span-1 text-right">
                  <span className="inline-block px-2 py-1 text-xs font-medium text-green-400 bg-green-900/30 rounded">
                    Active
                  </span>
                </div>
              </div>

              {/* Delivery Info (expandable row) */}
              <div className="px-6 pb-4 ml-20">
                <p className="text-sm text-gray-400">
                  <span className="text-gray-500">Delivery Expected by:</span>{" "}
                  <span className="text-indigo-300 font-medium">
                    23rd March 2025
                  </span>
                </p>
              </div>

              {/* Divider */}
              {index < items.length - 1 && (
                <div className="border-b border-gray-700"></div>
              )}
            </div>
          ))}
      </div>

      {/* Order Summary */}
      <div className="mt-6 bg-gray-900 border border-gray-700 rounded-lg p-6">
        <div className="max-w-md ml-auto space-y-3">
          {/* Items Subtotal */}
          <div className="flex justify-between items-center">
            <span className="text-gray-400">Items Subtotal:</span>
            <span className="text-gray-300 font-medium">
              {itemsSubtotal.toLocaleString("tr-TR")} ₺
            </span>
          </div>

          {/* Shipping */}
          <div className="flex justify-between items-center">
            <span className="text-gray-400">Shipping:</span>
            <span className="text-gray-300 font-medium">
              {shipping.toLocaleString("tr-TR")} ₺
            </span>
          </div>

          {/* Divider */}
          <div className="border-t border-gray-700 pt-3"></div>

          {/* Order Total */}
          <div className="flex justify-between items-center">
            <span className="text-lg font-semibold text-indigo-50">
              Order Total:
            </span>
            <span className="text-lg font-bold text-green-500">
              {orderTotal.toLocaleString("tr-TR")} ₺
            </span>
          </div>

          {/* Paid Amount */}
          <div className="border-t border-gray-700 pt-3 mt-3">
            <div className="flex justify-between items-center">
              <span className="text-gray-400">Paid By Customer:</span>
              <span className="text-green-400 font-semibold">
                {orderTotal.toLocaleString("tr-TR")} ₺
              </span>
            </div>
          </div>
        </div>
      </div>

      {/* Action Buttons */}
      <div className="mt-6 flex justify-between items-center">
        <button className="px-6 py-2 border border-gray-600 text-gray-300 rounded-lg hover:bg-gray-800 transition-colors">
          Refund
        </button>
        <p className="text-sm text-gray-500 italic">
          This order is no longer editable.
        </p>
      </div>
    </div>
  );
}
