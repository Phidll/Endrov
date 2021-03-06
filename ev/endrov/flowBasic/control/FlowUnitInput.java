/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.flowBasic.control;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.util.*;

import javax.swing.ImageIcon;
import javax.swing.JOptionPane;

import org.jdom.Element;

import endrov.flow.Flow;
import endrov.flow.FlowExec;
import endrov.flow.FlowType;
import endrov.flow.FlowUnit;
import endrov.flow.FlowUnitDeclaration;
import endrov.flowBasic.RendererFlowUtil;
import endrov.windowFlow.FlowView;

/**
 * Flow unit: input variable
 * @author Johan Henriksson
 *
 */
public class FlowUnitInput extends FlowUnit
	{
	
	public String varName="foo";
	public FlowUnit varUnit;
	private static final String metaType="input";
	private static ImageIcon icon=new ImageIcon(FlowUnitInput.class.getResource("jhInput.png"));
	
	public String toXML(Element e)
		{
		e.setAttribute("varname", varName);
		return metaType;
		}

	public void fromXML(Element e)
		{
		varName=e.getAttributeValue("varname");
		}

	
	
	public Dimension getBoundingBox(Component comp, Flow flow)
		{
		int w=fm.stringWidth("In: "+varName);
		Dimension d=new Dimension(w+15,fonth);
		return d;
		}
	
	public void paint(Graphics g, FlowView panel, Component comp)
		{
		Dimension d=getBoundingBox(comp, panel.getFlow());

//		g.drawRect(x,y,d.width,d.height);
		
		int arcsize=8;
		
		g.setColor(RendererFlowUtil.colControl);
		g.fillRoundRect(x,y,d.width,d.height,arcsize,arcsize);
		g.setColor(getBorderColor(panel));
		g.drawRoundRect(x,y,d.width,d.height,arcsize,arcsize);
		g.setColor(getTextColor());
		g.drawString("In: "+varName, x+5, y+fonta);
		
		
		panel.drawConnPointRight(g,this,"out",x+d.width,y+d.height/2);
		
		}

	public boolean mouseHoverMoveRegion(int x, int y, Component comp, Flow flow)
		{
		Dimension dim=getBoundingBox(comp, flow);
		return x>=this.x && y>=this.y && x<=this.x+dim.width && y<=this.y+dim.height;
		}


	/** Get types of flows in */
	protected void getTypesIn(Map<String, FlowType> types, Flow flow)
		{
		}
	/** Get types of flows out */
	protected void getTypesOut(Map<String, FlowType> types, Flow flow)
		{
		types.put("out", null);
		}
	
	
	public void editDialog()
		{
		String newVal=JOptionPane.showInputDialog(null,"Enter value",varName);
		if(newVal!=null)
			varName=newVal;
		}


	
	public Collection<FlowUnit> getSubUnits(Flow flow)
		{
		return Collections.singleton((FlowUnit)this);
		}

	/**
	 * There is an invisible connector to in. Whatever executes the flow is responsible for setting this input
	 */
	public void evaluate(Flow flow, FlowExec exec) throws Exception
		{
		Map<String,Object> lastOutput=exec.getLastOutput(this);
		lastOutput.clear();
		lastOutput.put("out", exec.listener.getInputObject(varName));
		}

	public Component getGUIcomponent(FlowView p){return null;}
	public int getGUIcomponentOffsetX(Component comp, Flow flow){return 0;}
	public int getGUIcomponentOffsetY(Component c, Flow f){return 0;}
	
	public String getHelpArticle()
		{
		return "Scripting with flows";
		}


	/******************************************************************************************************
	 * Plugin declaration
	 *****************************************************************************************************/
	public static void initPlugin() {}
	static
		{
		Flow.addUnitType(new FlowUnitDeclaration(CategoryInfo.name,"Input",metaType,FlowUnitInput.class, icon,"Input value from flow executor"));
		}

	}
