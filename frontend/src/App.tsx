import { Routes, Route, Navigate, Outlet, useLocation } from 'react-router-dom';
import ProtectedRoute from './components/ProtectedRoute';
import Navbar from './components/Navbar';
import Login from './pages/Login';
import Register from './pages/Register';
import Search from './pages/Search';
import Library from './pages/Library';
import Dashboard from './pages/Dashboard';
import { useAuth } from './context/AuthContext';

function MainLayout() {
  const { isAuthenticated } = useAuth();
  const location = useLocation();
  const isAuthPage = location.pathname === '/login' || location.pathname === '/register';

  if (isAuthPage) {
    return (
      <div className="auth-layout">
        <Outlet />
      </div>
    );
  }

  return (
    <div className="app-container">
      {isAuthenticated && <Navbar />}
      <div className="main-content" style={isAuthenticated ? {} : { marginLeft: 0, width: '100%' }}>
        <main>
          <Outlet />
        </main>
      </div>
    </div>
  );
}

export default function App() {
  return (
    <Routes>
      <Route element={<MainLayout />}>
        <Route path="/login" element={<Login />} />
        <Route path="/register" element={<Register />} />
        <Route element={<ProtectedRoute />}>
          <Route path="/dashboard" element={<Dashboard />} />
          <Route path="/search" element={<Search />} />
          <Route path="/library" element={<Library />} />
        </Route>
        <Route path="*" element={<Navigate to="/search" replace />} />
      </Route>
    </Routes>
  );
}
