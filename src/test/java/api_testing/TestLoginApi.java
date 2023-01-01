package api_testing;

import java.util.*;
import io.restassured.response.ValidatableResponse;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.specification.RequestSpecification;
import static io.restassured.RestAssured.*;
import static io.restassured.matcher.RestAssuredMatchers.*;
import io.restassured.http.ContentType;

import org.hamcrest.Matchers;
import static org.hamcrest.Matchers.*;

import org.testng.annotations.*;
import org.json.JSONObject;


import api_testing.BaseTest;

public class TestLoginApi extends BaseTest{
	
    private String endpoint = "/jwt-auth/v1/token";

    @BeforeClass
    public void setup() {
        RequestSpecBuilder requestSpecBuilder = new RequestSpecBuilder();
        requestSpecBuilder
                .setContentType(ContentType.JSON)
                .setAccept(ContentType.JSON);
        requestSpecification = requestSpecBuilder.build();
    } 

    @Test(dataProvider = "user_accounts")
    public void verifyLoginFunctiosnality(
        String username, 
        String password, 
        int statusCode,
        String messageCode){
        
        final JSONObject reqJson = new JSONObject();
        
        reqJson.put("username", username);
        reqJson.put("password", password);
       
        ValidatableResponse validatableResponse = given()
            .body(reqJson.toString())
            .when().log().all()
            .post(endpoint)
            .then();

        validatableResponse.body("statusCode", is(statusCode));
        validatableResponse.body("code", is(messageCode));
        if(validatableResponse.extract().path("success")){
            validatableResponse.body("data", hasKey("token"));
        }else{
            validatableResponse.body("data", not(hasKey("token")));
        }
        
    }   

    @DataProvider(name = "user_accounts")
    private Object[][] userAccounts() {
        return new Object[][] {
            //username, password, status code, message code
            { "", "", 403, "empty_username" },
            { "admin", "", 403, "empty_password" },
            { "admin", "wrong123", 403, "incorrect_password" },
            { "admin", "wpblogauto", 200, "jwt_auth_valid_credential" }
        };
    }
    
}