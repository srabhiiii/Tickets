// useMemo for portfolio-value calc + useTradeStream live feed.
import React, { useEffect, useState, useMemo } from 'react';
import { withAuth } from '@components/withAuth.jsx';
import { useTradeStream } from '@hooks/useTradeStream.js';
import { api } from '@services/apiService.js';

function StatCard({ label, value }) {
  return (
    <article className="stat-card">
      <h3>{label}</h3>
      <p>{value}</p>
    </article>
  );
}

function Dashboard({ trades: tradesProp }) {
  const stream = useTradeStream();
  const [dbTrades, setDbTrades] = useState([]);

  useEffect(() => {
    if (!tradesProp) {
      api.listTrades('size=100')
        .then((res) => {
          if (res && Array.isArray(res.items)) {
            setDbTrades(res.items);
          } else if (Array.isArray(res)) {
            setDbTrades(res);
          }
        })
        .catch(() => setDbTrades([]));
    }
  }, [tradesProp]);

  // Combine initial database trades with live SSE streamed trades (avoiding duplicate tradeRef)
  const trades = useMemo(() => {
    if (tradesProp) return tradesProp;
    const combined = [...stream.trades];
    const streamRefs = new Set(stream.trades.map((t) => t.tradeRef || t.id));
    for (const t of dbTrades) {
      if (!streamRefs.has(t.tradeRef || t.id)) {
        combined.push(t);
      }
    }
    return combined;
  }, [tradesProp, stream.trades, dbTrades]);

  const isConnected = tradesProp ? true : stream.isConnected;

  const portfolioValue = useMemo(
    () => trades.reduce((sum, t) => sum + (Number(t.quantity || t.qty) * Number(t.price) || 0), 0),
    [trades]
  );

  const { matched, unmatched, breaks } = useMemo(() => {
    let m = 0;
    let u = 0;
    let b = 0;
    for (const t of trades) {
      if (t.status === 'MATCHED') m++;
      else if (t.status === 'UNMATCHED') { u++; b++; }
      else if (t.status === 'MISMATCH' || t.status === 'DISPUTED') b++;
    }
    return { matched: m, unmatched: u, breaks: b };
  }, [trades]);

  return (
    <section>
      <h2>Dashboard</h2>
      <div className="stat-grid">
        <StatCard label="Portfolio value (USD)" value={`$${portfolioValue.toLocaleString(undefined, { minimumFractionDigits: 2, maximumFractionDigits: 2 })}`} />
        <StatCard label="Total trades" value={trades.length} />
        <StatCard label="Matched trades" value={matched} />
        <StatCard label="Unmatched trades" value={unmatched} />
        <StatCard label="Open breaks / mismatches" value={breaks} />
      </div>
      <div role="status" aria-live="polite" style={{ marginTop: '1rem', fontSize: '0.875rem', color: '#666' }}>
        SSE Stream: {isConnected ? '🟢 Connected' : '🔴 Disconnected'}
      </div>
    </section>
  );
}

export default withAuth(Dashboard);
