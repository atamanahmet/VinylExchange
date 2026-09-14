/**
 * Must stay in sync with backend PasswordValidation.java
 * (regex: ^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[@$!%*?&]).{8,64}$).
 * Duplicated across languages by necessity — update both when rules change.
 */
export function getPasswordStrengthChecks(password) {
  const value = password ?? "";

  return {
    length: value.length >= 8 && value.length <= 64,
    lowercase: /[a-z]/.test(value),
    uppercase: /[A-Z]/.test(value),
    digit: /\d/.test(value),
    special: /[@$!%*?&]/.test(value),
  };
}

export function isPasswordStrong(password) {
  const checks = getPasswordStrengthChecks(password);
  return Object.values(checks).every(Boolean);
}

export const PASSWORD_STRENGTH_RULES = [
  { key: "length", label: "8–64 characters" },
  { key: "lowercase", label: "One lowercase letter" },
  { key: "uppercase", label: "One uppercase letter" },
  { key: "digit", label: "One number" },
  { key: "special", label: "One special character (@$!%*?&)" },
];
