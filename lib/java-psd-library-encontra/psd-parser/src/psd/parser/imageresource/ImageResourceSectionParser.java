package psd.parser.imageresource;

import java.io.IOException;
import java.lang.System;

import psd.parser.PsdInputStream;
import psd.parser.object.PsdDescriptor;

public class ImageResourceSectionParser {
	private static final String PSD_TAG = "8BIM";
	private ImageResourceSectionHandler handler;

	public void setHandler(ImageResourceSectionHandler handler) {
		this.handler = handler;
	}

	public int parse(PsdInputStream stream) throws IOException {
		int length = stream.readInt();
		int pos = stream.getPos();
		int id = 0;
		while (length > 0) {
			String tag = stream.readString(4);
			if (!tag.equals(PSD_TAG) && !tag.equals("MeSa")) {
				throw new IOException("Format error: Invalid image resources section.: " + tag);
			}
			length -= 4;
			// Save ID
			id = stream.readShort();
			length -= 2;

			int sizeOfName = stream.readByte() & 0xFF;
			if ((sizeOfName & 0x01) == 0)
				sizeOfName++;
			@SuppressWarnings("unused")
			String name = stream.readString(sizeOfName);
			length -= sizeOfName + 1;

			int sizeOfData = stream.readInt();
			length -= 4;
			if ((sizeOfData & 0x01) == 1)
				sizeOfData++;
			length -= sizeOfData;
			int storePos = stream.getPos();

			// TODO FIXME Is id correct?
			if (sizeOfData > 0 && tag.equals(PSD_TAG) && id >= 4000 && id < 5000) {
				String key = stream.readString(4);
				if (key.equals("mani")) {
					stream.skipBytes(12 + 12); // unknown data
					PsdDescriptor descriptor = new PsdDescriptor(stream);
					if (handler != null) {
						handler.imageResourceManiSectionParsed(descriptor);
					}
				}
			}
			stream.skipBytes(sizeOfData - (stream.getPos() - storePos));
		}
		stream.skipBytes(length - (stream.getPos() - pos));
		return id;
	}
}
