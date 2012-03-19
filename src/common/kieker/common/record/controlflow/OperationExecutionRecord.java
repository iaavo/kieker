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

package kieker.common.record.controlflow;

import kieker.common.record.AbstractMonitoringRecord;
import kieker.common.record.IMonitoringRecord;
import kieker.common.util.ClassOperationSignaturePair;

/**
 * String variables must not be null.
 * 
 * @author Andre van Hoorn, Jan Waller
 */
public final class OperationExecutionRecord extends AbstractMonitoringRecord implements IMonitoringRecord.Factory {
	private static final long serialVersionUID = -3963151278085958619L;
	private static final Class<?>[] TYPES = {
		int.class, // experimentId
		String.class, // operationSignature
		String.class, // sessionId
		long.class, // traceId
		long.class, // tin
		long.class, // tout
		String.class, // hostName
		int.class, // eoi
		int.class, // ess
	};
	private static final String DEFAULT_VALUE = "N/A";

	private volatile int experimentId = -1;
	private volatile String hostName = OperationExecutionRecord.DEFAULT_VALUE;
	private volatile String operationSignature = OperationExecutionRecord.DEFAULT_VALUE;
	private volatile String sessionId = OperationExecutionRecord.DEFAULT_VALUE;
	private volatile long traceId = -1;
	private volatile long tin = -1;
	private volatile long tout = -1;
	private volatile int eoi = -1;
	private volatile int ess = -1;

	/**
	 * Used by probes to store the return value of executed operations.
	 * The field is marked transient as it must not be serialized.
	 */
	private transient volatile Object retVal = null;

	/**
	 * Used by probes to intermediate information.
	 * The field is marked transient as it must not be serialized.
	 */
	private transient volatile boolean entryPoint = false;

	/**
	 * Returns an instance of OperationExecutionRecord.
	 * The member variables are initialized that way that only actually
	 * used variables must be updated.
	 */
	public OperationExecutionRecord() {
		// nothing to do
	}

	public OperationExecutionRecord(final String operationSignature, final long traceId) {
		this.operationSignature = operationSignature;
		this.traceId = traceId;
	}

	public OperationExecutionRecord(final String operationSignature, final long traceId, final long tin, final long tout) {
		this(operationSignature, traceId);
		this.tin = tin;
		this.tout = tout;
	}

	public OperationExecutionRecord(final String operationSignature, final long tin, final long tout) {
		this(operationSignature, -1, tin, tout);
	}

	// public OperationExecutionRecord(final String operationSignature, final String sessionId, final long traceId, final long tin, final long tout) {
	// this(operationSignature, traceId, tin, tout);
	// this.sessionId = sessionId;
	// }

	public OperationExecutionRecord(final String operationSignature, final String sessionId, final long traceId, final long tin, final long tout,
			final String vnName, final int eoi, final int ess) {
		this(operationSignature, traceId, tin, tout);
		this.sessionId = sessionId;
		this.hostName = vnName;
		this.eoi = eoi;
		this.ess = ess;
	}

	/**
	 * 
	 * @param componentName
	 * @param methodName
	 * @param traceId
	 * 
	 * @deprecated will be removed in Kieker 1.6
	 */
	@Deprecated
	public OperationExecutionRecord(final String componentName, final String methodName, final long traceId) {
		this.operationSignature = componentName + '.' + methodName;
		this.traceId = traceId;
	}

	/**
	 * 
	 * @param componentName
	 * @param opName
	 * @param traceId
	 * @param tin
	 * @param tout
	 * 
	 * @deprecated will be removed in Kieker 1.6
	 */
	@Deprecated
	public OperationExecutionRecord(final String componentName, final String opName, final long traceId, final long tin, final long tout) {
		this(componentName, opName, traceId);
		this.tin = tin;
		this.tout = tout;
	}

	/**
	 * 
	 * @param componentName
	 * @param opName
	 * @param tin
	 * @param tout
	 * 
	 * @deprecated will be removed in Kieker 1.6
	 */
	@Deprecated
	public OperationExecutionRecord(final String componentName, final String opName, final long tin, final long tout) {
		this(componentName, opName, -1, tin, tout);
	}

	/**
	 * 
	 * @param componentName
	 * @param opName
	 * @param sessionId
	 * @param traceId
	 * @param tin
	 * @param tout
	 * 
	 * @deprecated will be removed in Kieker 1.6
	 */
	@Deprecated
	public OperationExecutionRecord(final String componentName, final String opName, final String sessionId, final long traceId, final long tin, final long tout) {
		this(componentName, opName, traceId, tin, tout);
		this.sessionId = sessionId;
	}

	/**
	 * 
	 * @param componentName
	 * @param opName
	 * @param sessionId
	 * @param traceId
	 * @param tin
	 * @param tout
	 * @param vnName
	 * @param eoi
	 * @param ess
	 * 
	 * @deprecated will be removed in Kieker 1.6
	 */
	@Deprecated
	public OperationExecutionRecord(final String componentName, final String opName, final String sessionId, final long traceId, final long tin, final long tout,
			final String vnName, final int eoi, final int ess) {
		this(componentName, opName, sessionId, traceId, tin, tout);
		this.hostName = vnName;
		this.eoi = eoi;
		this.ess = ess;
	}

	public OperationExecutionRecord(final Object[] values) {
		AbstractMonitoringRecord.checkArray(values, OperationExecutionRecord.TYPES);
		try {
			this.experimentId = (Integer) values[0];
			this.operationSignature = (String) values[1]; // NOCS
			this.sessionId = (String) values[2]; // NOCS
			this.traceId = (Long) values[3]; // NOCS
			this.tin = (Long) values[4]; // NOCS
			this.tout = (Long) values[5]; // NOCS
			this.hostName = (String) values[6]; // NOCS
			this.eoi = (Integer) values[7]; // NOCS
			this.ess = (Integer) values[8]; // NOCS
		} catch (final Exception exc) { // NOCS (IllegalCatchCheck) // NOPMD
			throw new IllegalArgumentException("Failed to init", exc);
		}
	}

	@Override
	public final Object[] toArray() {
		return new Object[] {
			this.experimentId,
			this.operationSignature,
			(this.sessionId == null) ? "NULL" : this.sessionId, // NOCS
			this.traceId,
			this.tin,
			this.tout,
			(this.hostName == null) ? "NULLHOST" : this.hostName, // NOCS
			this.eoi,
			this.ess, };
	}

	@Override
	@Deprecated
	public void initFromArray(final Object[] values) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Class<?>[] getValueTypes() {
		return OperationExecutionRecord.TYPES.clone();
	}

	/**
	 * @return the experimentId
	 */
	public final int getExperimentId() {
		return this.experimentId;
	}

	/**
	 * @deprecated this class will become immutable in Kieker 1.6
	 * @param experimentId
	 *            the experimentId to set
	 */
	@Deprecated
	public final void setExperimentId(final int experimentId) {
		this.experimentId = experimentId;
	}

	/**
	 * @return the hostName
	 */
	public final String getHostName() {
		return this.hostName;
	}

	/**
	 * @deprecated this class will become immutable in Kieker 1.6
	 * 
	 * @param hostName
	 *            the hostName to set
	 */
	@Deprecated
	public final void setHostName(final String hostName) {
		this.hostName = hostName;
	}

	public String getOperationSignature() {
		return this.operationSignature;
	}

	/**
	 * @deprecated this class will become immutable in Kieker 1.6
	 * 
	 *             A string can, for example, be created using {@link ClassOperationSignaturePair#toOperationSignatureString()}.
	 * 
	 * @param operationSignature
	 */
	@Deprecated
	public void setOperationSignature(final String operationSignature) {
		this.operationSignature = operationSignature;
	}

	/**
	 * @return the className
	 * 
	 * @deprecated will be removed in Kieker 1.6
	 */
	@Deprecated
	public final String getClassName() {
		final ClassOperationSignaturePair classOpPair = ClassOperationSignaturePair.splitOperationSignatureStr(this.operationSignature);
		return classOpPair.getFqClassname();
	}

	/**
	 * Use {@link #setOperationSignature(String)} instead.
	 * 
	 * @param className
	 *            the className to set
	 * 
	 * @deprecated will be removed in Kieker 1.6. Also, this class will become immutable in Kieker 1.6.
	 */
	@Deprecated
	public final void setClassName(final String className) {
		throw new UnsupportedOperationException();
	}

	/**
	 * @return the operationName
	 * 
	 * @deprecated will be removed in Kieker 1.6
	 */
	@Deprecated
	public final String getOperationName() {
		final String name = this.getOperationSignature();
		final int posParen = name.lastIndexOf('(');
		int posDot;
		if (posParen != -1) {
			posDot = name.substring(0, posParen).lastIndexOf('.');
		} else {
			posDot = name.lastIndexOf('.');
		}
		if (posDot == -1) {
			return name;
		} else {
			return name.substring(posDot + 1);
		}
	}

	/**
	 * Use {@link #setOperationSignature(String)} instead.
	 * 
	 * @param operationName
	 *            the operationName to set
	 * 
	 * @deprecated will be removed in Kieker 1.6. Also, this class will become immutable in Kieker 1.6.
	 */
	@Deprecated
	public final void setOperationName(final String operationName) {
		throw new UnsupportedOperationException();
	}

	/**
	 * @return the sessionId
	 */
	public final String getSessionId() {
		return this.sessionId;
	}

	/**
	 * @deprecated this class will become immutable in Kieker 1.6
	 * 
	 * @param sessionId
	 *            the sessionId to set
	 */
	@Deprecated
	public final void setSessionId(final String sessionId) {
		this.sessionId = sessionId;
	}

	/**
	 * @return the traceId
	 */
	public final long getTraceId() {
		return this.traceId;
	}

	/**
	 * @deprecated this class will become immutable in Kieker 1.6
	 * 
	 * @param traceId
	 *            the traceId to set
	 */
	@Deprecated
	public final void setTraceId(final long traceId) {
		this.traceId = traceId;
	}

	/**
	 * @return the tin
	 */
	public final long getTin() {
		return this.tin;
	}

	/**
	 * @deprecated this class will become immutable in Kieker 1.6
	 * 
	 * @param tin
	 *            the tin to set
	 */
	@Deprecated
	public final void setTin(final long tin) {
		this.tin = tin;
	}

	/**
	 * @return the tout
	 */
	public final long getTout() {
		return this.tout;
	}

	/**
	 * @deprecated this class will become immutable in Kieker 1.6
	 * 
	 * @param tout
	 *            the tout to set
	 */
	@Deprecated
	public final void setTout(final long tout) {
		this.tout = tout;
	}

	/**
	 * @return the eoi
	 */
	public final int getEoi() {
		return this.eoi;
	}

	/**
	 * @deprecated this class will become immutable in Kieker 1.6
	 * 
	 * @param eoi
	 *            the eoi to set
	 */
	@Deprecated
	public final void setEoi(final int eoi) {
		this.eoi = eoi;
	}

	/**
	 * @return the ess
	 */
	public final int getEss() {
		return this.ess;
	}

	/**
	 * @deprecated this class will become immutable in Kieker 1.6
	 * 
	 * @param ess
	 *            the ess to set
	 */
	@Deprecated
	public final void setEss(final int ess) {
		this.ess = ess;
	}

	public Object getRetVal() {
		return this.retVal;
	}

	/**
	 * @deprecated this class will become immutable in Kieker 1.6
	 * 
	 * @param retVal
	 */
	@Deprecated
	public void setRetVal(final Object retVal) {
		this.retVal = retVal;
	}

	public boolean isEntryPoint() {
		return this.entryPoint;
	}

	/**
	 * @deprecated this class will become immutable in Kieker 1.6
	 * 
	 * @param entryPoint
	 */
	@Deprecated
	public void setEntryPoint(final boolean entryPoint) {
		this.entryPoint = entryPoint;
	}
}