apply(plugin = "genesis.android.library")

// Compose-specific configuration must be done in the consuming module, not in buildSrc convention plugin.
// Remove all references to 'android', 'dependencies', and 'libs'.
