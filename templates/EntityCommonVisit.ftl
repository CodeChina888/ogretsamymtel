<#-- 自定义宏，一些属性用来同步数据库用的，避免多处重写就写到这儿了 -->

<#macro setField fieldName fieldType>
		//更新前的数据状态
		int statusOld = record.getStatus();
		
		//更新属性
		<#if fieldType == "boolean">
		record.set("${fieldName}", ${fieldName} ? 1 : 0);
		<#else>
		record.set("${fieldName}", ${fieldName});
		</#if>

		//更新后的数据状态
		int statusNew = record.getStatus();
		//1.如果更新前是普通状态 and 更新后是修改状态，那么就记录这条数据，用来稍后自动提交。
		//2.哪怕之前是修改状态，只要数据是刚创建或串行化过来的新对象，则也会记录修改，因为有些时候会串行化过来一个修改状态下的数据。
		if((statusOld == DBConsts.RECORD_STATUS_NONE && statusNew == DBConsts.RECORD_STATUS_MODIFIED) ||
		   (statusOld == DBConsts.RECORD_STATUS_MODIFIED && record.isNewness())) {
			//记录修改的数据 用来稍后自动提交
			Port.getCurrent().addEntityModify(this);
			//如果是刚创建或串行化过来的新对象 取消这个标示
			if(record.isNewness()) {
				record.setNewness(false);
			}
		}
</#macro>
<#macro setFieldNoChange fieldName fieldType>
		//更新属性
		<#if fieldType == "boolean">
		record.setNoUpdate("${fieldName}", ${fieldName} ? 1 : 0);
		<#else>
		record.setNoUpdate("${fieldName}", ${fieldName});
		</#if>
</#macro>

<#-- 这是get和set方法的宏定义 -->
<#macro getAndSetField fields>
<#list fields as field>
<#-- **************不要持久化到数据库中的****************** -->
<#if field.isTransient>
	/**
	 * ${field.comment}  *此为非持久化属性，请注意*
	 */
	<#-- **************除了布尔类型之外的属性调用函数*********** -->
	<#if field.type!="boolean">
	public ${field.type} get${field.name?cap_first}() {
		return getValueTrans("${field.name}");
	}

	public void set${field.name?cap_first}(${field.type} ${field.name}) {
		setValueTrans("${field.name}", ${field.name});
	}
	<#else>
	<#-- *************布尔类型进行特殊处理 独有的属性调用函数*********** -->
	public ${field.type} is${field.name?cap_first}() {
		return getValueTrans("${field.name}");
	}

	public void set${field.name?cap_first}(${field.type} ${field.name}) {
		setValueTrans("${field.name}", ${field.name});
	}
	</#if>
<#else>
	/**
	 * ${field.comment}
	 */
	<#-- **************要持久化到数据库中的********************* -->
	<#-- **************除了布尔类型之外的属性调用函数*********** -->
	<#if field.type!="boolean">
	public ${field.type} get${field.name?cap_first}() {
		return record.get("${field.name}");
	}

	public void set${field.name?cap_first}(final ${field.type} ${field.name}) {
		<@setField fieldName=field.name fieldType=field.type/>
	}
	<#else>
	<#-- *************布尔类型进行特殊处理 独有的属性调用函数*********** -->
	public ${field.type} is${field.name?cap_first}() {
		return record.<Integer>get("${field.name}") == 1;
	}

	public void set${field.name?cap_first}(${field.type} ${field.name}) {
		<@setField fieldName=field.name fieldType=field.type/>
	}
	</#if>
</#if>
</#list>
</#macro>
