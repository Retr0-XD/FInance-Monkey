import React, { useState, useEffect } from 'react';
import { 
  fetchTransactionsFromDrive, 
  fetchTransactionHistory, 
  exportTransactionsToDrive 
} from '../utils/driveStorage';

/**
 * Component for managing Google Drive storage
 */
const DriveStorage = () => {
  const [transactions, setTransactions] = useState([]);
  const [fileHistory, setFileHistory] = useState([]);
  const [loading, setLoading] = useState(false);
  const [exportLoading, setExportLoading] = useState(false);
  const [error, setError] = useState(null);
  const [successMessage, setSuccessMessage] = useState('');

  // Load transactions from Drive when component mounts
  useEffect(() => {
    loadTransactions();
    loadFileHistory();
  }, []);

  // Load transactions from Drive
  const loadTransactions = async () => {
    setLoading(true);
    setError(null);
    
    try {
      const data = await fetchTransactionsFromDrive();
      setTransactions(data);
    } catch (err) {
      setError('Failed to load transactions from Drive: ' + err.message);
    } finally {
      setLoading(false);
    }
  };

  // Load file history
  const loadFileHistory = async () => {
    try {
      const history = await fetchTransactionHistory();
      setFileHistory(history);
    } catch (err) {
      console.error('Failed to load file history:', err);
    }
  };

  // Trigger manual export to Drive
  const handleExportToDrive = async () => {
    setExportLoading(true);
    setError(null);
    setSuccessMessage('');
    
    try {
      const result = await exportTransactionsToDrive();
      setSuccessMessage(`Transactions exported successfully! File ID: ${result.fileId}`);
      // Reload file history to show the new file
      loadFileHistory();
    } catch (err) {
      setError('Failed to export to Drive: ' + err.message);
    } finally {
      setExportLoading(false);
    }
  };

  return (
    <div className="drive-storage">
      <h2>Google Drive Storage</h2>
      
      {/* Export Section */}
      <div className="export-section">
        <h3>Export Transactions</h3>
        <p>
          Export your current transactions to Google Drive for backup or sharing.
          This will create a new JSON file in your Drive account.
        </p>
        <button 
          onClick={handleExportToDrive} 
          disabled={exportLoading}
          className="primary-button"
        >
          {exportLoading ? 'Exporting...' : 'Export to Drive'}
        </button>
        
        {successMessage && (
          <div className="success-message">
            {successMessage}
          </div>
        )}
      </div>
      
      {/* File History Section */}
      <div className="file-history">
        <h3>Transaction File History</h3>
        {fileHistory.length === 0 ? (
          <p>No transaction files found in Drive.</p>
        ) : (
          <table>
            <thead>
              <tr>
                <th>Filename</th>
                <th>Created</th>
                <th>Actions</th>
              </tr>
            </thead>
            <tbody>
              {fileHistory.map((file) => (
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
        )}
      </div>
      
      {/* Transactions Section */}
      <div className="transactions">
        <h3>Latest Transactions from Drive</h3>
        {loading ? (
          <p>Loading transactions...</p>
        ) : error ? (
          <div className="error-message">{error}</div>
        ) : transactions.length === 0 ? (
          <p>No transactions found. Export your data to Drive first.</p>
        ) : (
          <table>
            <thead>
              <tr>
                <th>Date</th>
                <th>Description</th>
                <th>Category</th>
                <th>Amount</th>
              </tr>
            </thead>
            <tbody>
              {transactions.map((transaction) => (
                <tr key={transaction.id}>
                  <td>{new Date(transaction.transactionDate).toLocaleDateString()}</td>
                  <td>{transaction.description}</td>
                  <td>{transaction.category?.name || 'Uncategorized'}</td>
                  <td className={transaction.amount < 0 ? 'negative' : 'positive'}>
                    ${Math.abs(transaction.amount).toFixed(2)}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        )}
        <button onClick={loadTransactions} className="secondary-button">
          Refresh Transactions
        </button>
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

export default DriveStorage;
