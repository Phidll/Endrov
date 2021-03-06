/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package endrov.flow;


import java.io.File;

import endrov.typeImageset.EvChannel;
import endrov.typeImageset.EvImagePlane;
import endrov.typeImageset.EvImageReader;
import endrov.typeImageset.EvPixels;
import endrov.typeImageset.EvStack;
import endrov.util.ProgressHandle;
import endrov.util.lazy.MemoizeX;
import endrov.util.math.EvDecimal;

/**
 * Image operation defined by operation on stacks
 * @author Johan Henriksson
 *
 */
public abstract class EvOpStack extends EvOpGeneral
	{
	//By necessity, stack operators have to deal with laziness manually.
	//Example: avgZ only computes one slice and then duplicates it. other operands compute entire
	//stack. cannot fit together. possible to make functions beneath this.
	public EvPixels[] exec(ProgressHandle ph, EvPixels... p)
		{
		//TODO only one pixel supported
		//TODO where is lazyness? where is events?
		EvImagePlane im=new EvImagePlane();
		im.setPixelsReference(p[0]);
		EvStack stack[]=new EvStack[]{new EvStack()};
		stack[0].putPlane(0, im);
		stack=exec(ph, stack);
		EvPixels[] ret=new EvPixels[stack.length];
		for(int ac=0;ac<ret.length;ac++)
			ret[ac]=stack[ac].getPlane(0).getPixels(ph);
		return ret;
		}
		

	public EvChannel[] exec(ProgressHandle ph, EvChannel... ch)
		{
		System.out.println("here1");
		return applyStackOpOnChannelsSameSize(ph, ch, this);
		}
	
	public EvPixels exec1(ProgressHandle ph, EvPixels... p)
		{
		return exec(ph, p)[0];
		}

	public EvStack exec1(ProgressHandle ph, EvStack... p)
		{
		return exec(ph, p)[0];
		}
	
	public EvChannel exec1(ProgressHandle ph, EvChannel... ch)
		{
		return exec(ph, ch)[0];
		}

	
	/**
	 * Lazily create a channel using an operator that combines input channels.
	 * For this function to work, the output channel must have the same resolution and size
	 * as the input channels
	 * 
	 * Should ONLY be used for EvOpStack and EvOpStack1 
	 * (why?)
	 * 
	 */
	public static EvChannel[] applyStackOpOnChannelsSameSize(ProgressHandle progh, EvChannel[] ch, EvOpGeneral op)
		{
		//System.out.println("here3 ");

		int numInputChannels=ch.length;
		int numOutputChannels=op.getNumberChannels();
		
		//Not quite final: what if changes should go back into the channel? how?
		EvChannel[] retch=new EvChannel[numOutputChannels];
		
		//First argument decides which frames to apply for
		EvChannel refChannel=ch[0];
		
		for(int curOutputChanIndex=0;curOutputChanIndex<retch.length;curOutputChanIndex++)
			{
			EvChannel curReturnChan=new EvChannel();
			retch[curOutputChanIndex]=curReturnChan;
			
			//How to combine channels? if A & B, B not exist, make B black?
			
			//TODO If there is only one channel in each, combine these?
			
			//Operates on common subset of channels
			for(EvDecimal channelEntryFrame:refChannel.getFrames())
				{
				final EvStack curInputStack=refChannel.getStack(progh, channelEntryFrame);
				final EvStack curReturnStack=new EvStack();
				curReturnStack.copyMetaFrom(curInputStack);
				curReturnChan.putStack(channelEntryFrame, curReturnStack);
				
				//TODO register lazy operation
				
				final EvStack[] imlist=new EvStack[numInputChannels];
				int ci=0;
				for(EvChannel cit:ch)
					{
					imlist[ci]=cit.getStack(progh, channelEntryFrame);
					ci++;
					}
				

				//Apply operation lazily
				final MemoizeX<EvStack[]> ms=new MemoizeExecStack(imlist, op);
				for(EvChannel cit:ch)
					ms.dependsOn(cit.getStackLazy(channelEntryFrame));
				
				//TODO without lazy stacks, prior stacks are forced to be evaluated.
				//only fix is if the laziness is added directly at the source.
				/////////////// TODO adding lazy stacks!!!!!!!!!!!!!!!!
				
				final int finalCurReturnChanIndex=curOutputChanIndex;
				

				for(int az=0;az<curInputStack.getDepth();az++)
					{
					EvImagePlane newim=new EvImagePlane();
					curReturnStack.putPlane(az, newim);
					
					curReturnStack.copyMetaFrom(curInputStack); 
					//TODO This design makes it impossible to generate resolution lazily
					//TODO in particular, crop will not work nicely
					
					final int finalAz=az;	
					
					newim.io=new EvImageReader()
						{
						public EvPixels eval(ProgressHandle progh)
							{
							try
								{
								EvStack[] chans=ms.get(progh);
								if(finalCurReturnChanIndex>=chans.length)
									throw new RuntimeException("Trying to use index "+finalCurReturnChanIndex+" but there are only "+chans.length+" entries in chans");
								EvStack stack=chans[finalCurReturnChanIndex];
								if(stack==null)
									throw new RuntimeException("EvOp programming error: got null stack");
								EvImagePlane evim=stack.getPlane(finalAz);
								if(evim==null)
									throw new RuntimeException("There is no image for this z: "+finalAz);
								return evim.getPixels(progh);
								}
							catch (Exception e)
								{
								e.printStackTrace();
								System.out.println("want to get z: "+finalAz);
								System.out.println("index "+finalCurReturnChanIndex);
								throw new RuntimeException("failed in lazy execution");
								}
							}
						public File getRawJPEGData()
							{
							return null;
							}
						};
					newim.io.dependsOn(ms);
					
					newim.registerLazyOp(ms);		
					}
				
				}
			}
		return retch;
		}

	
	
	
	/**
	 * Lazily create a channel using an operator that combines input channels.
	 * For this function to work, the output channel must have the same resolution and size
	 * as the input channels
	 * 
	 * This function contains an optimization such that not all planes must be immediately generated, but
	 * rather it is done on a stack level
	 * 
	 * Should ONLY be used for EvOpStack and EvOpStack1 
	 * (why?)
	 * 
	 */
	public static EvChannel[] applyStackOpOnChannelsDifferentSize(ProgressHandle progh, EvChannel[] ch, EvOpGeneral op)
		{
		int numInputChannels=ch.length;
		int numOutputChannels=op.getNumberChannels();

		//Create output channels
		EvChannel[] retch=new EvChannel[numOutputChannels];
		for(int curOutputChanIndex=0;curOutputChanIndex<retch.length;curOutputChanIndex++)
			{
			EvChannel curReturnChan=new EvChannel();
			retch[curOutputChanIndex]=curReturnChan;
			}

		
		//First argument decides which frames to apply for
		EvChannel refChannel=ch[0];
		
			
		//Operates on common subset of channels
		for(EvDecimal channelEntryFrame:refChannel.getFrames())
			{
			final EvStack[] imlist=new EvStack[numInputChannels];
			int ci=0;
			for(EvChannel cit:ch)
				{
				imlist[ci]=cit.getStack(progh, channelEntryFrame);
				ci++;
				}
			

			
			EvStack[] out=op.exec(progh, imlist);
			
			for(int curOutputChanIndex=0;curOutputChanIndex<retch.length;curOutputChanIndex++)
				retch[curOutputChanIndex].putStack(channelEntryFrame, out[curOutputChanIndex]);
			}
		return retch;
		}

	
	
	private static class MemoizeExecStack extends MemoizeX<EvStack[]>
		{
		private final EvStack[] imlist;
		private final EvOpGeneral op;
		
		public MemoizeExecStack(EvStack[] imlist, EvOpGeneral op)
			{
			this.imlist = imlist;
			this.op = op;
			}

		@Override
		protected EvStack[] eval(ProgressHandle ph)
			{
			System.out.println("---- applying stack op over channel ------");
			EvStack[] ret=op.exec(ph, imlist);
			if(ret==null)
				throw new RuntimeException("EvOp programming error (2): Stack operation returns null array of channels");
			return ret;
			}
	
		}
	
	}