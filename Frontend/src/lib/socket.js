import { Client } from '@stomp/stompjs';
import SockJS from 'sockjs-client';

const SOCKET_URL = import.meta.env.VITE_API_URL?.replace('/api', '') || 'http://localhost:8080';

class SocketService {
  constructor() {
    this.client = null;
    this._listeners = new Map();
    this._currentToken = null;
  }

  connect(token) {
    if (this.client?.connected && this._currentToken === token) return;

    if (this.client) {
      this.client.deactivate();
    }

    this._currentToken = token;

    this.client = new Client({
      webSocketFactory: () => new SockJS(`${SOCKET_URL}/ws`),
      connectHeaders: {
        Authorization: `Bearer ${token}`
      },
      reconnectDelay: 5000,
      heartbeatIncoming: 4000,
      heartbeatOutgoing: 4000,
    });

    this.client.onConnect = (frame) => {
      console.log('✅ STOMP connected:', frame);
      
      // We subscribe to a user-specific queue or a generic events topic where 
      // the backend can push { event: 'name', data: payload }
      this.client.subscribe('/user/queue/events', (message) => {
        if (message.body) {
          try {
            const parsed = JSON.parse(message.body);
            const { event, data } = parsed;
            if (this._listeners.has(event)) {
              this._listeners.get(event).forEach(cb => cb(data));
            }
          } catch(e) {
            console.error("Failed to parse STOMP message", e);
          }
        }
      });
    };

    this.client.onStompError = (frame) => {
      console.error('❌ STOMP error:', frame);
    };

    this.client.activate();
  }

  disconnect() {
    if (this.client) {
      this.client.deactivate();
      this.client = null;
    }
    this._currentToken = null;
  }

  on(event, callback) {
    if (!this._listeners.has(event)) {
      this._listeners.set(event, new Set());
    }
    this._listeners.get(event).add(callback);
  }

  off(event, callback) {
    if (callback && this._listeners.has(event)) {
      this._listeners.get(event).delete(callback);
      if (this._listeners.get(event).size === 0) {
        this._listeners.delete(event);
      }
    } else if (!callback) {
      this._listeners.delete(event);
    }
  }

  emit(event, data) {
    if (this.client && this.client.connected) {
      this.client.publish({
        destination: '/app/message',
        body: JSON.stringify({ event, data })
      });
    }
  }
}

const socketService = new SocketService();
export default socketService;
