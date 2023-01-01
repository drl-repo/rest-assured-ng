package api_testing;

import java.util.*;
import io.restassured.RestAssured;
import io.restassured.path.json.JsonPath;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.specification.RequestSpecification;
import static io.restassured.RestAssured.*;
import static io.restassured.matcher.RestAssuredMatchers.*;
import io.restassured.http.ContentType;
import io.restassured.response.ValidatableResponse;
import static io.restassured.module.jsv.JsonSchemaValidator.*;
import io.restassured.path.json.JsonPath;

import org.hamcrest.Matchers;
import static org.hamcrest.Matchers.*;

import org.testng.annotations.*;
import org.testng.Assert;

import org.json.JSONObject;
import org.json.JSONArray;

import static api_testing.utils.DDTReader.JsonReader;

import api_testing.BaseTest;

public class TestCategoryApi extends BaseTest{

    private String endpoint = "/wp/v2/categories";
    private int freshCategoryId;

	@BeforeClass
    public void setup() {
        RequestSpecBuilder requestSpecBuilder = new RequestSpecBuilder();
        super.doAuth();
        requestSpecBuilder
                .setContentType(ContentType.JSON)
                .setAccept(ContentType.JSON)
                .addHeader("Authorization", "Bearer " + token);
                
        requestSpecification = requestSpecBuilder.build();
        clearExistingCategories();
    }

    private void clearExistingCategories(){
        
        String json = given()
            .get(endpoint)
            .asString();

        final JSONObject req = new JSONObject();
        req.put("force", true);
        
        JSONArray jsonArray = new JSONArray(json);
        for(int i=0; i < jsonArray.length(); i++){
            int categoryId = (int) jsonArray.getJSONObject(i).get("id");
            if( categoryId != 1){
                given()
                    .body(req.toString())
                    .log().all()
                    .delete(endpoint+"/"+categoryId)
                    .then()
                    .assertThat()
                    .statusCode(200)
                    .body("$", hasKey("deleted"))
                    .body("deleted", equalTo(true));
            }    
        }
    }
    
    @DataProvider(name="create_category_case")
    private Object[][] userAccounts() {
        String[] keysOfJson = { "testCase", "testType", "statusCode", "expectKey", "expectValue","data"};
        return JsonReader("src/test/resources/spec/category_add_spec.json", keysOfJson);
    }

    @Test(priority=0, dataProvider="create_category_case")
    public void createCategory(
        String testCase,
        String testType,
        int statusCode,
        String expectKey,
        String expectValue,
        JSONObject reqJson
        ){
    
        RequestSpecification requestSpec = given()
            .body(reqJson.toString())
            .when();
        //requestSpec.log().all();    

        ValidatableResponse validatableResp = requestSpec.post(endpoint)
            .then();

        if(validatableResp.extract().statusCode()==201){
            freshCategoryId = validatableResp.extract().path("id");
        }

        //validatableResp.log().all();
        validatableResp.assertThat()
            .statusCode(statusCode)
            .body("$", hasKey(expectKey))
            .body(expectKey, equalTo(expectValue));
    }

    @Test(priority=1)
    public void testGetListCategories(){
    	
    	given()
            .get(endpoint)
            .then()
            .assertThat()
            .body("$", Matchers.instanceOf(List.class))
            .body("size()",is(4))
    		.body("[0]", Matchers.instanceOf(Map.class))
            .body("[0]", hasKey("count"))
            .body("[0]", hasKey("description"))
            .body("[0]", hasKey("link"))
            .body("[0]", hasKey("name"))
            .body("[0]", hasKey("slug"))
            .body("[0]", hasKey("taxonomy"))
            .body("[0]", hasKey("parent"))
            .body("[0]", hasKey("meta"))
            .body("[0].meta", Matchers.instanceOf(List.class));
    }

    @Test(priority=2)
    public void validateSchemaListCategories(){
        //validate using draft-06
        given()
            .get(endpoint)
            .then()
            .assertThat()
            .body(matchesJsonSchemaInClasspath("schema/list_categories_schema.json"));
    }

    @Test(priority=3)
    public void testGetNewlyCreatedCategory(){
        get(endpoint+"/"+freshCategoryId)
        .then()
            .log().all()
            .statusCode(200)
        .assertThat()
            .body("", Matchers.instanceOf(Map.class))
            .body("", hasKey("id"))
            .body("id", equalTo(freshCategoryId))
            .body("", hasKey("count"))
            .body("", hasKey("description"))
            .body("", hasKey("link"))
            .body("", hasKey("name"))
            .body("", hasKey("slug"))
            .body("", hasKey("taxonomy"))
            .body("", hasKey("parent"))
            .body("", hasKey("meta"));
    }

    @Test(priority=4)
    public void validateSchemaSingleCategory(){
        //validate using draft-06
        given()
            .get(endpoint+"/"+freshCategoryId)
            .then()
            .assertThat()
            .body(matchesJsonSchemaInClasspath("schema/category_schema.json"));
    }
    
    @DataProvider(name="category_edit_case")
    private Object[][] categoryEditSpec(){
        String[] keysOfJson = { "testCase", "statusCode", "expectKey", "expectValue", "data" };
        return JsonReader("src/test/resources/spec/category_edit_spec.json", keysOfJson);
    }
    

    @Test(priority=5, dataProvider="category_edit_case")
    public void testUpdateCategory(
        String testCase,
        int statusCode,
        String expectKey,
        String expectValue,
        JSONObject reqBody){
    
        given()
            .body(reqBody.toString())
            .when().log().all()
            .post(endpoint+"/"+freshCategoryId)
            .then().log().all()
            .assertThat()
            .statusCode(statusCode)
            .body("", hasKey(expectKey))
            .body(expectKey, equalTo(expectValue));
    }

  
    @Test(priority=6)
    public void testDeleteCategory(){

        final JSONObject req = new JSONObject();
        req.put("force", true);

        given()
            .body(req.toString())
            .when()
            .log().all()
            .delete(endpoint+"/"+freshCategoryId)
            .then()
            .log().all()
            .statusCode(200);
    }

}