/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.typeParticleMeasure.flow;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.StringWriter;
import java.util.*;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;

import org.jdom.Element;

import endrov.core.log.EvLog;
import endrov.flow.Flow;
import endrov.flow.FlowExec;
import endrov.flow.FlowType;
import endrov.flow.FlowUnitBasic;
import endrov.flow.FlowUnitDeclaration;
import endrov.gui.EvSwingUtil;
import endrov.gui.component.JSpinnerSimpleEvFrame;
import endrov.gui.icon.BasicIcon;
import endrov.typeParticleMeasure.ParticleMeasure;
import endrov.typeParticleMeasure.ParticleMeasureIO;
import endrov.util.math.EvDecimal;
import endrov.windowFlow.FlowView;

/**
 * Flow unit: show particle measure
 * @author Johan Henriksson
 *
 */
public class FlowUnitShowMeasure extends FlowUnitBasic
	{
	public static FlowType flowTypeMeasure=new FlowType(ParticleMeasure.class);
	private static final String metaType="showMeasureParticle";
	
	
	private WeakHashMap<FlowView, TotalPanel> listPanels=new WeakHashMap<FlowView, TotalPanel>();
	
	
	public FlowUnitShowMeasure()
		{
		textPosition=TEXTABOVE;
		}
	
	
	public String toXML(Element e)
		{
		return metaType;
		}

	public void fromXML(Element e)
		{
		}

	

	
	public void evaluate(Flow flow, FlowExec exec) throws Exception
		{
		Map<String,Object> lastOutput=exec.getLastOutput(this);
		ParticleMeasure measure=(ParticleMeasure)flow.getInputValue(this, exec, "in");
		lastOutput.put("out",measure);
		
		//Update panels
		for(TotalPanel t:listPanels.values())
			t.setMeasure(measure);
		}
	
	
			
	
	
	public Component getGUIcomponent(final FlowView p)
		{
		TotalPanel t=listPanels.get(p);
		if(t==null)
			listPanels.put(p, t=new TotalPanel());
		return t;
		}

	
	
	/** Get types of flows in */
	protected void getTypesIn(Map<String, FlowType> types, Flow flow)
		{
		types.put("in", flowTypeMeasure);
		}
	
	/** Get types of flows out */
	protected void getTypesOut(Map<String, FlowType> types, Flow flow)
		{
		types.put("out", flowTypeMeasure);
		}
	

	
	@Override
	public Color getBackground()
		{
		return CategoryInfo.bgColor;
		}

	@Override
	public String getBasicShowName()
		{
		return "Particle measures";
		}

	@Override
	public ImageIcon getIcon()
		{
		return CategoryInfo.icon;
		}




	/*********************************************************************
	 * The special swing component for this unit
	 * @author Johan Henriksson
	 */
	private class TotalPanel extends JPanel implements TableModel, ActionListener
		{
		private static final long serialVersionUID = 1L;

		private LinkedList<TableModelListener> listeners=new LinkedList<TableModelListener>();

		//TODO Replace with something that knows which frames there are
		private JSpinnerSimpleEvFrame spFrame=new JSpinnerSimpleEvFrame();
		private ParticleMeasure measure=new ParticleMeasure();
		
		/**
		 * Row index to ID
		 */
		private List<Integer> mapToID=new ArrayList<Integer>();
		
		/**
		 * Column index to property. Does not include ID column
		 */
		private List<String> mapToColumn=new ArrayList<String>();

		private JTable table;
		private JButton bCopyToClipboard=BasicIcon.getButtonCopy();
		
		public TotalPanel()
			{
			setLayout(new BorderLayout());
	
			add(EvSwingUtil.layoutLCR(bCopyToClipboard, EvSwingUtil.withLabel("Frame ", spFrame), null),
					BorderLayout.NORTH);
			
			bCopyToClipboard.addActionListener(this);
			
			table=new JTable(this);
			JScrollPane sPane=new JScrollPane(table,JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
			add(sPane,BorderLayout.CENTER);
			
			table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
			
			spFrame.addChangeListener(new ChangeListener(){
				public void stateChanged(ChangeEvent e){
					updateNewFrame();
					}
				});
			
			setOpaque(false);
			Dimension size=new Dimension(300,200);
			setMaximumSize(size);
			setSize(size);
			setPreferredSize(size);
			}
		
		

		public EvDecimal getCurrentFrame()
			{
			return spFrame.getDecimalValue();
			}
		
		
		public ParticleMeasure.ColumnSet getCurrentPropMap(int id)
			{
			ParticleMeasure.Well well=measure.getWell(wellName);
			if(well!=null)
				{
				ParticleMeasure.Frame finfo=well.getFrame(getCurrentFrame());
				if(finfo==null)
					return null;
				else
					return finfo.getParticle(id);
				}
			else
				return null;
			}
		
		/**
		 * Call whenever there is a new measure
		 */
		public void setMeasure(ParticleMeasure m)
			{
			measure=m;			
			mapToColumn.clear();
			mapToColumn.addAll(measure.getParticleColumns());
			
			//table.setMinimumSize(new Dimension((mapToColumn.size()+1)*40,10));

			//TODO update frame pointer

			updateNewFrame();
			
			for(TableModelListener l:listeners)
				l.tableChanged(new TableModelEvent(this, TableModelEvent.HEADER_ROW));
			}

		String wellName="";
		
		/**
		 * Call whenever the frame is changed
		 */
		public void updateNewFrame()
			{
			mapToID.clear();
			ParticleMeasure.Well well=measure.getWell(wellName);
			if(well!=null)
				{
				ParticleMeasure.Frame map=well.getFrame(getCurrentFrame());
				if(map!=null)
					mapToID.addAll(map.getParticleIDs());
				}
					
			
			
			for(TableModelListener l:listeners)
				l.tableChanged(new TableModelEvent(this));
//				l.tableChanged(new TableModelEvent(this, TableModelEvent.HEADER_ROW));
			}
	
		public Class<?> getColumnClass(int columnIndex)
			{
			return String.class; //TODO
			}
	
		public int getColumnCount()
			{
			return mapToColumn.size()+1;
			}
	
		public String getColumnName(int columnIndex)
			{
			if(columnIndex==0)
				return "ID";
			else
				return mapToColumn.get(columnIndex-1);
			}
	
		public int getRowCount()
			{
			ParticleMeasure.Well well=measure.getWell(wellName);
			if(well!=null)
				{
				ParticleMeasure.Frame m=well.getFrame(getCurrentFrame());
				if(m==null)
					return 0;
				else
					return m.size();
				}
			else
				return 0;
			}
	
		public Object getValueAt(int rowIndex, int columnIndex)
			{
			int id=mapToID.get(rowIndex);
			if(columnIndex==0)
				return id;
			else
				{
				ParticleMeasure.ColumnSet info=getCurrentPropMap(id);
				String prop=mapToColumn.get(columnIndex-1);
				return info.getObject(prop).toString();
				}
			}
	
		public boolean isCellEditable(int rowIndex, int columnIndex)
			{
			return false;
			}
	
		
		public void addTableModelListener(TableModelListener l)
			{
			listeners.add(l);
			}
		
		public void removeTableModelListener(TableModelListener l)
			{
			listeners.remove(l);
			}
	
		public void setValueAt(Object aValue, int rowIndex, int columnIndex)
			{
			}

		public void actionPerformed(ActionEvent e)
			{
			if(e.getSource()==bCopyToClipboard)
				{
				try
					{
					StringWriter sw=new StringWriter();
					ParticleMeasureIO.writeCSVperparticle(measure, sw, true, "\t", true);
					ParticleMeasureIO.writeCSVperframe(measure, sw, true, "\t", true);
					EvSwingUtil.setClipBoardString(sw.getBuffer().toString());
					}
				catch (IOException e1)
					{
					EvLog.printError(e1);
					}
				}
			}
		}
	
	
	
	public String getHelpArticle()
		{
		return "Flow ParticleMeasure";
		}

	
	

	/******************************************************************************************************
	 * Plugin declaration
	 *****************************************************************************************************/
	public static void initPlugin() {}
	static
		{
		FlowUnitDeclaration decl=new FlowUnitDeclaration(CategoryInfo.name,"Show measures",metaType,FlowUnitShowMeasure.class, 
				CategoryInfo.icon,"Show the results of measure particle");
		Flow.addUnitType(decl);
		FlowType.registerSuggestCreateUnitInput(Boolean.class, decl);
		}

	}
