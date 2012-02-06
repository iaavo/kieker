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

package kieker.tools.traceAnalysis.plugins.flow;

import java.util.HashMap;
import java.util.Map;

import kieker.analysis.plugin.port.InputPort;
import kieker.analysis.plugin.port.OutputPort;
import kieker.analysis.plugin.port.Plugin;
import kieker.analysis.repository.AbstractRepository;
import kieker.common.configuration.Configuration;
import kieker.common.record.flow.TraceEvent;
import kieker.tools.traceAnalysis.plugins.AbstractTimestampFilter;
import kieker.tools.traceAnalysis.systemModel.Execution;

/**
 * Allows to filter {@link TraceEvent} objects based on their {@link TraceEvent#getTimestamp()}s.<br>
 * 
 * This class has exactly one input port and one output port. It receives only objects inheriting from the class {@link Execution}. If the received object is within
 * the defined timestamps, the object is delivered unmodified to the output port.
 * 
 * @author Andre van Hoorn
 */
@Plugin(
		outputPorts = { @OutputPort(name = TraceIdFilter.OUTPUT_PORT_NAME, description = "Trace event output", eventTypes = { TraceEvent.class })
		})
public class TimestampFilter extends AbstractTimestampFilter {

	public static final String INPUT_PORT_NAME = "inputTraceEvent";
	public static final String OUTPUT_PORT_NAME = "defaultOutput";

	public static final String CONFIG_IGNORE_EXECUTIONS_BEFORE_TIMESTAMP = TimestampFilter.class.getName() + ".ignoreExecutionsBeforeTimestamp";
	public static final String CONFIG_IGNORE_EXECUTIONS_AFTER_TIMESTAMP = TimestampFilter.class.getName() + ".ignorExecutionsAfterTimestamp";

	public static final long MAX_TIMESTAMP = AbstractTimestampFilter.MAX_TIMESTAMP;
	public static final long MIN_TIMESTAMP = AbstractTimestampFilter.MIN_TIMESTAMP;

	/**
	 * Creates a new instance of the class {@link TimestampFilter} with the
	 * given parameters.
	 * 
	 * @param configuration
	 *            The configuration used to initialize this instance. It should
	 *            contain the properties for the minimum and maximum timestamp.
	 */
	public TimestampFilter(final Configuration configuration, final Map<String, AbstractRepository> repositories) {
		super(configuration, repositories,
				/* Load the content for the fields from the given configuration. */
				configuration.getLongProperty(TimestampFilter.CONFIG_IGNORE_EXECUTIONS_BEFORE_TIMESTAMP),
				configuration.getLongProperty(TimestampFilter.CONFIG_IGNORE_EXECUTIONS_AFTER_TIMESTAMP));
	}

	/**
	 * Creates a filter instance that only passes {@link TraceEvent} objects <i>e</i>
	 * with the property <i>e.timestamp &gt;= ignoreExecutionsBeforeTimestamp</i> and
	 * <i>e.timestamp &lt;= ignoreExecutionsAfterTimestamp</i>.
	 * 
	 * @param ignoreExecutionsBeforeTimestamp
	 * @param ignoreExecutionsAfterTimestamp
	 */
	public TimestampFilter(final long ignoreExecutionsBeforeTimestamp, final long ignoreExecutionsAfterTimestamp) {
		super(new Configuration(null), new HashMap<String, AbstractRepository>(), ignoreExecutionsBeforeTimestamp, ignoreExecutionsAfterTimestamp);
	}

	@InputPort(description = "Trace event input", eventTypes = { TraceEvent.class })
	public void inputTraceEvent(final Object data) {
		final TraceEvent event = (TraceEvent) data;
		if (this.inRange(event.getTimestamp())) {
			super.deliver(TimestampFilter.OUTPUT_PORT_NAME, event);
		}
	}

	@Override
	protected String getConfigurationPropertyIgnoreBeforeTimestamp() {
		return TimestampFilter.CONFIG_IGNORE_EXECUTIONS_BEFORE_TIMESTAMP;
	}

	@Override
	protected String getConfigurationPropertyIgnoreAfterTimestamp() {
		return TimestampFilter.CONFIG_IGNORE_EXECUTIONS_AFTER_TIMESTAMP;
	}
}