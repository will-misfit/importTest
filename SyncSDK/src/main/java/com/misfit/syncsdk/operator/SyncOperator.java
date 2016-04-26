package com.misfit.syncsdk.operator;

import android.support.annotation.NonNull;

import com.misfit.syncsdk.callback.SyncOperationResultCallback;
import com.misfit.syncsdk.model.TaskSharedData;
import com.misfit.syncsdk.task.Task;

import java.util.List;

/**
 *
 */
public class SyncOperator extends Operator{

    //FIXME: add time out timer

    public SyncOperator(@NonNull TaskSharedData taskSharedData,
                        @NonNull List<Task> taskList,
                        @NonNull SyncOperationResultCallback resultCallback,
                        OperatorReleaseCallback releaseCallback) {
        super(taskSharedData, taskList, resultCallback, releaseCallback);
    }

    @Override
    public void start() {
        super.start();
    }
}
