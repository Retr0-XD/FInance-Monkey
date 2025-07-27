import React, { useState, useEffect } from 'react';
import EnhancedDriveStorage from '../components/EnhancedDriveStorage';
import '../styles/DriveStorage.css';
import '../styles/DriveAdmin.css';

/**
 * Admin dashboard for Google Drive storage management
 */
const DriveStorageAdmin = () => {
  const [migrationStatus, setMigrationStatus] = useState(null);
  const [loading, setLoading] = useState(false);
  
  // Function to trigger a full data migration to Google Drive
  const migrateAllData = async () => {
    if (!confirm('This will migrate ALL user data to Google Drive. Continue?')) {
      return;
    }
    
    setLoading(true);
    
    try {
      const response = await fetch('/api/drive/admin/migrate-all', {
        method: 'POST',
        headers: {
          'Authorization': `Bearer ${localStorage.getItem('auth_token')}`,
          'Content-Type': 'application/json',
        },
      });
      
      if (!response.ok) {
        throw new Error(`Error migrating data: ${response.statusText}`);
      }
      
      const data = await response.json();
      setMigrationStatus({
        success: true,
        message: 'Migration completed successfully',
        details: data
      });
    } catch (err) {
      setMigrationStatus({
        success: false,
        message: `Migration failed: ${err.message}`
      });
    } finally {
      setLoading(false);
    }
  };
  
  return (
    <div className="admin-page">
      <h1>Drive Storage Administration</h1>
      
      <div className="admin-section">
        <h2>Data Migration</h2>
        <p>
          Use this function to migrate all user transaction data to Google Drive.
          This is useful when transitioning away from database storage or creating backups.
        </p>
        
        <button
          onClick={migrateAllData}
          disabled={loading}
          className="admin-button"
        >
          {loading ? 'Migrating...' : 'Migrate All Data to Drive'}
        </button>
        
        {migrationStatus && (
          <div className={`migration-status ${migrationStatus.success ? 'success' : 'error'}`}>
            <h3>{migrationStatus.message}</h3>
            
            {migrationStatus.success && migrationStatus.details && (
              <div className="migration-details">
                <h4>Migration Details:</h4>
                <pre>{JSON.stringify(migrationStatus.details, null, 2)}</pre>
              </div>
            )}
          </div>
        )}
      </div>
      
      <EnhancedDriveStorage />
    </div>
  );
};

export default DriveStorageAdmin;
