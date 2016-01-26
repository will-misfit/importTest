package com.misfit.syncsdk.enums;

import com.misfit.cloud.algorithm.models.SessionType;

/**
 * a data model to reflect SessionType in algorithm library namespace
 * open to Misfit flagship app
 */
public class SdkSessionType {
    public final static int SESSION_TYPE_AUTO = 0;  // session created by auto tag in
    public final static int SESSION_TYPE_TAG = 1;   // session created by manual tag in
    public final static int SESSION_TYPE_ACE = 2;   // session created by ACE algorithm
    public final static int SESSION_TYPE_SWL = 3;   // session created by swim lap counting algorithm

    private SdkSessionType(){}

    /**
     * convert SessionType in algorithm namespace to the one in local namespace
     * */
    public static int getSessionType(SessionType sType) {
        int result = SESSION_TYPE_AUTO;
        if (sType == SessionType.SESSION_TYPE_AUTO) {
            result = SESSION_TYPE_AUTO;
        } else if (sType == SessionType.SESSION_TYPE_TAG) {
            result = SESSION_TYPE_TAG;
        } else if (sType == SessionType.SESSION_TYPE_ACE) {
            result = SESSION_TYPE_ACE;
        } else if (sType == SessionType.SESSION_TYPE_SWL) {
            result = SESSION_TYPE_SWL;
        }
        return result;
    }
}
