"use client";

import { useState, type FormEvent } from "react";
import Link from "next/link";
import Alert from "@mui/material/Alert";
import Box from "@mui/material/Box";
import Button from "@mui/material/Button";
import CircularProgress from "@mui/material/CircularProgress";
import MenuItem from "@mui/material/MenuItem";
import Paper from "@mui/material/Paper";
import Stack from "@mui/material/Stack";
import TextField from "@mui/material/TextField";
import { statusLabel } from "@/components/StatusChip";
import { ApiError } from "@/lib/api";
import { STATUSES, type JobApplicationRequest } from "@/lib/types";

type Errors = Partial<Record<keyof JobApplicationRequest, string>>;

// Today as "YYYY-MM-DD" in the user's local time zone ("en-CA" happens to use that format)
function today() {
  return new Date().toLocaleDateString("en-CA");
}

function emptyForm(): JobApplicationRequest {
  return { company: "", role: "", location: "", status: "APPLIED", appliedDate: today(), notes: "" };
}

// Mirrors the Bean Validation rules in JobApplicationRequest.java
function validate(form: JobApplicationRequest): Errors {
  const errors: Errors = {};
  if (!form.company.trim()) errors.company = "Company is required";
  else if (form.company.length > 100) errors.company = "Company must be at most 100 characters";
  if (!form.role.trim()) errors.role = "Role is required";
  else if (form.role.length > 100) errors.role = "Role must be at most 100 characters";
  if (form.location.length > 100) errors.location = "Location must be at most 100 characters";
  if (!form.appliedDate) errors.appliedDate = "Applied date is required";
  else if (form.appliedDate > today()) errors.appliedDate = "Applied date cannot be in the future";
  if (form.notes.length > 2000) errors.notes = "Notes must be at most 2000 characters";
  return errors;
}

interface Props {
  initialValues?: JobApplicationRequest;
  submitLabel: string;
  onSubmit: (values: JobApplicationRequest) => Promise<void>;
}

// Used by both the "Add" and "Edit" pages
export default function ApplicationForm({ initialValues, submitLabel, onSubmit }: Props) {
  const [form, setForm] = useState<JobApplicationRequest>(() => initialValues ?? emptyForm());
  const [errors, setErrors] = useState<Errors>({});
  const [serverError, setServerError] = useState<string | null>(null);
  const [submitting, setSubmitting] = useState(false);

  function update(field: keyof JobApplicationRequest, value: string) {
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
      await onSubmit(form);
    } catch (err) {
      if (err instanceof ApiError) {
        setServerError(err.message);
        setErrors(err.fieldErrors);
      } else {
        setServerError("Something went wrong");
      }
      setSubmitting(false);
    }
  }

  return (
    <Paper sx={{ p: { xs: 2, sm: 4 } }}>
      <form onSubmit={handleSubmit} noValidate>
        <Stack spacing={2.5}>
          {serverError && <Alert severity="error">{serverError}</Alert>}

          <Box sx={{ display: "grid", gap: 2.5, gridTemplateColumns: { xs: "1fr", sm: "1fr 1fr" } }}>
            <TextField
              label="Company"
              value={form.company}
              onChange={(e) => update("company", e.target.value)}
              error={!!errors.company}
              helperText={errors.company}
              required
              autoFocus
            />
            <TextField
              label="Role"
              placeholder="e.g. Frontend Developer"
              value={form.role}
              onChange={(e) => update("role", e.target.value)}
              error={!!errors.role}
              helperText={errors.role}
              required
            />
            <TextField
              label="Location"
              placeholder="e.g. Remote, Bangalore"
              value={form.location}
              onChange={(e) => update("location", e.target.value)}
              error={!!errors.location}
              helperText={errors.location}
            />
            <TextField
              select
              label="Status"
              value={form.status}
              onChange={(e) => update("status", e.target.value)}
              error={!!errors.status}
              helperText={errors.status}
              required
            >
              {STATUSES.map((status) => (
                <MenuItem key={status} value={status}>
                  {statusLabel(status)}
                </MenuItem>
              ))}
            </TextField>
            <TextField
              label="Applied date"
              type="date"
              value={form.appliedDate}
              onChange={(e) => update("appliedDate", e.target.value)}
              error={!!errors.appliedDate}
              helperText={errors.appliedDate}
              required
              slotProps={{ inputLabel: { shrink: true }, htmlInput: { max: today() } }}
            />
          </Box>

          <TextField
            label="Notes"
            value={form.notes}
            onChange={(e) => update("notes", e.target.value)}
            error={!!errors.notes}
            helperText={errors.notes ?? `${form.notes.length}/2000`}
            multiline
            minRows={4}
          />

          <Box sx={{ display: "flex", gap: 2, justifyContent: "flex-end" }}>
            <Button component={Link} href="/applications" disabled={submitting}>
              Cancel
            </Button>
            <Button type="submit" variant="contained" disabled={submitting}>
              {submitting ? <CircularProgress size={24} color="inherit" /> : submitLabel}
            </Button>
          </Box>
        </Stack>
      </form>
    </Paper>
  );
}
