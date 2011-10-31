package types;

import ma.glasnost.orika.test.proxy.SuperTypeTestCaseClasses.Author;

public class AuthorHidden implements Author {

	private String name;
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
}