package net.kdt.pojavlaunch;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;

import androidx.annotation.Nullable;

/**
 * Keeps Discord Social SDK calls in the launcher's :launcher process.
 * GameActivity runs in :game, so it sends state changes to this service.
 */
public final class DiscordPresenceService extends Service {
    public static final String ACTION_BROWSING = "net.kdt.pojavlaunch.discord.BROWSING";
    public static final String ACTION_PLAYING = "net.kdt.pojavlaunch.discord.PLAYING";
    public static final String ACTION_CLEAR = "net.kdt.pojavlaunch.discord.CLEAR";
    private static final String EXTRA_VERSION = "minecraft_version";
    private static final long CALLBACK_INTERVAL_MS = 50L;

    private final Handler handler = new Handler(Looper.getMainLooper());
    private final Runnable callbackPump = new Runnable() {
        @Override
        public void run() {
            nativeRunCallbacks();
            handler.postDelayed(this, CALLBACK_INTERVAL_MS);
        }
    };

    public static void update(Context context, String action, @Nullable String version) {
        Intent intent = new Intent(context, DiscordPresenceService.class);
        intent.setAction(action);
        if (version != null) intent.putExtra(EXTRA_VERSION, version);
        context.startService(intent);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        handler.post(callbackPump);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent == null || intent.getAction() == null) return START_NOT_STICKY;

        switch (intent.getAction()) {
            case ACTION_BROWSING:
                nativeUpdatePresence("Browsing CryonixLauncher V4", "Launcher");
                break;
            case ACTION_PLAYING:
                String version = intent.getStringExtra(EXTRA_VERSION);
                nativeUpdatePresence("Playing CryonixLauncher V4",
                        version == null || version.trim().isEmpty()
                                ? "Minecraft" : "Minecraft " + version.trim());
                break;
            case ACTION_CLEAR:
                nativeClearPresence();
                break;
            default:
                break;
        }
        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        handler.removeCallbacks(callbackPump);
        nativeClearPresence();
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private static native void nativeUpdatePresence(String details, String state);
    private static native void nativeClearPresence();
    private static native void nativeRunCallbacks();

    static {
        System.loadLibrary("discord_presence");
    }
}
