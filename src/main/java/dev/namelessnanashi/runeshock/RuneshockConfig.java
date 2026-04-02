package dev.namelessnanashi.runeshock;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.ConfigSection;
import net.runelite.client.config.Range;
import net.runelite.client.config.Units;

@ConfigGroup(RuneshockConfig.GROUP)
public interface RuneshockConfig extends Config
{
	String GROUP = "runeshock";
	String ENABLE_WARNING = "This plugin can send live OpenShock commands. Double-check your limits, token, and shocker before enabling.";

	@ConfigSection(
		name = "Connection",
		description = "OpenShock connection settings.",
		position = 0
	)
	String connectionSection = "connection";

	@ConfigSection(
		name = "Safety",
		description = "Master toggle and safety limits.",
		position = 1
	)
	String safetySection = "safety";

	@ConfigSection(
		name = "Damage",
		description = "Damage-trigger rules.",
		position = 2
	)
	String damageSection = "damage";

	@ConfigSection(
		name = "Death",
		description = "Trigger sent when your player dies.",
		position = 4
	)
	String deathSection = "death";

	@ConfigSection(
		name = "Idle",
		description = "Trigger sent after a configurable period of inactivity.",
		position = 5
	)
	String idleSection = "idle";

	@ConfigSection(
		name = "RuneLite Notifications",
		description = "Trigger sent when RuneLite fires a notification.",
		position = 6
	)
	String notificationSection = "notifications";

	@ConfigSection(
		name = "Chat Keywords",
		description = "Optional game-message substring matching.",
		position = 7
	)
	String keywordSection = "keywords";

	@ConfigItem(
		keyName = "enabled",
		name = "Enable RuneShock",
		description = "Master switch for all OpenShock sending.",
		position = 0,
		section = safetySection
	)
	default boolean enabled()
	{
		return false;
	}

	@ConfigItem(
		keyName = "apiBaseUrl",
		name = "API base URL",
		description = "OpenShock API base URL.",
		position = 0,
		section = connectionSection
	)
	default String apiBaseUrl()
	{
		return "https://api.openshock.app";
	}

	@ConfigItem(
		keyName = "apiToken",
		name = "API token",
		description = "OpenShock API token used for the Open-Shock-Token header.",
		position = 1,
		secret = true,
		section = connectionSection
	)
	default String apiToken()
	{
		return "";
	}

	@ConfigItem(
		keyName = "shockerId",
		name = "Shocker ID",
		description = "UUID of the single shocker this plugin should control.",
		position = 2,
		section = connectionSection
	)
	default String shockerId()
	{
		return "";
	}

	@ConfigItem(
		keyName = "requestName",
		name = "Request name",
		description = "Optional customName sent to OpenShock logs.",
		position = 3,
		section = connectionSection
	)
	default String requestName()
	{
		return "RuneShock";
	}

	@ConfigItem(
		keyName = "globalCooldownMillis",
		name = "Global cooldown",
		description = "Minimum time between OpenShock sends. Set to 600 ms to mirror one OSRS tick.",
		position = 1,
		section = safetySection
	)
	@Units(Units.MILLISECONDS)
	@Range(min = 0, max = 60000)
	default int globalCooldownMillis()
	{
		return 600;
	}

	@ConfigItem(
		keyName = "intensityCap",
		name = "Intensity cap",
		description = "Maximum intensity RuneShock is allowed to send.",
		position = 2,
		section = safetySection
	)
	@Units(Units.PERCENT)
	@Range(min = 1, max = 100)
	default int intensityCap()
	{
		return 100;
	}

	@ConfigItem(
		keyName = "durationCapMillis",
		name = "Duration cap",
		description = "Maximum duration RuneShock is allowed to send.",
		position = 3,
		section = safetySection
	)
	@Units(Units.MILLISECONDS)
	@Range(min = 300, max = 65535)
	default int durationCapMillis()
	{
		return 600;
	}

	@ConfigItem(
		keyName = "damageVibrateEnabled",
		name = "Low-damage rule",
		description = "Enable the low-damage vibrate rule.",
		position = 0,
		section = damageSection
	)
	default boolean damageVibrateEnabled()
	{
		return true;
	}

	@ConfigItem(
		keyName = "damageVibrateAction",
		name = "Low-damage action",
		description = "Action used for the low-damage rule.",
		position = 1,
		section = damageSection
	)
	default RuneshockActionType damageVibrateAction()
	{
		return RuneshockActionType.VIBRATE;
	}

	@ConfigItem(
		keyName = "damageVibrateMinDamage",
		name = "Low-damage minimum",
		description = "Minimum damage for the vibrate rule to match.",
		position = 2,
		section = damageSection
	)
	@Range(min = 1, max = 999)
	default int damageVibrateMinDamage()
	{
		return 1;
	}

	@ConfigItem(
		keyName = "damageVibrateMaxDamage",
		name = "Low-damage maximum",
		description = "Maximum damage for the vibrate rule to match.",
		position = 3,
		section = damageSection
	)
	@Range(min = 1, max = 999)
	default int damageVibrateMaxDamage()
	{
		return 19;
	}

	@ConfigItem(
		keyName = "damageVibrateIntensityMode",
		name = "Low-damage intensity mode",
		description = "Whether the vibrate rule uses a flat value or scales with damage.",
		position = 4,
		section = damageSection
	)
	default IntensityMode damageVibrateIntensityMode()
	{
		return IntensityMode.SCALED;
	}

	@ConfigItem(
		keyName = "damageVibrateFlatIntensity",
		name = "Low-damage flat intensity",
		description = "Used when the vibrate rule is in Flat mode.",
		position = 5,
		section = damageSection
	)
	@Units(Units.PERCENT)
	@Range(min = 1, max = 100)
	default int damageVibrateFlatIntensity()
	{
		return 20;
	}

	@ConfigItem(
		keyName = "damageVibrateMinIntensity",
		name = "Low-damage scaled minimum",
		description = "Used when the vibrate rule is in Scaled mode.",
		position = 6,
		section = damageSection
	)
	@Units(Units.PERCENT)
	@Range(min = 1, max = 100)
	default int damageVibrateMinIntensity()
	{
		return 10;
	}

	@ConfigItem(
		keyName = "damageVibrateMaxIntensity",
		name = "Low-damage scaled maximum",
		description = "Used when the vibrate rule is in Scaled mode.",
		position = 7,
		section = damageSection
	)
	@Units(Units.PERCENT)
	@Range(min = 1, max = 100)
	default int damageVibrateMaxIntensity()
	{
		return 35;
	}

	@ConfigItem(
		keyName = "damageVibrateDurationMillis",
		name = "Low-damage duration",
		description = "Duration for the vibrate damage rule.",
		position = 8,
		section = damageSection
	)
	@Units(Units.MILLISECONDS)
	@Range(min = 300, max = 65535)
	default int damageVibrateDurationMillis()
	{
		return 400;
	}

	@ConfigItem(
		keyName = "damageShockEnabled",
		name = "High-damage rule",
		description = "Enable the high-damage shock rule.",
		position = 10,
		section = damageSection
	)
	default boolean damageShockEnabled()
	{
		return true;
	}

	@ConfigItem(
		keyName = "damageShockAction",
		name = "High-damage action",
		description = "Action used for the high-damage rule.",
		position = 11,
		section = damageSection
	)
	default RuneshockActionType damageShockAction()
	{
		return RuneshockActionType.SHOCK;
	}

	@ConfigItem(
		keyName = "damageShockMinDamage",
		name = "High-damage minimum",
		description = "Minimum damage for the shock rule to match.",
		position = 12,
		section = damageSection
	)
	@Range(min = 1, max = 999)
	default int damageShockMinDamage()
	{
		return 20;
	}

	@ConfigItem(
		keyName = "damageShockMaxDamage",
		name = "High-damage maximum",
		description = "Maximum damage for the shock rule to match. Set to 0 for no upper bound.",
		position = 13,
		section = damageSection
	)
	@Range(min = 0, max = 999)
	default int damageShockMaxDamage()
	{
		return 0;
	}

	@ConfigItem(
		keyName = "damageShockIntensityMode",
		name = "High-damage intensity mode",
		description = "Whether the shock rule uses a flat value or scales with damage.",
		position = 14,
		section = damageSection
	)
	default IntensityMode damageShockIntensityMode()
	{
		return IntensityMode.SCALED;
	}

	@ConfigItem(
		keyName = "damageShockFlatIntensity",
		name = "High-damage flat intensity",
		description = "Used when the shock rule is in Flat mode.",
		position = 15,
		section = damageSection
	)
	@Units(Units.PERCENT)
	@Range(min = 1, max = 100)
	default int damageShockFlatIntensity()
	{
		return 40;
	}

	@ConfigItem(
		keyName = "damageShockMinIntensity",
		name = "High-damage scaled minimum",
		description = "Used when the shock rule is in Scaled mode.",
		position = 16,
		section = damageSection
	)
	@Units(Units.PERCENT)
	@Range(min = 1, max = 100)
	default int damageShockMinIntensity()
	{
		return 35;
	}

	@ConfigItem(
		keyName = "damageShockMaxIntensity",
		name = "High-damage scaled maximum",
		description = "Used when the shock rule is in Scaled mode.",
		position = 17,
		section = damageSection
	)
	@Units(Units.PERCENT)
	@Range(min = 1, max = 100)
	default int damageShockMaxIntensity()
	{
		return 60;
	}

	@ConfigItem(
		keyName = "damageShockDurationMillis",
		name = "High-damage duration",
		description = "Duration for the shock damage rule.",
		position = 18,
		section = damageSection
	)
	@Units(Units.MILLISECONDS)
	@Range(min = 300, max = 65535)
	default int damageShockDurationMillis()
	{
		return 300;
	}

	@ConfigItem(
		keyName = "deathEnabled",
		name = "Enable death trigger",
		description = "Send an OpenShock action when your player dies.",
		position = 0,
		section = deathSection
	)
	default boolean deathEnabled()
	{
		return true;
	}

	@ConfigItem(
		keyName = "deathAction",
		name = "Action",
		description = "Action used for the death trigger.",
		position = 1,
		section = deathSection
	)
	default RuneshockActionType deathAction()
	{
		return RuneshockActionType.SHOCK;
	}

	@ConfigItem(
		keyName = "deathIntensity",
		name = "Intensity",
		description = "Intensity used for the death trigger.",
		position = 2,
		section = deathSection
	)
	@Units(Units.PERCENT)
	@Range(min = 1, max = 100)
	default int deathIntensity()
	{
		return 55;
	}

	@ConfigItem(
		keyName = "deathDurationMillis",
		name = "Duration",
		description = "Duration used for the death trigger.",
		position = 3,
		section = deathSection
	)
	@Units(Units.MILLISECONDS)
	@Range(min = 300, max = 65535)
	default int deathDurationMillis()
	{
		return 400;
	}

	@ConfigItem(
		keyName = "idleEnabled",
		name = "Enable idle trigger",
		description = "Send an OpenShock action after prolonged inactivity.",
		position = 0,
		section = idleSection
	)
	default boolean idleEnabled()
	{
		return false;
	}

	@ConfigItem(
		keyName = "idleSeconds",
		name = "Idle seconds",
		description = "How long to wait before the idle trigger fires.",
		position = 1,
		section = idleSection
	)
	@Units(Units.SECONDS)
	@Range(min = 5, max = 3600)
	default int idleSeconds()
	{
		return 30;
	}

	@ConfigItem(
		keyName = "idleAction",
		name = "Action",
		description = "Action used for the idle trigger.",
		position = 2,
		section = idleSection
	)
	default RuneshockActionType idleAction()
	{
		return RuneshockActionType.VIBRATE;
	}

	@ConfigItem(
		keyName = "idleIntensity",
		name = "Intensity",
		description = "Intensity used for the idle trigger.",
		position = 3,
		section = idleSection
	)
	@Units(Units.PERCENT)
	@Range(min = 1, max = 100)
	default int idleIntensity()
	{
		return 20;
	}

	@ConfigItem(
		keyName = "idleDurationMillis",
		name = "Duration",
		description = "Duration used for the idle trigger.",
		position = 4,
		section = idleSection
	)
	@Units(Units.MILLISECONDS)
	@Range(min = 300, max = 65535)
	default int idleDurationMillis()
	{
		return 300;
	}

	@ConfigItem(
		keyName = "notificationEnabled",
		name = "Enable notification trigger",
		description = "Send an OpenShock action whenever RuneLite fires a notification.",
		position = 0,
		section = notificationSection
	)
	default boolean notificationEnabled()
	{
		return false;
	}

	@ConfigItem(
		keyName = "notificationAction",
		name = "Action",
		description = "Action used for RuneLite notifications.",
		position = 1,
		section = notificationSection
	)
	default RuneshockActionType notificationAction()
	{
		return RuneshockActionType.VIBRATE;
	}

	@ConfigItem(
		keyName = "notificationIntensity",
		name = "Intensity",
		description = "Intensity used for RuneLite notifications.",
		position = 2,
		section = notificationSection
	)
	@Units(Units.PERCENT)
	@Range(min = 1, max = 100)
	default int notificationIntensity()
	{
		return 20;
	}

	@ConfigItem(
		keyName = "notificationDurationMillis",
		name = "Duration",
		description = "Duration used for RuneLite notifications.",
		position = 3,
		section = notificationSection
	)
	@Units(Units.MILLISECONDS)
	@Range(min = 300, max = 65535)
	default int notificationDurationMillis()
	{
		return 300;
	}

	@ConfigItem(
		keyName = "keywordTriggerEnabled",
		name = "Enable chat keyword trigger",
		description = "Match game messages by substring and send an action.",
		position = 0,
		section = keywordSection
	)
	default boolean keywordTriggerEnabled()
	{
		return false;
	}

	@ConfigItem(
		keyName = "keywordPhrases",
		name = "Keyword list",
		description = "Comma-separated substrings to match against game messages.",
		position = 1,
		section = keywordSection
	)
	default String keywordPhrases()
	{
		return "";
	}

	@ConfigItem(
		keyName = "keywordAction",
		name = "Action",
		description = "Action used when a chat keyword matches.",
		position = 2,
		section = keywordSection
	)
	default RuneshockActionType keywordAction()
	{
		return RuneshockActionType.SHOCK;
	}

	@ConfigItem(
		keyName = "keywordIntensity",
		name = "Intensity",
		description = "Intensity used when a chat keyword matches.",
		position = 3,
		section = keywordSection
	)
	@Units(Units.PERCENT)
	@Range(min = 1, max = 100)
	default int keywordIntensity()
	{
		return 35;
	}

	@ConfigItem(
		keyName = "keywordDurationMillis",
		name = "Duration",
		description = "Duration used when a chat keyword matches.",
		position = 4,
		section = keywordSection
	)
	@Units(Units.MILLISECONDS)
	@Range(min = 300, max = 65535)
	default int keywordDurationMillis()
	{
		return 300;
	}
}
