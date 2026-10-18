#define DISCORDPP_IMPLEMENTATION
#include <discordpp.h>

#include <jni.h>
#include <android/log.h>
#include <memory>
#include <string>
#include <utility>

namespace {
constexpr uint64_t kApplicationId = 1557412253131743232ULL;
constexpr const char* kLogTag = "CryonixDiscordPresence";
std::unique_ptr<discordpp::Client> g_client;

std::string fromJavaString(JNIEnv* env, jstring value) {
    if (value == nullptr) return {};
    const char* chars = env->GetStringUTFChars(value, nullptr);
    if (chars == nullptr) return {};
    std::string result(chars);
    env->ReleaseStringUTFChars(value, chars);
    return result;
}

discordpp::Client* getClient() {
    if (!g_client) {
        g_client = std::make_unique<discordpp::Client>();
        g_client->SetApplicationId(kApplicationId);
    }
    return g_client.get();
}
}

extern "C" JNIEXPORT void JNICALL
Java_net_kdt_pojavlaunch_DiscordPresenceService_nativeUpdatePresence(
        JNIEnv* env, jclass, jstring details, jstring state) {
    const std::string detailsText = fromJavaString(env, details);
    // Reset the previous activity before starting a new browsing session so
    // Discord cannot retain the elapsed-time timestamp from the old game session.
    if (detailsText == "Browsing CryonixLauncher") {
        if (g_client) {
            g_client->ClearRichPresence();
            discordpp::RunCallbacks();
        }
    }
    discordpp::Activity activity{};
    // Explicitly override the activity's displayed title instead of relying on
    // the SDK's default application-name resolution (which was showing "Game").
    activity.SetName("CryonixLauncher");
    activity.SetDetails(detailsText);
    activity.SetState(fromJavaString(env, state));

    getClient()->UpdateRichPresence(std::move(activity), [](discordpp::ClientResult result) {
        if (!result.Successful()) {
            const std::string error = result.ToString();
            __android_log_print(ANDROID_LOG_WARN, kLogTag,
                                "Rich Presence update failed: %s", error.c_str());
        } else {
            __android_log_print(ANDROID_LOG_DEBUG, kLogTag, "Rich Presence updated");
        }
    });
}

extern "C" JNIEXPORT void JNICALL
Java_net_kdt_pojavlaunch_DiscordPresenceService_nativeClearPresence(JNIEnv*, jclass) {
    if (g_client) g_client->ClearRichPresence();
}

extern "C" JNIEXPORT void JNICALL
Java_net_kdt_pojavlaunch_DiscordPresenceService_nativeRunCallbacks(JNIEnv*, jclass) {
    discordpp::RunCallbacks();
}
