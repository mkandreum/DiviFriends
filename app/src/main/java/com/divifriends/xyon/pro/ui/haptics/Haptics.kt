package com.divifriends.xyon.pro.ui.haptics

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.os.SystemClock
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.view.HapticFeedbackConstants
import android.view.View
import androidx.compose.runtime.staticCompositionLocalOf

/**
 * Premium haptic engine.
 *
 * Everyday taps, ticks and toggles go through the system's own haptic
 * constants on the root [view]: the OEM tunes those per device and they honour
 * the system "touch feedback" setting. Big moments (success, celebrate, error…)
 * use our own vibration primitives (API 30+), with envelope effects on API 36+,
 * falling back to amplitude-controlled one-shots on older devices. Every call
 * is a no-op when the device has no vibrator or the user switched haptics off,
 * so callers never need to guard.
 */
class DiviHaptics(context: Context) {

    private val vibrator: Vibrator? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        (context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager)?.defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
    }

    private val hasVibrator: Boolean = vibrator?.hasVibrator() == true

    private val hasAmplitude: Boolean = vibrator?.hasAmplitudeControl() == true

    private val hasPrimitives: Boolean
        get() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && hasVibrator

    /** User preference (Ajustes → Vibración). */
    var userEnabled: Boolean = true

    /** Root view used for system haptic constants; set by the app root. */
    var view: View? = null

    private val active: Boolean get() = hasVibrator && userEnabled

    /** Last time a scrub tick fired, so fast drags don't turn into a buzz. */
    private var lastTickAt = 0L

    /** Ultra-light tick for taps, list scroll snaps and selections. */
    fun selection() = play {
        system(HapticFeedbackConstants.SEGMENT_TICK, Build.VERSION_CODES.R)
            ?: primitive(VibrationEffect.Composition.PRIMITIVE_LOW_TICK, 0.55f) ?: oneShot(6, 70)
    }

    /** Detent tick while scrubbing a chart or dragging a slider. Rate-limited. */
    fun tick() {
        val now = SystemClock.uptimeMillis()
        if (now - lastTickAt < 28) return
        lastTickAt = now
        play {
            system(HapticFeedbackConstants.SEGMENT_FREQUENT_TICK, Build.VERSION_CODES.R)
                ?: primitive(VibrationEffect.Composition.PRIMITIVE_TICK, 0.45f) ?: oneShot(5, 60)
        }
    }

    /** Crisp tick for toggles and segmented controls. */
    fun toggle() = play {
        system(HapticFeedbackConstants.TOGGLE_ON, Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
            ?: primitive(VibrationEffect.Composition.PRIMITIVE_TICK, 0.85f) ?: oneShot(9, 110)
    }

    /** Satisfying press used by primary buttons. */
    fun press() = play {
        system(HapticFeedbackConstants.VIRTUAL_KEY, Build.VERSION_CODES.R)
            ?: primitive(VibrationEffect.Composition.PRIMITIVE_CLICK, 0.7f) ?: oneShot(14, 130)
    }

    /** Swipe crossed its commit threshold. */
    fun threshold() = play {
        system(HapticFeedbackConstants.GESTURE_THRESHOLD_ACTIVATE, Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
            ?: primitive(VibrationEffect.Composition.PRIMITIVE_CLICK, 1f) ?: oneShot(16, 180)
    }

    /**
     * Swell that grows while a card expands into its screen: a smooth envelope
     * on API 36+, a quick rise + click on API 30+, a soft double pulse before.
     */
    fun swell() = play {
        if (envelope()) return@play
        if (compose(
                VibrationEffect.Composition.PRIMITIVE_QUICK_RISE to 0.45f,
                VibrationEffect.Composition.PRIMITIVE_LOW_TICK to 0.8f
            )
        ) return@play
        pattern(longArrayOf(0, 12, 30, 26), intArrayOf(0, 70, 0, 150))
    }

    /** Heavier, deliberate feedback (delete, destructive confirm). */
    fun heavy() = play { primitive(VibrationEffect.Composition.PRIMITIVE_THUD, 0.9f) ?: oneShot(24, 210) }

    /** Success pattern for confirmations and settlings: a rising swell + click. */
    fun success() = play {
        if (compose(
                VibrationEffect.Composition.PRIMITIVE_QUICK_RISE to 0.6f,
                VibrationEffect.Composition.PRIMITIVE_CLICK to 1.0f
            )
        ) return@play
        if (compose(
                VibrationEffect.Composition.PRIMITIVE_TICK to 0.7f,
                VibrationEffect.Composition.PRIMITIVE_CLICK to 1.0f
            )
        ) return@play
        pattern(longArrayOf(0, 22, 34, 42), intArrayOf(0, 120, 0, 190))
    }

    /** Celebration for big moments (group created, everyone settled). */
    fun celebrate() = play {
        if (compose(
                VibrationEffect.Composition.PRIMITIVE_SPIN to 0.5f,
                VibrationEffect.Composition.PRIMITIVE_CLICK to 0.8f,
                VibrationEffect.Composition.PRIMITIVE_CLICK to 1.0f
            )
        ) return@play
        pattern(longArrayOf(0, 18, 40, 18, 40, 36), intArrayOf(0, 110, 0, 150, 0, 220))
    }

    /** Warning pattern for reversible-but-important actions. */
    fun warning() = play {
        if (compose(
                VibrationEffect.Composition.PRIMITIVE_TICK to 0.8f,
                VibrationEffect.Composition.PRIMITIVE_LOW_TICK to 1.0f
            )
        ) return@play
        pattern(longArrayOf(0, 28, 56, 34), intArrayOf(0, 170, 0, 120))
    }

    /** Error pattern for validation failures. */
    fun error() = play {
        if (compose(
                VibrationEffect.Composition.PRIMITIVE_LOW_TICK to 1.0f,
                VibrationEffect.Composition.PRIMITIVE_LOW_TICK to 1.0f,
                VibrationEffect.Composition.PRIMITIVE_THUD to 0.8f
            )
        ) return@play
        pattern(longArrayOf(0, 34, 46, 34, 46, 54), intArrayOf(0, 210, 0, 210, 0, 200))
    }

    /* ------------------------------------------------------------------ */

    private inline fun play(block: () -> Unit) {
        if (!active) return
        runCatching { block() }
    }

    /**
     * Plays a system haptic constant on [view] when the OS is new enough.
     * Returns null (so callers fall back) only when it could not be attempted;
     * a constant the user silenced in system settings still counts as handled.
     */
    private fun system(constant: Int, minSdk: Int): Unit? {
        val v = view ?: return null
        if (Build.VERSION.SDK_INT < minSdk) return null
        v.performHapticFeedback(constant)
        return Unit
    }

    /** Rising then fading envelope (~320 ms); false when unsupported. */
    @SuppressLint("NewApi")
    private fun envelope(): Boolean {
        val v = vibrator ?: return false
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.BAKLAVA) return false
        if (!v.areEnvelopeEffectsSupported()) return false
        v.vibrate(
            VibrationEffect.BasicEnvelopeBuilder()
                .setInitialSharpness(0.3f)
                .addControlPoint(0.55f, 0.4f, 180)
                .addControlPoint(0.85f, 0.7f, 60)
                .addControlPoint(0f, 0.5f, 80)
                .build()
        )
        return true
    }

    @SuppressLint("NewApi")
    private fun primitive(primitiveId: Int, scale: Float): Unit? {
        val v = vibrator
        if (v == null || !hasPrimitives) return null
        if (!v.areAllPrimitivesSupported(primitiveId)) return null
        v.vibrate(
            VibrationEffect.startComposition()
                .addPrimitive(primitiveId, scale.coerceIn(0f, 1f))
                .compose()
        )
        return Unit
    }

    /** Plays a composed multi-primitive pattern when supported; false otherwise. */
    @SuppressLint("NewApi", "WrongConstant")
    private fun compose(vararg steps: Pair<Int, Float>): Boolean {
        val v = vibrator ?: return false
        if (!hasPrimitives) return false
        val ids = steps.map { it.first }.toIntArray()
        if (!v.areAllPrimitivesSupported(*ids)) return false
        val composition = VibrationEffect.startComposition()
        steps.forEachIndexed { index, (id, scale) ->
            if (index == 0) composition.addPrimitive(id, scale.coerceIn(0f, 1f))
            else composition.addPrimitive(id, scale.coerceIn(0f, 1f), 34)
        }
        v.vibrate(composition.compose())
        return true
    }

    private fun oneShot(durationMs: Long, amplitude: Int) {
        val v = vibrator ?: return
        val amp = if (hasAmplitude) amplitude.coerceIn(1, 255) else VibrationEffect.DEFAULT_AMPLITUDE
        v.vibrate(VibrationEffect.createOneShot(durationMs, amp))
    }

    private fun pattern(timings: LongArray, amplitudes: IntArray) {
        val v = vibrator ?: return
        if (hasAmplitude) v.vibrate(VibrationEffect.createWaveform(timings, amplitudes, -1))
        else v.vibrate(VibrationEffect.createWaveform(timings, -1))
    }
}

val LocalHaptics = staticCompositionLocalOf<DiviHaptics?> { null }
