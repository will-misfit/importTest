package com.misfit.ble;

import com.misfit.ble.shine.ShineConnectionParameters;
import com.misfit.ble.shine.controller.SetConnectionParametersPhaseController;
import com.misfit.ble.shine.core.Constants;
import com.misfit.ble.shine.request.SetConnectionParameterRequest;

import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Locale;

import static org.junit.Assert.assertEquals;

public class SetConnectionParameterTest {

    Method mBuildSetConnectionParametersRequestMethod;
    Field mCurrentParams;
    Field mAttemptsField;

    @Before
    public void setUp() throws Exception {
        mBuildSetConnectionParametersRequestMethod = SetConnectionParametersPhaseController.class.getDeclaredMethod("buildSetConnectionParamsRequest");
        mBuildSetConnectionParametersRequestMethod.setAccessible(true);
        mAttemptsField = SetConnectionParametersPhaseController.class.getDeclaredField("mNumOfSetActionAttempts");
        mAttemptsField.setAccessible(true);
        mCurrentParams = SetConnectionParametersPhaseController.class.getDeclaredField("mCurrConnectionParameters");
        mCurrentParams.setAccessible(true);
    }

    @Test
    public void testBuild() throws Exception {
        double expectedInterval = 7.5;
        int needAttempts = 2;
        double settleInterval = expectedInterval + Constants.CONNECTION_INTERVAL_STEP * needAttempts;
        ShineConnectionParameters connectionParameters = new ShineConnectionParameters(expectedInterval, 0, 720);
        SetConnectionParametersPhaseController controller = new SetConnectionParametersPhaseController(null, null, connectionParameters);
        mCurrentParams.set(controller, new ShineConnectionParameters(settleInterval, 0, 720));
        for (int i = 0; i < 6; i++) {
            mAttemptsField.set(controller, i);
            SetConnectionParameterRequest request = buildSetConnectionParametersRequest(controller);
            System.out.printf(String.format(Locale.getDefault(), "min=%f, max=%f, step=%f\n", request.getMinConnectionInterval(), request.getMaxConnectionInterval(), request.getMaxConnectionInterval() - request.getMinConnectionInterval()));
            assertEquals(request.getMaxConnectionInterval() - request.getMinConnectionInterval(), Constants.CONNECTION_INTERVAL_STEP - 0.01, 0.0000001f);
            if (i >= needAttempts) {
                assertEquals(true, settleInterval <= request.getMaxConnectionInterval() && settleInterval >= request.getMinConnectionInterval());
            } else {
                assertEquals(false, settleInterval <= request.getMaxConnectionInterval() && settleInterval >= request.getMinConnectionInterval());
            }
        }
    }


    private SetConnectionParameterRequest buildSetConnectionParametersRequest(SetConnectionParametersPhaseController controller) throws InvocationTargetException, IllegalAccessException {
        return (SetConnectionParameterRequest) mBuildSetConnectionParametersRequestMethod.invoke(controller, null);
    }
}
