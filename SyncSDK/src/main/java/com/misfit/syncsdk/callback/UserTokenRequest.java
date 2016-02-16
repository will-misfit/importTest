package com.misfit.syncsdk.callback;

/**
 * Used as callback to query user auth token from App invoker
 */
public interface UserTokenRequest {
    String getCurrentUserToken();
}
