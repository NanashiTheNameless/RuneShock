package dev.namelessnanashi.runeshock;

public enum IntensityMode
{
	FLAT("Flat"),
	SCALED("Scaled");

	private final String label;

	IntensityMode(String label)
	{
		this.label = label;
	}

	@Override
	public String toString()
	{
		return label;
	}
}
