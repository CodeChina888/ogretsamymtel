<#include "EntityCommonVisit.ftl">
package ${packageName};

import core.db.DBConsts;
import core.Port;
import core.Record;
import ${superClassPackage}.${superClassName};
import ${annotationPack};

${annotation}
public abstract class ${entityName} extends ${superClassName} {
	public ${entityName}() {
		super();
	<#list fields as field>
		<#if field.defaults??>
			<#if field.type == "String">
		set${field.name?cap_first}("${field.defaults}");
			<#else>
		set${field.name?cap_first}(${field.defaults});
			</#if>
		</#if>
	</#list>
	}

	public ${entityName}(Record record) {
		super(record);
	<#list fields as field>
		<#if field.isTransient>
		<#if field.defaults??>
			<#if field.type == "String">
		set${field.name?cap_first}("${field.defaults}");
			<#else>
		set${field.name?cap_first}(${field.defaults});
			</#if>
		</#if>
		</#if>
	</#list>
	}
	
	/**
	 * 属性关键字
	 */
	public static class SuperK {
		<#list fields as field>
		public static final String ${field.name} = "${field.name}";	//${field.comment}
		</#list>
	}

	<#-- get和set方法 -->
	<@getAndSetField fields=fields />

}