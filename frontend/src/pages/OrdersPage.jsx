import { button } from "@material-tailwind/react";
import axios from "axios";
import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";

export default function OrdersPage() {
  const [orders, setOrders] = useState();

  const navigate = useNavigate();

  const navigateToOrderItems = (orderItems) => {
    navigate("/orderItems", { state: orderItems });
  };

  async function fetchOrders() {
    try {
      const res = await axios.get("http://localhost:8080/order", {
        withCredentials: true,
      });

      if (res.status == 200) {
        console.log(res.data);
        setOrders(res.data);
      }
    } catch (error) {
      console.log(error);
    }
  }

  useEffect(() => {
    fetchOrders();
  }, []);

  const formatDate = (dateString) => {
    const date = new Date(dateString);
    const day = String(date.getDate()).padStart(2, "0");
    const month = String(date.getMonth() + 1).padStart(2, "0");
    const year = date.getFullYear();
    return `${day}.${month}.${year}`;
  };

  return (
    <>
      <section class="py-24 h-screen relative bg-gray-900">
        <div class="w-full max-w-7xl mx-auto px-4 md:px-8 ">
          <h2 class="font-manrope font-bold text-3xl lead-10 text-indigo-50 mb-9">
            Order History
          </h2>

          <div class="flex sm:flex-col lg:flex-row sm:items-center justify-between">
            <ul class="flex max-sm:flex-col sm:items-center gap-x-14 gap-y-3">
              <li class="font-medium text-lg leading-8 cursor-pointer text-indigo-600 transition-all duration-500 hover:text-indigo-600">
                All Orders
              </li>
              <li class="font-medium text-lg leading-8 cursor-pointer text-indigo-50 transition-all duration-500 hover:text-indigo-600">
                Summary
              </li>
              <li class="font-medium text-lg leading-8 cursor-pointer text-indigo-50 transition-all duration-500 hover:text-indigo-600">
                Completed
              </li>
              <li class="font-medium text-lg leading-8 cursor-pointer text-indigo-50 transition-all duration-500 hover:text-indigo-600">
                Cancelled
              </li>
            </ul>
            <div class="flex max-sm:flex-col items-center justify-end gap-2 max-lg:mt-5"></div>
          </div>
          {orders &&
            orders.map((order) => (
              <button
                onClick={() => navigateToOrderItems(order.orderItems)}
                className=""
              >
                <div class="mt-7 border border-gray-300 rounded-2xl py-4  transition-all duration-200 ease-in-out hover:-translate-y-1 ">
                  <div class="flex max-md:flex-col items-center justify-between px-3 md:px-11"></div>
                  <svg
                    class="my-3 w-full"
                    xmlns="http://www.w3.org/2000/svg"
                    width="1216"
                    height="2"
                    viewBox="0 0 1216 2"
                    fill="none"
                  >
                    <path d="M0 1H1216" stroke="#D1D5DB" />
                  </svg>

                  <div class="flex max-lg:flex-col items-center gap-8 lg:gap-24 px-3 md:px-11">
                    <div class="grid grid-cols-6 w-full">
                      <div class="col-span-6 sm:col-span-1">
                        <img
                          src="https://pagedone.io/asset/uploads/1705474774.png"
                          alt=""
                          class="max-sm:mx-auto object-cover"
                        />
                      </div>
                      <div class="col-span-4 sm:col-span-3 max-sm:mt-4 sm:pl-8 flex flex-col justify-center max-sm:items-center">
                        <h6 class="font-manrope font-semibold text-2xl leading-9 text-indigo-50 mb-3 whitespace-nowrap">
                          <div>
                            <p class="font-medium text-left text-lg leading-8 text-indigo-50 whitespace-nowrap">
                              Order Number:{" "}
                            </p>
                            <p className="font-medium text-left text-lg leading-8 text-indigo-50 whitespace-nowrap w-50 overflow-hidden hover:overflow-auto">
                              {order.orderNumber}
                            </p>
                          </div>
                        </h6>
                        <p class="font-medium text-left text-lg leading-8 text-indigo-50 whitespace-nowrap ">
                          Order Date : {formatDate(order.createdAt)}
                        </p>
                      </div>
                    </div>
                    <div class="flex items-center justify-between gap-10 w-full  sm:pl-28 lg:pl-0">
                      <div class="flex flex-col justify-center items-start max-sm:items-center">
                        <p class="font-normal text-lg text-gray-500 leading-8 mb-2 text-left whitespace-nowrap">
                          Item count
                        </p>
                        <p class="font-semibold text-lg leading-8 text-green-500 text-left whitespace-nowrap">
                          {order.orderItems.length}
                        </p>
                      </div>
                      <div class="flex flex-col justify-center items-start max-sm:items-center">
                        <p class="font-normal text-lg text-gray-500 leading-8 mb-2 text-left whitespace-nowrap">
                          Order price
                        </p>
                        <p class="font-semibold text-lg leading-8 text-green-500 text-left whitespace-nowrap">
                          {order.totalPrice.toLocaleString("tr-TR")} ₺
                        </p>
                      </div>
                      <div class="flex flex-col justify-center items-start max-sm:items-center">
                        <p class="font-normal text-lg text-gray-500 leading-8 mb-2 text-left whitespace-nowrap">
                          Status
                        </p>
                        <p class="font-semibold text-lg leading-8 text-green-500 text-left whitespace-nowrap">
                          {order.status}
                        </p>
                      </div>
                      <div class="flex flex-col justify-center items-start max-sm:items-center">
                        <p class="font-normal text-lg text-gray-500 leading-8 mb-2 text-left whitespace-nowrap">
                          Delivery Expected by
                        </p>
                        <p class="font-semibold text-lg leading-8 text-indigo-50 text-left whitespace-nowrap">
                          23rd March 2021
                        </p>
                      </div>
                    </div>
                  </div>

                  <svg
                    class="my-3 w-full"
                    xmlns="http://www.w3.org/2000/svg"
                    width="1216"
                    height="2"
                    viewBox="0 0 1216 2"
                    fill="none"
                  >
                    <path d="M0 1H1216" stroke="#D1D5DB" />
                  </svg>
                </div>
              </button>
            ))}
        </div>
        {/* <div class="px-3 md:px-11 flex items-center justify-between max-sm:flex-col-reverse">
          <div class="flex max-sm:flex-col-reverse items-center">
            <button class="flex items-center gap-3 py-10 pr-8 sm:border-r border-gray-300 font-normal text-xl leading-8 text-gray-500 group transition-all duration-500 hover:text-indigo-600">
              <svg
                width="40"
                height="41"
                viewBox="0 0 40 41"
                fill="none"
                xmlns="http://www.w3.org/2000/svg"
              >
                <path
                  class="stroke-gray-600 transition-all duration-500 group-hover:stroke-indigo-600"
                  d="M14.0261 14.7259L25.5755 26.2753M14.0261 26.2753L25.5755 14.7259"
                  stroke=""
                  stroke-width="1.8"
                  stroke-linecap="round"
                  stroke-linejoin="round"
                />
              </svg>
              cancel order
            </button>
            <p class="font-normal text-xl leading-8 text-gray-500 sm:pl-8">
              Payment Is Succesfull
            </p>
          </div>
          <p class="font-medium text-xl leading-8 text-indigo-50 max-sm:py-4">
            {" "}
            <span class="text-gray-500">Total Price: </span> &nbsp;
            {orders.totalPrice.toLocaleString("tr-TR")} ₺
          </p>
        </div> */}
      </section>
    </>
  );
}
