-- Insert default root categories
INSERT INTO categories (id, name, icon, color_code, created_at, updated_at)
VALUES 
    ('a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', 'Bills', 'receipt', '#FF5733', NOW(), NOW()),
    ('a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a12', 'Food & Dining', 'restaurant', '#33A8FF', NOW(), NOW()),
    ('a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a13', 'Shopping', 'shopping_cart', '#33FF57', NOW(), NOW()),
    ('a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a14', 'Entertainment', 'movie', '#F033FF', NOW(), NOW()),
    ('a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a15', 'Transportation', 'directions_car', '#FFFF33', NOW(), NOW()),
    ('a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a16', 'Travel', 'flight', '#33FFF0', NOW(), NOW()),
    ('a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a17', 'Health', 'healing', '#E433FF', NOW(), NOW()),
    ('a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a18', 'Subscriptions', 'subscriptions', '#3357FF', NOW(), NOW());

-- Insert subcategories
INSERT INTO categories (id, name, parent_category_id, icon, color_code, created_at, updated_at)
VALUES 
    -- Bills subcategories
    ('b0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', 'Electricity', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', 'bolt', '#FFA07A', NOW(), NOW()),
    ('b0eebc99-9c0b-4ef8-bb6d-6bb9bd380a12', 'Water', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', 'water_drop', '#ADD8E6', NOW(), NOW()),
    ('b0eebc99-9c0b-4ef8-bb6d-6bb9bd380a13', 'Internet', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', 'wifi', '#90EE90', NOW(), NOW()),
    ('b0eebc99-9c0b-4ef8-bb6d-6bb9bd380a14', 'Rent', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', 'home', '#FFB6C1', NOW(), NOW()),
    
    -- Food subcategories
    ('b0eebc99-9c0b-4ef8-bb6d-6bb9bd380a15', 'Groceries', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a12', 'shopping_basket', '#87CEEB', NOW(), NOW()),
    ('b0eebc99-9c0b-4ef8-bb6d-6bb9bd380a16', 'Restaurants', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a12', 'local_dining', '#98FB98', NOW(), NOW()),
    ('b0eebc99-9c0b-4ef8-bb6d-6bb9bd380a17', 'Coffee Shops', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a12', 'local_cafe', '#DDA0DD', NOW(), NOW()),
    
    -- Shopping subcategories
    ('b0eebc99-9c0b-4ef8-bb6d-6bb9bd380a18', 'Clothing', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a13', 'checkroom', '#FFDAB9', NOW(), NOW()),
    ('b0eebc99-9c0b-4ef8-bb6d-6bb9bd380a19', 'Electronics', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a13', 'devices', '#B0E0E6', NOW(), NOW()),
    ('b0eebc99-9c0b-4ef8-bb6d-6bb9bd380a20', 'Home Goods', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a13', 'chair', '#F0E68C', NOW(), NOW()),
    
    -- Entertainment subcategories
    ('b0eebc99-9c0b-4ef8-bb6d-6bb9bd380a21', 'Movies', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a14', 'movie', '#AFEEEE', NOW(), NOW()),
    ('b0eebc99-9c0b-4ef8-bb6d-6bb9bd380a22', 'Music', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a14', 'music_note', '#D8BFD8', NOW(), NOW()),
    ('b0eebc99-9c0b-4ef8-bb6d-6bb9bd380a23', 'Games', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a14', 'sports_esports', '#FFE4C4', NOW(), NOW()),
    
    -- Transportation subcategories
    ('b0eebc99-9c0b-4ef8-bb6d-6bb9bd380a24', 'Fuel', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a15', 'local_gas_station', '#F0FFF0', NOW(), NOW()),
    ('b0eebc99-9c0b-4ef8-bb6d-6bb9bd380a25', 'Public Transit', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a15', 'directions_bus', '#E6E6FA', NOW(), NOW()),
    ('b0eebc99-9c0b-4ef8-bb6d-6bb9bd380a26', 'Parking', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a15', 'local_parking', '#FFF0F5', NOW(), NOW()),
    
    -- Subscriptions subcategories
    ('b0eebc99-9c0b-4ef8-bb6d-6bb9bd380a27', 'Streaming Services', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a18', 'live_tv', '#E0FFFF', NOW(), NOW()),
    ('b0eebc99-9c0b-4ef8-bb6d-6bb9bd380a28', 'Software', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a18', 'computer', '#FFFACD', NOW(), NOW()),
    ('b0eebc99-9c0b-4ef8-bb6d-6bb9bd380a29', 'Memberships', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a18', 'card_membership', '#F5F5DC', NOW(), NOW());
