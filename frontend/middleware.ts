import { withAuth } from 'next-auth/middleware';
import { NextResponse } from 'next/server';
import { getToken } from 'next-auth/jwt';

export default withAuth(
  async function middleware(req) {
    const token = await getToken({ req });
    const isAuth = !!token;
    const isAuthPage = req.nextUrl.pathname.startsWith('/login') || 
                       req.nextUrl.pathname.startsWith('/register');

    // If they're trying to access a protected route and not logged in
    if (!isAuth && !isAuthPage && req.nextUrl.pathname !== '/') {
      return NextResponse.redirect(new URL('/login', req.url));
    }

    // If they're logged in and trying to access login/register page
    if (isAuth && isAuthPage) {
      return NextResponse.redirect(new URL('/dashboard', req.url));
    }

    return NextResponse.next();
  },
  {
    callbacks: {
      authorized: ({ token }) => true, // Let middleware handle the auth check
    },
  }
);

export const config = { 
  matcher: [
    '/dashboard/:path*',
    '/categories/:path*',
    '/transactions/:path*',
    '/email-accounts/:path*',
    '/login',
    '/register',
  ]
};
