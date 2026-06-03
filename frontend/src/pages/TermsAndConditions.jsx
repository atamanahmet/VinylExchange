export default function TermsAndConditions() {
  return (
    <div className="min-h-screen w-auto bg-gray-900 text-white p-6 md:p-12 text-left">
      <div className="max-w-4xl mx-auto  rounded-lg shadow-lg p-8">
        <h1 className="text-3xl font-bold mb-6">Terms and Conditions</h1>

        <p className="mb-4 text-gray-300">
          <strong>Effective Date:</strong> 11.29.2025
        </p>

        <h2 className="text-xl font-semibold mt-4 mb-2">
          1. Acceptance of Terms
        </h2>
        <p className="mb-4 text-gray-300">
          By using VexChange (“Platform”), you agree to these Terms and
          Conditions and our Privacy Policy. If you do not agree, you must not
          use the Platform.
        </p>

        <h2 className="text-xl font-semibold mt-4 mb-2">
          2. Platform Services
        </h2>
        <p className="mb-4 text-gray-300">VexChange allows users to:</p>
        <ul className="list-disc list-inside mb-4 text-gray-300">
          <li>Buy and sell vinyl records for a fee.</li>
          <li>
            Trade vinyl records with other users without fees, unless additional
            money is involved.
          </li>
        </ul>

        <h2 className="text-xl font-semibold mt-4 mb-2">
          3. Commissions and Fees
        </h2>
        <ul className="list-disc list-inside mb-4 text-gray-300">
          <li>
            <strong>Sales:</strong> All sales transactions are subject to a
            commission of 10% of the sale price.
          </li>
          <li>
            <strong>Trades:</strong> Trading vinyl between users is
            commission-free.
          </li>
          <li>
            <strong>Trades with additional payment:</strong> If one user adds
            extra money to a trade, the commission applies only to the money
            portion. Vinyl items exchanged remain commission-free.
          </li>
        </ul>

        <h2 className="text-xl font-semibold mt-4 mb-2">4. User Accounts</h2>
        <p className="mb-4 text-gray-300">
          Users must create an account to buy, sell, or trade vinyl records. You
          are responsible for keeping your account credentials secure and all
          activity under your account.
        </p>

        <h2 className="text-xl font-semibold mt-4 mb-2">5. Payments</h2>
        <p className="mb-4 text-gray-300">
          Payments for purchases are processed securely through [Payment
          Processor]. Sellers are responsible for accurate pricing,
          descriptions, and shipping of sold items. Trades are arranged directly
          between users; the Platform is not responsible for items exchanged.
        </p>

        <h2 className="text-xl font-semibold mt-4 mb-2">
          6. Prohibited Activities
        </h2>
        <ul className="list-disc list-inside mb-4 text-gray-300">
          <li>Listing illegal, counterfeit, or stolen items.</li>
          <li>Attempting to bypass Platform fees.</li>
          <li>Engaging in harassment, fraud, or illegal activity.</li>
        </ul>

        <h2 className="text-xl font-semibold mt-4 mb-2">
          7. Limitation of Liability
        </h2>
        <p className="mb-4 text-gray-300">
          The Platform is a marketplace connecting users; we are not responsible
          for the quality, condition, or delivery of items sold or traded. Users
          trade at their own risk.
        </p>

        <h2 className="text-xl font-semibold mt-4 mb-2">8. Termination</h2>
        <p className="mb-4 text-gray-300">
          The Platform may suspend or terminate accounts for violations of these
          Terms.
        </p>

        <h2 className="text-xl font-semibold mt-4 mb-2">9. Changes to Terms</h2>
        <p className="mb-4 text-gray-300">
          We may update these Terms at any time. Changes take effect when
          posted. Continued use constitutes acceptance.
        </p>

        <h2 className="text-xl font-semibold mt-4 mb-2">10. Contact</h2>
        <p className="mb-4 text-gray-300">
          Questions or concerns? Contact us at{" "}
          <a
            href="mailto:contact@vexchange.com"
            className="text-blue-500 hover:underline"
          >
            contact@vexchange.com
          </a>
          .
        </p>
      </div>
    </div>
  );
}
