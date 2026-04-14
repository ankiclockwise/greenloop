import { Navigate, useLocation } from "react-router-dom";
import { useAuth } from "./AuthProvider";

export function ProtectedRoute({ children }) {
  const { user, loading, configError } = useAuth();
  const location = useLocation();

  if (loading) {
    return <div className="page-status">Checking your session...</div>;
  }

  if (configError) {
    return (
      <div className="page-status">
        <div className="status-card">
          <h1>Firebase setup needed</h1>
          <p>{configError}</p>
        </div>
      </div>
    );
  }

  if (!user) {
    return <Navigate to="/login" replace state={{ from: location }} />;
  }

  if (!user.actor && location.pathname !== "/onboarding") {
    return <Navigate to="/onboarding" replace />;
  }

  if (user.actor && location.pathname === "/onboarding") {
    return <Navigate to="/" replace />;
  }

  return children;
}
