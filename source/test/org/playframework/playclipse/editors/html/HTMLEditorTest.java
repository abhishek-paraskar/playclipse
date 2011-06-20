package org.playframework.playclipse.editors.html;

import static org.junit.Assert.*;

import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.Test;

public class HTMLEditorTest {
	@Test
	public void testImportLineRegex() {
		String l1 = "`import my.pack.Model";
		String l2 = "`import my.pack.Model;";
		String l3 = "@import my.pack.*";
		String l4 = "@import my.pack.*;";
		
		Pattern p = Pattern.compile("[^`@]?[`@]\\s*import\\s+([a-zA-Z0-9\\._]+)[\\s;]?");
		
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
	
	@Test
	public void testEmptyLineInDerivedJavaFile() {
//		String linesep = System.getProperty("line.separator") ;
		
		String linesep = "\r\n";
		pciline(linesep);

		linesep = "\n";
		pciline(linesep);
	}

	/**
	 * @param linesep
	 */
	private void pciline(String linesep) {
		Pattern EMPLINE = Pattern.compile("\\s+;?\\s*// line [0-9]+.*");
		String line = "		// line 12" + linesep + "  ;// line 23" ;
		String regex = "[" + linesep + "]";
		System.out.println("split pattern: " + regex);
		String[] lines = line.split(regex);
		assertEquals(linesep.length() + 1, lines.length);
		assertTrue(EMPLINE.matcher(lines[0]).matches());
		assertTrue(EMPLINE.matcher(lines[linesep.length()]).matches());
	}
	
	/**
	 * @param linesep
	 */
	@Test
	public void testTagCommand() {
		Pattern p = Pattern.compile("[^`@]*[`@](tag|t)\\s+([\\w\\d\\./]+)\\s+.*");
		String line = "xx `tag my.Tag a b" ;
		Matcher matcher = p.matcher(line);
		assertTrue(matcher.matches());
		assertEquals("my.Tag", matcher.group(2));

		line = "xx @t my.Tag a b" ;
		matcher = p.matcher(line);
		assertTrue(matcher.matches());
		assertEquals("my.Tag", matcher.group(2));
		
	}

	@Test
	public void testTag3Command() {
		Pattern p = Pattern.compile("[^`]*`t\\s+([\\w\\d\\./]+)\\s*\\(.*");
		String line = "xx `t my.Tag(a, b)" ;
		Matcher matcher = p.matcher(line);
		assertTrue(matcher.matches());
		assertEquals("my.Tag", matcher.group(1));
		
	}
	
	
}
