import React from 'react';
import DriveStorage from '../components/DriveStorage';
import '../styles/DriveStorage.css';

/**
 * Page component for Google Drive storage management
 */
const DriveStoragePage = () => {
  return (
    <div className="page-container">
      <DriveStorage />
    </div>
  );
};

export default DriveStoragePage;
