package name.atsushieno.midi;
import java.util.Vector;

public class SmfTrack
{
	public SmfTrack ()
	{
		this(new Vector<SmfEvent>());
	}

	public SmfTrack (Vector<SmfEvent> events)
	{
		this.events = events instanceof Vector<?> ? (Vector<SmfEvent>) events : new Vector<SmfEvent> (events);
	}

	Vector<SmfEvent> events;

	public void AddEvent (SmfEvent evt)
	{
		events.add (evt);
	}

	public Vector<SmfEvent> getEvents () { return events; }
}
