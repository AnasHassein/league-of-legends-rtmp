package com.hawolt.rtmp.service.impl;

import com.hawolt.rtmp.RtmpClient;
import com.hawolt.rtmp.amf.Pair;
import com.hawolt.rtmp.amf.TypedObject;
import com.hawolt.rtmp.service.AbstractService;
import com.hawolt.rtmp.utility.PacketCallback;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;

/**
 * Service to fetch replay data
 *
 * @author Hawolt
 **/

public class ReplayService extends AbstractService {
    private final String platform;

    public ReplayService(RtmpClient client, String platform) {
        super(client);
        this.platform = platform;
    }

    private Pair<String, TypedObject> requestMultipleReplay(long... gameIds) {
        JSONArray array = new JSONArray();
        for (long gameId : gameIds) array.put(gameId);
        String data = new JSONObject().put("platformId", platform).put("gameIds", array).toString();
        String id = client.getAmfEncoder().generateUUID();
        return Pair.from(id, client.wrap("lcdsServiceProxy", "call", new Object[]{
                id,
                "replays.retrieval",
                "getMultipleMetadata",
                data
        }));
    }

    public TypedObject requestMultipleReplayMetadataBlocking(long... gameIds) throws IOException {
        Pair<String, TypedObject> pair = requestMultipleReplay(gameIds);
        client.invoke(pair.getValue());
        return client.await(pair.getKey()).getValue();
    }

    public void requestMultipleReplayMetadataAsynchronous(PacketCallback callback, long... gameIds) throws IOException {
        Pair<String, TypedObject> pair = requestMultipleReplay(gameIds);
        client.register(pair.getKey(), callback);
        client.invoke(pair.getValue());
    }

    private Pair<String, TypedObject> requestReplayDownloadUrl(long gameId) {
        String data = new JSONObject().put("platformId", platform).put("gameId", gameId).toString();
        String id = client.getAmfEncoder().generateUUID();
        return Pair.from(id, client.wrap("lcdsServiceProxy", "call", new Object[]{
                id,
                "replays.retrieval",
                "getDownloadUrl",
                data
        }));
    }

    public TypedObject requestReplayDownloadUrlBlocking(long gameId) throws IOException {
        Pair<String, TypedObject> pair = requestReplayDownloadUrl(gameId);
        client.invoke(pair.getValue());
        return client.await(pair.getKey()).getValue();
    }

    public void requestReplayDownloadUrlAsynchronous(PacketCallback callback, long gameId) throws IOException {
        Pair<String, TypedObject> pair = requestReplayDownloadUrl(gameId);
        client.register(pair.getKey(), callback);
        client.invoke(pair.getValue());
    }
}
