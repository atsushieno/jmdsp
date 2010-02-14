package name.atsushieno.midi;

import java.util.Vector;

public class SmfMusic
{
	Vector<SmfTrack> tracks = new Vector<SmfTrack> ();

	public SmfMusic ()
	{
		format = 1;
	}

	public short deltaTimeSpec;

	public byte format;

	public void addTrack (SmfTrack track)
	{
		this.tracks.add (track);
	}

	public Vector<SmfTrack> getTracks() { return tracks; }

	public int getTotalPlayTimeMilliseconds ()
	{
		if (format != 0)
			throw new UnsupportedOperationException ("Format 1 is not suitable to compute total play time within a song");
		return getTotalPlayTimeMilliseconds (tracks.get (0).getEvents(), deltaTimeSpec);
	}
	
	public static int getTotalPlayTimeMilliseconds (Vector<SmfEvent> events, int deltaTimeSpec)
	{
		if (deltaTimeSpec < 0)
			throw new UnsupportedOperationException ("non-tick based DeltaTime");
		else {
			int tempo = SmfMetaType.DefaultTempo;
			int v = 0;
			for (int i = 0; i < events.size(); i++) {
				SmfEvent e = events.get(i);
				v += (int) (tempo / 1000 * e.deltaTime / deltaTimeSpec);
				if (e.message.getMessageType() == SmfMessage.Meta && e.message.getMsb() == SmfMetaType.Tempo)
					tempo = SmfMetaType.getTempo (e.message.data);
			}
			return v;
		}
	}
}