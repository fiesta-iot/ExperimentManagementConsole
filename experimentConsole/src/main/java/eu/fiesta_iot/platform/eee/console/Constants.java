/*
===========================================================
Experiment Management Console
Copyright (C) 2018  Authors: Rachit Agarwal.

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program.  If not, see <https://www.gnu.org/licenses/>.

contact: rachit.agarwal@inria.fr 
===========================================================
*/
package eu.fiesta_iot.platform.eee.console;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Constants {
	private static final Object _sync = new Object();

	private static Properties props;

	public static String realPath;
	

	static Logger log = LoggerFactory.getLogger(Constants.class);
	
	public static String getPath() {
		return getPropVal("eee.console.PATH");
	}
	
	public static String getERMPath() {
		return getPropVal("eee.console.ERM_PATH");
	}
	
	public static String getERMAllUserExperimentsDescription() {
		//log.info("path=:"+getERMPath()+getPropVal("eee.console.ERM_API_GETAllUSEREXPERIMENTSDESCRIPTIONS"));
		return getERMPath()+getPropVal("eee.console.ERM_API_GETAllUSEREXPERIMENTSDESCRIPTIONS");
	}
	
	public static String getExperimentDescription() {
		return getERMPath()+getPropVal("eee.console.ERM_API_GETEXPERIMENTDESCRIPTION");
	}
	
	public static String getDownloadLink() {
		return getPropVal("eee.console.ERS_API_LINK");
	}
	public static String getERMServiceModelPath() {
		return getERMPath()+getPropVal("eee.console.ERM_API_GETEXPERIMENTSERVICEMODELOBJECT");
	}
	public static String getERMDiscoverableFismoPath() {
		return getERMPath()+getPropVal("eee.console.ERM_API_DISCOVERABLE");
	}
	public static String getEEEPath() {
		return getPropVal("eee.console.EEE_PATH");
	}
	public static String getJobIDFismoUserFemoGiven() {
		return getEEEPath()+getPropVal("eee.console.EEE_API_GETJOBIDFISMOUSERFEMO");
	}
	public static String getScheduler() {
		return getEEEPath()+getPropVal("eee.console.EEE_API_SCHEDULE");
	}
	public static String getJobStatus() {
		return getEEEPath()+getPropVal("eee.console.EEE_API_GETJOBSTATUS");
	}
	public static String getJobIDLog() {
		return getEEEPath()+getPropVal("eee.console.EEE_API_GETJOBIDLOG");
	}
	public static String getPolling() {
		return getEEEPath()+getPropVal("eee.console.EEE_API_POLLING");
	}
	public static String getEEEDeleteFISMO() {
		return getEEEPath()+getPropVal("eee.console.EEE_API_DELETEFISMO");
	}
	public static String getRestart() {
		return getEEEPath()+getPropVal("eee.console.EEE_API_RESTART");
	}
	public static String getStop() {
		return getEEEPath()+getPropVal("eee.console.EEE_API_STOP");
	}
	public static String getExperimenterSubscriptions() {
		return getEEEPath()+getPropVal("eee.console.EEE_API_GETEXPERIMENTERSUBSCRIPTIONS");
	}
	public static String getUnSubscriptions() {
		return getEEEPath()+getPropVal("eee.console.EEE_API_UNSUBSCRIBE");
	}
	public static String getSubscriptions() {
		return getEEEPath()+getPropVal("eee.console.EEE_API_SUBSCRIBE");
	}
	public static String getSecurityUserAPI() {
		return getPropVal("eee.console.SECURITY_API_GETUSER");
	}

	private static String getPropVal(String key) {
		if (props == null) load();
		if (props == null) return null;
		return props.getProperty(key);
	}

	private static void load() {
		InputStream input = null;
		if (props != null) return;
		synchronized (_sync) {
			if (props == null) {
				try {
					//InputStream input = Constants.class.getResourceAsStream("/Properties.properties");
					String PROPERTIES_FILE = "fiesta-iot.properties";  
	
					String jbosServerConfigDir = System.getProperty("jboss.server.config.dir");
					String fiestaIotConfigFile = jbosServerConfigDir + File.separator + PROPERTIES_FILE;
					input = new FileInputStream(fiestaIotConfigFile);
					
					if (input != null){
						props = new Properties();
						props.load(input);
					}
				} catch (IOException e) {
					log.error("Properties file not found");//e.printStackTrace();
				} finally {
					if (input != null) {
						try {
							input.close();
						} catch (IOException e) {
							log.error("",e);//e.printStackTrace();
						}
					}
				}
			}
		}
	}
	
	private Constants() {
	}
}
