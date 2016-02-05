package com.misfit.syncsdk.operator;

import com.misfit.syncsdk.model.TaskSharedData;
import com.misfit.syncsdk.task.Task;

import java.util.List;

/**
 *
 */
public class SyncOperator extends Operator{

    //FIXME: add time out timer

    public SyncOperator(TaskSharedData taskSharedData, List<Task> taskQueue, OperatorReleaseCallback releaseCallback) {
        super(taskSharedData, taskQueue, releaseCallback);
    }

    @Override
    public void start() {
        super.start();
    }
}
