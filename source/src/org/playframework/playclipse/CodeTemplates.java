package org.playframework.playclipse;

/**
 * Helpers to provide code templates for various files (controllers, views, models)
 * @author erwan
 *
 */
public final class CodeTemplates {

	public static String controller(String name, String packageName) {
		StringBuilder builder = new StringBuilder();
		builder.append("package ");
		builder.append(packageName);
		builder.append(";\n\n");
		builder.append("import play.mvc.*;\n\n");
		builder.append("public class ");
		builder.append(name);
		builder.append(" extends Controller {\n\n");
		builder.append("    public static void index() {\n");
		builder.append("        render();\n");
		builder.append("    }\n\n");
		builder.append("}\n");
		return builder.toString();
	}

	public static String japidController(String name, String packageName) {
		StringBuilder builder = new StringBuilder();
		builder.append("package ");
		builder.append(packageName);
		builder.append(";\n\n");
		builder.append("import play.mvc.*;\n\n");
		builder.append("import cn.bran.play.JapidController;\n\n");
		builder.append("// make sure you have \n");
		builder.append("// \t\tmodule.japid=${play.path}/modules/japid-head\n");
		builder.append("// in your application.conf file, and \"play eclipsify\"\n");
		builder.append("// if you notice the JapidController is not found.\n\n");
		builder.append("public class ");
		builder.append(name);
		builder.append(" extends JapidController {\n\n");
		builder.append("    public static void index() {\n");
		builder.append("        renderJapid(\"Hello world!\", 123);\n");
		builder.append("    }\n\n");
		builder.append("}\n");
		return builder.toString();
	}
	
	public static String view(String title) {
		StringBuilder builder = new StringBuilder();
		builder.append("#{extends 'main.html' /}\n");
		builder.append("#{set title:'");
		builder.append(title);
		builder.append("' /}\n\n");
		builder.append("Here goes your content.");
		return builder.toString();
	}

	public static String japidView(String title) {
		StringBuilder builder = new StringBuilder();
		builder.append("`extends \"SampleLayout.html\"\n");
		builder.append("`args String s, int i \n");
		builder.append("\n");
		builder.append("#{set title:\"");
		builder.append(title);
		builder.append("\" /}\n\n");
		builder.append("hello ${s}, ${i}.\n");
		builder.append("Here goes your Japid template content.\n");
		builder.append("call a tag: \n");
		builder.append("#{SampleTag \"world\" /}\n");
		return builder.toString();
	}
	
	public static String model(String modelName, String packageName) {
		StringBuilder builder = new StringBuilder();
		builder.append("package ");
		builder.append(packageName);
		builder.append(";\n\n");
		builder.append("import play.*;\n");
		builder.append("import play.db.jpa.*;\n\n");
		builder.append("import javax.persistence.*;\n");
		builder.append("import java.util.*;\n\n");
		builder.append("@Entity\n");
		builder.append("public class ");
		builder.append(modelName);
		builder.append(" extends Model {\n    \n}\n");
		return builder.toString();
	}

}
