"use client";

import { useRef, useEffect } from "react";
import VanillaTilt from "vanilla-tilt";

interface TiltCardProps {
    children: React.ReactNode;
    className?: string;
}

export default function TiltCard({ children, className }: TiltCardProps) {
    const tiltRef = useRef<HTMLDivElement>(null);

    useEffect(() => {
        const tiltNode = tiltRef.current;
        if (tiltNode) {
            VanillaTilt.init(tiltNode, {
                max: 8, // subtle tilt
                speed: 400,
                glare: true,
                "max-glare": 0.3,
                scale: 1.02,
                gyroscope: true, // for mobile if supported
                // @ts-expect-error vanilla-tilt supports element but types might be outdated
                "mouse-event-element": document.body,
            });
        }
        return () => {
            // @ts-expect-error VanillaTilt adds destroy method
            tiltNode?.vanillaTilt?.destroy();
        };
    }, []);

    return (
        <div ref={tiltRef} className={className}>
            {children}
        </div>
    );
}
