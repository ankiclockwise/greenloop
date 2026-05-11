import { act, renderHook, waitFor } from "@testing-library/react";
import { afterEach, beforeEach, describe, expect, it, vi } from "vitest";
import { useFeedWebSocket } from "../../src/hooks/useFeedWebSocket";

class MockWebSocket {
  static instances = [];

  constructor(url) {
    this.url = url;
    this.send = vi.fn();
    this.close = vi.fn();
    this.onopen = null;
    this.onmessage = null;
    this.onclose = null;
    this.onerror = null;
    MockWebSocket.instances.push(this);
  }
}

describe("useFeedWebSocket", () => {
  beforeEach(() => {
    MockWebSocket.instances = [];
    vi.stubGlobal("WebSocket", MockWebSocket);
  });

  afterEach(() => {
    vi.unstubAllGlobals();
    vi.useRealTimers();
  });

  it("connects and sends the STOMP connect frame when opened", async () => {
    const { result } = renderHook(() => useFeedWebSocket());
    const socket = MockWebSocket.instances[0];

    act(() => socket.onopen());

    await waitFor(() => expect(result.current.connected).toBe(true));
    expect(socket.url).toBe("ws://localhost:8080/ws");
    expect(socket.send).toHaveBeenCalledWith(
      "CONNECT\naccept-version:1.2\nheart-beat:10000,10000\n\n\0"
    );
  });

  it("parses listing messages from STOMP frames", async () => {
    const { result } = renderHook(() => useFeedWebSocket());
    const socket = MockWebSocket.instances[0];

    act(() => {
      socket.onmessage({
        data: 'MESSAGE\ndestination:/topic/listings\n\n{"id":"listing-1","title":"Bread"}\0'
      });
    });

    await waitFor(() =>
      expect(result.current.newListings).toEqual([
        { id: "listing-1", title: "Bread" }
      ])
    );
  });

  it("ignores connected, receipt, empty, and malformed frames", async () => {
    const { result } = renderHook(() => useFeedWebSocket());
    const socket = MockWebSocket.instances[0];

    act(() => {
      socket.onmessage({ data: "" });
      socket.onmessage({ data: "CONNECTED\n\n\0" });
      socket.onmessage({ data: "RECEIPT\n\n\0" });
      socket.onmessage({ data: "MESSAGE\n\nnot-json\0" });
    });

    expect(result.current.newListings).toEqual([]);
  });

  it("marks the hook disconnected and reconnects after close", async () => {
    vi.useFakeTimers();
    const { result } = renderHook(() => useFeedWebSocket());
    const socket = MockWebSocket.instances[0];

    act(() => socket.onopen());
    expect(result.current.connected).toBe(true);

    act(() => socket.onclose());
    expect(result.current.connected).toBe(false);

    act(() => vi.advanceTimersByTime(2000));
    expect(MockWebSocket.instances).toHaveLength(2);
  });

  it("closes the socket on cleanup", () => {
    const { unmount } = renderHook(() => useFeedWebSocket());
    const socket = MockWebSocket.instances[0];

    unmount();

    expect(socket.close).toHaveBeenCalledTimes(1);
  });
});
