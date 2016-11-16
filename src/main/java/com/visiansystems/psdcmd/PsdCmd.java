package com.visiansystems.psdcmd;

import com.sun.org.apache.bcel.internal.generic.TABLESWITCH;
import psd.Layer;
import psd.Psd;
import psd.parser.layer.LayerType;
import psd.parser.object.PsdObject;
import psd.parser.object.PsdText;
import psd.parser.object.PsdTextData;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.*;

/**
 * PSD Command line reader
 */
public class PsdCmd {
	/**
	 * Object Tags
	 */
	public enum TAGS {
		Txt,            // Text field
		AntA,           // Text AntiAliasing
		TextIndex,      // Text index
		Ornt,           // Ornament
		textGridding,   // Text Grid

		EngineData,     // Text Properties
		StyleRun,
		ParagraphRun,
		AntiAlias,
		Editor,
		GridInfo,
		Rendered,
		UseFractionalGlyphWidths,

		DocumentResources,  // More properties
		ResourceDict,

		FontSet,            // Group set for DocumentResources and ResourceDict
		MojiKumiSet,
		StyleSheetSet,
		SubscriptSize,
		SubscriptPosition,
		ParagraphSheetSet,
		KinsokuSet,
		TheNormalStyleSheet,
		SuperscriptPosition,
		TheNormalParagraphSheet,
		SmallCapSize,

		// StyleRun sub set
		DefaultRunData,
		RunArray,
		IsJoinable,
		RunLengthArray,

		// StyleSheetData
		FontBaseline,
		Tracking,
		HorizontalScale,
		Ligatures,
		FillFirst,
		AutoLeading,
		DLigatures,
		BaselineShift,
		Font,
		BaselineDirection,
		FauxBold,
		Strikethrough,
		Language,
		StrokeFlag,
		Underline,
		Tsume,
		Leading,
		OutlineWidth,
		FillColor,
		Kerning,
		StrokeColor,
		FontCaps,
		AutoKerning,
		FillFlag,
		VerticalScale,
		FauxItalic,
		FontSize,
		YUnderline,
		StyleRunAlignment,
		NoBreak,

	};

	/**
	 * Properties
 	 */
	public static StringBuffer cssBuffer = new StringBuffer();
	public static LinkedList<String> imageQueue = new LinkedList<String>();


	/**
	 * Main function
	 * @param args
	 */
	public static void main(String[] args) {
		if (args.length < 1) {
			System.out.println("Usage: PsdCmd <file>");
			return;
		}
		try {
			// Open PSD file
			File file = new File(args[0]);
			Psd psdFile = new Psd(file);

			// Prepare the name
			String name = file.getPath();
			int lastDot = name.lastIndexOf('.');
			if (lastDot > 2)
				name = name.substring(0, lastDot);
			// Create the main files directory
			name = createDirectory(name);
			// Create the images directory
			createDirectory(name + "/img");
			// Create the CSS directory
			createDirectory(name + "/css");

			// Create the HTML file
			String mainName = getMainName(name);
			File outFile = new File(name + "/" + mainName + ".html");
			FileOutputStream fos = new FileOutputStream(outFile);
			PrintStream ps = new PrintStream(fos);
			System.setOut(ps);

			// Export the PSD file
			exportPSD(name, psdFile);
		} catch (IOException ex) {
			System.out.println("Can't load psd-file " + args[0]);
		}
	}

	public static void exportPSD(String fileName, Psd psdFile) {
		// Create the header file
		exportHeader(fileName, psdFile);
		// Export all images
		createTreeModel(getLayers(psdFile), fileName);
		// Close file
		exportFooter(psdFile, fileName);
		// Create teh CSS file
		exportCSS(fileName);
	}

	/**
	 * exportHeader
	 * Create the HTML header.
	 * @param directory The export directory
	 * @param psd The PSD file.
	 */
	public static void exportHeader(String directory, Psd psd) {
		String fileName = getMainName(directory);
		System.out.println("<!DOCTYPE html>");
		System.out.println("<html>");
		System.out.println("<head>");
		System.out.println("<link rel=\"stylesheet\" href=\"css/" + fileName + ".css\">");
		System.out.println("</head>");
		System.out.println("<body>");
		System.out.println("<div class=\"Background\">");

		// Append the CSS class
		cssBuffer.append(".Background{\n");
		cssBuffer.append("	width:" + psd.getWidth() + "px;\n");
		cssBuffer.append("	height:" + psd.getHeight() + "px;\n");
		cssBuffer.append("}\n");
	}

	/**
	 * exportFooter
	 * Finalize the HTML file.
	 */
	public static void exportFooter(Psd psd, String directory) {
		// Export the final image
		if (psd.getBaseLayer() != null) {
			exportLayer(psd.getBaseLayer(), directory);
		}

		// Write images
		System.out.println(imageQueue.remove());
		while (! imageQueue.isEmpty())
			System.out.println(imageQueue.remove());

		// Close HTML
		System.out.println("</div>");
		System.out.println("</body>");
		System.out.println("</html>");
	}

	/**
	 * exportImage
	 * Create the image entry into HTML file.
	 * @param imageName The image name.
	 * @param imageFile The image file name.
	 * @param layer The layer to export
	 */
	public static void exportImage(String imageName, String imageFile, Layer layer) {
		// Save image into HTML
		if (imageName == null || imageName.isEmpty())
			imageName = "Main Image";
		imageQueue.add("<img class=\"" + imageFile + "\" src=\"img/" + imageFile + ".png\" alt=\"" + imageName + "\">");

		// Append the CSS class
		cssBuffer.append("." + imageFile + "{\n");
		cssBuffer.append("	position: fixed;\n");
		cssBuffer.append("	left:" + layer.getX() + "px;\n");
		cssBuffer.append("	top:" + layer.getY() + "px;\n");
		cssBuffer.append("	zoom: 1;\n");
		double opacity = layer.getAlpha() / 255.0;
		cssBuffer.append("	filter: alpha(opacity=" + opacity * 100.0 + ");\n");
		cssBuffer.append("	opacity: " + opacity + ";\n");
		cssBuffer.append("	width:" + layer.getWidth() + "px;\n");
		cssBuffer.append("	height:" + layer.getHeight() + "px;\n");
		cssBuffer.append("}\n");
	}

	/**
	 * exportCSS
	 * Export the CSS file with classes.
	 * @param fileName
	 */
	public static void exportCSS(String directory) {
		String fileName = getMainName(directory);
		File outFile = new File(directory + "/css/" + fileName + ".css");
		try {
			BufferedWriter bwr = new BufferedWriter(new FileWriter(outFile));
			//Write contents of StringBuffer to a file
			bwr.write(cssBuffer.toString());
			// Flush the stream
			bwr.flush();
			// Close the stream
			bwr.close();
		} catch (Exception e) {
		}
	}

	/**
	 * mountName
	 * For a given name, remove all unused characters.
	 * @param name The original name.
	 * @return The transformed name.
	 */
	private static String mountName(String name) {
		name = name.replace("  ", " ");
		name = name.replace(' ', '-');
		name = name.replace('/', '#');
		name = name.replaceAll("[^#A-Za-z0-9_-]", "");
		name = name.replace('#', '/');
		name = name.replaceAll("^-", "");
		name = name.replaceAll("-$", "");
		name = name.replaceAll("--", "-");
		return name;
	}

	/**
	 * exportText
	 * Export a text object.
	 * @param layer The layer with the object.
	 * @param className The class name of the CSS.
	 */
	public static void exportText(Layer layer, String className) {
		if (layer.getPsdDescriptor() == null)
			return;

		// Get the Text object
		PsdText text = (PsdText) layer.getPsdDescriptor().get(TAGS.Txt.name());
		imageQueue.add("<div class=\"" + className + "\"" + ">" + text.getValue().replaceAll("(\\r\\n|\\n)", "<br>") + "</div>");

		// Append the CSS class
		cssBuffer.append("." + className + "{\n");
		cssBuffer.append("	position: fixed;\n");
		cssBuffer.append("	left:" + layer.getX() + "px;\n");
		cssBuffer.append("	top:" + layer.getY() + "px;\n");

		// Get the tex properties
		PsdTextData textData = (PsdTextData) layer.getPsdDescriptor().get(TAGS.EngineData.name());
		if (textData == null || textData.getProperties() == null) {
			cssBuffer.append("}\n");
			return;
		}

		// Get the Font list
		HashMap<String, Object> documentResources = (HashMap<String, Object>)textData.getProperties().get(TAGS.DocumentResources.name());
		if (documentResources == null || documentResources.size() == 0) {
			cssBuffer.append("}\n");
			return;
		}
		ArrayList<HashMap<String, Object>> fontSet = (ArrayList<HashMap<String, Object>>)documentResources.get(TAGS.FontSet.name());
		if (fontSet == null || fontSet.size() == 0) {
			cssBuffer.append("}\n");
			return;
		}
		ArrayList<String> fontList = new ArrayList<String>();
		for (HashMap<String, Object> f : fontSet) {
			fontList.add(f.get("Name").toString());
		}

		// Run all properties
		for (Map.Entry<String, Object> entry : textData.getProperties().entrySet()) {
//			System.out.println("Key: " + entry.getKey());
//			System.out.println("Value: " + entry.getValue());

			// Find the StyleRun table
			Object o = ((HashMap<String, Object>)entry.getValue()).get(TAGS.StyleRun.name());
			if (o == null)
				continue;

			// Run the properties to find de Font Size and color
			HashMap<String, Object> styleRun = (HashMap<String, Object>) o;
			ArrayList<Object> runArray = (ArrayList<Object>)styleRun.get(TAGS.RunArray.name());
			for (Object i : runArray) {
				HashMap<String, Object> item = (HashMap<String, Object>) i;
				for (Map.Entry<String, Object> e : item.entrySet()) {
					HashMap<String, Object> styleSheet = (HashMap<String, Object>) e.getValue();
					for (Map.Entry<String, Object> z : styleSheet.entrySet()) {
						HashMap<String, Object> styleSheetData = (HashMap<String, Object>) z.getValue();
						Double fontIndex = (Double)styleSheetData.get(TAGS.Font.name());
						Double fontSize = (Double)styleSheetData.get(TAGS.FontSize.name());
						HashMap<String, Object> fontColor = (HashMap<String, Object>)styleSheetData.get(TAGS.FillColor.name());
						ArrayList<Double> colors = (ArrayList<Double>)fontColor.get("Values");
						cssBuffer.append("    font-family: " + fontList.get(fontIndex.intValue()) + "px;\n");
						cssBuffer.append("    font-size: " + fontSize.intValue() + "px;\n");
						cssBuffer.append("    color: rgb(" + (int)(colors.get(0) * 255.0)
								+ "," + (int)(colors.get(1) * 255.0)
								+ "," + (int)(colors.get(2) * 255.0)
								+ ");\n"
						);
						Double opacity = colors.get(3);
						cssBuffer.append("	filter: alpha(opacity=" + opacity * 100.0 + ");\n");
						cssBuffer.append("	opacity: " + opacity + ";\n");
					}
				}
			}
		}
		cssBuffer.append("}\n");
	}

	/**
	 * exportObject
	 * Export an object to HTML. Could be an image or text.
	 * @param layer The layer to export
	 * @param fileName The file name to use.
	 */
	public static void exportObject(Layer layer, String fileName) {
		// Export to HTML
		if (layer.getVersion() == 0 || layer.getPsdDescriptor() == null)
			exportImage(layer.toString(), fileName, layer);
		else
			exportText(layer, fileName);
	}

	/**
	 * exportLayer
	 * Export the image, CSS and HTML layer image
	 * @param layer The layer object to export.
	 * @param prefix The directory to export the image.
	 */
	public static void exportLayer(Layer layer, String prefix) {
		BufferedImage bi = layer.getImage();
		if (bi == null)
			return;

		// Export image
		try {
			// Create an unique file name
			UUID name = UUID.randomUUID();
			// Add an letter into the file name for CSS
			String fileName = "a" + name.toString();
			// Open file
			File outputFile = new File(prefix + "/img/" + fileName + ".png");
			// Save image
			ImageIO.write(bi, "png", outputFile);
			// Export object to HTML
			exportObject(layer, fileName);
		} catch (IOException e) {
			System.out.println(e.getMessage());
		}
	}

	/**
	 * getLayer
	 * Create a list with all layers
	 * @param psd The PSD object
	 * @return The layers list.
	 */
	public static List<Layer> getLayers(Psd psd) {
		List<Layer> layers = new LinkedList<Layer>();
		for (int i = 0; i < psd.getLayersCount(); i++)
			layers.add(psd.getLayer(i));
		return layers;
	}

	/**
	 * getLayer
	 * Create a list with all layers
	 * @param layer The layer
	 * @return The layers list.
	 */
	public static List<Layer> getLayers(Layer layer) {
		List<Layer> layers = new LinkedList<Layer>();
		for (int i = 0; i < layer.getLayersCount(); i++)
			layers.add(layer.getLayer(i));
		return layers;
	}

	/**
	 * createTreeModel
	 * Run the layers to create the images.
	 * @param layers
	 * @param prefix
	 */
	public static void createTreeModel(List<Layer> layers, String prefix) {
		//
		// Run all sub-layers
		//
		for (Layer l : layers) {
			if (!l.isVisible())
				continue;

			if (l.getType() == LayerType.HIDDEN)
				continue;

			if (l.getType() == LayerType.FOLDER) {
				if (l.getLayersCount() > 0) {
					List<Layer> ls = getLayers(l);
					createTreeModel(ls, prefix);
				}
				exportLayer(l, prefix);
			} else {
				exportLayer(l, prefix);
			}
		}
	}

	/**
	 * createDirectory
	 * Create a directory tree from a given name.
	 * @param name The directory name to create.
	 * @return The converted and created directory name.
	 */
	private static String createDirectory(String name) {
		// Prepare the name
		name = mountName(name);
		File dir = new File(name);
		// create multiple directories at one time
		dir.mkdirs();
		return name;
	}

	/**
	 * getMainName
	 * Return the file name.
	 * @param fileName
	 * @return
	 */
	private static String getMainName(String fileName) {
		String dir = fileName;
		int index = fileName.lastIndexOf('/');
		if (index > 0)
		fileName = fileName.substring(index + 1);
		return fileName;
	}
}

