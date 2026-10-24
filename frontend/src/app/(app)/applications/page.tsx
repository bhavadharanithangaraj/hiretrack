"use client";

import { useEffect, useState } from "react";
import Link from "next/link";
import Alert from "@mui/material/Alert";
import Box from "@mui/material/Box";
import Button from "@mui/material/Button";
import Dialog from "@mui/material/Dialog";
import DialogActions from "@mui/material/DialogActions";
import DialogContent from "@mui/material/DialogContent";
import DialogContentText from "@mui/material/DialogContentText";
import DialogTitle from "@mui/material/DialogTitle";
import IconButton from "@mui/material/IconButton";
import MenuItem from "@mui/material/MenuItem";
import Paper from "@mui/material/Paper";
import Table from "@mui/material/Table";
import TableBody from "@mui/material/TableBody";
import TableCell from "@mui/material/TableCell";
import TableContainer from "@mui/material/TableContainer";
import TableHead from "@mui/material/TableHead";
import TableRow from "@mui/material/TableRow";
import TextField from "@mui/material/TextField";
import Tooltip from "@mui/material/Tooltip";
import Typography from "@mui/material/Typography";
import AddIcon from "@mui/icons-material/Add";
import DeleteOutlineIcon from "@mui/icons-material/DeleteOutline";
import EditOutlinedIcon from "@mui/icons-material/EditOutlined";
import Loading from "@/components/Loading";
import StatusChip, { statusLabel } from "@/components/StatusChip";
import { apiFetch, ApiError } from "@/lib/api";
import { STATUSES, type ApplicationStatus, type JobApplication } from "@/lib/types";

type Filter = ApplicationStatus | "ALL";

// "2026-10-20" -> "20 Oct 2026" (parsed as a local date, so it never shifts by a day)
function formatDate(isoDate: string) {
  const [year, month, day] = isoDate.split("-").map(Number);
  return new Date(year, month - 1, day).toLocaleDateString(undefined, {
    day: "numeric",
    month: "short",
    year: "numeric",
  });
}

export default function ApplicationsPage() {
  const [filter, setFilter] = useState<Filter>("ALL");
  const [applications, setApplications] = useState<JobApplication[] | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [toDelete, setToDelete] = useState<JobApplication | null>(null);
  const [deleting, setDeleting] = useState(false);
  const [reloadKey, setReloadKey] = useState(0);

  // Re-fetch whenever the filter changes (or after a delete)
  useEffect(() => {
    let cancelled = false; // ignore a slow response if the filter changed in the meantime
    setApplications(null);
    setError(null);

    const query = filter === "ALL" ? "" : `?status=${filter}`;
    apiFetch<JobApplication[]>(`/api/applications${query}`)
      .then((data) => !cancelled && setApplications(data))
      .catch((err) => !cancelled && setError(err instanceof ApiError ? err.message : "Could not load applications"));

    return () => {
      cancelled = true;
    };
  }, [filter, reloadKey]);

  async function confirmDelete() {
    if (!toDelete) return;
    setDeleting(true);
    try {
      await apiFetch<void>(`/api/applications/${toDelete.id}`, { method: "DELETE" });
      setToDelete(null);
      setReloadKey((key) => key + 1);
    } catch (err) {
      setError(err instanceof ApiError ? err.message : "Could not delete application");
      setToDelete(null);
    } finally {
      setDeleting(false);
    }
  }

  return (
    <>
      <Box sx={{ display: "flex", justifyContent: "space-between", alignItems: "center", mb: 3, gap: 2, flexWrap: "wrap" }}>
        <Typography variant="h4" fontWeight={700}>
          Applications
        </Typography>
        <Box sx={{ display: "flex", gap: 2, alignItems: "center" }}>
          <TextField
            select
            size="small"
            label="Status"
            value={filter}
            onChange={(e) => setFilter(e.target.value as Filter)}
            sx={{ minWidth: 160 }}
          >
            <MenuItem value="ALL">All statuses</MenuItem>
            {STATUSES.map((status) => (
              <MenuItem key={status} value={status}>
                {statusLabel(status)}
              </MenuItem>
            ))}
          </TextField>
          <Button component={Link} href="/applications/new" variant="contained" startIcon={<AddIcon />}>
            Add
          </Button>
        </Box>
      </Box>

      {error && (
        <Alert severity="error" sx={{ mb: 2 }}>
          {error}
        </Alert>
      )}

      {!error && !applications && <Loading />}

      {applications && applications.length === 0 && (
        <Alert severity="info">
          {filter === "ALL"
            ? "No applications yet. Click \"Add\" to create one."
            : `No applications with status "${statusLabel(filter)}".`}
        </Alert>
      )}

      {applications && applications.length > 0 && (
        <TableContainer component={Paper}>
          <Table>
            <TableHead>
              <TableRow>
                <TableCell>Company</TableCell>
                <TableCell>Role</TableCell>
                <TableCell>Location</TableCell>
                <TableCell>Status</TableCell>
                <TableCell>Applied</TableCell>
                <TableCell align="right">Actions</TableCell>
              </TableRow>
            </TableHead>
            <TableBody>
              {applications.map((app) => (
                <TableRow key={app.id} hover>
                  <TableCell sx={{ fontWeight: 600 }}>{app.company}</TableCell>
                  <TableCell>{app.role}</TableCell>
                  <TableCell>{app.location || "—"}</TableCell>
                  <TableCell>
                    <StatusChip status={app.status} />
                  </TableCell>
                  <TableCell>{formatDate(app.appliedDate)}</TableCell>
                  <TableCell align="right" sx={{ whiteSpace: "nowrap" }}>
                    <Tooltip title="Edit">
                      <IconButton component={Link} href={`/applications/${app.id}/edit`} aria-label="Edit">
                        <EditOutlinedIcon />
                      </IconButton>
                    </Tooltip>
                    <Tooltip title="Delete">
                      <IconButton onClick={() => setToDelete(app)} color="error" aria-label="Delete">
                        <DeleteOutlineIcon />
                      </IconButton>
                    </Tooltip>
                  </TableCell>
                </TableRow>
              ))}
            </TableBody>
          </Table>
        </TableContainer>
      )}

      <Dialog open={!!toDelete} onClose={() => !deleting && setToDelete(null)}>
        <DialogTitle>Delete application?</DialogTitle>
        <DialogContent>
          <DialogContentText>
            This will permanently delete your application to <strong>{toDelete?.company}</strong> ({toDelete?.role}).
          </DialogContentText>
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setToDelete(null)} disabled={deleting}>
            Cancel
          </Button>
          <Button onClick={confirmDelete} color="error" variant="contained" disabled={deleting}>
            {deleting ? "Deleting..." : "Delete"}
          </Button>
        </DialogActions>
      </Dialog>
    </>
  );
}
