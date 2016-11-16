/*
 * This file is part of java-psd-library.
 *
 * This library is free software: you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation, either version 3 of
 * the License, or (at your option) any later version.

 * This library is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.

 * You should have received a copy of the GNU Lesser General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/>.
 */

package com.visiansystems.psdcmd;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.util.*;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTree;
import javax.swing.JTree.DynamicUtilTreeNode;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeModel;

import psd.Layer;
import psd.Psd;
import psd.parser.layer.LayerType;

/**
 * @author Dmitry Belsky, Boris Suska
 *
 */
public class PsdView extends JPanel {

	private static final long serialVersionUID = 1L;

	private Psd psdFile;

	private JTree layers;

	private PsdRenderer renderer;

	private PsdRenderer origRenderer;

	public PsdView() {
		super(new BorderLayout(), true);
		this.psdFile = null;
		this.layers = new JTree(new Object[0]);
		this.layers.setRootVisible(false);
		this.setPreferredSize(new Dimension(640, 480));

		JTabbedPane tab = new JTabbedPane();

		this.origRenderer = new PsdRenderer();
		this.renderer = new PsdRenderer();

		JScrollPane scrollLayers = new JScrollPane(layers);
		scrollLayers.setMinimumSize(new Dimension(150, 480));
		JScrollPane scrollRenderer = new JScrollPane(renderer);

		JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, scrollLayers, scrollRenderer);
		split.setContinuousLayout(true);
		split.setDividerLocation(0.2);

		tab.addTab("Original image", null, new JScrollPane(origRenderer), "Image is rendered as could be shown (without separate layers).");
		tab.addTab("Image by layers", null, split, "Image is rendered from bottom to upper layer.");

		this.add(tab, BorderLayout.CENTER);
	}

	public List<Layer> getLayers(Psd psd) {
		List<Layer> layers = new LinkedList<Layer>();
		for (int i = 0; i < psd.getLayersCount(); i++)
			layers.add(psd.getLayer(i));
		return  layers;
	}

	public List<Layer> getLayers(Layer layer) {
		List<Layer> layers = new LinkedList<Layer>();
		for (int i = 0; i < layer.getLayersCount(); i++)
			layers.add(layer.getLayer(i));
		return  layers;
	}

	public void setPsdFile(Psd psdFile) {
		this.psdFile = psdFile;

		List<Layer> layers = getLayers(psdFile);
		this.layers.setModel(this.createTreeModel(layers));

		List<Layer> baseLayer = new LinkedList<Layer>();
		baseLayer.add(this.psdFile.getBaseLayer());
		this.origRenderer.setPsd(psdFile.getWidth(), psdFile.getHeight(), baseLayer);
		this.origRenderer.revalidate();

		//this.renderer.setPsd(psdFile.getWidth(), psdFile.getHeight(), this.psdFile.getLayers());
		this.renderer.setPsd(psdFile.getWidth(), psdFile.getHeight(), layers);
		this.renderer.revalidate();
	}

	private TreeModel createTreeModel(List<Layer> layers) {
		NamedVector<Object> currLevel = new NamedVector<Object>();
		Queue<NamedVector<Object>> levelQueue = new LinkedList<NamedVector<Object>>();
		for (Layer l : layers) {
			if (l.getType() == LayerType.HIDDEN) {
				levelQueue.add(currLevel);
				currLevel = new NamedVector<Object>();
			}
			else if (l.getType() == LayerType.FOLDER) {
				currLevel.setName(l.toString());
				Vector<Object> prevLevel = currLevel;
				if (l.getLayersCount() > 0) {
					List<Layer> ls = getLayers(l);
					createTreeModel(ls);
				}
				currLevel.add(0, prevLevel);
			}
			else {
				currLevel.add(0, new NamedPsdLayer(l));
			}
		}
		DefaultMutableTreeNode root = new DefaultMutableTreeNode("root");
		DynamicUtilTreeNode.createChildren(root, currLevel);
		return new DefaultTreeModel(root);
	}

	class PsdRenderer extends JComponent {

		private static final long serialVersionUID = 1L;

		List<Layer> layers;

		private boolean[] selection;

		private int width;

		private int height;

		public PsdRenderer() {
			this.clear();
		}

		public void setPsd(int witdh, int height, List<Layer> layers) {
			this.layers = layers;
			this.selection = new boolean[layers.size()];
			this.width = witdh;
			this.height = height;
			setPreferredSize(new Dimension(witdh+100, height+100));
		}

		public void clear() {
			this.layers = null;
			this.selection = null;
			this.width = 0;
			this.height = 0;
		}

		public void setSelectedLayer(int layerIndex, boolean value) {
			selection[layerIndex] = value;
		}

		@Override
		protected void paintComponent(Graphics g) {
			super.paintComponent(g);
			g.setColor(Color.LIGHT_GRAY);
			g.fillRect(0, 0, this.getWidth(), this.getHeight());
			if (layers != null) {
				if (psdFile != null) {
					int x = (getWidth() - width) / 2;
					int y = (getHeight() - height) / 2;
					for (int i = 0; i < layers.size(); i++) {
						Layer layer = layers.get(i);
						if (layer.isVisible()) {
							int drawX = layer.getX() + x;
							int drawY = layer.getY() + y;
							g.drawImage(layer.getImage(), drawX, drawY, null);
						}
					}
					g.setColor(Color.RED);
					for (int i = 0; i < layers.size(); i++) {
						if (selection[i]) {
							Layer layer = layers.get(i);
							int drawX = layer.getX() + x;
							int drawY = layer.getY() + y;
							g.drawRect(drawX, drawY, layer.getWidth(), layer.getHeight());
						}
					}
//     g.drawRect(x-1, y-1, psdFile.getWidth()+1, psdFile.getHeight()+1);

					// TODO: paint border by 4 rectangles because clip doesn't work correctly
					g.setColor(Color.LIGHT_GRAY);
					g.fillRect(0, 0, this.getWidth(), y);
					g.fillRect(0, y, x, this.getHeight());
					g.fillRect(x, height+y, this.getWidth()-x, y);
					g.fillRect(width+x, y, x, height);

				}
			}
		}
	}
}
