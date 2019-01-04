package ${rootPackageName};

import core.gen.GofGenFile;
import core.InputStream;

@GofGenFile
public final class ${rootClassName}{
	public static ${interfaceName} create(int id){
		switch(id){
			<#list methodsList as m>
			case ${m.id}:
				return new ${m.className}();
			</#list>
		}
		return null;
	}
	public static void init(){
		InputStream.setCreateCommonFunc(${rootClassName}::create);
	}
}

