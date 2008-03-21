package evplugin.ev;

import java.io.*;
import java.lang.reflect.*;
import java.net.*;
import java.util.*;
import javax.swing.*;

/**
 * Load plugins. Every plugin is located in evplugin/.../ and has a PLUGIN.java describing how to load it.
 * 
 * @author Johan Henriksson
 */
public class PluginInfo
	{
	public String filename;
	public PluginDef pdef=null;
	

	
	/**
	 * Check if there was a plugin in the directory
	 */
	public boolean exists()
		{
		return pdef!=null;
		}

	/**
	 * Read directory for plugin information
	 * @param filename
	 */
	public PluginInfo(String filename)
		{
		this.filename=filename;
		pdef=null;
		try
			{
			String classPath=(new File(filename)).getAbsolutePath().
			substring(-1+(new File(".")).getAbsolutePath().length()).replace('/', '.').replace('\\','.');
			Class<?> theClass=Class.forName(classPath+".PLUGIN");
			Constructor<?> constr=theClass.getConstructor(new Class<?>[]{});
			pdef=(PluginDef)constr.newInstance(new Object[]{});
			}
		catch (Exception e)
			{
			}
		}
	
	
	/**
	 * Load the plugin
	 */
	public void load()
		{
		if(exists())
			{
			if(pdef.systemSupported())
				{
				Log.printLog("Loading plugin "+pdef.getPluginName());
				for(Class<?> foo:pdef.getInitClasses())
					{
					try
						{
						Method m=foo.getDeclaredMethod("initPlugin", new Class[]{});
						m.invoke(foo, new Object[]{});
						}
					catch (Exception e)
						{
						e.printStackTrace();
						JOptionPane.showMessageDialog(null, "Could not load EV; plugin "+pdef.getPluginName()+" broken\n"+
								MemoryLog.logPrintString(e));
						}
					}
				}
			else
				Log.printLog("Skipping unsupported plugin "+pdef.getPluginName());
			}
		}
	


	
	public String toString()
		{
		if(pdef!=null)
			return pdef.getPluginName();
		else
			return filename;
		}

	/**
	 * Get a list of all plugins
	 */
	public static List<PluginInfo> getPluginList()
		{
		final List<PluginInfo> p=new LinkedList<PluginInfo>();
		if(storedInJar())
			{
			try
				{
				InputStream input = EV.class.getResourceAsStream("pluginlist.txt");
				BufferedReader br = new BufferedReader(new InputStreamReader(input));
				String strLine;
				while ((strLine = br.readLine()) != null)
					p.add(new PluginInfo(strLine));
				}
			catch (Exception e)
				{
				JOptionPane.showMessageDialog(null, "Applet does not have a plugin listing");
				}
			}
		else
			{
			File pluginDir=new File("evplugin");
			if(!pluginDir.exists())
				JOptionPane.showMessageDialog(null, "Plugin directory does not exist!");
			else
				for(File subdir:pluginDir.listFiles())
					{
					String plugin="evplugin/"+subdir.getName();
					PluginInfo pi=new PluginInfo(plugin);
					if(pi.exists())
						p.add(pi);
					}
			}
		return p;
		}

	/**
	 * Check if EV is stored in a JAR-file
	 */
	public static boolean storedInJar()
		{
		URL url=EV.class.getResource("pluginlist.txt");
		return url!=null && url.toString().indexOf(".jar!")!=-1;
		}
	

	}
