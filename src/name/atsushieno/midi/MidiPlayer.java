package name.atsushieno.midi;

public class MidiPlayer implements IMidiPlayerStatus
{
	MidiSyncPlayer player;
	Thread sync_player_thread;

	public MidiPlayer (SmfMusic music)
	{
		player = new MidiSyncPlayer (music);
	}

	public void setCallback (MidiPlayerCallback callback)
	{
		player.setCallback(callback);
	}

	public PlayerState getState () { return player.getState(); }

	public int getTempo() { return player.getTempo(); }

	public void SetTempoRatio (double value)
	{
		player.setTempoRatio (value);
	}

	public int getPlayDeltaTime() { return player.getPlayDeltaTime(); }

	public int getTotalPlayTimeMilliseconds ()
	{
		return player.getTotalPlayTimeMilliseconds ();
	}

	public void close ()
	{
		player.stop ();
	}

	public void startLoop ()
	{
		Runnable ts = new Runnable () {
			public void run() { player.playerLoop (); }
		};
		sync_player_thread = new Thread (ts);
		sync_player_thread.start();
	}

	public void playAsync ()
	{
		switch (getState()) {
		case Playing:
			return; // do nothing
		case Paused:
			player.play ();
			return;
		case Stopped:
			if (sync_player_thread == null || !sync_player_thread.isAlive())
				startLoop ();
			player.play ();
			return;
		}
	}

	public void pauseAsync ()
	{
		switch (getState()) {
		case Playing:
			player.pause ();
			return;
		default: // do nothing
			return;
		}
	}

	public void seek (int milliseconds)
	{
		player.seek (milliseconds);
	}
}
