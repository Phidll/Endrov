/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.flowBasic.objects;

import java.awt.Color;
import java.util.Map;
import javax.swing.ImageIcon;

import org.jdom.Element;

import endrov.flow.Flow;
import endrov.flow.FlowExec;
import endrov.flow.FlowType;
import endrov.flow.FlowUnitBasic;
import endrov.flow.FlowUnitDeclaration;

/**
 * get "this", the flow object
 * @author Johan Henriksson
 *
 */
public class FlowUnitThisFlow extends FlowUnitBasic
	{
	private static final String metaType="getthisflow";
	private static final String showName="ThisFlow";
	
	/******************************************************************************************************
	 * Plugin declaration
	 *****************************************************************************************************/
	public static void initPlugin() {}
	static
		{
		Flow.addUnitType(new FlowUnitDeclaration(CategoryInfo.name,showName,metaType,FlowUnitThisFlow.class, null,"Get object that is this flow"));		
		}
	
	
	public String getBasicShowName(){return showName;}
	public ImageIcon getIcon(){return null;}	
	public Color getBackground(){return CategoryInfo.bgColor;}
	
	
	
	/** Get types of flows in */
	protected void getTypesIn(Map<String, FlowType> types, Flow flow)
		{
		}
	/** Get types of flows out */
	protected void getTypesOut(Map<String, FlowType> types, Flow flow)
		{
		types.put("this", new FlowType(Flow.class));
		}
	
	public String toXML(Element e){return metaType;}
	public void fromXML(Element e){}
	
	public void evaluate(Flow flow, FlowExec exec) throws Exception
		{
		Map<String,Object> lastOutput=exec.getLastOutput(this);
		lastOutput.put("this", flow);
		}
	
	}
	
