<#include "EntityCommonVisit.ftl">
package ${packageName};

import org.apache.commons.lang3.exception.ExceptionUtils;

import core.db.DBConsts;
import core.dbsrv.DB;
import core.Chunk;
import core.Port;
import core.Record;
import core.support.BufferPool;
import core.support.SysException;
import core.support.log.LogCore;
import ${superClassPackage}.${superClassName};
import ${annotationPack};

${annotation}
public final class ${entityName} extends ${superClassName} {
	public static final String tableName = "${tableName?lower_case}";

	/**
	 * 属性关键字
	 */
	public static final class K <#if hasSuper>extends SuperK </#if>{
		<#list fields as field>
		public static final String ${field.name} = "${field.name}";	//${field.comment}
		</#list>
	}

	@Override
	public String getTableName() {
		return tableName;
	}
	
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
	 * 新增数据
	 */
	@Override
	public void persist() {
		//状态错误
		if(record.getStatus() != DBConsts.RECORD_STATUS_NEW) {
			LogCore.db.error("只有新增包能调用persist函数，请确认状态：data={}, stackTrace={}", this, ExceptionUtils.getStackTrace(new Throwable()));
			return;
		}
		
		DB prx = DB.newInstance(getTableName());
		prx.insert(record);
		
		//重置状态
		record.resetStatus();
	}
	
	/**
	 * 同步修改数据至DB服务器
	 * 默认不立即持久化到数据库
	 */
	@Override
	public void update() {
		update(false);
	}
	
	/**
	 * 同步修改数据至DB服务器
	 * @param sync 是否立即同持久化到数据库
	 */
	@Override
	public void update(boolean sync) {
		//新增包不能直接调用update函数 请先调用persist
		if(record.getStatus() == DBConsts.RECORD_STATUS_NEW) {
			throw new SysException("新增包不能直接调用update函数，请先调用persist：data={}", this);
		}
		
		//升级包
		Chunk path = record.pathUpdateGen();
		if(path == null || path.length == 0) return;

		//将升级包同步至DB服务器
		DB prx = DB.newInstance(getTableName());
		prx.update(getId(), path, sync);
		
		//回收缓冲包
		BufferPool.deallocate(path.buffer);
		
		//重置状态
		record.resetStatus();
	}

	/**
	 * 删除数据
	 */
	@Override
	public void remove() {
		DB prx = DB.newInstance(getTableName());
		prx.delete(getId());
	}

	<#-- get和set方法 -->
	<@getAndSetField fields=fields/>

}