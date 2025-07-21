This is a [Next.js](https://nextjs.org) project bootstrapped with [`create-next-app`](https://nextjs.org/docs/app/api-reference/cli/create-next-app).

## Getting Started

First, run the development server:

```bash
npm run dev
# or
yarn dev
# or
pnpm dev
# or
bun dev
```

Open [http://localhost:3000](http://localhost:3000) with your browser to see the result.

You can start editing the page by modifying `app/page.tsx`. The page auto-updates as you edit the file.

This project uses [`next/font`](https://nextjs.org/docs/app/building-your-application/optimizing/fonts) to automatically optimize and load [Geist](https://vercel.com/font), a new font family for Vercel.

## Learn More

To learn more about Next.js, take a look at the following resources:

- [Next.js Documentation](https://nextjs.org/docs) - learn about Next.js features and API.
- [Learn Next.js](https://nextjs.org/learn) - an interactive Next.js tutorial.

You can check out [the Next.js GitHub repository](https://github.com/vercel/next.js) - your feedback and contributions are welcome!

## Deploy on Vercel

### Deployment Steps

1. **Create a Vercel Account**:
   - Visit [Vercel](https://vercel.com) and sign up or log in

2. **Connect to GitHub**:
   - Import your Finance Monkey repository from GitHub
   - Select the frontend directory as the root directory for deployment

3. **Configure Environment Variables**:
   - Add the following environment variables in the Vercel project settings:
     - `NEXT_PUBLIC_API_URL`: Your backend API URL (e.g., https://finance-monkey-backend.onrender.com/api)
     - `NEXTAUTH_URL`: Your frontend URL (e.g., https://finance-monkey.vercel.app)
     - `NEXTAUTH_SECRET`: A secure random string for NextAuth.js
     - Any other environment variables needed by your frontend

4. **Deploy**:
   - Click "Deploy" and Vercel will automatically build and deploy your frontend
   - The vercel.json file in this directory contains the configuration for your deployment

### Important Notes

- Make sure the backend is deployed and accessible before deploying the frontend
- Update the CORS configuration in your backend to allow requests from your Vercel domain
- The frontend communicates with the backend API through the URL specified in `NEXT_PUBLIC_API_URL`
