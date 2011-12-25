/**
 * This file is part of the hyk-proxy project.
 * Copyright (c) 2010 Yin QiWen <yinqiwen@gmail.com>
 *
 * Description: Config.java 
 *
 * @author yinqiwen [ 2010-5-14 | 08:49:33 PM]
 *
 */
package org.snova.framework.config;


import java.io.File;
import java.io.FileOutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.concurrent.TimeUnit;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.snova.framework.common.Constants;
import org.snova.framework.util.SharedObjectHelper;

/**
 *
 */
@XmlRootElement(name = "Configure")
public class DesktopFrameworkConfiguration implements FrameworkConfiguration
{
	protected static Logger logger = LoggerFactory.getLogger(DesktopFrameworkConfiguration.class);

	private static DesktopFrameworkConfiguration instance = null;

	private static long lastModifyTime = -1;
	static
	{
		loadConfig();
		SharedObjectHelper.getGlobalTimer().scheduleAtFixedRate(new ConfigurationFileMonitor(), 5, 5, TimeUnit.SECONDS);
	}
	
	static class ConfigurationFileMonitor implements Runnable
	{

		@Override
        public void run()
        {
			verifyConfigurationModified();
        }
		
	}
	
	private static File getConfigFile()
	{
		URL url = DesktopFrameworkConfiguration.class.getResource("/"
		        + Constants.CONF_FILE);
		String conf;
        try
        {
	        conf = URLDecoder.decode(url.getFile(), "UTF-8");
        }
        catch (UnsupportedEncodingException e)
        {
	        return null;
        }
		return new File(conf);
	}

	private static void loadConfig()
	{
		try
		{
			JAXBContext context = JAXBContext.newInstance(DesktopFrameworkConfiguration.class);
			Unmarshaller unmarshaller = context.createUnmarshaller();
			instance = (DesktopFrameworkConfiguration) unmarshaller.unmarshal(DesktopFrameworkConfiguration.class
			        .getResource("/" + Constants.CONF_FILE));			
			instance.init();
			String dest = DesktopFrameworkConfiguration.class
			        .getResource("/" + Constants.CONF_FILE).getFile();
			lastModifyTime = getConfigFile().lastModified();
		}
		catch (Exception e)
		{
			e.printStackTrace();
			logger.error("Failed to load default config file!", e);
		}
	}
	public void save()
	{
		try
		{
			init();
			JAXBContext context = JAXBContext.newInstance(DesktopFrameworkConfiguration.class);
			Marshaller marshaller = context.createMarshaller();
			marshaller.setProperty("jaxb.formatted.output", Boolean.TRUE);

			FileOutputStream fos = new FileOutputStream(getConfigFile());
			// fos.write("<!-- This is generated by hyk-proxy-client GUI, it's not the orignal conf file -->\r\n".getBytes());
			marshaller.marshal(this, fos);
			fos.close();
			lastModifyTime = getConfigFile().lastModified();
		}
		catch (Exception e)
		{
			logger.error("Failed to store framework config file!", e);
		}
	}

	@XmlElement(name = "LocalServer")
	private SimpleSocketAddress localProxyServerAddress = new SimpleSocketAddress(
	        "localhost", 48100);

	private int threadPoolSize = 30;

	@XmlElement(name = "ThreadPoolSize")
	public void setThreadPoolSize(int threadPoolSize)
	{
		this.threadPoolSize = threadPoolSize;
	}

	private String proxyEventServiceHandler = "GAE";

	public String getProxyEventHandler()
	{
		return proxyEventServiceHandler;
	}

	@XmlElement(name = "ProxyEventHandler")
	public void setProxyEventHandler(String handlerName)
	{
		this.proxyEventServiceHandler = handlerName;
	}
	
	private String pluginRepository = "";

	public String getPluginRepository()
    {
    	return pluginRepository;
    }

	@XmlElement(name = "PluginRepository")
	public void setPluginRepository(String pluginRepository)
    {
    	this.pluginRepository = pluginRepository;
    }

	public void init() throws Exception
	{

	}

	public SimpleSocketAddress getLocalProxyServerAddress()
	{
		return localProxyServerAddress;
	}

	public int getThreadPoolSize()
	{
		return threadPoolSize;
	}

	private DesktopFrameworkConfiguration()
	{
		// nothing
	}

	public static DesktopFrameworkConfiguration getInstance()
	{
		return instance;
	}
	
	public static boolean verifyConfigurationModified()
	{
		long ts = getConfigFile().lastModified();
		if(ts != lastModifyTime)
		{
			loadConfig();
			return true;
		}
		return false;
	}
}
