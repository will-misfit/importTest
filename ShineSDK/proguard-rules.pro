# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in /Users/minh/Workspace/DevelopmentTools/android-sdk-macosx/tools/proguard/proguard-android.txt
# You can edit the include path and order by changing the proguardFiles
# directive in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Add any project specific keep options here:

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

#-libraryjars libs_reference/samsung_ble_sdk_200.jar

#Specifies to keep the parameter names and types of methods that are kept. It can be useful when processing a library.
#Some IDEs can use the information to assist developers who use the library, for example with tool tips or autocompletion.
-keepparameternames

# Reduce the size of the output some more.
-flattenpackagehierarchy 'com.misfit.ble.obfuscated'
#-allowaccessmodification

# Keep a fixed source file attribute and all line number tables to get line
# numbers in the stack traces.
# You can comment this out if you're not interested in stack traces.
-renamesourcefileattribute SourceFile
-keepattributes Exceptions,InnerClasses,Signature,Deprecated,SourceFile,LineNumberTable,*Annotation*,EnclosingMethod

# Preserve the required interface from the License Verification Library
# (but don't nag the developer if the library is not used at all).
-keep public interface com.android.vending.licensing.ILicensingService
-dontnote com.android.vending.licensing.ILicensingService
-keep public interface com.google.vending.licensing.ILicensingService
-dontnote com.google.vending.licensing.ILicensingService

# If you wish, you can let the optimization step remove Android logging calls.
-assumenosideeffects class android.util.Log {
#    public static boolean isLoggable(java.lang.String, int);
    public static int v(...);
    public static int d(...);
    public static int i(...);
#    public static int w(...);
    public static int e(...);
}

# Your application may contain more items that need to be preserved;
# typically classes that are dynamically created using Class.forName:

# -keep public class mypackage.MyClass
# -keep public interface mypackage.MyInterface
# -keep public class * implements mypackage.MyInterface

-keep class com.misfit.ble.shine.parser.TimestampCorrectorNew { public *; }

-keep class com.misfit.ble.shine.ShineAdapter { public *; }
-keep class com.misfit.ble.shine.ShineAdapter$ShineScanCallback { public *; }
-keep class com.misfit.ble.shine.ShineAdapter$ShineScanCallbackForTest { public *; }
-keep class com.misfit.ble.shine.ShineAdapter$ShineRetrieveCallback { public *; }

-keep class com.misfit.ble.shine.controller.ConfigurationSession { public *; }
-keep class com.misfit.ble.shine.ShineConfiguration { public *; }
-keep class com.misfit.ble.shine.ShineConnectionParameters { public *; }
-keep class com.misfit.ble.shine.ShineStreamingConfiguration { public *; }
-keep class com.misfit.ble.shine.ShineEventAnimationMapping { public *; }

-keep class com.misfit.ble.shine.ShineDevice { public *; }
-keep class com.misfit.ble.shine.ShineDevice$ShineHIDConnectionCallback { public *; }

-keep class com.misfit.ble.shine.ShineProfile { public *; }
-keep class com.misfit.ble.shine.ShineProfile$State { public *; }
-keep class com.misfit.ble.shine.ShineProfile$ConfigurationCallback { public *; }
-keep class com.misfit.ble.shine.ShineProfile$OTACallback { public *; }
-keep class com.misfit.ble.shine.ShineProfile$SyncCallback { public *; }
-keep class com.misfit.ble.shine.ShineProfile$StreamingCallback { public *; }
-keep class com.misfit.ble.shine.ShineProfile$ConnectionCallback { public *; }
-keep class com.misfit.ble.shine.ShineProfile$ConnectionCallbackForTest { public *; }

-keep public enum com.misfit.ble.shine.ShineProfile$ActionResult {
    **[] $VALUES;
    public *;
}

-keep public enum com.misfit.ble.shine.ActionID {
    **[] $VALUES;
    public *;
}

-keep public enum com.misfit.ble.shine.ShineProperty {
    **[] $VALUES;
    public *;
}

-keep class com.misfit.ble.shine.result.SyncResult { public *; }
-keep class com.misfit.ble.shine.result.Event { public *; }
-keep class com.misfit.ble.shine.result.TapEvent { public *; }
-keep class com.misfit.ble.shine.result.TapEventSummary { public *; }
-keep class com.misfit.ble.shine.result.SessionEvent { public *; }
-keep class com.misfit.ble.shine.result.Activity { public <fields>; }
-keep class com.misfit.ble.shine.result.UserInputEvent { public <fields>; }
-keep class com.misfit.ble.shine.result.SwimLap { public <fields>; }
-keep class com.misfit.ble.shine.result.SwimSession { public <fields>; }

-keep class com.misfit.ble.setting.SDKSetting { public *; }

-keep class com.misfit.ble.util.MutableBoolean { public *; }
-keep class com.misfit.ble.util.Convertor { public *; }

# Pluto Settings
-keep class com.misfit.ble.setting.pluto.InactivityNudgeSettings { public *; }
-keep class com.misfit.ble.setting.pluto.AlarmSettings { public *; }
-keep class com.misfit.ble.setting.pluto.NotificationsSettings { public *; }
-keep class com.misfit.ble.setting.pluto.GoalHitNotificationSettings { public *; }
-keep class com.misfit.ble.setting.pluto.PlutoSequence$LED { public *; }
-keep class com.misfit.ble.setting.pluto.PlutoSequence$Vibe { public *; }
-keep class com.misfit.ble.setting.pluto.PlutoSequence$Sound { public *; }

# Log, ConnectionErrorCode enum
-keep class com.misfit.ble.shine.log.ConnectFailCode { public *; }

# Flashlink Settings
-keep public enum com.misfit.ble.setting.flashlink.CustomModeEnum$ActionType {
    **[] $VALUES;
    public *;
}

-keep public enum com.misfit.ble.setting.flashlink.CustomModeEnum$MemEventNumber {
    **[] $VALUES;
    public *;
}

-keep public enum com.misfit.ble.setting.flashlink.CustomModeEnum$AnimNumber {
    **[] $VALUES;
    public *;
}

-keep public enum com.misfit.ble.setting.flashlink.CustomModeEnum$KeyCode {
    **[] $VALUES;
    public *;
}

-keep public enum com.misfit.ble.setting.flashlink.FlashButtonMode {
    **[] $VALUES;
    public *;
}

-keep class com.misfit.ble.shine.core.DeviceTransparentCommand { public *; }
-keep class com.misfit.ble.shine.core.DeviceTransparentCommand$Animation { public *; }
