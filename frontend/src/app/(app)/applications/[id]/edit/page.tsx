"use client";

import { useEffect, useState } from "react";
import Link from "next/link";
import { useParams, useRouter } from "next/navigation";
import Alert from "@mui/material/Alert";
import Button from "@mui/material/Button";
import Typography from "@mui/material/Typography";
import ApplicationForm from "@/components/ApplicationForm";
import Loading from "@/components/Loading";
import { apiFetch, ApiError } from "@/lib/api";
import type { JobApplication, JobApplicationRequest } from "@/lib/types";

export default function EditApplicationPage() {
  const { id } = useParams<{ id: string }>(); // from the [id] folder name
  const router = useRouter();

  const [initialValues, setInitialValues] = useState<JobApplicationRequest | null>(null);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    apiFetch<JobApplication>(`/api/applications/${id}`)
      .then((app) =>
        setInitialValues({
          company: app.company,
          role: app.role,
          location: app.location ?? "",
          status: app.status,
          appliedDate: app.appliedDate,
          notes: app.notes ?? "",
        }),
      )
      .catch((err) => setError(err instanceof ApiError ? err.message : "Could not load application"));
  }, [id]);

  async function handleUpdate(values: JobApplicationRequest) {
    await apiFetch<JobApplication>(`/api/applications/${id}`, {
      method: "PUT",
      body: JSON.stringify(values),
    });
    router.push("/applications");
  }

  return (
    <>
      <Typography variant="h4" fontWeight={700} sx={{ mb: 3 }}>
        Edit application
      </Typography>

      {error && (
        <Alert severity="error" action={<Button component={Link} href="/applications">Back</Button>}>
          {error}
        </Alert>
      )}
      {!error && !initialValues && <Loading />}
      {initialValues && <ApplicationForm initialValues={initialValues} submitLabel="Save changes" onSubmit={handleUpdate} />}
    </>
  );
}
