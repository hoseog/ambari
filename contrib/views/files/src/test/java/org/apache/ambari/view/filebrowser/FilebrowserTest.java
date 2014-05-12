/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.ambari.view.filebrowser;

import static org.easymock.EasyMock.createNiceMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.client.WebTarget;

import org.apache.ambari.view.ViewContext;
import org.apache.ambari.view.ViewResourceHandler;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileUtil;
import org.apache.hadoop.hdfs.MiniDFSCluster;
import org.json.simple.JSONArray;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.Ignore;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.sun.jersey.core.header.FormDataContentDisposition;
import com.sun.jersey.multipart.FormDataBodyPart;



public class FilebrowserTest{
    private ViewResourceHandler handler;
    private ViewContext context;
    private HttpHeaders httpHeaders;
    private UriInfo uriInfo;

    private Map<String, String> properties;
    private FileBrowserService fileBrowserService;

    private MiniDFSCluster hdfsCluster;
    public static final String BASE_URI = "http://localhost:8084/myapp/";


    @Before
    public void setUp() throws Exception {
        handler = createNiceMock(ViewResourceHandler.class);
        context = createNiceMock(ViewContext.class);
        httpHeaders = createNiceMock(HttpHeaders.class);
        uriInfo = createNiceMock(UriInfo.class);

        properties = new HashMap<String, String>();
        File baseDir = new File("./target/hdfs/" + "FilebrowserTest")
            .getAbsoluteFile();
        FileUtil.fullyDelete(baseDir);
        Configuration conf = new Configuration();
        conf.set(MiniDFSCluster.HDFS_MINIDFS_BASEDIR, baseDir.getAbsolutePath());
        MiniDFSCluster.Builder builder = new MiniDFSCluster.Builder(conf);
        hdfsCluster = builder.build();
        String hdfsURI = hdfsCluster.getURI() + "/";
        properties.put("dataworker.defaultFs", hdfsURI);
        expect(context.getProperties()).andReturn(properties).anyTimes();
        expect(context.getUsername()).andReturn("ambari-qa").anyTimes();
        replay(handler, context, httpHeaders, uriInfo);
        fileBrowserService = getService(FileBrowserService.class, handler, context);
        FileOperationService.MkdirRequest request = new FileOperationService.MkdirRequest();
        request.path = "/tmp";
        fileBrowserService.fileOps().mkdir(request, httpHeaders, uriInfo);
    }

    @After
    public void tearDown() {
        hdfsCluster.shutdown();
    }

  // TODO : fix test!!!
    @Ignore
    @Test
    public void testListDir() throws IOException, Exception {
        FileOperationService.MkdirRequest request = new FileOperationService.MkdirRequest();
        request.path = "/tmp1";
        fileBrowserService.fileOps().mkdir(request, httpHeaders, uriInfo);
        Response response = fileBrowserService.fileOps().listdir("/", httpHeaders,
            uriInfo);
        JSONArray statuses = (JSONArray) response.getEntity();
        System.out.println(response.getEntity());
        Assert.assertEquals(200, response.getStatus());
        Assert.assertTrue(statuses.size() > 0);
        System.out.println(statuses);
    }

    private Response uploadFile(String path, String fileName,
        String fileExtension, String fileContent) throws Exception {
        File tempFile = File.createTempFile(fileName, fileExtension);
        BufferedWriter bw = new BufferedWriter(new FileWriter(tempFile));
        bw.write(fileContent);
        bw.close();
        InputStream content = new FileInputStream(tempFile);
        FormDataBodyPart inputStreamBody = new FormDataBodyPart(
            FormDataContentDisposition.name("file")
                .fileName(fileName + fileExtension).build(), content,
            MediaType.APPLICATION_OCTET_STREAM_TYPE);

        Response response = fileBrowserService.upload().uploadFile(content,
            inputStreamBody.getFormDataContentDisposition(), "/tmp/");
        return response;
    }

  // TODO : fix test!!!
    @Ignore
    @Test
    public void testUploadFile() throws Exception {
        Response response = uploadFile("/tmp/", "testUpload", ".tmp", "Hello world");
        Assert.assertEquals(200, response.getStatus());
        Response listdir = fileBrowserService.fileOps().listdir("/tmp", httpHeaders,
            uriInfo);
        JSONArray statuses = (JSONArray) listdir.getEntity();
        System.out.println(statuses.size());
        Response response2 = fileBrowserService.download().browse("/tmp/testUpload.tmp", false, httpHeaders, uriInfo);
        Assert.assertEquals(200, response2.getStatus());
    }

  // TODO : fix test!!!
    @Ignore
    @Test
    public void testStreamingGzip() throws Exception {
        String gzipDir = "/tmp/testGzip";
        FileOperationService.MkdirRequest request = new FileOperationService.MkdirRequest();
        request.path = gzipDir;
        fileBrowserService.fileOps().mkdir(request, httpHeaders, uriInfo);
        for (int i = 0; i < 10; i++) {
            uploadFile(gzipDir, "testGzip" + i, ".txt", "Hello world" + i);
        }
        DownloadService.DownloadRequest dr = new DownloadService.DownloadRequest();
        dr.entries = new String[] { gzipDir };

        Response result = fileBrowserService.download().downloadGZip(dr);
    }

    private static <T> T getService(Class<T> clazz,
        final ViewResourceHandler viewResourceHandler,
        final ViewContext viewInstanceContext) {
        Injector viewInstanceInjector = Guice.createInjector(new AbstractModule() {
            @Override
            protected void configure() {
                bind(ViewResourceHandler.class).toInstance(viewResourceHandler);
                bind(ViewContext.class).toInstance(viewInstanceContext);
            }
        });
        return viewInstanceInjector.getInstance(clazz);
    }


}