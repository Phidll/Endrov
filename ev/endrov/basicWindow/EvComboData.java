package endrov.basicWindow;

import java.util.LinkedList;

import endrov.data.EvContainer;
import endrov.data.EvObject;


/**
 * Combo that only shows EvData
 * @author Johan Henriksson
 *
 */
public class EvComboData extends EvComboObject
	{
	static final long serialVersionUID=0;

	
	public EvComboData(boolean allowNoSelection)
		{
		super(new LinkedList<EvObject>(), false, allowNoSelection);
		}
	
	public boolean includeObject(EvContainer cont)
		{
		return true;
		}


	}