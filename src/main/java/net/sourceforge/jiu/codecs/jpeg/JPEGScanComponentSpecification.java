package net.sourceforge.jiu.codecs.jpeg;

/**
 * Data class to store information on one component in one scan.
 * @author Marco Schmidt
 * @since 0.13.0
 */
public class JPEGScanComponentSpecification
{
	private int component;
	private int acEntropyTable;
	private int dcEntropyTable;

	public int getAcEntropyTable()
	{
		return acEntropyTable;
	}

	public int getComponent()
	{
		return component;
	}

	public int getDcEntropyTable()
	{
		return dcEntropyTable;
	}

	public void setAcEntropyTable(int i)
	{
		acEntropyTable = i;
	}

	public void setComponent(int i)
	{
		component = i;
	}

	public void setDcEntropyTable(int i)
	{
		dcEntropyTable = i;
	}
}
