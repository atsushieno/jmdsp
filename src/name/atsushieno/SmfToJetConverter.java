package name.atsushieno;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class SmfToJetConverter
{
	public static void main (String [] args) throws IOException
	{
		SmfToJetConverter c = new SmfToJetConverter ();
		for (int i = 0; i < args.length; i++) {
			String arg = args [i];
			c.convert (arg, arg.substring(0, arg.length() - 4) + ".jet");
		}
	}

	public void convert (String smf, String jet) throws IOException
	{
		convert (new File (smf), new FileOutputStream (jet));
	}

	public void convert (File smfFile, OutputStream jet) throws IOException
	{
		int headerSize = 0x30 + 8 - 8; // JET..JCOP0000JSMFxxxx
		int smfLen = (int) smfFile.length();
		FileInputStream smfStream = new FileInputStream (smfFile);
		DataOutputStream jetStream = new DataOutputStream (jet);
		jetStream.writeBytes("JET ");
		writeBigEndianInt(jetStream, smfLen + headerSize);
		jetStream.writeBytes("JINF");
		jetStream.writeInt(0x18000000);
		jetStream.writeBytes("JVER");
		jetStream.writeInt(1);
		jetStream.writeBytes("SMF#");
		jetStream.writeInt(0x01000000);
		jetStream.writeBytes("DLS#");
		jetStream.writeInt(0);
		jetStream.writeBytes("JCOP");
		jetStream.writeInt(0);
		jetStream.writeBytes("JSMF");
		writeBigEndianInt(jetStream, smfLen);
		byte [] smfBuf = new byte [smfLen];
		smfStream.read(smfBuf);
		smfStream.close();
		jetStream.write(smfBuf);
		jetStream.close();
	}

	void writeBigEndianInt(DataOutputStream s, int value)
		throws IOException
	{
		ByteBuffer buffer = ByteBuffer.allocate(4);
		buffer.order(ByteOrder.LITTLE_ENDIAN);
		buffer.putInt(0, value);
		for (int i = 0; i < 4; i++)
			s.writeByte(buffer.get());
	}
}
