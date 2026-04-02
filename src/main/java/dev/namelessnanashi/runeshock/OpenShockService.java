package dev.namelessnanashi.runeshock;

import com.google.gson.Gson;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Collections;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;
import javax.inject.Inject;
import javax.inject.Singleton;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.client.callback.ClientThread;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class OpenShockService
{
	private static final long ERROR_MESSAGE_COOLDOWN_MILLIS = 5000L;
	private static final Logger log = LoggerFactory.getLogger(OpenShockService.class);

	private final Client client;
	private final ClientThread clientThread;
	private final HttpClient httpClient;
	private final Gson gson = new Gson();
	private final AtomicLong lastDispatchMillis = new AtomicLong();
	private final AtomicLong lastErrorMessageMillis = new AtomicLong();

	@Inject
	OpenShockService(Client client, ClientThread clientThread)
	{
		this.client = client;
		this.clientThread = clientThread;
		this.httpClient = HttpClient.newBuilder()
			.connectTimeout(Duration.ofSeconds(5))
			.build();
	}

	public void reset()
	{
		lastDispatchMillis.set(0L);
	}

	public boolean dispatch(
		RuneshockConfig config,
		RuneshockActionType actionType,
		int requestedIntensity,
		int requestedDurationMillis,
		String reason
	)
	{
		if (!config.enabled())
		{
			return false;
		}

		final String token = trimToNull(config.apiToken());
		if (token == null)
		{
			warnUser("Missing OpenShock API token.");
			return false;
		}

		final UUID shockerUuid;
		try
		{
			shockerUuid = UUID.fromString(trimToNull(config.shockerId()));
		}
		catch (IllegalArgumentException ex)
		{
			warnUser("Shocker ID must be a valid UUID.");
			return false;
		}

		final long now = System.currentTimeMillis();
		final int cooldownMillis = Math.max(0, config.globalCooldownMillis());
		final long previousDispatch = lastDispatchMillis.get();
		if (cooldownMillis > 0 && previousDispatch > 0 && now - previousDispatch < cooldownMillis)
		{
			return false;
		}

		if (!lastDispatchMillis.compareAndSet(previousDispatch, now))
		{
			return false;
		}

		final int intensity = clamp(requestedIntensity, 1, Math.max(1, config.intensityCap()));
		final int durationMillis = clamp(requestedDurationMillis, 300, Math.max(300, config.durationCapMillis()));

		final String url;
		try
		{
			url = normalizeBaseUrl(config.apiBaseUrl()) + "/2/shockers/control";
		}
		catch (IllegalArgumentException ex)
		{
			warnUser("API base URL is invalid.");
			return false;
		}

		final ControlRequest payload = new ControlRequest(
			Collections.singletonList(new Control(shockerUuid.toString(), actionType.getApiValue(), intensity, durationMillis)),
			buildCustomName(config.requestName(), reason)
		);

		final HttpRequest request = HttpRequest.newBuilder()
			.uri(URI.create(url))
			.timeout(Duration.ofSeconds(10))
			.header("Content-Type", "application/json")
			.header("Open-Shock-Token", token)
			.POST(HttpRequest.BodyPublishers.ofString(gson.toJson(payload)))
			.build();

		httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
			.whenComplete((response, throwable) ->
			{
				if (throwable != null)
				{
					log.warn("OpenShock request failed for {}", reason, throwable);
					warnUser("OpenShock request failed: " + throwable.getMessage());
					return;
				}

				if (response.statusCode() / 100 != 2)
				{
					final String body = truncate(response.body(), 180);
					log.warn("OpenShock returned {} for {}: {}", response.statusCode(), reason, body);
					warnUser("OpenShock returned " + response.statusCode() + (body.isEmpty() ? "." : ": " + body));
					return;
				}

				log.debug("Sent OpenShock {} for {}", actionType, reason);
			});

		return true;
	}

	private void warnUser(String message)
	{
		final long now = System.currentTimeMillis();
		final long previous = lastErrorMessageMillis.get();
		if (previous > 0 && now - previous < ERROR_MESSAGE_COOLDOWN_MILLIS)
		{
			return;
		}

		if (!lastErrorMessageMillis.compareAndSet(previous, now))
		{
			return;
		}

		clientThread.invoke(() ->
		{
			if (client.getGameState() == GameState.LOGGED_IN)
			{
				client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", "[RuneShock] " + message, null);
			}
		});
	}

	private static String normalizeBaseUrl(String baseUrl)
	{
		final String trimmed = trimToNull(baseUrl);
		if (trimmed == null)
		{
			throw new IllegalArgumentException("Missing base URL");
		}

		return trimmed.endsWith("/") ? trimmed.substring(0, trimmed.length() - 1) : trimmed;
	}

	private static String buildCustomName(String configuredName, String reason)
	{
		final String prefix = trimToNull(configuredName);
		if (prefix == null)
		{
			return null;
		}

		return prefix + " - " + reason;
	}

	private static String trimToNull(String value)
	{
		if (value == null)
		{
			return null;
		}

		final String trimmed = value.trim();
		return trimmed.isEmpty() ? null : trimmed;
	}

	private static int clamp(int value, int min, int max)
	{
		return Math.max(min, Math.min(max, value));
	}

	private static String truncate(String value, int maxLength)
	{
		if (value == null)
		{
			return "";
		}

		final String collapsed = value.replaceAll("\\s+", " ").trim();
		if (collapsed.length() <= maxLength)
		{
			return collapsed;
		}

		return collapsed.substring(0, maxLength - 3) + "...";
	}

	@SuppressWarnings("unused")
	private static final class ControlRequest
	{
		private final java.util.List<Control> shocks;
		private final String customName;

		private ControlRequest(java.util.List<Control> shocks, String customName)
		{
			this.shocks = shocks;
			this.customName = customName;
		}
	}

	@SuppressWarnings("unused")
	private static final class Control
	{
		private final String id;
		private final String type;
		private final int intensity;
		private final int duration;

		private Control(String id, String type, int intensity, int duration)
		{
			this.id = id;
			this.type = type;
			this.intensity = intensity;
			this.duration = duration;
		}
	}
}
