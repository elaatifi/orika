package ma.glasnost.orika.test.community.issue41;


public class MyTargetObject {
	private MyTargetSubObject sub;
	private MyEnum directE;

	public MyEnum getDirectE() {
		return directE;
	}

	public void setDirectE(MyEnum directE) {
		this.directE = directE;
	}

	public MyTargetSubObject getSub() {
		return sub;
	}

	public void setSub(MyTargetSubObject sub) {
		this.sub = sub;
	}
}