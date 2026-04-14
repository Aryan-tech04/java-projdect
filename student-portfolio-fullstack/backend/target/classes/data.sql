merge into users (id, full_name, email, password_hash, role)
key(id)
values (1, 'Aarav Student', 'student@ledger.com', '703b0a3d6ad75b649a28adde7d83c6251da457549263bc7ff45ec709b0a8448b', 'STUDENT');

merge into assets (id, asset_name, ticker_symbol, category, quantity, buy_price, current_price, risk_level, notes, created_at, updated_at)
key(id)
values
(1, 'Apple Inc.', 'AAPL', 'TECHNOLOGY', 18, 174.20, 192.42, 'MEDIUM', 'Strong long term growth stock.', current_timestamp, current_timestamp),
(2, 'Microsoft Corp.', 'MSFT', 'TECHNOLOGY', 10, 312.80, 345.10, 'LOW', 'Cloud and AI exposure.', current_timestamp, current_timestamp),
(3, 'Vanguard S&P 500 ETF', 'VOO', 'ETF', 14, 385.55, 412.33, 'LOW', 'Stable ETF holding for balance.', current_timestamp, current_timestamp),
(4, 'Goldman Sachs', 'GS', 'FINANCIAL', 6, 355.00, 388.10, 'MEDIUM', 'Finance sector diversification.', current_timestamp, current_timestamp);
