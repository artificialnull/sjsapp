package com.gabdeg.sjsapp;


import android.preference.Preference;
import android.text.TextUtils;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.HttpCookie;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import javax.net.ssl.HttpsURLConnection;

/**
 * Created by ishan on 8/21/17.
 */

public class Browser {

    static CookieManager cookieManager = new CookieManager();

    public Browser() {
        CookieHandler.setDefault(cookieManager);
    }

    public InputStream get(String urlStr) throws IOException {
        URL url = new URL(urlStr);
        HttpsURLConnection urlConnection = (HttpsURLConnection) url.openConnection();
        if (cookieManager.getCookieStore().getCookies().size() > 0) {
            urlConnection.setRequestProperty(
                    "Cookie", TextUtils.join(
                            ";", cookieManager.getCookieStore().getCookies()
                    )
            );
            Log.v("GET", "There exist >0 cookies");
        }
        return urlConnection.getInputStream();
    }

    public String getAsString(String urlStr) {
        try {
            InputStream inp = get(urlStr);
            Scanner s = new java.util.Scanner(inp).useDelimiter("\\A");
            return s.hasNext() ? s.next() : "";
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }


    public InputStream postJSON(String urlStr, JSONObject toPost) throws IOException {
        URL url = new URL(urlStr);
        HttpsURLConnection urlConnection = (HttpsURLConnection) url.openConnection();
        urlConnection.setDoOutput(true);
        urlConnection.setChunkedStreamingMode(0);
        urlConnection.setRequestProperty("Content-Type", "application/json");

        OutputStream outp = urlConnection.getOutputStream();
        BufferedWriter out = new BufferedWriter(new OutputStreamWriter(outp, "UTF-8"));
        out.write(toPost.toString());
        out.flush();
        out.close();

        Map<String, List<String>> headerFields = urlConnection.getHeaderFields();
        List<String> cookieHeader = headerFields.get("Set-Cookie");

        if (cookieHeader != null) {
            for (String cookie : cookieHeader) {
                cookieManager.getCookieStore().add(null, HttpCookie.parse(cookie).get(0));
                Log.v("POST", "Cookie: " + cookie);
            }
        }

        return urlConnection.getInputStream();
    }

    public void validateLogIn(String username, String password) {
        try {
            JSONObject status = new JSONObject(
                    getAsString("https://sjs.myschoolapp.com/api/webapp/userstatus")
            );
            if (!status.getBoolean("TokenValid")) {
                signIn(username, password);
            }
        } catch (JSONException e) {
            e.printStackTrace();
            signIn(username, password);
        }
    }

    public boolean checkCredentialLegitimacy(String username, String password) {
        try {
            signIn(username, password);

            JSONObject status = new JSONObject(
                    getAsString("https://sjs.myschoolapp.com/api/webapp/userstatus")
            );
            if (!status.getBoolean("TokenValid")) {
                return false;
            }
        } catch (JSONException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public boolean signIn(String username, String password) {
        try {

            JSONObject toPost = new JSONObject();
            toPost.put("Username", username);
            toPost.put("Password", password);
            postJSON("https://sjs.myschoolapp.com/api/SignIn", toPost);

        } catch (JSONException | IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public JSONArray getAssignmentJSON() {
        try {
            return new JSONArray(getAsString(
                    "https://sjs.myschoolapp.com/api/DataDirect/AssignmentCenterAssignments/"
                            + "?format=json&filter=2&persona=2")
            );
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }

    public JSONArray getScheduleJSON(Date date) {

        SimpleDateFormat formatter = new SimpleDateFormat("M'%2F'd%'2F'yyyy");
        String dateStr = formatter.format(date);
        Log.v("DATE", dateStr);

        try {
            return new JSONArray(getAsString(
                    "https://sjs.myschoolapp.com/api/schedule/MyDayCalendarStudentList/?scheduleDate="
                            + dateStr)
            );
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }

    }

    public ArrayList<ScheduledClass> getScheduleList(Date date) {

        try {
            JSONArray jsonClasses = getScheduleJSON(date);

            ArrayList<ScheduledClass> scheduledClasses = new ArrayList<>();

            for (int i = 0; i < jsonClasses.length(); i++) {
                JSONObject jsonClass = jsonClasses.getJSONObject(i);
                scheduledClasses.add(
                        new ScheduledClass()
                                .setClassName(jsonClass.getString("CourseTitle"))
                                .setClassStart(jsonClass.getString("MyDayStartTime"))
                                .setClassEnd(jsonClass.getString("MyDayEndTime"))
                                .setClassBlock(jsonClass.getString("Block"))
                                .setClassRoom(jsonClass.getString("RoomNumber"))
                                .setClassTeacher(jsonClass.getString("Contact"))
                );
            }
            return scheduledClasses;
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }

    public ArrayList<Assignment> getAssignmentList() {
        try {
            JSONArray jsonAssignments = getAssignmentJSON();

            ArrayList<Assignment> assignments = new ArrayList<>();

            for (int i = 0; i < jsonAssignments.length(); i++) {
                JSONObject jsonAssignment = jsonAssignments.getJSONObject(i);
                assignments.add(
                        new Assignment()
                                .setAssignmentClass(jsonAssignment.getString("groupname"))
                                .setAssignmentType(jsonAssignment.getString("assignment_type"))
                                .setAssignmentShort(jsonAssignment.getString("short_description"))
                                .setAssignmentLong(jsonAssignment.getString("long_description"))
                                .setAssignmentAssigned(jsonAssignment.getString("date_assigned"))
                                .setAssignmentDue(jsonAssignment.getString("date_due"))
                                .setAssignmentStatus(jsonAssignment.getInt("assignment_status"))
                                .setAssignmentIndexID(jsonAssignment.getInt("assignment_index_id"))
                );
            }
            return assignments;

        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }

    public void updateAssignmentStatus(Assignment assignment) {
        try {
            JSONObject toPost = new JSONObject();
            toPost.put("assignmentIndexId", assignment.assignmentIndexID);
            String[] choices = {"To Do", "In Progress", "Completed"};
            toPost.put("assignmentStatus",
                    Arrays.asList(choices).indexOf(assignment.getAssignmentStatus()) - 1
            );
            Log.v("ASSIGNMENTS", toPost.toString(2));
            postJSON("https://sjs.myschoolapp.com/api/assignment2/assignmentstatusupdate", toPost);

        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }
    }

    //for testing only
    public String getAssignmentJSONString(int indent) {
        try {
            return getAssignmentJSON().toString(indent);
        } catch (JSONException e) {
            e.printStackTrace();
            return "null";
        }
    }

    public static class Assignment {

        public String getAssignmentClass() {
            return assignmentClass;
        }

        public Assignment setAssignmentClass(String assignmentClass) {
            this.assignmentClass = assignmentClass;
            return this;
        }

        public String getAssignmentShort() {
            return assignmentShort;
        }

        public Assignment setAssignmentShort(String assignmentShort) {
            this.assignmentShort = assignmentShort;
            return this;
        }

        public String getAssignmentLong() {
            return assignmentLong;
        }

        public Assignment setAssignmentLong(String assignmentLong) {
            this.assignmentLong = assignmentLong;
            return this;
        }

        public String getAssignmentType() {
            return assignmentType;
        }

        public Assignment setAssignmentType(String assignmentType) {
            this.assignmentType = assignmentType;
            return this;
        }

        public String getAssignmentAssigned() {
            return assignmentAssigned;
        }

        public Assignment setAssignmentAssigned(String assignmentAssigned) {
            this.assignmentAssigned = assignmentAssigned;
            return this;
        }

        public String getAssignmentDue() {
            return assignmentDue;
        }

        public Assignment setAssignmentDue(String assignmentDue) {
            this.assignmentDue = assignmentDue;
            return this;
        }

        public String getAssignmentStatus() {
            return assignmentStatus;
        }

        public Assignment setAssignmentStatus(String assignmentStatus) {
            this.assignmentStatus = assignmentStatus;
            return this;
        }

        public Assignment setAssignmentStatus(int assignmentStatus) {
            switch (assignmentStatus) {
                case -1:
                    this.assignmentStatus = "To Do";
                    return this;
                case 0:
                    this.assignmentStatus = "In Progress";
                    return this;
                case 1:
                    this.assignmentStatus = "Completed";
                    return this;
                default:
                    this.assignmentStatus = "Unknown";
                    return this;
            }
        }

        String assignmentStatus;
        String assignmentClass;
        String assignmentShort;
        String assignmentLong;
        String assignmentType;
        String assignmentAssigned;
        String assignmentDue;

        public int getAssignmentIndexID() {
            return assignmentIndexID;
        }

        public Assignment setAssignmentIndexID(int assignmentIndexID) {
            this.assignmentIndexID = assignmentIndexID;
            return this;
        }

        int assignmentIndexID;
    }

    public static class ScheduledClass {

        public String getClassName() {
            return className;
        }

        public ScheduledClass setClassName(String className) {
            this.className = className;
            return this;
        }

        public String getClassStart() {
            return classStart;
        }

        public ScheduledClass setClassStart(String classStart) {
            this.classStart = classStart;
            return this;
        }

        public String getClassEnd() {
            return classEnd;
        }

        public ScheduledClass setClassEnd(String classEnd) {
            this.classEnd = classEnd;
            return this;
        }

        public String getClassRoom() {
            return classRoom;
        }

        public ScheduledClass setClassRoom(String classRoom) {
            this.classRoom = classRoom;
            return this;
        }

        public String getClassTeacher() {
            return classTeacher;
        }

        public ScheduledClass setClassTeacher(String classTeacher) {
            this.classTeacher = classTeacher;
            return this;
        }

        public String getClassBlock() {
            return classBlock;
        }

        public ScheduledClass setClassBlock(String classBlock) {
            this.classBlock = classBlock;
            return this;
        }

        String className;
        String classStart;
        String classEnd;
        String classRoom;
        String classTeacher;
        String classBlock;

    }

}
