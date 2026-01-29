// Gateway Server URL - Update this when deployed to cloud
const GATEWAY_URL = process.env.REACT_APP_GATEWAY_URL || 'http://localhost:3001';

class GatewayClient {
  constructor() {
    this.ws = null;
    this.connected = false;
    this.subscribers = [];
    this.userId = null;
  }

  // Connect to Gateway Server
  connect(userId) {
    if (this.ws) {
      this.disconnect();
    }

    this.userId = userId;
    const wsUrl = GATEWAY_URL.replace('http://', 'ws://').replace('https://', 'wss://');
    
    try {
      this.ws = new WebSocket(wsUrl);

      this.ws.onopen = () => {
        console.log('[Gateway] Connected to server');
        this.connected = true;
        
        // Subscribe to user's BLE events
        this.ws.send(JSON.stringify({ type: 'subscribe', userId: this.userId }));
      };

      this.ws.onclose = () => {
        console.log('[Gateway] Disconnected from server');
        this.connected = false;
      };

      this.ws.onerror = (error) => {
        console.error('[Gateway] Connection error:', error);
      };

      // Listen for BLE events from Gateway
      this.ws.onmessage = (event) => {
        try {
          const data = JSON.parse(event.data);
          console.log('[Gateway] BLE event received:', data);
          this.notifySubscribers(data);
        } catch (error) {
          console.error('[Gateway] Error parsing message:', error);
        }
      };
    } catch (error) {
      console.error('[Gateway] Failed to create WebSocket:', error);
    }

    return this.ws;
  }

  // Subscribe to BLE events
  subscribe(callback) {
    this.subscribers.push(callback);
    
    // Return unsubscribe function
    return () => {
      this.subscribers = this.subscribers.filter(cb => cb !== callback);
    };
  }

  // Notify all subscribers
  notifySubscribers(data) {
    this.subscribers.forEach(callback => {
      try {
        callback(data);
      } catch (error) {
        console.error('[Gateway] Subscriber error:', error);
      }
    });
  }

  // Disconnect from Gateway
  disconnect() {
    if (this.ws) {
      this.ws.close();
      this.ws = null;
      this.connected = false;
      this.subscribers = [];
    }
  }

  // Check connection status
  isConnected() {
    return this.connected && this.ws && this.ws.readyState === WebSocket.OPEN;
  }

  // Get Gateway URL
  getGatewayUrl() {
    return GATEWAY_URL;
  }
}

// Export singleton instance
const gatewayClient = new GatewayClient();
export default gatewayClient;
