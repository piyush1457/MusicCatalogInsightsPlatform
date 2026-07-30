import { Link, useNavigate, useLocation } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { Home, Search, Library, LogOut, Music, Headphones } from 'lucide-react';

export default function Navbar() {
  const { isAuthenticated, logout } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();

  const handleLogout = () => {
    logout();
    navigate('/login');
  };

  const isActive = (path: string) => location.pathname === path;

  return (
    <aside className="sidebar">
      <div className="sidebar-logo">
        <div className="logo-icon-wrapper">
          <Headphones className="logo-icon" />
        </div>
        <span className="sidebar-logo-text">Music Catalog</span>
      </div>
      <div className="nav-items-container">
        {isAuthenticated ? (
          <>
            <Link to="/dashboard" className={`nav-item ${isActive('/dashboard') ? 'active' : ''}`}>
              <Home />
              <span>Dashboard</span>
            </Link>
            <Link to="/search" className={`nav-item ${isActive('/search') ? 'active' : ''}`}>
              <Search />
              <span>Search</span>
            </Link>
            <Link to="/library" className={`nav-item ${isActive('/library') ? 'active' : ''}`}>
              <Library />
              <span>Library</span>
            </Link>
            <button onClick={handleLogout} className="nav-item logout-btn">
              <LogOut />
              <span>Logout</span>
            </button>
          </>
        ) : (
          <div className="nav-item auth-prompt">
            <Music />
            <span>Please log in</span>
          </div>
        )}
      </div>
      <div className="sidebar-footer">
        <div className="footer-decoration" />
      </div>
    </aside>
  );
}
