import type { Metadata } from "next";
import { Outfit, JetBrains_Mono } from "next/font/google";
import "./globals.css";

const outfit = Outfit({
  variable: "--font-outfit",
  subsets: ["latin"],
  weight: ["400", "500", "600", "700"],
});

const jetbrains = JetBrains_Mono({
  variable: "--font-jetbrains",
  subsets: ["latin"],
  weight: ["400", "500"],
});

export const metadata: Metadata = {
  title: "SnipLink — URL Shortener",
  description: "Shorten, share, and track your URLs.",
};

export default function RootLayout({
  children,
}: Readonly<{
  children: React.ReactNode;
}>) {
  return (
    <html lang="en">
      <body className={`${outfit.variable} ${jetbrains.variable} antialiased`}>
        {/* 3D glass orbs */}
        <div className="orb orb-1" />
        <div className="orb orb-2" />

        <main className="relative z-10 min-h-screen flex items-center justify-center px-4 py-12">
          {children}
        </main>
      </body>
    </html>
  );
}
