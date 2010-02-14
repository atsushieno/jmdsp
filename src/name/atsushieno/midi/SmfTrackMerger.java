package name.atsushieno.midi;
import java.util.Collections;
import java.util.Comparator;
import java.util.Vector;

public class SmfTrackMerger
{
	public static SmfMusic merge (SmfMusic source)
	{
		return new SmfTrackMerger (source).getMergedEvents ();
	}

	SmfTrackMerger (SmfMusic source)
	{
		this.source = source;
	}

	SmfMusic source;

	// FIXME: it should rather be implemented to iterate all
	// tracks with index to events, pick the track which contains
	// the nearest event and push the events into the merged queue.
	// It's simpler, and costs less by removing sort operation
	// over thousands of events.
	SmfMusic getMergedEvents ()
	{
		Vector<SmfEvent> l = new Vector<SmfEvent> ();

		for (int i = 0; i < source.tracks.size(); i++) {
			SmfTrack track = source.tracks.get(i);
			int delta = 0;
			for (int j = 0; j < track.events.size(); j++) {
				SmfEvent mev = track.events.get(j);
				delta += mev.deltaTime;
				l.add (new SmfEvent (delta, mev.message));
			}
		}

		if (l.size() == 0) {
			SmfMusic ret = new SmfMusic ();
			ret.deltaTimeSpec = source.deltaTimeSpec; // empty (why did you need to sort your song file?)
			return ret;
		}

		// Sort() does not always work as expected.
		// For example, it does not always preserve event 
		// orders on the same channels when the delta time
		// of event B after event A is 0. It could be sorted
		// either as A->B or B->A.
		//
		// To resolve this issue, we have to sort "chunk"
		// of events, not all single events themselves, so
		// that order of events in the same chunk is preserved
		// i.e. [AB] at 48 and [CDE] at 0 should be sorted as
		// [CDE] [AB].

		Vector<Integer> idxl = new Vector<Integer> (l.size());
		idxl.add (0);
		int prev = 0;
		for (int i = 0; i < l.size(); i++) {
			if (l.get (i).deltaTime != prev) {
				idxl.add (i);
				prev = l.get (i).deltaTime;
			}
		}

		Collections.sort(idxl, new SmfEventComparator (l));

		// now build a new event list based on the sorted blocks.
		Vector<SmfEvent> l2 = new Vector<SmfEvent> (l.size());
		int idx;
		for (int i = 0; i < idxl.size(); i++)
			for (idx = idxl.get (i), prev = l.get (idx).deltaTime; idx < l.size() && l.get (idx).deltaTime == prev; idx++)
				l2.add (l.get (idx));
//if (l.Count != l2.Count) throw new Exception (String.Format ("Internal eror: count mismatch: l1 {0} l2 {1}", l.Count, l2.Count));
		l = l2;

		// now events should be sorted correctly.

		int waitToNext = l.get (0).deltaTime;
		for (int i = 0; i < l.size() - 1; i++) {
			if (l.get (i).message.value != 0) { // if non-dummy
				int tmp = l.get (i + 1).deltaTime - l.get (i).deltaTime;
				l.set (i, new SmfEvent (waitToNext, l.get (i).message));
				waitToNext = tmp;
			}
		}
		l.set (l.size ( )- 1, new SmfEvent (waitToNext, l.get (l.size() - 1).message));

		SmfMusic m = new SmfMusic ();
		m.deltaTimeSpec = source.deltaTimeSpec;
		m.format = 0;
		m.tracks.add (new SmfTrack (l));
		return m;
	}

	class SmfEventComparator implements Comparator<Integer>
	{
		Vector<SmfEvent> l;
		public SmfEventComparator (Vector<SmfEvent> l)
		{
			this.l = l;
		}
		public int compare(Integer i1, Integer i2)
		{
			return l.get (i1).deltaTime - l.get (i2).deltaTime;
		}
	}
}
