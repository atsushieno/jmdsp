package name.atsushieno.midi;

import java.io.IOException;
import java.io.InputStream;


public class SmfReader
{
	public SmfReader (InputStream stream)
	{
		this.stream = stream;
	}

	InputStream stream;
	SmfMusic data = new SmfMusic ();

	public SmfMusic getMusic() { return data; }

	public void parse () throws SmfParserException
	{
		if (
		    readByte ()  != 'M'
		    || readByte ()  != 'T'
		    || readByte ()  != 'h'
		    || readByte ()  != 'd')
			throw parseError ("MThd is expected");
		if (readInt32 () != 6)
			throw parseError ("Unexpeted data size (should be 6)");
		data.format = (byte) readInt16 ();
		int tracks = readInt16 ();
		data.deltaTimeSpec = readInt16 ();
//		try {
			for (int i = 0; i < tracks; i++)
				data.tracks.add (readTrack ());
		/*} catch (Exception ex) {
			throw parseError ("Unexpected data error", ex);
		}*/
	}

	SmfTrack readTrack () throws SmfParserException
	{
		SmfTrack tr = new SmfTrack ();
		if (
		    readByte ()  != 'M'
		    || readByte ()  != 'T'
		    || readByte ()  != 'r'
		    || readByte ()  != 'k')
			throw parseError ("MTrk is expected");
		int trackSize = readInt32 ();
		current_track_size = 0;
		int total = 0;
		while (current_track_size < trackSize) {
			int delta = readVariableLength ();
			tr.events.add (readEvent (delta));
			total += delta;
		}
		if (current_track_size != trackSize)
			throw parseError ("Size information mismatch");
		return tr;
	}

	int current_track_size;
	short running_status;

	SmfEvent readEvent (int deltaTime) throws SmfParserException
	{
		short b = peekByte ();
		if (b >= (short) 0x80)
			running_status = readByte ();
		int len;
		switch (running_status) {
		case SmfMessage.SysEx1:
		case SmfMessage.SysEx2:
		case SmfMessage.Meta:
			short metaType = 0;
			if (running_status == SmfMessage.Meta)
				metaType = readByte ();
			len = readVariableLength ();
			byte [] args = new byte [len];
			if (len > 0)
				readBytes (args);
			return new SmfEvent (deltaTime, new SmfMessage (running_status, metaType, (short) 0, args));
		default:
			int value = running_status;
			value += readByte () << 8;
			if (SmfMessage.fixedDataSize (running_status) == 2)
				value += readByte () << 16;
			return new SmfEvent (deltaTime, new SmfMessage (value));
		}
	}

	void readBytes (byte [] args) throws SmfParserException
	{
		current_track_size += args.length;
		int start = 0;
		if (peek_byte >= 0) {
			args [0] = (byte) peek_byte;
			peek_byte = -1;
			start = 1;
		}
		int len = 0;
		try {
			len = stream.read (args, start, args.length - start);
			if (len < args.length - start)
				throw parseError (String.format ("The stream is insufficient to read {0} bytes specified in the SMF event. Only {1} bytes read.", args.length, len));
		} catch (IOException ex) {
			throw parseError ("I/O error while parsing SMF", ex);
		} finally {
			stream_position += len;
		}
	}

	int readVariableLength () throws SmfParserException
	{
		int val = 0;
		for (int i = 0; i < 4; i++) {
			short b = readByte ();
			val = (val << 7) + b;
			if (b < 0x80)
				return val;
			val -= 0x80;
		}
		throw parseError ("Delta time specification exceeds the 4-byte limitation.");
	}

	int peek_byte = -1;
	int stream_position;

	short peekByte () throws SmfParserException
	{
		if (peek_byte < 0) {
			try {
				peek_byte = stream.read();
			} catch (IOException ex) {
				throw parseError ("I/O error while parsing SMF", ex);
			}
		}
		if (peek_byte < 0)
			throw parseError ("Insufficient stream. Failed to read a byte.");
		return (short) peek_byte;
	}

	short readByte () // to handle unsigned value
		throws SmfParserException
	{
		try {

		current_track_size++;
		if (peek_byte >= 0) {
			short b = (short) peek_byte;
			peek_byte = -1;
			return b;
		}
		try {
			int ret = stream.read();
			if (ret < 0)
				throw parseError ("Insufficient stream. Failed to read a byte.");
			return (short) ret;
		} catch (IOException ex) {
			throw parseError ("I/O error while parsing SMF", ex);
		}
		} finally {
			stream_position++;
		}
	}

	short readInt16 () throws SmfParserException
	{
		return (short) ((readByte () << 8) + readByte ());
	}

	int readInt32 () throws SmfParserException
	{
		return (((readByte () << 8) + readByte () << 8) + readByte () << 8) + readByte ();
	}

	SmfParserException parseError (String msg)
	{
		return parseError (msg, null);
	}

	SmfParserException parseError (String msg, Exception innerException)
	{
		return new SmfParserException (String.format (msg + "(at {0})", stream_position), innerException);
	}
}
