package org.apache.maven.resolver.internal.ant.guice;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;
import javax.inject.Singleton;

import com.google.inject.AbstractModule;
import com.google.inject.Binder;
import com.google.inject.Key;
import com.google.inject.Module;
import com.google.inject.Provides;
import com.google.inject.name.Names;
import org.apache.maven.model.building.DefaultModelBuilderFactory;
import org.apache.maven.model.building.ModelBuilder;
import org.apache.maven.repository.internal.DefaultArtifactDescriptorReader;
import org.apache.maven.repository.internal.DefaultVersionRangeResolver;
import org.apache.maven.repository.internal.DefaultVersionResolver;
import org.apache.maven.repository.internal.SnapshotMetadataGeneratorFactory;
import org.apache.maven.repository.internal.VersionsMetadataGeneratorFactory;
import org.apache.tools.ant.Project;
import org.eclipse.aether.impl.ArtifactDescriptorReader;
import org.eclipse.aether.impl.MetadataGeneratorFactory;
import org.eclipse.aether.impl.VersionRangeResolver;
import org.eclipse.aether.impl.VersionResolver;
import org.eclipse.aether.impl.guice.AetherModule;
import org.eclipse.sisu.Parameters;
import org.eclipse.sisu.bean.LifecycleModule;
import org.eclipse.sisu.inject.MutableBeanLocator;
import org.eclipse.sisu.wire.ParameterKeys;

/**
 * MavenResolverModule
 */
public final class AntTasksModule
    implements Module
{
    private final Map<?, ?> properties;

    private final Project project;

    public AntTasksModule( final Map<?, ?> properties, final Project project )
    {
        this.properties = properties;
        this.project = project;
    }

    @Override
    public void configure( final Binder binder )
    {
        binder.install( new LifecycleModule() );
        binder.bind( ParameterKeys.PROPERTIES ).toInstance( properties );
        binder.bind( ShutdownThread.class ).asEagerSingleton();

        binder.bind( Key.get( Project.class ) ).toInstance( project );
    }

    static final class ShutdownThread
        extends Thread
    {
        private final MutableBeanLocator locator;

        @Inject
        ShutdownThread( final MutableBeanLocator locator )
        {
            this.locator = locator;
            Runtime.getRuntime().addShutdownHook( this );
        }

        @Override
        public void run()
        {
            locator.clear();
        }
    }
}
