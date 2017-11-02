package com.huolihuoshan.volcano.test;

import java.util.EnumSet;

import javax.servlet.DispatcherType;

import org.eclipse.jetty.servlet.FilterHolder;
import org.eclipse.jetty.testing.HttpTester;
import org.eclipse.jetty.testing.ServletTester;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

public class MainModuleTest {

	private static ServletTester server;
	private static MockClient client;

	@BeforeClass
	public static void initClass() throws Exception {
		server = new ServletTester();
		server.setContextPath("/");

		// enabled nutz
		FilterHolder filter = server.addFilter(org.nutz.mvc.NutFilter.class, "/*",
				EnumSet.of(DispatcherType.FORWARD, DispatcherType.REQUEST));
		filter.setInitParameter("modules", "com.huolihuoshan.volcano.EntryModule");
		server.addServlet(org.nutz.mvc.NutServlet.class, "/");
		
		// start
		server.start();
		System.out.println("Servelet tester started.");

		// init client
		client = new MockClient(server, "/");
	}

	@AfterClass
	public static void destroyClass() throws Exception {
		server.stop();
		System.out.println("Servelet tester stopped.");
	}

	@Before
	public void before() {

	}

	@After
	public void after() {

	}

	@Test
	public void testFoo() throws Exception {
		// test foo
		HttpTester response = client.get("/foo", "args=foo");
		assertEquals("\"foo\"",response.getContent());
	}
}
