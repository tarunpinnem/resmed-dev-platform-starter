import axios, { AxiosError, InternalAxiosRequestConfig } from 'axios';
import type { ApiResponse, AuthResponse, Patient, PatientRequest, PaginatedResponse, HealthStatus } from '../types';

const API_BASE_URL = '/api/v1';

const api = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
});

// Request interceptor to add auth token
api.interceptors.request.use((config: InternalAxiosRequestConfig) => {
  const token = localStorage.getItem('token');
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

// Response interceptor to handle errors
api.interceptors.response.use(
  (response) => response,
  (error: AxiosError<ApiResponse<unknown>>) => {
    if (error.response?.status === 401) {
      localStorage.removeItem('token');
      localStorage.removeItem('user');
      window.location.href = '/login';
    }
    return Promise.reject(error);
  }
);

// Auth API
export const authApi = {
  login: async (username: string, password: string): Promise<AuthResponse> => {
    const response = await api.post<ApiResponse<AuthResponse>>('/auth/login', {
      username,
      password,
    });
    return response.data.data;
  },
};

// Patient API
export const patientApi = {
  getAll: async (page = 0, size = 10, search?: string): Promise<PaginatedResponse<Patient>> => {
    const params = new URLSearchParams({ page: String(page), size: String(size) });
    if (search) params.append('search', search);
    const response = await api.get<ApiResponse<PaginatedResponse<Patient>>>(`/patients?${params}`);
    return response.data.data;
  },

  getById: async (id: string): Promise<Patient> => {
    const response = await api.get<ApiResponse<Patient>>(`/patients/${id}`);
    return response.data.data;
  },

  create: async (patient: PatientRequest): Promise<Patient> => {
    const response = await api.post<ApiResponse<Patient>>('/patients', patient);
    return response.data.data;
  },

  update: async (id: string, patient: PatientRequest): Promise<Patient> => {
    const response = await api.put<ApiResponse<Patient>>(`/patients/${id}`, patient);
    return response.data.data;
  },

  delete: async (id: string): Promise<void> => {
    await api.delete(`/patients/${id}`);
  },
};

// Health API
export const healthApi = {
  getHealth: async (): Promise<HealthStatus> => {
    const response = await api.get<HealthStatus>('/health');
    return response.data;
  },

  getReady: async (): Promise<HealthStatus> => {
    const response = await api.get<HealthStatus>('/ready');
    return response.data;
  },
};

export default api;
