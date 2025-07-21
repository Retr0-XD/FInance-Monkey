'use client';

import * as React from 'react';
import { ThemeProvider, createTheme } from '@mui/material/styles';
import CssBaseline from '@mui/material/CssBaseline';

// Define theme settings
const lightTheme = createTheme({
  palette: {
    mode: 'light',
    primary: {
      main: '#00875A', // Refined green for finance, more modern
      light: '#60AD7F',
      dark: '#005F3F',
      contrastText: '#FFFFFF',
    },
    secondary: {
      main: '#FFB547', // Warm amber for contrast and call-to-actions
      light: '#FFECB3',
      dark: '#FF8F00',
      contrastText: '#000000',
    },
    background: {
      default: '#F8FAFC', // Subtle off-white for better readability
      paper: '#FFFFFF',
    },
    success: {
      main: '#10B981', // Bright green for success states
      light: '#D1FAE5',
      dark: '#047857',
      contrastText: '#FFFFFF',
    },
    error: {
      main: '#EF4444', // Modern red for errors
      light: '#FEE2E2',
      dark: '#B91C1C',
      contrastText: '#FFFFFF',
    },
    info: {
      main: '#3B82F6', // Bright blue for information
      light: '#DBEAFE',
      dark: '#1D4ED8',
      contrastText: '#FFFFFF',
    },
    warning: {
      main: '#F59E0B', // Amber for warnings
      light: '#FEF3C7',
      dark: '#D97706',
      contrastText: '#FFFFFF',
    },
    grey: {
      50: '#F9FAFB',
      100: '#F3F4F6',
      200: '#E5E7EB',
      300: '#D1D5DB',
      400: '#9CA3AF',
      500: '#6B7280',
      600: '#4B5563',
      700: '#374151',
      800: '#1F2937',
      900: '#111827',
    },
  },
  typography: {
    fontFamily: 'var(--font-geist-sans), system-ui, -apple-system, "Segoe UI", Roboto, "Helvetica Neue", Arial, sans-serif',
    h1: {
      fontSize: '2.75rem',
      fontWeight: 700,
      letterSpacing: '-0.025em',
      lineHeight: 1.2,
    },
    h2: {
      fontSize: '2rem',
      fontWeight: 700,
      letterSpacing: '-0.025em',
      lineHeight: 1.3,
    },
    h3: {
      fontSize: '1.75rem',
      fontWeight: 600,
      letterSpacing: '-0.0125em',
      lineHeight: 1.4,
    },
    h4: {
      fontSize: '1.5rem',
      fontWeight: 600,
      letterSpacing: '-0.0125em',
      lineHeight: 1.4,
    },
    h5: {
      fontSize: '1.25rem',
      fontWeight: 600,
      lineHeight: 1.5,
    },
    h6: {
      fontSize: '1.125rem',
      fontWeight: 600,
      lineHeight: 1.5,
    },
    body1: {
      fontSize: '1rem',
      lineHeight: 1.6,
    },
    body2: {
      fontSize: '0.875rem',
      lineHeight: 1.6,
    },
    button: {
      fontSize: '0.875rem',
      fontWeight: 500,
      letterSpacing: '0.025em',
    },
    caption: {
      fontSize: '0.75rem',
      lineHeight: 1.5,
      color: '#6B7280',
    },
  },
  shape: {
    borderRadius: 10,
  },
  components: {
    MuiButton: {
      styleOverrides: {
        root: {
          borderRadius: 8,
          textTransform: 'none',
          padding: '10px 18px',
          fontWeight: 500,
          boxShadow: 'none',
          transition: 'all 0.2s ease',
          '&:hover': {
            boxShadow: '0px 4px 10px rgba(0, 0, 0, 0.08)',
            transform: 'translateY(-1px)',
          },
        },
        contained: {
          boxShadow: '0px 2px 6px rgba(0, 0, 0, 0.1)',
        },
        outlined: {
          borderWidth: '1.5px',
          '&:hover': {
            borderWidth: '1.5px',
          },
        },
      },
    },
    MuiCard: {
      styleOverrides: {
        root: {
          borderRadius: 16,
          boxShadow: '0px 5px 20px rgba(0, 0, 0, 0.06)',
          overflow: 'hidden',
          transition: 'all 0.25s ease-in-out',
          '&:hover': {
            boxShadow: '0px 8px 25px rgba(0, 0, 0, 0.1)',
          },
        },
      },
    },
    MuiCardContent: {
      styleOverrides: {
        root: {
          padding: '24px',
          '&:last-child': {
            paddingBottom: '24px',
          },
        },
      },
    },
    MuiPaper: {
      styleOverrides: {
        root: {
          borderRadius: 12,
          backgroundImage: 'none',
        },
        elevation1: {
          boxShadow: '0px 2px 10px rgba(0, 0, 0, 0.05)',
        },
        elevation2: {
          boxShadow: '0px 4px 15px rgba(0, 0, 0, 0.07)',
        },
      },
    },
    MuiTextField: {
      styleOverrides: {
        root: {
          '& .MuiOutlinedInput-root': {
            borderRadius: 8,
            '& fieldset': {
              borderColor: '#E5E7EB',
              transition: 'all 0.2s',
            },
            '&:hover fieldset': {
              borderColor: '#D1D5DB',
            },
            '&.Mui-focused fieldset': {
              borderWidth: '1.5px',
            },
          },
        },
      },
    },
    MuiDrawer: {
      styleOverrides: {
        paper: {
          borderRight: 'none',
          boxShadow: '3px 0 15px rgba(0, 0, 0, 0.05)',
        },
      },
    },
    MuiAppBar: {
      styleOverrides: {
        root: {
          boxShadow: '0px 1px 5px rgba(0, 0, 0, 0.05)',
        },
      },
    },
    MuiListItem: {
      styleOverrides: {
        root: {
          borderRadius: 8,
          marginBottom: 4,
        },
      },
    },
    MuiChip: {
      styleOverrides: {
        root: {
          borderRadius: 6,
          fontWeight: 500,
        },
      },
    },
    MuiAlert: {
      styleOverrides: {
        root: {
          borderRadius: 8,
          padding: '12px 16px',
        },
        icon: {
          opacity: 0.9,
        },
      },
    },
  },
});

const darkTheme = createTheme({
  palette: {
    mode: 'dark',
    primary: {
      main: '#2DD4BF', // Bright teal for primary actions in dark mode
      light: '#5EEAD4',
      dark: '#14B8A6',
      contrastText: '#000000',
    },
    secondary: {
      main: '#F59E0B', // Amber for secondary actions
      light: '#FCD34D',
      dark: '#D97706',
      contrastText: '#000000',
    },
    background: {
      default: '#121212',
      paper: '#1E1E1E',
    },
    success: {
      main: '#10B981', // Bright green for success states
      light: '#34D399',
      dark: '#059669',
    },
    error: {
      main: '#EF4444', // Modern red for errors
      light: '#F87171',
      dark: '#B91C1C',
    },
    info: {
      main: '#3B82F6', // Bright blue for information
      light: '#60A5FA',
      dark: '#2563EB',
    },
    warning: {
      main: '#F59E0B', // Amber for warnings
      light: '#FBBF24',
      dark: '#D97706',
    },
  },
  typography: {
    fontFamily: 'var(--font-geist-sans), system-ui, -apple-system, "Segoe UI", Roboto, "Helvetica Neue", Arial, sans-serif',
    h1: {
      fontSize: '2.75rem',
      fontWeight: 700,
      letterSpacing: '-0.025em',
      lineHeight: 1.2,
    },
    h2: {
      fontSize: '2rem',
      fontWeight: 700,
      letterSpacing: '-0.025em',
      lineHeight: 1.3,
    },
    h3: {
      fontSize: '1.75rem',
      fontWeight: 600,
      letterSpacing: '-0.0125em',
      lineHeight: 1.4,
    },
    h4: {
      fontSize: '1.5rem',
      fontWeight: 600,
      letterSpacing: '-0.0125em',
      lineHeight: 1.4,
    },
  },
  components: {
    MuiButton: {
      styleOverrides: {
        root: {
          borderRadius: 8,
          textTransform: 'none',
          padding: '10px 18px',
          fontWeight: 500,
          boxShadow: 'none',
          transition: 'all 0.2s ease',
          '&:hover': {
            boxShadow: '0px 4px 10px rgba(0, 0, 0, 0.2)',
            transform: 'translateY(-1px)',
          },
        },
        contained: {
          boxShadow: '0px 2px 6px rgba(0, 0, 0, 0.2)',
        },
      },
    },
    MuiCard: {
      styleOverrides: {
        root: {
          borderRadius: 12,
          boxShadow: '0px 3px 15px rgba(0, 0, 0, 0.3)',
          background: '#1E1E1E',
          border: '1px solid rgba(255, 255, 255, 0.05)',
        },
      },
    },
    MuiPaper: {
      styleOverrides: {
        root: {
          backgroundImage: 'none',
          border: '1px solid rgba(255, 255, 255, 0.05)',
        },
      },
    },
  },
});

export default function ThemeRegistry({ children }: { children: React.ReactNode }) {
  const [darkMode, setDarkMode] = React.useState(false);
  
  // Check user preference on mount
  React.useEffect(() => {
    // Check local storage first
    const savedMode = localStorage.getItem('theme');
    if (savedMode) {
      setDarkMode(savedMode === 'dark');
    } else {
      // Check system preference
      const prefersDark = window.matchMedia('(prefers-color-scheme: dark)').matches;
      setDarkMode(prefersDark);
    }
  }, []);

  // Apply the selected theme
  const theme = darkMode ? darkTheme : lightTheme;

  return (
    <ThemeProvider theme={theme}>
      <CssBaseline />
      {children}
    </ThemeProvider>
  );
}
