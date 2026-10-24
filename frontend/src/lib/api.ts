// Small fetch wrapper: adds the JWT, parses JSON, and turns error responses into ApiError.

const API_URL = process.env.NEXT_PUBLIC_API_URL ?? "http://localhost:8080";
const TOKEN_KEY = "hiretrack_token";
const USER_KEY = "hiretrack_user";

export class ApiError extends Error {
  status: number;
  fieldErrors: Record<string, string>;

  constructor(status: number, message: string, fieldErrors: Record<string, string> = {}) {
    super(message);
    this.status = status;
    this.fieldErrors = fieldErrors;
  }
}

// ---- token storage (localStorage only exists in the browser) ----

export function getToken(): string | null {
  return typeof window === "undefined" ? null : localStorage.getItem(TOKEN_KEY);
}

export function saveSession(token: string, user: unknown) {
  localStorage.setItem(TOKEN_KEY, token);
  localStorage.setItem(USER_KEY, JSON.stringify(user));
}

export function getStoredUser<T>(): T | null {
  if (typeof window === "undefined") return null;
  const raw = localStorage.getItem(USER_KEY);
  return raw ? (JSON.parse(raw) as T) : null;
}

export function clearSession() {
  localStorage.removeItem(TOKEN_KEY);
  localStorage.removeItem(USER_KEY);
}

// ---- requests ----

export async function apiFetch<T>(path: string, options: RequestInit = {}): Promise<T> {
  const headers = new Headers(options.headers);
  headers.set("Content-Type", "application/json");

  const token = getToken();
  if (token) headers.set("Authorization", `Bearer ${token}`);

  let response: Response;
  try {
    response = await fetch(`${API_URL}${path}`, { ...options, headers });
  } catch {
    throw new ApiError(0, "Cannot reach the server. Is the backend running?");
  }

  // Token missing or expired on a protected route: log out and go to the login page
  if (response.status === 401 && token && !path.startsWith("/api/auth/")) {
    clearSession();
    window.location.href = "/login";
    throw new ApiError(401, "Your session has expired. Please log in again.");
  }

  if (!response.ok) {
    // Our backend always sends { message, fieldErrors } on errors (see ErrorResponse.java)
    const body = await response.json().catch(() => null);
    throw new ApiError(
      response.status,
      body?.message ?? `Request failed (${response.status})`,
      body?.fieldErrors ?? {},
    );
  }

  // 204 No Content (e.g. DELETE) has no body
  return response.status === 204 ? (undefined as T) : ((await response.json()) as T);
}
