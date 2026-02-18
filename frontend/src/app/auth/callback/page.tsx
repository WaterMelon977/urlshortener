"use client";

import { useEffect } from "react";
import { useRouter, useSearchParams } from "next/navigation";
import { Suspense } from "react";

function CallbackHandler() {
    const router = useRouter();
    const searchParams = useSearchParams();

    useEffect(() => {
        const token = searchParams.get("token");
        if (token) {
            localStorage.setItem("jwt", token);
        }
        // Always redirect to home
        router.replace("/");
    }, [searchParams, router]);

    return (
        <div className="flex items-center justify-center min-h-screen text-white">
            <p>Signing you in...</p>
        </div>
    );
}

export default function AuthCallbackPage() {
    return (
        <Suspense fallback={<div className="flex items-center justify-center min-h-screen text-white"><p>Loading...</p></div>}>
            <CallbackHandler />
        </Suspense>
    );
}
