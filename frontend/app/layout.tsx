import type { Metadata } from "next";
import { ReduxProvider } from './providers';
import ThemeRegistry from './theme-registry';
import "./globals.css";

export const metadata: Metadata = {
  title: "Finance Monkey",
  description: "Personal finance management application",
};

export default function RootLayout({
  children,
}: Readonly<{
  children: React.ReactNode;
}>) {
  return (
    <html lang="en">
      <body className="antialiased">
        <ReduxProvider>
          <ThemeRegistry>{children}</ThemeRegistry>
        </ReduxProvider>
      </body>
    </html>
  );
}
