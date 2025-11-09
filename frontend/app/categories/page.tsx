'use client';

import React, { useState, useEffect } from 'react';
import { Box, Container, Typography, Button, Card, CardContent, TextField, InputAdornment, Dialog, DialogTitle, DialogContent, DialogActions, IconButton, Alert, FormControl, InputLabel, Select, MenuItem, SelectChangeEvent, Stack } from '@mui/material';
import { useAppDispatch, useAppSelector } from '@/lib/hooks';
import { fetchCategories, createCategory, updateCategory, deleteCategory, Category } from '@/store/slices/categorySlice';
import { SideNav, DataTable } from '../components';
import SearchIcon from '@mui/icons-material/Search';
import AddIcon from '@mui/icons-material/Add';
import EditIcon from '@mui/icons-material/Edit';
import DeleteIcon from '@mui/icons-material/Delete';
import ColorPicker from '@mui/material/CircularProgress';  // This is a placeholder, we'd need a color picker component

interface CategoryFormData {
  id?: string;
  name: string;
  description?: string;
  parentCategoryId?: string;
  color?: string;
}

export default function Categories() {
  const dispatch = useAppDispatch();
  const { categories, loading, error } = useAppSelector((state) => state.categories);
  const [search, setSearch] = useState('');
  const [openDialog, setOpenDialog] = useState(false);
  const [formData, setFormData] = useState<CategoryFormData>({
    name: '',
  });
  const [formErrors, setFormErrors] = useState<Record<string, string>>({});
  const [editMode, setEditMode] = useState(false);
  const [selectedColor, setSelectedColor] = useState('#1976d2');

  useEffect(() => {
    dispatch(fetchCategories());
  }, [dispatch]);

  const handleSearchChange = (event: React.ChangeEvent<HTMLInputElement>) => {
    setSearch(event.target.value);
  };

  const filteredCategories = categories.filter((category) => {
    const searchLower = search.toLowerCase();
    return (
      category.name.toLowerCase().includes(searchLower) ||
      (category.description && category.description.toLowerCase().includes(searchLower))
    );
  });

  // Group categories by parent to create a hierarchy
  const parentCategories = categories.filter(cat => !cat.parentCategoryId);
  const childCategories = categories.filter(cat => cat.parentCategoryId);

  const getCategoryNameById = (id?: string) => {
    if (!id) return 'None';
    const category = categories.find(cat => cat.id === id);
    return category ? category.name : 'Unknown';
  };

  const handleOpenDialog = (category?: Category) => {
    if (category) {
      // Edit mode
      setFormData({
        id: category.id,
        name: category.name,
        description: category.description,
        parentCategoryId: category.parentCategoryId,
        color: category.color || '#1976d2',
      });
      setSelectedColor(category.color || '#1976d2');
      setEditMode(true);
    } else {
      // Create mode
      setFormData({
        name: '',
        color: '#1976d2',
      });
      setSelectedColor('#1976d2');
      setEditMode(false);
    }
    setOpenDialog(true);
    setFormErrors({});
  };

  const handleCloseDialog = () => {
    setOpenDialog(false);
  };

  const handleFormChange = (event: React.ChangeEvent<HTMLInputElement | { name?: string; value: unknown }>) => {
    const { name, value } = event.target;
    if (name) {
      setFormData((prev) => ({
        ...prev,
        [name]: value,
      }));
    }
  };

  const validateForm = () => {
    const errors: Record<string, string> = {};
    if (!formData.name) errors.name = 'Category name is required';
    return errors;
  };

  const handleSubmit = () => {
    const errors = validateForm();
    if (Object.keys(errors).length > 0) {
      setFormErrors(errors);
      return;
    }

    const categoryData = {
      ...formData,
      color: selectedColor,
    };

    if (editMode && formData.id) {
      dispatch(updateCategory({ ...categoryData, id: formData.id } as Category));
    } else {
      dispatch(createCategory(categoryData));
    }

    handleCloseDialog();
  };

  const handleDeleteCategory = (category: Category) => {
    // Check if there are child categories
    const hasChildren = categories.some(cat => cat.parentCategoryId === category.id);
    
    if (hasChildren) {
      alert('Cannot delete a category that has sub-categories. Please delete the sub-categories first.');
      return;
    }
    
    if (window.confirm(`Are you sure you want to delete the category "${category.name}"?`)) {
      dispatch(deleteCategory(category.id));
    }
  };

  const categoryColumns: Array<{
    id: keyof Category | 'actions';
    label: string;
    minWidth?: number;
    align?: 'right' | 'left' | 'center';
    format?: (value: unknown, row: Category) => React.ReactNode;
  }> = [
    { id: 'name', label: 'Category Name', minWidth: 200 },
    { 
      id: 'parentCategoryId', 
      label: 'Parent Category', 
      minWidth: 150,
      format: (value: unknown) => getCategoryNameById(value as string | undefined),
    },
    { id: 'description', label: 'Description', minWidth: 200 },
    { 
      id: 'color', 
      label: 'Color', 
      minWidth: 80,
      format: (value: unknown) => (
        <Box
          sx={{
            width: 24,
            height: 24,
            bgcolor: (value as string | undefined) || '#1976d2',
            borderRadius: '50%',
            border: '1px solid #ddd',
          }}
        />
      ),
    },
    {
      id: 'actions',
      label: 'Actions',
      minWidth: 100,
      align: 'right' as const,
      format: (_value: unknown, row: Category) => (
        <Box>
          <IconButton size="small" onClick={() => handleOpenDialog(row)}>
            <EditIcon fontSize="small" />
          </IconButton>
          <IconButton size="small" onClick={() => handleDeleteCategory(row)} color="error">
            <DeleteIcon fontSize="small" />
          </IconButton>
        </Box>
      ),
    },
  ];

  // For now we'll use a text field for color selection, but ideally we'd use a proper color picker component
  const ColorPickerField = () => (
    <TextField
      fullWidth
      label="Color"
      name="color"
      value={selectedColor}
      onChange={(e) => setSelectedColor(e.target.value)}
      placeholder="#RRGGBB"
      helperText="Enter a hex color code (e.g. #1976d2)"
    />
  );

  return (
    <SideNav>
      <Container maxWidth="xl">
        <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 3 }}>
          <Typography variant="h4" fontWeight="bold">Categories</Typography>
          <Box display="flex" gap={2}>
            <TextField
              placeholder="Search categories..."
              variant="outlined"
              size="small"
              value={search}
              onChange={handleSearchChange}
              InputProps={{
                startAdornment: (
                  <InputAdornment position="start">
                    <SearchIcon />
                  </InputAdornment>
                ),
              }}
            />
            <Button 
              variant="contained" 
              startIcon={<AddIcon />}
              onClick={() => handleOpenDialog()}
            >
              Add Category
            </Button>
          </Box>
        </Box>

        {error && <Alert severity="error" sx={{ mb: 3 }}>{error}</Alert>}

        <Card>
          <CardContent>
            <Typography variant="h6" gutterBottom>Transaction Categories</Typography>
            <Typography variant="body2" color="text.secondary" paragraph>
              Create and manage categories to organize your transactions.
            </Typography>
            
            <DataTable
              columns={categoryColumns}
              rows={filteredCategories}
              loading={loading}
              emptyMessage="No categories found. Click 'Add Category' to create your first category."
            />
          </CardContent>
        </Card>

        <Dialog open={openDialog} onClose={handleCloseDialog} maxWidth="sm" fullWidth>
          <DialogTitle>{editMode ? 'Edit Category' : 'Add Category'}</DialogTitle>
          <DialogContent>
            <Stack component="form" spacing={2} sx={{ mt: 2 }}>
              <TextField
                fullWidth
                label="Category Name"
                name="name"
                value={formData.name}
                onChange={handleFormChange}
                error={!!formErrors.name}
                helperText={formErrors.name}
              />
              <FormControl fullWidth>
                <InputLabel>Parent Category (Optional)</InputLabel>
                <Select
                  name="parentCategoryId"
                  value={formData.parentCategoryId || ''}
                  onChange={handleFormChange as (event: SelectChangeEvent<string>) => void}
                  label="Parent Category (Optional)"
                >
                  <MenuItem value="">
                    <em>None</em>
                  </MenuItem>
                  {parentCategories.map((category) => (
                    <MenuItem 
                      key={category.id} 
                      value={category.id}
                      disabled={formData.id === category.id} // Prevent self-reference
                    >
                      {category.name}
                    </MenuItem>
                  ))}
                </Select>
              </FormControl>
              <TextField
                fullWidth
                label="Description (Optional)"
                name="description"
                value={formData.description || ''}
                onChange={handleFormChange}
                placeholder="e.g., Expenses for dining out"
              />
              <ColorPickerField />
            </Stack>
          </DialogContent>
          <DialogActions>
            <Button onClick={handleCloseDialog}>Cancel</Button>
            <Button onClick={handleSubmit} variant="contained">
              {editMode ? 'Update' : 'Save'}
            </Button>
          </DialogActions>
        </Dialog>
      </Container>
    </SideNav>
  );
}
