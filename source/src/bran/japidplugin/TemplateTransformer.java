package bran.japidplugin;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.annotation.Annotation;
import java.util.Map;
import java.util.regex.Pattern;

import org.eclipse.core.resources.IFile;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.ToolFactory;
import org.eclipse.jdt.core.formatter.CodeFormatter;
import org.eclipse.jdt.core.formatter.DefaultCodeFormatterConstants;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.text.edits.TextEdit;
import org.playframework.playclipse.PlayPlugin;

import cn.bran.japid.classmeta.AbstractTemplateClassMetaData;
import cn.bran.japid.compiler.JapidAbstractCompiler;
import cn.bran.japid.compiler.JapidLayoutCompiler;
import cn.bran.japid.compiler.JapidTemplateCompiler;
import cn.bran.japid.compiler.JapidTemplateTransformer;
import cn.bran.japid.template.JapidTemplate;

/**
 * compile html based template to java files
 * 
 * The facade to all the compiler suite and configurations.
 * 
 * @author Bing Ran<bing_ran@hotmail.com>
 * 
 */
public class TemplateTransformer {

	// private MessageProvider messageProvider;
	// private UrlMapper urlMapper;

	/**
	 * 
	 * @param importLine
	 *            add an import to all the files generated. For examples:
	 *            "my.package.*", "my.package.MyClass"
	 */
	public static void addImportLine(String importLine) {
		AbstractTemplateClassMetaData.addImportLineGlobal(importLine);
	}

	/**
	 * effectively as in Java: "import static my.Tools.*;" if Tools.class is the
	 * parameter.
	 * 
	 * @param class1
	 */
	public static void addImportStatic(Class<?> class1) {
		AbstractTemplateClassMetaData.addImportStatic(class1);
	}

	/**
	 * 
	 * @return the generated java code 
	 * @throws Exception
	 */
	public static String generate(IFile srcFile) throws Exception {
		InputStream contents = srcFile.getContents();
		BufferedReader  br  = new BufferedReader(new InputStreamReader(contents, "UTF-8"));
		String src = "";
		String line = "";
		while ((line = br.readLine()) != null) {
			src += line + "\n";
		}
		contents.close();
		
		String fileName = srcFile.getProjectRelativePath().toString().substring("app/".length());
		// the compiler assumes the path separator is system specific separator
		fileName =  fileName.replace('/', File.separatorChar);
		JapidTemplate temp = new JapidTemplate(fileName, src);
		JapidAbstractCompiler c = null;
		if (JapidTemplateTransformer.looksLikeLayout(src)) {
			c = new JapidLayoutCompiler();
		} else {
			// regular template and tag are the same thing
			c = new JapidTemplateCompiler();
		}
	
		c.compile(temp);
		// now we have the derived source
		String text = temp.javaSource;
		
		String newline = "\n";//System.getProperty("line.separator");
		String[] lines = text.split("[" + newline + "]");
		text = "";
		for (String l : lines) {
			if (!EMPLINE.matcher(l).matches()) {
				text += l + "\n";
			}
		}
		
		// let's format the code
		@SuppressWarnings("unchecked")
		Map<String, String> options = DefaultCodeFormatterConstants.getEclipseDefaultSettings();
		// initialize the compiler settings to be able to format 1.5 code
		options.put(JavaCore.COMPILER_COMPLIANCE, JavaCore.VERSION_1_6);
		options.put(JavaCore.COMPILER_CODEGEN_TARGET_PLATFORM, JavaCore.VERSION_1_6);
		options.put(JavaCore.COMPILER_SOURCE, JavaCore.VERSION_1_6);
		options.put(DefaultCodeFormatterConstants.FORMATTER_JOIN_WRAPPED_LINES, DefaultCodeFormatterConstants.FALSE);

		CodeFormatter codeFormatter = ToolFactory.createCodeFormatter(options);
		TextEdit format = codeFormatter.format(CodeFormatter.K_COMPILATION_UNIT, text, 0, text.length(), 0, null);
		IDocument document= new Document(text);
		format.apply(document);
		return document.get();
	}

	static Pattern EMPLINE = Pattern.compile("\\s*;?// line [0-9]+.*");
	/**
	 * add class level annotation for whatever purpose
	 * 
	 * @param anno
	 */
	public void addAnnotation(Class<? extends Annotation> anno) {
		AbstractTemplateClassMetaData.addAnnotation(anno);
		// typeAnnotations.add(anno);
	}
}
