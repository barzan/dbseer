/*
 * Copyright 2013 Barzan Mozafari
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package dbseer.gui.chart;

import dbseer.comp.clustering.StreamClustering;
import org.jfree.chart.JFreeChart;
import org.jfree.data.general.DefaultPieDataset;
import org.jfree.data.xy.XYSeriesCollection;

/**
 * Created by dyoon on 10/8/15.
 */
public class DBSeerChart
{
	private String name;
	private JFreeChart chart;
	private XYSeriesCollection XYDataset;
	private DefaultPieDataset pieDataset;

	private String XAxisName;
	private String YAxisName;

	public DBSeerChart(String name, JFreeChart chart)
	{
		this.name = name;
		this.chart = chart;
	}

	public String getName()
	{
		return name;
	}

	public JFreeChart getChart()
	{
		return chart;
	}

	public XYSeriesCollection getXYDataset()
	{
		return XYDataset;
	}

	public void setXYDataset(XYSeriesCollection XYDataset)
	{
		this.XYDataset = XYDataset;
	}

	public DefaultPieDataset getPieDataset()
	{
		return pieDataset;
	}

	public void setPieDataset(DefaultPieDataset pieDataset)
	{
		this.pieDataset = pieDataset;
	}

	public String getXAxisName()
	{
		return XAxisName;
	}

	public void setXAxisName(String XAxisName)
	{
		this.XAxisName = XAxisName;
	}

	public String getYAxisName()
	{
		return YAxisName;
	}

	public void setYAxisName(String YAxisName)
	{
		this.YAxisName = YAxisName;
	}
}
