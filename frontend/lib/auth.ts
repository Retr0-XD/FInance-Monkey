import { NextAuthOptions } from 'next-auth';
import CredentialsProvider from 'next-auth/providers/credentials';
import GoogleProvider from 'next-auth/providers/google';
import { login } from '@/store/slices/authSlice';
import { store } from '@/store';
import api from './api';

export const authOptions: NextAuthOptions = {
  providers: [
    CredentialsProvider({
      name: 'Credentials',
      credentials: {
        email: { label: 'Email', type: 'email' },
        password: { label: 'Password', type: 'password' },
      },
      async authorize(credentials) {
        try {
          if (!credentials?.email || !credentials?.password) {
            throw new Error('Email and password are required');
          }

          const response = await api.post('/auth/login', {
            email: credentials.email,
            password: credentials.password,
          });

          const { token, user, refreshToken } = response.data;

          // Store token in localStorage for API calls
          if (typeof window !== 'undefined') {
            localStorage.setItem('token', token);
            localStorage.setItem('refreshToken', refreshToken);
            localStorage.setItem('user', JSON.stringify(user));
          }

          // Update Redux state
          store.dispatch(login({ token, user }));

          return {
            id: user.id,
            email: user.email,
            name: user.name,
          };
        } catch (error: any) {
          throw new Error(error.response?.data?.message || 'Authentication failed');
        }
      },
    }),
    GoogleProvider({
      clientId: process.env.GOOGLE_CLIENT_ID || '',
      clientSecret: process.env.GOOGLE_CLIENT_SECRET || '',
      authorization: {
        params: {
          prompt: 'consent',
          access_type: 'offline',
          response_type: 'code',
        },
      },
    }),
  ],
  callbacks: {
    async jwt({ token, user }) {
      if (user) {
        token.id = user.id;
      }
      return token;
    },
    async session({ session, token }) {
      if (token && session.user) {
        session.user = {
          ...session.user,
          id: token.id as string
        };
      }
      return session;
    },
    async signIn({ account, profile }) {
      if (account?.provider === 'google' && profile?.email) {
        try {
          // Handle Google OAuth sign-in
          const response = await api.post('/auth/google', {
            email: profile.email,
            name: profile.name,
            googleId: profile.sub,
          });
          
          const { token, user, refreshToken } = response.data;
          
          if (typeof window !== 'undefined') {
            localStorage.setItem('token', token);
            localStorage.setItem('refreshToken', refreshToken);
            localStorage.setItem('user', JSON.stringify(user));
          }
          
          // Update Redux state
          store.dispatch(login({ token, user }));
          
          return true;
        } catch (error) {
          console.error('Google sign in error:', error);
          return false;
        }
      }
      return true;
    },
  },
  pages: {
    signIn: '/login',
    error: '/login',
  },
  session: {
    strategy: 'jwt',
  },
  secret: process.env.NEXTAUTH_SECRET,
};
