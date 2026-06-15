import { create } from 'zustand';
import api from '../lib/axios';
import socketService from '../lib/socket';

export const useNotificationStore = create((set, get) => ({
  notifications: [],
  unreadCount: 0,
  loading: false,
  _listening: false,

  fetchNotifications: async () => {
    set({ loading: true });
    try {
      const response = await api.get('/notifications');
      if (response.success) {
        set({
          notifications: response.data.notifications,
          unreadCount: response.data.unreadCount,
          loading: false,
        });
      }
    } catch (err) {
      console.error('Failed to fetch notifications', err);
      set({ loading: false });
    }
  },

  markAsRead: async (id) => {
    try {
      await api.put(`/notifications/${id}/read`);
      set((state) => ({
        notifications: state.notifications.map((n) =>
          (n._id === id || n.id === id) ? { ...n, read: true } : n
        ),
        unreadCount: Math.max(0, state.unreadCount - 1),
      }));
    } catch (err) {
      console.error('Failed to mark read', err);
    }
  },

  markAllAsRead: async () => {
    try {
      await api.put('/notifications/read-all');
      set((state) => ({
        notifications: state.notifications.map((n) => ({ ...n, read: true })),
        unreadCount: 0,
      }));
    } catch (err) {
      console.error('Failed to mark all read', err);
    }
  },

  
  startListening: () => {
    if (get()._listening) return;

    const handleNew = (notification) => {
      set((state) => ({
        notifications: [notification, ...state.notifications],
        unreadCount: state.unreadCount + 1,
      }));
    };

    const handleCount = (data) => {
      set({ unreadCount: data.count });
    };

    socketService.on('notification:new', handleNew);
    socketService.on('notification:count', handleCount);
    set({ _listening: true });
  },

  stopListening: () => {
    socketService.off('notification:new');
    socketService.off('notification:count');
    set({ _listening: false });
  },

  subscribeToWebPush: async () => {
    try {
      if (!('serviceWorker' in navigator) || !('PushManager' in window)) return;
      
      
      if (Notification.permission === 'denied') return;
      
      
      const registration = await navigator.serviceWorker.ready;
      let subscription = await registration.pushManager.getSubscription();
      
      if (!subscription) {
        
        const permission = await Notification.requestPermission();
        if (permission !== 'granted') return;
        
        const vapidPublicKey = 'BPtfqb63qPnbCu5ZZNQaz_jBqpUGNqT7Pit03ojCIcr1WdMdSFX1Es2wPak3i2gLVESlWjYF9GzWgPZn_TXKneA';
        const convertedVapidKey = urlBase64ToUint8Array(vapidPublicKey);
        
        subscription = await registration.pushManager.subscribe({
          userVisibleOnly: true,
          applicationServerKey: convertedVapidKey
        });
      }

      await api.post('/push/subscribe', subscription);
    } catch (err) {
      
      if (err.name !== 'NotAllowedError') {
        console.warn('Web Push not available:', err.message);
      }
    }
  }
}));


function urlBase64ToUint8Array(base64String) {
  const padding = '='.repeat((4 - base64String.length % 4) % 4);
  const base64 = (base64String + padding).replace(/\-/g, '+').replace(/_/g, '/');
  const rawData = window.atob(base64);
  const outputArray = new Uint8Array(rawData.length);
  for (let i = 0; i < rawData.length; ++i) {
    outputArray[i] = rawData.charCodeAt(i);
  }
  return outputArray;
}
