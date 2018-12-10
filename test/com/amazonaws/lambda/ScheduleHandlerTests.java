package com.amazonaws.lambda;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.TimeUnit;

import org.json.simple.JSONObject;
import org.junit.Assert;
import org.junit.Test;

import com.amazonaws.lambda.cancelMeeting.CancelMeetingHandler;
import com.amazonaws.lambda.cancelMeeting.CancelMeetingRequest;
import com.amazonaws.lambda.cancelMeetingParticipant.CancelMeetingParticipantHandler;
import com.amazonaws.lambda.cancelMeetingParticipant.CancelMeetingParticipantRequest;
import com.amazonaws.lambda.closeAllSlotsDay.CloseAllSlotsDayHandler;
import com.amazonaws.lambda.closeAllSlotsDay.CloseAllSlotsDayRequest;
import com.amazonaws.lambda.closeAllTimeSlotsTime.CloseAllTimeSlotsTimeHandler;
import com.amazonaws.lambda.closeAllTimeSlotsTime.CloseAllTimeSlotsTimeRequest;
import com.amazonaws.lambda.createMeeting.CreateMeetingHandler;
import com.amazonaws.lambda.createMeeting.CreateMeetingRequest;
import com.amazonaws.lambda.createSchedule.CreateScheduleHandler;
import com.amazonaws.lambda.createSchedule.CreateScheduleRequest;
import com.amazonaws.lambda.deleteSchedule.DeleteScheduleHandler;
import com.amazonaws.lambda.deleteSchedule.DeleteScheduleRequest;
import com.amazonaws.lambda.getSchedule.GetScheduleRequest;
import com.amazonaws.lambda.getSchedule.OrganizerGetScheduleHandler;
import com.amazonaws.lambda.openAllSlotsDay.OpenAllSlotsDayHandler;
import com.amazonaws.lambda.openAllSlotsDay.OpenAllSlotsDayRequest;
import com.amazonaws.lambda.openAllTimeSlotsTime.OpenAllTimeSlotsTimeHandler;
import com.amazonaws.lambda.openAllTimeSlotsTime.OpenAllTimeSlotsTimeRequest;
import com.amazonaws.lambda.openOrCloseTimeSlot.OpenOrCloseTimeSlotHandler;
import com.amazonaws.lambda.openOrCloseTimeSlot.OpenOrCloseTimeSlotRequest;
import com.amazonaws.services.lambda.runtime.Context;
import com.google.gson.Gson;

import db.DatabaseUtils;
import junit.framework.TestCase;

/**
 * A simple test harness for locally invoking your Lambda function handler.
 */
public class ScheduleHandlerTests extends TestCase{

	public String tempShareCode = "";
	public String tempOrganizerCode = "";
	public String tempMeetingCode = "";
	java.sql.Connection conn;

	Context createContext(String apiCall) {
        TestContext ctx = new TestContext();
        ctx.setFunctionName(apiCall);
        return ctx;
    }

    /**
     * Basically verifies that a schedule is created
     * @throws Exception 
     */
    @Test
    public void testCreateDeleteScheduleHandler() throws Exception {// throws IOException {
    	
    	//*******************************************
    	//Create Schedule
    	Context c = createContext("test");
    	conn = DatabaseUtils.connect();
    	
    	CreateScheduleRequest req = new CreateScheduleRequest("toBeDeleted", 60, "2018-10-26", "2018-10-27", 300, 400);
    	CreateScheduleHandler handler = new CreateScheduleHandler();
    	JSONObject reqJson = new JSONObject();
    	reqJson.put("body", new Gson().toJson(req));
    	
    	
        InputStream input = new ByteArrayInputStream(reqJson.toJSONString().getBytes());;
        OutputStream output = new ByteArrayOutputStream();
                
        handler.handleRequest(input, output, c);
        tempShareCode = handler.getShareCode();
        tempOrganizerCode = handler.getOrganizerCode();
        System.out.println("SHARE CODE: " + tempShareCode + ", ORGANIZER CODE: "+tempOrganizerCode);

        String sampleOutputString = output.toString();
        System.out.println("output");
        System.out.println(sampleOutputString);
        System.out.println("/output");
        //*******************************************
        //Create Meeting
    	CreateMeetingRequest req3 = new CreateMeetingRequest(tempShareCode, tempOrganizerCode, 300, "2018-10-26", "Samuel Vimes", "The Watch");
    	CreateMeetingHandler handler3 = new CreateMeetingHandler();
    	JSONObject req3Json = new JSONObject();
    	req3Json.put("body", new Gson().toJson(req3));
    	
    	
        InputStream input3 = new ByteArrayInputStream(req3Json.toJSONString().getBytes());;
        OutputStream output3 = new ByteArrayOutputStream();
                
        handler3.handleRequest(input3, output3, c);

        String sampleOutputString3 = output3.toString();
        System.out.println("output3");
        System.out.println(sampleOutputString3);
        System.out.println("/output3");
        
        //*******************************************
        //Cancel Meeting
    	CancelMeetingRequest req4 = new CancelMeetingRequest(tempShareCode, tempOrganizerCode, 300, "2018-10-26");
    	CancelMeetingHandler handler4 = new CancelMeetingHandler();
    	JSONObject req4Json = new JSONObject();
    	req4Json.put("body", new Gson().toJson(req4));
    	
    	
        InputStream input4 = new ByteArrayInputStream(req4Json.toJSONString().getBytes());;
        OutputStream output4 = new ByteArrayOutputStream();
                
        handler4.handleRequest(input4, output4, c);

        String sampleOutputString4 = output4.toString();
        System.out.println("output4");
        System.out.println(sampleOutputString4);
        System.out.println("/output4");
        
        //*******************************************
        //Cancel Meeting Participant
        
    	CreateMeetingRequest req31 = new CreateMeetingRequest(tempShareCode, tempOrganizerCode, 300, "2018-10-26", "Samuel Vimes", "The Watch");
    	CreateMeetingHandler handler31 = new CreateMeetingHandler();
    	JSONObject req31Json = new JSONObject();
    	req31Json.put("body", new Gson().toJson(req31));
    	
    	
        InputStream input31 = new ByteArrayInputStream(req31Json.toJSONString().getBytes());;
        OutputStream output31 = new ByteArrayOutputStream();
                
        handler31.handleRequest(input31, output31, c);
        
        CancelMeetingParticipantRequest req5 = new CancelMeetingParticipantRequest(tempShareCode, "The Watch");
        CancelMeetingParticipantHandler handler5 = new CancelMeetingParticipantHandler();
    	JSONObject req5Json = new JSONObject();
    	req5Json.put("body", new Gson().toJson(req5));
    	
    	
        InputStream input5 = new ByteArrayInputStream(req5Json.toJSONString().getBytes());;
        OutputStream output5 = new ByteArrayOutputStream();
                
        handler5.handleRequest(input5, output5, c);

        String sampleOutputString5 = output5.toString();
        System.out.println("output5");
        System.out.println(sampleOutputString5);
        System.out.println("/output5");
        
        //*******************************************
        //Close Slots Day
        CloseAllSlotsDayRequest req6 = new CloseAllSlotsDayRequest(tempShareCode, tempOrganizerCode, "2018-10-26");
        CloseAllSlotsDayHandler handler6 = new CloseAllSlotsDayHandler();
    	JSONObject req6Json = new JSONObject();
    	req6Json.put("body", new Gson().toJson(req6));
    	
    	
        InputStream input6 = new ByteArrayInputStream(req6Json.toJSONString().getBytes());;
        OutputStream output6 = new ByteArrayOutputStream();
                
        handler6.handleRequest(input6, output6, c);

        String sampleOutputString6 = output6.toString();
        System.out.println("output6");
        System.out.println(sampleOutputString6);
        System.out.println("/output6");
        
        //*******************************************
        //Open Slots Day
        OpenAllSlotsDayRequest req7 = new OpenAllSlotsDayRequest(tempShareCode, tempOrganizerCode, "2018-10-26");
        OpenAllSlotsDayHandler handler7 = new OpenAllSlotsDayHandler();
    	JSONObject req7Json = new JSONObject();
    	req7Json.put("body", new Gson().toJson(req7));
    	
    	
        InputStream input7 = new ByteArrayInputStream(req7Json.toJSONString().getBytes());;
        OutputStream output7 = new ByteArrayOutputStream();
                
        handler7.handleRequest(input7, output7, c);

        String sampleOutputString7 = output7.toString();
        System.out.println("output7");
        System.out.println(sampleOutputString7);
        System.out.println("/output7");
        
        //*******************************************
        //Close Slots Time
        CloseAllTimeSlotsTimeRequest req8 = new CloseAllTimeSlotsTimeRequest(tempShareCode, tempOrganizerCode, 300);
        CloseAllTimeSlotsTimeHandler handler8 = new CloseAllTimeSlotsTimeHandler();
    	JSONObject req8Json = new JSONObject();
    	req8Json.put("body", new Gson().toJson(req8));
    	
    	
        InputStream input8 = new ByteArrayInputStream(req8Json.toJSONString().getBytes());;
        OutputStream output8 = new ByteArrayOutputStream();
                
        handler8.handleRequest(input8, output8, c);

        String sampleOutputString8 = output8.toString();
        System.out.println("output8");
        System.out.println(sampleOutputString8);
        System.out.println("/output8");
        
        //*******************************************
        //Open Slots Time
        OpenAllTimeSlotsTimeRequest req9 = new OpenAllTimeSlotsTimeRequest(tempShareCode, tempOrganizerCode, 300);
        OpenAllTimeSlotsTimeHandler handler9 = new OpenAllTimeSlotsTimeHandler();
    	JSONObject req9Json = new JSONObject();
    	req9Json.put("body", new Gson().toJson(req9));
    	
    	
        InputStream input9 = new ByteArrayInputStream(req9Json.toJSONString().getBytes());;
        OutputStream output9 = new ByteArrayOutputStream();
                
        handler9.handleRequest(input9, output9, c);

        String sampleOutputString9 = output9.toString();
        System.out.println("output9");
        System.out.println(sampleOutputString9);
        System.out.println("/output9");
        
        //*******************************************
        //Open/Close Timeslot
        //OpenOrCloseTimeSlotRequest req10 = new OpenOrCloseTimeSlotRequest(tempShareCode, tempOrganizerCode, 0300, "2018-10-26");
    	OpenOrCloseTimeSlotRequest req10 = new OpenOrCloseTimeSlotRequest("UAMH9LE21Y", "KTL849UY1Z", 1000, "2018-04-20");
        OpenOrCloseTimeSlotHandler handler10 = new OpenOrCloseTimeSlotHandler();
    	JSONObject req10Json = new JSONObject();
    	req10Json.put("body", new Gson().toJson(req10));
    	
    	
        InputStream input10 = new ByteArrayInputStream(req10Json.toJSONString().getBytes());;
        OutputStream output10 = new ByteArrayOutputStream();
                
        handler10.handleRequest(input10, output10, c);

        String sampleOutputString10 = output10.toString();
        System.out.println("output10");
        System.out.println(sampleOutputString10);
        System.out.println("/output10");
        
        //*******************************************
        //Get Schedule
        GetScheduleRequest req11 = new GetScheduleRequest(tempShareCode, tempOrganizerCode);
        OrganizerGetScheduleHandler handler11 = new OrganizerGetScheduleHandler();
    	JSONObject req11Json = new JSONObject();
    	req11Json.put("body", new Gson().toJson(req11));
    	
    	
        InputStream input11 = new ByteArrayInputStream(req11Json.toJSONString().getBytes());;
        OutputStream output11 = new ByteArrayOutputStream();
                
        handler11.handleRequest(input11, output11, c);

        String sampleOutputString11 = output11.toString();
        System.out.println("output11");
        System.out.println(sampleOutputString11);
        System.out.println("/output11");
        
        //*******************************************
        //Delete Schedule
    	DeleteScheduleRequest req2 = new DeleteScheduleRequest(tempShareCode, tempOrganizerCode);
    	DeleteScheduleHandler handler2 = new DeleteScheduleHandler();
    	JSONObject req2Json = new JSONObject();
    	req2Json.put("body", new Gson().toJson(req2));
    	
    	
        InputStream input2 = new ByteArrayInputStream(req2Json.toJSONString().getBytes());;
        OutputStream output2 = new ByteArrayOutputStream();
                
        handler2.handleRequest(input2, output2, c);

        String sampleOutputString2 = output2.toString();
        System.out.println("output2");
        System.out.println(sampleOutputString2);
        System.out.println("/output2");
      //*******************************************
        
    }
    
//    @Test
//    public void testDeleteScheduleHandler() throws Exception {// throws IOException {
//    	Context c = createContext("test");
//    	conn = DatabaseUtils.connect();
//    	String scheduleName = "toBeDeleted";
//    	
//    	String query = "SELECT * FROM Schedule WHERE scheduleID = ?";
//    	PreparedStatement ps = conn.prepareStatement(query);
//    	
//    	ps.setInt(1, 3);
//    	ResultSet resultSet1 = ps.executeQuery();
//    	String shareCode = resultSet1.getString("shareCode");
//    	String organizerCode = resultSet1.getString("organizerCode");
//    	
//    	
//    	DeleteScheduleRequest req = new DeleteScheduleRequest(tempShareCode, tempOrganizerCode);
//    	DeleteScheduleHandler handler = new DeleteScheduleHandler();
//    	JSONObject reqJson = new JSONObject();
//    	reqJson.put("body", new Gson().toJson(req));
//    	
//    	
//        InputStream input = new ByteArrayInputStream(reqJson.toJSONString().getBytes());;
//        OutputStream output = new ByteArrayOutputStream();
//                
//        handler.handleRequest(input, output, c);
//
//        String sampleOutputString = output.toString();
//        System.out.println("output");
//        System.out.println(sampleOutputString);
//        System.out.println("/output");
//        //assertTrue(true);
//    }
}