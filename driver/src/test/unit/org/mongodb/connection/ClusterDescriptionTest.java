/*
 * Copyright (c) 2008 - 2013 10gen, Inc. <http://10gen.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.mongodb.connection;

import org.junit.Test;

import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mongodb.connection.ClusterConnectionMode.Discovering;
import static org.mongodb.connection.ClusterType.ReplicaSet;
import static org.mongodb.connection.ServerConnectionState.Connected;
import static org.mongodb.connection.ServerConnectionState.Connecting;
import static org.mongodb.connection.ServerType.ReplicaSetPrimary;
import static org.mongodb.connection.ServerType.StandAlone;

public class ClusterDescriptionTest {
    @Test
    public void testMode() {
        ClusterDescription description = new ClusterDescription(Collections.<ServerDescription>emptyList(), Discovering);
        assertEquals(Discovering, description.getMode());
    }

    @Test
    public void testEmptySet() {
        ClusterDescription description = new ClusterDescription(Collections.<ServerDescription>emptyList(), Discovering);
        assertTrue(description.getAll().isEmpty());
    }

    @Test
    public void testType() throws UnknownHostException {
        ClusterDescription description = new ClusterDescription(Arrays.asList(
                ServerDescription.builder().state(Connected).address(new ServerAddress("loc:27017")).type(ReplicaSetPrimary).build()),
                Discovering);
        assertEquals(ReplicaSet, description.getType());

        description = new ClusterDescription(Arrays.asList(
                ServerDescription.builder().state(Connecting).address(new ServerAddress("loc:27017")).build(),
                ServerDescription.builder().state(Connected).address(new ServerAddress("loc:27018")).type(ReplicaSetPrimary).build()),
                Discovering);
        assertEquals(ReplicaSet, description.getType());

        description = new ClusterDescription(Arrays.asList(
                ServerDescription.builder().state(Connected).address(new ServerAddress("loc:27017")).type(StandAlone).build(),
                ServerDescription.builder().state(Connected).address(new ServerAddress("loc:27018")).type(ReplicaSetPrimary).build()),
                Discovering);
        assertEquals(ClusterType.Mixed, description.getType());

        description = new ClusterDescription(Arrays.asList(
                ServerDescription.builder().state(Connected).address(new ServerAddress("loc:27017")).type(StandAlone).build(),
                ServerDescription.builder().state(Connected).address(new ServerAddress("loc:27018")).type(StandAlone).build()),
                Discovering);
        assertEquals(ClusterType.Mixed, description.getType());

        description = new ClusterDescription(Arrays.asList(
                ServerDescription.builder().state(Connected).address(new ServerAddress("loc:27017")).type(StandAlone).setName("test2")
                        .build(),
                ServerDescription.builder().state(Connected).address(new ServerAddress("loc:27018")).type(StandAlone).build()),
                Discovering, "test1");
        assertEquals(ClusterType.Mixed, description.getType());

        description = new ClusterDescription(Arrays.asList(
                ServerDescription.builder().state(Connected).address(new ServerAddress("loc:27017")).type(ReplicaSetPrimary).setName("t1")
                        .build(),
                ServerDescription.builder().state(Connected).address(new ServerAddress("loc:27018")).type(ReplicaSetPrimary).setName("t2")
                        .build()),
                Discovering);
        assertEquals(ClusterType.Mixed, description.getType());
    }

    @Test
    public void testReplicaSetName() {
        ClusterDescription description = new ClusterDescription(Arrays.asList(
                ServerDescription.builder().state(Connected).address(new ServerAddress("loc:27017")).type(ReplicaSetPrimary).setName("t1")
                        .build()),
                Discovering);
        assertEquals("t1", description.getReplicaSetName());

        description = new ClusterDescription(Arrays.asList(
                ServerDescription.builder().state(Connected).address(new ServerAddress("loc:27017")).type(ReplicaSetPrimary).setName("t1")
                        .build()),
                Discovering, "t2");
        assertNull(description.getReplicaSetName());

        description = new ClusterDescription(Arrays.asList(
                ServerDescription.builder().state(Connected).address(new ServerAddress("loc:27017")).type(ReplicaSetPrimary).setName("t1")
                        .build(),
                ServerDescription.builder().state(Connected).address(new ServerAddress("loc:27018")).type(ReplicaSetPrimary).setName("t2")
                        .build()),
                Discovering);
        assertNull(description.getReplicaSetName());
    }


    @Test
    public void testIsConnecting() throws UnknownHostException {
        ClusterDescription description = new ClusterDescription(Arrays.asList(
                ServerDescription.builder().state(Connecting).address(new ServerAddress()).type(ReplicaSetPrimary).build()), Discovering);
        assertTrue(description.isConnecting());

        description = new ClusterDescription(Arrays.asList(
                ServerDescription.builder().state(Connected).address(new ServerAddress()).type(ReplicaSetPrimary).build()), Discovering);
        assertFalse(description.isConnecting());
    }

    @Test
    public void testSortingOfAll() {
        ClusterDescription description = new ClusterDescription(Arrays.asList(
                ServerDescription.builder().state(Connecting).address(new ServerAddress("loc:27019")).build(),
                ServerDescription.builder().state(Connecting).address(new ServerAddress("loc:27018")).build(),
                ServerDescription.builder().state(Connecting).address(new ServerAddress("loc:27017")).build()),
                Discovering);
        Iterator<ServerDescription> iter = description.getAll().iterator();
        assertEquals(new ServerAddress("loc:27017"), iter.next().getAddress());
        assertEquals(new ServerAddress("loc:27018"), iter.next().getAddress());
        assertEquals(new ServerAddress("loc:27019"), iter.next().getAddress());
    }

    @Test
    public void testObjectOverrides() {
        ClusterDescription description = new ClusterDescription(Arrays.asList(
                ServerDescription.builder().state(Connecting).address(new ServerAddress("loc:27019")).build(),
                ServerDescription.builder().state(Connecting).address(new ServerAddress("loc:27018")).build(),
                ServerDescription.builder().state(Connecting).address(new ServerAddress("loc:27017")).build()),
                Discovering);
        ClusterDescription descriptionTwo = new ClusterDescription(Arrays.asList(
                ServerDescription.builder().state(Connecting).address(new ServerAddress("loc:27019")).build(),
                ServerDescription.builder().state(Connecting).address(new ServerAddress("loc:27018")).build(),
                ServerDescription.builder().state(Connecting).address(new ServerAddress("loc:27017")).build()),
                Discovering);
        assertEquals(description, descriptionTwo);
        assertEquals(description.hashCode(), descriptionTwo.hashCode());
        assertTrue(description.toString().startsWith("ClusterDescription"));
   }
}
