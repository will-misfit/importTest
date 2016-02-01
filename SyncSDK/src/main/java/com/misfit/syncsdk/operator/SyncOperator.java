package com.misfit.syncsdk.operator;

import com.misfit.ble.shine.result.SyncResult;
import com.misfit.syncsdk.DeviceType;
import com.misfit.syncsdk.device.DeviceBehavior;
import com.misfit.syncsdk.model.TaskSharedData;
import com.misfit.syncsdk.task.SyncAndCalculateTask;
import com.misfit.syncsdk.task.Task;

import java.util.List;

/**
 * Created by Will-Hou on 1/12/16.
 */
public class SyncOperator extends Operator{

    //FIXME: add time out timer

    public SyncOperator(TaskSharedData taskSharedData, List<Task> taskQueue) {
        super(taskSharedData, taskQueue);
    }

    @Override
    public void start() {
        super.start();
    }
}
