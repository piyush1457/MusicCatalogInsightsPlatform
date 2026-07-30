import { useState } from 'react';
import { useQuery, useMutation } from '@tanstack/react-query';
import { motion, AnimatePresence } from 'framer-motion';
import { BarChart3, Music, Sparkles, TrendingUp } from 'lucide-react';
import {
  PieChart, Pie, Cell, BarChart, Bar, XAxis, YAxis, Tooltip,
  ResponsiveContainer, Legend
} from 'recharts';
import apiClient, { parseError } from '../lib/apiClient';
import { useAuth } from '../context/AuthContext';

const SPOTIFY_COLORS = ['#1db954', '#1ed760', '#169c46', '#1aa34a', '#14823d',
  '#1db954', '#1ed760', '#169c46', '#1aa34a', '#14823d'];

const VIBRANT_COLORS = ['#1db954', '#e91e63', '#9c27b0', '#ff9800', '#2196f3',
  '#4caf50', '#f44336', '#00bcd4', '#ff5722', '#795548'];

interface GenreCount { genre: string; count: number; }
interface YearCount { year: string; count: number; }
interface ArtistCount { artistName: string; count: number; }
interface DurationHistogram {
  bucket0to2: number; bucket2to3: number; bucket3to4: number;
  bucket4to5: number; bucket5plus: number;
}
interface AnalyticsData {
  genreDistribution: GenreCount[];
  releasesByYear: YearCount[];
  topArtists: ArtistCount[];
  durationHistogram: DurationHistogram;
  totalItems: number;
}

const DURATION_LABELS = ['0–2 min', '2–3 min', '3–4 min', '4–5 min', '5+ min'];
const DURATION_KEYS = ['bucket0to2', 'bucket2to3', 'bucket3to4', 'bucket4to5', 'bucket5plus'];

const cardVariants = {
  hidden: { opacity: 0, y: 20, scale: 0.95 },
  visible: { 
    opacity: 1, 
    y: 0, 
    scale: 1
  },
  hover: {
    y: -4,
    scale: 1.01
  }
};

const containerVariants = {
  hidden: { opacity: 0 },
  visible: {
    opacity: 1,
    transition: {
      staggerChildren: 0.1
    }
  }
};

export default function Dashboard() {
  const { token } = useAuth();
  const [aiInsight, setAiInsight] = useState<string | null>(null);
  const [aiError, setAiError] = useState<string | null>(null);

  const { data, isLoading, error } = useQuery<AnalyticsData>({
    queryKey: ['analytics-summary'],
    queryFn: async () => {
      const res = await apiClient.get('/api/analytics/summary');
      return res.data;
    },
    enabled: !!token,
  });

  const insightMutation = useMutation({
    mutationFn: async () => {
      const summary = JSON.stringify(data ?? { totalItems: 0 });
      const res = await apiClient.post('/api/insights/summary', { summary });
      return res.data.insight;
    },
    onSuccess: (insight) => setAiInsight(insight),
    onError: (err) => setAiError(parseError(err)),
  });

  if (isLoading) return (
    <div className="page">
      <motion.h1 
        initial={{ opacity: 0, y: -20 }}
        animate={{ opacity: 1, y: 0 }}
        transition={{ duration: 0.5 }}
      >
        Dashboard
      </motion.h1>
      <div className="loading">
        <motion.div
          animate={{ rotate: 360 }}
          transition={{ duration: 1, repeat: Infinity, ease: "linear" }}
          style={{ display: 'inline-block' }}
        >
          <BarChart3 size={48} style={{ opacity: 0.5 }} />
        </motion.div>
        <p style={{ marginTop: 16 }}>Loading your music insights…</p>
      </div>
    </div>
  );

  if (error) return (
    <div className="page">
      <motion.h1 
        initial={{ opacity: 0, y: -20 }}
        animate={{ opacity: 1, y: 0 }}
        transition={{ duration: 0.5 }}
      >
        Dashboard
      </motion.h1>
      <motion.p 
        className="error-message"
        initial={{ opacity: 0, y: 10 }}
        animate={{ opacity: 1, y: 0 }}
      >
        {parseError(error)}
      </motion.p>
    </div>
  );

  if (!data || data.totalItems === 0) {
    return (
      <div className="page">
        <motion.h1 
          initial={{ opacity: 0, y: -20 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ duration: 0.5 }}
        >
          Dashboard
        </motion.h1>
        <motion.div 
          className="empty-state"
          initial={{ opacity: 0, scale: 0.95 }}
          animate={{ opacity: 1, scale: 1 }}
        >
          <Music size={64} style={{ marginBottom: 20, opacity: 0.3 }} />
          <p style={{ fontSize: '1.2rem', fontWeight: 600, marginBottom: 8 }}>
            No data yet
          </p>
          <p style={{ fontSize: '0.95rem', color: 'var(--text-tertiary)' }}>
            Save songs to your library to see your music analytics
          </p>
        </motion.div>
      </div>
    );
  }

  const pieData = data.genreDistribution.map(g => ({ name: g.genre, value: g.count }));
  const barYearData = data.releasesByYear.map(y => ({ name: y.year, count: y.count }));
  const barArtistData = data.topArtists.map(a => ({ name: a.artistName, count: a.count }));
  const histogramData = DURATION_KEYS.map((key, i) => {
    const count = (data.durationHistogram as unknown as Record<string, number>)[key] ?? 0;
    return {
      name: DURATION_LABELS[i],
      count,
    };
  });

  return (
    <div className="page">
      <motion.div
        initial={{ opacity: 0, y: -20 }}
        animate={{ opacity: 1, y: 0 }}
        transition={{ duration: 0.5 }}
      >
        <h1>Dashboard</h1>
        <motion.p 
          className="meta"
          initial={{ opacity: 0 }}
          animate={{ opacity: 1 }}
          transition={{ delay: 0.2 }}
          style={{ marginBottom: 32, fontSize: '1.1rem', color: 'var(--text-secondary)' }}
        >
          <TrendingUp size={18} style={{ display: 'inline', marginRight: 8, verticalAlign: 'text-bottom' }} />
          {data.totalItems} song{data.totalItems !== 1 ? 's' : ''} in your library
        </motion.p>
      </motion.div>

      <motion.div 
        className="charts-grid"
        variants={containerVariants}
        initial="hidden"
        animate="visible"
      >
        <motion.div 
          className="chart-card"
          variants={cardVariants}
          whileHover="hover"
          transition={{ duration: 0.3 }}
        >
          <h3>Genre Distribution</h3>
          <ResponsiveContainer width="100%" height={300}>
            <PieChart>
              <Pie 
                data={pieData} 
                dataKey="value" 
                nameKey="name" 
                cx="50%" 
                cy="50%" 
                outerRadius={100} 
                label
                labelLine={false}
              >
                {pieData.map((_, i) => <Cell key={i} fill={VIBRANT_COLORS[i % VIBRANT_COLORS.length]} />)}
              </Pie>
              <Tooltip 
                contentStyle={{ 
                  backgroundColor: 'var(--bg-card)', 
                  border: '1px solid var(--border)',
                  borderRadius: '8px',
                  color: 'var(--text-primary)'
                }}
              />
              <Legend />
            </PieChart>
          </ResponsiveContainer>
        </motion.div>

        <motion.div 
          className="chart-card"
          variants={cardVariants}
          whileHover="hover"
          transition={{ duration: 0.3 }}
        >
          <h3>Releases by Year</h3>
          <ResponsiveContainer width="100%" height={300}>
            <BarChart data={barYearData}>
              <XAxis 
                dataKey="name" 
                stroke="var(--text-secondary)"
                style={{ fontSize: '12px' }}
              />
              <YAxis 
                stroke="var(--text-secondary)"
                style={{ fontSize: '12px' }}
              />
              <Tooltip 
                contentStyle={{ 
                  backgroundColor: 'var(--bg-card)', 
                  border: '1px solid var(--border)',
                  borderRadius: '8px',
                  color: 'var(--text-primary)'
                }}
              />
              <Bar dataKey="count" fill="#1db954" radius={[4, 4, 0, 0]} />
            </BarChart>
          </ResponsiveContainer>
        </motion.div>

        <motion.div 
          className="chart-card"
          variants={cardVariants}
          whileHover="hover"
          transition={{ duration: 0.3 }}
        >
          <h3>Top Artists</h3>
          <ResponsiveContainer width="100%" height={300}>
            <BarChart data={barArtistData} layout="vertical">
              <XAxis 
                type="number" 
                stroke="var(--text-secondary)"
                style={{ fontSize: '12px' }}
              />
              <YAxis 
                dataKey="name" 
                type="category" 
                width={100}
                stroke="var(--text-secondary)"
                style={{ fontSize: '12px' }}
              />
              <Tooltip 
                contentStyle={{ 
                  backgroundColor: 'var(--bg-card)', 
                  border: '1px solid var(--border)',
                  borderRadius: '8px',
                  color: 'var(--text-primary)'
                }}
              />
              <Bar dataKey="count" fill="#e91e63" radius={[0, 4, 4, 0]} />
            </BarChart>
          </ResponsiveContainer>
        </motion.div>

        <motion.div 
          className="chart-card"
          variants={cardVariants}
          whileHover="hover"
          transition={{ duration: 0.3 }}
        >
          <h3>Duration Distribution</h3>
          <ResponsiveContainer width="100%" height={300}>
            <BarChart data={histogramData}>
              <XAxis 
                dataKey="name" 
                stroke="var(--text-secondary)"
                style={{ fontSize: '12px' }}
              />
              <YAxis 
                stroke="var(--text-secondary)"
                style={{ fontSize: '12px' }}
              />
              <Tooltip 
                contentStyle={{ 
                  backgroundColor: 'var(--bg-card)', 
                  border: '1px solid var(--border)',
                  borderRadius: '8px',
                  color: 'var(--text-primary)'
                }}
              />
              <Bar dataKey="count" fill="#ff9800" radius={[4, 4, 0, 0]} />
            </BarChart>
          </ResponsiveContainer>
        </motion.div>
      </motion.div>

      <motion.div 
        className="ai-panel"
        initial={{ opacity: 0, y: 20 }}
        animate={{ opacity: 1, y: 0 }}
        transition={{ delay: 0.4 }}
        style={{ marginTop: 40 }}
      >
        <motion.h2 
          initial={{ opacity: 0 }}
          animate={{ opacity: 1 }}
          transition={{ delay: 0.5 }}
          style={{ display: 'flex', alignItems: 'center', gap: 12 }}
        >
          <Sparkles size={24} style={{ color: '#1db954' }} />
          AI Insights
        </motion.h2>
        <motion.button
          className="btn-primary"
          style={{ width: 'auto', marginBottom: 16, marginTop: 16 }}
          onClick={() => {
            setAiInsight(null);
            setAiError(null);
            insightMutation.mutate();
          }}
          disabled={insightMutation.isPending}
          whileHover={{ scale: 1.02 }}
          whileTap={{ scale: 0.98 }}
        >
          {insightMutation.isPending ? (
            <span style={{ display: 'flex', alignItems: 'center', gap: 8 }}>
              <motion.div
                animate={{ rotate: 360 }}
                transition={{ duration: 1, repeat: Infinity, ease: "linear" }}
              >
                <Sparkles size={16} />
              </motion.div>
              Generating…
            </span>
          ) : (
            <span style={{ display: 'flex', alignItems: 'center', gap: 8 }}>
              <Sparkles size={16} />
              Generate Insight
            </span>
          )}
        </motion.button>

        <AnimatePresence>
          {aiError && (
            <motion.p 
              className="error-message"
              initial={{ opacity: 0, y: -10 }}
              animate={{ opacity: 1, y: 0 }}
              exit={{ opacity: 0, y: -10 }}
            >
              {aiError}
            </motion.p>
          )}
        </AnimatePresence>

        <AnimatePresence>
          {aiInsight && (
            <motion.div 
              className="ai-response"
              initial={{ opacity: 0, y: 10 }}
              animate={{ opacity: 1, y: 0 }}
              exit={{ opacity: 0, y: 10 }}
              style={{
                background: 'var(--bg-card)',
                padding: 24,
                borderRadius: '12px',
                border: '1px solid var(--border)',
                lineHeight: 1.7,
                fontSize: '1rem',
                color: 'var(--text-primary)',
                boxShadow: 'var(--shadow-md)'
              }}
            >
              {aiInsight}
            </motion.div>
          )}
        </AnimatePresence>
      </motion.div>
    </div>
  );
}
