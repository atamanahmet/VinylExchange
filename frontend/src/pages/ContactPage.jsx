import { useEffect, useMemo, useState } from "react";
import { Mail, MessageSquare } from "lucide-react";
import { toast } from "sonner";

import axios from "@/api/axiosInstance";
import { Button } from "@/components/ui/button";
import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from "@/components/ui/card";
import { Field, FieldGroup, FieldLabel } from "@/components/ui/field";
import { Input } from "@/components/ui/input";
import { Textarea } from "@/components/ui/textarea";
import { useAuthStore } from "@/stores/authStore";

const SUPPORT_EMAIL = "contact@vexchange.com";

const SELECT_CLASS =
  "h-9 w-full min-w-0 rounded-lg border border-accent-muted bg-surface-form px-2.5 py-1 text-sm text-on-surface transition-colors outline-none focus-visible:border-brand-active focus-visible:ring-3 focus-visible:ring-brand-active/50";

const ISSUE_OPTIONS = [
  { value: "", label: "Select an issue...", orderRelated: false },
  {
    value: "ORDER_STATUS",
    label: "Where is my order? / tracking",
    orderRelated: true,
  },
  {
    value: "PAYMENT",
    label: "Payment or charge issue",
    orderRelated: true,
  },
  {
    value: "REFUND",
    label: "Refund request",
    orderRelated: true,
  },
  {
    value: "NOT_RECEIVED",
    label: "Item not received",
    orderRelated: true,
  },
  {
    value: "NOT_AS_DESCRIBED",
    label: "Item not as described",
    orderRelated: true,
  },
  {
    value: "CANCEL",
    label: "Cancel an order",
    orderRelated: true,
  },
  {
    value: "RETURN",
    label: "Return or exchange",
    orderRelated: true,
  },
  {
    value: "SHIPPING",
    label: "Shipping delay or damage",
    orderRelated: true,
  },
  {
    value: "DISPUTE",
    label: "Buyer / seller dispute",
    orderRelated: true,
  },
  {
    value: "LISTING",
    label: "Listing or selling help",
    orderRelated: false,
  },
  {
    value: "ACCOUNT",
    label: "Account or login issue",
    orderRelated: false,
  },
  {
    value: "PAYOUT",
    label: "Seller payout issue",
    orderRelated: false,
  },
  {
    value: "OTHER",
    label: "Something else",
    orderRelated: false,
  },
];

function getIssueOption(value) {
  return ISSUE_OPTIONS.find((option) => option.value === value);
}

export default function ContactPage() {
  const user = useAuthStore((state) => state.user);

  const [pageContent, setPageContent] = useState(null);
  const [loading, setLoading] = useState(true);
  const [form, setForm] = useState({
    name: user?.username ?? "",
    email: user?.email ?? "",
    issueType: "",
    orderNumber: "",
    message: "",
  });

  const selectedIssue = useMemo(
    () => getIssueOption(form.issueType),
    [form.issueType],
  );

  useEffect(() => {
    setForm((current) => ({
      ...current,
      name: user?.username ?? current.name,
      email: user?.email ?? current.email,
    }));
  }, [user]);

  useEffect(() => {
    async function fetchContactContent() {
      try {
        const res = await axios.get("/api/cms/contact");
        if (res.status === 200) {
          setPageContent(res.data);
        }
      } catch (error) {
        console.error("Error fetching contact page:", error);
      } finally {
        setLoading(false);
      }
    }

    fetchContactContent();
  }, []);

  const updateField = (field, value) => {
    setForm((current) => ({ ...current, [field]: value }));
  };

  const handleSubmit = (event) => {
    event.preventDefault();

    const { name, email, issueType, orderNumber, message } = form;
    const issue = getIssueOption(issueType);

    if (!name.trim() || !email.trim() || !issueType || !message.trim()) {
      toast.error("Fill in all required fields.");
      return;
    }

    if (issue?.orderRelated && !orderNumber.trim()) {
      toast.error("Order number is required for this issue type.");
      return;
    }

    const subject = `Support: ${issue?.label ?? issueType}`;
    const body = [
      `Name: ${name.trim()}`,
      `Email: ${email.trim()}`,
      `Issue: ${issue?.label ?? issueType}`,
      orderNumber.trim() ? `Order number: ${orderNumber.trim()}` : null,
      "",
      message.trim(),
    ]
      .filter(Boolean)
      .join("\n");

    const mailto = `mailto:${SUPPORT_EMAIL}?subject=${encodeURIComponent(subject)}&body=${encodeURIComponent(body)}`;
    window.location.href = mailto;
    toast.success("Opening your email app to send the message.");
  };

  return (
    <div className="min-h-screen bg-surface-base text-on-surface">
      <div className="w-full max-w-4xl mx-auto px-4 md:px-8 lg:px-10 py-8 text-left">
        <header className="mb-8">
          <h1 className="text-2xl font-semibold text-on-surface">
            {loading ? "Customer support" : pageContent?.header || "Customer support"}
          </h1>
          <p className="mt-2 text-sm text-on-surface-muted max-w-2xl">
            {loading
              ? "Loading support information..."
              : pageContent?.textContent ||
                "Questions about orders, listings, or payments? Send us a message and we will get back to you."}
          </p>
        </header>

        <div className="grid gap-6 lg:grid-cols-[minmax(0,1fr)_minmax(0,1.2fr)]">
          <div className="space-y-4">
            <Card className="border-surface-4">
              <CardHeader>
                <CardTitle className="text-base">Contact details</CardTitle>
                <CardDescription>We usually reply within 1–2 business days.</CardDescription>
              </CardHeader>
              <CardContent className="space-y-4">
                <a
                  href={`mailto:${SUPPORT_EMAIL}`}
                  className="flex items-center gap-3 rounded-lg border border-surface-4 bg-surface-2/80 px-4 py-3 text-sm text-on-surface hover:bg-surface-3 transition-colors"
                >
                  <Mail className="size-4 text-brand shrink-0" />
                  {SUPPORT_EMAIL}
                </a>
                <div className="flex items-start gap-3 rounded-lg border border-surface-4 bg-surface-2/80 px-4 py-3 text-sm text-on-surface-muted">
                  <MessageSquare className="size-4 text-brand shrink-0 mt-0.5" />
                  <p>
                    Pick the issue type so we can route your request faster.
                    Order-related issues need an order number.
                  </p>
                </div>
              </CardContent>
            </Card>
          </div>

          <Card className="border-surface-4">
            <CardHeader>
              <CardTitle className="text-base">Send a message</CardTitle>
              <CardDescription>
                Your message opens in your email app addressed to support.
              </CardDescription>
            </CardHeader>
            <CardContent>
              <form onSubmit={handleSubmit}>
                <FieldGroup className="gap-4">
                  <Field>
                    <FieldLabel htmlFor="contact-name">Name</FieldLabel>
                    <Input
                      id="contact-name"
                      value={form.name}
                      onChange={(event) => updateField("name", event.target.value)}
                      required
                    />
                  </Field>

                  <Field>
                    <FieldLabel htmlFor="contact-email">Email</FieldLabel>
                    <Input
                      id="contact-email"
                      type="email"
                      value={form.email}
                      onChange={(event) => updateField("email", event.target.value)}
                      required
                    />
                  </Field>

                  <Field>
                    <FieldLabel htmlFor="contact-issue">What do you need help with?</FieldLabel>
                    <select
                      id="contact-issue"
                      className={SELECT_CLASS}
                      value={form.issueType}
                      onChange={(event) => updateField("issueType", event.target.value)}
                      required
                    >
                      {ISSUE_OPTIONS.map((option) => (
                        <option key={option.value || "placeholder"} value={option.value}>
                          {option.label}
                        </option>
                      ))}
                    </select>
                  </Field>

                  <Field>
                    <FieldLabel htmlFor="contact-order-number">
                      Order number
                      {selectedIssue?.orderRelated ? (
                        <span className="text-brand"> *</span>
                      ) : (
                        <span className="text-on-surface-muted font-normal"> (optional)</span>
                      )}
                    </FieldLabel>
                    <Input
                      id="contact-order-number"
                      value={form.orderNumber}
                      onChange={(event) => updateField("orderNumber", event.target.value)}
                      placeholder="e.g. 10482"
                      required={Boolean(selectedIssue?.orderRelated)}
                    />
                  </Field>

                  <Field>
                    <FieldLabel htmlFor="contact-message">Message</FieldLabel>
                    <Textarea
                      id="contact-message"
                      rows={6}
                      value={form.message}
                      onChange={(event) => updateField("message", event.target.value)}
                      placeholder="Add any details that will help us resolve your issue..."
                      required
                    />
                  </Field>
                </FieldGroup>

                <Button type="submit" className="mt-6 w-full sm:w-auto">
                  Send message
                </Button>
              </form>
            </CardContent>
          </Card>
        </div>
      </div>
    </div>
  );
}
