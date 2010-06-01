package endrov.recording.frapWindow;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JMenu;

import org.jdom.Element;

import endrov.basicWindow.BasicWindow;
import endrov.data.EvContainer;
import endrov.data.EvData;
import endrov.data.EvObject;
import endrov.data.EvPath;
import endrov.flow.Flow;
import endrov.flow.FlowConn;
import endrov.flowBasic.constants.FlowUnitConstEvDecimal;
import endrov.flowBasic.control.FlowUnitShow;
import endrov.flowBasic.objects.FlowUnitObjectIO;
import endrov.hardware.EvHardware;
import endrov.imageset.EvChannel;
import endrov.imageset.EvImage;
import endrov.imageset.EvStack;
import endrov.imageset.Imageset;
import endrov.recording.CameraImage;
import endrov.recording.HWImageScanner;
import endrov.recording.RecordingResource;
import endrov.roi.ROI;
import endrov.util.EvDecimal;

/**
 * 
 * 
 * @author Johan Henriksson
 *
 */
public class EvFRAPAcquisition extends EvObject
	{
	/******************************************************************************************************
	 *                               Static                                                               *
	 *****************************************************************************************************/
	
	private static final String metaType="frapAcq";
	

	/******************************************************************************************************
	 *                               Instance                                                             *
	 *****************************************************************************************************/
	
	
	
	public EvDecimal getRecoveryTime()
		{
		return recoveryTime;
		}

	public void setRecoveryTime(EvDecimal recoveryTime)
		{
		this.recoveryTime = recoveryTime;
		}

	public EvDecimal getBleachTime()
		{
		return bleachTime;
		}

	public void setBleachTime(EvDecimal bleachTime)
		{
		this.bleachTime = bleachTime;
		}

	public EvDecimal getRate()
		{
		return rate;
		}

	public void setRate(EvDecimal rate)
		{
		this.rate = rate;
		}

	public EvContainer getContainer()
		{
		return container;
		}

	public void setContainer(EvContainer container)
		{
		this.container = container;
		}

	public String getContainerStoreName()
		{
		return containerStoreName;
		}

	public void setContainerStoreName(String containerStoreName)
		{
		this.containerStoreName = containerStoreName;
		}

	public ROI getRoi()
		{
		return roi;
		}

	public void setRoi(ROI roi)
		{
		this.roi = roi;
		}
	
	private EvDecimal recoveryTime;
	private EvDecimal bleachTime;
	private EvDecimal rate;
	private EvContainer container;
	private String containerStoreName;
	private ROI roi;

	private List<Listener> listeners=new LinkedList<Listener>();
	
	/**
	 * Thread activity listener
	 */
	public interface Listener
		{
		public void acqStopped();
		}
	
	
	public void addListener(Listener l)
		{
		listeners.add(l);
		}

	public void removeListener(Listener l)
		{
		listeners.remove(l);
		}
	
	
	/**
	 * Thread to perform acquisition
	 */
	public class AcqThread extends Thread
		{
		private EvFRAPAcquisition settings;
		private boolean toStop=true;

		
		public boolean isRunning()
			{
			return !toStop || isAlive();
			}
		
		public void tryStop()
			{
			toStop=true;
			}
		
		private AcqThread(EvFRAPAcquisition settings)
			{
			this.settings=settings;
			}

		
		
		@Override
		public void run()
			{
			//TODO need to choose camera, at least!
			
			
			acqLoop: 
			do
				{
				Iterator<HWImageScanner> itcam=EvHardware.getDeviceMapCast(HWImageScanner.class).values().iterator();
				HWImageScanner cam=null;
				if(itcam.hasNext())
					cam=itcam.next();
				
				//Check that there are enough parameters
				if(cam!=null && container!=null)
					{

					Imageset imset=new Imageset();
					for(int i=0;;i++)
						if(container.getChild(containerStoreName+i)==null)
							{
							container.metaObject.put(containerStoreName+i, imset);
							imset.metaObject.put("ch", new EvChannel());
							break;
							}

					//TODO signal update on the object
					BasicWindow.updateWindows();

					
					EvDecimal curFrame=new EvDecimal(0);

					try
						{
						//Acquire image before bleaching
						snapOneImage(imset, cam, curFrame);
						BasicWindow.updateWindows();
						
						if(toStop)
							break acqLoop;
						
						//Bleach ROI
						double stageX=RecordingResource.getCurrentStageX();
						double stageY=RecordingResource.getCurrentStageY();
						String normalExposureTime=cam.getPropertyValue("Exposure");
						cam.setPropertyValue("Exposure", ""+bleachTime);
						int[] roiArray=RecordingResource.makeScanningROI(cam, roi, stageX, stageY);
						cam.scan(null, null, roiArray);
						cam.setPropertyValue("Exposure", normalExposureTime);
						curFrame=curFrame.add(settings.rate); //If frames are missed then this will suck. better base it on real time 

						
						System.out.println("rec time "+settings.recoveryTime.doubleValue());
						System.out.println("rate "+settings.rate.doubleValue());
						
						//Acquire images as the intensity recovers
						for(int i=0;i<settings.recoveryTime.doubleValue()/settings.rate.doubleValue();i++)
							{
							if(toStop)
								break acqLoop;
							
							curFrame=curFrame.add(settings.rate); //If frames are missed then this will suck. better base it on real time 
							
							snapOneImage(imset, cam, curFrame);
							BasicWindow.updateWindows();
							yield(settings.rate.doubleValue()/10);
							}
						
						}
					catch (Exception e)
						{
						e.printStackTrace();
						}
					
					////// Build flow to analyze this experiment
					Flow flow=new Flow();
					
					FlowUnitCalcFRAP frapUnit=new FlowUnitCalcFRAP();
					flow.units.add(frapUnit);
					
					FlowUnitObjectIO frapUnitGetChan=new FlowUnitObjectIO(new EvPath("ch"));
					FlowUnitObjectIO frapUnitGetROI=new FlowUnitObjectIO(new EvPath("roi"));
					FlowUnitConstEvDecimal frapUnitFrame=new FlowUnitConstEvDecimal(EvDecimal.ZERO);
					FlowUnitShow frapUnitShowLifetime=new FlowUnitShow();
					FlowUnitShow frapUnitShowMobile=new FlowUnitShow();
					FlowUnitShowGraph frapUnitShowSeries=new FlowUnitShowGraph();
					
					flow.units.add(frapUnitGetChan);
					flow.units.add(frapUnitGetROI);
					flow.units.add(frapUnitFrame);
					flow.units.add(frapUnitShowLifetime);
					flow.units.add(frapUnitShowMobile);
					flow.units.add(frapUnitShowSeries);

					flow.conns.add(new FlowConn(frapUnitGetChan,"out",frapUnit,"ch"));
					flow.conns.add(new FlowConn(frapUnitGetROI,"out",frapUnit,"roi"));
					flow.conns.add(new FlowConn(frapUnitFrame,"out",frapUnit,"t1"));
					flow.conns.add(new FlowConn(frapUnitFrame,"out",frapUnit,"t2"));
					flow.conns.add(new FlowConn(frapUnit,"lifetime",frapUnitShowLifetime,"in"));
					flow.conns.add(new FlowConn(frapUnit,"mobile",frapUnitShowMobile,"in"));
					flow.conns.add(new FlowConn(frapUnit,"series",frapUnitShowSeries,"in"));
					
					frapUnit.x=100;
					
					frapUnitFrame.y=0;
					frapUnitGetROI.y=30;
					frapUnitGetChan.y=60;

					frapUnitShowLifetime.x=300;
					frapUnitShowMobile.x=300;
					frapUnitShowSeries.x=300;
					
					frapUnitShowMobile.y=30;
					frapUnitShowSeries.y=60;

					/*EvOpCalcFRAP calc=new EvOpCalcFRAP(imset.getChannel("ch"), roi, EvDecimal.ZERO, EvDecimal.ZERO, "ch");
					System.out.println("lifetime "+calc.lifetime);
					System.out.println("initial conc "+calc.initialConcentration);
					System.out.println("mob frac "+calc.mobileFraction);
					System.out.println("curve "+calc.recoveryCurve);*/

					imset.metaObject.put("roi",roi.cloneBySerialize());
					imset.metaObject.put("flow",flow);
					
					BasicWindow.updateWindows();
					}
			

				}
			while(false);
		
		
		
		
		
			
			//System.out.println("---------stop-----------");
			toStop=false;
			for(Listener l:listeners)
				l.acqStopped();
			}
		
		/**
		 * To avoid busy loops
		 */
		private void yield(double t)
			{
			try
				{
				Thread.sleep((long)t);
				}
			catch (Exception e)
				{
				e.printStackTrace();
				}
			}
		
			
		private void snapOneImage(Imageset imset, HWImageScanner cam, EvDecimal curFrame)
			{
			CameraImage camIm=cam.snap();
			EvImage evim=new EvImage(camIm.getPixels()[0]);
			EvDecimal z=new EvDecimal(0);
			
			EvChannel ch=imset.getCreateChannel("ch");
			EvStack stack=ch.getCreateFrame(curFrame);
			stack.resX=RecordingResource.getCurrentTotalMagnification(cam);
			stack.resY=RecordingResource.getCurrentTotalMagnification(cam);
			stack.resZ=EvDecimal.ONE;
			//TODO displacement?
			
			stack.put(z, evim);
			}
		
		
		public void stopAcquisition()
			{
			toStop=true;
			}
		
		
		private void startAcquisition()
			{
			if(!isRunning())
				{
				toStop=false;
				start();
				}
			}
		
		}
	
	
	
	
	
	/**
	 * Get acquisition thread that links to this data
	 */
	public AcqThread startAcquisition()
		{
		AcqThread th=new AcqThread(this);
		th.startAcquisition();
		return th;
		}


	@Override
	public void buildMetamenu(JMenu menu)
		{
		}


	@Override
	public String getMetaTypeDesc()
		{
		return "FRAP acquisition";
		}


	@Override
	public void loadMetadata(Element e)
		{
		
		// TODO Auto-generated method stub
		
		}


	@Override
	public String saveMetadata(Element e)
		{
		//TODO
		/*
		Element eRate=new Element("rate");
		eRate.setAttribute("value",rate.toString());
		eRate.setAttribute("unit",rateUnit);
		e.addContent(eRate);
		
		Element eDur=new Element("duration");
		eDur.setAttribute("value",duration.toString());
		eDur.setAttribute("unit",durationUnit);
		e.addContent(eDur);
		*/
		return metaType;
		}
	
	
	/******************************************************************************************************
	 * Plugin declaration
	 *****************************************************************************************************/
	public static void initPlugin() {}
	static
		{
		EvData.supportedMetadataFormats.put(metaType,EvFRAPAcquisition.class);
		}
	
	}