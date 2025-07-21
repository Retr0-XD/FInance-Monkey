import { useSelector, useDispatch } from 'react-redux';
import { RootState, AppDispatch } from '../store';
import { useAuth } from './hooks/useAuth';

// Use this hook throughout the app instead of useDispatch and useSelector
export const useAppDispatch = () => useDispatch<AppDispatch>();
export const useAppSelector = <T>(selector: (state: RootState) => T): T => useSelector<RootState, T>(selector);

export { useAuth };
