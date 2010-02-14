package name.atsushieno.midi;


public class SmfMessage
{
	public static final short NoteOff = (short) 0x80;
	public static final short NoteOn = (short) 0x90;
	public static final short PAf = (short) 0xA0;
	public static final short CC = (short) 0xB0;
	public static final short Program = (short) 0xC0;
	public static final short CAf = (short) 0xD0;
	public static final short Pitch = (short) 0xE0;
	public static final short SysEx1 = (short) 0xF0;
	public static final short SysEx2 = (short) 0xF7;
	public static final short Meta = (short) 0xFF;

	public static final byte EndSysEx = (byte) (short) 0xF7;

	public SmfMessage (int value)
	{
		this.value = value;
		data = null;
	}

	public SmfMessage (short type, short arg1, short arg2, byte [] data)
	{
		value = type + (arg1 << 8) + (arg2 << 16);
		this.data = data;
	}

	public final int value;

	// This expects EndSysEx byte _inclusive_ for F0 message.
	public final byte [] data;

	public short getStatusByte ()
	{
		return (short) (value & 0xFF);
	}

	public short getMessageType ()
	{
		switch (getStatusByte()) {
		case Meta:
		case SysEx1:
		case SysEx2:
			return getStatusByte();
		default:
			return (short) (value & 0xF0);
		}
	}

	public short getMsb ()
	{
		return (short) ((value & 0xFF00) >> 8);
	}

	public short getLsb ()
	{
		return (short) ((value & 0xFF0000) >> 16);
	}

	public short getMetaType ()
	{
		return getMsb ();
	}

	public byte getChannel ()
	{
		return (byte) (value & 0x0F);
	}

	public static byte fixedDataSize (short statusByte)
	{
		switch (statusByte & (short) 0xF0) {
		case (short) 0xF0: // and 0xF7, 0xFF
			return 0; // no fixed data
		case SmfMessage.Program: // ProgramChg
		case SmfMessage.CAf: // CAf
			return 1;
		default:
			return 2;
		}
	}

	@Override
	public String toString ()
	{
		return String.format("{0:X02}:{1:X02}:{2:X02}{3}", getStatusByte(), getMsb(), getLsb(), data != null ? data.length + "[data]" : "");
	}
}
