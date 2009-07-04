package endrov.flowBasic.math;

import endrov.flow.EvOpSlice1;
import endrov.imageset.EvPixels;
import endrov.imageset.EvPixelsType;

/**
 * max(A,b)
 */
public class EvOpMaxImageScalar extends EvOpSlice1
	{
	private final Number b;
	
	public EvOpMaxImageScalar(Number b)
		{
		this.b = b;
		}

	public EvPixels exec1(EvPixels... p)
		{
		return greater(p[0],b);
		}
	
	public static EvPixels greater(EvPixels a, Number b)
		{
		//Should use the common higher type here
		a=a.getReadOnly(EvPixelsType.INT);
		
		int tb=b.intValue();
		
		int w=a.getWidth();
		int h=a.getHeight();
		EvPixels out=new EvPixels(a.getType(),w,h);
		int[] aPixels=a.getArrayInt();
		int[] outPixels=out.getArrayInt();
		
		for(int i=0;i<aPixels.length;i++)
			outPixels[i]=aPixels[i]>tb ? aPixels[i] : tb;
		
		return out;
		}
	
	}