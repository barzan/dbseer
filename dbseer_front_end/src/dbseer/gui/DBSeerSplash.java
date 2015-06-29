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

package dbseer.gui;

import javax.swing.*;
import java.awt.*;
import java.lang.reflect.InvocationTargetException;

/**
 * Created by dyoon on 2014. 5. 25..
 */
public class DBSeerSplash extends JWindow
{
	private JLabel status;
	public DBSeerSplash() throws InvocationTargetException, InterruptedException
	{
		Dimension screenSize =
				Toolkit.getDefaultToolkit().getScreenSize();

		status = new JLabel("initializing the statistical libraries...");
		status.setHorizontalAlignment(JLabel.CENTER);
		status.setPreferredSize(new Dimension(500, 24));

		Font labelFont = status.getFont();

		status.setFont(new Font(labelFont.getName(), Font.PLAIN, 18));
		Dimension labelSize = status.getPreferredSize();
		this.setBounds(screenSize.width/2 - (labelSize.width/2),
				screenSize.height/2 - (labelSize.height/2), 400,200);
		status.setBorder(BorderFactory.createLineBorder(Color.BLACK, 1));
		this.getContentPane().add(status, BorderLayout.CENTER);

		pack();
		this.setLocationRelativeTo(null);
		setVisible(true);
	}

	public void setText(String text)
	{
	    status.setText(text);
	}
}
