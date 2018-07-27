/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.oozie.util;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.filecache.DistributedCache;
import org.apache.hadoop.fs.Path;
import org.apache.oozie.test.XFsTestCase;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class TestClasspathUtils extends XFsTestCase {

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        // This is normally true, and adds the entirety of the current classpath in ClasspathUtils, which we don't want to test or
        // worry about here.  Temporarily set this back to false so it behaves normally.
        ClasspathUtils.setUsingMiniYarnCluster(false);
    }

    @Override
    protected void tearDown() throws Exception {
        // Make sure to turn this back on for subsequent tests
        ClasspathUtils.setUsingMiniYarnCluster(true);
        super.tearDown();
    }

    public void testSetupClasspath() throws Exception {
        Configuration conf = new Configuration(false);
        Map<String, String> env = new HashMap<String, String>();

        Path p1 = new Path(getFsTestCaseDir(), "foo.xml");
        getFileSystem().createNewFile(p1);
        DistributedCache.addFileToClassPath(p1, conf);

        Path p2 = new Path(getFsTestCaseDir(), "foo.txt");
        getFileSystem().createNewFile(p2);
        DistributedCache.addFileToClassPath(p2, conf);

        Path p3 = new Path(getFsTestCaseDir(), "foo.zip");
        getFileSystem().createNewFile(p3);
        DistributedCache.addArchiveToClassPath(p3, conf);

        ClasspathUtils.setupClasspath(env, conf);

        assertEquals("environment variables size mismatch", 3, env.size());
        assertTrue("CLASSPATH is not present", env.containsKey("CLASSPATH"));
        String[] paths = env.get("CLASSPATH").split(":");
        assertEquals("CLASSPATH size mismatch", 10, paths.length);
        Arrays.sort(paths);
        assertEquals("CLASSPATH content mismatch", "$HADOOP_COMMON_HOME/share/hadoop/common/*", paths[0]);
        assertEquals("CLASSPATH content mismatch", "$HADOOP_COMMON_HOME/share/hadoop/common/lib/*", paths[1]);
        assertEquals("CLASSPATH content mismatch", "$HADOOP_CONF_DIR", paths[2]);
        assertEquals("CLASSPATH content mismatch", "$HADOOP_HDFS_HOME/share/hadoop/hdfs/*", paths[3]);
        assertEquals("CLASSPATH content mismatch", "$HADOOP_HDFS_HOME/share/hadoop/hdfs/lib/*", paths[4]);
        assertEquals("CLASSPATH content mismatch", "$HADOOP_YARN_HOME/share/hadoop/yarn/*", paths[5]);
        assertEquals("CLASSPATH content mismatch", "$HADOOP_YARN_HOME/share/hadoop/yarn/lib/*", paths[6]);
        assertEquals("CLASSPATH content mismatch", "$MR2_CLASSPATH", paths[7]);
        assertEquals("CLASSPATH content mismatch", "$PWD", paths[8]);
        assertEquals("CLASSPATH content mismatch", "$PWD/*", paths[9]);

        assertTrue("$PWD is not present", env.containsKey("$PWD"));
        paths = env.get("$PWD").split(":");
        assertEquals("$PWD size mismatch", 3, paths.length);
        Arrays.sort(paths);
        assertEquals("$PWD content mismatch", "$PWD/foo.txt", paths[0]);
        assertEquals("$PWD content mismatch", "$PWD/foo.xml", paths[1]);
        assertEquals("$PWD content mismatch", "$PWD/foo.zip", paths[2]);

        assertTrue("LD_LIBRARY_PATH is not present", env.containsKey("LD_LIBRARY_PATH"));
        assertEquals("LD_LIBRARY_PATH content mismatch", "$JAVA_LIBRARY_PATH", env.get("LD_LIBRARY_PATH"));
    }

    public void testAddMapReduceToClasspath() throws Exception {
        Configuration conf = new Configuration(false);
        Map<String, String> env = new HashMap<String, String>();

        ClasspathUtils.addMapReduceToClasspath(env, conf);

        assertEquals(1, env.size());
        assertTrue("CLASSPATH is not present", env.containsKey("CLASSPATH"));
        String[] paths = env.get("CLASSPATH").split(":");
        assertEquals("CLASSPATH size mismatch", 2, paths.length);
        Arrays.sort(paths);
        assertEquals("CLASSPATH content mismatch", "$HADOOP_MAPRED_HOME/share/hadoop/mapreduce/*", paths[0]);
        assertEquals("CLASSPATH content mismatch", "$HADOOP_MAPRED_HOME/share/hadoop/mapreduce/lib/*", paths[1]);
    }
}
