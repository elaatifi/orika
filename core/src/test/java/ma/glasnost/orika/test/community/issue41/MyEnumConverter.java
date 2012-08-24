package ma.glasnost.orika.test.community.issue41;

import ma.glasnost.orika.CustomConverter;
import ma.glasnost.orika.metadata.Type;

public class MyEnumConverter extends CustomConverter<String, MyEnum> {

	public MyEnum convert(String source, Type<? extends MyEnum> destinationType) {
		if ("un".equals(source)) {
			return MyEnum.one;
		}

		if ("deux".equals(source)) {
			return MyEnum.two;
		}
		return null;
	}
}