import React from 'react';
import { Card, CardContent, Typography, Box, CircularProgress, useTheme } from '@mui/material';
import { styled, alpha } from '@mui/material/styles';

const StyledCard = styled(Card)(({ theme }) => ({
  height: '100%',
  display: 'flex',
  flexDirection: 'column',
  transition: 'all 0.3s cubic-bezier(0.25, 0.8, 0.25, 1)',
  position: 'relative',
  overflow: 'hidden',
  backgroundColor: theme.palette.mode === 'dark' ? alpha(theme.palette.background.paper, 0.8) : '#ffffff',
  borderRadius: 20,
  backdropFilter: 'blur(10px)',
  boxShadow: theme.palette.mode === 'dark' 
    ? '0 8px 15px rgba(0,0,0,0.2)'
    : '0 10px 30px rgba(0,0,0,0.07)',
  '&:hover': {
    transform: 'translateY(-5px)',
    boxShadow: theme.palette.mode === 'dark' 
      ? '0 12px 25px rgba(0,0,0,0.25)'
      : '0 15px 35px rgba(0,0,0,0.1)',
  },
  '&:before': {
    content: '""',
    position: 'absolute',
    top: 0,
    left: 0,
    width: '100%',
    height: '4px',
    backgroundImage: theme.palette.mode === 'dark'
      ? 'linear-gradient(to right, rgba(255,255,255,0.1), rgba(255,255,255,0.3))'
      : 'linear-gradient(to right, rgba(0,0,0,0.05), rgba(0,0,0,0.15))',
    opacity: 1,
  }
}));

interface StatCardProps {
  title: string;
  value: string | number;
  subtitle?: string;
  icon?: React.ReactNode;
  loading?: boolean;
  color?: 'primary' | 'secondary' | 'success' | 'error' | 'warning' | 'info';
}

const StatCard: React.FC<StatCardProps> = ({
  title,
  value,
  subtitle,
  icon,
  loading = false,
  color = 'primary',
}) => {
  const theme = useTheme();
  
  return (
    <StyledCard sx={{ '&:before': { backgroundImage: color ? `linear-gradient(to right, ${alpha(theme.palette[color].main, 0.5)}, ${theme.palette[color].main})` : `linear-gradient(to right, ${alpha(theme.palette.primary.main, 0.5)}, ${theme.palette.primary.main})` }}}>
      <CardContent sx={{ p: 3, height: '100%' }}>
        <Box display="flex" justifyContent="space-between" alignItems="flex-start" height="100%">
          <Box>
            <Typography
              variant="subtitle2"
              color="text.secondary"
              gutterBottom
              sx={{ 
                fontWeight: 600, 
                textTransform: 'uppercase', 
                letterSpacing: '0.8px', 
                fontSize: '0.75rem',
                opacity: 0.8,
              }}
            >
              {title}
            </Typography>
            {loading ? (
              <Box display="flex" alignItems="center" height="3rem">
                <CircularProgress size={28} thickness={4} sx={{ color: color ? `${color}.main` : 'primary.main' }} />
              </Box>
            ) : (
              <Typography 
                variant="h4" 
                component="div" 
                sx={{ 
                  fontWeight: 700, 
                  my: 1, 
                  color: color ? `${color}.main` : 'primary.main',
                  fontSize: '1.8rem',
                  letterSpacing: '-0.025em',
                }}
              >
                {value}
              </Typography>
            )}
            {subtitle && (
              <Typography 
                variant="caption" 
                sx={{ 
                  display: 'block',
                  mt: 1,
                  fontSize: '0.75rem',
                  fontWeight: 500,
                  color: theme.palette.mode === 'dark' ? alpha(theme.palette.text.secondary, 0.8) : alpha(theme.palette.text.secondary, 0.9),
                }}
              >
                {subtitle}
              </Typography>
            )}
          </Box>
          {icon && (
            <Box
              sx={{
                background: color 
                  ? `linear-gradient(135deg, ${alpha(theme.palette[color].light, 0.2)}, ${alpha(theme.palette[color].light, 0.4)})`
                  : `linear-gradient(135deg, ${alpha(theme.palette.primary.light, 0.2)}, ${alpha(theme.palette.primary.light, 0.4)})`,
                color: color ? `${color}.main` : 'primary.main',
                p: 1.5,
                borderRadius: '50%',
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'center',
                boxShadow: `0 4px 12px ${alpha(color ? theme.palette[color].main : theme.palette.primary.main, 0.15)}`,
              }}
            >
              {icon}
            </Box>
          )}
        </Box>
      </CardContent>
    </StyledCard>
  );
};

export default StatCard;
