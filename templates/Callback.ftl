package ${packageName};

import ${annotationPack};

${annotation}
public class ${callbackName} {
	<#list methods as method>
	public static final String ${method} = "${method}";
	</#list>
}
