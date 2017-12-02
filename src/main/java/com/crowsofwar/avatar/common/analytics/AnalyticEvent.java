package com.crowsofwar.avatar.common.analytics;

public enum AnalyticEvent {

	TEST_1("test", "one"),
	TEST_2("test", "two");

	private final String category, name;

	AnalyticEvent(String category, String name) {
		this.category = category;
		this.name = name;
	}

	public String getCategory() {
		return category;
	}

	public String getAction() {
		return name;
	}

}
