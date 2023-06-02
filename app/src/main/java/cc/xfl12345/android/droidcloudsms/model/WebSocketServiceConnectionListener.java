package cc.xfl12345.android.droidcloudsms.model;

import cc.xfl12345.android.droidcloudsms.WebsocketService;

public interface WebSocketServiceConnectionListener {
    default void onServiceConnected(WebsocketService service) {}

    default void onServiceDisconnected() {}
}
