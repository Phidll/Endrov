/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.recording.recmetFRAP;


import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.LinkedList;
import javax.swing.*;

import org.jdom.*;

import endrov.data.EvContainer;
import endrov.data.EvData;
import endrov.data.EvObject;
import endrov.gui.EvSwingUtil;
import endrov.gui.component.EvComboObject;
import endrov.gui.component.JSpinnerSimpleEvDecimal;
import endrov.gui.window.EvBasicWindow;
import endrov.gui.window.EvBasicWindowExtension;
import endrov.gui.window.EvBasicWindowHook;
import endrov.recording.EvAcquisition;
import endrov.recording.RecordingResource;
import endrov.recording.recmetMultidim.RecWidgetAcquire;
import endrov.roi.ROI;
import endrov.util.math.EvDecimal;

/**
 * FRAP acquisition
 * @author Johan Henriksson 
 */
public class RecWindowFRAP extends EvBasicWindow 
	{
	/******************************************************************************************************
	 *                               Static                                                               *
	 *****************************************************************************************************/
	static final long serialVersionUID=0;

	private JSpinnerSimpleEvDecimal spRecoveryTime=new JSpinnerSimpleEvDecimal();
	private JSpinnerSimpleEvDecimal spBleachTime=new JSpinnerSimpleEvDecimal();
	private JSpinnerSimpleEvDecimal spRate=new JSpinnerSimpleEvDecimal();

	private EvFRAPAcquisition acq=new EvFRAPAcquisition();

	private RecWidgetAcquire wAcq=new RecWidgetAcquire()
		{
			private static final long serialVersionUID = 1L;

			@Override
			public boolean getAcquisitionSettings()
				{
				
				acq.bleachTime=spBleachTime.getDecimalValue();
				acq.rate=spRate.getDecimalValue();
				acq.recoveryTime=spRecoveryTime.getDecimalValue();
				acq.roi=(ROI)roiCombo.getSelectedObject();
				
				if(acq.roi==null)
					{
					showErrorDialog("Need to select a ROI");
					return false;
					}
				else
					return true;
				}
			
			@Override
			public EvAcquisition getAcquisition()
				{
				return acq;
				}
		};

		
	private EvComboObject roiCombo=new EvComboObject(new LinkedList<EvObject>(), true, false)
		{
		private static final long serialVersionUID = 1L;
		public boolean includeObject(EvContainer cont)
			{
			return cont instanceof ROI;
			}
		};

	
	public RecWindowFRAP()
		{
		
		roiCombo.setRoot(RecordingResource.getData());
		
		spRecoveryTime.setDecimalValue(new EvDecimal(10));
		spRate.setDecimalValue(new EvDecimal(1));
		spBleachTime.setDecimalValue(new EvDecimal(1));
		wAcq.setStoreName("frap");
		
		
		////////////////////////////////////////////////////////////////////////
		setLayout(new BorderLayout());
		add(EvSwingUtil.layoutCompactVertical(
				
				EvSwingUtil.withTitledBorder("Settings",
						EvSwingUtil.layoutEvenVertical(
								EvSwingUtil.layoutLCR(
										new JLabel("ROI"),
										roiCombo,
										null
										),

								EvSwingUtil.layoutLCR(
										new JLabel("Bleach time"),
										spBleachTime,
										new JLabel("[s]")
										),

								EvSwingUtil.layoutLCR(
										new JLabel("Recovery time"),
										spRecoveryTime,
										new JLabel("[s]")
										),

								EvSwingUtil.layoutLCR(
										new JLabel("Sampling intervals"),
										spRate,
										new JLabel("[s]")
										)
								)
						
				),
				
				wAcq
				),
				BorderLayout.CENTER);
		
		//Window overall things
		setTitleEvWindow("FRAP acquisition");
		packEvWindow();
		setVisibleEvWindow(true);
		//setBoundsEvWindow(bounds);
		}
	
	
	
	
	public void dataChangedEvent()
		{
		roiCombo.updateList();
		wAcq.dataChangedEvent();
		}

	public void windowEventUserLoadedFile(EvData data){}
	public void windowSavePersonalSettings(Element e){}
	public void windowLoadPersonalSettings(Element e){}
	public void windowFreeResources()
		{
		}
	
	public static void main(String[] args)
		{
		new RecWindowFRAP();
		
		}


	@Override
	public String windowHelpTopic()
		{
		return "The multi-dimensional acquisition window";
		}
	
	/******************************************************************************************************
	 * Plugin declaration
	 *****************************************************************************************************/
	public static void initPlugin() {}
	static
		{
		EvBasicWindow.addBasicWindowExtension(new EvBasicWindowExtension()
			{
			public void newBasicWindow(EvBasicWindow w)
				{
				w.addHook(this.getClass(),new Hook());
				}
			class Hook implements EvBasicWindowHook, ActionListener
				{
				public void createMenus(EvBasicWindow w)
					{
					JMenuItem mi=new JMenuItem("Acquire: FRAP",new ImageIcon(getClass().getResource("tangoCamera.png")));
					mi.addActionListener(this);
					EvBasicWindow.addMenuItemSorted(w.getCreateMenuWindowCategory("Recording"), mi);
					}
	
				public void actionPerformed(ActionEvent e) 
					{
					new RecWindowFRAP();
					}
	
				public void buildMenu(EvBasicWindow w){}
				}
			});
		
		
		
		}
	
	
	
	}
