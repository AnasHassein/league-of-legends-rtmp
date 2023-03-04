package com.hawolt.rtmp;

import com.hawolt.logger.Logger;
import com.hawolt.rtmp.amf.Pair;
import com.hawolt.rtmp.amf.TypedObject;
import com.hawolt.rtmp.amf.encoder.AMFEncoder;
import com.hawolt.rtmp.handshake.Handshake;
import com.hawolt.rtmp.io.RtmpPacket;
import com.hawolt.rtmp.io.RtmpPacketReader;
import com.hawolt.rtmp.utility.PacketCallback;
import com.hawolt.rtmp.utility.PacketManager;

import javax.net.ssl.SSLSocketFactory;
import java.io.IOException;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


/**
 * A very basic stripped down rtmp client for basic interaction with League of Legends rtmp server
 *
 * @author Hawolt
 */

public abstract class RtmpClient implements SocketConnection, PacketManager {
    private final Map<Object, Pair<RtmpPacket, TypedObject>> cache = new HashMap<>();
    private final ExecutorService service = Executors.newSingleThreadExecutor();
    private final Map<Object, PacketCallback> callbacks = new HashMap<>();
    private PacketCallback defaultCallback;
    private Socket socket;
    protected final AMFEncoder amfEncoder = new AMFEncoder();
    protected final String host;
    protected final int port;
    protected int peerBandwidth, messageCounter, heartbeat = 1, invokeID = 2;
    protected String DSId;

    public RtmpClient(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public void connect() {
        try {
            this.socket = SSLSocketFactory.getDefault().createSocket(host, port);
        } catch (IOException e) {
            onConnectionFailure(e);
        }
    }

    public void initialize(Handshake handshake) {
        this.connect();
        try {
            Logger.debug("Performing handshake");
            handshake.perform(this);
            Logger.debug("Handshake complete");
            service.execute(new RtmpPacketReader(this));
        } catch (IOException e) {
            onConnectionFailure(e);
        }
    }

    public void connect(RtmpConnectInfo rtmpConnectInfo) {
        try {
            write(amfEncoder.encodeConnect(rtmpConnectInfo.getMap()));
            Pair<RtmpPacket, TypedObject> pair = await(1);
            onConnect(pair.getValue().getTypedObject("data").getString("id"));
        } catch (IOException e) {
            onConnectionFailure(e);
        }
    }

    public AMFEncoder getAmfEncoder() {
        return amfEncoder;
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    public int getPeerBandwidth() {
        return peerBandwidth;
    }

    public void setPeerBandwidth(int peerBandwidth) {
        Logger.debug("Peer Bandwidth: {}", this.peerBandwidth = peerBandwidth);
    }

    public PacketCallback getDefaultCallback() {
        return defaultCallback;
    }

    public void setDefaultCallback(PacketCallback defaultCallback) {
        this.defaultCallback = defaultCallback;
    }

    @Override
    public boolean isConnected() {
        return socket.isConnected();
    }

    @Override
    public void write(byte... b) throws IOException {
        socket.getOutputStream().write(b, 0, b.length);
        socket.getOutputStream().flush();
    }

    @Override
    public byte read() throws IOException {
        return (byte) socket.getInputStream().read();
    }

    @Override
    public int read(byte[] b) throws IOException {
        return socket.getInputStream().read(b);
    }

    @Override
    public int read(byte[] b, int offset, int length) throws IOException {
        return socket.getInputStream().read(b, offset, length);
    }

    @Override
    public Socket getSocket() {
        return socket;
    }

    public Pair<RtmpPacket, TypedObject> await(Object o) {
        Pair<RtmpPacket, TypedObject> object = null;
        do {
            if (cache.containsKey(o)) {
                Logger.debug("[rtmp] pulling value from cache for callback {}", o);
                object = cache.remove(o);
            } else {
                try {
                    Thread.sleep(100L);
                } catch (InterruptedException e) {
                    Logger.error(e);
                }
            }
        } while (object == null);
        return object;
    }

    @Override
    public PacketCallback getPacketCallback(Object o) {
        return callbacks.get(o);
    }

    @Override
    public void cache(Object id, RtmpPacket packet, TypedObject object) {
        Logger.debug("[rtmp-in] caching: {}, as: {}", id, object);
        this.cache.put(id, Pair.from(packet, object));
    }

    @Override
    public void register(Object id, PacketCallback callback) {
        Logger.debug("[rtmp] registering callback for {}", id);
        this.callbacks.put(id, callback);
    }

    @Override
    public boolean isPacketCallbackRegistered(Object o) {
        Logger.debug("[rtmp] checking for callback {}", o);
        return callbacks.containsKey(o);
    }

    @Override
    public void unregister(Object o) {
        callbacks.remove(o);
    }

    protected int nextInvokeID() {
        return invokeID++;
    }

    protected synchronized int invoke(String destination, String operation, Object[] body) throws IOException {
        return invoke(wrap(destination, operation, body));
    }

    public synchronized int invoke(TypedObject typedObject) throws IOException {
        int id = nextInvokeID();
        submit(id, typedObject);
        return id;
    }

    private void submit(int id, TypedObject typedObject) throws IOException {
        Logger.debug("[rtmp-out] invokeId: {}, data: {}", id, typedObject);
        byte[] data = amfEncoder.encodeInvoke(id, typedObject);
        socket.getOutputStream().write(data, 0, data.length);
        socket.getOutputStream().flush();
    }

    public abstract TypedObject wrap(String destination, Object operation, Object body);

}
