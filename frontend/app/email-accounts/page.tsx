'use client';

import React, { useState } from 'react';
import { Box, Container, Grid, Typography, Button, Card, CardContent, TextField, InputAdornment, Dialog, DialogTitle, DialogContent, DialogActions } from '@mui/material';
import { useAppDispatch, useAppSelector } from '@/lib/hooks';
import { fetchEmailAccounts, connectEmailAccount, disconnectEmailAccount, fetchEmailAccountData, EmailAccount } from '@/store/slices/emailAccountSlice';
import { SideNav, DataTable } from '../components';
import SearchIcon from '@mui/icons-material/Search';
import AddIcon from '@mui/icons-material/Add';
import RefreshIcon from '@mui/icons-material/Refresh';
import DeleteIcon from '@mui/icons-material/Delete';
import { format } from 'date-fns';
import { IconButton, Chip, CircularProgress, Alert } from '@mui/material';

interface EmailAccountFormData {
  email: string;
  provider: string;
  description?: string;
}

export default function EmailAccounts() {
  const dispatch = useAppDispatch();
  const { emailAccounts, loading, error, connectionStatus } = useAppSelector((state) => state.emailAccounts);
  const [search, setSearch] = useState('');
  const [openDialog, setOpenDialog] = useState(false);
  const [formData, setFormData] = useState<EmailAccountFormData>({
    email: '',
    provider: 'GMAIL',
  });
  const [formErrors, setFormErrors] = useState<Record<string, string>>({});
  const [refreshingId, setRefreshingId] = useState<string | null>(null);

  React.useEffect(() => {
    dispatch(fetchEmailAccounts());
  }, [dispatch]);

  const handleSearchChange = (event: React.ChangeEvent<HTMLInputElement>) => {
    setSearch(event.target.value);
  };

  const filteredAccounts = emailAccounts.filter((account) => {
    const searchLower = search.toLowerCase();
    return (
      account.email.toLowerCase().includes(searchLower) ||
      account.provider.toLowerCase().includes(searchLower) ||
      (account.description && account.description.toLowerCase().includes(searchLower))
    );
  });

  const handleOpenDialog = () => {
    setFormData({
      email: '',
      provider: 'GMAIL',
    });
    setOpenDialog(true);
    setFormErrors({});
  };

  const handleCloseDialog = () => {
    setOpenDialog(false);
  };

  const handleFormChange = (event: React.ChangeEvent<HTMLInputElement | { name?: string; value: unknown }>) => {
    const { name, value } = event.target;
    if (name) {
      setFormData((prev) => ({
        ...prev,
        [name]: value,
      }));
    }
  };

  const validateForm = () => {
    const errors: Record<string, string> = {};
    if (!formData.email) errors.email = 'Email is required';
    if (!formData.email.includes('@')) errors.email = 'Please enter a valid email';
    if (!formData.provider) errors.provider = 'Provider is required';
    return errors;
  };

  const handleSubmit = () => {
    const errors = validateForm();
    if (Object.keys(errors).length > 0) {
      setFormErrors(errors);
      return;
    }

    dispatch(connectEmailAccount(formData));
    handleCloseDialog();
  };

  const handleRefreshEmailAccount = async (account: EmailAccount) => {
    setRefreshingId(account.id);
    await dispatch(fetchEmailAccountData(account.id));
    setRefreshingId(null);
  };

  const handleDisconnectAccount = (account: EmailAccount) => {
    if (window.confirm(`Are you sure you want to disconnect ${account.email}?`)) {
      dispatch(disconnectEmailAccount(account.id));
    }
  };

  const emailAccountColumns: Array<{
    id: keyof EmailAccount | 'actions';
    label: string;
    minWidth?: number;
    align?: 'right' | 'left' | 'center';
    format?: (value: unknown, row?: EmailAccount) => React.ReactNode;
  }> = [
    { id: 'email', label: 'Email Address', minWidth: 200 },
    { id: 'provider', label: 'Provider', minWidth: 100 },
    { 
      id: 'connected', 
      label: 'Status', 
      minWidth: 100,
      format: (value: unknown) => (
        <Chip 
          label={(value as boolean) ? 'Connected' : 'Disconnected'} 
          color={(value as boolean) ? 'success' : 'default'} 
          size="small" 
        />
      ),
    },
    { 
      id: 'lastFetched', 
      label: 'Last Synced', 
      minWidth: 150,
      format: (value: unknown) => (value as string | null) ? format(new Date(value as string), 'MMM dd, yyyy HH:mm') : 'Never',
    },
    {
      id: 'actions',
      label: 'Actions',
      minWidth: 100,
      align: 'right' as const,
      format: (_value: unknown, row?: EmailAccount) => (
        <Box>
          <IconButton 
            size="small" 
            onClick={() => row && handleRefreshEmailAccount(row)}
            disabled={!row?.connected || refreshingId === row?.id}
          >
            {refreshingId === row?.id ? (
              <CircularProgress size={20} />
            ) : (
              <RefreshIcon fontSize="small" />
            )}
          </IconButton>
          <IconButton 
            size="small" 
            onClick={() => row && handleDisconnectAccount(row)} 
            color="error"
            disabled={!row?.connected}
          >
            <DeleteIcon fontSize="small" />
          </IconButton>
        </Box>
      ),
    },
  ];

  return (
    <SideNav>
      <Container maxWidth="xl">
        <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 3 }}>
          <Typography variant="h4" fontWeight="bold">Email Accounts</Typography>
          <Box display="flex" gap={2}>
            <TextField
              placeholder="Search accounts..."
              variant="outlined"
              size="small"
              value={search}
              onChange={handleSearchChange}
              InputProps={{
                startAdornment: (
                  <InputAdornment position="start">
                    <SearchIcon />
                  </InputAdornment>
                ),
              }}
            />
            <Button 
              variant="contained" 
              startIcon={<AddIcon />}
              onClick={handleOpenDialog}
            >
              Connect Email
            </Button>
          </Box>
        </Box>

        {error && <Alert severity="error" sx={{ mb: 3 }}>{error}</Alert>}

        <Card>
          <CardContent>
            <Typography variant="h6" gutterBottom>Connected Email Accounts</Typography>
            <Typography variant="body2" color="text.secondary" paragraph>
              Connect your email accounts to automatically process financial transactions from your inbox.
            </Typography>
            
            <DataTable
              columns={emailAccountColumns}
              rows={filteredAccounts}
              loading={loading}
              emptyMessage="No email accounts connected yet. Click 'Connect Email' to get started."
            />
          </CardContent>
        </Card>

        <Dialog open={openDialog} onClose={handleCloseDialog} maxWidth="sm" fullWidth>
          <DialogTitle>Connect Email Account</DialogTitle>
          <DialogContent>
            <Box component="form" sx={{ mt: 2 }}>
              <Grid container spacing={2}>
                <Grid size={{ xs: 12 }}>
                  <TextField
                    fullWidth
                    label="Email Address"
                    name="email"
                    type="email"
                    value={formData.email}
                    onChange={handleFormChange}
                    error={!!formErrors.email}
                    helperText={formErrors.email}
                  />
                </Grid>
                <Grid size={{ xs: 12 }}>
                  <TextField
                    fullWidth
                    label="Provider"
                    name="provider"
                    select
                    value={formData.provider}
                    onChange={handleFormChange}
                    error={!!formErrors.provider}
                    helperText={formErrors.provider}
                    SelectProps={{
                      native: true,
                    }}
                  >
                    <option value="GMAIL">Gmail</option>
                    <option value="OUTLOOK">Outlook</option>
                    <option value="YAHOO">Yahoo Mail</option>
                    <option value="OTHER">Other</option>
                  </TextField>
                </Grid>
                <Grid size={{ xs: 12 }}>
                  <TextField
                    fullWidth
                    label="Description (Optional)"
                    name="description"
                    value={formData.description || ''}
                    onChange={handleFormChange}
                    placeholder="e.g., Work Email, Personal Email"
                  />
                </Grid>
              </Grid>

              <Box sx={{ mt: 3 }}>
                <Alert severity="info">
                  After connecting, you will need to authorize Finance Monkey to access your email account. You will be redirected to the provider&apos;s login page.
                </Alert>
              </Box>
            </Box>
          </DialogContent>
          <DialogActions>
            <Button onClick={handleCloseDialog}>Cancel</Button>
            <Button 
              onClick={handleSubmit} 
              variant="contained" 
              disabled={connectionStatus === 'connecting'}
            >
              {connectionStatus === 'connecting' ? <CircularProgress size={24} /> : 'Connect'}
            </Button>
          </DialogActions>
        </Dialog>
      </Container>
    </SideNav>
  );
}
