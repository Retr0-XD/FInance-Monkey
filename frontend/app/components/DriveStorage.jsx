import { useState, useEffect } from 'react';
import { Box, Typography, Button, CircularProgress, Alert, Paper, List, ListItem, ListItemText, ListItemIcon } from '@mui/material';
import CloudDownloadIcon from '@mui/icons-material/CloudDownload';
import CloudUploadIcon from '@mui/icons-material/CloudUpload';
import RefreshIcon from '@mui/icons-material/Refresh';
import FolderIcon from '@mui/icons-material/Folder';
import InsertDriveFileIcon from '@mui/icons-material/InsertDriveFile';

// API service for Drive storage operations
const DriveStorageService = {
  getStatus: async () => {
    try {
      const response = await fetch('/api/drive/status');
      if (!response.ok) throw new Error('Failed to get Drive status');
      return await response.json();
    } catch (error) {
      console.error('Error getting Drive status:', error);
      throw error;
    }
  },
  
  getFiles: async () => {
    try {
      const response = await fetch('/api/drive/files');
      if (!response.ok) throw new Error('Failed to get Drive files');
      return await response.json();
    } catch (error) {
      console.error('Error getting Drive files:', error);
      throw error;
    }
  },
  
  exportData: async () => {
    try {
      const response = await fetch('/api/drive/export', { 
        method: 'POST',
        headers: { 'Content-Type': 'application/json' }
      });
      if (!response.ok) throw new Error('Failed to export to Drive');
      return await response.json();
    } catch (error) {
      console.error('Error exporting to Drive:', error);
      throw error;
    }
  }
};

export default function DriveStorage() {
  const [status, setStatus] = useState(null);
  const [files, setFiles] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [exportStatus, setExportStatus] = useState(null);
  
  // Load Drive status on component mount
  useEffect(() => {
    fetchDriveStatus();
  }, []);
  
  const fetchDriveStatus = async () => {
    try {
      setLoading(true);
      setError(null);
      const statusData = await DriveStorageService.getStatus();
      setStatus(statusData);
      
      // Also fetch files if connected
      if (statusData.connected) {
        const filesData = await DriveStorageService.getFiles();
        setFiles(filesData.files || []);
      }
    } catch (err) {
      setError('Failed to connect to Google Drive. Please check your credentials.');
      console.error(err);
    } finally {
      setLoading(false);
    }
  };
  
  const handleExportData = async () => {
    try {
      setExportStatus({ loading: true, message: 'Exporting data to Google Drive...' });
      const result = await DriveStorageService.exportData();
      setExportStatus({ success: true, message: 'Data exported successfully to Drive!' });
      
      // Refresh files list
      fetchDriveStatus();
    } catch (err) {
      setExportStatus({ error: true, message: 'Failed to export data to Drive.' });
    }
  };
  
  if (loading) {
    return (
      <Box sx={{ display: 'flex', justifyContent: 'center', p: 3 }}>
        <CircularProgress />
      </Box>
    );
  }
  
  return (
    <Paper sx={{ p: 3, mb: 4 }}>
      <Typography variant="h5" gutterBottom>
        Google Drive Integration
      </Typography>
      
      {error && <Alert severity="error" sx={{ mb: 2 }}>{error}</Alert>}
      
      <Box sx={{ mb: 2 }}>
        <Typography variant="body1">
          Status: {status?.connected ? 
            <span style={{ color: 'green' }}>Connected</span> : 
            <span style={{ color: 'red' }}>Not Connected</span>}
        </Typography>
        
        {status?.connected && (
          <Typography variant="body2" color="text.secondary">
            Storage Folder: {status.folderName || 'Finance Monkey Data'}
          </Typography>
        )}
      </Box>
      
      <Box sx={{ display: 'flex', gap: 2, mb: 3 }}>
        <Button 
          variant="outlined" 
          startIcon={<RefreshIcon />} 
          onClick={fetchDriveStatus}
        >
          Refresh Status
        </Button>
        
        <Button 
          variant="contained" 
          startIcon={<CloudUploadIcon />} 
          onClick={handleExportData}
          disabled={!status?.connected || exportStatus?.loading}
        >
          Export to Drive
        </Button>
      </Box>
      
      {exportStatus && (
        <Alert 
          severity={exportStatus.error ? 'error' : exportStatus.success ? 'success' : 'info'} 
          sx={{ mb: 2 }}
        >
          {exportStatus.message}
          {exportStatus.loading && <CircularProgress size={20} sx={{ ml: 2 }} />}
        </Alert>
      )}
      
      {status?.connected && files.length > 0 && (
        <>
          <Typography variant="h6" gutterBottom>
            Drive Files
          </Typography>
          <List>
            {files.map((file) => (
              <ListItem key={file.id}>
                <ListItemIcon>
                  {file.mimeType === 'application/vnd.google-apps.folder' 
                    ? <FolderIcon /> 
                    : <InsertDriveFileIcon />}
                </ListItemIcon>
                <ListItemText 
                  primary={file.name} 
                  secondary={new Date(file.modifiedTime).toLocaleString()} 
                />
              </ListItem>
            ))}
          </List>
        </>
      )}
      
      {status?.connected && files.length === 0 && (
        <Typography variant="body1" color="text.secondary">
          No files found in your Finance Monkey Drive storage.
        </Typography>
      )}
    </Paper>
  );
}
