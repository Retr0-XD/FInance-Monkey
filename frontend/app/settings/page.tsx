'use client';

import React, { useState } from 'react';
import { Box, Container, Typography, Paper, TextField, Button, Stack, Alert } from '@mui/material';
import { SideNav } from '../components';
import { useSession } from 'next-auth/react';
import SettingsIcon from '@mui/icons-material/Settings';
import LockIcon from '@mui/icons-material/Lock';
import AccountCircleIcon from '@mui/icons-material/AccountCircle';
import NotificationsIcon from '@mui/icons-material/Notifications';

export default function Settings() {
  const { data: session } = useSession();
  const [name, setName] = useState(session?.user?.name || '');
  const [email, setEmail] = useState(session?.user?.email || '');
  const [currentPassword, setCurrentPassword] = useState('');
  const [newPassword, setNewPassword] = useState('');
  const [confirmPassword, setConfirmPassword] = useState('');
  const [success, setSuccess] = useState<string | null>(null);
  const [error, setError] = useState<string | null>(null);

  const handleProfileUpdate = async (e: React.FormEvent) => {
    e.preventDefault();
    setError(null);
    setSuccess(null);
    
    // Update user profile logic would go here
    try {
      // Mock successful update
      setSuccess('Profile updated successfully!');
    } catch {
      setError('Failed to update profile');
    }
  };

  const handlePasswordChange = async (e: React.FormEvent) => {
    e.preventDefault();
    setError(null);
    setSuccess(null);
    
    // Validate passwords
    if (newPassword !== confirmPassword) {
      setError('New passwords do not match');
      return;
    }
    
    if (newPassword.length < 8) {
      setError('Password must be at least 8 characters long');
      return;
    }
    
    // Update password logic would go here
    try {
      // Mock successful password change
      setSuccess('Password changed successfully!');
      setCurrentPassword('');
      setNewPassword('');
      setConfirmPassword('');
    } catch {
      setError('Failed to change password');
    }
  };

  const settingsContent = (
    <Container maxWidth="lg" sx={{ mt: 4, mb: 4 }}>
      <Typography variant="h4" component="h1" gutterBottom fontWeight="bold" sx={{ display: 'flex', alignItems: 'center' }}>
        <SettingsIcon sx={{ mr: 1 }} /> Settings
      </Typography>
      
      {success && (
        <Alert severity="success" sx={{ mb: 3 }}>
          {success}
        </Alert>
      )}
      
      {error && (
        <Alert severity="error" sx={{ mb: 3 }}>
          {error}
        </Alert>
      )}
      
      <Stack spacing={3}>
        <Box sx={{ display: 'flex', flexDirection: { xs: 'column', md: 'row' }, gap: 3 }}>
          <Paper 
            sx={{ 
              p: 3, 
              display: 'flex', 
              flexDirection: 'column',
              borderRadius: 2,
              flex: 1
            }}
          >
            <Typography variant="h6" component="h2" gutterBottom sx={{ display: 'flex', alignItems: 'center' }}>
              <AccountCircleIcon sx={{ mr: 1 }} /> Profile Information
            </Typography>
            <Box component="form" onSubmit={handleProfileUpdate} noValidate>
              <TextField
                margin="normal"
                fullWidth
                id="name"
                label="Full Name"
                name="name"
                value={name}
                onChange={(e) => setName(e.target.value)}
              />
              <TextField
                margin="normal"
                fullWidth
                id="email"
                label="Email Address"
                name="email"
                type="email"
                value={email}
                onChange={(e) => setEmail(e.target.value)}
              />
              <Button
                type="submit"
                variant="contained"
                sx={{ mt: 3 }}
              >
                Save Changes
              </Button>
            </Box>
          </Paper>
          
          <Paper 
            sx={{ 
              p: 3, 
              display: 'flex', 
              flexDirection: 'column',
              borderRadius: 2,
              flex: 1
            }}
          >
            <Typography variant="h6" component="h2" gutterBottom sx={{ display: 'flex', alignItems: 'center' }}>
              <LockIcon sx={{ mr: 1 }} /> Change Password
            </Typography>
            <Box component="form" onSubmit={handlePasswordChange} noValidate>
              <TextField
                margin="normal"
                fullWidth
                name="currentPassword"
                label="Current Password"
                type="password"
                id="currentPassword"
                value={currentPassword}
                onChange={(e) => setCurrentPassword(e.target.value)}
              />
              <TextField
                margin="normal"
                fullWidth
                name="newPassword"
                label="New Password"
                type="password"
                id="newPassword"
                value={newPassword}
                onChange={(e) => setNewPassword(e.target.value)}
              />
              <TextField
                margin="normal"
                fullWidth
                name="confirmPassword"
                label="Confirm New Password"
                type="password"
                id="confirmPassword"
                value={confirmPassword}
                onChange={(e) => setConfirmPassword(e.target.value)}
              />
              <Button
                type="submit"
                variant="contained"
                sx={{ mt: 3 }}
              >
                Update Password
              </Button>
            </Box>
          </Paper>
        </Box>
        
        <Paper 
          sx={{ 
            p: 3, 
            display: 'flex', 
            flexDirection: 'column',
            borderRadius: 2
          }}
        >
          <Typography variant="h6" component="h2" gutterBottom sx={{ display: 'flex', alignItems: 'center' }}>
            <NotificationsIcon sx={{ mr: 1 }} /> Notification Settings
          </Typography>
          <Typography variant="body2" color="text.secondary" paragraph>
            Configure your notification preferences to stay updated about your finances.
          </Typography>
          {/* Notification settings would go here */}
          <Button variant="outlined" sx={{ alignSelf: 'flex-start' }}>
            Configure Notifications
          </Button>
        </Paper>
      </Stack>
    </Container>
  );

  return (
    <SideNav>
      {settingsContent}
    </SideNav>
  );
}
