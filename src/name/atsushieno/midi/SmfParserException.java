package name.atsushieno.midi;

public class SmfParserException extends Exception
{
	private static final long serialVersionUID = 357878884842321668L;

	public SmfParserException (String message)
	{
		super(message);
	}
	public SmfParserException (String message, Exception innerException)
	{
		super(message, innerException);
	}
}
