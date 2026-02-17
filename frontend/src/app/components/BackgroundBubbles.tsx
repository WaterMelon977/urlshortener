"use client";

import { useEffect, useState, useCallback } from "react";

interface Bubble {
    id: number;
    size: number;
    left: number;
    animationDuration: number;
    animationDelay: number;
}

export default function BackgroundBubbles() {
    const [bubbles, setBubbles] = useState<Bubble[]>([]);

    const createBubble = useCallback((id: number) => {
        const size = Math.random() * 60 + 20; // 20px to 80px

        // Avoid the center 30% (35% to 65%) to exclude the frosted box column
        const section = Math.random() > 0.5 ? 'left' : 'right';
        const left = section === 'left'
            ? Math.random() * 35 // 0% to 35%
            : Math.random() * 35 + 65; // 65% to 100%

        const animationDuration = Math.random() * 10 + 40; // 10s to 20s
        const animationDelay = Math.random() * 10; // 0s to 10s

        return {
            id,
            size,
            left,
            animationDuration,
            animationDelay,
        };
    }, []);

    useEffect(() => {
        // Initial population
        const initialBubbles = Array.from({ length: 15 }).map((_, i) => createBubble(i));
        setBubbles(initialBubbles);
    }, [createBubble]);

    const popBubble = (id: number) => {
        setBubbles((prev) =>
            prev.map((b) => (b.id === id ? createBubble(Date.now() + Math.random()) : b))
        );
    };

    return (
        <div className="fixed inset-0 pointer-events-none overflow-hidden z-0">
            {bubbles.map((bubble) => (
                <div
                    key={bubble.id}
                    className="bubble absolute rounded-full pointer-events-auto"
                    style={{
                        width: `${bubble.size}px`,
                        height: `${bubble.size}px`,
                        left: `${bubble.left}%`,
                        bottom: `-${bubble.size}px`, // Start below the screen
                        animationDuration: `${bubble.animationDuration}s`,
                        animationDelay: `${bubble.animationDelay}s`,
                    }}
                    onClick={() => popBubble(bubble.id)}
                    onAnimationEnd={() => popBubble(bubble.id)}
                />
            ))}
        </div>
    );
}
