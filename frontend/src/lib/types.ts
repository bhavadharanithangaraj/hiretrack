// TypeScript versions of the backend DTOs

export const STATUSES = ["APPLIED", "INTERVIEW", "OFFER", "REJECTED"] as const;
export type ApplicationStatus = (typeof STATUSES)[number];

export type Role = "ROLE_USER" | "ROLE_ADMIN";

export interface AuthResponse {
  token: string;
  name: string;
  email: string;
  role: Role;
}

export type CurrentUser = Omit<AuthResponse, "token">;

export interface JobApplication {
  id: number;
  company: string;
  role: string;
  location: string | null;
  status: ApplicationStatus;
  appliedDate: string; // "YYYY-MM-DD"
  notes: string | null;
  createdAt: string;
  updatedAt: string;
}

export interface JobApplicationRequest {
  company: string;
  role: string;
  location: string;
  status: ApplicationStatus;
  appliedDate: string;
  notes: string;
}

export interface DashboardResponse {
  total: number;
  countsByStatus: Record<ApplicationStatus, number>;
}
