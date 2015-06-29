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

package dbseer.gui.panel.prediction;

import net.miginfocom.swing.MigLayout;

import javax.swing.*;

/**
 * Created by dyoon on 2014. 6. 22..
 */
public class TransactionCountsToIOPanel extends JPanel
{
	public TransactionCountsToIOPanel()
	{
		initializeGUI();
	}

	private void initializeGUI()
	{
		this.setLayout(new MigLayout("fill"));
//		this.add(new JLabel("Prediction: TransactionCountsToIO"));
	}
}
