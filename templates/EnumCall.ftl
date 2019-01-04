package ${packageName};

public final class EnumCall {
 	<#assign i = 0>
	<#list methodsList as map>
	<#list map.methods as method>
	<#assign i = i + 1> 
	public static final int ${method.enumCall} = ${method.enumCallHashCode};
	</#list>
	</#list>
}
	
