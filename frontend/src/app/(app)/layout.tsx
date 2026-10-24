"use client";

import { useEffect, type ReactNode } from "react";
import { useRouter } from "next/navigation";
import Container from "@mui/material/Container";
import Loading from "@/components/Loading";
import NavBar from "@/components/NavBar";
import { useAuth } from "@/context/AuthContext";

// The "(app)" folder is a route group: it doesn't appear in the URL,
// but every page inside it (dashboard, applications) gets this layout and login check.
export default function AppLayout({ children }: { children: ReactNode }) {
  const { user, ready } = useAuth();
  const router = useRouter();

  useEffect(() => {
    if (ready && !user) router.replace("/login");
  }, [ready, user, router]);

  if (!ready || !user) return <Loading />;

  return (
    <>
      <NavBar />
      <Container maxWidth="lg" sx={{ py: 4 }}>
        {children}
      </Container>
    </>
  );
}
