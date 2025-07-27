import React, { useState, useEffect } from 'react';
import { fetchTransactionsFromDrive, fetchTransactionHistory, exportTransactionsToDrive } from '../utils/driveStorage';
import '../styles/DriveStorage.css';

/**
 * Drive Status component that shows Google Drive connection status and storage details
 */
const DriveStatus = () => {
  const [status, setStatus] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  
  const checkDriveStatus = async () => {
    setLoading(true);
    setError(null);
    
    try {
      const response = await fetch('/api/drive/status', {
        method: 'GET',
        headers: {
          'Authorization': `Bearer ${localStorage.getItem('auth_token')}`,
          'Content-Type': 'application/json',
        },
      });
      
      if (!response.ok) {
        throw new Error(`Error checking drive status: ${response.statusText}`);
      }
      
      const data = await response.json();
      setStatus(data);
    } catch (err) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  };
  
  // Initialize Drive storage (admin only)
  const initializeDriveStorage = async () => {
    try {
      const response = await fetch('/api/drive/initialize', {
        method: 'POST',
        headers: {
          'Authorization': `Bearer ${localStorage.getItem('auth_token')}`,
          'Content-Type': 'application/json',
        },
      });
      
      if (!response.ok) {
        throw new Error(`Error initializing drive storage: ${response.statusText}`);
      }
      
      const data = await response.json();
      alert(`Drive storage initialized: ${data.message}`);
      checkDriveStatus();
    } catch (err) {
      alert(`Error initializing drive storage: ${err.message}`);
    }
  };
  
  useEffect(() => {
    checkDriveStatus();
  }, []);
  
  const formatBytes = (bytes) => {
    if (!bytes) return '0 Bytes';
    
    const k = 1024;
    const sizes = ['Bytes', 'KB', 'MB', 'GB', 'TB'];
    const i = Math.floor(Math.log(bytes) / Math.log(k));
    
    return `${parseFloat((bytes / Math.pow(k, i)).toFixed(2))} ${sizes[i]}`;
  };
  
  return (
    <div className="drive-status">
      <h3>Google Drive Status</h3>
      
      {loading ? (
        <p>Checking Google Drive connection...</p>
      ) : error ? (
        <div className="error-message">
          <h4>Connection Error</h4>
          <p>{error}</p>
          <button onClick={checkDriveStatus} className="secondary-button">
            Try Again
          </button>
        </div>
      ) : status ? (
        <div className="status-details">
          <div className={`status-indicator ${status.status === 'connected' ? 'connected' : 'disconnected'}`}>
            <span className="status-dot"></span>
            <span className="status-text">
              {status.status === 'connected' ? 'Connected' : 'Disconnected'}
            </span>
          </div>
          
          <p className="status-message">{status.message}</p>
          
          {status.status === 'connected' && (
            <div className="storage-info">
              <h4>Storage Information</h4>
              {status.quotaInfo && (
                <div className="quota-info">
                  <div className="info-row">
                    <span>Usage:</span>
                    <span>{formatBytes(status.quotaInfo.usage)}</span>
                  </div>
                  <div className="info-row">
                    <span>Usage in Drive:</span>
                    <span>{formatBytes(status.quotaInfo.usageInDrive)}</span>
                  </div>
                  <div className="info-row">
                    <span>Available Files:</span>
                    <span>{status.availableFiles}</span>
                  </div>
                </div>
              )}
            </div>
          )}
          
          <div className="actions">
            <button onClick={checkDriveStatus} className="secondary-button">
              Refresh Status
            </button>
            
            {/* Only show for admins - you may want to add a role check here */}
            <button onClick={initializeDriveStorage} className="primary-button">
              Initialize Drive Storage
            </button>
          </div>
        </div>
      ) : (
        <p>No status information available</p>
      )}
    </div>
  );
};

/**
 * Enhanced DriveStorage component with status information
 */
const EnhancedDriveStorage = () => {
  return (
    <div className="drive-storage">
      <h2>Google Drive Storage</h2>
      
      {/* Drive status section */}
      <DriveStatus />
      
      {/* Export Section */}
      <div className="export-section">
        <h3>Export Transactions</h3>
        <ExportTransactions />
      </div>
      
      {/* File History Section */}
      <div className="file-history">
        <h3>Transaction File History</h3>
        <FileHistory />
      </div>
      
      {/* Info Section */}
      <div className="info-section">
        <h3>About Drive Storage</h3>
        <p>
          Finance Monkey stores your processed transaction data in Google Drive 
          to provide a reliable, cost-effective storage solution. Your data is 
          automatically exported once a month, but you can trigger a manual 
          export at any time.
        </p>
        <p>
          The exported files are standard JSON format and can be viewed directly
          in your Google Drive account.
        </p>
      </div>
    </div>
  );
};

/**
 * Export Transactions component
 */
const ExportTransactions = () => {
  const [exporting, setExporting] = useState(false);
  const [result, setResult] = useState(null);
  const [error, setError] = useState(null);
  
  const handleExport = async () => {
    setExporting(true);
    setError(null);
    setResult(null);
    
    try {
      const data = await exportTransactionsToDrive();
      setResult(data);
    } catch (err) {
      setError(err.message);
    } finally {
      setExporting(false);
    }
  };
  
  return (
    <div>
      <p>
        Export your current transactions to Google Drive for backup or sharing.
        This will create a new JSON file in your Drive account.
      </p>
      
      <button 
        onClick={handleExport} 
        disabled={exporting}
        className="primary-button"
      >
        {exporting ? 'Exporting...' : 'Export to Drive'}
      </button>
      
      {result && (
        <div className="success-message">
          <p>Transactions exported successfully!</p>
          <p>File ID: {result.fileId}</p>
        </div>
      )}
      
      {error && (
        <div className="error-message">
          <p>Export failed: {error}</p>
        </div>
      )}
    </div>
  );
};

/**
 * File History component
 */
const FileHistory = () => {
  const [files, setFiles] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  
  const loadFiles = async () => {
    setLoading(true);
    setError(null);
    
    try {
      const data = await fetchTransactionHistory();
      setFiles(data);
    } catch (err) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  };
  
  useEffect(() => {
    loadFiles();
  }, []);
  
  return (
    <div>
      {loading ? (
        <p>Loading file history...</p>
      ) : error ? (
        <div className="error-message">
          <p>Failed to load file history: {error}</p>
          <button onClick={loadFiles} className="secondary-button">
            Try Again
          </button>
        </div>
      ) : files.length === 0 ? (
        <p>No transaction files found in Drive.</p>
      ) : (
        <>
          <table>
            <thead>
              <tr>
                <th>Filename</th>
                <th>Created</th>
                <th>Actions</th>
              </tr>
            </thead>
            <tbody>
              {files.map((file) => (
                <tr key={file.id}>
                  <td>{file.name}</td>
                  <td>{new Date(file.createdTime).toLocaleString()}</td>
                  <td>
                    <a 
                      href={file.webViewLink} 
                      target="_blank" 
                      rel="noopener noreferrer"
                      className="link-button"
                    >
                      View in Drive
                    </a>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
          
          <button onClick={loadFiles} className="secondary-button refresh-button">
            Refresh Files
          </button>
        </>
      )}
    </div>
  );
};

export default EnhancedDriveStorage;
