package psd.parser;

import java.io.*;

import psd.parser.header.HeaderSectionParser;
import psd.parser.imageresource.ImageResourceSectionParser;
import psd.parser.layer.LayersSectionParser;

public class PsdFileParser {
	private HeaderSectionParser headerParser;
	private ColorModeSectionParser colorModeSectionParser;
	private ImageResourceSectionParser imageResourceSectionParser;
	private LayersSectionParser layersSectionParser;
	private int imageID;
	
	public PsdFileParser() {
		headerParser = new HeaderSectionParser();
		colorModeSectionParser = new ColorModeSectionParser();
		imageResourceSectionParser = new ImageResourceSectionParser();
		layersSectionParser = new LayersSectionParser(headerParser.getHeader());
	}
	
	public HeaderSectionParser getHeaderSectionParser() {
		return headerParser;
	}
	
	public ImageResourceSectionParser getImageResourceSectionParser() {
		return imageResourceSectionParser;
	}

	public int getImageID() {
		return imageID;
	}
	
	public LayersSectionParser getLayersSectionParser() {
		return layersSectionParser;
	}
	
	public void parse(InputStream inputStream) throws IOException {
		PsdInputStream stream = new PsdInputStream(inputStream);
		headerParser.parse(stream);
		colorModeSectionParser.parse(stream);
		imageID = imageResourceSectionParser.parse(stream);
		layersSectionParser.parse(stream, imageID);
	}
}
