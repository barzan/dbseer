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

package dbseer.gui.panel;

import dbseer.gui.DBSeerGUI;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;

/**
 * Created by dyoon on 2014. 6. 2..
 */
public class DBSeerConfigurationPanel extends JPanel
{
	private DBSeerMiddlewarePanel loginPanel;
	private DBSeerConfigListPanel configListPanel;
	private DBSeerDatasetListPanel profileListPanel;

	public DBSeerConfigurationPanel()
	{
		initializeGUI();
	}

	private void initializeGUI()
	{
		this.setLayout(new MigLayout("fill"));

		loginPanel = new DBSeerMiddlewarePanel();
		loginPanel.setBorder(BorderFactory.createTitledBorder("Middleware"));
		DBSeerGUI.middlewarePanel = loginPanel;
		configListPanel = new DBSeerConfigListPanel();
		configListPanel.setBorder(BorderFactory.createTitledBorder("Available Train Configs"));
		profileListPanel = new DBSeerDatasetListPanel();
		profileListPanel.setBorder(BorderFactory.createTitledBorder("Available Datasets"));

		this.add(loginPanel, "dock west");
		this.add(profileListPanel, "grow");
		this.add(configListPanel, "grow");
	}
}
