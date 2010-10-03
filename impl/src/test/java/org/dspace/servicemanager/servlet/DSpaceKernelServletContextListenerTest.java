/**
 * $Id$
 * $URL$
 * *************************************************************************
 * Copyright (c) 2002-2009, DuraSpace.  All rights reserved
 * Licensed under the DuraSpace License.
 *
 * A copy of the DuraSpace License has been included in this
 * distribution and is available at: http://scm.dspace.org/svn/repo/licenses/LICENSE.txt
 */
package org.dspace.servicemanager.servlet;

import static org.junit.Assert.*;

import java.io.IOException;

import org.dspace.kernel.DSpaceKernelManager;
import org.dspace.utils.servlet.DSpaceWebappServletFilter;
import org.junit.Test;

import org.mortbay.jetty.Handler;
import org.mortbay.jetty.testing.HttpTester;
import org.mortbay.jetty.testing.ServletTester;


/**
 * This starts up a jetty server and tests the ability for the servlet filter to start a kernel
 * and correctly shut it down
 * 
 * @author Aaron Zeckoski (azeckoski @ gmail.com)
 */
public class DSpaceKernelServletContextListenerTest {

    @Test
    public void testSampleRequest() {
        // make sure no kernel yet
        try {
            new DSpaceKernelManager().getKernel();
            fail("Should have thrown exception");
        } catch (IllegalStateException e) {
            assertNotNull(e.getMessage());
        }

        ServletTester tester = new ServletTester();
        tester.setContextPath("/");
        tester.getContext().addEventListener(new DSpaceKernelServletContextListener());
        tester.addFilter(DSpaceWebappServletFilter.class, "/*", Handler.REQUEST);
        tester.addServlet(SampleServlet.class, "/dspace");
        try {
            tester.start();
        } catch (Exception e) {
            fail("Could not start the jetty server: " + e.getMessage());
        }

        // now there should be a kernel
        assertNotNull( new DSpaceKernelManager().getKernel() );

        // now fire the request
        String jettyRequest = 
            "GET /dspace HTTP/1.1\r\n"+
            "Host: tester\r\n"+
            "\r\n";
        try {
            String content = tester.getResponses(jettyRequest);
            assertNotNull(content);
            assertTrue(content.contains("DSpaceTest"));
//            assertFalse(content.contains("session=null"));
//            assertFalse(content.contains("request=null"));
        } catch (Exception e) {
            fail("Could not fire request: " + e.getMessage());
        }

        // now there should be a kernel
        assertNotNull( new DSpaceKernelManager().getKernel() );

        // try a request a different way
        HttpTester request = new HttpTester();
        HttpTester response = new HttpTester();
        request.setMethod("GET");
        request.setHeader("Host","tester");
        request.setVersion("HTTP/1.0");
        request.setURI("/dspace");

        try {
            response.parse( tester.getResponses(request.generate()) );
        } catch (IOException e1) {
            fail("Could not parse response: " + e1.getMessage());
        } catch (Exception e1) {
            fail("Could not parse response: " + e1.getMessage());
        }

        assertTrue(response.getMethod() == null);
        assertEquals(200, response.getStatus());
        String content = response.getContent();
        assertNotNull(content);
        assertTrue(content.contains("DSpaceTest"));
//        assertFalse(content.contains("session=null"));
//        assertFalse(content.contains("request=null"));

        // now there should be a kernel
        assertNotNull( new DSpaceKernelManager().getKernel() );

        try {
            tester.stop();
        } catch (Exception e) {
            fail("Could not stop the jetty server: " + e.getMessage());
        }

        // back to no kernel again
        try {
            new DSpaceKernelManager().getKernel();
            fail("Should have thrown exception");
        } catch (IllegalStateException e) {
            assertNotNull(e.getMessage());
        }
    }

    @Test
    public void testSampleRequestDouble() {
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        testSampleRequest();
    }


}
