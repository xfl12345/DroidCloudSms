package cc.xfl12345.android.droidcloudsms.model.ws;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Supplier;

import jakarta.annotation.PreDestroy;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import okio.ByteString;

// source code URL=https://blog.csdn.net/qq_25602107/article/details/104022502
public class WebSocketManagerBackup {

    protected Supplier<OkHttpClient> clientSupplier;

    protected Supplier<Request> requestSupplier;

    private WebSocketListener defaultExtraWebSocketListener = new WebSocketListener() {
        @Override
        public void onClosed(@NonNull WebSocket webSocket, int code, @NonNull String reason) {
            super.onClosed(webSocket, code, reason);
        }
    };

    protected WebSocketListener extraWebSocketListener = defaultExtraWebSocketListener;

    protected StatusListener statusListener;

    protected WebSocket webSocket;

    protected final ReentrantReadWriteLock destroyLock = new ReentrantReadWriteLock(true);

    protected volatile boolean destroying = false;

    protected volatile boolean manualClose = false;

    protected boolean connected = false;

    protected boolean reconnecting = false;

    protected int retryCount = 0;

    protected int maxRetry = 5;       // 最大重连数

    protected int reconnectInterval = 5000;     // 重连间隔时间，毫秒


    public WebSocketManagerBackup(Supplier<OkHttpClient> clientSupplier, Supplier<Request> requestSupplier, StatusListener statusListener) {
        this.clientSupplier = clientSupplier;
        this.requestSupplier = requestSupplier;
        this.statusListener = statusListener != null ? statusListener : new StatusListener() {
            @Override
            public void onConnected() {
            }

            @Override
            public void onDisconnected(String reason, Integer code, Throwable throwable, Response response) {
            }

            @Override
            public void onRetryMaxReached() {
            }
        };
    }


    /**
     * 设置最大重连数
     *
     * @param maxRetry 最大重连数
     */
    public void setMaxRetry(int maxRetry) {
        this.maxRetry = maxRetry;
    }

    /**
     * 设置重连间隔时间
     *
     * @param reconnectInterval 重连间隔时间，单位是毫秒
     */
    public void setReconnectInterval(int reconnectInterval) {
        this.reconnectInterval = reconnectInterval;
    }

    public void setExtraWebSocketListener(WebSocketListener extraWebSocketListener) {
        this.extraWebSocketListener = extraWebSocketListener == null
            ? defaultExtraWebSocketListener
            : extraWebSocketListener;
    }

    public boolean isReconnecting() {
        return reconnecting;
    }

    public boolean isDestroying() {
        return destroying;
    }

    public boolean isManualClose() {
        return manualClose;
    }

    /**
     * 连接
     */
    public void connect() throws IllegalArgumentException {
        if (isConnected()) {
            return;
        }

        OkHttpClient client = clientSupplier.get();
        if (client == null) {
            throw new IllegalArgumentException("Recreating client failed. clientSupplier.get() == null");
        }
        Request request = requestSupplier.get();

        client.newWebSocket(request, createListener());
    }

    /**
     * 重连
     */
    public void reconnect() {
        reconnecting = true;
        new Thread(() -> {
            synchronized (WebSocketManagerBackup.class) {
                boolean canRun;
                destroyLock.readLock().lock();
                canRun = !destroying;
                destroyLock.readLock().unlock();

                if (canRun && !isConnected()) {
                    if (retryCount <= maxRetry) {
                        statusListener.onReconnecting();
                        try {
                            Thread.sleep(reconnectInterval);
                            connect();
                            retryCount++;
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    } else {
                        retryCount = 0;
                        statusListener.onRetryMaxReached();
                    }
                }
            }
        }, WebSocketManagerBackup.class.getName() + "_reconnect").start();
    }

    /**
     * 是否连接
     */
    public boolean isConnected() {
        return connected;
    }

    protected void setConnected(boolean connected) {
        this.connected = connected;
        if (connected) {
            statusListener.onConnected();
        }
    }

    public WebSocket getWebSocket() {
        return webSocket;
    }

    /**
     * 发送消息
     *
     * @param text 字符串
     * @return boolean
     */
    public boolean sendMessage(String text) {
        return isConnected() && webSocket.send(text);
    }

    /**
     * 发送消息
     *
     * @param byteString 字符集
     * @return boolean
     */
    public boolean sendMessage(ByteString byteString) {
        return isConnected() && webSocket.send(byteString);
    }


    @PreDestroy
    public void destroy() {
        destroyLock.writeLock().lock();
        destroying = true;
        destroyLock.writeLock().unlock();

        close();
    }

    /**
     * 关闭连接
     */
    public void close() {
        if (isConnected()) {
            manualClose = true;
            webSocket.close(1001, "manual close");
            Thread thread = new Thread(() -> {
                int recheckTimes = 100;
                int recheckCount = 0;
                while (isConnected() && recheckCount <= recheckTimes) {
                    try {
                        Thread.sleep(100);
                        recheckCount += 1;
                        if (recheckCount % 10 == 0) {
                            Log.d(
                                WebSocketManagerBackup.class.getCanonicalName(),
                                String.format("Waiting for socket close.(%s/%s)", recheckCount, recheckTimes)
                            );
                        }
                    } catch (InterruptedException e) {
                        // ignore
                    }
                }
                webSocket.cancel();
                manualClose = false;
            }, WebSocketManagerBackup.class.getName() + "_wait_normal_close");
            thread.start();
            try {
                thread.join();
            } catch (InterruptedException e) {
                // ignore
            }
        }
    }

    protected WebSocketListener createListener() {
        return new WebSocketListener() {
            @Override
            public void onOpen(@NonNull WebSocket webSocket, @NonNull Response response) {
                WebSocketManagerBackup webSocketManager = WebSocketManagerBackup.this;
                webSocketManager.webSocket = webSocket;
                webSocketManager.extraWebSocketListener.onOpen(webSocket, response);
                super.onOpen(webSocket, response);
                webSocketManager.setConnected(response.code() == 101);
            }

            @Override
            public void onMessage(@NonNull WebSocket webSocket, @NonNull String text) {
                super.onMessage(webSocket, text);
                WebSocketManagerBackup webSocketManager = WebSocketManagerBackup.this;
                webSocketManager.extraWebSocketListener.onMessage(webSocket, text);
            }

            @Override
            public void onMessage(@NonNull WebSocket webSocket, @NonNull ByteString bytes) {
                super.onMessage(webSocket, bytes);
                WebSocketManagerBackup webSocketManager = WebSocketManagerBackup.this;
                webSocketManager.extraWebSocketListener.onMessage(webSocket, bytes);
            }

            @Override
            public void onClosing(@NonNull WebSocket webSocket, int code, @NonNull String reason) {
                WebSocketManagerBackup webSocketManager = WebSocketManagerBackup.this;
                webSocketManager.extraWebSocketListener.onClosing(webSocket, code, reason);
                super.onClosing(webSocket, code, reason);
                // 远端关闭 socket 的情况，不会触发 onClosed 事件。因此只好手动调用了……
                if (!webSocketManager.manualClose) {
                    onClosed(webSocket, code, reason);
                }
            }

            @Override
            public void onClosed(@NonNull WebSocket webSocket, int code, @NonNull String reason) {
                WebSocketManagerBackup webSocketManager = WebSocketManagerBackup.this;
                webSocketManager.reconnecting = false;
                webSocketManager.extraWebSocketListener.onClosed(webSocket, code, reason);
                super.onClosed(webSocket, code, reason);
                webSocketManager.setConnected(false);
                webSocketManager.statusListener.onDisconnected(reason, code, null, null);
            }

            @Override
            public void onFailure(@NonNull WebSocket webSocket, @NonNull Throwable throwable, Response response) {
                WebSocketManagerBackup webSocketManager = WebSocketManagerBackup.this;
                webSocketManager.reconnecting = false;
                // if (throwable.getMessage() != null && !throwable.getMessage().equals("Socket closed") && response == null) {
                if (response == null) {
                    reconnect();
                } else {
                    webSocketManager.extraWebSocketListener.onFailure(webSocket, throwable, response);
                    super.onFailure(webSocket, throwable, response);
                    webSocketManager.setConnected(false);
                    webSocketManager.statusListener.onDisconnected(null, null, throwable, response);
                }
            }
        };
    }

    public static interface StatusListener {

        default void onConnected() {}

        default void onReconnecting() {}

        default void onDisconnected(@Nullable String reason, @Nullable Integer code, @Nullable Throwable throwable, @Nullable Response response) {}

        default void onRetryMaxReached() {}

    }
}
