package com.misfit.syncsdk.algorithm;

import android.util.Log;

import com.misfit.cloud.algorithm.algos.GraphItemShineAlgorithm;
import com.misfit.cloud.algorithm.models.ActivityShineVect;
import com.misfit.cloud.algorithm.models.GraphItemShine;
import com.misfit.cloud.algorithm.models.GraphItemShineVect;
import com.misfit.syncsdk.model.SdkGraphDay;
import com.misfit.syncsdk.model.SdkGraphItem;
import com.misfit.syncsdk.utils.MLog;

import java.util.ArrayList;
import java.util.List;

/**
 * similar to prometheus.algorithm.GraphItemBuilder
 */
public class SdkGraphItemBuilder {

    private static final String TAG = "SdkGraphItemBuilder";
    public static final int GRAPH_ITEM_RESOLUTION = 900;  // seconds of 15 min

    /**
     * @parameter ActivityShineVect of per minute activity data. its inner ActivityShine list must have been filtered by lastSyncTime
     * */
    public static List<SdkGraphItem> buildGraphItems(ActivityShineVect activityShineVect) {
        GraphItemShineAlgorithm graphItemShineAlgorithm = new GraphItemShineAlgorithm();
        int activityStartTime = activityShineVect.get(0).getStartTime();
        int inGraphItemTimestamp = activityStartTime - activityStartTime % GRAPH_ITEM_RESOLUTION; // start from a timestamp aligned with 15 min
        Log.d(TAG, "incomplete graph item timestamp " + inGraphItemTimestamp);

        GraphItemShineVect inGraphItemShineVect = new GraphItemShineVect();
        GraphItemShineVect outGraphItemShineVect = new GraphItemShineVect();
        graphItemShineAlgorithm.buildGraphItems(activityShineVect, inGraphItemShineVect, outGraphItemShineVect);
        return convertGraphItemShineVect2List(outGraphItemShineVect);
    }

    /**
     * among a list of SdkGraphItem, find the one which start time equals to given timestamp
     * */
    private static GraphItemShineVect getIncompleteGraphItemShine(SdkGraphDay graphDay, int inGraphItemTimestamp) {
        GraphItemShineVect graphItemShineVect = new GraphItemShineVect();
        if (graphDay == null) {
            return graphItemShineVect;
        }
        List<SdkGraphItem> graphItems = graphDay.getItems();
        for (SdkGraphItem item : graphItems) {
            if(item.getStartTime() == inGraphItemTimestamp && !isCompleteSdkGraphItem(item)) {
                Log.d(TAG, "find incomplete graph item " + item.getStartTime());
                GraphItemShine graphItemShine = new GraphItemShine();
                graphItemShine.setEndTime(item.getEndTime());
                graphItemShine.setStartTime(item.getStartTime());
                graphItemShine.setAveragePoint((float) item.getValue());
                graphItemShineVect.add(graphItemShine);
                return graphItemShineVect;
            }
        }
        return graphItemShineVect;
    }

    private static List<SdkGraphItem> convertGraphItemShineVect2List(GraphItemShineVect graphItemShineVect) {
        List<SdkGraphItem> graphItems = new ArrayList<>();
        for (int i = 0; i < graphItemShineVect.size(); i ++) {
            SdkGraphItem graphItem = convertGraphItemShine2SdkGraphItem(graphItemShineVect.get(i));
            graphItems.add(graphItem);
        }
        return graphItems;
    }

    private static SdkGraphItem convertGraphItemShine2SdkGraphItem(GraphItemShine graphItemShine) {
        SdkGraphItem graphItem = new SdkGraphItem();
        graphItem.setValue(graphItemShine.getAveragePoint());
        graphItem.setStartTime(graphItemShine.getStartTime());
        graphItem.setEndTime(graphItemShine.getEndTime());
        return graphItem;
    }

    private static boolean isCompleteSdkGraphItem(SdkGraphItem graphItem) {
        return (graphItem.getEndTime() - graphItem.getStartTime() + 1) == GRAPH_ITEM_RESOLUTION;
    }
}
