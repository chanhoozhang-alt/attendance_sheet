# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in Android SDK tools.
# For more details, see
#   https://developer.android.com/build/shrink-code

# Keep Room entities
-keep class com.attendance.app.data.local.entity.** { *; }

# Keep Apache POI
-keep class org.apache.poi.** { *; }
-dontwarn org.apache.poi.**

# Keep Kotlinx DateTime
-keep class kotlinx.datetime.** { *; }
