import { useState } from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { motion, AnimatePresence } from 'framer-motion';
import { Library as LibraryIcon, Music, Trash2, X } from 'lucide-react';
import apiClient, { parseError } from '../lib/apiClient';
import StarRating from '../components/StarRating';

interface LibraryItem {
  id: number;
  appleCatalogId: number;
  title: string;
  artistName: string;
  genre: string;
  releaseDate: string;
  durationSeconds: number;
  artworkUrl: string;
  userRating: number | null;
  userNotes: string | null;
}

interface PagedResponse {
  content: LibraryItem[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
  last: boolean;
}

const cardVariants = {
  hidden: { opacity: 0, y: 20, scale: 0.95 },
  visible: { 
    opacity: 1, 
    y: 0, 
    scale: 1
  },
  hover: {
    y: -8,
    scale: 1.02
  }
};

const containerVariants = {
  hidden: { opacity: 0 },
  visible: {
    opacity: 1,
    transition: {
      staggerChildren: 0.05
    }
  }
};

export default function Library() {
  const [page, setPage] = useState(0);
  const queryClient = useQueryClient();
  const [deletingId, setDeletingId] = useState<number | null>(null);
  const [expandedId, setExpandedId] = useState<number | null>(null);

  const { data, isLoading, error } = useQuery<PagedResponse>({
    queryKey: ['library', page],
    queryFn: async () => {
      const res = await apiClient.get('/api/library', { params: { page, size: 12 } });
      return res.data;
    },
  });

  const updateMutation = useMutation({
    mutationFn: async ({ id, userRating, userNotes }: { id: number; userRating?: number | null; userNotes?: string | null }) => {
      const body: Record<string, unknown> = {};
      if (userRating !== undefined) body.userRating = userRating;
      if (userNotes !== undefined) body.userNotes = userNotes;
      await apiClient.put(`/api/library/${id}`, body);
    },
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ['library'] }),
  });

  const deleteMutation = useMutation({
    mutationFn: async (id: number) => {
      await apiClient.delete(`/api/library/${id}`);
    },
    onSuccess: () => {
      setDeletingId(null);
      queryClient.invalidateQueries({ queryKey: ['library'] });
      queryClient.invalidateQueries({ queryKey: ['library-ids'] });
    },
  });

  const formatDuration = (seconds: number) => {
    const mins = Math.floor(seconds / 60);
    const secs = seconds % 60;
    return `${mins}:${secs.toString().padStart(2, '0')}`;
  };

  if (isLoading) return (
    <div className="page">
      <motion.h1 
        initial={{ opacity: 0, y: -20 }}
        animate={{ opacity: 1, y: 0 }}
        transition={{ duration: 0.5 }}
      >
        My Library
      </motion.h1>
      <motion.div 
        className="skeleton-grid"
        variants={containerVariants}
        initial="hidden"
        animate="visible"
      >
        {[1, 2, 3, 4, 5, 6, 7, 8].map((i) => (
          <motion.div 
            key={i} 
            className="skeleton-card"
            variants={cardVariants}
            transition={{ duration: 0.3 }}
          />
        ))}
      </motion.div>
    </div>
  );

  if (error) return (
    <div className="page">
      <motion.h1 
        initial={{ opacity: 0, y: -20 }}
        animate={{ opacity: 1, y: 0 }}
        transition={{ duration: 0.5 }}
      >
        My Library
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

  const items = data?.content ?? [];

  return (
    <div className="page">
      <motion.h1 
        initial={{ opacity: 0, y: -20 }}
        animate={{ opacity: 1, y: 0 }}
        transition={{ duration: 0.5 }}
      >
        My Library
      </motion.h1>

      <AnimatePresence>
        {items.length === 0 ? (
          <motion.div 
            className="empty-state"
            initial={{ opacity: 0, scale: 0.95 }}
            animate={{ opacity: 1, scale: 1 }}
            exit={{ opacity: 0, scale: 0.95 }}
          >
            <LibraryIcon size={48} style={{ marginBottom: 16, opacity: 0.5 }} />
            <p>Your library is empty</p>
            <p style={{ fontSize: '0.9rem', marginTop: 8, color: 'var(--text-tertiary)' }}>
              Search and save songs to build your collection
            </p>
          </motion.div>
        ) : (
          <>
            <motion.div 
              className="library-list"
              variants={containerVariants}
              initial="hidden"
              animate="visible"
            >
              {items.map((item) => (
                <motion.div
                  key={item.id}
                  className="library-card"
                  variants={cardVariants}
                  whileHover="hover"
                  initial="hidden"
                  animate="visible"
                  transition={{ duration: 0.3 }}
                  style={{ originY: 0 }}
                  onClick={() => setExpandedId(expandedId === item.id ? null : item.id)}
                >
                  <img 
                    src={item.artworkUrl} 
                    alt={item.title} 
                    className="artwork"
                    loading="lazy"
                  />
                  <div className="library-info">
                    <h3>{item.title}</h3>
                    <p className="artist">{item.artistName}</p>
                    <p className="meta">{item.genre} • {formatDuration(item.durationSeconds)}</p>

                    <AnimatePresence>
                      {expandedId === item.id && (
                        <motion.div 
                          className="library-editor"
                          initial={{ opacity: 0, height: 0 }}
                          animate={{ opacity: 1, height: 'auto' }}
                          exit={{ opacity: 0, height: 0 }}
                          onClick={(e) => e.stopPropagation()}
                        >
                          <StarRating
                            value={item.userRating}
                            onChange={(rating) =>
                              updateMutation.mutate({ id: item.id, userRating: rating })
                            }
                          />

                          <textarea
                            defaultValue={item.userNotes ?? ''}
                            placeholder="Add notes..."
                            onBlur={(e) => {
                              const val = e.target.value.trim() || null;
                              if (val !== item.userNotes) {
                                updateMutation.mutate({ id: item.id, userNotes: val });
                              }
                            }}
                            rows={2}
                          />
                        </motion.div>
                      )}
                    </AnimatePresence>
                  </div>

                  <div className="library-actions">
                    {deletingId === item.id ? (
                      <motion.div 
                        className="confirm-delete"
                        initial={{ opacity: 0, scale: 0.9 }}
                        animate={{ opacity: 1, scale: 1 }}
                      >
                        <span>Delete?</span>
                        <motion.button 
                          onClick={(e) => {
                            e.stopPropagation();
                            deleteMutation.mutate(item.id);
                          }} 
                          className="btn-danger"
                          whileHover={{ scale: 1.05 }}
                          whileTap={{ scale: 0.95 }}
                        >
                          Yes
                        </motion.button>
                        <motion.button 
                          onClick={(e) => {
                            e.stopPropagation();
                            setDeletingId(null);
                          }} 
                          className="btn-secondary"
                          whileHover={{ scale: 1.05 }}
                          whileTap={{ scale: 0.95 }}
                        >
                          No
                        </motion.button>
                      </motion.div>
                    ) : (
                      <motion.button 
                        onClick={(e) => {
                          e.stopPropagation();
                          setDeletingId(item.id);
                        }} 
                        className="btn-danger"
                        whileHover={{ scale: 1.05 }}
                        whileTap={{ scale: 0.95 }}
                      >
                        <Trash2 size={16} />
                      </motion.button>
                    )}
                  </div>
                </motion.div>
              ))}
            </motion.div>

            {data && data.totalPages > 1 && (
              <motion.div 
                className="pagination"
                initial={{ opacity: 0 }}
                animate={{ opacity: 1 }}
                transition={{ delay: 0.3 }}
              >
                <motion.button 
                  disabled={page === 0} 
                  onClick={() => setPage((p) => p - 1)}
                  whileHover={{ scale: page === 0 ? 1 : 1.05 }}
                  whileTap={{ scale: page === 0 ? 1 : 0.95 }}
                >
                  Previous
                </motion.button>
                <span>Page {page + 1} of {data.totalPages}</span>
                <motion.button 
                  disabled={data.last} 
                  onClick={() => setPage((p) => p + 1)}
                  whileHover={{ scale: data.last ? 1 : 1.05 }}
                  whileTap={{ scale: data.last ? 1 : 0.95 }}
                >
                  Next
                </motion.button>
              </motion.div>
            )}
          </>
        )}
      </AnimatePresence>
    </div>
  );
}
