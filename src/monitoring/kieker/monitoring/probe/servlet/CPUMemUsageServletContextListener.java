/***************************************************************************
 * Copyright 2011 by
 *  + Christian-Albrechts-University of Kiel
 *    + Department of Computer Science
 *      + Software Engineering Group 
 *  and others.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ***************************************************************************/

package kieker.monitoring.probe.servlet;

import java.util.Collection;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import kieker.common.logging.Log;
import kieker.common.logging.LogFactory;
import kieker.monitoring.core.controller.IMonitoringController;
import kieker.monitoring.core.controller.MonitoringController;
import kieker.monitoring.core.sampler.ScheduledSamplerJob;
import kieker.monitoring.probe.sigar.ISigarSamplerFactory;
import kieker.monitoring.probe.sigar.SigarSamplerFactory;
import kieker.monitoring.probe.sigar.samplers.CPUsDetailedPercSampler;
import kieker.monitoring.probe.sigar.samplers.MemSwapUsageSampler;

/**
 * <p>
 * Starts and stops the periodic logging of CPU utilization and Memory usage employing the {@link SigarSamplerFactory} as the Servlet is initialized and destroyed
 * respectively. <br/>
 * The initial delay and the sampling period (both given in seconds) can be configured via context-params in the web.xml file, as shown below.
 * </p>
 * 
 * <p>
 * The integration and configuration in a web.xml file works as follows:<br/>
 * 
 * <pre>
 *  {@code
 *  <web-app>
 *  ...
 *  
 *  <context-param>
 *   <param-name>CPUMemUsageServletContextListener.samplingIntervalSeconds</param-name>
 *   <param-value>15</param-value>
 *  </context-param>
 *  
 *  <context-param>
 *   <param-name>CPUMemUsageServletContextListener.initialSamplingDelaySeconds</param-name>
 *   <param-value>0</param-value>
 *  </context-param>
 *  
 *  <listener>
 *    <listener-class>
 *     kieker.monitoring.probe.servlet.CPUMemUsageServletContextListener
 *    </listener-class>
 *  </listener>
 * 
 * ... 
 * </web-app>}
 * </pre>
 * 
 * </p>
 * 
 * @author Andre van Hoorn
 */
public class CPUMemUsageServletContextListener implements ServletContextListener {
	public static final long DEFAULT_SENSOR_INTERVAL_SECONDS = 15;
	public static final long DEFAULT_SENSOR_INITIAL_DELAY_SECONDS = 0;

	/** Prefix for parameters used in the web.xml file */
	// NOTE that this declaration must be BEFORE the following public constants!
	private static final String CONTEXT_PARAM_NAME_PREFIX = CPUMemUsageServletContextListener.class.getSimpleName();

	/** Parameter name for the sampling interval to be used in the web.xml file */
	public static final String CONTEXT_PARAM_NAME_SAMPLING_INTERVAL_SECONDS = CPUMemUsageServletContextListener.CONTEXT_PARAM_NAME_PREFIX
			+ ".samplingIntervalSeconds";
	/** Parameter name for the initial delay to be used in the web.xml file */
	public static final String CONTEXT_PARAM_NAME_INITIAL_SAMPLING_DELAY_SECONDS = CPUMemUsageServletContextListener.CONTEXT_PARAM_NAME_PREFIX
			+ ".initialSamplingDelaySeconds";

	private static final Log LOG = LogFactory.getLog(CPUMemUsageServletContextListener.class);

	private final IMonitoringController monitoringController = MonitoringController.getInstance();

	/**
	 * Stores the {@link ScheduledSamplerJob}s which are scheduled in {@link #contextInitialized(ServletContextEvent)} and removed from the
	 * scheduler in {@link #contextDestroyed(ServletContextEvent)}.
	 */
	private final Collection<ScheduledSamplerJob> samplerJobs = new CopyOnWriteArrayList<ScheduledSamplerJob>();

	private volatile long sensorIntervalSeconds = CPUMemUsageServletContextListener.DEFAULT_SENSOR_INTERVAL_SECONDS;
	private volatile long initialDelaySeconds = CPUMemUsageServletContextListener.DEFAULT_SENSOR_INITIAL_DELAY_SECONDS;

	public CPUMemUsageServletContextListener() {
		// nothing to do
	}

	public void contextDestroyed(final ServletContextEvent sce) {
		for (final ScheduledSamplerJob s : this.samplerJobs) {
			this.monitoringController.removeScheduledSampler(s);
		}
	}

	public void contextInitialized(final ServletContextEvent sce) {
		this.initParameters(sce.getServletContext());
		this.initSensors();
	}

	/**
	 * Initializes the variables {@link #sensorIntervalSeconds} and {@link #initialDelaySeconds} based on the values given in the web.xml
	 * file. If no parameter values are defined in the web.xml, the default
	 * values {@link #DEFAULT_SENSOR_INTERVAL_SECONDS} and {@link #DEFAULT_SENSOR_INITIAL_DELAY_SECONDS} are used.
	 * 
	 * @param c
	 *            the {@link ServletContext} providing access to the parameter
	 *            values via {@link ServletContext#getInitParameter(String)}
	 */
	private void initParameters(final ServletContext c) {
		if (c == null) {
			CPUMemUsageServletContextListener.LOG.warn("ServletContext == null");
			// we are using the default values assigned during variable
			// declaration.
			return;
		}

		// allowed values: Int>=0
		this.initialDelaySeconds = this.readLongInitParameter(c, CPUMemUsageServletContextListener.CONTEXT_PARAM_NAME_INITIAL_SAMPLING_DELAY_SECONDS,
				CPUMemUsageServletContextListener.DEFAULT_SENSOR_INITIAL_DELAY_SECONDS);

		// allows values: Int>0
		this.sensorIntervalSeconds = this.readLongInitParameter(c, CPUMemUsageServletContextListener.CONTEXT_PARAM_NAME_SAMPLING_INTERVAL_SECONDS,
				CPUMemUsageServletContextListener.DEFAULT_SENSOR_INTERVAL_SECONDS);
		if (this.sensorIntervalSeconds == 0) {
			CPUMemUsageServletContextListener.LOG.warn("values for the init-param '"
					+ CPUMemUsageServletContextListener.CONTEXT_PARAM_NAME_SAMPLING_INTERVAL_SECONDS + "' must be >0; found: " + this.sensorIntervalSeconds
					+ ". Using default value: "
					+ CPUMemUsageServletContextListener.DEFAULT_SENSOR_INTERVAL_SECONDS);
			this.sensorIntervalSeconds = CPUMemUsageServletContextListener.DEFAULT_SENSOR_INTERVAL_SECONDS;
		}

	}

	private long readLongInitParameter(final ServletContext c, final String paramName, final long defaultValue) {
		long val = -1;
		final String valStr = c.getInitParameter(paramName);

		if (valStr != null) {
			try {
				val = Long.parseLong(valStr);
			} catch (final NumberFormatException exc) {
				val = -1;
				// will use default value below.
			}
		}

		if (val < 0) {
			CPUMemUsageServletContextListener.LOG.warn("Invalid or missing value for context-param '" + paramName + "': " + valStr + ". Using default value: "
					+ defaultValue);
			val = defaultValue;
		}

		return val;
	}

	/**
	 * Creates and schedules the {@link ScheduledSamplerJob}s and stores them
	 * for later removal in the {@link Collection} {@link #samplerJobs}.
	 */
	private void initSensors() {
		final ISigarSamplerFactory sigarFactory = SigarSamplerFactory.INSTANCE;

		// Log utilization of each CPU every X seconds
		final CPUsDetailedPercSampler cpuSensor = sigarFactory.createSensorCPUsDetailedPerc();
		final ScheduledSamplerJob cpuSensorJob = this.monitoringController.schedulePeriodicSampler(cpuSensor, this.initialDelaySeconds, this.sensorIntervalSeconds,
				TimeUnit.SECONDS);
		this.samplerJobs.add(cpuSensorJob);

		// Log memory and swap statistics every Y seconds
		final MemSwapUsageSampler memSensor = sigarFactory.createSensorMemSwapUsage();
		final ScheduledSamplerJob memSensorJob = this.monitoringController.schedulePeriodicSampler(memSensor, this.initialDelaySeconds, this.sensorIntervalSeconds,
				TimeUnit.SECONDS);
		this.samplerJobs.add(memSensorJob);
	}
}
