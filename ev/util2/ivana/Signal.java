/***
 * Copyright (C) 2010 Johan Henriksson
 * This code is under the Endrov / BSD license. See www.endrov.net
 * for the full text and how to cite.
 */
package util2.ivana;

import java.io.File;
import java.io.IOException;

import endrov.core.EndrovCore;
import endrov.core.EndrovUtil;
import endrov.core.log.EvLog;
import endrov.core.log.EvLogStdout;
import endrov.data.EvData;
import endrov.flowThreshold.EvOpThresholdFukunaga2D;
import endrov.typeImageset.EvChannel;
import endrov.typeImageset.EvImagePlane;
import endrov.typeImageset.EvPixels;
import endrov.typeImageset.EvPixelsType;
import endrov.util.ProgressHandle;
import endrov.util.io.EvFileUtil;

public class Signal 
	{
	
	public static EvPixels getTheImage(EvData data)
		{
		if(data==null)
			System.out.println("No such file");
		EvChannel im=data.getIdObjectsRecursive(EvChannel.class).values().iterator().next();
		EvImagePlane evim=im.getFirstStack(new ProgressHandle()).getFirstPlane();
		EvPixels pixels=evim.getPixels(new ProgressHandle()).getReadOnly(EvPixelsType.DOUBLE);
		return pixels;
		}
	
	public static void doDirectory(String basedir, String basename)
		{
		ProgressHandle progh=new ProgressHandle();
		
		try
			{
			StringBuffer sb=new StringBuffer();
			
			for(int cf=2;/*cf<49*/;cf+=2)
				{
				
				File dicf=new File(basedir + basename+EndrovUtil.pad(cf, 4)+"-.tif");
				File sigf=new File(basedir + basename+EndrovUtil.pad(cf+1, 4)+"-.tif");
				
				//Stop if no more files
				if(!dicf.exists())
					break;
				
				System.out.println(dicf);
				EvPixels pixelsDic=getTheImage(EvData.loadFile(dicf));
				double[] pdic=pixelsDic.getArrayDouble();
				EvPixels pixelsSig=getTheImage(EvData.loadFile(sigf));
				double[] psig=pixelsSig.getArrayDouble();
				
				double dicThreshold=EvOpThresholdFukunaga2D.findThreshold(progh, pixelsDic,2)[0];

				int count=0;
				double sum=0;
				for(int i=0;i<pdic.length;i++)
					if(pdic[i]>dicThreshold)
						{
						count++;
						sum+=psig[i];
						}
				sum/=count;
				
				sb.append(cf+"\t"+sum+"\n");
				
				System.out.println("========================================Average "+sum+"\t\tthres "+dicThreshold);
				}
			
			EvFileUtil.writeFile(new File(basedir+"dat.txt"), sb.toString());
			
			}
		catch (IOException e)
			{
			e.printStackTrace();
			}
		}
	

	
	public static void main(String[] args)
		{
		EvLog.addListener(new EvLogStdout());
		EndrovCore.loadPlugins();
		
		doDirectory("/home/ivana/lab_data/lab_journal/2009/data2009_06/Nile_Red_20C/01062009_Nilered/AT2633/AT2633_NR/",
				"010609_AT2633_D1_NR-");
		
		System.exit(0);
		
		}
	}
