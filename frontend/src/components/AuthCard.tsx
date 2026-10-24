import type { ReactNode } from "react";
import Box from "@mui/material/Box";
import Paper from "@mui/material/Paper";
import Typography from "@mui/material/Typography";
import WorkOutlineIcon from "@mui/icons-material/WorkOutline";

// Centered card used by the Login and Register pages
export default function AuthCard({ title, children }: { title: string; children: ReactNode }) {
  return (
    <Box sx={{ minHeight: "100vh", display: "flex", alignItems: "center", justifyContent: "center", p: 2 }}>
      <Paper sx={{ p: 4, width: "100%", maxWidth: 420 }} elevation={3}>
        <Box sx={{ display: "flex", alignItems: "center", gap: 1, mb: 1, color: "primary.main" }}>
          <WorkOutlineIcon />
          <Typography variant="h6" fontWeight={700}>
            HireTrack
          </Typography>
        </Box>
        <Typography variant="h5" fontWeight={600} sx={{ mb: 3 }}>
          {title}
        </Typography>
        {children}
      </Paper>
    </Box>
  );
}
