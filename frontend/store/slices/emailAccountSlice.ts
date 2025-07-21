import { createSlice, PayloadAction, createAsyncThunk } from '@reduxjs/toolkit';
import axios from 'axios';

export interface EmailAccount {
  id: string;
  email: string;
  provider: string;
  connected: boolean;
  lastFetched: string | null;
  description?: string;
}

interface EmailAccountState {
  emailAccounts: EmailAccount[];
  loading: boolean;
  error: string | null;
  currentAccount: EmailAccount | null;
  connectionStatus: 'idle' | 'connecting' | 'success' | 'failed';
}

const initialState: EmailAccountState = {
  emailAccounts: [],
  loading: false,
  error: null,
  currentAccount: null,
  connectionStatus: 'idle',
};

export const fetchEmailAccounts = createAsyncThunk(
  'emailAccounts/fetchEmailAccounts',
  async (_, { rejectWithValue }) => {
    try {
      const token = localStorage.getItem('token');
      const response = await axios.get('/api/emails/accounts', {
        headers: { Authorization: `Bearer ${token}` },
      });
      return response.data;
    } catch (error: any) {
      return rejectWithValue(error.response?.data?.message || 'Failed to fetch email accounts');
    }
  }
);

export const connectEmailAccount = createAsyncThunk(
  'emailAccounts/connectAccount',
  async (accountData: Omit<EmailAccount, 'id' | 'connected' | 'lastFetched'>, { rejectWithValue }) => {
    try {
      const token = localStorage.getItem('token');
      const response = await axios.post('/api/emails/connect', accountData, {
        headers: { Authorization: `Bearer ${token}` },
      });
      return response.data;
    } catch (error: any) {
      return rejectWithValue(error.response?.data?.message || 'Failed to connect email account');
    }
  }
);

export const disconnectEmailAccount = createAsyncThunk(
  'emailAccounts/disconnectAccount',
  async (id: string, { rejectWithValue }) => {
    try {
      const token = localStorage.getItem('token');
      await axios.post(`/api/emails/disconnect/${id}`, {}, {
        headers: { Authorization: `Bearer ${token}` },
      });
      return id;
    } catch (error: any) {
      return rejectWithValue(error.response?.data?.message || 'Failed to disconnect email account');
    }
  }
);

export const fetchEmailAccountData = createAsyncThunk(
  'emailAccounts/fetchData',
  async (id: string, { rejectWithValue }) => {
    try {
      const token = localStorage.getItem('token');
      const response = await axios.post(`/api/emails/fetch/${id}`, {}, {
        headers: { Authorization: `Bearer ${token}` },
      });
      return response.data;
    } catch (error: any) {
      return rejectWithValue(error.response?.data?.message || 'Failed to fetch email data');
    }
  }
);

export const emailAccountSlice = createSlice({
  name: 'emailAccounts',
  initialState,
  reducers: {
    setCurrentAccount: (state, action: PayloadAction<EmailAccount | null>) => {
      state.currentAccount = action.payload;
    },
    clearEmailAccountError: (state) => {
      state.error = null;
    },
    resetConnectionStatus: (state) => {
      state.connectionStatus = 'idle';
    }
  },
  extraReducers: (builder) => {
    builder
      .addCase(fetchEmailAccounts.pending, (state) => {
        state.loading = true;
        state.error = null;
      })
      .addCase(fetchEmailAccounts.fulfilled, (state, action) => {
        state.emailAccounts = action.payload;
        state.loading = false;
      })
      .addCase(fetchEmailAccounts.rejected, (state, action) => {
        state.loading = false;
        state.error = action.payload as string;
      })
      .addCase(connectEmailAccount.pending, (state) => {
        state.loading = true;
        state.error = null;
        state.connectionStatus = 'connecting';
      })
      .addCase(connectEmailAccount.fulfilled, (state, action) => {
        state.emailAccounts.push(action.payload);
        state.loading = false;
        state.connectionStatus = 'success';
      })
      .addCase(connectEmailAccount.rejected, (state, action) => {
        state.loading = false;
        state.error = action.payload as string;
        state.connectionStatus = 'failed';
      })
      .addCase(disconnectEmailAccount.pending, (state) => {
        state.loading = true;
        state.error = null;
      })
      .addCase(disconnectEmailAccount.fulfilled, (state, action) => {
        const id = action.payload;
        const index = state.emailAccounts.findIndex(account => account.id === id);
        if (index !== -1) {
          state.emailAccounts[index].connected = false;
        }
        state.loading = false;
      })
      .addCase(disconnectEmailAccount.rejected, (state, action) => {
        state.loading = false;
        state.error = action.payload as string;
      })
      .addCase(fetchEmailAccountData.pending, (state) => {
        state.loading = true;
        state.error = null;
      })
      .addCase(fetchEmailAccountData.fulfilled, (state, action) => {
        // Update lastFetched timestamp for the account
        const id = action.meta.arg;
        const index = state.emailAccounts.findIndex(account => account.id === id);
        if (index !== -1) {
          state.emailAccounts[index].lastFetched = new Date().toISOString();
        }
        state.loading = false;
      })
      .addCase(fetchEmailAccountData.rejected, (state, action) => {
        state.loading = false;
        state.error = action.payload as string;
      });
  },
});

export const { setCurrentAccount, clearEmailAccountError, resetConnectionStatus } = emailAccountSlice.actions;

export default emailAccountSlice.reducer;
