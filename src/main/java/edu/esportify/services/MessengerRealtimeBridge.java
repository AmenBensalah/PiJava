package edu.esportify.services;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public final class MessengerRealtimeBridge {
    public interface Listener {
        void onMessengerChanged();
    }

    private static final List<Listener> LISTENERS = new CopyOnWriteArrayList<>();

    private MessengerRealtimeBridge() {
    }

    public static void subscribe(Listener listener) {
        if (listener != null) {
            LISTENERS.add(listener);
        }
    }

    public static void unsubscribe(Listener listener) {
        LISTENERS.remove(listener);
    }

    public static void publish() {
        for (Listener listener : LISTENERS) {
            listener.onMessengerChanged();
        }
    }
}
