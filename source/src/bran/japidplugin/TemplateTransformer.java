package bran.japidplugin;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.annotation.Annotation;

import org.eclipse.core.resources.IFile;

import cn.bran.japid.classmeta.AbstractTemplateClassMetaData;
import cn.bran.japid.compiler.JapidAbstractCompiler;
import cn.bran.japid.compiler.JapidLayoutCompiler;
import cn.bran.japid.compiler.JapidTemplateCompiler;
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
		if (src.indexOf("#{doLayout") >= 0 || src.indexOf("#{get") >= 0) {
			c = new JapidLayoutCompiler();
		} else {
			// regular template and tag are the same thing
			c = new JapidTemplateCompiler();
		}
		c.compile(temp);
		return temp.javaSource;
	}

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
