package api_testing.utils;

import java.util.Iterator;
import java.util.Arrays;
import java.io.File;
import java.io.FileReader;
import java.io.FileInputStream;
import java.io.IOException;

import org.json.JSONObject;
import org.json.JSONArray;
import org.json.JSONTokener;


public class DDTReader{
    
	public static Object[][] JsonReader(String filePath, String[] keysOfJson)
	{
		Object[][] returnData = new Object[0][];

    	try{
			
			FileInputStream file = new FileInputStream(new File(filePath));
			
			JSONTokener jsonTokener = new JSONTokener(file);

			JSONArray jsonArray = new JSONArray(jsonTokener);

			returnData = new Object[jsonArray.length()][];
			
			for(int i=0; i < jsonArray.length(); i++){
				
				JSONObject jsonObject = jsonArray.getJSONObject(i);
				
				returnData[i] = new Object[keysOfJson.length];
				int no = 0;
				
				for(String key : keysOfJson){
					returnData[i][no] = jsonObject.get(key);
					no++;
				}
			}
			file.close();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return returnData;

    }


}
