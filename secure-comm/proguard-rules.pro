# Genesis Protocol - Secure Communication Module
-keep class dev.aurakai.auraframefx.securecomm.** { *; }

# Crypto and security classes
-keep class javax.crypto.** { *; }
-keep class java.security.** { *; }
-keep class org.bouncycastle.** { *; }

# Kotlin serialization
-keepclassmembers class **$$serializer {
    *** serializer(...);
}
-keepclasseswithmembers class * {
    *** Companion;
}
-keepclassmembers class * {
    *** Companion;
}

# Hilt
-keepclasseswithmembers class * {
    @dagger.hilt.** <methods>;
}

# Remove debug logging
-assumenosideeffects class android.util.Log {
    public static *** d(...);
    public static *** v(...);
    public static *** i(...);
}
