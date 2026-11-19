-- V3: Dữ liệu mặt bằng mẫu (bộ dữ liệu minh họa thống nhất ở mục 3.1 thiết kế UI)

INSERT INTO floor (id, floor_code, name, level_no, created_at, updated_at) VALUES
    ('00000000-0000-0000-0000-0000000000f1', 'F1', 'Tầng 1', 1, now(), now()),
    ('00000000-0000-0000-0000-0000000000f2', 'F2', 'Tầng 2', 2, now(), now());

INSERT INTO zone (id, floor_id, zone_code, name, created_at, updated_at) VALUES
    ('00000000-0000-0000-0000-00000000a1e1', '00000000-0000-0000-0000-0000000000f1', 'W', 'Khu phía Tây', now(), now()),
    ('00000000-0000-0000-0000-00000000a1e2', '00000000-0000-0000-0000-0000000000f1', 'E', 'Khu phía Đông', now(), now()),
    ('00000000-0000-0000-0000-00000000a2e1', '00000000-0000-0000-0000-0000000000f2', 'FA', 'Khu Thời trang', now(), now());

INSERT INTO unit (id, zone_id, unit_code, area_m2, status, planned_category, ref_rent, created_at, updated_at) VALUES
    (gen_random_uuid(), '00000000-0000-0000-0000-00000000a1e1', 'F1-13', 120.00, 'AVAILABLE',   'F&B',        1500000, now(), now()),
    (gen_random_uuid(), '00000000-0000-0000-0000-00000000a1e1', 'F1-14', 90.00,  'AVAILABLE',   'F&B',        1500000, now(), now()),
    (gen_random_uuid(), '00000000-0000-0000-0000-00000000a1e2', 'F1-15', 150.00, 'AVAILABLE',   'F&B',        1666667, now(), now()),
    (gen_random_uuid(), '00000000-0000-0000-0000-00000000a1e2', 'F1-16', 80.00,  'MAINTENANCE', 'Mỹ phẩm',    1400000, now(), now()),
    (gen_random_uuid(), '00000000-0000-0000-0000-00000000a2e1', 'L2-07', 100.00, 'AVAILABLE',   'Thời trang', 1200000, now(), now()),
    (gen_random_uuid(), '00000000-0000-0000-0000-00000000a2e1', 'L2-08', 110.00, 'AVAILABLE',   'Thời trang', 1200000, now(), now());
