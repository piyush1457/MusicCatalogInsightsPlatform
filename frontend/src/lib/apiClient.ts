import axios, { AxiosError } from 'axios';

const TOKEN_KEY = 'music_catalog_token';

const apiClient = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL,
  headers: { 'Content-Type': 'application/json' },
});

apiClient.interceptors.request.use((config) => {
  const token = localStorage.getItem(TOKEN_KEY);
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

apiClient.interceptors.response.use(
  (response) => response,
  (error: AxiosError<{ message?: string }>) => {
    if (error.response?.status === 401) {
      localStorage.removeItem(TOKEN_KEY);
      window.location.href = '/login';
    }
    return Promise.reject(error);
  },
);

export function parseError(error: unknown): string {
  if (error instanceof AxiosError && error.response?.data?.message) {
    return error.response.data.message;
  }
  if (error instanceof Error) return error.message;
  return 'An unexpected error occurred';
}

export { TOKEN_KEY };
export default apiClient;
