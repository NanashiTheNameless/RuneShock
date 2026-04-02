package dev.namelessnanashi.runeshock;

import com.google.inject.Provides;
import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.Hitsplat;
import net.runelite.api.Player;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.ActorDeath;
import net.runelite.api.events.ChatMessage;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.HitsplatApplied;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.EventBus;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.events.NotificationFired;
import net.runelite.client.events.ProfileChanged;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;

@PluginDescriptor(
	name = "RuneShock",
	configName = RuneshockConfig.GROUP,
	description = "RuneShock is a RuneLite external plugin that sends OpenShock commands from in-game events.",
	tags = {"openshock", "shock", "vibrate", "damage", "idle"},
	enabledByDefault = true
)
public class RuneshockPlugin extends Plugin
{
	@Inject
	private Client client;

	@Inject
	private RuneshockConfig config;

	@Inject
	private OpenShockService openShockService;

	@Inject
	private ConfigManager configManager;

	@Inject
	private EventBus eventBus;

	private long lastActivityMillis;
	private boolean idleTriggered;
	private WorldPoint lastPosition;
	private boolean enableConfirmationPending;
	private boolean suppressEnableRevert;

	@Provides
	RuneshockConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(RuneshockConfig.class);
	}

	@Override
	protected void startUp()
	{
		resetState();
	}

	@Override
	protected void shutDown()
	{
		resetState();
		openShockService.reset();
	}

	@Subscribe
	public void onGameStateChanged(GameStateChanged event)
	{
		final GameState gameState = event.getGameState();
		if (gameState == GameState.LOGGED_IN
			|| gameState == GameState.LOADING
			|| gameState == GameState.LOGIN_SCREEN
			|| gameState == GameState.HOPPING)
		{
			resetState();
		}
	}

	@Subscribe
	public void onConfigChanged(ConfigChanged event)
	{
		if (!RuneshockConfig.GROUP.equals(event.getGroup())
			|| !"enabled".equals(event.getKey())
			|| suppressEnableRevert)
		{
			return;
		}

		final boolean wasEnabled = Boolean.parseBoolean(event.getOldValue());
		final boolean isEnabled = Boolean.parseBoolean(event.getNewValue());
		if (wasEnabled || !isEnabled)
		{
			return;
		}

		enableConfirmationPending = true;
		try
		{
			if (confirmEnableRuneshock())
			{
				return;
			}

			suppressEnableRevert = true;
			configManager.setConfiguration(RuneshockConfig.GROUP, "enabled", false);
			eventBus.post(new ProfileChanged());
		}
		finally
		{
			suppressEnableRevert = false;
			enableConfirmationPending = false;
		}
	}

	@Subscribe
	public void onHitsplatApplied(HitsplatApplied event)
	{
		if (!isRuneshockEnabled() || event.getActor() != client.getLocalPlayer())
		{
			return;
		}

		final Hitsplat hitsplat = event.getHitsplat();
		if (!hitsplat.isMine())
		{
			return;
		}

		final int damage = hitsplat.getAmount();
		if (damage <= 0)
		{
			return;
		}

		final DamageRule shockRule = createShockRule();
		if (matches(shockRule, damage))
		{
			send(shockRule.actionType, computeIntensity(shockRule, damage), shockRule.durationMillis, "damage " + damage);
			return;
		}

		final DamageRule vibrateRule = createVibrateRule();
		if (matches(vibrateRule, damage))
		{
			send(vibrateRule.actionType, computeIntensity(vibrateRule, damage), vibrateRule.durationMillis, "damage " + damage);
		}
	}

	@Subscribe
	public void onActorDeath(ActorDeath event)
	{
		if (!isRuneshockEnabled() || !config.deathEnabled() || event.getActor() != client.getLocalPlayer())
		{
			return;
		}

		send(config.deathAction(), config.deathIntensity(), config.deathDurationMillis(), "death");
	}

	@Subscribe
	public void onNotificationFired(NotificationFired event)
	{
		if (!isRuneshockEnabled() || !config.notificationEnabled())
		{
			return;
		}

		final String message = event.getMessage();
		if (message == null || message.trim().isEmpty())
		{
			return;
		}

		send(config.notificationAction(), config.notificationIntensity(), config.notificationDurationMillis(), "notification");
	}

	@Subscribe
	public void onChatMessage(ChatMessage event)
	{
		if (!isRuneshockEnabled() || !config.keywordTriggerEnabled())
		{
			return;
		}

		if (event.getType() != ChatMessageType.GAMEMESSAGE && event.getType() != ChatMessageType.SPAM)
		{
			return;
		}

		final List<String> keywords = parseKeywords(config.keywordPhrases());
		if (keywords.isEmpty())
		{
			return;
		}

		final String normalizedMessage = normalizeMessage(event.getMessage());
		for (String keyword : keywords)
		{
			if (normalizedMessage.contains(keyword))
			{
				send(config.keywordAction(), config.keywordIntensity(), config.keywordDurationMillis(), "chat keyword");
				return;
			}
		}
	}

	@Subscribe
	public void onGameTick(GameTick event)
	{
		if (client.getGameState() != GameState.LOGGED_IN)
		{
			return;
		}

		final Player localPlayer = client.getLocalPlayer();
		if (localPlayer == null)
		{
			return;
		}

		if (isPlayerActive(localPlayer))
		{
			lastActivityMillis = System.currentTimeMillis();
			idleTriggered = false;
		}

		if (!isRuneshockEnabled() || !config.idleEnabled() || idleTriggered)
		{
			return;
		}

		final long idleForMillis = System.currentTimeMillis() - lastActivityMillis;
		if (idleForMillis >= config.idleSeconds() * 1000L)
		{
			if (send(config.idleAction(), config.idleIntensity(), config.idleDurationMillis(), "idle"))
			{
				idleTriggered = true;
			}
		}
	}

	private boolean isPlayerActive(Player localPlayer)
	{
		boolean active = false;

		final WorldPoint currentPosition = localPlayer.getWorldLocation();
		if (lastPosition == null || !lastPosition.equals(currentPosition))
		{
			active = true;
		}

		if (localPlayer.getAnimation() != -1 || localPlayer.getInteracting() != null)
		{
			active = true;
		}

		if (client.getMouseIdleTicks() < 2 || client.getKeyboardIdleTicks() < 2)
		{
			active = true;
		}

		lastPosition = currentPosition;
		return active;
	}

	private DamageRule createVibrateRule()
	{
		return new DamageRule(
			config.damageVibrateEnabled(),
			config.damageVibrateAction(),
			config.damageVibrateMinDamage(),
			config.damageVibrateMaxDamage(),
			config.damageVibrateIntensityMode(),
			config.damageVibrateFlatIntensity(),
			config.damageVibrateMinIntensity(),
			config.damageVibrateMaxIntensity(),
			config.damageVibrateDurationMillis()
		);
	}

	private DamageRule createShockRule()
	{
		return new DamageRule(
			config.damageShockEnabled(),
			config.damageShockAction(),
			config.damageShockMinDamage(),
			config.damageShockMaxDamage(),
			config.damageShockIntensityMode(),
			config.damageShockFlatIntensity(),
			config.damageShockMinIntensity(),
			config.damageShockMaxIntensity(),
			config.damageShockDurationMillis()
		);
	}

	private static boolean matches(DamageRule rule, int damage)
	{
		if (!rule.enabled || damage < Math.max(1, rule.minDamage))
		{
			return false;
		}

		return rule.maxDamage <= 0 || damage <= rule.maxDamage;
	}

	private static int computeIntensity(DamageRule rule, int damage)
	{
		if (rule.intensityMode == IntensityMode.FLAT)
		{
			return rule.flatIntensity;
		}

		final int minIntensity = Math.min(rule.minIntensity, rule.maxIntensity);
		final int maxIntensity = Math.max(rule.minIntensity, rule.maxIntensity);
		final int upperDamageBound = rule.maxDamage > rule.minDamage ? rule.maxDamage : damage;
		if (upperDamageBound <= rule.minDamage)
		{
			return maxIntensity;
		}

		final int clampedDamage = Math.max(rule.minDamage, Math.min(damage, upperDamageBound));
		final double ratio = (double) (clampedDamage - rule.minDamage) / (double) (upperDamageBound - rule.minDamage);
		return minIntensity + (int) Math.round(ratio * (maxIntensity - minIntensity));
	}

	private boolean send(RuneshockActionType actionType, int intensity, int durationMillis, String reason)
	{
		return openShockService.dispatch(config, actionType, intensity, durationMillis, reason);
	}

	private boolean isRuneshockEnabled()
	{
		return config.enabled() && !enableConfirmationPending;
	}

	private void resetState()
	{
		lastActivityMillis = System.currentTimeMillis();
		idleTriggered = false;
		lastPosition = null;
	}

	private static List<String> parseKeywords(String rawKeywords)
	{
		final List<String> keywords = new ArrayList<>();
		if (rawKeywords == null || rawKeywords.trim().isEmpty())
		{
			return keywords;
		}

		for (String keyword : rawKeywords.split(","))
		{
			final String normalized = normalizeMessage(keyword);
			if (!normalized.isEmpty())
			{
				keywords.add(normalized);
			}
		}

		return keywords;
	}

	private static String normalizeMessage(String message)
	{
		if (message == null)
		{
			return "";
		}

		return message
			.replaceAll("<[^>]+>", "")
			.trim()
			.toLowerCase();
	}

	private boolean confirmEnableRuneshock()
	{
		final int[] result = new int[1];
		final Runnable prompt = () -> result[0] = JOptionPane.showOptionDialog(
			null,
			RuneshockConfig.ENABLE_WARNING,
			"Enable RuneShock?",
			JOptionPane.YES_NO_OPTION,
			JOptionPane.WARNING_MESSAGE,
			null,
			new String[]{"Enable", "Cancel"},
			"Cancel"
		);

		if (SwingUtilities.isEventDispatchThread())
		{
			prompt.run();
		}
		else
		{
			try
			{
				SwingUtilities.invokeAndWait(prompt);
			}
			catch (Exception ex)
			{
				return false;
			}
		}

		return result[0] == JOptionPane.YES_OPTION;
	}

	private static final class DamageRule
	{
		private final boolean enabled;
		private final RuneshockActionType actionType;
		private final int minDamage;
		private final int maxDamage;
		private final IntensityMode intensityMode;
		private final int flatIntensity;
		private final int minIntensity;
		private final int maxIntensity;
		private final int durationMillis;

		private DamageRule(
			boolean enabled,
			RuneshockActionType actionType,
			int minDamage,
			int maxDamage,
			IntensityMode intensityMode,
			int flatIntensity,
			int minIntensity,
			int maxIntensity,
			int durationMillis
		)
		{
			this.enabled = enabled;
			this.actionType = actionType;
			this.minDamage = minDamage;
			this.maxDamage = maxDamage;
			this.intensityMode = intensityMode;
			this.flatIntensity = flatIntensity;
			this.minIntensity = minIntensity;
			this.maxIntensity = maxIntensity;
			this.durationMillis = durationMillis;
		}
	}
}
