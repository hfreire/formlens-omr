package net.sourceforge.jiu.codecs.jpeg;

/**
 * Data class for information from a JPEG scan header (stored in an SOS marker).
 * @author Marco Schmidt
 * @since 0.13.0
 */
public class JPEGScan
{
	private int numComponents;
	private JPEGScanComponentSpecification[] compSpecs;

	public int getNumComponents()
	{
		return numComponents;
	}

	public void setNumComponents(int i)
	{
		numComponents = i;
	}

	public JPEGScanComponentSpecification[] getCompSpecs()
	{
		return compSpecs;
	}

	public void setCompSpecs(JPEGScanComponentSpecification[] specifications)
	{
		compSpecs = specifications;
	}
}
