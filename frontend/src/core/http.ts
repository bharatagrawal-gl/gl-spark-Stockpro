import axios from 'axios';
import { environment } from '../environments/environment';

export const http = axios.create({ baseURL: environment.apiUrl });

http.interceptors.request.use((config) => {
  const token = localStorage.getItem('token');
  if (token) {
    config.headers = config.headers ?? {};
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

http.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error?.response?.status === 401) {
      ['token', 'role', 'fullName', 'email'].forEach((key) => localStorage.removeItem(key));
      if (window.location.pathname !== '/login') {
        window.location.assign('/login');
      }
    }
    return Promise.reject(error);
  }
);
