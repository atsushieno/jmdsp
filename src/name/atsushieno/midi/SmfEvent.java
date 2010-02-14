package name.atsushieno.midi;

public class SmfEvent
{
	public SmfEvent (int deltaTime, SmfMessage msg)
	{
		this.deltaTime = deltaTime;
		message = msg;
	}

	public int deltaTime;
	public SmfMessage message;

	@Override
	public String toString ()
	{
		return String.format ("[dt{0}]{1}", deltaTime, message);
	}
}
