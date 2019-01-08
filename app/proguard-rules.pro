# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

#common
-renamesourcefileattribute SourceFile
-keepattributes SourceFile,LineNumberTable,*Annotation*,Signature,Exceptions
-keepclassmembers class * implements android.os.Parcelable {
  public static final android.os.Parcelable$Creator CREATOR;
}

-keepclassmembers class * implements java.io.Serializable {
    static final long serialVersionUID;
    private static final java.io.ObjectStreamField[] serialPersistentFields;
    private void writeObject(java.io.ObjectOutputStream);
    private void readObject(java.io.ObjectInputStream);
    java.lang.Object writeReplace();
    java.lang.Object readResolve();
}
-keep class com.xgame.common.api.IProtocol
-keep class * implements com.xgame.common.api.IProtocol{
    *;
}

#for glide
-keep public class * implements com.bumptech.glide.module.GlideModule
-keep public class * extends com.bumptech.glide.module.AppGlideModule
-keep public enum com.bumptech.glide.load.resource.bitmap.ImageHeaderParser$** {
  **[] $VALUES;
  public *;
}

#for okhttp
-dontwarn okhttp3.**
-dontwarn okio.**
-dontwarn javax.annotation.**
# A resource is loaded with a relative path so the package of this class must be preserved.
-keepnames class okhttp3.internal.publicsuffix.PublicSuffixDatabase

#for retrofit
# Retain service method parameters.
-dontnote retrofit2.Platform
-dontwarn retrofit2.Platform$Java8
-keepclassmembernames interface * {
    @retrofit2.http.* <methods>;
}
# Ignore annotation used for build tooling.
-dontwarn org.codehaus.mojo.animal_sniffer.IgnoreJRERequirement

#for eventbus
-keepclassmembers class ** {
    @org.greenrobot.eventbus.Subscribe <methods>;
}
-keep enum org.greenrobot.eventbus.ThreadMode { *; }
# Only required if you use AsyncExecutor
-keepclassmembers class * extends org.greenrobot.eventbus.util.ThrowableFailureEvent {
    <init>(java.lang.Throwable);
}

#for BaiLu sdk
-keep class org.egret.runtime.component.alert.EgretAlertDialog {
   public static void showAlert(java.lang.String);
}

-keep class org.egret.runtime.component.FPSBoard.FPSBoard {
   public static void sendFpsLog(java.lang.String, int);
   public static void onNativeRenderInitialized();
   public static void update(int, int, int);
   public static void updateError();
}

-keep class org.egret.runtime.component.websocket.JniShell {
   public static void websocket_open(java.lang.Object, int, java.lang.String);
   public static void websocket_send(java.lang.Object, int, java.lang.String);
   public static void websocket_send(java.lang.Object, int, byte[]);
   public static void websocket_close(java.lang.Object, int);
   public static void websocket_dispose(java.lang.Object);
}

-keep class org.egret.runtime.component.device.DeviceInfo {
   public static float getDevicePixelRatio();
}

-keep class org.egret.runtime.component.device.MemoryUsageInfo {
   public static boolean lowMemory();
}

-keep class org.egret.runtime.component.file.LocalStorage {
   public static void setItem(java.lang.String, java.lang.String);
   public static java.lang.String getItem(java.lang.String);
   public static void removeItem(java.lang.String);
   public static void clear();
}

-keep class org.egret.runtime.component.inputBox.InputBoxOperation {
   public static void enterEditing(java.lang.String, float, float, float, float, boolean);
   public static void exitEditing();
   public static void updateConfig(byte[]);
}

-keep class org.egret.runtime.component.externalInterface.ExternalInterface {
   public static void callNativeFunction(java.lang.String, java.lang.String);
}

-keep class org.egret.runtime.component.label.TextBitmap {
   public <init>();
   public void init(int, int, boolean, boolean, java.lang.String);
   public void generateTextBitmapData(byte[], int, int);
   public int getTextWidth(byte[]);
}

-keep class org.egret.launcher.versioncontroller1_0.VersionController {
    public static void httpPost(java.lang.String, java.lang.String, boolean);
    public static java.lang.String gzipString(java.lang.String, java.lang.String);
    public static java.lang.String getScreenSize();
    public static java.lang.String getRAMMemory();
}

-keep class org.egret.launcher.versioncontroller1_0.RSAUtils {
    public static java.lang.String encryptBase64DataWithPublicKey(java.lang.String, java.lang.String);
}

# Common Data
-keep interface com.xgame.account.model.Data {*;}


-dontwarn com.fasterxml.jackson.databind.**

-dontwarn java.lang.ClassValue
-dontwarn com.google.common.**
-dontwarn com.xiaomi.push.**
-keep class com.xgame.push.XGameMessageReceiver {*;}

#wechat
-keep class com.tencent.mm.opensdk.** {
   *;
}
-keep class com.tencent.wxop.** {
   *;
}
-keep class com.tencent.mm.sdk.** {
   *;
}

# umeng
-keep class com.umeng.commonsdk.** {*;}
-keepclassmembers class * {
   public <init> (org.json.JSONObject);
}
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}
-dontwarn com.umeng.**
-keep class com.umeng.error.UMError{ *; }

-keep class com.umeng.error.UMErrorCatch{ *; }

-keep class com.umeng.error.UMErrorDataManger{ *; }

-keep class com.umeng.error.BatteryUtils{ *; }

#baserecycleviewadapter
-keep class com.chad.library.adapter.** {
*;
}
-keep public class * extends com.chad.library.adapter.base.BaseQuickAdapter
-keep public class * extends com.chad.library.adapter.base.BaseViewHolder
-keepclassmembers  class **$** extends com.chad.library.adapter.base.BaseViewHolder {
     <init>(...);
}

#-----zenus-mario-sdk ---- begin---
-keep class com.miui.zeus.**{ *;}

-keep class com.xiaomi.analytics.**{
    *;
}

-keep class com.miui.analytics.*{
    *;
}

-keep class oauth.signpost.**{
    *;
}

-keepclassmembernames class * {
    java.lang.Class class$(java.lang.String);
    java.lang.Class class$(java.lang.String, boolean);
}

-keepclasseswithmembernames class * {
    native <methods>;
}

-keepclassmembers,allowoptimization enum * {
    public static final ** *;
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

-keepclassmembers class * implements java.io.Serializable {
    static final long serialVersionUID;
    private static final java.io.ObjectStreamField[] serialPersistentFields;
    private void writeObject(java.io.ObjectOutputStream);
    private void readObject(java.io.ObjectInputStream);
    java.lang.Object writeReplace();
    java.lang.Object readResolve();
}

-keep class * implements android.os.Parcelable {
    public static final android.os.Parcelable$Creator *;
    private ** *;
    public ** *();
}

-keep class * extends android.os.IInterface{
    *;
}

-keep class * implements android.os.Parcelable$Creator

# umeng
-keep public class com.miui.zeus.mario.** {*;}
-keep public class com.umeng.** {*;}
-keep public class com.google.zxing.** {*;}
-keep public class cn.shuzilm.core.** {*;}
-dontwarn com.google.zxing.**

# do not remove any classes
#-dontshrink
-dontwarn oauth.signpost.**
-dontwarn com.xiaomi.analytics.**
-dontwarn com.umeng.socialize.shareboard.**
#-----zenus-mario-sdk ---- end---