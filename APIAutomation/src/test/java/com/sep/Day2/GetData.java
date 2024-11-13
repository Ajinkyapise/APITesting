package com.sep.Day2;

import io.restassured.response.Response;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import java.util.*;

import com.sep.models.requests.RepositoryPojo;
import com.sep.models.responses.CreateRepositoryResponsePojo;
import com.sep.models.responses.ErrorResponsePojo;
import com.sep.models.responses.ResponsePojo;

import io.restassured.module.jsv.JsonSchemaValidator;

public class GetData extends BaseTest {

	String owner = "Ajinkyapise";
	String repo = "Snapgram2";

	String newRepoString = "Hello-World";
	String updatedNewRepo = "Updated-Repo-Hello-World";

	@BeforeMethod
	private void Syso() {
		System.out.println("-----------------------");

	}

	@Test(priority = 1)
	public void testGetSingleRepository() {

		Response response = request.when().get("/repos/" + owner + "/" + repo);
		Assert.assertEquals(response.getStatusCode(), 200, "Status code mismatch");
		ResponsePojo repository = response.as(ResponsePojo.class);
		String expectedFullName = owner + "/" + repo;
		Assert.assertEquals(repository.getFull_name(), expectedFullName, "Repository full_name mismatch");
		Assert.assertEquals(repository.getDefault_branch(), "main", "Default branch mismatch");
		String contentType = response.getHeader("Content-Type");
		Assert.assertEquals(contentType, "application/json; charset=utf-8", "Content-Type mismatch");
		System.out.println("Valid Status Code: " + response.getStatusCode());
		System.out.println("Repository full_name: " + repository.getFull_name());
		System.out.println("Default branch: " + repository.getDefault_branch());
		System.out.println("Content-Type: " + contentType);
	}

	@Test(priority = 2)
	public void testGetNonExistingRepository() {
		String Wrongrepo = "WrongRepo";

		Response response = request.when().get("/repos/" + owner + "/" + Wrongrepo);

		Assert.assertEquals(response.getStatusCode(), 404, "Status code mismatch");

		ErrorResponsePojo errorResponse = response.as(ErrorResponsePojo.class);
		Assert.assertEquals(errorResponse.getMessage(), "Not Found", "Error message mismatch");

		System.out.println("Response status code: " + response.getStatusCode());
		System.out.println("Response message: " + errorResponse.getMessage());
	}

	@Test(priority = 3)
	public void testGetAllRepositories() {

		Response response = request.when().get("/user/repos");

		Assert.assertEquals(response.getStatusCode(), 200, "Status code mismatch");

		response.then().assertThat()
				.body(JsonSchemaValidator.matchesJsonSchemaInClasspath("schemas/userReposSchema.json"));

		List<RepositoryPojo> repositories = Arrays.asList(response.as(RepositoryPojo[].class));

		System.out.println("Total number of repositories: " + repositories.size());
		System.out.println("Public repositories:");
		repositories.stream().filter(repo -> !repo.isPrivate()).map(RepositoryPojo::getName)
				.forEach(System.out::println);

		String contentType = response.getHeader("Content-Type");
		Assert.assertEquals(contentType, "application/json; charset=utf-8", "Content-Type mismatch");

		System.out.println("Response Content-Type: " + contentType);
	}

	@Test(priority = 4)
	public void testCreateRepository() {
		Map<String, Object> requestBody = new HashMap<>();
		requestBody.put("name", newRepoString);
		requestBody.put("description", "This is a new repo");
		requestBody.put("homepage", "https://github.com");
		requestBody.put("private", false);

		Response response = request.body(requestBody).when().post("/user/repos");

		Assert.assertEquals(response.getStatusCode(), 201, "Status code mismatch");

		CreateRepositoryResponsePojo repoResponse = response.as(CreateRepositoryResponsePojo.class);

		Assert.assertEquals(repoResponse.getName(), newRepoString, "Repository name mismatch");
		Assert.assertEquals(repoResponse.getOwner().getLogin(), owner, "Owner login mismatch");
		Assert.assertEquals(repoResponse.getOwner().getType(), "User", "Owner type mismatch");

		System.out.println("Repository name: " + repoResponse.getName());
		System.out.println("Owner login: " + repoResponse.getOwner().getLogin());
		System.out.println("Owner type: " + repoResponse.getOwner().getType());
	}

	@Test(priority = 5)
	public void testCreateRepositoryWithExistingName() {
		Map<String, Object> requestBody = new HashMap<>();
		requestBody.put("name", newRepoString);
		requestBody.put("description", "This is a duplicate repo");
		requestBody.put("homepage", "https://github.com");
		requestBody.put("private", false);

		Response response = request.body(requestBody).when().post("/user/repos");

		Assert.assertEquals(response.getStatusCode(), 422, "Status code mismatch");

		ErrorResponsePojo errorResponse = response.as(ErrorResponsePojo.class);

		Assert.assertEquals(errorResponse.getMessage(), "Repository creation failed.", "Error message mismatch");

		System.out.println("Response status code: " + response.getStatusCode());
		System.out.println("Error message: " + errorResponse.getMessage());
	}

	@Test(priority = 6)
	// dependsOnMethods = "testCreateRepository",
	public void testUpdateRepositoryName() {

		Map<String, Object> requestBody = new HashMap<>();
		requestBody.put("name", updatedNewRepo);
		requestBody.put("description", "Updated via API");
		requestBody.put("private", false);

		Response response = request.body(requestBody).when().patch("/repos/" + owner + "/" + newRepoString);

		System.out.println("Response: " + response.asString());

		Assert.assertEquals(response.getStatusCode(), 200,
				"Expected status code 200 but received " + response.getStatusCode());

		RepositoryPojo updatedRepo = response.as(RepositoryPojo.class);

		Assert.assertEquals(updatedRepo.getName(), updatedNewRepo, "Repository name mismatch");

		Assert.assertEquals(updatedRepo.getDescription(), "Updated via API", "Repository description mismatch");

		Assert.assertFalse(updatedRepo.isPrivate(), "Repository privacy setting mismatch");

		System.out.println("Repository updated successfully:");
		System.out.println("New Name: " + updatedRepo.getName());
		System.out.println("Description: " + updatedRepo.getDescription());
		System.out.println("Is Private: " + updatedRepo.isPrivate());
	}

	@Test(priority = 7)
	public void testDeleteRepository() {

		if (updatedNewRepo == null || updatedNewRepo.isEmpty()) {
			throw new IllegalStateException(
					"No repository name found to delete. Ensure the update test ran successfully.");
		}

		Response response = request.delete("/repos/" + owner + "/" + updatedNewRepo);

		Assert.assertEquals(response.getStatusCode(), 204,
				"Expected status code 204 but received " + response.getStatusCode());

		String responseBody = response.getBody().asString();
		Assert.assertTrue(responseBody == null || responseBody.isEmpty(),
				"Response body should be null or empty but found: " + responseBody);

		System.out.println("Got the correct status code: " + response.getStatusCode());
		System.out.println("Repository deleted successfully: " + updatedNewRepo);
	}

	@Test(priority = 8)
	public void testDeleteNonExistingRepository() {
		String owner = "Ajinkyapise";
		String nonExistingRepoName = "Non-Existing-Repo-12345";

		Response response = request.delete("/repos/" + owner + "/" + nonExistingRepoName);

		System.out.println("Response Status Code: " + response.getStatusCode());
		System.out.println("Response Body: " + response.getBody().asString());

		Assert.assertEquals(response.getStatusCode(), 404,
				"Expected status code 404 but received " + response.getStatusCode());

		ErrorResponsePojo errorResponse = response.as(ErrorResponsePojo.class);

		Assert.assertEquals(errorResponse.getMessage(), "Not Found",
				"Expected 'Not Found' in the response body but found: " + errorResponse.getMessage());

		System.out.println("Validation for non-existing repository deletion passed successfully.");
	}

}
