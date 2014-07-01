/***************************************************************************
 * Copyright 2012 Kieker Project (http://kieker-monitoring.net)
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

package kieker.tools.traceAnalysis.systemModel;

import java.util.Comparator;

import kieker.tools.traceAnalysis.systemModel.util.AbstractTraceStartTimestampComparator;

/**
 * Specialized sub-class for sessions which are derived from execution traces (see {@link ExecutionTrace}).
 * 
 * @author Holger Knoche
 * 
 */
public class ExecutionTraceBasedSession extends AbstractSession<ExecutionTrace> {

	/**
	 * Creates a new execution trace-based session with the given session ID.
	 * 
	 * @param sessionId
	 *            The session ID to use
	 */
	public ExecutionTraceBasedSession(final String sessionId) {
		super(sessionId);
	}

	@Override
	protected Comparator<? super ExecutionTrace> getOrderComparator() {
		return new AbstractTraceStartTimestampComparator();
	}

}