package name.atsushieno.midi;

import java.lang.Thread.State;
import java.util.Vector;

// Player implementation. Plays a MIDI song synchronously.
public class MidiSyncPlayer implements IMidiPlayerStatus
{
	public MidiSyncPlayer (SmfMusic music)
	{
		this.music = music;
		if (music.tracks.size() != 1)
			events = SmfTrackMerger.merge (music).tracks.get(0).events;
		else
			events = music.tracks.get(0).events;
		state = PlayerState.Stopped;
	}

	SmfMusic music;
	Vector<SmfEvent> events;
	//ManualResetEvent pause_handle = new ManualResetEvent (false);
	PlayerState state;
	boolean do_pause, do_stop;
	int play_delta_time;

	public PlayerState getState () { return state; }
	public int getPlayDeltaTime() { return play_delta_time; }
	public int getTempo () { return current_tempo; }
	public void setTempoRatio (double ratio) { tempo_ratio = ratio; }
	public int getTotalPlayTimeMilliseconds ()
	{
		return SmfMusic.getTotalPlayTimeMilliseconds (events, music.deltaTimeSpec);
	}

	public void close()
	{
		if (state != PlayerState.Stopped)
			stop ();
		mute ();
	}

	public void play ()
	{
		state = PlayerState.Playing;
	}

	void allControlReset ()
	{
		for (int i = 0; i < 16; i++)
			onMessage (new SmfMessage ((short) (i + 0xB0), (short) 0x79, (short) 0, null));
	}

	void mute ()
	{
		for (int i = 0; i < 16; i++)
			onMessage (new SmfMessage ((short) (i + 0xB0), (short) 0x78, (short) 0, null));
	}

	public void pause ()
	{
		do_pause = true;
	}

	public void seek (int milliseconds)
	{
		state = PlayerState.Paused;
		int v = 0;
		play_delta_time = 0;
		if (milliseconds == 0) { // head
			event_idx = 0;
			return;
		}
		for (event_idx = 0; event_idx < events.size(); event_idx++) {
			SmfEvent e = events.get (event_idx);
			int l = getDeltaTimeInMilliseconds (e.deltaTime);
			v += l;
			if (v > milliseconds) {
				event_idx--;
				return;
			}
			play_delta_time += e.deltaTime;
		}
	}

	int event_idx = 0;

	MidiPlayerCallback callback;
	public void setCallback (MidiPlayerCallback callback)
	{
		this.callback = callback;
	}

	public void playerLoop ()
	{
		allControlReset ();
		try {
			while (true) {
				if (do_stop)
					break;
				if (state == PlayerState.Paused) {
					Thread.sleep(100);
					continue;
				}
				if (do_pause) {
					do_pause = false;
					state = PlayerState.Paused;
					mute ();
					continue;
				}
				if (event_idx == events.size())
					break;
				handleEvent (events.get (event_idx++));
			}
		} catch (InterruptedException ex) {
			// stop playing
		}
		do_stop = false;
		mute ();
		state = PlayerState.Stopped;
		if (event_idx == events.size())
			if (callback != null)
				callback.onFinished ();
		event_idx = 0;
	}

	int current_tempo = SmfMetaType.DefaultTempo; // dummy
	double tempo_ratio = 1.0;

	int getDeltaTimeInMilliseconds (int deltaTime)
	{
		if (music.deltaTimeSpec >= 0x80)
			throw new UnsupportedOperationException ();
		return (int) (current_tempo / 1000 * deltaTime / music.deltaTimeSpec / tempo_ratio);
	}

	public void handleEvent (SmfEvent e) throws InterruptedException
	{
		if (e.deltaTime != 0) {
			int ms = getDeltaTimeInMilliseconds (e.deltaTime);
			Thread.sleep (ms);
		}
		if (e.message.getStatusByte() == 0xFF && e.message.getMsb() == SmfMetaType.Tempo)
			current_tempo = SmfMetaType.getTempo (e.message.data);

		onMessage (e.message);
		play_delta_time += e.deltaTime;
	}

	protected void onMessage (SmfMessage m)
	{
		if (callback != null)
			callback.onMessage(m);
	}

	public void stop ()
	{
		if (state != PlayerState.Stopped)
			do_stop = true;
	}
}
