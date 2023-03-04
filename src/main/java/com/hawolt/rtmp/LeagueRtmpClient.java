package com.hawolt.rtmp;

import com.hawolt.generic.token.impl.StringTokenSupplier;
import com.hawolt.logger.Logger;
import com.hawolt.rtmp.amf.TypedObject;
import com.hawolt.rtmp.amf.encoder.AMFEncoder;
import com.hawolt.rtmp.handshake.Handshake;
import com.hawolt.rtmp.handshake.impl.LegacyHandshake;
import com.hawolt.rtmp.service.impl.ReplayService;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Base64;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;


/**
 * Extension of the very basic RtmpClient allowing to exchange data with League of Legends rtmp server
 *
 * @author Hawolt
 * @see RtmpClient
 */
public class LeagueRtmpClient extends RtmpClient {

    private final ScheduledExecutorService service = Executors.newSingleThreadScheduledExecutor();
    public static RtmpConnectInfo.Builder base = new RtmpConnectInfo.Builder()
            .setFlashVersion("WIN 11,7,700,169")
            .setFPAD(false)
            .setCapabilities(239)
            .setAudioCodecs(3191)
            .setVideoCodecs(242)
            .setVideoFunction(1)
            .setObjectEncoding(3);
    private final AMFEncoder encoder = new AMFEncoder();
    private StringTokenSupplier supplier;
    private String platform, username, token;
    private long accountId;

    private ReplayService replayService;

    public LeagueRtmpClient(String platform, String host, int port) {
        super(host, port);
        this.platform = platform;
    }

    public void authorize(Handshake handshake) {
        super.initialize(handshake);
        RtmpConnectInfo rtmpConnectInfo = LeagueRtmpClient.base.replicate()
                .setApp("")
                .setSwfURL("app:/LolClient.swf/[[DYNAMIC]]/50")
                .setTcURL(String.format("rtmps://%s", String.join(":", host, String.valueOf(port))))
                .setPageURL("")
                .build();
        this.connect(rtmpConnectInfo);
    }

    @Override
    public void onConnectionFailure(IOException e) {
        Logger.error("[rtmp] failed to connect");
        Logger.error(e);
    }

    public void connect(String username, StringTokenSupplier supplier) {
        connect(username, supplier, new LegacyHandshake());
    }

    public void connect(String username, StringTokenSupplier supplier, Handshake handshake) {
        this.supplier = supplier;
        this.username = username;
        this.authorize(handshake);
    }

    @Override
    public void onConnect(String DSId) {
        this.DSId = DSId;
        try {
            this.login();
        } catch (IOException e) {
            onLoginException(e);
        }
    }

    private void login() throws IOException {
        TypedObject typedObject = new TypedObject("com.riotgames.platform.login.AuthenticationCredentials");
        typedObject.put("macAddress", "000000000000");
        typedObject.put("authToken", "");
        typedObject.put("userInfoTokenJwe", supplier.get("rtmp.userinfo.lol.userinfo_token", true));
        typedObject.put("leagueSessionToken", supplier.get("rtmp.session.session_token", true));
        typedObject.put("sessionIpToken", supplier.get("rtmp.sipt.sipt_token", true));
        typedObject.put("partnerCredentials", supplier.get("rtmp.lol.access_token", true));
        typedObject.put("domain", "lolclient.lol.riotgames.com");
        typedObject.put("clientVersion", "LCU");
        typedObject.put("locale", "en_GB");
        typedObject.put("username", username);
        typedObject.put("operatingSystem", "{\"edition\":\"Professional, x64\",\"platform\":\"Windows\",\"versionMajor\":\"10\",\"versionMinor\":\"\"}");
        typedObject.put("securityAnswer", null);
        typedObject.put("oldPassword", null);
        typedObject.put("password", null);
        int id = invoke("loginService", "login", new Object[]{typedObject});
        TypedObject result = await(id).getValue();
        if (result.get("result").equals("_error")) throw new IOException(getErrorMessage(result));
        TypedObject body = result.getTypedObject("data").getTypedObject("body");
        this.token = body.getString("token");
        this.accountId = body.getTypedObject("accountSummary").getLong("accountId");
        service.scheduleAtFixedRate(this::heartbeat, 0, 2, TimeUnit.MINUTES);
        String[] channels = new String[]{"gn", "cn", "bc"};
        for (int i = 1; i >= 0; i--) {
            for (String channel : channels) {
                subscribe(String.join("-", channel, String.valueOf(accountId)), i);
            }
        }
        byte[] buffer = String.join(":", username.toLowerCase(), token).getBytes(StandardCharsets.UTF_8);
        TypedObject auth = wrap("auth", 8, Base64.getEncoder().encodeToString(buffer));
        auth.setType("flex.messaging.messages.CommandMessage");
        TypedObject response = await(invoke(auth)).getValue();
        //TODO maybe confirm login worked here
        configure();
    }

    private void configure() {
        this.replayService = new ReplayService(this, platform);
    }

    private void subscribe(String client, int operation) throws IOException {
        TypedObject body = wrap("messagingDestination", operation, new Object[]{new TypedObject()});
        body.setType("flex.messaging.messages.CommandMessage");
        TypedObject headers = new TypedObject();
        headers.put("DSEndpoint", "my-rtmp");
        headers.put("DSSubtopic", !client.startsWith("b") ? client : client.split("-")[0]);
        headers.put("DSRequestTimeout", 60);
        headers.put("DSId", DSId);
        body.put("headers", headers);
        body.put("clientId", client);
        int id = invoke(body);
        await(id);
    }

    private void heartbeat() {
        Object[] data = new Object[]{
                accountId,
                token,
                heartbeat,
                Instant.now().atOffset(ZoneOffset.UTC).toLocalDateTime()
        };
        try {
            invoke("loginService", "performLCDSHeartBeat", data);
            heartbeat++;
        } catch (IOException e) {
            Logger.error("[rtmp-out] failed to send heartbeat");
        }
    }

    public String getErrorMessage(TypedObject typedObject) {
        return typedObject.getTypedObject("data").getTypedObject("rootCause").getString("message");
    }

    public void onLoginException(IOException e) {
        Logger.fatal("[rtmp] failed to login");
        Logger.error(e);
    }

    @Override
    public void onDisconnect() {
        Logger.fatal("[rtmp] connection lost");
    }

    public ReplayService getReplayService() {
        return replayService;
    }

    @Override
    public TypedObject wrap(String destination, Object operation, Object body) {
        TypedObject headers = new TypedObject();
        headers.put("DSRequestTimeout", 60);
        headers.put("DSId", DSId);
        headers.put("DSEndpoint", "my-rtmp");

        TypedObject typedObject = new TypedObject("flex.messaging.messages.RemotingMessage");
        typedObject.put("destination", destination);
        typedObject.put("operation", operation);
        typedObject.put("source", null);
        typedObject.put("timestamp", 0);
        typedObject.put("messageId", String.join("-", String.valueOf(accountId), String.valueOf(messageCounter++)));
        typedObject.put("timeToLive", 0);
        typedObject.put("clientId", null);
        typedObject.put("headers", headers);
        typedObject.put("body", body);
        return typedObject;
    }
}
