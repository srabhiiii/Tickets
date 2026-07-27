-- ============================================================================
-- TICKET-ADV017 - Seed data: 10 counterparties, 50 instruments, 500 trades
-- Safe for local/dev use. Run on a fresh schema or after truncation.
-- ============================================================================

-- Counterparties: 10 explicit rows
INSERT INTO counterparties (id, name, lei_code, region) VALUES
  (1,  'Apex Brokers Inc',         '5493001ABCDE12345001', 'NAMR'),
  (2,  'Vertex Securities LLC',    '5493001ABCDE12345002', 'NAMR'),
  (3,  'Helix Capital Markets',    '5493001ABCDE12345003', 'APAC'),
  (4,  'Aurora Markets SA',        '5493001ABCDE12345004', 'LATAM'),
  (5,  'Borealis Trading GmbH',    '5493001ABCDE12345005', 'EMEA'),
  (6,  'Cascadia Investments PLC', '5493001ABCDE12345006', 'EMEA'),
  (7,  'Delphi Asset Management',  '5493001ABCDE12345007', 'EMEA'),
  (8,  'Equinox Securities Pty',   '5493001ABCDE12345008', 'APAC'),
  (9,  'Fjord Capital Partners',   '5493001ABCDE12345009', 'EMEA'),
  (10, 'Granite Hill Brokers',     '5493001ABCDE12345010', 'NAMR')
ON CONFLICT (id) DO NOTHING;

-- Instruments: 5 explicit + 45 generated = 50 total
INSERT INTO instruments (id, symbol, name, asset_class, currency, isin, metadata) VALUES
  (1, 'SAP.DE', 'SAP SE',                'EQUITY',       'EUR', 'DE0007164600', '{"sector":"Technology","exchange":"XETR"}'::jsonb),
  (2, 'US10Y',  'US 10-Year Treasury',   'FIXED_INCOME', 'USD', 'US912828F622', '{"tenor":"10Y","issuer":"US Treasury"}'::jsonb),
  (3, 'EURUSD', 'Euro / US Dollar',      'FX',           'USD', NULL,           '{"pair":["EUR","USD"]}'::jsonb),
  (4, 'XAU',    'Spot Gold',             'COMMODITY',    'USD', NULL,           '{"unit":"troy ounce"}'::jsonb),
  (5, 'CL_FUT', 'WTI Crude Oil Futures', 'DERIVATIVE',   'USD', NULL,           '{"underlying":"WTI","contractSize":1000}'::jsonb)
ON CONFLICT (id) DO NOTHING;

INSERT INTO instruments (id, symbol, name, asset_class, currency, isin, metadata)
SELECT
    5 + g,
    'GEN' || LPAD(g::text, 4, '0'),
    'Generated Instrument ' || g,
    (ARRAY['EQUITY','FIXED_INCOME','FX','COMMODITY','DERIVATIVE'])[1 + (g % 5)],
    (ARRAY['USD','EUR','GBP','JPY'])[1 + (g % 4)],
    NULL,
    jsonb_build_object('sector', (ARRAY['Banking','Energy','Tech','Healthcare'])[1 + (g % 4)],
                       'bucket', g % 10)
FROM generate_series(1, 45) g
ON CONFLICT (id) DO NOTHING;

-- Trades: 500 rows spread across Apr-Jul 2026 (~125 per month)
INSERT INTO trades (
    trade_ref, instrument_id, counterparty_id, asset_class, side,
    quantity, price, trade_date, status, created_at, modified_at
)
SELECT
    'TRD-2026-' || LPAD(n::text, 6, '0') AS trade_ref,
    1 + (n % 50) AS instrument_id,
    1 + (n % 10) AS counterparty_id,
    (ARRAY['EQUITY','FIXED_INCOME','FX','COMMODITY','DERIVATIVE'])[1 + (n % 5)] AS asset_class,
    (ARRAY['BUY','SELL'])[1 + (n % 2)] AS side,
    ROUND((10 + random() * 1000)::numeric, 4) AS quantity,
    ROUND((5 + random() * 500)::numeric, 4) AS price,
    (DATE '2026-04-01' + ((n - 1) % 120))::date AS trade_date,
    (ARRAY['MATCHED','UNMATCHED','DISPUTED'])[1 + (n % 3)] AS status,
    now(),
    now()
FROM generate_series(1, 500) n
ON CONFLICT (trade_ref, trade_date) DO NOTHING;

-- Optional verification queries
-- SELECT COUNT(*) FROM counterparties; -- 10
-- SELECT COUNT(*) FROM instruments;     -- 50
-- SELECT COUNT(*) FROM trades;          -- 500
-- SELECT DATE_TRUNC('month', trade_date) AS m, COUNT(*) FROM trades GROUP BY 1 ORDER BY 1;
