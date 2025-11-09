'use client';

import React from 'react';
import { Box, Container, Grid, Typography, Button, IconButton, Card, CardContent, useTheme, Skeleton, MenuItem, TextField } from '@mui/material';
import { useAppDispatch, useAppSelector } from '@/lib/hooks';
import { fetchDashboardSummary, setDateRange } from '@/store/slices/dashboardSlice';
import { SideNav } from '../components';
import StatCard from '../components/StatCard';
import ChartCard from '../components/ChartCard';
import DataTable from '../components/DataTable';
import MoneyOffIcon from '@mui/icons-material/MoneyOff';
import AccountBalanceIcon from '@mui/icons-material/AccountBalance';
import TrendingUpIcon from '@mui/icons-material/TrendingUp';
import TrendingDownIcon from '@mui/icons-material/TrendingDown';
import AttachMoneyIcon from '@mui/icons-material/AttachMoney';
import FilterListIcon from '@mui/icons-material/FilterList';
import { format } from 'date-fns';

export default function Dashboard() {
  const theme = useTheme();
  const dispatch = useAppDispatch();
  const { summary, loading, dateRange } = useAppSelector((state) => state.dashboard);

  const formatCurrency = (value: number) => {
    return new Intl.NumberFormat('en-US', {
      style: 'currency',
      currency: 'USD',
    }).format(value);
  };

  React.useEffect(() => {
    dispatch(fetchDashboardSummary(dateRange));
  }, [dispatch, dateRange]);

  const handleDateRangeChange = (event: React.ChangeEvent<HTMLInputElement>) => {
    const { name, value } = event.target;
    dispatch(setDateRange({ ...dateRange, [name]: value }));
  };

  // Chart data for monthly transactions
  const monthlyChartData = {
    labels: summary?.transactionsByMonth?.map(item => item.month) || [],
    datasets: [
      {
        label: 'Income',
        data: summary?.transactionsByMonth?.map(item => item.income) || [],
        backgroundColor: theme.palette.success.main,
        borderColor: theme.palette.success.main,
        borderWidth: 2,
      },
      {
        label: 'Expense',
        data: summary?.transactionsByMonth?.map(item => item.expense) || [],
        backgroundColor: theme.palette.error.main,
        borderColor: theme.palette.error.main,
        borderWidth: 2,
      },
    ],
  };

  // Chart data for category breakdown
  const categoryChartData = {
    labels: summary?.transactionsByCategory?.map(item => item.categoryName) || [],
    datasets: [
      {
        data: summary?.transactionsByCategory?.map(item => Math.abs(item.amount)) || [],
        backgroundColor: summary?.transactionsByCategory?.map(item => item.color || '#' + Math.floor(Math.random() * 16777215).toString(16)) || [],
        borderWidth: 1,
      },
    ],
  };

  type TransactionRow = {
    id: string;
    amount: number;
    vendor: string;
    description: string;
    transactionDate: string;
    categoryName?: string;
  };

  const transactionColumns: Array<{
    id: keyof TransactionRow | 'actions';
    label: string;
    minWidth?: number;
    align?: 'right' | 'left' | 'center';
    format?: (value: unknown, row?: TransactionRow) => React.ReactNode;
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
  ];

  return (
    <SideNav>
      <Container maxWidth="xl">
        <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 3 }}>
          <Typography variant="h4" fontWeight="bold">Dashboard</Typography>
          <Box sx={{ display: 'flex', gap: 2 }}>
            <TextField
              name="startDate"
              label="Start Date"
              type="date"
              value={dateRange.startDate}
              onChange={handleDateRangeChange}
              InputLabelProps={{ shrink: true }}
              size="small"
            />
            <TextField
              name="endDate"
              label="End Date"
              type="date"
              value={dateRange.endDate}
              onChange={handleDateRangeChange}
              InputLabelProps={{ shrink: true }}
              size="small"
            />
          </Box>
        </Box>

        <Grid container spacing={3} mb={4}>
          <Grid size={{ xs: 12, sm: 6, md: 3 }}>
            <StatCard
              title="Total Balance"
              value={loading ? '---' : formatCurrency(summary?.balance || 0)}
              icon={<AccountBalanceIcon />}
              loading={loading}
            />
          </Grid>
          <Grid size={{ xs: 12, sm: 6, md: 3 }}>
            <StatCard
              title="Total Income"
              value={loading ? '---' : formatCurrency(summary?.totalIncome || 0)}
              icon={<TrendingUpIcon />}
              loading={loading}
              color="success"
            />
          </Grid>
          <Grid size={{ xs: 12, sm: 6, md: 3 }}>
            <StatCard
              title="Total Expense"
              value={loading ? '---' : formatCurrency(Math.abs(summary?.totalExpense || 0))}
              icon={<TrendingDownIcon />}
              loading={loading}
              color="error"
            />
          </Grid>
          <Grid size={{ xs: 12, sm: 6, md: 3 }}>
            <StatCard
              title="Transaction Count"
              value={loading ? '---' : summary?.totalTransactions || 0}
              icon={<AttachMoneyIcon />}
              loading={loading}
              color="info"
            />
          </Grid>
        </Grid>

        <Grid container spacing={3} mb={4}>
          <Grid size={{ xs: 12, md: 8 }}>
            <ChartCard
              title="Monthly Income vs. Expense"
              chartType="bar"
              data={monthlyChartData}
              loading={loading}
              height={300}
            />
          </Grid>
          <Grid size={{ xs: 12, md: 4 }}>
            <ChartCard
              title="Spending by Category"
              chartType="doughnut"
              data={categoryChartData}
              loading={loading}
              height={300}
            />
          </Grid>
        </Grid>

        <Box sx={{ mb: 4 }}>
          <Typography variant="h6" sx={{ mb: 2 }}>Recent Transactions</Typography>
          <DataTable
            columns={transactionColumns}
            rows={summary?.recentTransactions || []}
            loading={loading}
            emptyMessage="No recent transactions found"
          />
        </Box>

        <Grid container spacing={3}>
          <Grid size={{ xs: 12 }}>
            <Card>
              <CardContent>
                <Typography variant="h6" gutterBottom>Recurring Payments</Typography>
                {loading ? (
                  <Box sx={{ mt: 2 }}>
                    <Skeleton height={40} />
                    <Skeleton height={40} />
                    <Skeleton height={40} />
                  </Box>
                ) : summary?.recurringPayments?.length ? (
                  <DataTable
                    columns={[
                      { id: 'vendor', label: 'Vendor', minWidth: 100 },
                      { id: 'recurrencePattern', label: 'Frequency', minWidth: 100 },
                      { 
                        id: 'amount', 
                        label: 'Amount', 
                        minWidth: 100,
                        align: 'right' as const,
                        format: (value: unknown) => (
                          <Typography variant="body2" fontWeight="bold">
                            {formatCurrency(Math.abs(value as number))}
                          </Typography>
                        ),
                      },
                      { 
                        id: 'nextDueDate', 
                        label: 'Next Due Date', 
                        minWidth: 100,
                        format: (value: unknown) => (value as string) ? format(new Date(value as string), 'MMM dd, yyyy') : 'N/A',
                      },
                    ]}
                    rows={summary.recurringPayments}
                    emptyMessage="No recurring payments found"
                  />
                ) : (
                  <Typography variant="body2" color="text.secondary" sx={{ mt: 2 }}>
                    No recurring payments found
                  </Typography>
                )}
              </CardContent>
            </Card>
          </Grid>
        </Grid>
      </Container>
    </SideNav>
  );
}
