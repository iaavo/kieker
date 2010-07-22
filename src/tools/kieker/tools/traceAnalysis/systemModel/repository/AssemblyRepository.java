package kieker.tools.traceAnalysis.systemModel.repository;

import java.util.Collection;
import java.util.Hashtable;
import kieker.tools.traceAnalysis.systemModel.AssemblyComponent;
import kieker.tools.traceAnalysis.systemModel.ComponentType;

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
public class AssemblyRepository extends AbstractSystemSubRepository{
    private final Hashtable<String, AssemblyComponent>
            assemblyComponentInstancesByName =
            new Hashtable<String, AssemblyComponent>();
    private final Hashtable<Integer, AssemblyComponent>
            assemblyComponentInstancesById = new Hashtable<Integer, AssemblyComponent>();

    public final AssemblyComponent rootAssemblyComponent;

    public AssemblyRepository(final SystemModelRepository systemFactory,
            final AssemblyComponent rootAssemblyComponent){
        super(systemFactory);
        this.rootAssemblyComponent = rootAssemblyComponent;
    }

   /**
    * Returns the instance for the passed ID; null if no instance
     *  with this ID.
     */
    public final AssemblyComponent getAssemblyComponentById(
            final int containerId){
        return this.assemblyComponentInstancesById.get(containerId);
    }

    /** Returns the instance for the passed factoryIdentifier; null if no instance
     *  with this factoryIdentifier.
     */
    public final AssemblyComponent getAssemblyComponentInstanceByNamedIdentifier(final String namedIdentifier){
        return this.assemblyComponentInstancesByName.get(namedIdentifier);
    }

    public final AssemblyComponent createAndRegisterAssemblyComponentInstance(
            final String namedIdentifier,
            final ComponentType componentType){
            AssemblyComponent newInst;
            if (this.assemblyComponentInstancesByName.containsKey(namedIdentifier)){
                throw new IllegalArgumentException("Element with name " + namedIdentifier + "exists already");
            }
            int id = this.getAndIncrementNextId();
            newInst = new AssemblyComponent(id, "@"+id, componentType);
            this.assemblyComponentInstancesById.put(id, newInst);
            this.assemblyComponentInstancesByName.put(namedIdentifier, newInst);
            return newInst;
    }

    public final Collection<AssemblyComponent> getAssemblyComponentInstances(){
        return this.assemblyComponentInstancesById.values();
    }
}
