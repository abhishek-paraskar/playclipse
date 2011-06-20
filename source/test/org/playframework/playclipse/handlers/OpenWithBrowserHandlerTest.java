package org.playframework.playclipse.handlers;

import static org.junit.Assert.*;

import java.util.regex.Matcher;

import org.junit.Test;


public class OpenWithBrowserHandlerTest {
	@Test
	public void testPortLinePattern() {
		String line = "http.port=9000";
		Matcher matcher = OpenWithBrowserHandler.portlinePattern.matcher(line);
		assertTrue(matcher.matches());
		assertEquals("9000", matcher.group(1));
	}
}
