'use client';

import React, { useState } from 'react';
import { Box, Container, Grid, Typography, Button, Card, CardContent, Tabs, Tab, TextField, MenuItem, InputAdornment } from '@mui/material';
import { useAppDispatch, useAppSelector } from '@/lib/hooks';
import { fetchTransactions, Transaction, createTransaction, updateTransaction, deleteTransaction } from '@/store/slices/transactionSlice';
import { fetchCategories } from '@/store/slices/categorySlice';
import { SideNav, DataTable } from '../components';
import SearchIcon from '@mui/icons-material/Search';
import AddIcon from '@mui/icons-material/Add';
import EditIcon from '@mui/icons-material/Edit';
import DeleteIcon from '@mui/icons-material/Delete';
import { format } from 'date-fns';
import { IconButton, Dialog, DialogTitle, DialogContent, DialogActions, FormControl, InputLabel, Select, FormHelperText, SelectChangeEvent } from '@mui/material';

interface TabPanelProps {
  children?: React.ReactNode;
  index: number;
  value: number;
}

function TabPanel(props: TabPanelProps) {
  const { children, value, index, ...other } = props;

  return (
    <div
      role="tabpanel"
      hidden={value !== index}
      id={`transaction-tabpanel-${index}`}
      aria-labelledby={`transaction-tab-${index}`}
      {...other}
    >
      {value === index && <Box sx={{ pt: 3 }}>{children}</Box>}
    </div>
  );
}

interface TransactionFormData {
  id?: string;
  amount: number;
  currency: string;
  vendor: string;
  description: string;
  transactionDate: string;
  recurring: boolean;
  recurrencePattern?: string;
  categoryId?: string;
}

export default function Transactions() {
  const dispatch = useAppDispatch();
  const { transactions, loading, error } = useAppSelector((state) => state.transactions);
  const { categories } = useAppSelector((state) => state.categories);
  const [tabValue, setTabValue] = useState(0);
  const [search, setSearch] = useState('');
  const [openDialog, setOpenDialog] = useState(false);
  const [formData, setFormData] = useState<TransactionFormData>({
    amount: 0,
    currency: 'USD',
    vendor: '',
    description: '',
    transactionDate: new Date().toISOString().split('T')[0],
    recurring: false,
  });
  const [formErrors, setFormErrors] = useState<Record<string, string>>({});
  const [editMode, setEditMode] = useState(false);

  React.useEffect(() => {
    dispatch(fetchTransactions());
    dispatch(fetchCategories());
  }, [dispatch]);

  const handleTabChange = (event: React.SyntheticEvent, newValue: number) => {
    setTabValue(newValue);
  };

  const handleSearchChange = (event: React.ChangeEvent<HTMLInputElement>) => {
    setSearch(event.target.value);
  };

  const formatCurrency = (value: number) => {
    return new Intl.NumberFormat('en-US', {
      style: 'currency',
      currency: 'USD',
    }).format(value);
  };

  const filteredTransactions = transactions.filter((transaction) => {
    const searchLower = search.toLowerCase();
    return (
      transaction.vendor.toLowerCase().includes(searchLower) ||
      transaction.description.toLowerCase().includes(searchLower) ||
      (transaction.categoryName && transaction.categoryName.toLowerCase().includes(searchLower))
    );
  });

  const incomeTransactions = filteredTransactions.filter((transaction) => transaction.amount > 0);
  const expenseTransactions = filteredTransactions.filter((transaction) => transaction.amount < 0);

  const handleOpenDialog = (transaction?: Transaction) => {
    if (transaction) {
      // Edit mode
      setFormData({
        id: transaction.id,
        amount: Math.abs(transaction.amount),
        currency: transaction.currency,
        vendor: transaction.vendor,
        description: transaction.description,
        transactionDate: transaction.transactionDate.split('T')[0],
        recurring: transaction.recurring,
        recurrencePattern: transaction.recurrencePattern,
        categoryId: transaction.categoryId,
      });
      setEditMode(true);
    } else {
      // Create mode
      setFormData({
        amount: 0,
        currency: 'USD',
        vendor: '',
        description: '',
        transactionDate: new Date().toISOString().split('T')[0],
        recurring: false,
      });
      setEditMode(false);
    }
    setOpenDialog(true);
    setFormErrors({});
  };

  const handleCloseDialog = () => {
    setOpenDialog(false);
  };

  const handleFormChange = (event: React.ChangeEvent<HTMLInputElement | { name?: string; value: unknown }>) => {
    const { name, value } = event.target;
    if (name) {
      if (name === 'recurring' && value === false) {
        setFormData((prev) => ({
          ...prev,
          [name]: value,
          recurrencePattern: undefined,
        }));
      } else {
        setFormData((prev) => ({
          ...prev,
          [name]: value,
        }));
      }
    }
  };

  const validateForm = () => {
    const errors: Record<string, string> = {};
    if (!formData.vendor) errors.vendor = 'Vendor is required';
    if (!formData.description) errors.description = 'Description is required';
    if (!formData.amount) errors.amount = 'Amount is required';
    if (!formData.transactionDate) errors.transactionDate = 'Date is required';
    if (formData.recurring && !formData.recurrencePattern) {
      errors.recurrencePattern = 'Recurrence pattern is required for recurring transactions';
    }
    return errors;
  };

  const handleSubmit = () => {
    const errors = validateForm();
    if (Object.keys(errors).length > 0) {
      setFormErrors(errors);
      return;
    }

    // Determine if expense or income based on the tab value
    const isExpense = tabValue === 1;
    const amount = isExpense ? -Math.abs(formData.amount) : Math.abs(formData.amount);

    const transactionData = {
      ...formData,
      amount,
    };

    if (editMode && formData.id) {
      dispatch(updateTransaction({ ...transactionData, id: formData.id } as Transaction));
    } else {
      dispatch(createTransaction(transactionData));
    }

    handleCloseDialog();
  };

  const handleDeleteTransaction = (transaction: Transaction) => {
    if (window.confirm('Are you sure you want to delete this transaction?')) {
      dispatch(deleteTransaction(transaction.id));
    }
  };

  const transactionColumns: Array<{
    id: keyof Transaction | 'actions';
    label: string;
    minWidth?: number;
    align?: 'right' | 'left' | 'center';
    format?: (value: unknown, row?: Transaction) => React.ReactNode;
  }> = [
    { id: 'transactionDate', label: 'Date', minWidth: 100, format: (value: unknown) => format(new Date(value as string), 'MMM dd, yyyy') },
    { id: 'description', label: 'Description', minWidth: 170 },
    { id: 'vendor', label: 'Vendor', minWidth: 100 },
    { id: 'categoryName', label: 'Category', minWidth: 100 },
    { 
      id: 'amount', 
      label: 'Amount', 
      minWidth: 100, 
      align: 'right' as const,
      format: (value: unknown) => (
        <Typography
          variant="body2"
          color={(value as number) < 0 ? 'error' : 'success.main'}
          fontWeight="bold"
        >
          {formatCurrency(value as number)}
        </Typography>
      ),
    },
    {
      id: 'actions',
      label: 'Actions',
      minWidth: 100,
      align: 'right' as const,
      format: (_value: unknown, row?: Transaction) => (
        <Box>
          <IconButton size="small" onClick={() => row && handleOpenDialog(row)}>
            <EditIcon fontSize="small" />
          </IconButton>
          <IconButton size="small" onClick={() => row && handleDeleteTransaction(row)} color="error">
            <DeleteIcon fontSize="small" />
          </IconButton>
        </Box>
      ),
    },
  ];

  const recurrenceOptions = [
    { value: 'DAILY', label: 'Daily' },
    { value: 'WEEKLY', label: 'Weekly' },
    { value: 'BIWEEKLY', label: 'Bi-weekly' },
    { value: 'MONTHLY', label: 'Monthly' },
    { value: 'QUARTERLY', label: 'Quarterly' },
    { value: 'YEARLY', label: 'Yearly' },
  ];

  return (
    <SideNav>
      <Container maxWidth="xl">
        <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 3 }}>
          <Typography variant="h4" fontWeight="bold">Transactions</Typography>
          <Box display="flex" gap={2}>
            <TextField
              placeholder="Search transactions..."
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
              onClick={() => handleOpenDialog()}
            >
              Add Transaction
            </Button>
          </Box>
        </Box>

        <Card>
          <CardContent>
            <Tabs value={tabValue} onChange={handleTabChange} aria-label="transaction tabs">
              <Tab label="All Transactions" />
              <Tab label="Expenses" />
              <Tab label="Income" />
            </Tabs>

            <TabPanel value={tabValue} index={0}>
              <DataTable
                columns={transactionColumns}
                rows={filteredTransactions}
                loading={loading}
                emptyMessage="No transactions found"
              />
            </TabPanel>
            <TabPanel value={tabValue} index={1}>
              <DataTable
                columns={transactionColumns}
                rows={expenseTransactions}
                loading={loading}
                emptyMessage="No expense transactions found"
              />
            </TabPanel>
            <TabPanel value={tabValue} index={2}>
              <DataTable
                columns={transactionColumns}
                rows={incomeTransactions}
                loading={loading}
                emptyMessage="No income transactions found"
              />
            </TabPanel>
          </CardContent>
        </Card>

        <Dialog open={openDialog} onClose={handleCloseDialog} maxWidth="sm" fullWidth>
          <DialogTitle>{editMode ? 'Edit Transaction' : 'Add Transaction'}</DialogTitle>
          <DialogContent>
            <Box component="form" sx={{ mt: 2 }}>
              <Grid container spacing={2}>
                <Grid size={{ xs: 12 }}>
                  <TextField
                    fullWidth
                    label="Vendor"
                    name="vendor"
                    value={formData.vendor}
                    onChange={handleFormChange}
                    error={!!formErrors.vendor}
                    helperText={formErrors.vendor}
                  />
                </Grid>
                <Grid size={{ xs: 12 }}>
                  <TextField
                    fullWidth
                    label="Description"
                    name="description"
                    value={formData.description}
                    onChange={handleFormChange}
                    error={!!formErrors.description}
                    helperText={formErrors.description}
                  />
                </Grid>
                <Grid size={{ xs: 6 }}>
                  <TextField
                    fullWidth
                    label="Amount"
                    name="amount"
                    type="number"
                    value={formData.amount}
                    onChange={handleFormChange}
                    error={!!formErrors.amount}
                    helperText={formErrors.amount}
                    InputProps={{
                      startAdornment: <InputAdornment position="start">$</InputAdornment>,
                    }}
                  />
                </Grid>
                <Grid size={{ xs: 6 }}>
                  <TextField
                    fullWidth
                    label="Date"
                    name="transactionDate"
                    type="date"
                    value={formData.transactionDate}
                    onChange={handleFormChange}
                    error={!!formErrors.transactionDate}
                    helperText={formErrors.transactionDate}
                    InputLabelProps={{ shrink: true }}
                  />
                </Grid>
                <Grid size={{ xs: 12 }}>
                  <FormControl fullWidth>
                    <InputLabel>Category</InputLabel>
                    <Select
                      name="categoryId"
                      value={formData.categoryId || ''}
                      onChange={handleFormChange as (event: SelectChangeEvent<string>) => void}
                      label="Category"
                    >
                      <MenuItem value="">
                        <em>None</em>
                      </MenuItem>
                      {categories.map((category) => (
                        <MenuItem key={category.id} value={category.id}>
                          {category.name}
                        </MenuItem>
                      ))}
                    </Select>
                  </FormControl>
                </Grid>
                <Grid size={{ xs: 12 }}>
                  <FormControl fullWidth>
                    <InputLabel>Recurring</InputLabel>
                    <Select
                      name="recurring" /* Line 395 specific fix below */
                      value={formData.recurring}
                      onChange={handleFormChange as (event: SelectChangeEvent<boolean>) => void}
                      label="Recurring"
                    >
                      <MenuItem value={false}>No</MenuItem>
                      <MenuItem value={true}>Yes</MenuItem>
                    </Select>
                  </FormControl>
                </Grid>
                {formData.recurring && (
                  <Grid size={{ xs: 12 }}>
                    <FormControl fullWidth error={!!formErrors.recurrencePattern}>
                      <InputLabel>Recurrence Pattern</InputLabel>
                      <Select
                        name="recurrencePattern"
                        value={formData.recurrencePattern || ''}
                        onChange={handleFormChange as (event: SelectChangeEvent<string>) => void}
                        label="Recurrence Pattern"
                      >
                        {recurrenceOptions.map((option) => (
                          <MenuItem key={option.value} value={option.value}>
                            {option.label}
                          </MenuItem>
                        ))}
                      </Select>
                      {formErrors.recurrencePattern && (
                        <FormHelperText>{formErrors.recurrencePattern}</FormHelperText>
                      )}
                    </FormControl>
                  </Grid>
                )}
              </Grid>
            </Box>
          </DialogContent>
          <DialogActions>
            <Button onClick={handleCloseDialog}>Cancel</Button>
            <Button onClick={handleSubmit} variant="contained">
              {editMode ? 'Update' : 'Save'}
            </Button>
          </DialogActions>
        </Dialog>
      </Container>
    </SideNav>
  );
}
