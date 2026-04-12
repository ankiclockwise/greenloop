import { useState, useEffect, useRef } from "react";

const WS_URL = import.meta.env.VITE_WS_URL || "ws://localhost:8080/ws";
const RECONNECT_BASE_MS = 2000;
const RECONNECT_MAX_MS = 30000;

export function useFeedWebSocket() {
  const [connected, setConnected] = useState(false);
  const [newListings, setNewListings] = useState([]);
  const wsRef = useRef(null);
  const retryRef = useRef(0);
  const timerRef = useRef(null);

  useEffect(() => {
    function connect() {
      try {
        const ws = new WebSocket(WS_URL);
        wsRef.current = ws;

        ws.onopen = () => {
          setConnected(true);
          retryRef.current = 0;
          // STOMP CONNECT frame
          ws.send("CONNECT\naccept-version:1.2\nheart-beat:10000,10000\n\n\0");
        };

        ws.onmessage = (event) => {
          if (!event.data || event.data.startsWith("CONNECTED") || event.data.startsWith("RECEIPT")) return;
          if (event.data.startsWith("MESSAGE")) {
            try {
              const body = event.data.split("\n\n").slice(1).join("\n\n").replace(/\0$/, "");
              const listing = JSON.parse(body);
              setNewListings([listing]);
            } catch {
              // non-JSON frame, ignore
            }
          }
        };

        ws.onclose = () => {
          setConnected(false);
          const delay = Math.min(RECONNECT_BASE_MS * 2 ** retryRef.current, RECONNECT_MAX_MS);
          retryRef.current += 1;
          timerRef.current = setTimeout(connect, delay);
        };

        ws.onerror = () => ws.close();
      } catch {
        // WebSocket not available (e.g. backend not running) — fail silently
      }
    }

    connect();

    return () => {
      clearTimeout(timerRef.current);
      wsRef.current?.close();
    };
  }, []);

  return { connected, newListings };
}
