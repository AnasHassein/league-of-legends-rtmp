# league-of-legends-rtmp

league-of-legends-rtmp allows you to connect to league of legends rtmp server and communicate with it

## Maven

to use league-of-legends-rtmp in your maven project include the following repository

```xml
<repositories>
    <repository>
        <id>jitpack.io</id>
        <url>https://jitpack.io</url>
    </repository>
</repositories>
```
and this dependency

```xml
<dependency>
    <groupId>com.github.hawolt</groupId>
    <artifactId>league-of-legends-rtmp</artifactId>
    <version>04e3785d15</version>
</dependency>
```

## Usage

an example usage that will connect you to a rtmp server and fetches the replay download link for a specific game looks as follows

```java
import com.hawolt.authentication.LocalCookieSupplier;
import com.hawolt.generic.data.Platform;
import com.hawolt.generic.token.impl.StringTokenSupplier;
import com.hawolt.logger.Logger;
import com.hawolt.manifest.RMANCache;
import com.hawolt.rtmp.LeagueRtmpClient;
import com.hawolt.rtmp.amf.TypedObject;
import com.hawolt.virtual.leagueclient.VirtualLeagueClient;
import com.hawolt.virtual.leagueclient.VirtualLeagueClientInstance;
import com.hawolt.virtual.leagueclient.authentication.Sipt;
import com.hawolt.virtual.leagueclient.exception.LeagueException;
import com.hawolt.virtual.riotclient.VirtualRiotClient;
import com.hawolt.virtual.riotclient.VirtualRiotClientInstance;
import com.hawolt.yaml.ConfigValue;
import com.hawolt.yaml.YamlWrapper;
import org.json.JSONObject;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;

/**
 * Created: 04/03/2023 15:34
 * Author: Twitter @hawolt
 **/

public class Example {
    public static void main(String[] args) {
        RMANCache.active = true;
        LocalCookieSupplier localCookieSupplier = new LocalCookieSupplier();
        VirtualRiotClientInstance virtualRiotClientInstance = VirtualRiotClientInstance.create(localCookieSupplier);
        try {
            VirtualRiotClient virtualRiotClient = virtualRiotClientInstance.login(args[0], args[1]);
            VirtualLeagueClientInstance virtualLeagueClientInstance = virtualRiotClient.createVirtualLeagueClientInstance();
            CompletableFuture<VirtualLeagueClient> virtualLeagueClientFuture = virtualLeagueClientInstance.login(true, false);
            virtualLeagueClientFuture.whenComplete(((virtualLeagueClient, throwable) -> {
                if (throwable != null) throwable.printStackTrace();
                else {
                    Logger.info("Client setup complete");
                    Logger.info(virtualLeagueClientInstance.getUserInformation());
                    YamlWrapper wrapper = virtualLeagueClient.getYamlWrapper();
                    Platform platform = virtualLeagueClientInstance.getPlatform();
                    try {
                        Sipt sipt = new Sipt(platform, wrapper.get(ConfigValue.LEDGE));
                        sipt.authenticate(virtualRiotClientInstance.getGateway(), virtualLeagueClientInstance.getLocalLeagueFileVersion(), virtualLeagueClient.getSession());
                        virtualLeagueClient.setSipt(sipt);
                        StringTokenSupplier rtmpSupplier = StringTokenSupplier.merge(
                                "rtmp",
                                virtualLeagueClient.getUserinfo(),
                                virtualLeagueClientInstance.getLeagueClientSupplier(),
                                virtualLeagueClient.getSession(),
                                virtualLeagueClient.getSipt()
                        );
                        for (String key : rtmpSupplier.keySet()) {
                            System.out.println(key + ": " + rtmpSupplier.get(key, true));
                        }
                        String[] lcds = wrapper.get(ConfigValue.LCDS).split(":");
                        LeagueRtmpClient rtmpClient = new LeagueRtmpClient(platform.name(), lcds[0], Integer.parseInt(lcds[1]));
                        rtmpClient.connect(args[0], rtmpSupplier);
                        long gameId = 6298035040L;
                        TypedObject metadata = rtmpClient.getReplayService().requestMultipleReplayMetadataBlocking(gameId);
                        String status = metadata.getTypedObject("data").getTypedObject("body").getString("status");
                        if ("OK".equals(status)) {
                            TypedObject response = rtmpClient.getReplayService().requestReplayDownloadUrlBlocking(gameId);
                            String payload = response.getTypedObject("data").getTypedObject("body").getString("payload");
                            JSONObject json = new JSONObject(payload);
                            if (json.has("status") && "OK".equals(json.getString("status"))) {
                                Logger.info("Replay file is available at {}", json.getString("url"));
                            }
                        } else {
                            Logger.error("Unable to parse replay download for game {}_{}", platform.name(), gameId);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }));
        } catch (IOException e) {
            Logger.error(e);
        } catch (LeagueException e) {
            throw new RuntimeException(e);
        }
    }
}

```
