import React from 'react';
import { Card, CardContent, CardHeader, Box, CircularProgress, useTheme } from '@mui/material';
import { styled, alpha } from '@mui/material/styles';
import { Line, Bar, Pie, Doughnut } from 'react-chartjs-2';
import {
  Chart as ChartJS,
  CategoryScale,
  LinearScale,
  PointElement,
  LineElement,
  BarElement,
  ArcElement,
  Title,
  Tooltip,
  Legend,
  ChartData,
  ChartOptions
} from 'chart.js';

// Register ChartJS components
ChartJS.register(
  CategoryScale,
  LinearScale,
  PointElement,
  LineElement,
  BarElement,
  ArcElement,
  Title,
  Tooltip,
  Legend
);

const StyledCard = styled(Card)(({ theme }) => ({
  height: '100%',
  display: 'flex',
  flexDirection: 'column',
  borderRadius: 20,
  overflow: 'hidden',
  backgroundColor: theme.palette.mode === 'dark' 
    ? alpha(theme.palette.background.paper, 0.8) 
    : '#ffffff',
  backdropFilter: 'blur(10px)',
  position: 'relative',
  boxShadow: theme.palette.mode === 'dark'
    ? '0 10px 25px rgba(0, 0, 0, 0.25)'
    : '0 12px 28px rgba(0, 0, 0, 0.07)',
  transition: 'all 0.3s cubic-bezier(0.25, 0.8, 0.25, 1)',
  border: theme.palette.mode === 'dark'
    ? '1px solid rgba(255, 255, 255, 0.05)'
    : '1px solid rgba(0, 0, 0, 0.03)',
  '&:hover': {
    boxShadow: theme.palette.mode === 'dark'
      ? '0 15px 35px rgba(0, 0, 0, 0.35)'
      : '0 18px 35px rgba(0, 0, 0, 0.1)',
    transform: 'translateY(-5px)',
  },
  '&:before': {
    content: '""',
    position: 'absolute',
    top: 0,
    left: 0,
    right: 0,
    height: '4px',
    backgroundImage: theme.palette.mode === 'dark'
      ? 'linear-gradient(to right, rgba(45, 212, 191, 0.3), rgba(20, 184, 166, 0.7))'
      : 'linear-gradient(to right, rgba(0, 135, 90, 0.3), rgba(0, 95, 63, 0.7))',
  }
}));

type ChartType = 'line' | 'bar' | 'pie' | 'doughnut';

interface ChartCardProps {
  title: string;
  subtitle?: string;
  chartType: ChartType;
  data: ChartData<'line' | 'bar' | 'pie' | 'doughnut'>;
  options?: ChartOptions<'line' | 'bar' | 'pie' | 'doughnut'>;
  loading?: boolean;
  height?: number | string;
  action?: React.ReactNode;
}

const ChartCard: React.FC<ChartCardProps> = ({
  title,
  subtitle,
  chartType,
  data,
  options,
  loading = false,
  height = 300,
  action,
}) => {
  const theme = useTheme();

  const defaultOptions: ChartOptions<'line' | 'bar' | 'pie' | 'doughnut'> = {
    responsive: true,
    maintainAspectRatio: false,
    plugins: {
      legend: {
        position: 'top' as const,
        labels: {
          color: theme.palette.text.primary,
        },
      },
      title: {
        display: false,
      },
      tooltip: {
        mode: 'index',
        intersect: false,
      },
    },
    scales: chartType === 'line' || chartType === 'bar' ? {
      x: {
        grid: {
          color: theme.palette.divider,
        },
        ticks: {
          color: theme.palette.text.secondary,
        },
      },
      y: {
        grid: {
          color: theme.palette.divider,
        },
        ticks: {
          color: theme.palette.text.secondary,
        },
      },
    } : undefined,
  };

  const mergedOptions = { ...defaultOptions, ...options };

  const renderChart = () => {
    switch (chartType) {
      case 'line':
        return <Line data={data as ChartData<'line'>} options={mergedOptions as ChartOptions<'line'>} height={height} />;
      case 'bar':
        return <Bar data={data as ChartData<'bar'>} options={mergedOptions as ChartOptions<'bar'>} height={height} />;
      case 'pie':
        return <Pie data={data as ChartData<'pie'>} options={mergedOptions as ChartOptions<'pie'>} />;
      case 'doughnut':
        return <Doughnut data={data as ChartData<'doughnut'>} options={mergedOptions as ChartOptions<'doughnut'>} />;
      default:
        return null;
    }
  };

  return (
    <StyledCard>
      <CardHeader 
        title={title} 
        subheader={subtitle}
        action={action}
        titleTypographyProps={{ 
          variant: 'h6', 
          fontWeight: 700,
          fontSize: '1.125rem',
          letterSpacing: '-0.01em',
          color: theme.palette.mode === 'dark' 
            ? alpha(theme.palette.common.white, 0.95) 
            : alpha(theme.palette.common.black, 0.87),
        }}
        subheaderTypographyProps={{ 
          variant: 'body2',
          color: alpha(theme.palette.text.secondary, 0.85),
          fontSize: '0.875rem',
          fontWeight: 500,
          marginTop: '4px'
        }}
        sx={{
          paddingBottom: 0.5,
          paddingTop: 2.5,
          paddingX: 3,
          borderBottom: '1px solid',
          borderColor: alpha(theme.palette.divider, 0.6),
          '& .MuiCardHeader-action': {
            margin: 0,
          }
        }}
      />
      <CardContent sx={{ 
        flexGrow: 1, 
        position: 'relative', 
        minHeight: height,
        padding: theme.spacing(3),
        paddingTop: theme.spacing(3),
        backgroundColor: theme.palette.mode === 'dark'
          ? alpha(theme.palette.background.paper, 0.4)
          : alpha(theme.palette.background.default, 0.3),
        '&:last-child': { paddingBottom: theme.spacing(3) }
      }}>
        {loading ? (
          <Box
            sx={{
              display: 'flex',
              justifyContent: 'center',
              alignItems: 'center',
              height: '100%',
              position: 'absolute',
              width: '100%',
              top: 0,
              left: 0,
              bgcolor: alpha(theme.palette.background.paper, 0.6),
              backdropFilter: 'blur(4px)',
              zIndex: 2,
              borderRadius: 2,
            }}
          >
            <CircularProgress 
              size={44} 
              thickness={4.5} 
              sx={{ 
                color: theme.palette.primary.main,
                boxShadow: `0 0 15px ${alpha(theme.palette.primary.main, 0.3)}`,
              }} 
            />
          </Box>
        ) : (
          renderChart()
        )}
      </CardContent>
    </StyledCard>
  );
};

export default ChartCard;
