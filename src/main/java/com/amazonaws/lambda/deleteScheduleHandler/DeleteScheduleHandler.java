package com.amazonaws.lambda.deleteScheduleHandler;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
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
public class DeleteScheduleHandler implements RequestStreamHandler {

	public LambdaLogger logger = null;

	// handle to our s3 storage
	private AmazonS3 s3 = AmazonS3ClientBuilder.standard()
			.withRegion("us-east-2").build();

	boolean useRDS = true;
	
//	// not yet connected to RDS, so comment this out...
//	public double loadConstant(String arg) {
//		if (useRDS) {
//			double val = 0;
//			try {
//				val = loadValueFromRDS(arg);
//				return val;
//			} catch (Exception e) {
//				return 0;
//			}
//		}
//		
//		return loadValueFromBucket(arg);
//	}
//
//	/** Load from RDS, if it exists
//	 * 
//	 * @throws Exception 
//	 */
//	double loadValueFromRDS(String arg) throws Exception {
//		if (logger != null) { logger.log("in loadValue"); }
//		ConstantsDAO dao = new ConstantsDAO();
//		Constant constant = dao.getConstant(arg);
//		return constant.value;
//	}
//	
//	/** Load up S3 Bucket with given key and interpret contents as double. */
//	double loadValueFromBucket(String arg) {
//		if (logger != null) { logger.log("load from bucket:" + arg); }
//		try {
//			S3Object pi = s3.getObject("cs3733/constants", arg);
//			if (pi == null) {
//				return 0;
//			} else {
//				S3ObjectInputStream pis = pi.getObjectContent();
//				Scanner sc = new Scanner(pis);
//				String val = sc.nextLine();
//				sc.close();
//				try { pis.close(); } catch (IOException e) { }
//				try {
//					return Double.valueOf(val);
//				} catch (NumberFormatException nfe) {
//					return 0.0;
//				}
//			}
//		} catch (SdkClientException sce) {
//			return 0;
//		}
//	}

	boolean deleteSchedule(String scheduleCode, String secretCode) throws Exception {
		SchedulerDAO dao = new SchedulerDAO();	
		return dao.deleteSchedule(scheduleCode, secretCode);	
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

		DeleteScheduleResponse response = null;
		
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
				response = new DeleteScheduleResponse("Option", 200);  // OPTIONS needs a 200 response
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
			response = new DeleteScheduleResponse("Failure", 400);  // unable to process input
	        responseJson.put("body", new Gson().toJson(response));
	        processed = true;
	        body = null;
		}

		if (!processed) 
		{
			DeleteScheduleRequest req = new Gson().fromJson(body, DeleteScheduleRequest.class);
			logger.log(req.toString());
			
			logger.log("***"+req.toString()+"***");
			// compute proper response
			DeleteScheduleResponse resp;
			logger.log(" ***Request made succ*** ");
			try {
				logger.log(" **** In the Try loop *** ");
				logger.log(req.scheduleCode);
				logger.log(req.secretCode);
				boolean del = deleteSchedule(req.scheduleCode, req.secretCode);
				if (del) {
					logger.log(" *** It definitely worked right? probably. *** ");
					resp = new DeleteScheduleResponse(req.scheduleCode, req.secretCode, 200);					
				}
				else {
					logger.log(" *** fuck it failed *** ");
					resp = new DeleteScheduleResponse("The schedule could not be deleted", 400);					
					}
				logger.log("WTF");
				} 
			catch (Exception e) 
			{
				logger.log(" ***EXCEPTION*** " + e);
				resp = new DeleteScheduleResponse("Something went wrong in the database", 400);					
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
}
