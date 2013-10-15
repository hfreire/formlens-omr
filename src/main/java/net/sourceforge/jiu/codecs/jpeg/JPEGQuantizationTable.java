package net.sourceforge.jiu.codecs.jpeg;

/**
 * Data for a JPEG bitstream quantization table.
 * Includes quantization data, table id (0 to 3)
 * and element precision in bits (8 or 16).
 * @author Marco Schmidt
 * @since 0.13.0
 */
public class JPEGQuantizationTable
{
	private int[] data;
	private int elementPrecision;
	private int id;
	
	public int[] getData()
	{
		return data;
	}

	public int getElementPrecision()
	{
		return elementPrecision;
	}

	public int getId()
	{
		return id;
	}

	public void setData(int[] is)
	{
		data = is;
	}

	public void setElementPrecision(int i)
	{
		elementPrecision = i;
	}

	public void setId(int i)
	{
		id = i;
	}
}
