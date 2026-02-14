"use client";

import { useState } from "react";

const API = process.env.NEXT_PUBLIC_API_BASE ?? "";

type Mode = "shorten" | "fetch";

export default function Home() {
  const [mode, setMode] = useState<Mode>("shorten");

  // ── Shorten state ──
  const [longUrl, setLongUrl] = useState("");
  const [shortCode, setShortCode] = useState<string | null>(null);
  const [shortenLoading, setShortenLoading] = useState(false);
  const [shortenError, setShortenError] = useState<string | null>(null);

  // ── Fetch state ──
  const [fetchCode, setFetchCode] = useState("");
  const [resolvedUrl, setResolvedUrl] = useState<string | null>(null);
  const [fetchLoading, setFetchLoading] = useState(false);
  const [fetchError, setFetchError] = useState<string | null>(null);

  // ── Stats state ──
  const [clickCount, setClickCount] = useState<number | null>(null);
  const [statsLoading, setStatsLoading] = useState(false);
  const [statsError, setStatsError] = useState<string | null>(null);

  const [copied, setCopied] = useState(false);

  // ─── Handlers ──────────────────────────────────────────

  async function handleShorten() {
    if (!longUrl.trim()) return;
    setShortenLoading(true);
    setShortenError(null);
    setShortCode(null);

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

      setShortCode(data.shortCode);
    } catch {
      setShortenError("Network error. Is the backend running?");
    } finally {
      setShortenLoading(false);
    }
  }

  async function handleGo() {
    if (!fetchCode.trim()) return;
    setFetchLoading(true);
    setFetchError(null);
    setResolvedUrl(null);
    setClickCount(null);
    setStatsError(null);

    try {
      const res = await fetch(`${API}/url/${fetchCode}`, {
        redirect: "manual",
      });

      const location = res.headers.get("Location");

      if (location) {
        setResolvedUrl(location);
      } else {
        setFetchError("Could not resolve — short code may be invalid.");
      }
    } catch {
      setFetchError("Network error. Is the backend running?");
    } finally {
      setFetchLoading(false);
    }
  }

  async function handleStats() {
    if (!fetchCode.trim()) return;
    setStatsLoading(true);
    setStatsError(null);
    setClickCount(null);

    try {
      const res = await fetch(`${API}/url/${fetchCode}/stats`);

      if (!res.ok) {
        setStatsError("Could not fetch stats.");
        return;
      }

      const count = await res.json();
      setClickCount(count);
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
    setShortenError(null);
    setShortCode(null);
    setFetchError(null);
    setResolvedUrl(null);
    setClickCount(null);
    setStatsError(null);
  }

  // ─── Render ────────────────────────────────────────────

  const heading = mode === "shorten" ? "Shorten URL" : "Fetch Link";

  return (
    <div className="w-full max-w-md fade-in">
      {/* ── Glass Card ── */}
      <div className="glass rounded-3xl px-8 pt-10 pb-8">
        {/* Heading */}
        <h1
          className="text-center text-2xl font-bold text-slate-800 mb-7 tracking-tight"
          style={{ fontFamily: "var(--font-outfit)" }}
        >
          {heading}
        </h1>

        {/* ── Pill Toggle ── */}
        <div className="glass-inner rounded-full p-1 flex mb-7">
          <button
            onClick={() => switchMode("shorten")}
            className={`flex-1 py-2 text-sm font-semibold rounded-full transition-all duration-300 cursor-pointer ${mode === "shorten"
                ? "bg-white/70 text-slate-800 shadow-sm"
                : "text-slate-400 hover:text-slate-500"
              }`}
          >
            Shorten URL
          </button>
          <button
            onClick={() => switchMode("fetch")}
            className={`flex-1 py-2 text-sm font-semibold rounded-full transition-all duration-300 cursor-pointer ${mode === "fetch"
                ? "bg-white/70 text-slate-800 shadow-sm"
                : "text-slate-400 hover:text-slate-500"
              }`}
          >
            Fetch Link
          </button>
        </div>

        {/* ══════════════ Shorten Mode ══════════════ */}
        {mode === "shorten" && (
          <div className="fade-in space-y-4">
            {/* Input row with inline button */}
            <div className="glass-inner rounded-full flex items-center pr-1.5 pl-4">
              <input
                type="url"
                value={longUrl}
                onChange={(e) => setLongUrl(e.target.value)}
                onKeyDown={(e) => e.key === "Enter" && handleShorten()}
                placeholder="https://example.com/very-long-url..."
                className="flex-1 bg-transparent py-3 text-sm text-slate-700 placeholder-slate-400/60 outline-none"
              />
              <button
                onClick={handleShorten}
                disabled={shortenLoading || !longUrl.trim()}
                className="shrink-0 px-5 py-2 bg-white/70 hover:bg-white disabled:opacity-40 disabled:cursor-not-allowed text-slate-700 text-sm font-semibold rounded-full border border-white/60 shadow-sm transition-all duration-200 btn-press cursor-pointer"
              >
                {shortenLoading ? <Spinner /> : "Shorten"}
              </button>
            </div>

            {/* Success result */}
            {shortCode && (
              <div className="fade-in glass-inner rounded-2xl p-4 space-y-3">
                <p className="text-xs font-semibold text-emerald-700/80 uppercase tracking-wider">
                  Your short link
                </p>
                <div className="flex items-center gap-2">
                  <code
                    className="flex-1 px-3 py-2 bg-white/50 rounded-lg text-slate-700 text-sm border border-white/40 truncate"
                    style={{ fontFamily: "var(--font-jetbrains)" }}
                  >
                    {`${API}/url/${shortCode}`}
                  </code>
                  <button
                    onClick={() => copyToClipboard(`${API}/url/${shortCode}`)}
                    className="shrink-0 px-3 py-2 bg-white/70 hover:bg-white text-slate-600 text-xs font-semibold rounded-lg border border-white/50 shadow-sm transition btn-press cursor-pointer"
                  >
                    {copied ? "✓ Copied" : "Copy"}
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
          <div className="fade-in space-y-4">
            {/* Input row with inline GO button */}
            <div className="glass-inner rounded-full flex items-center pr-1.5 pl-4">
              <input
                type="text"
                value={fetchCode}
                onChange={(e) => setFetchCode(e.target.value)}
                onKeyDown={(e) => e.key === "Enter" && handleGo()}
                placeholder="Enter short code..."
                className="flex-1 bg-transparent py-3 text-sm text-slate-700 placeholder-slate-400/60 outline-none"
                style={{ fontFamily: "var(--font-jetbrains)" }}
              />
              <button
                onClick={handleGo}
                disabled={fetchLoading || !fetchCode.trim()}
                className="shrink-0 px-5 py-2 bg-white/70 hover:bg-white disabled:opacity-40 disabled:cursor-not-allowed text-slate-700 text-sm font-semibold rounded-full border border-white/60 shadow-sm transition-all duration-200 btn-press cursor-pointer"
              >
                {fetchLoading ? <Spinner /> : "GO"}
              </button>
            </div>

            {/* Stats button */}
            <button
              onClick={handleStats}
              disabled={statsLoading || !fetchCode.trim()}
              className="w-full py-2.5 bg-white/30 hover:bg-white/50 disabled:opacity-40 disabled:cursor-not-allowed text-slate-600 text-sm font-semibold rounded-full border border-white/40 transition-all duration-200 btn-press cursor-pointer"
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
              <div className="fade-in glass-inner rounded-2xl p-4 space-y-3">
                <p className="text-xs font-semibold text-emerald-700/80 uppercase tracking-wider">
                  Original URL
                </p>
                <p className="text-sm text-slate-700 break-all leading-relaxed">
                  {resolvedUrl}
                </p>
                <div className="flex gap-2">
                  <a
                    href={resolvedUrl}
                    target="_blank"
                    rel="noopener noreferrer"
                    className="flex-1 text-center py-2 bg-white/60 hover:bg-white text-slate-700 text-xs font-semibold rounded-lg border border-white/50 shadow-sm transition btn-press"
                  >
                    Open in new tab ↗
                  </a>
                  <button
                    onClick={() => copyToClipboard(resolvedUrl)}
                    className="flex-1 py-2 bg-white/60 hover:bg-white text-slate-700 text-xs font-semibold rounded-lg border border-white/50 shadow-sm transition btn-press cursor-pointer"
                  >
                    {copied ? "✓ Copied" : "Copy URL"}
                  </button>
                </div>
              </div>
            )}

            {/* Stats result */}
            {clickCount !== null && (
              <div className="fade-in glass-inner rounded-2xl p-4 text-center">
                <p className="text-xs font-semibold text-slate-500 uppercase tracking-wider mb-1">
                  Click Count
                </p>
                <p
                  className="text-3xl font-bold text-slate-800"
                  style={{ fontFamily: "var(--font-jetbrains)" }}
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
      </div>

      {/* ── Footer ── */}
      <p className="text-center text-xs text-slate-400 mt-5 tracking-wide">
        Built by Sumanth
      </p>
    </div>
  );
}

// ─── Sub-components ──────────────────────────────────────

function Spinner() {
  return (
    <svg className="animate-spin h-4 w-4 inline-block" viewBox="0 0 24 24" fill="none">
      <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="3" />
      <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.37 0 0 5.37 0 12h4z" />
    </svg>
  );
}

function ErrorPanel({ message }: { message: string }) {
  return (
    <div className="fade-in bg-rose-100/40 border border-rose-200/50 rounded-2xl px-4 py-3">
      <p className="text-sm text-rose-600 font-medium">{message}</p>
    </div>
  );
}
