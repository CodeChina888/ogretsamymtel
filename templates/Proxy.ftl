package ${packageName};
                    
import core.CallPoint;
import core.Port;
import core.Service;
import core.gen.proxy.ProxyBase;
<#if hasDefault>
import core.support.Distr;
import core.support.log.LogCore;
</#if>
import core.support.Param;
import core.support.Utils;
import core.support.function.*;
import ${annotationPack};
<#if importPackages??>
<#list importPackages as package>
import ${package};
</#list>
</#if>

${annotation}
public final class ${proxyName} extends ProxyBase {
	public final class EnumCall{
		<#assign i = 0>
		<#list methods as method>
		<#assign i = i + 1> 
		public static final int ${method.enumCall} = ${i};
		</#list>		
	}
	<#if hasDefault>
	private static final String SERV_ID = "${servId}";
	</#if>
	
	private CallPoint remote;
	private Port localPort;
	private String callerInfo;
	// 当此参数为true时同NODE间传递将不在进行克隆，直接使用此对象进行。可极大提高性能，但如果设置不当可能引起错误。
	private boolean immutableOnce;
	
	/**
	 * 私有构造函数
	 * 防止实例被私自创建 必须通过newInstance函数
	 */
	private ${proxyName}() {}
	
	/**
	 * 获取函数指针
	 */
	@Override
	${methodFunctionAnnotation}
	public Object getMethodFunction(Service service, int methodKey) {
		${className} serv = (${className})service;
		switch (methodKey) {
			<#list methods as method>
			case EnumCall.${method.enumCall}: {
				<#if method.hasException>
				GofFunction${method.paramsSize}${method.functionTypes} f = (${method.paramsCall}) -> { try { serv.${method.name}(${method.paramsCall}); } catch(Exception e) { throw new core.support.SysException(e); } };
				return f;
				<#else>
				return (GofFunction${method.paramsSize}${method.functionTypes})serv::${method.name};
				</#if>
			}
			</#list>
			default: break;
		}
		return null;
	}
	
	<#if hasDefault>
	/**
	 * 获取实例
	 * 大多数情况下可用此函数获取
	 * @return
	 */
	public static ${proxyName} newInstance() {
		String portId = Distr.getPortId(SERV_ID);
		if(portId == null) {
			LogCore.remote.error("通过servId未能找到查找上级Port: servId={}", SERV_ID);
			return null;
		}
		
		String nodeId = Distr.getNodeId(portId);
		if(nodeId == null) {
			LogCore.remote.error("通过portId未能找到查找上级Node: portId={}", portId);
			return null;
		}
		
		return createInstance(nodeId, portId, SERV_ID);
	}
	</#if>
	
	<#if !hasDefault>
	/**
	 * 获取实例
	 * @return
	 */
	public static ${proxyName} newInstance(CallPoint targetPoint) {
		return createInstance(targetPoint.nodeId, targetPoint.portId, targetPoint.servId);
	}
	
	/**
	 * 获取实例
	 * @return
	 */
	public static ${proxyName} newInstance(String node, String port, Object id) {
		return createInstance(node, port, id);
	}
	</#if>
	
	/**
	 * 创建实例
	 * @param node
	 * @param port
	 * @param id
	 * @return
	 */
	private static ${proxyName} createInstance(String node, String port, Object id) {
		${proxyName} inst = new ${proxyName}();
		inst.localPort = Port.getCurrent();
		inst.remote = new CallPoint(node, port, id);
		
		return inst;
	}
	
	/**
	 * 监听返回值
	 * @param method
	 * @param context
	 */
	public void listenResult(GofFunction2<Param, Param> method, Object...context) {
		listenResult(method, new Param(context));
	}
	
	/**
	 * 监听返回值
	 * @param method
	 * @param context
	 */
	public void listenResult(GofFunction2<Param, Param> method, Param context) {
		context.put("_callerInfo", callerInfo);
		localPort.listenResult(method, context);
	}
	
	
	public void listenResult(GofFunction3<Boolean, Param, Param> method, Object...context) {
		listenResult(method, new Param(context));
	}
	
	public void listenResult(GofFunction3<Boolean, Param, Param> method, Param context) {
		context.put("_callerInfo", callerInfo);
		localPort.listenResult(method, context);
	}
	
	
	/**
	 * 等待返回值
	 */
	public Param waitForResult() {
		return localPort.waitForResult();
	}
	
	/**
	 * 设置后预先提醒框架下一次RPC调用参数是不可变的，可进行通信优化。<br/>
	 * 同Node间通信将不在进行Call对象克隆，可极大提高性能。<br/>
	 * 但设置后由于没进行克隆操作，接发双方都可对同一对象进行操作，可能会引起错误。<br/>
	 * 
	 * *由于有危险性，并且大多数时候RPC成本不高，建议只有业务中频繁调用或参数克隆成本较高时才使用本函数。<br/>
	 * *当接发双方仅有一方会对通信参数进行处理时，哪怕参数中有可变类型，也可以调用本函数进行优化。<br/>
	 * *当接发双方Node不相同时，本参数无效，双方处理不同对象；<br/>
	 *  当接发双方Node相同时，双方处理相同对象，这种差异逻辑会对分布式应用带来隐患，要小心使用。 <br/>
	 */
	public void immutableOnce() {
		this.immutableOnce = true;
	}
	<#list methods as method>
	${method.annotation}
	public void ${method.name}(${method.params}) {
		callerInfo = Utils.getCallerInfo();
		remote.callerInfo = callerInfo;
		localPort.call(<#if method.argsImmutable>true<#else>immutableOnce</#if>, remote, EnumCall.${method.enumCall}, "${proxyName}.${method.name}", new Object[] {${method.paramsCall}});
		if(immutableOnce) immutableOnce = false;
	}
	</#list>
}
