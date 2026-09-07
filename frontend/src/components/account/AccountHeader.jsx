import { useEffect, useState } from "react";
import { Pencil } from "lucide-react";
import { toast } from "sonner";

import { Button } from "@/components/ui/button";
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from "@/components/ui/dialog";
import { Field, FieldGroup, FieldLabel } from "@/components/ui/field";
import { Input } from "@/components/ui/input";
import { useAuthStore } from "@/stores/authStore";

export default function AccountHeader() {
  const user = useAuthStore((state) => state.user);
  const updateEmail = useAuthStore((state) => state.updateEmail);

  const [isOpen, setIsOpen] = useState(false);
  const [emailDraft, setEmailDraft] = useState(user?.email ?? "");
  const [isSaving, setIsSaving] = useState(false);

  useEffect(() => {
    setEmailDraft(user?.email ?? "");
  }, [user?.email]);

  const handleSave = async () => {
    const trimmed = emailDraft.trim();
    if (!trimmed) {
      toast.error("Email cannot be empty.");
      return;
    }

    if (trimmed === user?.email) {
      setIsOpen(false);
      return;
    }

    setIsSaving(true);
    const result = await updateEmail(trimmed);
    setIsSaving(false);

    if (result.success) {
      toast.success("Email updated.");
      setIsOpen(false);
      return;
    }

    toast.error(result.message || "Could not update email.");
  };

  if (!user) return null;

  return (
    <section className="mb-8">
      <h1 className="text-2xl font-semibold text-on-surface mb-4">Your Account</h1>

      <div className="rounded-xl border border-accent-muted bg-surface-1 p-4 sm:p-5">
        <div className="flex flex-col gap-1 sm:flex-row sm:items-center sm:justify-between">
          <div>
            <p className="text-sm text-on-surface-muted">Name</p>
            <p className="text-base font-medium text-on-surface">{user.username}</p>
          </div>

          <div className="mt-3 sm:mt-0 sm:text-right">
            <p className="text-sm text-on-surface-muted">Email</p>
            <div className="flex items-center gap-2 justify-start sm:justify-end">
              <p className="text-base text-on-surface">{user.email}</p>
              <Button
                type="button"
                variant="ghost"
                size="icon-sm"
                onClick={() => setIsOpen(true)}
                aria-label="Edit email"
              >
                <Pencil className="size-4" />
              </Button>
            </div>
          </div>
        </div>
      </div>

      <Dialog open={isOpen} onOpenChange={setIsOpen}>
        <DialogContent className="bg-surface-1 text-on-surface sm:max-w-md">
          <DialogHeader>
            <DialogTitle>Edit email</DialogTitle>
            <DialogDescription className="text-on-surface-muted">
              Update the email address on your account.
            </DialogDescription>
          </DialogHeader>

          <FieldGroup>
            <Field>
              <FieldLabel htmlFor="account-email">Email</FieldLabel>
              <Input
                id="account-email"
                type="email"
                value={emailDraft}
                onChange={(event) => setEmailDraft(event.target.value)}
                autoComplete="email"
              />
            </Field>
          </FieldGroup>

          <DialogFooter>
            <Button
              type="button"
              variant="outline"
              onClick={() => setIsOpen(false)}
              disabled={isSaving}
            >
              Cancel
            </Button>
            <Button type="button" onClick={handleSave} disabled={isSaving}>
              {isSaving ? "Saving..." : "Save"}
            </Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>
    </section>
  );
}
