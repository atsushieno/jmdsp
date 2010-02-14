package name.atsushieno.midi;

public class SmfMetaType
{
	public static final short SequenceNumber = 0x00;
	public static final short Text = 0x01;
	public static final short Copyright = 0x02;
	public static final short TrackName = 0x03;
	public static final short InstrumentName = 0x04;
	public static final short Lyric = 0x05;
	public static final short Marker = 0x06;
	public static final short Cue = 0x07;
	public static final short ChannelPrefix = 0x20;
	public static final short EndOfTrack = 0x2F;
	public static final short Tempo = 0x51;
	public static final short SmpteOffset = 0x54;
	public static final short TimeSignature = 0x58;
	public static final short KeySignature = 0x59;
	public static final short SequencerSpecific = 0x7F;

	public static final int DefaultTempo = 500000;
	
	public static int getTempo (byte [] data)
	{
		return (((short) data [0]) << 16) + (((short) data [1]) << 8) + ((short) data [2]);
	}
}