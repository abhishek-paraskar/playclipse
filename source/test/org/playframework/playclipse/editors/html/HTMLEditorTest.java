package org.playframework.playclipse.editors.html;

import static org.junit.Assert.*;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.Test;

public class HTMLEditorTest {
	@Test
	public void testImportLineRegex() {
		String l1 = "`import my.pack.Model";
		String l2 = "`import my.pack.Model;";
		String l3 = "`import my.pack.*";
		String l4 = "`import my.pack.*;";
		
		Pattern p = Pattern.compile("[^`]?`\\s*import\\s+([a-zA-Z0-9\\._]+)[\\s;]?");
		
		Matcher matcher = p.matcher(l1);
		assertTrue(matcher.find());
		assertEquals("my.pack.Model", matcher.group(1));

		matcher = p.matcher(l2);
		assertTrue(matcher.find());
		assertEquals("my.pack.Model", matcher.group(1));

		matcher = p.matcher(l3);
		assertTrue(matcher.find());
		assertEquals("my.pack.", matcher.group(1));

		matcher = p.matcher(l4);
		assertTrue(matcher.find());
		assertEquals("my.pack.", matcher.group(1));
	}
}
