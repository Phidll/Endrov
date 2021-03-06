/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.flowBasic;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import endrov.typeImageset.EvPixels;
import endrov.typeImageset.EvPixelsType;

/**
 * Calculate histograms
 * @author Johan Henriksson
 *
 */
public class EvImageHistogram
	{
	private static <A> void inc(Map<A, Integer> m, A a)
		{
		Integer i=m.get(a);
		if(i==null)
			i=1;
		else
			i=i+1;
		m.put(a, i);
		}
	
	/**
	 * Given a histogram, generate a new one that is the cumulative count
	 */
	public static <A> SortedMap<A,Integer> makeHistCumulative(Map<A,Integer> in)
		{
		TreeMap<A, Integer> cumsum=new TreeMap<A, Integer>();
		Integer lastCount=0;
		for(Map.Entry<A, Integer> e:new TreeMap<A, Integer>(in).entrySet())
			{
			lastCount+=e.getValue();
			cumsum.put(e.getKey(), lastCount);
			}
		return cumsum;
		}
	
	/**
	 * Use the original type of the pixels later? simplifies extraction, less conversion, only one function,
	 * but the invoker will have less information.
	 * 
	 * NOTE! Does not return a sorted map. Invoker has to sort himself.
	 */
	public static Map<Integer,Integer> intHistogram(EvPixels in)
		{
		in=in.getReadOnly(EvPixelsType.INT);
		int[] inPixels=in.getArrayInt();
		Map<Integer,Integer> hist=new HashMap<Integer, Integer>(); 
		for(int p:inPixels)
			inc(hist,(Integer)p);
		return hist;
		}
	
	
	/**
	 * Use the original type of the pixels later? simplifies extraction, less conversion, only one function,
	 * but the invoker will have less information.
	 * 
	 * NOTE! Does not return a sorted map. Invoker has to sort himself.
	 */
	public static Map<Double,Integer> doubleHistogram(EvPixels in)
		{
		in=in.getReadOnly(EvPixelsType.DOUBLE);
		double[] inPixels=in.getArrayDouble();
		Map<Double,Integer> hist=new HashMap<Double, Integer>(); 
		for(double p:inPixels)
			inc(hist,(Double)p);
		return hist;
		}
	

	//Will we be forced to reification by metaprog ourselves?
	//A<B> will generate AiiiB as a subclass. instanceof will be
	//possible on many levels. AiiiB.getClassAiii1() could return B.class
	
	public static boolean isIntegerHist(Map<?,?> m)
		{
		Iterator<?> it=m.keySet().iterator();
		if(it.hasNext() && !(it.next() instanceof Integer))
			return false;
		return true;
		}
	
	}
