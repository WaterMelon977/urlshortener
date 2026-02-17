"use client";

import { useState, useRef, useEffect } from "react";
import Link from "next/link";
import BackgroundBubbles from "./components/BackgroundBubbles";
import TiltCard from "./components/TiltCard";
import { Spinner, CopyIcon, CheckIcon, ErrorPanel } from "./components/UIComponents";

const API = process.env.NEXT_PUBLIC_API_BASE ?? "";

type Mode = "shorten" | "fetch";

interface UserInfo {
  username: string;
  avatar: string;
  provider: string;
}

export default function Home() {
  const [mode, setMode] = useState<Mode>("shorten");
  const [user, setUser] = useState<UserInfo | null>(null);
  const [userLoading, setUserLoading] = useState(true);

  // ── Shorten state ──
  const [longUrl, setLongUrl] = useState("");
  const [shortCode, setShortCode] = useState<string>("");
  const [shortenLoading, setShortenLoading] = useState(false);
  const [shortenError, setShortenError] = useState<string>("");

  // ── Fetch state ──
  const [fetchCode, setFetchCode] = useState("");
  const [resolvedUrl, setResolvedUrl] = useState<string>("");
  const [fetchLoading, setFetchLoading] = useState(false);
  const [fetchError, setFetchError] = useState<string>("");

  // ── Stats state ──
  const [clickCount, setClickCount] = useState<number | null>(null);
  const [statsLoading, setStatsLoading] = useState(false);
  const [statsError, setStatsError] = useState<string>("");

  const [copied, setCopied] = useState(false);

  // ─── User Fetch ─────────────────────────────────────────

  useEffect(() => {
    async function fetchUser() {
      try {
        const res = await fetch(`${API}/api/me`, {
          credentials: "include", // Essential for sending cookies
        });
        if (res.ok) {
          const data = await res.json();
          setUser(data);
        }
      } catch (err) {
        console.error("Failed to fetch user:", err);
      } finally {
        setUserLoading(false);
      }
    }
    fetchUser();
  }, []);

  // ─── Handlers ──────────────────────────────────────────

  async function handleShorten() {
    if (!longUrl.trim()) return;
    setShortenLoading(true);
    setShortenError("");
    setShortCode("");

    try {
      const res = await fetch(`${API}/shorten`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ longUrl }),
      });

      const data = await res.json();

      if (!res.ok) {
        setShortenError(data.message ?? "Something went wrong");
        return;
      }

      setShortCode(data.shortUrl);
    } catch {
      setShortenError("Network error. Is the backend running?");
    } finally {
      setShortenLoading(false);
    }
  }

  async function handleGo() {
    if (!fetchCode.trim()) return;
    setFetchLoading(true);
    setFetchError("");
    setResolvedUrl("");
    setClickCount(null);
    setStatsError("");

    try {
      const res = await fetch(`${API}/${fetchCode}`, {
        headers: { "Accept": "application/json" }
      });

      if (!res.ok) {
        if (res.status === 404) setFetchError("Short code not found");
        else setFetchError("Error fetching URL");
        return;
      }

      const data = await res.json();

      if (data && data.longUrl) {
        setResolvedUrl(data.longUrl);
      } else {
        setFetchError("Invalid response from server.");
      }
    } catch {
      setFetchError("Network error.");
    } finally {
      setFetchLoading(false);
    }
  }

  async function handleStats() {
    if (!fetchCode.trim()) return;
    setStatsLoading(true);
    setStatsError("");
    setClickCount(null);

    try {
      const res = await fetch(`${API}/${fetchCode}/stats`);

      if (!res.ok) {
        if (res.status === 404) setStatsError("Short code not found");
        else setStatsError("Error fetching stats");
        return;
      }

      const data = await res.json();
      setClickCount(data);
    } catch {
      setStatsError("Network error.");
    } finally {
      setStatsLoading(false);
    }
  }

  function copyToClipboard(text: string) {
    navigator.clipboard.writeText(text);
    setCopied(true);
    setTimeout(() => setCopied(false), 1800);
  }

  function switchMode(newMode: Mode) {
    setMode(newMode);
    setShortenError("");
    setShortCode("");
    setFetchError("");
    setResolvedUrl("");
    setClickCount(null);
    setStatsError("");
  }

  // ─── Render ────────────────────────────────────────────

  const heading = mode === "shorten" ? "Shorten URL" : "Fetch Link";

  return (
    <>
      <BackgroundBubbles />

      {/* ── User Session Info (Floating top-right) ── */}
      <div className="fixed top-6 right-6 z-50 fade-in">
        {userLoading ? (
          <div className="glass px-4 py-2 border border-white/10 bg-black/20 text-white/40 text-xs">
            <Spinner />
          </div>
        ) : user ? (
          <div className="glass px-4 py-2.5 flex items-center gap-3 border border-white/20 bg-black/30 backdrop-blur-xl">
            {user.avatar && (
              <img src={user.avatar} alt={user.username} className="w-8 h-8 rounded-full border border-white/20 shadow-sm" />
            )}
            <div className="text-left hidden sm:block">
              <p className="text-xs font-bold text-white tracking-tight">{user.username}</p>
              <p className="text-[10px] text-white/40 uppercase tracking-widest">{user.provider}</p>
            </div>
            <button
              onClick={() => {
                // Logout is usually done by clearing the cookie, which backend can do on a /logout endpoint
                // For now, redirect or just clear state if it's purely client-side logout
                window.location.href = `${API}/logout`; // Assuming standard Spring Security logout
              }}
              className="ml-2 text-xs text-white/40 hover:text-rose-400 transition-colors cursor-pointer"
            >
              Sign out
            </button>
          </div>
        ) : (
          <Link
            href="/login"
            className="glass px-6 py-2.5 bg-emerald-500/10 hover:bg-emerald-500/20 text-emerald-400 text-sm font-bold border border-emerald-500/30 transition-all btn-press"
          >
            Sign In
          </Link>
        )}
      </div>

      <div className="w-full max-w-md fade-in perspective-1000">

        {/* ── Glass Card with Tilt ── */}
        <TiltCard className="glass rounded-3xl px-8 pt-10 pb-8 relative z-10 w-full max-w-lg transform-style-3d">
          {/* Heading */}
          <h1
            className="text-center text-3xl font-bold text-white mb-8 tracking-tight drop-shadow-md font-display translate-z-10"
            style={{ fontFamily: "var(--font-outfit)" }}
          >
            {heading}
          </h1>

          {/* ── Pill Toggle ── */}
          <div className="glass-inner rounded-full p-1 flex mb-8 translate-z-20">
            <button
              onClick={() => switchMode("shorten")}
              className={`flex-1 py-3 text-sm font-semibold rounded-full transition-all duration-300 cursor-pointer ${mode === "shorten"
                ? "bg-emerald-500/90 text-slate-900 shadow-[0_0_15px_rgba(16,185,129,0.4)] scale-[1.02]"
                : "text-slate-300 hover:text-white hover:bg-white/10"
                }`}
            >
              Shorten URL
            </button>
            <button
              onClick={() => switchMode("fetch")}
              className={`flex-1 py-3 text-sm font-semibold rounded-full transition-all duration-300 cursor-pointer ${mode === "fetch"
                ? "bg-emerald-500/90 text-slate-900 shadow-[0_0_15px_rgba(16,185,129,0.4)] scale-[1.02]"
                : "text-slate-300 hover:text-white hover:bg-white/10"
                }`}
            >
              Fetch Link
            </button>
          </div>

          {/* ══════════════ Shorten Mode ══════════════ */}
          {mode === "shorten" && (
            <div className="fade-in space-y-6 translate-z-30">
              {/* Input row with inline button */}
              <div className="glass-inner rounded-full flex items-center pr-1.5 pl-5 focus-within:ring-2 focus-within:ring-emerald-500/30 transition-all duration-300">
                <input
                  type="url"
                  value={longUrl}
                  onChange={(e) => setLongUrl(e.target.value)}
                  onKeyDown={(e) => e.key === "Enter" && handleShorten()}
                  placeholder="https://example.com/very-long-url..."
                  className="flex-1 bg-transparent py-4 text-sm text-white placeholder-slate-400 outline-none"
                  style={{ fontFamily: "var(--font-jetbrains)" }}
                />
                <button
                  onClick={handleShorten}
                  disabled={shortenLoading || !longUrl.trim()}
                  className="shrink-0 px-6 py-2.5 bg-emerald-500/90 hover:bg-emerald-400 disabled:opacity-50 disabled:cursor-not-allowed text-slate-900 text-sm font-bold rounded-full shadow-[0_0_15px_rgba(16,185,129,0.3)] transition-all duration-200 btn-press cursor-pointer uppercase tracking-wider"
                >
                  {shortenLoading ? <Spinner /> : "Shorten"}
                </button>
              </div>

              {/* Success result */}
              {shortCode && (
                <div className="fade-in glass-inner rounded-xl p-6 border border-emerald-500/30" style={{ fontFamily: "var(--font-jetbrains)" }}>
                  <p className="text-[10px] font-bold text-emerald-400 uppercase tracking-widest mb-1 shadow-black/50 drop-shadow-sm">
                    Short Code
                  </p>
                  <div className="text-2xl font-bold text-white mb-5 drop-shadow-md tracking-tight">{shortCode}</div>

                  <p className="text-[10px] font-bold text-emerald-400 uppercase tracking-widest mb-2 shadow-black/50 drop-shadow-sm">
                    Full Link
                  </p>
                  <div className="flex items-center gap-2">
                    <a
                      href={`${API}/${shortCode}`}
                      target="_blank"
                      rel="noopener noreferrer"
                      className="flex-1 px-4 py-3 bg-black/20 hover:bg-black/30 rounded-lg text-emerald-300 text-sm border border-white/5 truncate transition-colors"
                    >
                      {`${API}/${shortCode}`}
                    </a>
                    <button
                      onClick={() => copyToClipboard(`${API}/${shortCode}`)}
                      className="p-3 bg-white/10 hover:bg-emerald-500/20 text-slate-300 hover:text-emerald-400 rounded-lg border border-white/10 transition-all duration-200 btn-press cursor-pointer shrink-0"
                      title="Copy Link"
                    >
                      {copied ? <CheckIcon /> : <CopyIcon />}
                    </button>
                  </div>
                </div>
              )}

              {/* Error */}
              {shortenError && <ErrorPanel message={shortenError} />}
            </div>
          )}

          {/* ══════════════ Fetch Mode ══════════════ */}
          {mode === "fetch" && (
            <div className="fade-in space-y-6 translate-z-30">
              {/* Input row with inline GO button */}
              <div className="glass-inner rounded-full flex items-center pr-1.5 pl-5 focus-within:ring-2 focus-within:ring-emerald-500/30 transition-all duration-300">
                <input
                  type="text"
                  value={fetchCode}
                  onChange={(e) => setFetchCode(e.target.value)}
                  onKeyDown={(e) => e.key === "Enter" && handleGo()}
                  placeholder="Enter short code..."
                  className="flex-1 bg-transparent py-4 text-sm text-white placeholder-slate-400 outline-none"
                  style={{ fontFamily: "var(--font-jetbrains)" }}
                />
                <button
                  onClick={handleGo}
                  disabled={fetchLoading || !fetchCode.trim()}
                  className="shrink-0 px-8 py-2.5 bg-emerald-500/90 hover:bg-emerald-400 disabled:opacity-50 disabled:cursor-not-allowed text-slate-900 text-sm font-bold rounded-full shadow-[0_0_15px_rgba(16,185,129,0.3)] transition-all duration-200 btn-press cursor-pointer uppercase tracking-wider"
                >
                  {fetchLoading ? <Spinner /> : "GO"}
                </button>
              </div>

              {/* Stats button */}
              <button
                onClick={handleStats}
                disabled={statsLoading || !fetchCode.trim()}
                className="w-full py-3.5 bg-cyan-500/90 hover:bg-cyan-400 disabled:opacity-30 disabled:cursor-not-allowed text-slate-900 text-sm font-bold rounded-full shadow-[0_0_15px_rgba(6,182,212,0.4)] transition-all duration-200 btn-press cursor-pointer uppercase tracking-wider"
              >
                {statsLoading ? (
                  <span className="inline-flex items-center gap-2 justify-center">
                    <Spinner /> Loading stats...
                  </span>
                ) : (
                  "View Stats"
                )}
              </button>

              {/* Resolved URL */}
              {resolvedUrl && (
                <div className="fade-in glass-inner rounded-xl p-6 border border-white/10" style={{ fontFamily: "var(--font-jetbrains)" }}>
                  <p className="text-[10px] font-bold text-slate-400 uppercase tracking-widest mb-3">
                    Destination URL
                  </p>
                  <div className="flex items-center gap-2 mb-5">
                    <a
                      href={resolvedUrl}
                      target="_blank"
                      rel="noopener noreferrer"
                      className="flex-1 px-4 py-3 bg-black/20 hover:bg-black/30 rounded-lg text-white text-sm border border-white/5 truncate transition-colors drop-shadow-sm"
                    >
                      {resolvedUrl}
                    </a>
                    <button
                      onClick={() => copyToClipboard(resolvedUrl)}
                      className="p-3 bg-white/10 hover:bg-white/20 text-slate-300 hover:text-white rounded-lg border border-white/10 transition-all duration-200 btn-press cursor-pointer shrink-0"
                      title="Copy URL"
                    >
                      {copied ? <CheckIcon /> : <CopyIcon />}
                    </button>
                  </div>
                  <a
                    href={resolvedUrl}
                    target="_blank"
                    rel="noopener noreferrer"
                    className="block w-full text-center py-3 bg-emerald-500 hover:bg-emerald-400 text-slate-900 text-xs font-bold uppercase tracking-wider rounded-lg shadow-lg shadow-emerald-900/20 transition btn-press cursor-pointer"
                  >
                    Visit URL ↗
                  </a>
                </div>
              )}

              {/* Stats result */}
              {clickCount !== null && (
                <div className="fade-in glass-inner rounded-xl p-6 text-center border border-white/10" style={{ fontFamily: "var(--font-jetbrains)" }}>
                  <p className="text-[10px] font-bold text-slate-400 uppercase tracking-widest mb-2">
                    Total Clicks
                  </p>
                  <p
                    className="text-5xl font-bold text-white drop-shadow-lg"
                  >
                    {clickCount}
                  </p>
                </div>
              )}

              {/* Errors */}
              {fetchError && <ErrorPanel message={fetchError} />}
              {statsError && <ErrorPanel message={statsError} />}
            </div>
          )}
        </TiltCard>

        {/* ── Footer ── */}
        <p className="text-center text-xs text-white/40 mt-8 tracking-widest font-medium uppercase relative z-10">
          Built by Sumanth
        </p>
      </div>
    </>
  );
}

