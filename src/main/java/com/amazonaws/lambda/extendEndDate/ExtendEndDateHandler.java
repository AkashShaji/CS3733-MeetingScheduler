package com.amazonaws.lambda.extendEndDate;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.Scanner;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.amazonaws.SdkClientException;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestStreamHandler;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import com.google.gson.Gson;

import entity.Schedule;
import db.SchedulerDAO;


/**
 * Found gson JAR file from
 * https://repo1.maven.org/maven2/com/google/code/gson/gson/2.6.2/gson-2.6.2.jar
 */
public class ExtendEndDateHandler implements RequestStreamHandler {

	public LambdaLogger logger = null;

	// handle to our s3 storage
	private AmazonS3 s3 = AmazonS3ClientBuilder.standard()
			.withRegion("us-east-2").build();

	boolean useRDS = true;
	

	boolean extendEndDate(String shareCode, String organizerCode, String date) throws Exception {
		SchedulerDAO dao = new SchedulerDAO();
		GregorianCalendar endDate = parseDate(date);
		return dao.extendEndDate(shareCode, organizerCode, endDate);	
	}
	
	@Override
	public void handleRequest(InputStream input, OutputStream output, Context context) throws IOException {
		LambdaLogger logger = context.getLogger();
		logger.log("Loading Java Lambda handler of RequestStreamHandler");

		JSONObject headerJson = new JSONObject();
		headerJson.put("Content-Type",  "application/json");  // not sure if needed anymore?
		headerJson.put("Access-Control-Allow-Methods", "GET,POST,OPTIONS");
	    headerJson.put("Access-Control-Allow-Origin",  "*");
	        
		JSONObject responseJson = new JSONObject();
		responseJson.put("headers", headerJson);

		ExtendEndDateResponse response = null;
		
		// extract body from incoming HTTP POST request. If any error, then return 422 error
		String body;
		boolean processed = false;
		
		
		try {
			BufferedReader reader = new BufferedReader(new InputStreamReader(input));
			JSONParser parser = new JSONParser();
			JSONObject event = (JSONObject) parser.parse(reader);
			logger.log("event:" + event.toJSONString());
			
			String method = (String) event.get("httpMethod");
			if (method != null && method.equalsIgnoreCase("OPTIONS")) {
				logger.log("Options request");
				response = new ExtendEndDateResponse("Option", 200);  // OPTIONS needs a 200 response
		        responseJson.put("body", new Gson().toJson(response));
		        processed = true;
		        body = null;
			} else {
				body = (String)event.get("body");
				if (body == null) {
					body = event.toJSONString();  // this is only here to make testing easier
				}
			}
		} catch (ParseException pe) {
			logger.log(pe.toString());
			response = new ExtendEndDateResponse("Failure", 400);  // unable to process input
	        responseJson.put("body", new Gson().toJson(response));
	        processed = true;
	        body = null;
		}

		if (!processed) 
		{
			ExtendEndDateRequest req = new Gson().fromJson(body, ExtendEndDateRequest.class);
			logger.log(req.toString());
			
			logger.log("***"+req.toString()+"***");
			// compute proper response
			ExtendEndDateResponse resp;
			logger.log(" ***Request made successfully*** ");
			try {
				logger.log(" **** In the Try loop *** ");
				boolean s = extendEndDate(req.shareCode, req.organizerCode, req.newEndDate);
				if (s) {
					System.out.println("Found schedule to extend!");
					resp = new ExtendEndDateResponse(req.newEndDate.toString(), 200);					
				}
				else {
					System.out.println("No schedules found");
					resp = new ExtendEndDateResponse("No schedules were found", 400);					
					}
				} 
			catch (Exception e) 
			{
				logger.log(" ***EXCEPTION*** " + e);
				resp = new ExtendEndDateResponse("Something went wrong in the database", 400);					
			}
	        
			logger.log(" ***something did happen*** ");
			logger.log(resp.toString());
			responseJson.put("body", new Gson().toJson(resp));  
		}
		
        logger.log("end result:" + responseJson.toJSONString());
        logger.log(responseJson.toJSONString());
        OutputStreamWriter writer = new OutputStreamWriter(output, "UTF-8");
        writer.write(responseJson.toJSONString());  
        writer.close();
	}
	
	public GregorianCalendar parseDate(String date) { ///take in date as "YYYY-MM-DD"
		int year = Integer.parseInt(date.substring(0, 4));
		int month = Integer.parseInt(date.substring(5, 7));
		int day = Integer.parseInt(date.substring(8));
		return new GregorianCalendar(year, month-1, day);
	}
}