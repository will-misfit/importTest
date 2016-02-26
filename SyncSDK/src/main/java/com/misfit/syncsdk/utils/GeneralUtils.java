package com.misfit.syncsdk.utils;

import com.misfit.syncsdk.log.LogEvent;
import com.misfit.syncsdk.log.LogEventType;

import java.util.Locale;
import java.util.Random;

/**
 * some general utility methods
 */
public class GeneralUtils {

    private Random mRandom;

    private static GeneralUtils mGeneralUtils;

    private static final String languagesSupport = "en,ar,de,es,fr,he,it,iw,ja,ko,ms,pt,ru,th,tr,zh";

    private GeneralUtils() {
        mRandom = new Random();
    }

    public static GeneralUtils getInstance() {
        if (mGeneralUtils == null) {
            mGeneralUtils = new GeneralUtils();
        }
        return mGeneralUtils;
    }

    public static LogEvent createLogEvent(int eventId) {
        String eventName = null;
        if (eventId >= 0 && eventId < LogEventType.LogEventNames.length) {
            eventName = LogEventType.LogEventNames[eventId];
        }
        return new LogEvent(eventId, eventName);
    }

    public int randomInt() {
        return mRandom.nextInt();
    }

    /*
     * .prometheus.app.PrometheusConfig#reloadCurrentLocal()
     * */
    public static String reloadCurrentLocale () {
        String localLanguage = Locale.getDefault().getLanguage().toLowerCase(Locale.getDefault());
        if (!isSupported(localLanguage)) {
            localLanguage = "en";
        } else if (localLanguage.equals("zh")){
            String country = Locale.getDefault().getCountry().toLowerCase(Locale.getDefault());
            if (country.equals("cn")) {
                localLanguage = "zh-hans";
            } else if (country.equals("tw")) {
                localLanguage = "zh-hant";
            } else {
                localLanguage = "en";
            }
        }
        return localLanguage;
    }

    public static boolean isSupported(String localLanguage) {
        return languagesSupport.indexOf(localLanguage) >= 0;
    }
    
}
