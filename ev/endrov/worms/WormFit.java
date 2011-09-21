package endrov.worms;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

import javax.swing.JMenu;
import javax.vecmath.Vector2d;

import org.jdom.Element;

import endrov.data.EvContainer;
import endrov.data.EvObject;
import endrov.util.EvDecimal;

public class WormFit extends EvObject
	{

	public static class WormFrame
		{
		
		public List<Vector2d> centerPoints=new ArrayList<Vector2d>();
		public List<Double> thickness=new ArrayList<Double>();
		
		}
	
	public TreeMap<EvDecimal, WormFrame> frames=new TreeMap<EvDecimal, WormFit.WormFrame>();
	
	
	
	
	
	@Override
	public String saveMetadata(Element e)
		{
		// TODO Auto-generated method stub
		return null;
		}

	@Override
	public void loadMetadata(Element e)
		{
		// TODO Auto-generated method stub
		
		}

	@Override
	public String getMetaTypeDesc()
		{
		return "worm fit";
		}

	@Override
	public void buildMetamenu(JMenu menu, EvContainer parentObject)
		{
		// TODO Auto-generated method stub
		
		}

	@Override
	public EvObject cloneEvObject()
		{
		// TODO Auto-generated method stub
		return null;
		}

	}
