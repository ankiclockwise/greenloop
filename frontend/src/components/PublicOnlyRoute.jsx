import { Navigate } from "react-router-dom";
import { useAuth } from "../auth/AuthProvider";

export function PublicOnlyRoute({ children }) {
  const { user, loading, configError } = useAuth();

  if (loading) {
    return <div className="page-status">Loading...</div>;
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

  if (user) {
    return <Navigate to="/" replace />;
  }

  return children;
}
