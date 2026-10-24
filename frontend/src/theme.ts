"use client";

import { createTheme } from "@mui/material/styles";

const theme = createTheme({
  palette: {
    primary: { main: "#3f51b5" },
    background: { default: "#f5f6fa" },
  },
  shape: { borderRadius: 10 },
  typography: {
    fontFamily: "Roboto, system-ui, -apple-system, 'Segoe UI', Arial, sans-serif",
  },
});

export default theme;
