/*
 * JPEGMarkerReader
 *
 * Copyright (c) 2006 Marco Schmidt.
 * All rights reserved.
 */

package net.sourceforge.jiu.codecs.jpeg;

import java.io.DataInput;
import java.io.IOException;
import net.sourceforge.jiu.codecs.InvalidFileStructureException;
import net.sourceforge.jiu.codecs.UnsupportedTypeException;
import net.sourceforge.jiu.util.ArrayConverter;

/**
 * Static helper methods to read various JPEG bitstream headers from a
 * {@link java.io.DataInput} source into objects of the appropriate
 * data classes.
 * Objects are then added to a {@link JPEGData} object.
 * @author Marco Schmidt
 * @since 0.14.0
 */
public class JPEGMarkerReader
{
	/**
	 * Private constructor to prevent instantiation.
	 */
	private JPEGMarkerReader()
	{
	}

	public static void readHuffmanTables(DataInput in, JPEGData jpegData, int length) throws
		InvalidFileStructureException,
		IOException
	{
		while (length > 0)
		{
			if (length < 17)
			{
				throw new InvalidFileStructureException("DHT marker - " +
					"less than 17 bytes left.");
			}
			JPEGHuffmanTable table = new JPEGHuffmanTable();
			int classId = in.readUnsignedByte();
			// class (AC or DC)
			int tableClass = (classId >> 4) & 0x0f;
			if (tableClass != JPEGHuffmanTable.TABLE_CLASS_AC &&
				tableClass != JPEGHuffmanTable.TABLE_CLASS_DC)
			{
				throw new InvalidFileStructureException(
					"Table class in DHT marker is neither AC nor DC.");
			}
			table.setClassAcDc(tableClass);
			// ID
			int tableId = classId & 0x0f;
			table.setId(tableId);
			// codes
			byte[] numCodes = new byte[16];
			in.readFully(numCodes);
			length -= 17;
			int[][] codes = new int[16][];
			for (int codeLength = 1; codeLength <= 16; codeLength++)
			{
				int number = numCodes[codeLength - 1] & 0xff;
				if (length < number)
				{
					throw new InvalidFileStructureException(
						"Not enough data left in DHT marker for codes of " +
						"length " + codeLength + ".");
				}
				codes[codeLength - 1] = new int[number];
				for (int codeIndex = 0; codeIndex < number; codeIndex++)
				{
					codes[codeLength - 1][codeIndex] = in.readUnsignedByte();
				}
				length -= number;
			}
			table.setCodes(codes);
			System.out.println(table);
			jpegData.addHuffmanTable(table);
		}
	}

	/**
	 * Read quantization tables from a DQT marker.
	 * P&M 7.8.3, p. 118f.
	 * @param jpegData data object which will store the table(s)
	 * @param length length of marker
	 * @throws InvalidFileStructureException if the DQT contains invalid data
	 * @throws IOException on reading errors
	 */
	public static void readQuantizationTables(DataInput in, JPEGData jpegData, int length) throws
		InvalidFileStructureException,
		IOException
	{
		while (length > 0)
		{
			int precId = in.readUnsignedByte();
			length--;
			JPEGQuantizationTable table = new JPEGQuantizationTable();
			int elementPrecision = (precId >> 4) & 0x0f;
			switch(elementPrecision)
			{
				case(0):
				{
					table.setElementPrecision(8);
					break;
				}
				case(1):
				{
					table.setElementPrecision(16);
					break;
				}
				default:
				{
					throw new InvalidFileStructureException(
						"Not a valid value for quantization table element precision: " +
						elementPrecision + " (expected 0 or 1).");
				}
			}
			int id = precId & 0x0f;
			if (id > 3)
			{
				throw new InvalidFileStructureException(
					"Not a valid quantization table id: " +
					id + " (expected 0 to 3).");
			}
			table.setId(id);
			// check if there's enough data left for the table elements
			int tableDataLength = JPEGConstants.SAMPLES_PER_BLOCK * (elementPrecision + 1); 
			if (length < tableDataLength)
			{
				throw new InvalidFileStructureException(
					"Not enough data left in marker for quantization table data: " +
					length + "byte(s) (expected at least " + tableDataLength + ").");
			}
			int[] data = new int[JPEGConstants.SAMPLES_PER_BLOCK];
			for (int i = 0; i < data.length; i++)
			{
				switch(elementPrecision)
				{
					case(0):
					{
						data[i] = in.readUnsignedByte();
						break;
					}
					case(1):
					{
						data[i] = in.readShort() & 0xffff;
						break;
					}
				}
			}
			length -= tableDataLength;
			table.setData(data);
			jpegData.addQuantizationTable(table);
		}
	}

	public static void readStartOfFrame(DataInput in, JPEGData jpegData,
		int marker, int length) throws
		InvalidFileStructureException,
		IOException,
		UnsupportedTypeException
	{
		if (length < 9)
		{
			throw new InvalidFileStructureException(
				"JPEG SOF header length must be at least nine bytes; got " +
				length + ".");
		}
		byte[] data = new byte[6];
		in.readFully(data);
		JPEGFrame frame = new JPEGFrame();
		// sample precision
		int samplePrecision = data[0] & 0xff;
		if (samplePrecision != 8)
		{
			throw new UnsupportedTypeException("Unsupported JPEG sample precision: " +
				samplePrecision);
		}
		frame.setSamplePrecision(samplePrecision);
		// height
		int height = ArrayConverter.getShortBEAsInt(data, 1);
		if (height < 1)
		{
			throw new InvalidFileStructureException(
				"JPEG SOF height value must be 1 or higher; got " +
				height + ".");
		}
		frame.setHeight(height);
		// width
		int width = ArrayConverter.getShortBEAsInt(data, 3);
		if (width < 1)
		{
			throw new InvalidFileStructureException(
				"JPEG SOF width value must be 1 or higher; got " +
				width + ".");
		}
		frame.setWidth(width);
		// number of components (= channels)
		int numComponents = data[5] & 0xff;
		if (numComponents != 1)
		{
			throw new UnsupportedTypeException("Unsupported number of JPEG components: " +
				numComponents);
		}
		frame.setNumComponents(numComponents);
		if (length - 6 != numComponents * 3)
		{
			throw new InvalidFileStructureException(
				"SOF marker has not expected size for " +
				numComponents + " component(s); got " + length +
				" instead of " + (6 + numComponents * 3));
		}
		JPEGFrameComponent[] frameComponents = new JPEGFrameComponent[numComponents]; 
		for (int componentIndex = 0; componentIndex < numComponents; componentIndex++)
		{
			in.readFully(data, 0, 3);
			JPEGFrameComponent frameComponent = new JPEGFrameComponent();
			int componentIdentifier = data[0] & 0xff;
			frameComponent.setComponentId(componentIdentifier);
			int horizontalSamplingFactor = (data[1] & 0xf0) >> 4; 
			frameComponent.setHorizontalSamplingFactor(horizontalSamplingFactor);
			int verticalSamplingFactor = data[1] & 0x0f; 
			frameComponent.setVerticalSamplingFactor(verticalSamplingFactor);
			int quantizationTable = data[2] & 0xff; 
			frameComponent.setQuantizationTableId(quantizationTable);
			frameComponents[componentIndex] = frameComponent;
		}
		frame.setComponents(frameComponents);
		jpegData.setFrame(frame);
		System.out.println(frame);
	}

	/**
	 * Read an SOS (start of scan) marker.
	 * P&M 7.6, p. 113.
	 * @param in source to read marker information from
	 * @param jpegData {@link JPEGData} object to update with information from the marker
	 * @param length size of marker in bytes
	 * @throws InvalidFileStructureException if encountered data does not follow the JPEG standard 
	 * @throws IOException on I/O errors
	 * @throws UnsupportedTypeException if encountered data is valid but unsupported by this package
	 */
	public static void readStartOfScan(DataInput in, JPEGData jpegData, int length) throws
		InvalidFileStructureException,
		IOException,
		UnsupportedTypeException
	{
		if (length < 6)
		{
			throw new InvalidFileStructureException("SOS marker must be at least six bytes large; got " + length);
		}
		int numScanComponents = in.readUnsignedByte();
		length--;
		if (numScanComponents < 1)
		{
			throw new InvalidFileStructureException("Number of scan components must be one or larger.");
		}
		JPEGScan scan = new JPEGScan();
		scan.setNumComponents(numScanComponents);
		JPEGScanComponentSpecification[] specs = new JPEGScanComponentSpecification[numScanComponents]; 
		byte[] data = new byte[2];
		for (int i = 0; i < numScanComponents; i++)
		{
			in.readFully(data);
			length -= 2;
			int componentSelector = data[0] & 0xff;
			int dcTableSelector = (data[1] & 0xf0) >> 4;
			int acTableSelector = data[1] & 0x0f;
			JPEGScanComponentSpecification spec = new JPEGScanComponentSpecification();
			spec.setAcEntropyTable(acTableSelector);
			spec.setDcEntropyTable(dcTableSelector);
			spec.setComponent(componentSelector);
			specs[i] = spec;
		}
		scan.setCompSpecs(specs);
		if (length != 3)
		{
			throw new InvalidFileStructureException(
				"Expected exactly three bytes left after scan component specs; got " +
				length);
		}
		data = new byte[3];
		in.readFully(data);
		// TODO: add these as fields to JPEGScan, check their values and call the scan setters to copy the data
		/*
		int spectralStart = data[0] & 0xff;
		int spectralEnd = data[1] & 0xff;
		int highSuccessiveBit = (data[2] & 0xf0) >> 4;
		int lowSuccessiveBit = data[2] & 0x0f;
		*/
		jpegData.addScan(scan);
	}
}
