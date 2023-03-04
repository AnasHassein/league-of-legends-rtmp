package com.hawolt.rtmp;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Object including Builder to create an Object that allows you to establish a rtmp connection
 *
 * @author Hawolt
 * @see "https://en.wikipedia.org/wiki/Real-Time_Messaging_Protocol#Connect"
 */
public class RtmpConnectInfo {
    private final Map<String, Object> map;

    public RtmpConnectInfo(Map<String, Object> map) {
        this.map = map;
    }

    public Object getValue(String property) {
        return map.get(property);
    }

    public Map<String, Object> getMap() {
        return map;
    }

    public static class Builder {
        private final Map<String, Object> properties = new HashMap<>();

        public Builder() {

        }

        public Builder replicate() {
            Builder builder = new Builder();
            List<Map.Entry<String, Object>> list = new ArrayList<>(properties.entrySet());
            for (Map.Entry<String, Object> entries : list) {
                builder.set(entries.getKey(), entries.getValue());
            }
            return builder;
        }

        public Builder set(String property, Object o) {
            this.properties.put(property, o);
            return this;
        }

        public Builder setApp(Object o) {
            return set("app", o);
        }

        public Builder setFlashVersion(Object o) {
            return set("flashVer", o);
        }

        public Builder setSwfURL(Object o) {
            return set("swfUrl", o);
        }

        public Builder setTcURL(Object o) {
            return set("tcUrl", o);
        }

        public Builder setFPAD(Object o) {
            return set("fpad", o);
        }

        public Builder setCapabilities(Object o) {
            return set("capabilities", o);
        }

        public Builder setAudioCodecs(Object o) {
            return set("audioCodecs", o);
        }

        public Builder setVideoCodecs(Object o) {
            return set("videoCodecs", o);
        }

        public Builder setVideoFunction(Object o) {
            return set("objectEncoding", o);
        }

        public Builder setPageURL(Object o) {
            return set("pageUrl", o);
        }

        public Builder setObjectEncoding(Object o) {
            return set("objectEncoding", o);
        }

        public RtmpConnectInfo build() {
            return new RtmpConnectInfo(properties);
        }
    }
}
