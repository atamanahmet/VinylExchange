import { useState, useEffect } from "react";
import axios from "axios";
import SkeletonAboutPage from "../comps/Skeletons/SkeletonAboutPage";

export default function About() {
  const [pageContent, setPageContent] = useState(null);
  const [loading, setLoading] = useState(true);

  async function fetchAboutPageContent() {
    try {
      const res = await axios.get("http://localhost:8080/api/cms/about", {
        withCredentials: true,
      });
      if (res.status === 200) {
        setPageContent(res.data);
      }
    } catch (error) {
      console.error("Error fetching about page:", error);
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => {
    fetchAboutPageContent();
  }, []);

  if (loading) {
    return <SkeletonAboutPage />;
  }

  if (!pageContent) {
    return (
      <div className="min-h-screen flex items-center justify-center bg-gray-900">
        <div className="text-center">
          <p className="text-white text-xl mb-4">Failed to load content</p>
          <button
            onClick={fetchAboutPageContent}
            className="px-6 py-3 bg-indigo-600 text-white rounded-lg hover:bg-indigo-700"
          >
            Retry
          </button>
        </div>
      </div>
    );
  }

  return (
    <div
      className="relative h-237 overflow-hidden -mt-12  flex justify-center"
      style={{ height: "calc(100vh - 64px)" }}
    >
      <div className="absolute inset-0 -z-10">
        <img
          src={pageContent.backgroundImagePath}
          alt="Background"
          className="h-230 object-cover absolute mt-7"
        />
        <div className="absolute inset-0 bg-linear-to-r from-black/85 via-black/65 to-black/45"></div>
      </div>

      <div className="relative z-10 max-w-7xl mx-auto px-6 py-20 lg:py-32 -mt-5">
        <div className="max-w-4xl">
          <h1 className="text-5xl md:text-6xl lg:text-7xl font-bold text-white mb-8 leading-tight">
            {pageContent.header}
          </h1>

          <div className="w-24 h-1 bg-indigo-600 mb-8"></div>

          <p className="text-xl md:text-2xl text-gray-200 leading-relaxed mb-12 font-light">
            {pageContent.textContent}
          </p>

          <div className="flex flex-col sm:flex-row gap-4">
            <a
              href="/"
              className="inline-flex items-center justify-center px-8 py-4 text-lg font-semibold text-white bg-indigo-600 rounded-lg hover:bg-indigo-700 transition-all duration-200 shadow-lg hover:shadow-xl hover:scale-105"
            >
              Browse Collection
              <svg
                className="w-5 h-5 ml-2"
                fill="none"
                stroke="currentColor"
                viewBox="0 0 24 24"
              >
                <path
                  strokeLinecap="round"
                  strokeLinejoin="round"
                  strokeWidth="2"
                  d="M13 7l5 5m0 0l-5 5m5-5H6"
                />
              </svg>
            </a>

            <a
              href="/contact"
              className="inline-flex items-center justify-center px-8 py-4 text-lg font-semibold text-white border-2 border-white/30 rounded-lg hover:bg-white/10 hover:border-white/50 transition-all duration-200"
            >
              Get in Touch
            </a>
          </div>

          <div className="grid grid-cols-1 md:grid-cols-3 gap-6 mt-15">
            <div className="bg-white/10 backdrop-blur-sm p-6 rounded-lg border border-white/20">
              <div className="text-indigo-400 text-3xl mb-3 flex justify-center">
                <svg
                  width="50px"
                  height="50px"
                  viewBox="0 0 24 24"
                  fill="#4f46e5"
                  xmlns="http://www.w3.org/2000/svg"
                >
                  <path
                    fillRule="evenodd"
                    clipRule="evenodd"
                    d="M10.0905 11.9629L19.3632 8.63087L20.9996 7.95235V7.49236C20.9996 6.37238 20.9996 5.4331 20.9118 4.68472C20.8994 4.57895 20.8848 4.4738 20.8686 4.37569C20.7841 3.86441 20.6348 3.38745 20.3465 2.98917C20.2024 2.79002 20.0235 2.61055 19.8007 2.45628C19.7589 2.42736 19.7156 2.39932 19.6707 2.3722L19.6617 2.36679C18.8901 1.90553 18.0228 1.93852 17.1293 2.14305C16.2652 2.34086 15.194 2.74368 13.8803 3.23763L11.5959 4.09656C10.9801 4.32806 10.4584 4.52419 10.049 4.72734C9.61332 4.94348 9.23805 5.1984 8.95662 5.57828C8.67519 5.95817 8.55831 6.36756 8.50457 6.81203C8.45406 7.22978 8.45408 7.7378 8.4541 8.33743V12.6016L10.0905 11.9629Z"
                    fill="#4f46e5"
                  />
                  <g opacity="0.5">
                    <path
                      d="M8.45455 16.1305C7.90347 15.8136 7.24835 15.6298 6.54545 15.6298C4.58735 15.6298 3 17.0558 3 18.8148C3 20.5738 4.58735 21.9998 6.54545 21.9998C8.50355 21.9998 10.0909 20.5738 10.0909 18.8148L10.0909 11.9627L8.45455 12.6014V16.1305Z"
                      fill="#4f46e5"
                    />
                    <path
                      d="M19.3636 8.63067V14.1705C18.8126 13.8536 18.1574 13.6698 17.4545 13.6698C15.4964 13.6698 13.9091 15.0958 13.9091 16.8548C13.9091 18.6138 15.4964 20.0398 17.4545 20.0398C19.4126 20.0398 21 18.6138 21 16.8548L21 7.95215L19.3636 8.63067Z"
                      fill="#4f46e5"
                    />
                  </g>
                </svg>
              </div>
              <h3 className="text-white font-semibold text-lg mb-2">
                Rare Finds
              </h3>
              <p className="text-gray-300 text-sm">
                Discover unique pressings you won't find anywhere else
              </p>
            </div>

            <div className="bg-white/10 backdrop-blur-sm p-6 rounded-lg border border-white/20">
              <div className="text-indigo-400 text-3xl mb-3 flex justify-center">
                {" "}
                <svg
                  fill="#4f46e5"
                  version="1.1"
                  id="Capa_1"
                  xmlns="http://www.w3.org/2000/svg"
                  xmlnsXlink="http://www.w3.org/1999/xlink"
                  width="50px"
                  height="50px"
                  viewBox="0 0 355.768 355.768"
                  xmlSpace="preserve"
                >
                  <g>
                    <g>
                      <path
                        d="M204.937,137.949c4.626-3.445,7.287-8.47,7.297-13.794c0-5.309-2.646-10.341-7.251-13.792l-68.969-51.785
			c-3.207-2.409-6.548-3.644-9.889-3.644c-6.503,0-13.091,5.2-13.091,15.138v10.506c0,1.589-1.287,2.869-2.867,2.869H22.074
			c-11.093,0-20.121,9.029-20.121,20.122v40.116c0,11.09,9.028,20.122,20.121,20.122h88.078c0.764,0,1.445,0.276,1.973,0.812
			c0.515,0.521,0.8,1.201,0.792,1.976l-0.097,11.539c-0.071,6.022,2.211,9.603,4.141,11.562c2.354,2.377,5.497,3.672,8.864,3.672
			c3.341,0,6.66-1.219,9.866-3.626L204.937,137.949z M110.159,146.56H22.074c-1.589,0-2.876-1.295-2.876-2.867v-40.116
			c0-1.572,1.287-2.869,2.876-2.869h88.078c11.083,0,20.111-9.029,20.111-20.122c0,0,0-1.066,0-2.376
			c0-1.313,2.829-0.254,6.32,2.369l51.714,38.829c3.493,2.623,3.483,6.865-0.005,9.473l-51.849,38.781
			c-3.494,2.615-6.314,3.476-6.305,1.927l0.023-2.803c0.056-5.403-1.998-10.461-5.776-14.277
			C120.602,148.654,115.539,146.56,110.159,146.56z"
                      />
                      <path
                        d="M240.762,166.063c-2.356-2.361-5.5-3.656-8.866-3.656c-3.341,0-6.657,1.216-9.872,3.625l-69.23,51.801
			c-4.628,3.428-7.292,8.455-7.299,13.776c0,5.327,2.648,10.359,7.254,13.812l68.966,51.766c3.21,2.427,6.551,3.646,9.892,3.646
			c6.5,0,13.092-5.199,13.092-15.138v-10.506c0-1.574,1.284-2.869,2.863-2.869h88.088c11.08,0,20.114-9.028,20.114-20.124v-40.096
			c0-11.096-9.034-20.124-20.114-20.124h-88.088c-0.756,0-1.441-0.274-1.975-0.822c-0.514-0.519-0.798-1.214-0.787-1.991
			l0.091-11.526C244.971,171.619,242.687,168.024,240.762,166.063z M335.665,209.222c1.579,0,2.869,1.279,2.869,2.869v40.101
			c0,1.59-1.29,2.884-2.869,2.884h-88.083c-11.085,0-20.118,9.029-20.118,20.104c0,0,0,1.066,0,2.376
			c0,1.315-2.824,0.254-6.317-2.366l-51.714-38.831c-3.491-2.62-3.483-6.865,0.008-9.471l51.848-38.78
			c3.494-2.615,6.312-3.469,6.303-1.92l-0.021,2.813c-0.056,5.388,1.995,10.44,5.763,14.274c3.794,3.823,8.852,5.941,14.239,5.941
			h88.093V209.222z"
                      />
                      <path
                        d="M122.08,249.693H7.901c-4.362,0-7.901,2.655-7.901,5.926c0,3.27,3.54,5.926,7.901,5.926h114.17
			c4.359,0,7.899-2.656,7.899-5.926C129.971,252.349,126.439,249.693,122.08,249.693z"
                      />
                      <path
                        d="M122.08,212.177H7.901c-4.362,0-7.901,2.656-7.901,5.926c0,3.271,3.54,5.927,7.901,5.927h114.17
			c4.359,0,7.899-2.656,7.899-5.927C129.971,214.833,126.439,212.177,122.08,212.177z"
                      />
                      <path
                        d="M347.877,128.075H233.699c-4.362,0-7.901,2.653-7.901,5.924c0,3.27,3.539,5.926,7.901,5.926h114.168
			c4.362,0,7.901-2.656,7.901-5.926C355.768,130.729,352.239,128.075,347.877,128.075z"
                      />
                      <path
                        d="M347.877,90.549H233.699c-4.362,0-7.901,2.653-7.901,5.926c0,3.27,3.539,5.923,7.901,5.923h114.168
			c4.362,0,7.901-2.653,7.901-5.923C355.768,93.202,352.239,90.549,347.877,90.549z"
                      />
                    </g>
                  </g>
                </svg>
              </div>
              <h3 className="text-white font-semibold text-lg mb-2">
                Trade & Swap
              </h3>
              <p className="text-gray-300 text-sm">
                Connect with collectors who share your passion
              </p>
            </div>

            <div className="bg-white/10 backdrop-blur-sm p-6 rounded-lg border border-white/20">
              <div className="text-indigo-400 text-3xl mb-3 flex justify-center">
                <svg
                  version="1.0"
                  xmlns="http://www.w3.org/2000/svg"
                  width="50px"
                  height="50px"
                  viewBox="0 0 1000.000000 1000.000000"
                  preserveAspectRatio="xMidYMid meet"
                >
                  <g
                    transform="translate(0.000000,1000.000000) scale(0.100000,-0.100000)"
                    fill="#4338ca"
                    stroke="none"
                  >
                    <path
                      d="M4695 9989 c-671 -42 -1308 -213 -1905 -510 -521 -260 -913 -542
                  -1320 -949 -414 -413 -695 -807 -955 -1335 -128 -260 -183 -393 -265 -645
                  -309 -941 -329 -1972 -55 -2923 89 -309 170 -518 320 -822 260 -529 541 -922
                  955 -1335 405 -405 797 -688 1312 -945 877 -438 1854 -605 2829 -485 1538 191
                  2886 1074 3692 2420 87 145 280 538 342 695 307 779 417 1632 315 2452 -52
                  422 -159 841 -315 1238 -102 260 -310 657 -478 913 -306 463 -732 912 -1189
                  1253 -939 700 -2118 1051 -3283 978z m750 -704 c638 -68 1228 -266 1774 -594
                  306 -185 545 -373 822 -650 277 -277 465 -516 650 -822 651 -1084 795 -2392
                  393 -3588 -165 -490 -436 -978 -754 -1360 l-53 -63 -1174 1173 -1175 1174 -39
                  83 c-108 228 -200 609 -225 923 l-7 86 -71 28 c-181 70 -347 206 -436 358
                  l-45 76 -250 0 c-137 0 -297 -4 -355 -9 l-105 -10 -1093 1093 -1093 1092 43
                  39 c66 59 291 226 403 298 531 343 1116 563 1730 652 87 13 233 28 400 40 103
                  8 525 -4 660 -19z m-1793 -3589 c-130 -136 -238 -313 -296 -488 -60 -180 -59
                  -168 -63 -865 -2 -354 -6 -643 -9 -643 -3 0 -30 16 -60 36 -89 59 -163 165
                  -198 283 -14 48 -16 153 -16 913 l0 858 -38 0 c-59 0 -186 -36 -251 -70 -280
                  -149 -371 -493 -200 -750 47 -69 113 -130 186 -172 l52 -29 3 -382 3 -382 27
                  -80 c41 -119 109 -228 197 -314 69 -68 109 -94 455 -291 209 -119 381 -221
                  383 -226 2 -7 -187 -352 -303 -551 l-25 -43 384 0 383 0 269 478 c147 262 269
                  479 271 480 1 2 62 -18 136 -45 l133 -48 2 -410 c1 -225 2 -420 3 -432 l0 -23
                  330 0 330 0 0 536 0 535 50 52 49 52 976 -975 976 -975 -43 -39 c-67 -60 -263
                  -205 -389 -288 -526 -345 -1124 -572 -1744 -662 -383 -56 -847 -56 -1230 0
                  -563 81 -1112 278 -1604 573 -306 185 -545 373 -822 650 -277 277 -465 516
                  -650 822 -651 1083 -794 2385 -394 3584 162 487 436 982 755 1364 l53 63 1005
                  -1005 1006 -1006 -82 -85z"
                    />
                    <path
                      d="M5390 7795 c0 -3 8 -47 17 -97 21 -116 24 -414 4 -503 -7 -33 -39
                  -148 -71 -255 -76 -256 -84 -295 -84 -420 1 -202 57 -345 188 -475 110 -109
                  214 -159 417 -199 l26 -6 6 -142 c29 -671 270 -1263 577 -1418 442 -223 874
                  442 926 1428 6 124 8 133 28 137 112 22 184 45 260 83 222 111 346 325 346
                  596 0 115 -12 173 -89 433 -33 111 -65 238 -72 284 -14 99 -6 369 14 479 8 41
                  13 77 11 79 -2 2 -21 -8 -43 -22 -78 -52 -157 -184 -196 -327 -15 -56 -19
                  -129 -24 -510 -6 -397 -8 -449 -24 -485 -26 -58 -73 -102 -136 -128 l-56 -22
                  -730 -3 c-756 -3 -820 0 -894 38 -51 26 -97 77 -117 130 -15 39 -19 103 -24
                  495 -6 433 -7 453 -29 520 -26 80 -81 188 -114 225 -52 58 -117 105 -117 85z"
                    />
                    <path
                      d="M2581 3074 c-89 -24 -191 -84 -262 -154 -100 -99 -158 -219 -174
                  -357 l-7 -63 597 0 596 0 -6 58 c-19 150 -96 300 -202 391 -62 53 -162 105
                  -239 125 -77 20 -230 20 -303 0z"
                    />
                    <path
                      d="M235 2340 c-4 -6 -16 -8 -26 -5 -35 11 -69 -42 -39 -60 20 -12 93
                  -15 107 -4 18 15 16 44 -3 63 -18 19 -30 20 -39 6z m-27 -37 c-4 -22 -22 -20
                  -26 1 -2 10 3 16 13 16 10 0 15 -7 13 -17z m60 0 c-2 -10 -10 -18 -18 -18 -8
                  0 -16 8 -18 18 -2 12 3 17 18 17 15 0 20 -5 18 -17z"
                    />
                  </g>
                </svg>
              </div>
              <h3 className="text-white font-semibold text-lg mb-2">
                No Nonsense
              </h3>
              <p className="text-gray-300 text-sm">
                Just records, music, and pure vinyl obsession
              </p>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}
