"use client";

import { useRouter } from "next/navigation";
import Typography from "@mui/material/Typography";
import ApplicationForm from "@/components/ApplicationForm";
import { apiFetch } from "@/lib/api";
import type { JobApplication, JobApplicationRequest } from "@/lib/types";

export default function NewApplicationPage() {
  const router = useRouter();

  async function handleCreate(values: JobApplicationRequest) {
    await apiFetch<JobApplication>("/api/applications", {
      method: "POST",
      body: JSON.stringify(values),
    });
    router.push("/applications");
  }

  return (
    <>
      <Typography variant="h4" fontWeight={700} sx={{ mb: 3 }}>
        Add application
      </Typography>
      <ApplicationForm submitLabel="Save" onSubmit={handleCreate} />
    </>
  );
}
