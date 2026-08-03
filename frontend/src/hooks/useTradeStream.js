// useTradeStream() — SSE subscription returning live trades.
import { useEffect, useState } from 'react';

const MAX_BUFFER = 200;

export function useTradeStream(url = '/api/v1/trades/stream') {
  const [trades, setTrades] = useState([]);
  const [isConnected, setConnected] = useState(false);

  useEffect(() => {
    const token = typeof sessionStorage !== 'undefined' ? sessionStorage.getItem('reconx-token') : null;
    const streamUrl = token && !url.includes('token=')
      ? `${url}${url.includes('?') ? '&' : '?'}token=${encodeURIComponent(token)}`
      : url;

    const sse = new EventSource(streamUrl);
    sse.onopen  = () => setConnected(true);
    sse.onerror = () => setConnected(false);
    sse.onmessage = (e) => {
      try {
        const trade = JSON.parse(e.data);
        setTrades((prev) => [trade, ...prev].slice(0, MAX_BUFFER));
      } catch { /* ignore malformed payload */ }
    };
    return () => sse.close();
  }, [url]);

  return { trades, isConnected };
}
