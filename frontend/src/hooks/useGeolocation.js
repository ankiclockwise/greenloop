import { useState, useEffect } from "react";

export function useGeolocation() {
  const [state, setState] = useState({
    lat: null,
    lng: null,
    error: null,
    loading: true,
  });

  useEffect(() => {
    if (!navigator.geolocation) {
      setState({ lat: null, lng: null, error: "Geolocation is not supported by your browser.", loading: false });
      return;
    }

    navigator.geolocation.getCurrentPosition(
      (position) => {
        setState({
          lat: position.coords.latitude,
          lng: position.coords.longitude,
          error: null,
          loading: false,
        });
      },
      (err) => {
        const messages = {
          1: "Location access was denied. Enter your city or ZIP below.",
          2: "Your location could not be determined.",
          3: "Location request timed out.",
        };
        setState({ lat: null, lng: null, error: messages[err.code] || "Location unavailable.", loading: false });
      },
      { timeout: 10000, maximumAge: 3600000 }
    );
  }, []);

  function setManualLocation(lat, lng) {
    setState({ lat, lng, error: null, loading: false });
  }

  return { ...state, setManualLocation };
}
