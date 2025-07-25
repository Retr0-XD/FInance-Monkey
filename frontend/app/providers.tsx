'use client';

import { store } from '@/store';
import { Provider } from 'react-redux';
import { SessionProvider } from 'next-auth/react';

export function ReduxProvider({ children }: { children: React.ReactNode }) {
  return (
    <SessionProvider>
      <Provider store={store}>{children}</Provider>
    </SessionProvider>
  );
}
