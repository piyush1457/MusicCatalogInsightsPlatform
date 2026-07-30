import { useState } from 'react';
import { useQuery, useQueryClient } from '@tanstack/react-query';
import { motion, AnimatePresence } from 'framer-motion';
import { Search as SearchIcon, Music, TrendingUp, Flame } from 'lucide-react';
import apiClient, { parseError } from '../lib/apiClient';
import { useDebounce } from '../hooks/useDebounce';

interface SearchResult {
  appleCatalogId: number;
  title: string;
  artistName: string;
  genre: string;
  releaseDate: string;
  durationSeconds: number;
  artworkUrl: string;
}

interface LibraryItem {
  id: number;
  appleCatalogId: number;
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

const featuredSongs: SearchResult[] = [
  {
    appleCatalogId: 1,
    title: "Blinding Lights",
    artistName: "The Weeknd",
    genre: "Synth-pop",
    releaseDate: "2020-03-20",
    durationSeconds: 200,
    artworkUrl: "https://picsum.photos/seed/song1/300/300"
  },
  {
    appleCatalogId: 2,
    title: "Levitating",
    artistName: "Dua Lipa",
    genre: "Disco-pop",
    releaseDate: "2020-10-01",
    durationSeconds: 203,
    artworkUrl: "https://picsum.photos/seed/song2/300/300"
  },
  {
    appleCatalogId: 3,
    title: "Stay",
    artistName: "The Kid LAROI, Justin Bieber",
    genre: "Pop",
    releaseDate: "2021-07-09",
    durationSeconds: 141,
    artworkUrl: "https://picsum.photos/seed/song3/300/300"
  },
  {
    appleCatalogId: 4,
    title: "Good 4 U",
    artistName: "Olivia Rodrigo",
    genre: "Pop Rock",
    releaseDate: "2021-05-14",
    durationSeconds: 178,
    artworkUrl: "https://picsum.photos/seed/song4/300/300"
  },
  {
    appleCatalogId: 5,
    title: "Peaches",
    artistName: "Justin Bieber",
    genre: "R&B",
    releaseDate: "2021-03-19",
    durationSeconds: 198,
    artworkUrl: "https://picsum.photos/seed/song5/300/300"
  },
  {
    appleCatalogId: 6,
    title: "Montero",
    artistName: "Lil Nas X",
    genre: "Pop Rap",
    releaseDate: "2021-03-26",
    durationSeconds: 137,
    artworkUrl: "https://picsum.photos/seed/song6/300/300"
  },
  {
    appleCatalogId: 7,
    title: "Kiss Me More",
    artistName: "Doja Cat ft. SZA",
    genre: "Pop",
    releaseDate: "2021-04-09",
    durationSeconds: 208,
    artworkUrl: "https://picsum.photos/seed/song7/300/300"
  },
  {
    appleCatalogId: 8,
    title: "Save Your Tears",
    artistName: "The Weeknd",
    genre: "Synth-pop",
    releaseDate: "2020-08-20",
    durationSeconds: 215,
    artworkUrl: "https://picsum.photos/seed/song8/300/300"
  }
];

export default function Search() {
  const [query, setQuery] = useState('');
  const debouncedQuery = useDebounce(query, 300);

  const queryClient = useQueryClient();

  const searchQuery = useQuery<SearchResult[]>({
    queryKey: ['search', debouncedQuery],
    queryFn: async () => {
      if (!debouncedQuery.trim()) return [];
      const res = await apiClient.get('/api/search', {
        params: { query: debouncedQuery, type: 'song', limit: 25 },
      });
      return res.data;
    },
    enabled: debouncedQuery.trim().length > 0,
  });

  const libraryIdsQuery = useQuery<Set<number>>({
    queryKey: ['library-ids'],
    queryFn: async () => {
      const res = await apiClient.get('/api/library', { params: { size: 100 } });
      return new Set<number>((res.data.content as LibraryItem[]).map((i) => i.appleCatalogId));
    },
    staleTime: 30_000,
  });

  const saveToLibrary = async (item: SearchResult) => {
    await apiClient.post('/api/library', {
      appleCatalogId: item.appleCatalogId,
      title: item.title,
      artistName: item.artistName,
      genre: item.genre,
      releaseDate: item.releaseDate,
      durationSeconds: item.durationSeconds,
      artworkUrl: item.artworkUrl,
    });
    queryClient.invalidateQueries({ queryKey: ['library-ids'] });
    queryClient.invalidateQueries({ queryKey: ['library'] });
  };

  const isSaved = (id: number) => libraryIdsQuery.data?.has(id) ?? false;

  const showResults = debouncedQuery.trim().length > 0;

  const formatDuration = (seconds: number) => {
    const mins = Math.floor(seconds / 60);
    const secs = seconds % 60;
    return `${mins}:${secs.toString().padStart(2, '0')}`;
  };

  const SongCard = ({ item, index }: { item: SearchResult; index: number }) => (
    <motion.div
      key={item.appleCatalogId}
      className="result-card"
      variants={cardVariants}
      whileHover="hover"
      initial="hidden"
      animate="visible"
      transition={{ duration: 0.3 }}
      style={{ originY: 0 }}
    >
      <img 
        src={item.artworkUrl} 
        alt={item.title} 
        className="artwork"
        loading="lazy"
      />
      <div className="result-info">
        <h3>{item.title}</h3>
        <p className="artist">{item.artistName}</p>
        <p className="meta">{item.genre} • {formatDuration(item.durationSeconds)}</p>
      </div>
      <motion.button
        className="btn-save"
        disabled={isSaved(item.appleCatalogId)}
        onClick={() => saveToLibrary(item)}
        whileHover={{ scale: 1.05 }}
        whileTap={{ scale: 0.95 }}
      >
        {isSaved(item.appleCatalogId) ? '✓ Saved' : '+ Save'}
      </motion.button>
    </motion.div>
  );

  return (
    <div className="page">
      <motion.h1 
        initial={{ opacity: 0, y: -20 }}
        animate={{ opacity: 1, y: 0 }}
        transition={{ duration: 0.5 }}
      >
        Search
      </motion.h1>
      
      <div className="search-bar">
        <SearchIcon className="search-icon" />
        <input
          type="text"
          placeholder="What do you want to listen to?"
          value={query}
          onChange={(e) => setQuery(e.target.value)}
          autoFocus
        />
      </div>

      {searchQuery.isLoading && (
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
      )}

      {searchQuery.error && (
        <motion.p 
          className="error-message"
          initial={{ opacity: 0, y: 10 }}
          animate={{ opacity: 1, y: 0 }}
        >
          {parseError(searchQuery.error)}
        </motion.p>
      )}

      <AnimatePresence>
        {searchQuery.isSuccess && showResults && searchQuery.data.length === 0 && (
          <motion.div 
            className="empty-state"
            initial={{ opacity: 0, scale: 0.95 }}
            animate={{ opacity: 1, scale: 1 }}
            exit={{ opacity: 0, scale: 0.95 }}
          >
            <Music size={48} style={{ marginBottom: 16, opacity: 0.5 }} />
            <p>No results found for "{debouncedQuery}"</p>
            <p style={{ fontSize: '0.9rem', marginTop: 8, color: 'var(--text-tertiary)' }}>
              Try different keywords or check your spelling
            </p>
          </motion.div>
        )}
      </AnimatePresence>

      <AnimatePresence>
        {searchQuery.isSuccess && searchQuery.data && searchQuery.data.length > 0 && (
          <motion.div 
            className="results-grid"
            variants={containerVariants}
            initial="hidden"
            animate="visible"
          >
            {searchQuery.data.map((item, index) => (
              <SongCard key={item.appleCatalogId} item={item} index={index} />
            ))}
          </motion.div>
        )}
      </AnimatePresence>

      <AnimatePresence>
        {!showResults && (
          <>
            <motion.div 
              className="featured-section"
              initial={{ opacity: 0, y: 20 }}
              animate={{ opacity: 1, y: 0 }}
              transition={{ delay: 0.2 }}
            >
              <div className="featured-header">
                <TrendingUp className="featured-icon" />
                <h2>Popular Genres</h2>
              </div>
              <div className="genre-grid">
                {['Pop', 'Rock', 'Hip-Hop', 'Electronic', 'R&B', 'Jazz'].map((genre, index) => (
                  <motion.div
                    key={genre}
                    className="genre-card"
                    initial={{ opacity: 0, scale: 0.9 }}
                    animate={{ opacity: 1, scale: 1 }}
                    transition={{ delay: 0.3 + (index * 0.05) }}
                    whileHover={{ scale: 1.05, y: -4 }}
                    onClick={() => setQuery(genre)}
                  >
                    {genre}
                  </motion.div>
                ))}
              </div>
            </motion.div>

            <motion.div 
              className="featured-section"
              initial={{ opacity: 0, y: 20 }}
              animate={{ opacity: 1, y: 0 }}
              transition={{ delay: 0.4 }}
            >
              <div className="featured-header">
                <Flame className="featured-icon" />
                <h2>Trending Now</h2>
              </div>
              <motion.div 
                className="results-grid"
                variants={containerVariants}
                initial="hidden"
                animate="visible"
              >
                {featuredSongs.map((item, index) => (
                  <SongCard key={item.appleCatalogId} item={item} index={index} />
                ))}
              </motion.div>
            </motion.div>
          </>
        )}
      </AnimatePresence>
    </div>
  );
}
