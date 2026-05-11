import { act, renderHook, waitFor } from "@testing-library/react";
import { afterEach, describe, expect, it, vi } from "vitest";
import { useGeolocation } from "../../src/hooks/useGeolocation";

function setGeolocation(geolocation) {
  Object.defineProperty(navigator, "geolocation", {
    configurable: true,
    value: geolocation
  });
}

describe("useGeolocation", () => {
  afterEach(() => {
    vi.restoreAllMocks();
    setGeolocation(undefined);
  });

  it("reports unsupported geolocation", async () => {
    setGeolocation(undefined);

    const { result } = renderHook(() => useGeolocation());

    await waitFor(() => expect(result.current.loading).toBe(false));
    expect(result.current.error).toBe("Geolocation is not supported by your browser.");
  });

  it("stores coordinates from browser geolocation", async () => {
    setGeolocation({
      getCurrentPosition: vi.fn((success) =>
        success({ coords: { latitude: 42.39, longitude: -72.52 } })
      )
    });

    const { result } = renderHook(() => useGeolocation());

    await waitFor(() => expect(result.current.loading).toBe(false));
    expect(result.current.lat).toBe(42.39);
    expect(result.current.lng).toBe(-72.52);
    expect(result.current.error).toBeNull();
  });

  it("maps geolocation errors and accepts a manual location", async () => {
    setGeolocation({
      getCurrentPosition: vi.fn((success, error) => error({ code: 1 }))
    });

    const { result } = renderHook(() => useGeolocation());

    await waitFor(() => expect(result.current.loading).toBe(false));
    expect(result.current.error).toBe("Location access was denied. Enter your city or ZIP below.");

    act(() => result.current.setManualLocation(40, -70));
    expect(result.current.lat).toBe(40);
    expect(result.current.lng).toBe(-70);
    expect(result.current.error).toBeNull();
  });
});
