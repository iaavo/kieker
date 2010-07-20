package kieker.analysis.datamodel;

/*
 * ==================LICENCE=========================
 * Copyright 2006-2010 Kieker Project
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
 * ==================================================
 */

/**
 *
 * @author Andre van Hoorn
 */
public class SynchronousReplyMessage extends Message {

    public SynchronousReplyMessage() {
        super();
    }

    public SynchronousReplyMessage(final long timestamp,
            final Execution sendingExecution,
            final Execution receivingExecution){
        super(timestamp, sendingExecution, receivingExecution);
    }

    @Override
    public boolean equals(Object obj) {
       if (!(obj instanceof SynchronousReplyMessage)){
            return false;
        }
        if (this == obj) {
            return true;
        }
        SynchronousReplyMessage other = (SynchronousReplyMessage)obj;

        return this.getTimestamp() == other.getTimestamp()
                && this.getSendingExecution().equals(other.getSendingExecution())
                && this.getReceivingExecution().equals(other.getReceivingExecution());
    }
}
