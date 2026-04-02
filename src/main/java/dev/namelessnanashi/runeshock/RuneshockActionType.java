package dev.namelessnanashi.runeshock;

public enum RuneshockActionType
{
	SHOCK("Shock", "Shock"),
	VIBRATE("Vibrate", "Vibrate"),
	BEEP("Sound", "Beep");

	private final String apiValue;
	private final String label;

	RuneshockActionType(String apiValue, String label)
	{
		this.apiValue = apiValue;
		this.label = label;
	}

	public String getApiValue()
	{
		return apiValue;
	}

	@Override
	public String toString()
	{
		return label;
	}
}
