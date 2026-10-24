import { redirect } from "next/navigation";

// "/" just sends you to the dashboard (which sends you to /login if you're not logged in)
export default function Home() {
  redirect("/dashboard");
}
