'use client';

import React, { useState } from 'react';
import { styled, Theme, CSSObject, alpha } from '@mui/material/styles';
import Box from '@mui/material/Box';
import MuiDrawer from '@mui/material/Drawer';
import MuiAppBar, { AppBarProps as MuiAppBarProps } from '@mui/material/AppBar';
import Toolbar from '@mui/material/Toolbar';
import List from '@mui/material/List';
import Typography from '@mui/material/Typography';
import IconButton from '@mui/material/IconButton';
import MenuIcon from '@mui/icons-material/Menu';
import ChevronLeftIcon from '@mui/icons-material/ChevronLeft';
import DashboardIcon from '@mui/icons-material/Dashboard';
import AccountBalanceWalletIcon from '@mui/icons-material/AccountBalanceWallet';
import EmailIcon from '@mui/icons-material/Email';
import CategoryIcon from '@mui/icons-material/Category';
import SettingsIcon from '@mui/icons-material/Settings';
import LogoutIcon from '@mui/icons-material/Logout';
import Avatar from '@mui/material/Avatar';
import { ListItem, ListItemButton, ListItemIcon, ListItemText, Menu, MenuItem } from '@mui/material';
import { useRouter, usePathname } from 'next/navigation';
import { signOut, useSession } from 'next-auth/react';

const drawerWidth = 260;

const openedMixin = (theme: Theme): CSSObject => ({
  width: drawerWidth,
  transition: theme.transitions.create('width', {
    easing: theme.transitions.easing.easeInOut,
    duration: theme.transitions.duration.enteringScreen,
  }),
  overflowX: 'hidden',
  backgroundImage: theme.palette.mode === 'dark'
    ? 'linear-gradient(180deg, rgba(30,30,30,0.9) 0%, rgba(18,18,18,0.95) 100%)'
    : 'linear-gradient(180deg, rgba(255,255,255,0.99) 0%, rgba(248,250,252,0.95) 100%)',
  backdropFilter: 'blur(10px)',
  borderRight: theme.palette.mode === 'dark' 
    ? '1px solid rgba(255, 255, 255, 0.05)'
    : '1px solid rgba(0, 0, 0, 0.06)',
});

const closedMixin = (theme: Theme): CSSObject => ({
  transition: theme.transitions.create('width', {
    easing: theme.transitions.easing.easeInOut,
    duration: theme.transitions.duration.leavingScreen,
  }),
  overflowX: 'hidden',
  width: `calc(${theme.spacing(7)} + 1px)`,
  [theme.breakpoints.up('sm')]: {
    width: `calc(${theme.spacing(8)} + 1px)`,
  },
  backgroundImage: theme.palette.mode === 'dark'
    ? 'linear-gradient(180deg, rgba(30,30,30,0.9) 0%, rgba(18,18,18,0.95) 100%)'
    : 'linear-gradient(180deg, rgba(255,255,255,0.99) 0%, rgba(248,250,252,0.95) 100%)',
  backdropFilter: 'blur(10px)',
  borderRight: theme.palette.mode === 'dark' 
    ? '1px solid rgba(255, 255, 255, 0.05)'
    : '1px solid rgba(0, 0, 0, 0.06)',
});

const DrawerHeader = styled('div')(({ theme }) => ({
  display: 'flex',
  alignItems: 'center',
  justifyContent: 'flex-end',
  padding: theme.spacing(0, 1),
  // necessary for content to be below app bar
  ...theme.mixins.toolbar,
}));

interface AppBarProps extends MuiAppBarProps {
  open?: boolean;
}

const AppBar = styled(MuiAppBar, {
  shouldForwardProp: (prop) => prop !== 'open',
})<AppBarProps>(({ theme, open }) => ({
  zIndex: theme.zIndex.drawer + 1,
  transition: theme.transitions.create(['width', 'margin'], {
    easing: theme.transitions.easing.sharp,
    duration: theme.transitions.duration.leavingScreen,
  }),
  ...(open && {
    marginLeft: drawerWidth,
    width: `calc(100% - ${drawerWidth}px)`,
    transition: theme.transitions.create(['width', 'margin'], {
      easing: theme.transitions.easing.sharp,
      duration: theme.transitions.duration.enteringScreen,
    }),
  }),
}));

const Drawer = styled(MuiDrawer, { shouldForwardProp: (prop) => prop !== 'open' })(
  ({ theme, open }) => ({
    width: drawerWidth,
    flexShrink: 0,
    whiteSpace: 'nowrap',
    boxSizing: 'border-box',
    ...(open && {
      ...openedMixin(theme),
      '& .MuiDrawer-paper': openedMixin(theme),
    }),
    ...(!open && {
      ...closedMixin(theme),
      '& .MuiDrawer-paper': closedMixin(theme),
    }),
  }),
);

export default function SideNav({ children }: { children: React.ReactNode }) {
  const [open, setOpen] = useState(true);
  const [anchorEl, setAnchorEl] = useState<null | HTMLElement>(null);
  const router = useRouter();
  const pathname = usePathname();
  const { data: session } = useSession();

  const handleDrawerOpen = () => {
    setOpen(true);
  };

  const handleDrawerClose = () => {
    setOpen(false);
  };

  const handleMenuClick = (event: React.MouseEvent<HTMLElement>) => {
    setAnchorEl(event.currentTarget);
  };

  const handleMenuClose = () => {
    setAnchorEl(null);
  };

  const handleLogout = async () => {
    handleMenuClose();
    await signOut({ redirect: false });
    router.push('/login');
  };

  const navigationItems = [
    { 
      text: 'Dashboard', 
      icon: <DashboardIcon />, 
      path: '/dashboard' 
    },
    { 
      text: 'Transactions', 
      icon: <AccountBalanceWalletIcon />, 
      path: '/transactions' 
    },
    { 
      text: 'Email Accounts', 
      icon: <EmailIcon />, 
      path: '/email-accounts' 
    },
    { 
      text: 'Categories', 
      icon: <CategoryIcon />, 
      path: '/categories' 
    },
    { 
      text: 'Settings', 
      icon: <SettingsIcon />, 
      path: '/settings' 
    }
  ];

  return (
    <Box sx={{ display: 'flex' }}>
      <AppBar 
        position="fixed" 
        open={open}
        elevation={0}
        sx={{ 
          backdropFilter: 'blur(12px)',
          backgroundColor: (theme) => theme.palette.mode === 'dark' 
            ? alpha(theme.palette.background.default, 0.8)
            : alpha(theme.palette.background.default, 0.85),
          color: 'text.primary',
          borderBottom: '1px solid',
          borderColor: 'divider',
          boxShadow: (theme) => theme.palette.mode === 'dark'
            ? '0 4px 15px rgba(0,0,0,0.2)'
            : '0 2px 10px rgba(0,0,0,0.05)'
        }}
      >
        <Toolbar sx={{ justifyContent: 'space-between' }}>
          <Box sx={{ display: 'flex', alignItems: 'center' }}>
            <IconButton
              color="inherit"
              aria-label="open drawer"
              onClick={handleDrawerOpen}
              edge="start"
              sx={{
                marginRight: 2,
                ...(open && { display: 'none' }),
              }}
            >
              <MenuIcon />
            </IconButton>
            {!open && (
              <Typography variant="h6" fontWeight="bold" sx={{ color: 'primary.main', display: { xs: 'none', sm: 'block' } }}>
                Finance Monkey
              </Typography>
            )}
          </Box>
          
          <Box sx={{ display: 'flex', alignItems: 'center', gap: 2 }}>
            <Box sx={{ 
              display: 'flex',
              alignItems: 'center',
              px: 2,
              py: 1,
              borderRadius: 2,
              backgroundColor: (theme) => alpha(theme.palette.background.paper, 0.8),
              backdropFilter: 'blur(10px)',
              boxShadow: (theme) => theme.palette.mode === 'dark'
                ? '0 2px 8px rgba(0,0,0,0.25)'
                : '0 2px 12px rgba(0,0,0,0.08)',
              border: '1px solid',
              borderColor: 'divider',
              mr: 1.5
            }}>
              <Typography 
                variant="body2" 
                fontWeight={600}
                sx={{
                  color: (theme) => theme.palette.mode === 'dark'
                    ? alpha(theme.palette.common.white, 0.9)
                    : alpha(theme.palette.common.black, 0.85),
                  letterSpacing: '0.01em'
                }}
              >
                {session?.user?.name || 'Guest User'}
              </Typography>
            </Box>
            
            <IconButton 
              onClick={handleMenuClick} 
              sx={{ 
                p: 0.5, 
                border: '2px solid',
                borderColor: 'primary.main',
                boxShadow: (theme) => `0 0 8px ${alpha(theme.palette.primary.main, 0.4)}`,
                transition: 'all 0.2s ease',
                '&:hover': {
                  transform: 'scale(1.05)',
                  boxShadow: (theme) => `0 0 12px ${alpha(theme.palette.primary.main, 0.6)}`
                }
              }}
            >
              <Avatar 
                alt={session?.user?.name || 'User'} 
                src="/avatar.png" 
                sx={{ width: 32, height: 32 }}
              />
            </IconButton>
          </Box>
          
          <Menu
            anchorEl={anchorEl}
            open={Boolean(anchorEl)}
            onClose={handleMenuClose}
            transformOrigin={{ horizontal: 'right', vertical: 'top' }}
            anchorOrigin={{ horizontal: 'right', vertical: 'bottom' }}
            PaperProps={{
              elevation: 0,
              sx: {
                overflow: 'visible',
                filter: 'drop-shadow(0px 3px 12px rgba(0,0,0,0.25))',
                mt: 1.5,
                borderRadius: 2,
                minWidth: 180,
                border: (theme) => `1px solid ${theme.palette.divider}`,
                backgroundColor: (theme) => theme.palette.mode === 'dark' 
                  ? alpha(theme.palette.background.paper, 0.8)
                  : alpha(theme.palette.background.paper, 0.95),
                backdropFilter: 'blur(10px)',
                '&:before': {
                  content: '""',
                  display: 'block',
                  position: 'absolute',
                  top: 0,
                  right: 14,
                  width: 10,
                  height: 10,
                  bgcolor: (theme) => theme.palette.mode === 'dark' 
                    ? alpha(theme.palette.background.paper, 0.8)
                    : alpha(theme.palette.background.paper, 0.95),
                  transform: 'translateY(-50%) rotate(45deg)',
                  zIndex: 0,
                  borderTop: (theme) => `1px solid ${theme.palette.divider}`,
                  borderLeft: (theme) => `1px solid ${theme.palette.divider}`,
                },
                '& .MuiMenuItem-root': {
                  borderRadius: 1,
                  mx: 0.5,
                  my: 0.3,
                  transition: 'all 0.15s ease',
                  '&:hover': {
                    backgroundColor: (theme) => alpha(theme.palette.primary.main, 0.1),
                  }
                },
                '& .MuiAvatar-root': {
                  width: 32,
                  height: 32,
                  ml: -0.5,
                  mr: 1,
                },
              },
            }}
          >
            <MenuItem 
              onClick={() => { handleMenuClose(); router.push('/profile'); }}
              sx={{ 
                py: 1.2,
                fontWeight: 500
              }}
            >
              <ListItemIcon>
                <Avatar 
                  alt={session?.user?.name || 'User'} 
                  src="/avatar.png"
                  sx={{ 
                    width: 24, 
                    height: 24,
                    boxShadow: '0 2px 4px rgba(0,0,0,0.1)'
                  }}
                />
              </ListItemIcon>
              <Typography variant="body2" fontWeight={500}>Profile</Typography>
            </MenuItem>
            <MenuItem 
              onClick={handleLogout}
              sx={{ 
                py: 1.2,
                fontWeight: 500
              }}
            >
              <ListItemIcon>
                <LogoutIcon 
                  fontSize="small"
                  sx={{
                    color: 'error.main',
                  }}
                />
              </ListItemIcon>
              <Typography 
                variant="body2" 
                fontWeight={500}
                sx={{ color: 'error.main' }}
              >
                Logout
              </Typography>
            </MenuItem>
          </Menu>
        </Toolbar>
      </AppBar>
      <Drawer variant="permanent" open={open}>
        <Box sx={{ 
          display: 'flex', 
          alignItems: 'center',
          justifyContent: open ? 'space-between' : 'center',
          py: 2.5, 
          px: open ? 3 : 2,
          mb: 1,
          position: 'relative',
          '&::after': {
            content: '""',
            position: 'absolute',
            bottom: 0,
            left: '50%',
            transform: 'translateX(-50%)',
            width: '80%',
            height: '1px',
            backgroundImage: (theme) => theme.palette.mode === 'dark'
              ? 'linear-gradient(90deg, rgba(255,255,255,0), rgba(255,255,255,0.1), rgba(255,255,255,0))'
              : 'linear-gradient(90deg, rgba(0,0,0,0), rgba(0,0,0,0.06), rgba(0,0,0,0))',
          }
        }}>
          {open && (
            <Typography 
              variant="h6" 
              fontWeight="bold" 
              sx={{ 
                display: 'flex', 
                alignItems: 'center', 
                color: 'primary.main',
                letterSpacing: '-0.025em',
                fontSize: '1.4rem',
                background: (theme) => theme.palette.mode === 'dark'
                  ? 'linear-gradient(90deg, #2DD4BF, #14B8A6)'
                  : 'linear-gradient(90deg, #00875A, #005F3F)',
                WebkitBackgroundClip: 'text',
                WebkitTextFillColor: 'transparent',
                textShadow: (theme) => theme.palette.mode === 'dark'
                  ? '0 0 25px rgba(45, 212, 191, 0.3)'
                  : '0 0 15px rgba(0, 135, 90, 0.15)'
              }}
            >
              Finance Monkey
            </Typography>
          )}
          <IconButton 
            onClick={handleDrawerClose}
            sx={{
              border: '1px solid',
              borderColor: 'divider',
              borderRadius: '8px',
              padding: '4px',
              '&:hover': {
                backgroundColor: (theme) => alpha(theme.palette.primary.main, 0.08),
              }
            }}
          >
            <ChevronLeftIcon />
          </IconButton>
        </Box>
        {/* Removed divider as we have a visual separator in the Box above */}
        <List sx={{ px: 1 }}>
          {navigationItems.map((item) => {
            const isActive = pathname === item.path;
            return (
              <ListItem key={item.text} disablePadding sx={{ display: 'block', mb: 1 }}>
                <ListItemButton
                  sx={{
                    minHeight: 48,
                    justifyContent: open ? 'initial' : 'center',
                    px: open ? 2 : 1.5,
                    py: 1.2,
                    borderRadius: 2,
                    transition: 'all 0.2s ease',
                    backgroundColor: (theme) => isActive 
                      ? theme.palette.mode === 'dark' 
                        ? alpha(theme.palette.primary.main, 0.2)
                        : alpha(theme.palette.primary.main, 0.1)
                      : 'transparent',
                    '&:hover': {
                      backgroundColor: (theme) => isActive 
                        ? theme.palette.mode === 'dark' 
                          ? alpha(theme.palette.primary.main, 0.3)
                          : alpha(theme.palette.primary.main, 0.15)
                        : theme.palette.mode === 'dark'
                          ? alpha(theme.palette.action.hover, 0.2)
                          : alpha(theme.palette.action.hover, 0.8),
                      transform: 'translateX(5px)'
                    },
                    position: 'relative',
                    ...(isActive && {
                      '&::before': {
                        content: '""',
                        position: 'absolute',
                        left: 0,
                        top: '25%',
                        height: '50%',
                        width: 4,
                        backgroundColor: 'primary.main',
                        borderRadius: '0 4px 4px 0',
                      }
                    })
                  }}
                  onClick={() => router.push(item.path)}
                >
                  <ListItemIcon
                    sx={{
                      minWidth: 0,
                      mr: open ? 2.5 : 'auto',
                      justifyContent: 'center',
                      color: isActive ? 'primary.main' : 'inherit',
                      transition: 'transform 0.2s ease',
                      '& .MuiSvgIcon-root': {
                        fontSize: '1.4rem',
                        transition: 'all 0.2s ease',
                        ...(isActive && {
                          transform: 'scale(1.1)',
                        })
                      }
                    }}
                  >
                    {item.icon}
                  </ListItemIcon>
                  <ListItemText 
                    primary={item.text} 
                    sx={{ 
                      opacity: open ? 1 : 0,
                      '& .MuiTypography-root': { 
                        fontWeight: isActive ? 600 : 500,
                        color: isActive ? 'primary.main' : 'inherit',
                        transition: 'color 0.2s ease',
                        fontSize: '0.95rem',
                        letterSpacing: isActive ? '0.02em' : 'normal',
                      }
                    }} 
                  />
                </ListItemButton>
              </ListItem>
            );
          })}
        </List>
      </Drawer>
      <Box 
        component="main" 
        sx={{ 
          flexGrow: 1, 
          p: 3,
          pt: 2,
          transition: 'padding 0.3s ease',
          height: '100vh',
          overflowY: 'auto',
          position: 'relative',
          '&:after': {
            content: '""',
            position: 'fixed',
            top: 0,
            right: 0,
            width: '100%',
            height: '100%',
            pointerEvents: 'none',
            backgroundImage: (theme) => theme.palette.mode === 'dark'
              ? 'radial-gradient(circle at top right, rgba(45, 212, 191, 0.03), transparent 70%)'
              : 'radial-gradient(circle at top right, rgba(0, 135, 90, 0.02), transparent 70%)',
            zIndex: -1,
          }
        }}
      >
        <DrawerHeader />
        {children}
      </Box>
    </Box>
  );
}
