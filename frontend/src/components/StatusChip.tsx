import Chip from "@mui/material/Chip";
import type { ApplicationStatus } from "@/lib/types";

export const STATUS_COLORS: Record<ApplicationStatus, "info" | "warning" | "success" | "error"> = {
  APPLIED: "info",
  INTERVIEW: "warning",
  OFFER: "success",
  REJECTED: "error",
};

// "INTERVIEW" -> "Interview"
export function statusLabel(status: ApplicationStatus) {
  return status.charAt(0) + status.slice(1).toLowerCase();
}

export default function StatusChip({ status }: { status: ApplicationStatus }) {
  return <Chip label={statusLabel(status)} color={STATUS_COLORS[status]} size="small" />;
}
