package com.misfit.syncsdk.algorithm;

import android.support.annotation.NonNull;

import com.misfit.cloud.algorithm.algos.GraphItemShineAlgorithm;
import com.misfit.cloud.algorithm.models.ActivityShineVect;
import com.misfit.cloud.algorithm.models.GraphItemShine;
import com.misfit.cloud.algorithm.models.GraphItemShineVect;
import com.misfit.syncsdk.callback.SyncCalculationCallback;
import com.misfit.syncsdk.model.SdkActivitySession;
import com.misfit.syncsdk.model.SdkDayRange;
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

    public static List<SdkGraphItem> buildGraphItems(ActivityShineVect activityShineVect,
                                                     @NonNull SdkDayRange dayRange,
                                                     @NonNull SyncCalculationCallback calculationCallback) {
        GraphItemShineAlgorithm graphItemShineAlgorithm = new GraphItemShineAlgorithm();
        int activityStartTime = activityShineVect.get(0).getStartTime();
        int inGraphItemTimestamp = activityStartTime - activityStartTime % GRAPH_ITEM_RESOLUTION;
        MLog.d(TAG, "incomplete graph item timestamp " + inGraphItemTimestamp);

        SdkGraphDay graphDay= calculationCallback.getSdkGraphDayByDate(dayRange.day);
        GraphItemShineVect inGraphItemShineVect = getIncompleteGraphItemShine(graphDay, inGraphItemTimestamp);
        GraphItemShineVect outGraphItemShineVect = new GraphItemShineVect();
        graphItemShineAlgorithm.buildGraphItems(activityShineVect, inGraphItemShineVect, outGraphItemShineVect);
        return convertGraphItemShineVect2List(outGraphItemShineVect);
    }

    /**
     * Build graph items from the activity sessions, all the sessions should be in the same day.
     *
     * @param activitySessions the activity sessions from which to build graph items
     * @param appendLastIncompleteGraph whether we should append the last incomplete graph if exists
     *
     * @return the built graph items
     *
     * NOTE: this method will not be used in SyncSDK v1.0
     */
    public static List<SdkGraphItem> buildGraphItemsForGoogleFit(@NonNull List<SdkActivitySession> activitySessions,
                                                                 @NonNull SdkDayRange dayRange,
                                                                 boolean appendLastIncompleteGraph,
                                                                 @NonNull SyncCalculationCallback calculationCallback) {
        List<SdkGraphItem> graphItems = new ArrayList<>();

        if (activitySessions.isEmpty()) {
            return graphItems;
        }

        int sessionStartTimeInDay = (int) (activitySessions.get(0).getStartTime() - dayRange.startTime);
        //int SECONDS_OF_QUARTER = 900; // 900s is one quarter.
        int graphStartTime = (int) dayRange.startTime
            + (sessionStartTimeInDay - (sessionStartTimeInDay % GRAPH_ITEM_RESOLUTION));
        int graphEndTime = graphStartTime + GRAPH_ITEM_RESOLUTION - 1;
        SdkGraphItem graphItem = new SdkGraphItem();
        graphItem.setTimestamp(graphStartTime);
        graphItem.setStartTime(graphStartTime);
        graphItem.setEndTime(graphEndTime);
        graphItems.add(graphItem);

        // Append the value of last uncompleted graph item.
        if (appendLastIncompleteGraph) {
            MLog.d(TAG, "incomplete graph item timestamp " + graphStartTime);
            SdkGraphDay graphDay = calculationCallback.getSdkGraphDayByDate(dayRange.day);
            GraphItemShineVect incompleteGraphItemShineVect = getIncompleteGraphItemShine(graphDay, graphStartTime);
            if (!incompleteGraphItemShineVect.isEmpty()) {
                graphItem.setValue(incompleteGraphItemShineVect.get(0).getAveragePoint());
            }
        }

        for (int i = 0, N = activitySessions.size(); i < N; i++) {
            SdkActivitySession activitySession = activitySessions.get(i);
            int sessionStartTime = (int) activitySession.getStartTime();
            int sessionDuration = activitySession.getDuration();
            // For Google Fit, startTime + duration = endTime.
            int sessionEndTime = sessionStartTime + sessionDuration;
            float sessionPoints = activitySession.getPoints();
            if (sessionEndTime <= graphEndTime + 1) {
                // Graph:   |________|
                // Session: |||____|||
                // This session is totally in this quarter, let's add the point.
                graphItem.setValue((graphItem.getValue() * GRAPH_ITEM_RESOLUTION + sessionPoints) / GRAPH_ITEM_RESOLUTION);
            } else if (sessionStartTime <= graphEndTime) {
                // Graph:   |________|
                // Session:       |||______|
                // Should separate this session to multiple graph items.

                // Step 1: Add the points within current graph item.
                float pointsInCurrentGraph = (sessionPoints / sessionDuration * (graphEndTime + 1 - sessionStartTime));
                graphItem.setValue((graphItem.getValue() * GRAPH_ITEM_RESOLUTION + pointsInCurrentGraph) / GRAPH_ITEM_RESOLUTION);

                // Step 2: Start a new graph item.
                graphStartTime = graphEndTime + 1;
                graphEndTime = graphStartTime + GRAPH_ITEM_RESOLUTION - 1;
                graphItem = new SdkGraphItem();
                graphItem.setTimestamp(graphStartTime);
                graphItem.setStartTime(graphStartTime);
                graphItem.setEndTime(graphEndTime);
                graphItems.add(graphItem);

                // Step 3: Slim the temp session.
                sessionPoints -= pointsInCurrentGraph;
                sessionDuration = sessionEndTime - graphStartTime;

                // Step 4: Add the points within new graph item.
                while (sessionEndTime > graphEndTime + 1) {
                    pointsInCurrentGraph = (sessionPoints / sessionDuration * GRAPH_ITEM_RESOLUTION);
                    graphItem.setValue((graphItem.getValue() * GRAPH_ITEM_RESOLUTION + pointsInCurrentGraph) / GRAPH_ITEM_RESOLUTION);

                    // Start a new graph item.
                    graphStartTime = graphEndTime + 1;
                    graphEndTime = graphStartTime + GRAPH_ITEM_RESOLUTION - 1;
                    graphItem = new SdkGraphItem();
                    graphItem.setTimestamp(graphStartTime);
                    graphItem.setStartTime(graphStartTime);
                    graphItem.setEndTime(graphEndTime);
                    graphItems.add(graphItem);

                    // Slim the temp session.
                    sessionPoints -= pointsInCurrentGraph;
                    sessionStartTime = graphStartTime;
                    sessionDuration = sessionEndTime - sessionStartTime;
                }
                // Save the end session if needed.
                if (sessionEndTime > graphStartTime) {
                    graphItem.setValue((graphItem.getValue() * GRAPH_ITEM_RESOLUTION + sessionPoints) / GRAPH_ITEM_RESOLUTION);
                }
            } else {
                // Graph:   |________|
                // Session:          |||______|
                // This session occur after current graph item, let's create the target graph item.
                sessionStartTimeInDay = sessionStartTime - (int) dayRange.startTime;
                graphStartTime = (int) dayRange.startTime
                    + (sessionStartTimeInDay - (sessionStartTimeInDay % GRAPH_ITEM_RESOLUTION));
                graphEndTime = graphStartTime + GRAPH_ITEM_RESOLUTION - 1;
                graphItem = new SdkGraphItem();
                graphItem.setTimestamp(graphStartTime);
                graphItem.setStartTime(graphStartTime);
                graphItem.setEndTime(graphEndTime);
                graphItems.add(graphItem);

                // Redirect to this session in next loop.
                i--;
            }
        }

        // Adjust the latest graph item's end time, in case the graph is incomplete.
        SdkGraphItem lastGraphItem = graphItems.get(graphItems.size() - 1);
        SdkActivitySession lastActivitySession = activitySessions.get(activitySessions.size() - 1);
        long activitySessionEndTime = lastActivitySession.getStartTime() + lastActivitySession.getDuration();
        lastGraphItem.setEndTime(Math.min(lastGraphItem.getEndTime(), activitySessionEndTime));

        return graphItems;
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
                MLog.d(TAG, "find incomplete graph item " + item.getStartTime());
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
        MLog.d(TAG, "convertGraphItemShineVect2List: " + graphItemShineVect.size());
        List<SdkGraphItem> graphItems = new ArrayList<>();
        for (int i = 0; i < graphItemShineVect.size(); i ++) {
            SdkGraphItem graphItem = convertGraphItemShine2SdkGraphItem(graphItemShineVect.get(i));
            graphItems.add(graphItem);
        }
        return graphItems;
    }

    private static SdkGraphItem convertGraphItemShine2SdkGraphItem(GraphItemShine graphItemShine) {
        MLog.d(TAG, "convertGraphItemShine2SdkGraphItem");
        SdkGraphItem graphItem = new SdkGraphItem();
        graphItem.setTimestamp((int) graphItemShine.getStartTime());
        graphItem.setValue(graphItemShine.getAveragePoint());
        graphItem.setStartTime(graphItemShine.getStartTime());
        graphItem.setEndTime(graphItemShine.getEndTime());
        MLog.d(TAG, String.format("timestamp: %d, startTime: %d, endTime: %d, value: %f",
            graphItem.getTimestamp(), graphItem.getStartTime(), graphItem.getEndTime(), graphItem.getValue()));
        return graphItem;
    }

    private static boolean isCompleteSdkGraphItem(SdkGraphItem graphItem) {
        return (graphItem.getEndTime() - graphItem.getStartTime() + 1) == GRAPH_ITEM_RESOLUTION;
    }
}
