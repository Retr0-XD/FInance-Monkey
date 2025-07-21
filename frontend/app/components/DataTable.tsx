'use client';

import React from 'react';
import {
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  Paper,
  Box,
  CircularProgress,
  Typography,
  TablePagination,
  Chip
} from '@mui/material';
import { alpha } from '@mui/material/styles';

interface Column<T> {
  id: keyof T | 'actions';
  label: string;
  minWidth?: number;
  align?: 'right' | 'left' | 'center';
  format?: (value: any, row: T) => React.ReactNode;
}

interface DataTableProps<T> {
  columns: Column<T>[];
  rows: T[];
  loading?: boolean;
  emptyMessage?: string;
  rowsPerPageOptions?: number[];
  onRowClick?: (row: T) => void;
}

function DataTable<T extends { id: string | number }>({
  columns,
  rows,
  loading = false,
  emptyMessage = 'No data available',
  rowsPerPageOptions = [5, 10, 25, 50],
  onRowClick,
}: DataTableProps<T>) {
  const [page, setPage] = React.useState(0);
  const [rowsPerPage, setRowsPerPage] = React.useState(10);

  const handleChangePage = (event: unknown, newPage: number) => {
    setPage(newPage);
  };

  const handleChangeRowsPerPage = (event: React.ChangeEvent<HTMLInputElement>) => {
    setRowsPerPage(+event.target.value);
    setPage(0);
  };

  return (
    <Paper sx={{ 
      width: '100%', 
      overflow: 'hidden', 
      boxShadow: (theme) => theme.palette.mode === 'dark'
        ? '0 8px 20px rgba(0,0,0,0.2)'
        : '0 6px 24px rgba(0,0,0,0.06)',
      borderRadius: 3,
      border: (theme) => theme.palette.mode === 'dark'
        ? '1px solid rgba(255, 255, 255, 0.05)'
        : '1px solid rgba(0, 0, 0, 0.03)',
      position: 'relative',
      '&:before': {
        content: '""',
        position: 'absolute',
        top: 0,
        left: 0,
        right: 0,
        height: '3px',
        backgroundImage: (theme) => theme.palette.mode === 'dark'
          ? 'linear-gradient(to right, rgba(45, 212, 191, 0.3), rgba(20, 184, 166, 0.7))'
          : 'linear-gradient(to right, rgba(0, 135, 90, 0.3), rgba(0, 95, 63, 0.7))',
        zIndex: 1,
      },
      transition: 'all 0.3s ease',
      '&:hover': {
        transform: 'translateY(-2px)',
        boxShadow: (theme) => theme.palette.mode === 'dark'
          ? '0 12px 28px rgba(0,0,0,0.25)'
          : '0 12px 30px rgba(0,0,0,0.09)',
      },
    }}>
      <TableContainer sx={{ 
        maxHeight: 440,
        '&::-webkit-scrollbar': {
          width: '8px',
          height: '8px',
        },
        '&::-webkit-scrollbar-thumb': {
          backgroundColor: (theme) => alpha(theme.palette.primary.main, 0.2),
          borderRadius: '4px',
          '&:hover': {
            backgroundColor: (theme) => alpha(theme.palette.primary.main, 0.3),
          }
        },
        '&::-webkit-scrollbar-track': {
          backgroundColor: (theme) => alpha(theme.palette.background.paper, 0.5),
          borderRadius: '4px',
        },
      }}>
        <Table stickyHeader aria-label="sticky table">
          <TableHead>
            <TableRow>
              {columns.map((column) => (
                <TableCell
                  key={String(column.id)}
                  align={column.align}
                  sx={{ 
                    minWidth: column.minWidth, 
                    fontWeight: 700,
                    backgroundColor: (theme) => theme.palette.mode === 'dark' 
                      ? alpha(theme.palette.background.paper, 0.8)
                      : alpha(theme.palette.background.paper, 0.97),
                    color: (theme) => theme.palette.mode === 'dark'
                      ? alpha(theme.palette.common.white, 0.87)
                      : theme.palette.grey[800],
                    borderBottom: (theme) => `2px solid ${
                      theme.palette.mode === 'dark' 
                        ? alpha(theme.palette.divider, 0.7)
                        : theme.palette.divider
                    }`,
                    padding: (theme) => theme.spacing(2, 2.5),
                    fontSize: '0.825rem',
                    letterSpacing: '0.03em',
                    textTransform: 'uppercase',
                  }}
                >
                  {column.label}
                </TableCell>
              ))}
            </TableRow>
          </TableHead>
          <TableBody>
            {loading ? (
              <TableRow>
                <TableCell colSpan={columns.length} align="center" sx={{ py: 8 }}>
                  <Box 
                    sx={{
                      display: 'flex',
                      flexDirection: 'column',
                      alignItems: 'center',
                      justifyContent: 'center',
                      gap: 2
                    }}
                  >
                    <CircularProgress 
                      size={44} 
                      thickness={4.5}
                      sx={{
                        color: (theme) => theme.palette.primary.main,
                        boxShadow: (theme) => `0 0 15px ${alpha(theme.palette.primary.main, 0.3)}`,
                      }}  
                    />
                    <Typography 
                      variant="body2" 
                      sx={{ 
                        color: (theme) => alpha(theme.palette.text.secondary, 0.7),
                        fontWeight: 500,
                        mt: 1
                      }}
                    >
                      Loading data...
                    </Typography>
                  </Box>
                </TableCell>
              </TableRow>
            ) : rows.length > 0 ? (
              rows
                .slice(page * rowsPerPage, page * rowsPerPage + rowsPerPage)
                .map((row) => {
                  return (
                    <TableRow
                      hover
                      tabIndex={-1}
                      key={row.id}
                      onClick={onRowClick ? () => onRowClick(row) : undefined}
                      sx={{ 
                        cursor: onRowClick ? 'pointer' : 'default',
                        transition: 'all 0.2s',
                        '&:hover': {
                          backgroundColor: (theme) => theme.palette.mode === 'dark' 
                            ? alpha(theme.palette.primary.main, 0.08)
                            : alpha(theme.palette.primary.light, 0.05),
                        },
                        '&:nth-of-type(odd)': {
                          backgroundColor: (theme) => theme.palette.mode === 'dark' 
                            ? alpha(theme.palette.background.paper, 0.5)
                            : alpha(theme.palette.background.default, 0.5),
                        }
                      }}
                    >
                      {columns.map((column) => {
                        const value = column.id !== 'actions' ? row[column.id as keyof T] : null;
                        return (
                          <TableCell 
                            key={String(column.id)} 
                            align={column.align} 
                            sx={{ 
                              padding: (theme) => theme.spacing(1.5, 2),
                              borderBottom: (theme) => `1px solid ${theme.palette.divider}`
                            }}
                          >
                            {column.format ? column.format(value, row) : (value as React.ReactNode)}
                          </TableCell>
                        );
                      })}
                    </TableRow>
                  );
                })
            ) : (
              <TableRow>
                <TableCell colSpan={columns.length} align="center" sx={{ py: 8 }}>
                  <Box 
                    sx={{
                      display: 'flex',
                      flexDirection: 'column',
                      alignItems: 'center',
                      justifyContent: 'center',
                      py: 2
                    }}
                  >
                    <Box 
                      sx={{
                        width: 60,
                        height: 60,
                        borderRadius: '50%',
                        display: 'flex',
                        alignItems: 'center',
                        justifyContent: 'center',
                        backgroundColor: (theme) => alpha(theme.palette.background.default, 0.8),
                        border: '1px dashed',
                        borderColor: 'divider',
                        mb: 2
                      }}
                    >
                      <Typography variant="h5" sx={{ opacity: 0.5 }}>?</Typography>
                    </Box>
                    <Typography 
                      variant="body1" 
                      sx={{ 
                        color: (theme) => alpha(theme.palette.text.primary, 0.8),
                        fontWeight: 500,
                        mb: 1
                      }}
                    >
                      No Data Found
                    </Typography>
                    <Typography 
                      variant="body2" 
                      sx={{ 
                        color: (theme) => alpha(theme.palette.text.secondary, 0.7),
                        textAlign: 'center',
                        maxWidth: '80%'
                      }}
                    >
                      {emptyMessage}
                    </Typography>
                  </Box>
                </TableCell>
              </TableRow>
            )}
          </TableBody>
        </Table>
      </TableContainer>
      {rows.length > 0 && (
        <TablePagination
          rowsPerPageOptions={rowsPerPageOptions}
          component="div"
          count={rows.length}
          rowsPerPage={rowsPerPage}
          page={page}
          onPageChange={handleChangePage}
          onRowsPerPageChange={handleChangeRowsPerPage}
          sx={{
            borderTop: '1px solid',
            borderColor: 'divider',
            '.MuiTablePagination-selectLabel, .MuiTablePagination-displayedRows': {
              fontWeight: 500,
              color: (theme) => alpha(theme.palette.text.secondary, 0.8),
            },
            '.MuiTablePagination-select': {
              borderRadius: 1,
            },
            '.MuiTablePagination-actions': {
              '.MuiIconButton-root': {
                borderRadius: 1,
                border: '1px solid',
                borderColor: 'divider',
                mx: 0.5,
                transition: 'all 0.2s',
                '&:hover': {
                  backgroundColor: (theme) => alpha(theme.palette.primary.main, 0.1),
                  borderColor: 'primary.main',
                }
              }
            }
          }}
        />
      )}
    </Paper>
  );
}

export default DataTable;
