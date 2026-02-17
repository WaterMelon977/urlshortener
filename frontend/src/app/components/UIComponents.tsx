export function Spinner() {
    return (
        <svg className="animate-spin h-4 w-4 inline-block" viewBox="0 0 24 24" fill="none">
            <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="3" />
            <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.37 0 0 5.37 0 12h4z" />
        </svg>
    );
}

export function CopyIcon() {
    return (
        <svg className="w-4 h-4" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5" strokeLinecap="round" strokeLinejoin="round">
            <rect x="9" y="9" width="13" height="13" rx="2" ry="2" />
            <path d="M5 15H4a2 2 0 01-2-2V4a2 2 0 012-2h9a2 2 0 012 2v1" />
        </svg>
    );
}

export function CheckIcon() {
    return (
        <svg className="w-4 h-4 text-emerald-600" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="3" strokeLinecap="round" strokeLinejoin="round">
            <polyline points="20 6 9 17 4 12" />
        </svg>
    );
}

export function ErrorPanel({ message }: { message: string }) {
    return (
        <div className="fade-in bg-rose-100/40 border border-rose-200/50 rounded-2xl px-4 py-3" style={{ fontFamily: "var(--font-jetbrains)" }}>
            <p className="text-sm text-rose-600 font-medium">{message}</p>
        </div>
    );
}
