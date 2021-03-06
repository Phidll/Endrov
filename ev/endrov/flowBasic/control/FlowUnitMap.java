/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.flowBasic.control;

import java.awt.Component;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import javax.swing.ImageIcon;

import org.jdom.Element;

import endrov.flow.Flow;
import endrov.flow.FlowExec;
import endrov.flow.FlowType;
import endrov.flow.FlowUnitContainer;
import endrov.flow.FlowUnitDeclaration;
import endrov.windowFlow.FlowView;

/**
 * Flow unit: Map
 * 
 * -> in in' ----- out' out ->
 * 
 * @author Johan Henriksson
 *
 */
public class FlowUnitMap extends FlowUnitContainer
	{
	private static final String metaType="map";

	private static ImageIcon icon=new ImageIcon(FlowUnitMap.class.getResource("jhMap.png"));
	
	
	public String getContainerName()
		{
		return "map";
		}

	public String toXML(Element e)
		{
		e.setAttribute("w",""+contw);
		e.setAttribute("h",""+conth);
		return metaType;
		}
	public void fromXML(Element e)
		{
		contw=Integer.parseInt(e.getAttributeValue("w"));
		conth=Integer.parseInt(e.getAttributeValue("h"));
		}

	
	/** Get types of flows in */
	protected void getTypesIn(Map<String, FlowType> types, Flow flow)
		{
		types.put("in", null);
		types.put("out", null);
		}
	/** Get types of flows out */
	protected void getTypesOut(Map<String, FlowType> types, Flow flow)
		{
		types.put("in'", null);
		types.put("out'", null);
		}

	public Set<String> getInsideConns()
		{
		HashSet<String> s=new HashSet<String>();
		s.add("in'");
		s.add("out'");
		return s;
		}
	
	
	public void editDialog(){}


	public void evaluate(Flow flow, FlowExec exec) throws Exception
		{
	//	Map<String,Object> lastOutput=exec.getLastOutput(this);
		//TODO flowunit
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
		Flow.addUnitType(new FlowUnitDeclaration(CategoryInfo.name,"Map",metaType,FlowUnitMap.class, icon,"Operate on all values element by element"));
		}

	}
