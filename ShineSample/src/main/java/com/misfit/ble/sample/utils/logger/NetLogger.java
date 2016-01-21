package com.misfit.ble.sample.utils.logger;


/**
 * output via network
 */
public class NetLogger implements MFLog.LogNode {

    private final static int OUTPUT_PRIORITY = MFLog.WARN;

    @Override
    public void log(int priority, String tag, String content) {
        if (priority < OUTPUT_PRIORITY) {
            return;
        }
//        AVObject avObject = new AVObject("Bugs");
//        avObject.put("manufacturer", Build.MANUFACTURER);
//        avObject.put("product", Build.PRODUCT);
//        avObject.put("device", Build.DEVICE);
//        avObject.put("model", Build.MODEL);
//        avObject.put("display", Build.DISPLAY);
//        avObject.put("os", String.valueOf(Build.VERSION.SDK_INT));
//        avObject.put("pkgName", com.quxue.common.logger.MFLog.getApplicationId());
//        avObject.put("version", String.valueOf(com.quxue.common.logger.MFLog.getVersionCode()));
//        avObject.put("tag", tag);
//        avObject.put("detail", content);
//        avObject.saveInBackground();
    }
}
