package name.atsushieno.midi;

public interface IMidiPlayerStatus
{
	PlayerState getState();
	int getTempo();
	int getPlayDeltaTime();
	int getTotalPlayTimeMilliseconds();
}
