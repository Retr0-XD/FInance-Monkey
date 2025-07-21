import { createSlice, PayloadAction, createAsyncThunk } from '@reduxjs/toolkit';
import axios from 'axios';

interface DashboardSummary {
  totalTransactions: number;
  totalIncome: number;
  totalExpense: number;
  balance: number;
  transactionsByCategory: {
    categoryName: string;
    amount: number;
    color: string;
  }[];
  transactionsByMonth: {
    month: string;
    income: number;
    expense: number;
  }[];
  recentTransactions: {
    id: string;
    amount: number;
    vendor: string;
    description: string;
    transactionDate: string;
    categoryName?: string;
  }[];
  recurringPayments: {
    id: string;
    amount: number;
    vendor: string;
    recurrencePattern: string;
    nextDueDate?: string;
  }[];
}

interface DashboardState {
  summary: DashboardSummary | null;
  loading: boolean;
  error: string | null;
  dateRange: {
    startDate: string;
    endDate: string;
  };
}

const initialState: DashboardState = {
  summary: null,
  loading: false,
  error: null,
  dateRange: {
    startDate: new Date(new Date().getFullYear(), new Date().getMonth() - 1, 1).toISOString().split('T')[0],
    endDate: new Date().toISOString().split('T')[0],
  },
};

export const fetchDashboardSummary = createAsyncThunk(
  'dashboard/fetchSummary',
  async (dateRange: { startDate: string; endDate: string }, { rejectWithValue }) => {
    try {
      const token = localStorage.getItem('token');
      const response = await axios.get('/api/dashboard/summary', {
        params: dateRange,
        headers: { Authorization: `Bearer ${token}` },
      });
      return response.data;
    } catch (error: any) {
      return rejectWithValue(error.response?.data?.message || 'Failed to fetch dashboard summary');
    }
  }
);

export const dashboardSlice = createSlice({
  name: 'dashboard',
  initialState,
  reducers: {
    setDateRange: (state, action: PayloadAction<{ startDate: string; endDate: string }>) => {
      state.dateRange = action.payload;
    },
    clearDashboardError: (state) => {
      state.error = null;
    },
  },
  extraReducers: (builder) => {
    builder
      .addCase(fetchDashboardSummary.pending, (state) => {
        state.loading = true;
        state.error = null;
      })
      .addCase(fetchDashboardSummary.fulfilled, (state, action) => {
        state.summary = action.payload;
        state.loading = false;
      })
      .addCase(fetchDashboardSummary.rejected, (state, action) => {
        state.loading = false;
        state.error = action.payload as string;
      });
  },
});

export const { setDateRange, clearDashboardError } = dashboardSlice.actions;

export default dashboardSlice.reducer;
