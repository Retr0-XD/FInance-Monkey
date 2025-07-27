/**
 * Utility functions for Google Drive integration
 * Used to fetch transaction data from the backend that is stored in Google Drive
 */

/**
 * Fetches the latest transactions from Google Drive
 * @returns {Promise<Array>} Transactions array
 */
export async function fetchTransactionsFromDrive() {
  const token = localStorage.getItem('auth_token');
  
  try {
    const response = await fetch('/api/drive/transactions', {
      method: 'GET',
      headers: {
        'Authorization': `Bearer ${token}`,
        'Content-Type': 'application/json',
      },
    });
    
    if (!response.ok) {
      throw new Error(`Error fetching transactions: ${response.statusText}`);
    }
    
    return await response.json();
  } catch (error) {
    console.error('Failed to fetch transactions from Drive:', error);
    throw error;
  }
}

/**
 * Fetches transaction file history from Google Drive
 * @returns {Promise<Array>} Array of file metadata objects
 */
export async function fetchTransactionHistory() {
  const token = localStorage.getItem('auth_token');
  
  try {
    const response = await fetch('/api/drive/transactions/history', {
      method: 'GET',
      headers: {
        'Authorization': `Bearer ${token}`,
        'Content-Type': 'application/json',
      },
    });
    
    if (!response.ok) {
      throw new Error(`Error fetching transaction history: ${response.statusText}`);
    }
    
    return await response.json();
  } catch (error) {
    console.error('Failed to fetch transaction history from Drive:', error);
    throw error;
  }
}

/**
 * Triggers a manual export of transactions to Google Drive
 * @returns {Promise<Object>} Response with status and file ID
 */
export async function exportTransactionsToDrive() {
  const token = localStorage.getItem('auth_token');
  
  try {
    const response = await fetch('/api/drive/export/transactions', {
      method: 'POST',
      headers: {
        'Authorization': `Bearer ${token}`,
        'Content-Type': 'application/json',
      },
    });
    
    if (!response.ok) {
      throw new Error(`Error exporting transactions: ${response.statusText}`);
    }
    
    return await response.json();
  } catch (error) {
    console.error('Failed to export transactions to Drive:', error);
    throw error;
  }
}
