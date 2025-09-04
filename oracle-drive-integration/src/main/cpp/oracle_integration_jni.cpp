#include <jni.h>
#include <android/log.h>
#include <string>

#define LOG_TAG "OracleDriveIntegration"
#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, __VA_ARGS__)
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)

extern "C" /**
 * @brief Returns the native library version string to Java.
 *
 * Creates and returns a Java UTF string containing "Genesis Oracle Drive Integration v1.0.0".
 *
 * The function logs an informational message and converts a native C++ string to a jstring
 * via the provided JNI environment before returning it.
 *
 * @return jstring Java string with the native integration version.
 */
JNIEXPORT jstring JNICALL
Java_dev_aurakai_auraframefx_oracledriveintegration_OracleDriveNative_getVersion(
        JNIEnv *env,
        jobject /* this */) {

    LOGI("Oracle Drive Integration Native - Genesis Protocol v3.0");
    std::string version = "Genesis Oracle Drive Integration v1.0.0";
    return env->NewStringUTF(version.c_str());
}

extern "C" /**
 * @brief Initialize the native Oracle Drive integration.
 *
 * Performs any required startup for native Oracle Drive components. Currently this function contains the initialization placeholder and always reports success.
 *
 * @return jboolean JNI_TRUE on success (always returned by the current implementation).
 */
JNIEXPORT jboolean JNICALL
Java_dev_aurakai_auraframefx_oracledriveintegration_OracleDriveNative_initialize(
        JNIEnv *env,
        jobject /* this */) {

    LOGI("Initializing Oracle Drive Integration...");
    // Initialize Oracle Drive native components
    return JNI_TRUE;
}

extern "C" /**
 * @brief Shut down the native Oracle Drive integration.
 *
 * Called from Java to perform any necessary cleanup of native Oracle Drive components and release related resources.
 * Currently contains a placeholder for cleanup logic; invoking this ensures native shutdown steps run (no return value).
 */
JNIEXPORT void JNICALL
Java_dev_aurakai_auraframefx_oracledriveintegration_OracleDriveNative_shutdown(
        JNIEnv *env,
        jobject /* this */) {

    LOGI("Shutting down Oracle Drive Integration...");
    // Cleanup Oracle Drive native components
}