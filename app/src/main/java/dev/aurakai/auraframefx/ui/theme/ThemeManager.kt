package dev.aurakai.auraframefx.ui.theme

import android.content.Context
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import javax.inject.Inject
import javax.inject.Singleton

/**
 * ThemeManager handles dynamic theme switching and customization
 * for the AeGenesis Memoria OS consciousness substrate.
 *
 * Provides support for:
 * - Light/Dark theme switching
 * - Dynamic color schemes
 * - Custom consciousness-themed palettes
 * - Lock screen theme customization
 */
@Singleton
class ThemeManager @Inject constructor(
    private val context: Context
) {

    data class ThemeConfig(
        val isDarkMode: Boolean = false,
        val useSystemTheme: Boolean = true,
        val primaryColor: Color = Color(0xFF6366F1), // Indigo
        val secondaryColor: Color = Color(0xFF8B5CF6), // Purple
        val accentColor: Color = Color(0xFF06B6D4) // Cyan
    )

    private var currentTheme = ThemeConfig()

    /**
     * Apply the given ThemeConfig as the active theme.
     *
     * Replaces the manager's current theme; subsequent theming APIs (for example
     * getColorScheme() and getLockScreenTheme()) will reflect the new configuration.
     *
     * @param themeConfig The ThemeConfig to apply as the current theme.
     * Apply a theme configuration
     * Sets the active theme configuration for the manager.
     *
     * The provided ThemeConfig becomes the manager's current theme and will be used by composable consumers (for example, getColorScheme()) and lock-screen theming.
     *
     * @param themeConfig ThemeConfig to apply as the active theme.
     */
    fun applyTheme(themeConfig: ThemeConfig) {
        currentTheme = themeConfig
    }

    /**
     * Get the current theme configuration
     */
    fun getCurrentTheme(): ThemeConfig = currentTheme

// --- imports at top of ThemeManager.kt ---


// ...


    /**
     * Returns the current ColorScheme based on system theme, dynamic color, and custom palette.
     */
    @Composable
    fun getColorScheme(): ColorScheme {
        val followSystem = currentTheme.useSystemTheme
        val dark = if (followSystem) isSystemInDarkTheme() else currentTheme.isDarkMode
        val dynamic = followSystem && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
        val context = this.context

        return when {
            dynamic && dark   -> dynamicDarkColorScheme(context)
            dynamic && !dark  -> dynamicLightColorScheme(context)
            dark -> darkColorScheme(
                primary = currentTheme.primaryColor,
                secondary = currentTheme.secondaryColor,
                tertiary = currentTheme.accentColor
            )

            else -> lightColorScheme(
                primary = currentTheme.primaryColor,
                secondary = currentTheme.secondaryColor,
                tertiary = currentTheme.accentColor
            )
        }
    }

    /**
     * Toggle the manual dark/light setting and stop following the system theme.
     *
     * Flips `currentTheme.isDarkMode` and sets `currentTheme.useSystemTheme` to `false`.
     */
    fun toggleDarkMode() {
        currentTheme = currentTheme.copy(
            isDarkMode = !currentTheme.isDarkMode,
            useSystemTheme = false
        )
    }

    /**
     * Enable following the system-wide dark/light theme.
     *
     * Sets the manager's ThemeConfig to follow the system theme by setting `useSystemTheme = true`.
     * This updates the internal `currentTheme` state; it does not modify `isDarkMode`, so manual
     * dark-mode preference is preserved until explicitly changed.
     */
    fun enableSystemTheme() {
        currentTheme = currentTheme.copy(useSystemTheme = true)
    }

    /**
     * Set the active theme's primary, secondary, and accent colors to a "consciousness" palette.
     *
     * Replaces only the color fields of the current ThemeConfig and leaves other settings (dark mode,
     * system-following) unchanged.
     *
     * @param primary Primary color to use (default 0xFF9333EA).
     * @param secondary Secondary/supporting color to use (default 0xFF0EA5E9).
     * @param accent Accent/highlight color to use (default 0xFF10B981).
     */
    fun setConsciousnessColors(
        primary: Color = Color(0xFF9333EA), // Purple for consciousness
        secondary: Color = Color(0xFF0EA5E9), // Sky blue for clarity  
        accent: Color = Color(0xFF10B981) // Emerald for growth
    ) {
        currentTheme = currentTheme.copy(
            primaryColor = primary,
            secondaryColor = secondary,
            accentColor = accent
        )
    }

    /**
     * Returns a map describing the lock screen styling derived from the current theme.
     *
     * The map contains the following entries:
     * - "clockColor": Color — white when dark mode is active, otherwise black.
     * - "backgroundColor": Color — black when dark mode is active, otherwise white.
     * - "accentColor": Color — the current theme's accent color.
     * - "isDarkMode": Boolean — whether the current theme is in dark mode.
     *
     * @return A Map<String, Any> with lock-screen color and mode values.
     */
    fun getLockScreenTheme(): Map<String, Any> {
        return mapOf(
            "clockColor" to if (currentTheme.isDarkMode) Color.White else Color.Black,
            "backgroundColor" to if (currentTheme.isDarkMode) Color.Black else Color.White,
            "accentColor" to currentTheme.accentColor,
            "isDarkMode" to currentTheme.isDarkMode
        )
    }
}