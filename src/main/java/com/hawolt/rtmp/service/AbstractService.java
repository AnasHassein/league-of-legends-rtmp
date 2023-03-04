package com.hawolt.rtmp.service;

import com.hawolt.rtmp.RtmpClient;

/**
 * Helper class to build Service extensions
 *
 * @author Hawolt
 **/

public class AbstractService {
    protected final RtmpClient client;

    public AbstractService(RtmpClient client) {
        this.client = client;
    }
}
