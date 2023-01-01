package api_testing;

import org.testng.annotations.*;
import io.restassured.RestAssured;
import static io.restassured.RestAssured.*;
import static org.hamcrest.Matchers.*;
import io.restassured.response.ValidatableResponse;
import org.json.JSONObject;

public class BaseTest{
	
	protected static String token;

	@BeforeSuite
	public void setUp(){
		
		RestAssured.baseURI = "http://localhost";
		RestAssured.port = 80;
		RestAssured.basePath = "/wordpress/wp-json";
		
	}
	
	protected void doAuth(){
		if(token==null){
			final JSONObject reqJson = new JSONObject();
	        reqJson.put("username", "admin");
	        reqJson.put("password", "wpblogauto");
	        
	        ValidatableResponse validatableResponse = given()
	        	.contentType("application/json")
		        .body(reqJson.toString())
		        .when().log().all()
		        .post("/jwt-auth/v1/token")
		        .then();

	        validatableResponse.log().all();
	        validatableResponse.statusCode(200);
	        validatableResponse.body("data", hasKey("token"));
	        token = validatableResponse.extract().path("data.token");
		}
	}
}