package name.atsushieno.midi;

public interface MidiPlayerCallback
{
	public void onFinished();
	public void onMessage(SmfMessage message);
}
