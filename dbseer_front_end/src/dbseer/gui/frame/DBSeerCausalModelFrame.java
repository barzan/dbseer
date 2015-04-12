package dbseer.gui.frame;

import com.mxgraph.model.mxCell;
import com.mxgraph.model.mxGeometry;
import com.mxgraph.swing.mxGraphComponent;
import com.mxgraph.util.mxConstants;
import com.mxgraph.util.mxRectangle;
import com.mxgraph.view.mxGraph;
import com.mxgraph.view.mxStylesheet;
import dbseer.gui.user.DBSeerCausalModel;
import dbseer.gui.user.DBSeerPredicate;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Map;
import java.util.Random;

/**
 * Created by dyoon on 14. 11. 30..
 */
public class DBSeerCausalModelFrame extends JFrame
{
	DBSeerCausalModel model;
	public DBSeerCausalModelFrame(DBSeerCausalModel model)
	{
		this.model = model;
		initializeGUI();
	}

	private void initializeGUI()
	{
		this.setTitle("DBSherlock: Causal Model");
		this.setLayout(new MigLayout("fill"));

		mxGraph graph = new mxGraph();
		Object parent = graph.getDefaultParent();

		mxStylesheet stylesheet = graph.getStylesheet();
		Hashtable<String, Object> causeStyle = new Hashtable<String, Object>();
		causeStyle.put(mxConstants.STYLE_SHAPE, mxConstants.SHAPE_ELLIPSE);
		causeStyle.put(mxConstants.STYLE_FONTCOLOR, "#FF0000");
		causeStyle.put(mxConstants.STYLE_STROKECOLOR, "#000000");
		causeStyle.put(mxConstants.STYLE_FILLCOLOR, "#FFFFFF");
		causeStyle.put(mxConstants.STYLE_FONTSIZE, "16");
		stylesheet.putCellStyle("CauseStyle", causeStyle);

		Hashtable<String, Object> predicateStyle = new Hashtable<String, Object>();
		predicateStyle.put(mxConstants.STYLE_SHAPE, mxConstants.SHAPE_RECTANGLE);
		predicateStyle.put(mxConstants.STYLE_STROKECOLOR, "#000000");
//		predicateStyle.put(mxConstants.STYLE_FILLCOLOR, "#FFFFFF");
		predicateStyle.put(mxConstants.STYLE_FONTSIZE, "13");
		stylesheet.putCellStyle("PredicateStyle", predicateStyle);

		Map<String, Object> edgeStyle = graph.getStylesheet().getDefaultEdgeStyle();
		edgeStyle.put(mxConstants.STYLE_ROUNDED, true);
		edgeStyle.put(mxConstants.STYLE_EDGE, mxConstants.EDGESTYLE_ENTITY_RELATION);

		Dimension panelSize = this.getSize();
		double panelWidth = 1024;
		double panelHeight = 768;

		Random r = new Random();

		int rowCount = 13;
		int row = 0;
		int col = 0;


		graph.getModel().beginUpdate();
		try
		{
			Object cause = graph.insertVertex(parent, null, model.getCause(), 500, 10, 160, 40, "CauseStyle");
//			mxCell cell = (mxCell)cause;
//			mxGeometry geom = (mxGeometry) cell.getGeometry().clone();
//			mxRectangle bounds = graph.getView().getState(cell).getLabelBounds();
//			geom.setWidth(bounds.getWidth() + 10);
//			geom.setHeight(bounds.getHeight() + 10);
//			graph.cellsResized(new Object[]{cell}, new mxRectangle[]{geom});
			ArrayList<Object> vertices = new ArrayList<Object>();
			for (DBSeerPredicate predicate : model.getPredicates())
			{
				Object pred = graph.insertVertex(parent, null, predicate.toString(),
						10 + 500 * col, 60 + 50 * row, 420, 40, "PredicateStyle");
				vertices.add(pred);
//				graph.insertEdge(parent, null, "", cause, pred);
				++row;
				if (row == rowCount)
				{
					row = 0;
					col++;
				}
			}
			for (int i=vertices.size()-1;i>=0;--i)
			{
				graph.insertEdge(parent, null, "", cause, vertices.get(i));
			}
		}
		finally
		{
			graph.getModel().endUpdate();
		}

		graph.setAutoSizeCells(true);
		graph.setCellsDisconnectable(false);
		mxGraphComponent component = new mxGraphComponent(graph);
		component.setConnectable(false);
		JScrollPane scrollPane = new JScrollPane(component);
		scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
		scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		this.add(scrollPane, "grow");
	}
}
