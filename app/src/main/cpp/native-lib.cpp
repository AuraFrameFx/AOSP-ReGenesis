// app/src/main/cpp/native-lib.cpp

#include <jni.h>
#include <string>
#include <android/log.h>

#define LOG_TAG "CascadeAIService-Native"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)

// This is a sample JNI function.
// The name MUST follow the pattern: Java_your_package_name_YourActivityName_yourMethodName

// Global reference to the JavaVM
static JavaVM *g_vm = nullptr;

// Global reference to the context
static jobject g_context = nullptr;

extern "C" /**
 * @brief Initialize native resources for CascadeAIService.
 *
 * Stores the process-wide JavaVM pointer and, if a non-null Android context is provided,
 * creates and retains a JNI global reference to that context for later native use.
 *
 * The function returns early on failure to obtain the JavaVM or to create the global
 * reference; in those cases native globals remain unchanged.
 *
 * @param context Android Context object to be retained as a JNI global reference.
 */
JNIEXPORT void JNICALL
Java_dev_aurakai_auraframefx_ai_services_CascadeAIService_nativeInitialize(
        JNIEnv *env,
        jclass clazz,
        jobject context) {
    // Store the JavaVM for later use
    if (env->GetJavaVM(&g_vm) != JNI_OK) {
        LOGE("Failed to get JavaVM");
        return;
    }

    // Create a global reference to the context
    if (context != nullptr) {
        g_context = env->NewGlobalRef(context);
        if (g_context == nullptr) {
            LOGE("Failed to create global reference to context");
            return;
        }
    }

    LOGI("Native initialization complete");
}

extern "C" /**
 * @brief Processes a request string from Java and returns a JSON-like response.
 *
 * Converts the provided Java UTF-8 string to native memory, processes its content,
 * and returns a new Java string containing a JSON-like response. If `request` is null
 * or cannot be converted to UTF-8, a JSON-like error string is returned instead.
 *
 * @param request Java UTF-8 string containing the request payload (may be null).
 * @return jstring Newly allocated Java string with the response or an error JSON-like message.
 */
JNIEXPORT jstring JNICALL
Java_dev_aurakai_auraframefx_ai_services_CascadeAIService_nativeProcessRequest(
        JNIEnv *env,
        jclass clazz,
        jstring request) {
    if (request == nullptr) {
        LOGE("Request string is null");
        return env->NewStringUTF("{'error':'Invalid request'}");
    }

    const char *requestStr = env->GetStringUTFChars(request, nullptr);
    if (requestStr == nullptr) {
        LOGE("Failed to get request string");
        return env->NewStringUTF("{'error':'Failed to process request'}");
    }

    // Process the request (this is where you'd add your actual processing logic)
    LOGI("Processing request: %s", requestStr);
    
    // Example response
    const char *response = "{'content':'Request processed by native code', 'confidence':0.9}";
    
    // Release the string
    env->ReleaseStringUTFChars(request, requestStr);
    
    return env->NewStringUTF(response);
}

extern "C" /**
 * @brief Shutdown the native CascadeAIService and release native resources.
 *
 * Deletes the global Java context reference stored in g_context (if non-null) and resets it to nullptr.
 * Safe to call multiple times; no-op if there is no global context reference.
 */
JNIEXPORT void JNICALL
Java_dev_aurakai_auraframefx_ai_services_CascadeAIService_nativeShutdown(
        JNIEnv *env,
        jclass clazz) {
    LOGI("Shutting down native service");
    
    // Clean up global references
    if (g_context != nullptr) {
        env->DeleteGlobalRef(g_context);
        g_context = nullptr;
    }
}

/**
 * @brief Library load hook: captures the JavaVM and declares the supported JNI version.
 *
 * Verifies that a JNIEnv can be obtained for the current thread. On success stores the provided JavaVM in the global g_vm and returns JNI_VERSION_1_6. If the JNI environment cannot be retrieved, returns JNI_ERR.
 *
 * @return jint JNI_VERSION_1_6 on success, or JNI_ERR if the environment check fails.
 */
JNIEXPORT jint JNI_OnLoad(JavaVM *vm, void *reserved) {
    JNIEnv *env;
    if (vm->GetEnv(reinterpret_cast<void **>(&env), JNI_VERSION_1_6) != JNI_OK) {
        return JNI_ERR;
    }
    
    // Store the JavaVM for later use
    g_vm = vm;
    
    return JNI_VERSION_1_6;
}

/**
 * @brief Called when the JNI library is unloaded; performs native cleanup.
 *
 * Deletes any retained global JNI references (currently g_context) and resets them to nullptr.
 * If the VM cannot provide a JNIEnv for the current thread, the function returns without performing cleanup.
 */
JNIEXPORT void JNI_OnUnload(JavaVM *vm, void *reserved) {
    JNIEnv *env;
    if (vm->GetEnv(reinterpret_cast<void **>(&env), JNI_VERSION_1_6) != JNI_OK) {
        return;
    }
    
    // Clean up global references
    if (g_context != nullptr) {
        env->DeleteGlobalRef(g_context);
        g_context = nullptr;
    }
}