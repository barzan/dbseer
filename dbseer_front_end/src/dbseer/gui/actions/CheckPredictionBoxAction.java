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

package dbseer.gui.actions;

import dbseer.gui.panel.DBSeerPredictionConsolePanel;

import javax.swing.*;
import java.awt.event.ActionEvent;

/**
 * Created by dyoon on 2014. 6. 9..
 */
public class CheckPredictionBoxAction extends AbstractAction
{
	private String name;

	public CheckPredictionBoxAction(String name)
	{
		super(name);
		this.name = name;
	}

	@Override
	public void actionPerformed(ActionEvent actionEvent)
	{
		Object source = actionEvent.getSource();
		if (source instanceof JCheckBox)
		{
			JCheckBox box = (JCheckBox)source;
			if (box.isSelected())
			{
				DBSeerPredictionConsolePanel.predictionSet.add(box.getText());
			}
			else
			{
				DBSeerPredictionConsolePanel.predictionSet.remove(box.getText());
			}
		}
	}
}
