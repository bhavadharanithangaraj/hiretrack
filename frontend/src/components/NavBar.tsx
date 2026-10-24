"use client";

import Link from "next/link";
import { usePathname, useRouter } from "next/navigation";
import AppBar from "@mui/material/AppBar";
import Box from "@mui/material/Box";
import Button from "@mui/material/Button";
import Toolbar from "@mui/material/Toolbar";
import Typography from "@mui/material/Typography";
import WorkOutlineIcon from "@mui/icons-material/WorkOutline";
import { useAuth } from "@/context/AuthContext";

const LINKS = [
  { href: "/dashboard", label: "Dashboard" },
  { href: "/applications", label: "Applications" },
];

export default function NavBar() {
  const { user, logout } = useAuth();
  const pathname = usePathname();
  const router = useRouter();

  function handleLogout() {
    logout();
    router.replace("/login");
  }

  return (
    <AppBar position="static" elevation={0}>
      <Toolbar sx={{ gap: 1, flexWrap: "wrap" }}>
        <WorkOutlineIcon />
        <Typography variant="h6" sx={{ mr: 2, fontWeight: 700 }}>
          HireTrack
        </Typography>

        {LINKS.map((link) => (
          <Button
            key={link.href}
            component={Link}
            href={link.href}
            color="inherit"
            sx={{ opacity: pathname.startsWith(link.href) ? 1 : 0.75, fontWeight: 600 }}
          >
            {link.label}
          </Button>
        ))}

        <Box sx={{ flexGrow: 1 }} />

        {user && (
          <Typography variant="body2" sx={{ display: { xs: "none", sm: "block" } }}>
            {user.name}
            {user.role === "ROLE_ADMIN" && " (admin)"}
          </Typography>
        )}
        <Button color="inherit" variant="outlined" size="small" onClick={handleLogout}>
          Logout
        </Button>
      </Toolbar>
    </AppBar>
  );
}
