'use client';

import React from 'react';
import { Box, Container, Typography, Button, Grid, Paper } from '@mui/material';
import { styled } from '@mui/material/styles';
import Link from 'next/link';
import { useSession } from 'next-auth/react';
import { useRouter } from 'next/navigation';

// Custom styled components
const HeroSection = styled(Box)(({ theme }) => ({
  background: `linear-gradient(45deg, ${theme.palette.primary.main} 30%, ${theme.palette.secondary.main} 90%)`,
  color: 'white',
  padding: theme.spacing(10, 0),
  borderRadius: theme.shape.borderRadius,
  marginBottom: theme.spacing(4),
}));

const FeatureCard = styled(Paper)(({ theme }) => ({
  padding: theme.spacing(3),
  height: '100%',
  display: 'flex',
  flexDirection: 'column',
  transition: 'transform 0.3s ease-in-out',
  '&:hover': {
    transform: 'translateY(-5px)',
  },
}));

export default function Home() {
  const { data: session } = useSession();
  const router = useRouter();

  // If user is already logged in, redirect to dashboard
  React.useEffect(() => {
    if (session) {
      router.push('/dashboard');
    }
  }, [session, router]);

  // Features list
  const features = [
    {
      title: 'Automated Transaction Tracking',
      description: 'Connect your email accounts to automatically extract and categorize financial transactions.',
      icon: '📧',
    },
    {
      title: 'Intelligent Categorization',
      description: 'AI-powered categorization helps organize your transactions for better financial insights.',
      icon: '🤖',
    },
    {
      title: 'Recurring Payment Detection',
      description: 'Automatically identify and track recurring payments to manage your subscriptions.',
      icon: '🔄',
    },
    {
      title: 'Visual Analytics',
      description: 'Comprehensive charts and graphs to visualize your spending patterns and financial health.',
      icon: '📊',
    },
  ];

  return (
    <Container maxWidth="lg">
      <HeroSection sx={{ mt: 4 }}>
        <Container>
          <Grid container spacing={4} alignItems="center">
            <Grid size={{ xs: 12, md: 7 }}>
              <Typography variant="h2" component="h1" fontWeight="bold" gutterBottom>
                Take Control of Your Finances
              </Typography>
              <Typography variant="h5" paragraph>
                Finance Monkey makes tracking your finances effortless by automatically extracting transactions from your emails.
              </Typography>
              <Box sx={{ mt: 4, display: 'flex', gap: 2 }}>
                <Button 
                  variant="contained" 
                  color="secondary" 
                  size="large"
                  component={Link}
                  href="/register"
                >
                  Get Started - It&apos;s Free
                </Button>
                <Button 
                  variant="outlined" 
                  color="inherit" 
                  size="large"
                  component={Link}
                  href="/login"
                >
                  Sign In
                </Button>
              </Box>
            </Grid>
            <Grid size={{ xs: 12, md: 5 }}>
              <Box sx={{ position: 'relative', height: 300, width: '100%', bgcolor: 'rgba(255,255,255,0.1)', borderRadius: 2 }}>
                {/* Placeholder for hero image */}
                <Typography variant="h4" sx={{ display: 'flex', height: '100%', alignItems: 'center', justifyContent: 'center' }}>
                  Dashboard Preview
                </Typography>
              </Box>
            </Grid>
          </Grid>
        </Container>
      </HeroSection>

      <Box sx={{ mb: 8 }}>
        <Typography variant="h3" component="h2" textAlign="center" fontWeight="bold" gutterBottom>
          How It Works
        </Typography>
        <Typography variant="h6" textAlign="center" color="textSecondary" paragraph>
          Three simple steps to financial clarity
        </Typography>
        
        <Grid container spacing={4} sx={{ mt: 4 }}>
          <Grid size={{ xs: 12, md: 4 }}>
            <FeatureCard elevation={2}>
              <Typography variant="h2" color="primary" textAlign="center">1</Typography>
              <Typography variant="h6" textAlign="center" gutterBottom>Connect Your Email</Typography>
              <Typography variant="body1" textAlign="center">
                Securely connect your email accounts to start extracting financial data from receipts and notifications.
              </Typography>
            </FeatureCard>
          </Grid>
          <Grid size={{ xs: 12, md: 4 }}>
            <FeatureCard elevation={2}>
              <Typography variant="h2" color="primary" textAlign="center">2</Typography>
              <Typography variant="h6" textAlign="center" gutterBottom>AI Processes Your Data</Typography>
              <Typography variant="body1" textAlign="center">
                Our AI analyzes your emails to identify transactions, categorize spending, and detect recurring payments.
              </Typography>
            </FeatureCard>
          </Grid>
          <Grid size={{ xs: 12, md: 4 }}>
            <FeatureCard elevation={2}>
              <Typography variant="h2" color="primary" textAlign="center">3</Typography>
              <Typography variant="h6" textAlign="center" gutterBottom>Gain Financial Insights</Typography>
              <Typography variant="body1" textAlign="center">
                Access interactive dashboards to understand your spending habits and make smarter financial decisions.
              </Typography>
            </FeatureCard>
          </Grid>
        </Grid>
      </Box>

      <Box sx={{ mb: 8 }}>
        <Typography variant="h3" component="h2" textAlign="center" fontWeight="bold" gutterBottom>
          Features
        </Typography>
        <Typography variant="h6" textAlign="center" color="textSecondary" paragraph>
          Everything you need to manage your finances
        </Typography>
        
        <Grid container spacing={4} sx={{ mt: 4 }}>
          {features.map((feature, index) => (
            <Grid size={{ xs: 12, sm: 6 }} key={index}>
              <FeatureCard elevation={2}>
                <Box display="flex" alignItems="center" mb={2}>
                  <Typography variant="h3" mr={2}>{feature.icon}</Typography>
                  <Typography variant="h6" fontWeight="bold">{feature.title}</Typography>
                </Box>
                <Typography variant="body1">{feature.description}</Typography>
              </FeatureCard>
            </Grid>
          ))}
        </Grid>
      </Box>

      <Box sx={{ py: 8, textAlign: 'center' }}>
        <Typography variant="h3" component="h2" fontWeight="bold" gutterBottom>
          Ready to take control of your finances?
        </Typography>
        <Typography variant="h6" color="textSecondary" paragraph>
          Join thousands of users who have simplified their financial management.
        </Typography>
        <Button 
          variant="contained" 
          color="primary" 
          size="large"
          component={Link}
          href="/register"
          sx={{ mt: 2 }}
        >
          Create Your Free Account
        </Button>
      </Box>

      <Box component="footer" sx={{ py: 4, textAlign: 'center', borderTop: 1, borderColor: 'divider' }}>
        <Typography variant="body2" color="text.secondary">
          © {new Date().getFullYear()} Finance Monkey. All rights reserved.
        </Typography>
      </Box>
    </Container>
  );
}
