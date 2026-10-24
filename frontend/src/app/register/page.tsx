"use client";

import { useEffect, useState, type FormEvent } from "react";
import Link from "next/link";
import { useRouter } from "next/navigation";
import Alert from "@mui/material/Alert";
import Button from "@mui/material/Button";
import CircularProgress from "@mui/material/CircularProgress";
import Stack from "@mui/material/Stack";
import TextField from "@mui/material/TextField";
import Typography from "@mui/material/Typography";
import AuthCard from "@/components/AuthCard";
import { useAuth } from "@/context/AuthContext";
import { ApiError } from "@/lib/api";

interface Form {
  name: string;
  email: string;
  password: string;
  confirmPassword: string;
}

type Errors = Partial<Record<keyof Form, string>>;

// Same rules as RegisterRequest.java, so most mistakes are caught before calling the API
function validate(form: Form): Errors {
  const errors: Errors = {};
  if (!form.name.trim()) errors.name = "Name is required";
  if (!form.email.trim()) errors.email = "Email is required";
  else if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(form.email.trim())) errors.email = "Email must be valid";
  if (form.password.length < 8 || form.password.length > 72) errors.password = "Password must be 8-72 characters";
  if (form.confirmPassword !== form.password) errors.confirmPassword = "Passwords do not match";
  return errors;
}

export default function RegisterPage() {
  const { user, ready, register } = useAuth();
  const router = useRouter();

  const [form, setForm] = useState<Form>({ name: "", email: "", password: "", confirmPassword: "" });
  const [errors, setErrors] = useState<Errors>({});
  const [serverError, setServerError] = useState<string | null>(null);
  const [submitting, setSubmitting] = useState(false);

  useEffect(() => {
    if (ready && user) router.replace("/dashboard");
  }, [ready, user, router]);

  function update(field: keyof Form, value: string) {
    setForm((prev) => ({ ...prev, [field]: value }));
    setErrors((prev) => ({ ...prev, [field]: undefined }));
  }

  async function handleSubmit(event: FormEvent) {
    event.preventDefault();
    const validationErrors = validate(form);
    setErrors(validationErrors);
    if (Object.keys(validationErrors).length > 0) return;

    setSubmitting(true);
    setServerError(null);
    try {
      await register(form.name, form.email, form.password);
      router.replace("/dashboard");
    } catch (err) {
      if (err instanceof ApiError) {
        setServerError(err.message);
        setErrors(err.fieldErrors); // e.g. { email: "Email must be valid" } from the backend
      } else {
        setServerError("Registration failed");
      }
      setSubmitting(false);
    }
  }

  return (
    <AuthCard title="Create an account">
      <form onSubmit={handleSubmit} noValidate>
        <Stack spacing={2}>
          {serverError && <Alert severity="error">{serverError}</Alert>}
          <TextField
            label="Name"
            value={form.name}
            onChange={(e) => update("name", e.target.value)}
            error={!!errors.name}
            helperText={errors.name}
            autoComplete="name"
            autoFocus
            fullWidth
          />
          <TextField
            label="Email"
            type="email"
            value={form.email}
            onChange={(e) => update("email", e.target.value)}
            error={!!errors.email}
            helperText={errors.email}
            autoComplete="email"
            fullWidth
          />
          <TextField
            label="Password"
            type="password"
            value={form.password}
            onChange={(e) => update("password", e.target.value)}
            error={!!errors.password}
            helperText={errors.password ?? "At least 8 characters"}
            autoComplete="new-password"
            fullWidth
          />
          <TextField
            label="Confirm password"
            type="password"
            value={form.confirmPassword}
            onChange={(e) => update("confirmPassword", e.target.value)}
            error={!!errors.confirmPassword}
            helperText={errors.confirmPassword}
            autoComplete="new-password"
            fullWidth
          />
          <Button type="submit" variant="contained" size="large" disabled={submitting}>
            {submitting ? <CircularProgress size={24} color="inherit" /> : "Register"}
          </Button>
          <Typography variant="body2" textAlign="center">
            Already have an account? <Link href="/login">Log in</Link>
          </Typography>
        </Stack>
      </form>
    </AuthCard>
  );
}
