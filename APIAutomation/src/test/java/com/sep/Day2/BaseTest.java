package com.sep.Day2;

import io.restassured.RestAssured;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;

public class BaseTest {
	protected RequestSpecification request;
	String GitHUB_TOKEN = "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";

	public BaseTest() {
		RestAssured.baseURI = "https://api.github.com/";
		request = RestAssured.given().headers("Authorization", "Bearer " + GitHUB_TOKEN);

	}

}
