/***************************************************************************
 * Copyright 2014 Kicker Project (http://kicker-monitoring.net)
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
package kicker.monitoring.sampler.mxbean;

import java.lang.management.CompilationMXBean;
import java.lang.management.ManagementFactory;

import kicker.common.record.IMonitoringRecord;
import kicker.common.record.jvm.CompilationRecord;

/**
 * A sampler using the MXBean interface to access information about the compilation time. The sampler produces a {@link CompilationRecord} each time the
 * {@code sample} method is called.
 * 
 * @author Nils Christian Ehmke
 * 
 * @since 1.10
 */
public class CompilationSampler extends AbstractMXBeanSampler {

	public CompilationSampler() {
		// Empty default constructor
	}

	@Override
	protected IMonitoringRecord[] createNewMonitoringRecords(final long timestamp, final String hostname, final String vmName) {
		final CompilationMXBean compilationBean = ManagementFactory.getCompilationMXBean();
		return new IMonitoringRecord[] { new CompilationRecord(timestamp, hostname, vmName, compilationBean.getName(), compilationBean.getTotalCompilationTime()), };
	}
}