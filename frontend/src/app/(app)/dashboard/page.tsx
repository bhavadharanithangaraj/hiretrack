"use client";

import { useEffect, useState } from "react";
import Link from "next/link";
import Alert from "@mui/material/Alert";
import Box from "@mui/material/Box";
import Button from "@mui/material/Button";
import Card from "@mui/material/Card";
import CardContent from "@mui/material/CardContent";
import Typography from "@mui/material/Typography";
import AddIcon from "@mui/icons-material/Add";
import Loading from "@/components/Loading";
import { STATUS_COLORS, statusLabel } from "@/components/StatusChip";
import { useAuth } from "@/context/AuthContext";
import { apiFetch, ApiError } from "@/lib/api";
import { STATUSES, type DashboardResponse } from "@/lib/types";

export default function DashboardPage() {
  const { user } = useAuth();
  const [data, setData] = useState<DashboardResponse | null>(null);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    apiFetch<DashboardResponse>("/api/dashboard")
      .then(setData)
      .catch((err) => setError(err instanceof ApiError ? err.message : "Could not load dashboard"));
  }, []);

  return (
    <>
      <Box sx={{ display: "flex", justifyContent: "space-between", alignItems: "center", mb: 3, gap: 2, flexWrap: "wrap" }}>
        <Box>
          <Typography variant="h4" fontWeight={700}>
            Dashboard
          </Typography>
          <Typography color="text.secondary">Welcome back, {user?.name}</Typography>
        </Box>
        <Button component={Link} href="/applications/new" variant="contained" startIcon={<AddIcon />}>
          Add application
        </Button>
      </Box>

      {error && <Alert severity="error">{error}</Alert>}
      {!error && !data && <Loading />}

      {data && (
        <Box
          sx={{
            display: "grid",
            gap: 2,
            gridTemplateColumns: { xs: "1fr", sm: "repeat(2, 1fr)", md: "repeat(5, 1fr)" },
          }}
        >
          <StatCard label="Total" value={data.total} color="primary.main" />
          {STATUSES.map((status) => (
            <StatCard
              key={status}
              label={statusLabel(status)}
              value={data.countsByStatus[status] ?? 0}
              color={`${STATUS_COLORS[status]}.main`}
            />
          ))}
        </Box>
      )}

      {data?.total === 0 && (
        <Alert severity="info" sx={{ mt: 3 }}>
          No applications yet. Click &quot;Add application&quot; to track your first one.
        </Alert>
      )}
    </>
  );
}

function StatCard({ label, value, color }: { label: string; value: number; color: string }) {
  return (
    <Card sx={{ borderTop: 4, borderColor: color }}>
      <CardContent>
        <Typography color="text.secondary" variant="body2" fontWeight={600}>
          {label}
        </Typography>
        <Typography variant="h3" fontWeight={700} sx={{ color }}>
          {value}
        </Typography>
      </CardContent>
    </Card>
  );
}
