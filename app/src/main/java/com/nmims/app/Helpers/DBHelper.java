package com.nmims.app.Helpers;

import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.nmims.app.Models.AnnouncementsDataModel;
import com.nmims.app.Models.AttendanceStudentDataModel;
import com.nmims.app.Models.BackendModel;
import com.nmims.app.Models.FacultyNotificationModel;
import com.nmims.app.Models.LaundryDataModel;
import com.nmims.app.Models.LectureList;
import com.nmims.app.Models.LecturesDataModel;
import com.nmims.app.Models.MinMaxDateModel;
import com.nmims.app.Models.MyDate;
import com.nmims.app.Models.MyPermission;
import com.nmims.app.Models.NotificationDataModel;
import com.nmims.app.Models.SchoolAttendanceDataModel;
import com.nmims.app.Models.StudentAssignmentDataModel;
import com.nmims.app.Models.Survey.SurveyName;
import com.nmims.app.Models.Survey.SurveyOption;
import com.nmims.app.Models.Survey.SurveyQuestion;
import com.nmims.app.Models.Survey.SurveyUserResponse;
import com.nmims.app.Models.TimeTableDataModel;
import com.nmims.app.Models.TimetableModel;

import java.util.ArrayList;

public class DBHelper extends SQLiteOpenHelper {
    Context context;
    private static final int DATABASE_VERSION = 11;
    public static final String DATABASE_NAME = "userdata.db";

    String CREATE_TABLE_USERDATA = "CREATE TABLE IF NOT EXISTS userData (" +
            "sapid TEXT, lastName TEXT, firstName TEXT, emailId TEXT," +
            " mobile TEXT, role TEXT, school TEXT, currentSchool TEXT, schoolCount TEXT, copyPreviousAttendanceData TEXT , userImage TEXT , token TEXT)";

    private static final String CREATE_TABLE_DATE =
            "CREATE TABLE IF NOT EXISTS myDate (" +
                    "dateId TEXT , currentDate TEXT, startDate TEXT, endDate TEXT)";

    private static final String CREATE_TABLE_PERMISSION =
            "CREATE TABLE IF NOT EXISTS myPermission (" +
                    "permissionId TEXT , permissionName TEXT, permissionStatus TEXT)";

    private static final String CREATE_TABLE_NOTIFICATION =
            "CREATE TABLE IF NOT EXISTS myNotification (" +
                    "not_id TEXT , playerId TEXT)";

    private static final String CREATE_TABLE_ATTENDANCE_DATA =
            "CREATE TABLE IF NOT EXISTS attendance_data (" +
                    "schoolName TEXT , date TEXT , status TEXT , isExpanded TEXT , endTime TEXT , isApiHit TEXT)";

    private static final String CREATE_TABLE_TEST_DETAILS =
            "CREATE TABLE IF NOT EXISTS test_details (" +
                    "id TEXT , courseId TEXT , testDate TEXT , startDate TEXT , endDate TEXT , testName TEXT, testType TEXT , duration TEXT , createdDate TEXT , showResultsToStudent TEXT , active TEXT, facultyId TEXT , maxmAttempt TEXT , maxmScore TEXT , studentCount TEXT, schoolName TEXT, stud_count TEXT, faculty_name TEXT)";

    private static final String CREATE_TABLE_LECTURE_DATA =
            "CREATE TABLE IF NOT EXISTS lecture_data (" +
                    "lectureId TEXT , facultyId TEXT , flag TEXT , class_date TEXT , start_time TEXT , end_time TEXT , courseId TEXT, courseName TEXT , programId TEXT , programName TEXT , maxEndTime TEXT , dateFlag TEXT , schoolName TEXT , event_id TEXT , allotted_lectures TEXT , conducted_lectures TEXT , remaining_lectures TEXT , presentFacultyId TEXT , UNIQUE (facultyId, class_date, start_time, end_time, courseId))";

    private static final String CREATE_TABLE_STUDENT_DATA =
            "CREATE TABLE IF NOT EXISTS student_data (" +
                    "studentName TEXT , studentSapId TEXT , studentRollNo TEXT , studentAbsentPresent TEXT , courseId TEXT , isMarked TEXT , startTime TEXT , endTime TEXT , isAttendanceSubmitted TEXT , dateEntry TEXT , lectureId TEXT , attendanceStatus TEXT , attendanceFlag TEXT , schoolName TEXT , blockMarking TEXT , createdDate TEXT , lastModifiedDate TEXT , UNIQUE (startTime, endTime, lectureId, studentSapId, courseId))";

    private static final String CREATE_TABLE_ASSIGNMENT_DATA =
            "CREATE TABLE IF NOT EXISTS assignment_data (" +
                    "assignmentId TEXT , assignmentCourse TEXT , assignmentName TEXT , assignmentEndDate TEXT , assignmentStatus TEXT , marksOutOf TEXT , assignmentType TEXT , assignmentFile TEXT , studentFilePath TEXT , assignmentText TEXT , allowAfterEndDate TEXT , submitByOneInGroup TEXT, isSubmitterInGroup TEXT , studentAssignmentId TEXT , evaluationStatus TEXT , lastUpdatedOn TEXT )";

    private static final String CREATE_TABLE_TIMETABLE_DATA =
            "CREATE TABLE IF NOT EXISTS timetable_data (" +
                    "id TEXT , subject TEXT , description TEXT , startDate TEXT , filePath TEXT , acadSession TEXT , acadYear TEXT , lastUpdatedOn TEXT)";

    private static final String CREATE_TABLE_ANNOUNCEMENTS_DATA =
            "CREATE TABLE IF NOT EXISTS announcement_data (" +
                    "id TEXT , announcementType TEXT , subject TEXT , description TEXT , startDate TEXT , endDate TEXT , filePath TEXT , lastUpdatedOn TEXT)";

    private static final String CREATE_TABLE_LAUNDRY_DATA =
            "CREATE TABLE IF NOT EXISTS laundry_data (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT , itemName TEXT , itemType TEXT , itemQuantity TEXT , submitted TEXT , uniqueKey TEXT)";

    private static final String CREATE_TABLE_SURVEY_NAME =
            "CREATE TABLE IF NOT EXISTS survey_name (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT , surveyName TEXT , userId TEXT, startDate TEXT , endDate TEXT , submitted TEXT , responseFlag TEXT , createdDate TEXT , locallySubmitted TEXT)";

    private static final String CREATE_TABLE_SURVEY_QUESTION =
            "CREATE TABLE IF NOT EXISTS survey_question (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT , title TEXT , type_id TEXT , isChecked TEXT , survey_id TEXT  )";

    private static final String CREATE_TABLE_SURVEY_OPTION =
            "CREATE TABLE IF NOT EXISTS survey_option (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT , options TEXT , question_id TEXT , type_id TEXT)";

    private static final String CREATE_TABLE_SURVEY_USER_RESPONSE =
            "CREATE TABLE IF NOT EXISTS survey_user_response (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT , survey_id TEXT , question_id TEXT , response TEXT)";

    private static final String CREATE_FACULTY_NOTIFICATION =
            "CREATE TABLE IF NOT EXISTS faculty_notification (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT , message TEXT , createdDate TEXT , isRead TEXT , UNIQUE (message))";

    private static final String CREATE_BACKEND_CONTROL =
            "CREATE TABLE IF NOT EXISTS backend_control (" +
                    "key TEXT , value TEXT , UNIQUE (key))";

    private static final String CREATE_AUTH_CONTROL =
            "CREATE TABLE IF NOT EXISTS auth_control (" +
                    "authkey TEXT , authvalue TEXT , UNIQUE (authkey))";
    private static final String CREATE_LECTURE_LIST =
            "CREATE TABLE IF NOT EXISTS lecture_timetable (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, room_no TEXT ,date_str TEXT, start_time TEXT , end_time TEXT, div TEXT, acad_session TEXT , event_name TEXT, sap_event_id TEXT, active TEXT )";

    private static final String CREATE_TIMETABLE_DATE =
            "CREATE TABLE IF NOT EXISTS timetable_date (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, dateString TEXT, isHoliday INTEGER)";

    private static final String CREATE_STUDENT_LECTURE_LIST =
            "CREATE TABLE IF NOT EXISTS student_lecture_timetable (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, room_no TEXT ,date_str TEXT, start_time TEXT , end_time TEXT, div TEXT, acad_session TEXT , event_name TEXT, sap_event_id TEXT, active TEXT )";
    private static final String CREATE_DIVISION =
            "CREATE TABLE IF NOT EXISTS student_div (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, div TEXT)";



    public DBHelper( Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE_USERDATA);
        new MyLog(NMIMSApplication.getAppContext()).debug("Create table ", "CREATE_TABLE_USERDATA -->" + CREATE_TABLE_USERDATA);
        db.execSQL(CREATE_TABLE_DATE);
        new MyLog(NMIMSApplication.getAppContext()).debug("Create table ", "CREATE_TABLE_DATE -->" + CREATE_TABLE_DATE);
        db.execSQL(CREATE_TABLE_PERMISSION);
        new MyLog(NMIMSApplication.getAppContext()).debug("Create table ", "CREATE_TABLE_PERMISSION -->" + CREATE_TABLE_PERMISSION);
        db.execSQL(CREATE_TABLE_NOTIFICATION);
        new MyLog(NMIMSApplication.getAppContext()).debug("Create table ", "CREATE_TABLE_NOTIFICATION -->" + CREATE_TABLE_NOTIFICATION);
        db.execSQL(CREATE_TABLE_ATTENDANCE_DATA);
        new MyLog(NMIMSApplication.getAppContext()).debug("Create table ", "CREATE_TABLE_ATTENDANCE_DATA -->" + CREATE_TABLE_ATTENDANCE_DATA);
        db.execSQL(CREATE_TABLE_TEST_DETAILS);
        new MyLog(NMIMSApplication.getAppContext()).debug("Create table ", "CREATE_TABLE_TEST_DETAILS -->" + CREATE_TABLE_TEST_DETAILS);
        db.execSQL(CREATE_TABLE_LECTURE_DATA);
        new MyLog(NMIMSApplication.getAppContext()).debug("Create table ", "CREATE_TABLE_LECTURE_DATA -->" + CREATE_TABLE_LECTURE_DATA);
        db.execSQL(CREATE_TABLE_STUDENT_DATA);
        new MyLog(NMIMSApplication.getAppContext()).debug("Create table ", "CREATE_TABLE_STUDENT_DATA -->" + CREATE_TABLE_STUDENT_DATA);
        db.execSQL(CREATE_TABLE_ASSIGNMENT_DATA);
        new MyLog(NMIMSApplication.getAppContext()).debug("Create table ", "CREATE_TABLE_ASSIGNMENT_DATA -->" + CREATE_TABLE_ASSIGNMENT_DATA);
        db.execSQL(CREATE_TABLE_TIMETABLE_DATA);
        new MyLog(NMIMSApplication.getAppContext()).debug("Create table ", "CREATE_TABLE_TIMETABLE_DATA -->" + CREATE_TABLE_TIMETABLE_DATA);
        db.execSQL(CREATE_TABLE_ANNOUNCEMENTS_DATA);
        new MyLog(NMIMSApplication.getAppContext()).debug("Create table ", "CREATE_TABLE_ANNOUNCEMENTS_DATA -->" + CREATE_TABLE_ANNOUNCEMENTS_DATA);
        db.execSQL(CREATE_TABLE_LAUNDRY_DATA);
        new MyLog(NMIMSApplication.getAppContext()).debug("Create table ", "CREATE_TABLE_LAUNDRY_DATA -->" + CREATE_TABLE_LAUNDRY_DATA);
        db.execSQL(CREATE_TABLE_SURVEY_NAME);
        new MyLog(NMIMSApplication.getAppContext()).debug("Create table ", "CREATE_TABLE_SURVEY_NAME -->" + CREATE_TABLE_SURVEY_NAME);
        db.execSQL(CREATE_TABLE_SURVEY_QUESTION);
        new MyLog(NMIMSApplication.getAppContext()).debug("Create table ", "CREATE_TABLE_SURVEY_QUESTION -->" + CREATE_TABLE_SURVEY_QUESTION);
        db.execSQL(CREATE_TABLE_SURVEY_OPTION);
        new MyLog(NMIMSApplication.getAppContext()).debug("Create table ", "CREATE_TABLE_SURVEY_OPTION -->" + CREATE_TABLE_SURVEY_OPTION);
        db.execSQL(CREATE_TABLE_SURVEY_USER_RESPONSE);
        new MyLog(NMIMSApplication.getAppContext()).debug("Create table ", "CREATE_TABLE_SURVEY_USER_RESPONSE -->" + CREATE_TABLE_SURVEY_USER_RESPONSE);
        db.execSQL(CREATE_FACULTY_NOTIFICATION);
        new MyLog(NMIMSApplication.getAppContext()).debug("Create table ", "CREATE_FACULTY_NOTIFICATION -->" + CREATE_FACULTY_NOTIFICATION);
        db.execSQL(CREATE_BACKEND_CONTROL);
        new MyLog(NMIMSApplication.getAppContext()).debug("Create table ", "CREATE_BACKEND_CONTROL -->" + CREATE_BACKEND_CONTROL);
        db.execSQL(CREATE_AUTH_CONTROL);
        new MyLog(NMIMSApplication.getAppContext()).debug("Create table ", "CREATE_AUTH_CONTROL -->" + CREATE_AUTH_CONTROL);
        db.execSQL(CREATE_LECTURE_LIST);
        new MyLog(NMIMSApplication.getAppContext()).debug("Create table ", "CREATE_MICROSOFT_EVENTS -->" + CREATE_LECTURE_LIST);
        db.execSQL(CREATE_TIMETABLE_DATE);
        db.execSQL(CREATE_STUDENT_LECTURE_LIST);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS userData");
        db.execSQL("DROP TABLE IF EXISTS myDate");
        db.execSQL("DROP TABLE IF EXISTS myPermission");
        db.execSQL("DROP TABLE IF EXISTS myNotification");
        db.execSQL("DROP TABLE IF EXISTS attendance_data");
        db.execSQL("DROP TABLE IF EXISTS test_details");
        db.execSQL("DROP TABLE IF EXISTS lecture_data");
        db.execSQL("DROP TABLE IF EXISTS student_data");
        db.execSQL("DROP TABLE IF EXISTS assignment_data");
        db.execSQL("DROP TABLE IF EXISTS timetable_data");
        db.execSQL("DROP TABLE IF EXISTS announcement_data");
        db.execSQL("DROP TABLE IF EXISTS laundry_data");
        db.execSQL("DROP TABLE IF EXISTS survey_name");
        db.execSQL("DROP TABLE IF EXISTS survey_question");
        db.execSQL("DROP TABLE IF EXISTS survey_option");
        db.execSQL("DROP TABLE IF EXISTS survey_user_response");
        db.execSQL("DROP TABLE IF EXISTS faculty_notification");
        db.execSQL("DROP TABLE IF EXISTS backend_control");
        db.execSQL("DROP TABLE IF EXISTS auth_control");
        db.execSQL("DROP TABLE IF EXISTS timetable_date");
        db.execSQL("DROP TABLE IF EXISTS student_div");
        db.execSQL("DROP TABLE IF EXISTS student_lecture_timetable");

        onCreate(db);
    }

    public Cursor getUserDataValues()
    {
        String sql = "select * from userData";
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(sql, null);

        if (cursor != null){
            if(cursor.moveToFirst()){
                new MyLog(NMIMSApplication.getAppContext()).debug("cursor", cursor.getString(cursor.getColumnIndex("sapid")));
            }
        }
        return cursor;
    }

    public Cursor getLatestCurrentSurveyName()
    {
        String sql = "SELECT MAX(createdDate) as maxCreatedDate FROM survey_name WHERE startDate <= date() and endDate >= date()";
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(sql, null);

        if (cursor != null){
            if(cursor.moveToFirst())
            {

            }
        }
        return cursor;
    }


    public void insertBackendControl(BackendModel backendModel)
    {
        SQLiteDatabase db = this.getWritableDatabase();
        String sql = "insert into backend_control (key , value) values ('"+backendModel.getKey()+"','"+backendModel.getValue()+"')";
        db.execSQL(sql);
        db.close();
        new MyLog(NMIMSApplication.getAppContext()).debug("insertBackendControl", sql);
    }

    public void insertFacultyNotification(FacultyNotificationModel facultyNotificationModel)
    {
        SQLiteDatabase db = this.getWritableDatabase();
        String sql = "insert into faculty_notification (message , createdDate , isRead) values ('"+facultyNotificationModel.getMessage()+"','"+facultyNotificationModel.getCreatedDate()+"','"+facultyNotificationModel.getIsRead()+"')";
        db.execSQL(sql);
        db.close();
        new MyLog(NMIMSApplication.getAppContext()).debug("insertFacultyNotification", sql);
    }

    public void insertSurveyName(SurveyName surveyName)
    {
        SQLiteDatabase db = this.getWritableDatabase();
        String sql = "insert into survey_name (surveyName , userId, startDate , endDate , submitted , responseFlag , createdDate , locallySubmitted) values ('"+surveyName.getSurveyName()+"','"+surveyName.getUserId()+"','"+surveyName.getStartDate()+"','"+surveyName.getEndDate()+"','"+surveyName.getSubmitted()+"','"+surveyName.getResponseFlag()+"','"+surveyName.getCreatedDate()+"','"+surveyName.getLocallySubmitted()+"')";
        db.execSQL(sql);
        db.close();
        new MyLog(NMIMSApplication.getAppContext()).debug("insertSurveyName", sql);
    }

    public void insertSurveyQuestion(SurveyQuestion surveyQuestion)
    {
        SQLiteDatabase db = this.getWritableDatabase();
        String sql = "insert into survey_question (title , type_id , isChecked , survey_id) values ('"+surveyQuestion.getTitle()+"','"+surveyQuestion.getType_id()+"','"+surveyQuestion.getIsChecked()+"','"+surveyQuestion.getSurvey_id()+"')";
        db.execSQL(sql);
        db.close();
        new MyLog(NMIMSApplication.getAppContext()).debug("insertSurveyQuestion", sql);
    }

    public void insertSurveyOption(SurveyOption surveyOption)
    {
        SQLiteDatabase db = this.getWritableDatabase();
        String sql = "insert into survey_option (options , question_id , type_id ) values ('"+surveyOption.getOptions()+"','"+surveyOption.getQuestion_id()+"','"+surveyOption.getType_id()+"')";
        db.execSQL(sql);
        db.close();
        new MyLog(NMIMSApplication.getAppContext()).debug("insertSurveyOption", sql);
    }

    public void insertSurveyUserResponse(SurveyUserResponse surveyUserResponse)
    {
        SQLiteDatabase db = this.getWritableDatabase();
        String sql = "insert into survey_user_response (survey_id , question_id , response ) values ('"+surveyUserResponse.getSurvey_id()+"','"+surveyUserResponse.getQuestion_id()+"','"+surveyUserResponse.getResponse()+"')";
        db.execSQL(sql);
        db.close();
        new MyLog(NMIMSApplication.getAppContext()).debug("insertSurveyUserResponse", sql);
    }


    public void insertLaundryData(LaundryDataModel laundryDataModel)
    {
        SQLiteDatabase db = this.getWritableDatabase();
        String sql = "insert into laundry_data (id, itemName, itemType, itemQuantity, submitted, uniqueKey) values ('"+laundryDataModel.getId()+"','"+laundryDataModel.getItemName()+"','"+laundryDataModel.getItemType()+"','"+laundryDataModel.getItemQuantity()+"','"+laundryDataModel.getSubmitted()+"','"+laundryDataModel.getUniqueKey()+"')";
        db.execSQL(sql);
        db.close();
        new MyLog(NMIMSApplication.getAppContext()).debug("insertLaundryData", sql);
    }

    public void insertAnnouncementData(AnnouncementsDataModel announcementsDataModel)
    {
        SQLiteDatabase db = this.getWritableDatabase();
        String sql = "insert into announcement_data (id, announcementType, subject, description, startDate, endDate, filePath, lastUpdatedOn) values ('"+announcementsDataModel.getId()+"','"+announcementsDataModel.getAnnouncementType()+"','"+announcementsDataModel.getSubject()+"','"+announcementsDataModel.getDescription()+"','"+announcementsDataModel.getStartDate()+"','"+announcementsDataModel.getEndDate()+"','"+announcementsDataModel.getFilePath()+"','"+announcementsDataModel.getLastUpdatedOn()+"')";
        db.execSQL(sql);
        db.close();
        new MyLog(NMIMSApplication.getAppContext()).debug("insertAnnouncementData", sql);
    }

    public void insertTimeTableData(TimeTableDataModel timeTableDataModel)
    {
        SQLiteDatabase db = this.getWritableDatabase();
        String sql = "insert into timetable_data (id, subject, description, startDate, filePath, acadSession, acadYear, lastUpdatedOn) values ('"+timeTableDataModel.getId()+"','"+timeTableDataModel.getSubject()+"','"+timeTableDataModel.getDescription()+"','"+timeTableDataModel.getStartDate()+"','"+timeTableDataModel.getFilePath()+"','"+timeTableDataModel.getAcadSession()+"','"+timeTableDataModel.getAcadYear()+"','"+timeTableDataModel.getLastUpdatedOn()+"')";
        db.execSQL(sql);
        db.close();
        new MyLog(NMIMSApplication.getAppContext()).debug("insertTimeTableData", sql);
    }

    public void insertAssignmentData(StudentAssignmentDataModel studentAssignmentDataModel)
    {
        SQLiteDatabase db = this.getWritableDatabase();
        String sql = "insert into assignment_data (assignmentId , assignmentCourse , assignmentName , assignmentEndDate , assignmentStatus , marksOutOf , assignmentType , assignmentFile , studentFilePath , assignmentText , allowAfterEndDate , submitByOneInGroup , isSubmitterInGroup , studentAssignmentId , evaluationStatus , lastUpdatedOn) " +
                "values ('"+studentAssignmentDataModel.getAssignmentId()+"','"+studentAssignmentDataModel.getAssignmentCourse()+"','"+studentAssignmentDataModel.getAssignmentName()+"','"+studentAssignmentDataModel.getAssignmentEndDate()+"','"+studentAssignmentDataModel.getAssignmentStatus()+"','"+studentAssignmentDataModel.getMarksOutOf()+"','"+studentAssignmentDataModel.getAssignmentType()+"','"+studentAssignmentDataModel.getAssignmentFile()+"','"+studentAssignmentDataModel.getStudentFilePath()+"','"+studentAssignmentDataModel.getAssignmentText()+"','"+studentAssignmentDataModel.getAllowAfterEndDate()+"','"+studentAssignmentDataModel.getSubmitByOneInGroup()+"','"+studentAssignmentDataModel.getIsSubmitterInGroup()+"','"+studentAssignmentDataModel.getStudentAssignmentId()+"','"+studentAssignmentDataModel.getEvaluationStatus()+"','"+studentAssignmentDataModel.getLastUpdatedOn()+"')";
        db.execSQL(sql);
        db.close();
        new MyLog(NMIMSApplication.getAppContext()).debug("insertAssignmentData", sql);
    }

    public void insertStudentData(AttendanceStudentDataModel attendanceStudentDataModel)
    {
        new MyLog(NMIMSApplication.getAppContext()).debug("attendanceStudentDataModel_insert", attendanceStudentDataModel.toString());
        SQLiteDatabase db = this.getWritableDatabase();
        String sql = "insert into student_data (studentName, studentSapId, studentRollNo, studentAbsentPresent, courseId, isMarked, startTime, endTime, isAttendanceSubmitted, dateEntry , lectureId , attendanceStatus , attendanceFlag, schoolName, blockMarking, createdDate,  lastModifiedDate) " +
                "values ('"+attendanceStudentDataModel.getStudentName()+"','"+attendanceStudentDataModel.getStudentSapId()+"','"+attendanceStudentDataModel.getStudentRollNo()+"','"+attendanceStudentDataModel.getStudentAbsentPresent()+"','"+attendanceStudentDataModel.getCourseId()+"','"+attendanceStudentDataModel.getIsMarked()+"','"+attendanceStudentDataModel.getStartTime()+"','"+attendanceStudentDataModel.getEndTime()+"','"+attendanceStudentDataModel.getIsAttendanceSubmitted()+"','"+attendanceStudentDataModel.getDateEntry()+"','"+attendanceStudentDataModel.getLectureId()+"','"+attendanceStudentDataModel.getAttendanceStatus()+"','"+attendanceStudentDataModel.getAttendanceFlag()+"','"+attendanceStudentDataModel.getSchoolName()+"','"+attendanceStudentDataModel.getBlockMarking()+"','"+attendanceStudentDataModel.getCreatedDate()+"','"+attendanceStudentDataModel.getLastModifiedDate()+"')";
        db.execSQL(sql);
        db.close();
        new MyLog(NMIMSApplication.getAppContext()).debug("insertStudentData", sql);
    }

    public void insertLectureData(LecturesDataModel lecturesDataModel)
    {
        SQLiteDatabase db = this.getWritableDatabase();
        String sql = "insert into lecture_data (lectureId, facultyId , flag , class_date , start_time , end_time , courseId , courseName , programId , programName , maxEndTime , dateFlag, schoolName, event_id, allotted_lectures, conducted_lectures, remaining_lectures, presentFacultyId)" +
                " values ('"+lecturesDataModel.getLectureId()+"','"+lecturesDataModel.getFacultyId()+"','"+lecturesDataModel.getFlag()+"','"+lecturesDataModel.getClass_date()+"','"+lecturesDataModel.getStart_time()+"','"+lecturesDataModel.getEnd_time()+"','"+lecturesDataModel.getCourseId()+"','"+lecturesDataModel.getCourseName()+"','"+lecturesDataModel.getProgramId()+"','"+lecturesDataModel.getProgramName()+"','"+lecturesDataModel.getMaxEndTime()+"','"+lecturesDataModel.getDateFlag()+"','"+lecturesDataModel.getSchoolName()+"','"+lecturesDataModel.getEvent_id()+"','"+lecturesDataModel.getAllotted_lectures()+"','"+lecturesDataModel.getConducted_lectures()+"','"+lecturesDataModel.getRemaining_lectures()+"','"+lecturesDataModel.getPresentFacultyId()+"')";
        db.execSQL(sql);
        db.close();
        new MyLog(NMIMSApplication.getAppContext()).debug("insertLectureData", sql);
    }

  /* public void insertTestDetails(TestDetailsDataModel testDetailsDataModel)
    {
        SQLiteDatabase db = this.getWritableDatabase();
        String sql = "insert into test_details (id , courseId , testDate , startDate , endDate , testName , testType , duration , createdDate , showResultsToStudent , active , facultyId , maxmAttempt , maxmScore , studentCount , schoolName , stud_count, faculty_name)" +
                " values ('"+testDetailsDataModel.getId()+"','"+testDetailsDataModel.getCourseId()+"','"+testDetailsDataModel.getTestDate()+"','"+testDetailsDataModel.getStartDate()+"','"+testDetailsDataModel.getEndDate()+"','"+testDetailsDataModel.getTestName()+"','"+testDetailsDataModel.getTestType()+"','"+testDetailsDataModel.getDuration()+"','"+testDetailsDataModel.getCreatedDate()+"','"+testDetailsDataModel.getShowResultsToStudent()+"','"+testDetailsDataModel.getActive()+"','"+testDetailsDataModel.getFacultyId()+"','"+testDetailsDataModel.getMaxmAttempt()+"','"+testDetailsDataModel.getMaxmScore()+"','"+testDetailsDataModel.getStudentCount()+"','"+testDetailsDataModel.getSchoolName()+"','"+testDetailsDataModel.getStudentCount()+\"','\"+testDetailsDataModel.getSchoolName()+\"')";
        db.execSQL(sql);
        db.close();
        new MyLog(NMIMSApplication.getAppContext()).debug("insertTestDetails", sql);
    }*/

    public void insertAttendanceData(SchoolAttendanceDataModel schoolAttendanceDataModel)
    {
        SQLiteDatabase db = this.getWritableDatabase();
        String sql = "insert into attendance_data (schoolName, date, status, isExpanded, endTime, isApiHit) " +
                "values ('"+schoolAttendanceDataModel.getSchoolName()+"','"+schoolAttendanceDataModel.getDate()+"','"+schoolAttendanceDataModel.getStatus()+"','"+schoolAttendanceDataModel.getIsExpanded()+"','"+schoolAttendanceDataModel.getEndTime()+"','"+schoolAttendanceDataModel.getIsApiHit()+"')";
        db.execSQL(sql);
        db.close();
        new MyLog(NMIMSApplication.getAppContext()).debug("insertAttendanceData", sql);
    }

    public void insertMyDate(MyDate myDate)
    {
        SQLiteDatabase db = this.getWritableDatabase();
        String sql = "insert into myDate (dateId,currentDate, startDate, endDate) values ('"+myDate.getDateId()+"','"+myDate.getCurrentDate()+"','"+myDate.getStartDate()+"','"+myDate.getEndDate()+"')";
        db.execSQL(sql);
        db.close();
        new MyLog(NMIMSApplication.getAppContext()).debug("insertDate", sql);
    }

    public void insertMyPermission(MyPermission myPermission)
    {
        SQLiteDatabase db = this.getWritableDatabase();
        String sql = "insert into myPermission (permissionId,permissionName, permissionStatus) values ('"+myPermission.getPermissionId()+"','"+myPermission.getPermissionName()+"','"+myPermission.getPermissionStatus()+"')";
        db.execSQL(sql);
        db.close();
        new MyLog(NMIMSApplication.getAppContext()).debug("insertMyPermission", sql);
    }

    public void insertMyNotification(NotificationDataModel notificationDataModel)
    {
        SQLiteDatabase db = this.getWritableDatabase();
        String sql = "insert into myNotification (not_id, playerId) values ('"+notificationDataModel.getNot_id()+"','"+notificationDataModel.getPlayerId()+"')";
        db.execSQL(sql);
        db.close();
        new MyLog(NMIMSApplication.getAppContext()).debug("insertMyNotification", sql);
    }

    public long getAssignmentCount() {
        SQLiteDatabase db = this.getReadableDatabase();
        long count = DatabaseUtils.queryNumEntries(db, "assignment_data");
        db.close();
        return count;
    }

    public long getLaundryCount() {
        SQLiteDatabase db = this.getReadableDatabase();
        long count = DatabaseUtils.queryNumEntries(db, "laundry_data");
        db.close();
        return count;
    }

    public long getAuthControlCount() {
        SQLiteDatabase db = this.getReadableDatabase();
        long count = DatabaseUtils.queryNumEntries(db, "auth_control");
        db.close();
        return count;
    }

    public long getAnnouncementCount() {
        SQLiteDatabase db = this.getReadableDatabase();
        long count = DatabaseUtils.queryNumEntries(db, "announcement_data");
        db.close();
        return count;
    }

    public long getFacultyNotificationCount() {
        SQLiteDatabase db = this.getReadableDatabase();
        long count = DatabaseUtils.queryNumEntries(db, "faculty_notification");
        db.close();
        return count;
    }


    public long getTimeTableCount() {
        SQLiteDatabase db = this.getReadableDatabase();
        long count = DatabaseUtils.queryNumEntries(db, "timetable_data");
        db.close();
        return count;
    }

    /*public long getSurveyNameCount()
    {
        SQLiteDatabase db = this.getReadableDatabase();
        long count = DatabaseUtils.queryNumEntries(db, "survey_name");
        db.close();
        return count;
    }*///select count(*) from survey_name where startDate <= date() and endDate >= date() and submitted = 'N' and locallySubmitted = 'Y'

    public long getSurveyNameCount()
    {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor= db.rawQuery("select count(*) from survey_name where startDate <= date() and endDate >= date() "
                , new String[] { });
        cursor.moveToFirst();
        int count= cursor.getInt(0);
        cursor.close();
        return count;
    }

    public long getMultipleFacultySchoolCount()
    {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor= db.rawQuery("select count(*) from backend_control where (key = 'multipleFacultySchool' AND key IS NOT NULL)   "
                , new String[] { });
        int count = 0;
        if(null != cursor)
            if(cursor.getCount() > 0)
            {
                cursor.moveToFirst();
                count = cursor.getInt(0);
            }
        cursor.close();
        return count;
    }

   /* public long getServerAddressCount()
    {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor= db.rawQuery("select count(*) from backend_control where (key = 'myApiUrlUsermgmt' AND key IS NOT NULL) AND (key = 'myApiUrlLms' AND key IS NOT NULL) AND (key = 'myApiUrlUsermgmtCrud' AND key IS NOT NULL) AND (key = 'myApiUrlSurvey' AND key IS NOT NULL)  "
                , new String[] { });
        cursor.moveToFirst();
        int count= cursor.getInt(0);
        cursor.close();
        return count;
    }*/

//    public long getServerAddressCount()
//    {
//        SQLiteDatabase db = this.getReadableDatabase();
//        Cursor cursor= db.rawQuery("select count(*) from backend_control where (key = 'myApiUrlUsermgmt' AND key IS NOT NULL) OR (key = 'myApiUrlLms' AND key IS NOT NULL) OR (key = 'myApiUrlUsermgmtCrud' AND key IS NOT NULL) OR (key = 'myApiUrlSurvey' AND key IS NOT NULL)  "
//                , new String[] { });
//        int count = 0;
//        if(null != cursor)
//            if(cursor.getCount() > 0)
//            {
//                cursor.moveToFirst();
//                count = cursor.getInt(0);
//            }
//        cursor.close();
//        return count;
//    }

    public long getServerAddressCount()
    {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor= db.rawQuery("select count(*) from backend_control where (key = 'myApiUrlUsermgmt' AND key IS NOT NULL) OR (key = 'myApiUrlLms' AND key IS NOT NULL) OR (key = 'myApiUrlUsermgmtCrud' AND key IS NOT NULL) OR (key = 'myApiUrlSurvey' AND key IS NOT NULL)  or ((key = 'saltKey' AND key IS NOT NULL) or (key = 'secretKey' AND key IS NOT NULL) )"
                , new String[] { });
        int count = 0;
        if(null != cursor)
            if(cursor.getCount() > 0)
            {
                cursor.moveToFirst();
                count = cursor.getInt(0);
            }
        cursor.close();
        return count;
    }

    public long getUnreadNotificationCount()
    {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor= db.rawQuery("select count(*) from faculty_notification where isRead = 'N' "
                , new String[] { });
        cursor.moveToFirst();
        int count= cursor.getInt(0);
        cursor.close();
        return count;
    }


    public long getPendingSurveyNameCountWhoseAnswerIsGiven()
    {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor= db.rawQuery("select count(*) from survey_name where startDate <= date() and endDate >= date() and submitted = 'N' and locallySubmitted = 'Y' "
                , new String[] { });
        cursor.moveToFirst();
        int count= cursor.getInt(0);
        cursor.close();
        return count;
    }
    public long getUserCount()
    {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor= db.rawQuery("SELECT COUNT (*) FROM " + "userData"
                , new String[] {  });
        cursor.moveToFirst();
        int count= cursor.getInt(0);
        cursor.close();
        return count;
    }

    public long getLectureCount(String schoolName)
    {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor= db.rawQuery("SELECT COUNT (*) FROM " + "lecture_data" + " where " + "schoolName" + " = ? "
                , new String[] { schoolName });
        cursor.moveToFirst();
        int count= cursor.getInt(0);
        cursor.close();
        new MyLog(NMIMSApplication.getAppContext()).debug("getStudentCount", "schoolName :" +schoolName);
        return count;
    }

    public long getSurveyNameResponseCount(String id, String responseFlag)
    {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor= db.rawQuery("SELECT COUNT (*) FROM " + "survey_name" + " where " + "id" + " = ? and responseFlag = ?"
                , new String[] { id, responseFlag });
        cursor.moveToFirst();
        int count= cursor.getInt(0);
        cursor.close();
        new MyLog(NMIMSApplication.getAppContext()).debug("getSurveyNameResponseCount", "id :" +id);
        return count;
    }

    public long getLectureCountByDateOnly(String dateFlag)
    {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor= db.rawQuery("SELECT COUNT (*) FROM " + "lecture_data" + " where " + "dateFlag" + " = ? "
                , new String[] { dateFlag });
        cursor.moveToFirst();
        int count= cursor.getInt(0);
        cursor.close();
        new MyLog(NMIMSApplication.getAppContext()).debug("getStudentCountD", "dateFlag :" +dateFlag);
        return count;
    }

    public long getLectureCountByDateAndSchool(String dateFlag, String schoolName)
    {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor= db.rawQuery("SELECT COUNT (*) FROM " + "lecture_data" + " where " + "dateFlag" + " = ? and " + "schoolName" + " = ? "
                , new String[] { dateFlag, schoolName });
        cursor.moveToFirst();
        int count= cursor.getInt(0);
        cursor.close();
        new MyLog(NMIMSApplication.getAppContext()).debug("getStudentCountDS", "dateFlag :" +dateFlag+" "+ "schoolName : "+schoolName);
        return count;
    }

//    public int getStudentCount(String courseId, String startTime, String endTime)
//    {
//        SQLiteDatabase db = this.getWritableDatabase();
//        Cursor cursor= db.rawQuery("SELECT COUNT (*) FROM " + "student_data" + " where " + "courseId" + " = ? and " + "startTime" + " = ? and " + "endTime" + " = ? "
//                , new String[] { courseId,startTime,endTime });
//        cursor.moveToFirst();
//        int count= cursor.getInt(0);
//        cursor.close();
//        new MyLog(NMIMSApplication.getAppContext()).debug("getStudentCount", "courseId :" +courseId +" , " +"startTime :" +startTime+" , " + "endTime :" +endTime);
//        return count;
//    }

    public int getStudentCountByLectureId(String lectureId, String startTime, String endTime,String dateEntry,int x)
    {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor= db.rawQuery("SELECT COUNT (*) FROM " + "student_data" + " where " + "lectureId" + " = ? and " + "startTime" + " = ? and " + "endTime" + " = ? and " + "dateEntry" + " = ? "
                , new String[] { lectureId,startTime,endTime,dateEntry });
        cursor.moveToFirst();
        int count= cursor.getInt(0);
        cursor.close();
        new MyLog(NMIMSApplication.getAppContext()).debug("getStudentCount", "lectureId :" +lectureId +" , " +"startTime :" +startTime+" , " + "endTime :" +endTime);
        return count;
    }

    public int getStudentCountByLectureIdForBlockedAttendance(String lectureId, String startTime, String endTime, String dateEntry)
    {
        SQLiteDatabase db = this.getWritableDatabase();

        Cursor cursor= db.rawQuery("SELECT COUNT (*) FROM " + "student_data" + " where " + "lectureId" + " = ? and " + "startTime" + " = ? and " + "endTime" + " = ? and " + "dateEntry" + " = ? and  blockMarking = 'Y'"
                , new String[] { lectureId,startTime,endTime,dateEntry });
        cursor.moveToFirst();
        int count= cursor.getInt(0);
        cursor.close();
        return count;
    }


//    public int getStudentCountByLectureId(String courseId,String lectureId, String startTime, String endTime,String dateEntry,int x)
//    {
//        SQLiteDatabase db = this.getWritableDatabase();
//        Cursor cursor= db.rawQuery("SELECT COUNT (*) FROM " + "student_data" + " where " + "courseId" + " = ? and  " +  "lectureId" + " = ? and  " + "startTime" + " = ? and " + "endTime" + " = ? and " + "dateEntry" + " = ? "
//                , new String[] { courseId,lectureId,startTime,endTime,dateEntry });
//        cursor.moveToFirst();
//        int count= cursor.getInt(0);
//        cursor.close();
//        new MyLog(NMIMSApplication.getAppContext()).debug("getStudentCount", "lectureId :" +lectureId +" , " +"startTime :" +startTime+" , " + "endTime :" +endTime);
//        return count;
//    }

//    public int getStudentCountByLectureId(String startTime, String endTime,String dateEntry,int x)
//    {
//        SQLiteDatabase db = this.getWritableDatabase();
//        Cursor cursor= db.rawQuery("SELECT COUNT (*) FROM " + "student_data" + " where "  + "startTime" + " = ? and " + "endTime" + " = ? and " + "dateEntry" + " = ? "
//                , new String[] { startTime,endTime,dateEntry });
//        cursor.moveToFirst();
//        int count= cursor.getInt(0);
//        cursor.close();
//        new MyLog(NMIMSApplication.getAppContext()).debug("getStudentCount",  "startTime :" +startTime+" , " + "endTime :" +endTime);
//        return count;
//    }

   /* public ArrayList<LaundryDataModel> getLaundryList()
    {
        try
        {
            LaundryDataModel laundryDataModel = new LaundryDataModel();
            ArrayList<LaundryDataModel> laundryDataModelArrayList = new ArrayList<>();
            SQLiteDatabase db = this.getWritableDatabase();
            Cursor cursor = db.query("laundry_data",
                    new String[]{"id" , "itemName" , "itemType" , "itemQuantity" , "submitted" , "uniqueKey" },"",
                    new String[]{},null, null, "id ASC", null);

            if(cursor != null)
            {
                if(cursor.moveToFirst())
                {
                    do
                    {
                        laundryDataModel = new LaundryDataModel(
                                cursor.getInt(cursor.getColumnIndex("id")),
                                cursor.getString(cursor.getColumnIndex("itemName")),
                                cursor.getString(cursor.getColumnIndex("itemType")),
                                cursor.getString(cursor.getColumnIndex("itemQuantity")),
                                        cursor.getString(cursor.getColumnIndex("submitted")),
                                                cursor.getString(cursor.getColumnIndex("uniqueKey")));

                        laundryDataModelArrayList.add(laundryDataModel);
                    }
                    while(cursor.moveToNext());
                }
            }

            cursor.close();
            return laundryDataModelArrayList;
        }
        catch (Exception e)
        {
            new MyLog(NMIMSApplication.getAppContext()).debug("getLaundryListEX",e.getMessage());
            e.printStackTrace();
            return null;
        }
    }*/

    public ArrayList<AnnouncementsDataModel> getAnnouncementData()
    {
        try
        {
            AnnouncementsDataModel announcementsDataModel = new AnnouncementsDataModel();
            ArrayList<AnnouncementsDataModel> announcementsDataModels = new ArrayList<>();
            SQLiteDatabase db = this.getWritableDatabase();
            Cursor cursor = db.query("announcement_data",
                    new String[]{"id" , "announcementType" , "subject" , "description" , "startDate" , "endDate" , "filePath" , "lastUpdatedOn"},"",
                    new String[]{},null, null, null, null);

            if(cursor != null)
            {
                if(cursor.moveToFirst())
                {
                    do
                    {
                        announcementsDataModel = new AnnouncementsDataModel(
                                cursor.getString(cursor.getColumnIndex("id")),
                                cursor.getString(cursor.getColumnIndex("announcementType")),
                                cursor.getString(cursor.getColumnIndex("subject")),
                                cursor.getString(cursor.getColumnIndex("description")),
                                cursor.getString(cursor.getColumnIndex("startDate")),
                                cursor.getString(cursor.getColumnIndex("endDate")),
                                cursor.getString(cursor.getColumnIndex("filePath")),
                                cursor.getString(cursor.getColumnIndex("lastUpdatedOn")));

                        announcementsDataModels.add(announcementsDataModel);
                    }
                    while(cursor.moveToNext());
                }
            }

            cursor.close();
            return announcementsDataModels;
        }
        catch (Exception e)
        {
            new MyLog(NMIMSApplication.getAppContext()).debug("getTimeTableDataEX",e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    public ArrayList<TimeTableDataModel> getTimeTableData()
    {
        try
        {
            TimeTableDataModel timeTableDataModel = new TimeTableDataModel();
            ArrayList<TimeTableDataModel> timeTableDataModelArrayList = new ArrayList<>();
            SQLiteDatabase db = this.getWritableDatabase();
            Cursor cursor = db.query("timetable_data",
                    new String[]{"id" , "subject" , "description" , "startDate" , "filePath" , "acadSession" , "acadYear" , "lastUpdatedOn"},"",
                    new String[]{},null, null, null, null);

            if(cursor != null)
            {
                if(cursor.moveToFirst())
                {
                    do
                    {
                        timeTableDataModel = new TimeTableDataModel(
                                cursor.getString(cursor.getColumnIndex("id")),
                                cursor.getString(cursor.getColumnIndex("subject")),
                                cursor.getString(cursor.getColumnIndex("description")),
                                cursor.getString(cursor.getColumnIndex("startDate")),
                                cursor.getString(cursor.getColumnIndex("filePath")),
                                cursor.getString(cursor.getColumnIndex("acadSession")),
                                cursor.getString(cursor.getColumnIndex("acadYear")),
                                cursor.getString(cursor.getColumnIndex("lastUpdatedOn")));

                        timeTableDataModelArrayList.add(timeTableDataModel);
                    }
                    while(cursor.moveToNext());
                }
            }

            cursor.close();
            return timeTableDataModelArrayList;
        }
        catch (Exception e)
        {
            new MyLog(NMIMSApplication.getAppContext()).debug("getTimeTableDataEX",e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    public ArrayList<SurveyName> getSurveyName()
    {
        try
        {
            SurveyName surveyName = new SurveyName();
            ArrayList<SurveyName> surveyNameArrayList = new ArrayList<>();
            SQLiteDatabase db = this.getWritableDatabase();
            String sql = "select * from survey_name where startDate <= date() and endDate >= date()";
            SQLiteDatabase dbSurvey = this.getWritableDatabase();
            Cursor cursor = dbSurvey.rawQuery(sql, null);

            if(cursor != null)
            {
                if(cursor.moveToFirst())
                {
                    do
                    {
                        surveyName = new SurveyName(
                                cursor.getString(cursor.getColumnIndex("id")),
                                cursor.getString(cursor.getColumnIndex("surveyName")),
                                cursor.getString(cursor.getColumnIndex("userId")),
                                cursor.getString(cursor.getColumnIndex("startDate")),
                                cursor.getString(cursor.getColumnIndex("endDate")),
                                cursor.getString(cursor.getColumnIndex("submitted")),
                                cursor.getString(cursor.getColumnIndex("responseFlag")),
                                cursor.getString(cursor.getColumnIndex("createdDate")),
                                cursor.getString(cursor.getColumnIndex("locallySubmitted")));

                        surveyNameArrayList.add(surveyName);
                    }
                    while(cursor.moveToNext());
                }
            }

            cursor.close();
            return surveyNameArrayList;
        }
        catch (Exception e)
        {
            new MyLog(NMIMSApplication.getAppContext()).debug("getSurveyName",e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    public ArrayList<SurveyName> getPendingSurveyName()
    {
        try
        {
            SurveyName surveyName = new SurveyName();
            ArrayList<SurveyName> surveyNameArrayList = new ArrayList<>();
            SQLiteDatabase db = this.getWritableDatabase();
            String sql = "select * from survey_name where startDate <= date() and endDate >= date() and submitted = 'N' and locallySubmitted = 'Y' ";
            SQLiteDatabase dbSurvey = this.getWritableDatabase();
            Cursor cursor = dbSurvey.rawQuery(sql, null);

            if(cursor != null)
            {
                if(cursor.moveToFirst())
                {
                    do
                    {
                        surveyName = new SurveyName(
                                cursor.getString(cursor.getColumnIndex("id")),
                                cursor.getString(cursor.getColumnIndex("surveyName")),
                                cursor.getString(cursor.getColumnIndex("userId")),
                                cursor.getString(cursor.getColumnIndex("startDate")),
                                cursor.getString(cursor.getColumnIndex("endDate")),
                                cursor.getString(cursor.getColumnIndex("submitted")),
                                cursor.getString(cursor.getColumnIndex("responseFlag")),
                                cursor.getString(cursor.getColumnIndex("createdDate")),
                                cursor.getString(cursor.getColumnIndex("locallySubmitted")));

                        surveyNameArrayList.add(surveyName);
                    }
                    while(cursor.moveToNext());
                }
            }

            cursor.close();
            return surveyNameArrayList;
        }
        catch (Exception e)
        {
            new MyLog(NMIMSApplication.getAppContext()).debug("getSurveyName",e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    public BackendModel getBackEndControl(String key)
    {
        try
        {
            BackendModel backendModel = new BackendModel();
            SQLiteDatabase db = this.getWritableDatabase();
            Cursor cursor = db.query("backend_control",
                    new String[]{"key" , "value"},"key" + " = ?",
                    new String[]{key},null, null, null, null);

            if(cursor != null)
            {
                if(cursor.moveToFirst())
                {
                    backendModel = new BackendModel(
                            cursor.getString(cursor.getColumnIndex("key")),
                            cursor.getString(cursor.getColumnIndex("value")));
                }
            }

            cursor.close();
            return backendModel;
        }
        catch (Exception e)
        {
            new MyLog(NMIMSApplication.getAppContext()).debug("getBackEndControlEx",e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    public SurveyName getSurveyNameById(String id)
    {
        try
        {
            SurveyName surveyName = new SurveyName();
            SQLiteDatabase db = this.getWritableDatabase();
            Cursor cursor = db.query("survey_name",
                    new String[]{"id" , "surveyName" , "userId" , "startDate" , "endDate" , "submitted" , "responseFlag" , "createdDate" , "locallySubmitted"},"id" + " = ?",
                    new String[]{id},null, null, "id ASC", null);

            if(cursor != null)
            {
                if(cursor.moveToFirst())
                {
                    surveyName = new SurveyName(
                            cursor.getString(cursor.getColumnIndex("id")),
                            cursor.getString(cursor.getColumnIndex("surveyName")),
                            cursor.getString(cursor.getColumnIndex("userId")),
                            cursor.getString(cursor.getColumnIndex("startDate")),
                            cursor.getString(cursor.getColumnIndex("endDate")),
                            cursor.getString(cursor.getColumnIndex("submitted")),
                            cursor.getString(cursor.getColumnIndex("responseFlag")),
                            cursor.getString(cursor.getColumnIndex("createdDate")),
                            cursor.getString(cursor.getColumnIndex("locallySubmitted")));
                }
            }

            cursor.close();
            return surveyName;
        }
        catch (Exception e)
        {
            new MyLog(NMIMSApplication.getAppContext()).debug("getSurveyName",e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    public ArrayList<SurveyQuestion> getSurveyQuestion(String survey_id)
    {
        try
        {
            SurveyQuestion surveyQuestion = new SurveyQuestion();
            ArrayList<SurveyQuestion> surveyQuestionArrayList = new ArrayList<>();
            SQLiteDatabase db = this.getWritableDatabase();
            Cursor cursor = db.query("survey_question",
                    new String[]{"id" , "title" , "type_id" , "isChecked" , "survey_id" },"survey_id" + " = ?",
                    new String[]{survey_id},null, null, "id ASC", null);

            if(cursor != null)
            {
                if(cursor.moveToFirst())
                {
                    do
                    {
                        surveyQuestion = new SurveyQuestion(
                                cursor.getString(cursor.getColumnIndex("id")),
                                cursor.getString(cursor.getColumnIndex("title")),
                                cursor.getString(cursor.getColumnIndex("type_id")),
                                cursor.getString(cursor.getColumnIndex("isChecked")),
                                cursor.getString(cursor.getColumnIndex("survey_id")));

                        surveyQuestionArrayList.add(surveyQuestion);
                    }
                    while(cursor.moveToNext());
                }
            }

            cursor.close();
            return surveyQuestionArrayList;
        }
        catch (Exception e)
        {
            new MyLog(NMIMSApplication.getAppContext()).debug("getSurveyQuestion",e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    public String getClassDate() {
        String sql = "select class_date from lecture_data";
        SQLiteDatabase db = this.getWritableDatabase();
        String classDate = "";
        Cursor cursor = db.rawQuery(sql, null);

        if (cursor != null){
            if(cursor.moveToFirst())
            {
                classDate = cursor.getString(0);
            }
        }
        return classDate;
    }

    public String getLastModifiedDate(String classDate){
        String sql = "select lastModifiedDate from student_data where startTime like '"+classDate+"%"+"' and attendanceFlag = 'N'";
        SQLiteDatabase db = this.getWritableDatabase();
        String lastModifiedDate = "";
        Cursor cursor = db.rawQuery(sql, null);
        if (cursor != null){
            if(cursor.moveToFirst()){
                lastModifiedDate = cursor.getString(0);
            }
        }
        return lastModifiedDate;
    }

    public SurveyUserResponse getSurveyUserResponse(String survey_id, String question_id)
    {
        try
        {
            SurveyUserResponse surveyUserResponse = new SurveyUserResponse();
            ArrayList<SurveyUserResponse> surveyUserResponseArrayList = new ArrayList<>();
            SQLiteDatabase db = this.getWritableDatabase();
            Cursor cursor = db.query("survey_user_response",
                    new String[]{"id" , "survey_id" , "question_id" , "response"},"survey_id" + " = ? and question_id = ?",
                    new String[]{survey_id,question_id},null, null, "id ASC", null);

            if(cursor != null)
            {
                if(cursor.moveToFirst())
                {
                    surveyUserResponse = new SurveyUserResponse(
                            cursor.getString(cursor.getColumnIndex("id")),
                            cursor.getString(cursor.getColumnIndex("survey_id")),
                            cursor.getString(cursor.getColumnIndex("question_id")),
                            cursor.getString(cursor.getColumnIndex("response")));
                }
            }

            cursor.close();
            return surveyUserResponse;
        }
        catch (Exception e)
        {
            new MyLog(NMIMSApplication.getAppContext()).debug("getSurveyUserResponseEX",e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    public ArrayList<SurveyOption> getSurveyQuestionOptions(String question_id)
    {
        try
        {
            SurveyOption surveyOption = new SurveyOption();
            ArrayList<SurveyOption> surveyOptionArrayList = new ArrayList<>();
            SQLiteDatabase db = this.getWritableDatabase();
            Cursor cursor = db.query("survey_option",
                    new String[]{"id" , "options" , "question_id" , "type_id"},"question_id" + " = ?",
                    new String[]{question_id},null, null, "id ASC", null);

            if(cursor != null)
            {
                if(cursor.moveToFirst())
                {
                    do
                    {
                        surveyOption = new SurveyOption(
                                cursor.getString(cursor.getColumnIndex("id")),
                                cursor.getString(cursor.getColumnIndex("options")),
                                cursor.getString(cursor.getColumnIndex("question_id")),
                                cursor.getString(cursor.getColumnIndex("type_id")));

                        surveyOptionArrayList.add(surveyOption);
                    }
                    while(cursor.moveToNext());
                }
            }

            cursor.close();
            return surveyOptionArrayList;
        }
        catch (Exception e)
        {
            new MyLog(NMIMSApplication.getAppContext()).debug("getSurveyQuestion",e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    public ArrayList<FacultyNotificationModel> getAllNotification()
    {
        try
        {
            FacultyNotificationModel facultyNotificationModel = new FacultyNotificationModel();
            ArrayList<FacultyNotificationModel> facultyNotificationModelArrayList = new ArrayList<>();
            SQLiteDatabase db = this.getWritableDatabase();
            Cursor cursor = db.query("faculty_notification",
                    new String[]{"id" , "message" , "createdDate" , "isRead"},"",
                    new String[]{},null, null, "createdDate DESC", null);

            if(cursor != null)
            {
                if(cursor.moveToFirst())
                {
                    do
                    {
                        facultyNotificationModel = new FacultyNotificationModel(
                                cursor.getString(cursor.getColumnIndex("id")),
                                cursor.getString(cursor.getColumnIndex("message")),
                                cursor.getString(cursor.getColumnIndex("createdDate")),
                                cursor.getString(cursor.getColumnIndex("isRead")));

                        facultyNotificationModelArrayList.add(facultyNotificationModel);
                    }
                    while(cursor.moveToNext());
                }
            }

            cursor.close();
            return facultyNotificationModelArrayList;
        }
        catch (Exception e)
        {
            new MyLog(NMIMSApplication.getAppContext()).debug("getAllNotificationEx",e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    public ArrayList<StudentAssignmentDataModel> getAssignmentData()
    {
        try
        {
            StudentAssignmentDataModel studentAssignmentDataModel = new StudentAssignmentDataModel();
            ArrayList<StudentAssignmentDataModel> studentAssignmentDataModelArrayList = new ArrayList<>();
            SQLiteDatabase db = this.getWritableDatabase();
            Cursor cursor = db.query("assignment_data",
                    new String[]{"assignmentId" , "assignmentCourse" , "assignmentName" , "assignmentEndDate" , "assignmentStatus" , "marksOutOf" , "assignmentType" , "assignmentFile" , "studentFilePath" , "assignmentText" , "allowAfterEndDate" , "submitByOneInGroup" , "isSubmitterInGroup" , "studentAssignmentId" , "evaluationStatus" , "lastUpdatedOn"},"",
                    new String[]{},null, null, null, null);

            if(cursor != null)
            {
                if(cursor.moveToFirst())
                {
                    do
                    {
                        studentAssignmentDataModel = new StudentAssignmentDataModel(
                                cursor.getString(cursor.getColumnIndex("assignmentId")),
                                cursor.getString(cursor.getColumnIndex("assignmentCourse")),
                                cursor.getString(cursor.getColumnIndex("assignmentName")),
                                cursor.getString(cursor.getColumnIndex("assignmentEndDate")),
                                cursor.getString(cursor.getColumnIndex("assignmentStatus")),
                                cursor.getString(cursor.getColumnIndex("marksOutOf")),
                                cursor.getString(cursor.getColumnIndex("assignmentType")),
                                cursor.getString(cursor.getColumnIndex("assignmentFile")),
                                cursor.getString(cursor.getColumnIndex("studentFilePath")),
                                cursor.getString(cursor.getColumnIndex("assignmentText")),
                                cursor.getString(cursor.getColumnIndex("allowAfterEndDate")),
                                cursor.getString(cursor.getColumnIndex("submitByOneInGroup")),
                                cursor.getString(cursor.getColumnIndex("isSubmitterInGroup")),
                                cursor.getString(cursor.getColumnIndex("studentAssignmentId")),
                                cursor.getString(cursor.getColumnIndex("evaluationStatus")),
                                cursor.getString(cursor.getColumnIndex("lastUpdatedOn")));

                        studentAssignmentDataModelArrayList.add(studentAssignmentDataModel);
                    }
                    while(cursor.moveToNext());
                }
            }

            cursor.close();
            return studentAssignmentDataModelArrayList;
        }
        catch (Exception e)
        {
            new MyLog(NMIMSApplication.getAppContext()).debug("getAssignmentDataEX",e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    public ArrayList<AttendanceStudentDataModel> getStudentData(String courseId, String startTime, String endTime,String dateEntry, String lectureId)
    {
        try
        {
            AttendanceStudentDataModel attendanceStudentDataModel = new AttendanceStudentDataModel();
            ArrayList<AttendanceStudentDataModel> attendanceStudentDataModelArrayList = new ArrayList<>();
            SQLiteDatabase db = this.getWritableDatabase();
            Cursor cursor = db.query("student_data",
                    new String[]{"studentName" , "studentSapId" , "studentRollNo" , "studentAbsentPresent" , "courseId" , "isMarked" , "startTime" , "endTime", "isAttendanceSubmitted", "dateEntry", "lectureId", "attendanceStatus" , "attendanceFlag" , "schoolName" , "blockMarking" , "createdDate" , "lastModifiedDate"},"courseId" + " = ? and " + "startTime" + " = ? and " + "endTime" + " = ? and " + "dateEntry" + " = ? and " + "lectureId" + " = ?",
                    new String[]{courseId,startTime,endTime,dateEntry,lectureId },null, null, "studentRollNo ASC", null);

            if(cursor != null)
            {
                if(cursor.moveToFirst())
                {
                    do
                    {
                        attendanceStudentDataModel = new AttendanceStudentDataModel(
                                cursor.getString(cursor.getColumnIndex("studentName")),
                                cursor.getString(cursor.getColumnIndex("studentSapId")),
                                cursor.getString(cursor.getColumnIndex("studentRollNo")),
                                cursor.getString(cursor.getColumnIndex("studentAbsentPresent")),
                                cursor.getString(cursor.getColumnIndex("courseId")),
                                cursor.getString(cursor.getColumnIndex("isMarked")),
                                cursor.getString(cursor.getColumnIndex("startTime")),
                                cursor.getString(cursor.getColumnIndex("endTime")),
                                cursor.getString(cursor.getColumnIndex("isAttendanceSubmitted")),
                                cursor.getString(cursor.getColumnIndex("dateEntry")),
                                cursor.getString(cursor.getColumnIndex("lectureId")),
                                cursor.getString(cursor.getColumnIndex("attendanceStatus")),
                                cursor.getString(cursor.getColumnIndex("attendanceFlag")),
                                cursor.getString(cursor.getColumnIndex("schoolName")),
                                cursor.getString(cursor.getColumnIndex("blockMarking")),
                                cursor.getString(cursor.getColumnIndex("createdDate")),
                                cursor.getString(cursor.getColumnIndex("lastModifiedDate")));

                        attendanceStudentDataModelArrayList.add(attendanceStudentDataModel);
                    }
                    while(cursor.moveToNext());
                }
            }

            cursor.close();
            return attendanceStudentDataModelArrayList;
        }
        catch (Exception e)
        {
            new MyLog(NMIMSApplication.getAppContext()).debug("LecturesDataModel",e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    public ArrayList<AttendanceStudentDataModel> getStudentData(String isAttendanceSubmitted, String isMarked, String lectureId, int z)
    {
        try
        {
            AttendanceStudentDataModel attendanceStudentDataModel = new AttendanceStudentDataModel();
            ArrayList<AttendanceStudentDataModel> attendanceStudentDataModelArrayList = new ArrayList<>();
            SQLiteDatabase db = this.getWritableDatabase();
            Cursor cursor = db.query("student_data",
                    new String[]{"studentName" , "studentSapId" , "studentRollNo" , "studentAbsentPresent" , "courseId" , "isMarked" , "startTime" , "endTime", "isAttendanceSubmitted", "dateEntry", "lectureId", "attendanceStatus" , "attendanceFlag" , "schoolName" , "blockMarking" , "createdDate" , "lastModifiedDate"},"isAttendanceSubmitted" + " = ? and " + "isMarked" + " = ? and "  + "lectureId" + " = ? ",
                    new String[]{isAttendanceSubmitted, isMarked, lectureId},null, null, "studentRollNo ASC", null);

            if(cursor != null)
            {
                if(cursor.moveToFirst())
                {
                    do
                    {
                        attendanceStudentDataModel = new AttendanceStudentDataModel(
                                cursor.getString(cursor.getColumnIndex("studentName")),
                                cursor.getString(cursor.getColumnIndex("studentSapId")),
                                cursor.getString(cursor.getColumnIndex("studentRollNo")),
                                cursor.getString(cursor.getColumnIndex("studentAbsentPresent")),
                                cursor.getString(cursor.getColumnIndex("courseId")),
                                cursor.getString(cursor.getColumnIndex("isMarked")),
                                cursor.getString(cursor.getColumnIndex("startTime")),
                                cursor.getString(cursor.getColumnIndex("endTime")),
                                cursor.getString(cursor.getColumnIndex("isAttendanceSubmitted")),
                                cursor.getString(cursor.getColumnIndex("dateEntry")),
                                cursor.getString(cursor.getColumnIndex("lectureId")),
                                cursor.getString(cursor.getColumnIndex("attendanceStatus")),
                                cursor.getString(cursor.getColumnIndex("attendanceFlag")),
                                cursor.getString(cursor.getColumnIndex("schoolName")),
                                cursor.getString(cursor.getColumnIndex("blockMarking")),
                                cursor.getString(cursor.getColumnIndex("createdDate")),
                                cursor.getString(cursor.getColumnIndex("lastModifiedDate")));

                        attendanceStudentDataModelArrayList.add(attendanceStudentDataModel);
                    }
                    while(cursor.moveToNext());
                }
            }
            cursor.close();
            return attendanceStudentDataModelArrayList;
        }
        catch (Exception e)
        {
            new MyLog(NMIMSApplication.getAppContext()).debug("asynEx",e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    public ArrayList<AttendanceStudentDataModel> getStudentData()
    {
        try
        {
            AttendanceStudentDataModel attendanceStudentDataModel = new AttendanceStudentDataModel();
            ArrayList<AttendanceStudentDataModel> attendanceStudentDataModelArrayList = new ArrayList<>();
            SQLiteDatabase db = this.getWritableDatabase();
            Cursor cursor = db.query("student_data",
                    new String[]{"studentName" , "studentSapId" , "studentRollNo" , "studentAbsentPresent" , "courseId" , "isMarked" , "startTime" , "endTime", "isAttendanceSubmitted", "dateEntry", "lectureId", "attendanceStatus" , "attendanceFlag" , "schoolName" , "blockMarking" ,"createdDate" ,"lastModifiedDate"},null,
                    new String[]{},null, null, "studentRollNo ASC", null);

            if(cursor != null)
            {
                if(cursor.moveToFirst())
                {
                    do
                    {
                        attendanceStudentDataModel = new AttendanceStudentDataModel(
                                cursor.getString(cursor.getColumnIndex("studentName")),
                                cursor.getString(cursor.getColumnIndex("studentSapId")),
                                cursor.getString(cursor.getColumnIndex("studentRollNo")),
                                cursor.getString(cursor.getColumnIndex("studentAbsentPresent")),
                                cursor.getString(cursor.getColumnIndex("courseId")),
                                cursor.getString(cursor.getColumnIndex("isMarked")),
                                cursor.getString(cursor.getColumnIndex("startTime")),
                                cursor.getString(cursor.getColumnIndex("endTime")),
                                cursor.getString(cursor.getColumnIndex("isAttendanceSubmitted")),
                                cursor.getString(cursor.getColumnIndex("dateEntry")),
                                cursor.getString(cursor.getColumnIndex("lectureId")),
                                cursor.getString(cursor.getColumnIndex("attendanceStatus")),
                                cursor.getString(cursor.getColumnIndex("attendanceFlag")),
                                cursor.getString(cursor.getColumnIndex("schoolName")),
                                cursor.getString(cursor.getColumnIndex("blockMarking")),
                                cursor.getString(cursor.getColumnIndex("createdDate")),
                                cursor.getString(cursor.getColumnIndex("lastModifiedDate")));

                        attendanceStudentDataModelArrayList.add(attendanceStudentDataModel);
                    }
                    while(cursor.moveToNext());
                }
            }
            cursor.close();
            return attendanceStudentDataModelArrayList;
        }
        catch (Exception e)
        {
            new MyLog(NMIMSApplication.getAppContext()).debug("asynEx",e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    public ArrayList<LecturesDataModel> getLecturesDataModel(String dateFlag, String schoolName)
    {
        try
        {
            LecturesDataModel lecturesDataModel = new LecturesDataModel();
            ArrayList<LecturesDataModel> lecturesDataModelList = new ArrayList<>();
            SQLiteDatabase db = this.getWritableDatabase();
            Cursor cursor = db.query("lecture_data",
                    new String[]{"lectureId" , "facultyId" , "flag" , "class_date" , "start_time" , "end_time" , "courseId" , "courseName" , "programId" , "programName" , "maxEndTime" , "dateFlag" , "schoolName" , "event_id" , "allotted_lectures" , "conducted_lectures" , "remaining_lectures", "presentFacultyId"},"dateFlag" + " = ? and " + "schoolName" + " = ? ",
                    new String[]{dateFlag,schoolName},null, null, null, null);

            if(cursor != null)
            {
                if(cursor.moveToFirst())
                {
                    do
                    {
                        lecturesDataModel = new LecturesDataModel(
                                cursor.getString(cursor.getColumnIndex("lectureId")),
                                cursor.getString(cursor.getColumnIndex("facultyId")),
                                cursor.getString(cursor.getColumnIndex("flag")),
                                cursor.getString(cursor.getColumnIndex("class_date")),
                                cursor.getString(cursor.getColumnIndex("start_time")),
                                cursor.getString(cursor.getColumnIndex("end_time")),
                                cursor.getString(cursor.getColumnIndex("courseId")),
                                cursor.getString(cursor.getColumnIndex("courseName")),
                                cursor.getString(cursor.getColumnIndex("programId")),
                                cursor.getString(cursor.getColumnIndex("programName")),
                                cursor.getString(cursor.getColumnIndex("maxEndTime")),
                                cursor.getString(cursor.getColumnIndex("dateFlag")),
                                cursor.getString(cursor.getColumnIndex("schoolName")),
                                cursor.getString(cursor.getColumnIndex("event_id")),
                                cursor.getString(cursor.getColumnIndex("allotted_lectures")),
                                cursor.getString(cursor.getColumnIndex("conducted_lectures")),
                                cursor.getString(cursor.getColumnIndex("remaining_lectures")),
                                cursor.getString(cursor.getColumnIndex("presentFacultyId")));

                        lecturesDataModelList.add(lecturesDataModel);
                    }
                    while(cursor.moveToNext());
                }
            }

            cursor.close();
            return lecturesDataModelList;
        }
        catch (Exception e)
        {
            new MyLog(NMIMSApplication.getAppContext()).debug("LecturesDataModel",e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    public ArrayList<LecturesDataModel> getAllLecturesDataModel()
    {
        try
        {
            LecturesDataModel lecturesDataModel = new LecturesDataModel();
            ArrayList<LecturesDataModel> lecturesDataModelList = new ArrayList<>();
            SQLiteDatabase db = this.getWritableDatabase();
            Cursor cursor = db.query("lecture_data",
                    new String[]{"lectureId" , "facultyId" , "flag" , "class_date" , "start_time" , "end_time" , "courseId" , "courseName" , "programId" , "programName" , "maxEndTime" , "dateFlag" , "schoolName" , "event_id" , "allotted_lectures" , "conducted_lectures" , "remaining_lectures" , "presentFacultyId"},null,
                    new String[]{},null, null, "class_date ASC", null);

            if(cursor != null)
            {
                if(cursor.moveToFirst())
                {
                    do
                    {
                        lecturesDataModel = new LecturesDataModel(
                                cursor.getString(cursor.getColumnIndex("lectureId")),
                                cursor.getString(cursor.getColumnIndex("facultyId")),
                                cursor.getString(cursor.getColumnIndex("flag")),
                                cursor.getString(cursor.getColumnIndex("class_date")),
                                cursor.getString(cursor.getColumnIndex("start_time")),
                                cursor.getString(cursor.getColumnIndex("end_time")),
                                cursor.getString(cursor.getColumnIndex("courseId")),
                                cursor.getString(cursor.getColumnIndex("courseName")),
                                cursor.getString(cursor.getColumnIndex("programId")),
                                cursor.getString(cursor.getColumnIndex("programName")),
                                cursor.getString(cursor.getColumnIndex("maxEndTime")),
                                cursor.getString(cursor.getColumnIndex("dateFlag")),
                                cursor.getString(cursor.getColumnIndex("schoolName")),
                                cursor.getString(cursor.getColumnIndex("event_id")),
                                cursor.getString(cursor.getColumnIndex("allotted_lectures")),
                                cursor.getString(cursor.getColumnIndex("conducted_lectures")),
                                cursor.getString(cursor.getColumnIndex("remaining_lectures")),
                                cursor.getString(cursor.getColumnIndex("presentFacultyId")));

                        lecturesDataModelList.add(lecturesDataModel);
                    }
                    while(cursor.moveToNext());
                }
            }

            cursor.close();
            return lecturesDataModelList;
        }
        catch (Exception e)
        {
            new MyLog(NMIMSApplication.getAppContext()).debug("LecturesDataModel",e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    public ArrayList<LecturesDataModel> getLecturesDataModel(String schoolName)
    {
        try
        {
            LecturesDataModel lecturesDataModel = new LecturesDataModel();
            ArrayList<LecturesDataModel> lecturesDataModelList = new ArrayList<>();
            SQLiteDatabase db = this.getWritableDatabase();
            Cursor cursor = db.query("lecture_data",
                    new String[]{"lectureId" , "facultyId" , "flag" , "class_date" , "start_time" , "end_time" , "courseId" , "courseName" , "programId" , "programName" , "maxEndTime" , "dateFlag" , "schoolName" , "event_id" , "allotted_lectures" , "conducted_lectures" , "remaining_lectures" , "presentFacultyId"},"schoolName" + " = ? ",
                    new String[]{schoolName},null, null, null, null);

            if(cursor != null)
            {
                if(cursor.moveToFirst())
                {
                    do
                    {
                        lecturesDataModel = new LecturesDataModel(
                                cursor.getString(cursor.getColumnIndex("lectureId")),
                                cursor.getString(cursor.getColumnIndex("facultyId")),
                                cursor.getString(cursor.getColumnIndex("flag")),
                                cursor.getString(cursor.getColumnIndex("class_date")),
                                cursor.getString(cursor.getColumnIndex("start_time")),
                                cursor.getString(cursor.getColumnIndex("end_time")),
                                cursor.getString(cursor.getColumnIndex("courseId")),
                                cursor.getString(cursor.getColumnIndex("courseName")),
                                cursor.getString(cursor.getColumnIndex("programId")),
                                cursor.getString(cursor.getColumnIndex("programName")),
                                cursor.getString(cursor.getColumnIndex("maxEndTime")),
                                cursor.getString(cursor.getColumnIndex("dateFlag")),
                                cursor.getString(cursor.getColumnIndex("schoolName")),
                                cursor.getString(cursor.getColumnIndex("event_id")),
                                cursor.getString(cursor.getColumnIndex("allotted_lectures")),
                                cursor.getString(cursor.getColumnIndex("conducted_lectures")),
                                cursor.getString(cursor.getColumnIndex("remaining_lectures")),
                                cursor.getString(cursor.getColumnIndex("presentFacultyId")));

                        lecturesDataModelList.add(lecturesDataModel);
                    }
                    while(cursor.moveToNext());
                }
            }

            cursor.close();
            return lecturesDataModelList;
        }
        catch (Exception e)
        {
            new MyLog(NMIMSApplication.getAppContext()).debug("LecturesDataModel",e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    public LecturesDataModel getLectureDataAttendanceAsync(String lectureId)
    {
        try
        {
            LecturesDataModel lecturesDataModel = new LecturesDataModel();
            ArrayList<LecturesDataModel> lecturesDataModelList = new ArrayList<>();
            SQLiteDatabase db = this.getWritableDatabase();
            Cursor cursor = db.query("lecture_data",
                    new String[]{"lectureId" , "facultyId" , "flag" , "class_date" , "start_time" , "end_time" , "courseId" , "courseName" , "programId" , "programName" , "maxEndTime" , "dateFlag" , "schoolName" , "event_id" , "allotted_lectures" , "conducted_lectures" , "remaining_lectures" , "presentFacultyId"},"lectureId" + "=?",
                    new String[]{lectureId},null, null, null, null);

            if(cursor != null)
            {
                if(cursor.moveToFirst())
                {
                    lecturesDataModel = new LecturesDataModel(
                            cursor.getString(cursor.getColumnIndex("lectureId")),
                            cursor.getString(cursor.getColumnIndex("facultyId")),
                            cursor.getString(cursor.getColumnIndex("flag")),
                            cursor.getString(cursor.getColumnIndex("class_date")),
                            cursor.getString(cursor.getColumnIndex("start_time")),
                            cursor.getString(cursor.getColumnIndex("end_time")),
                            cursor.getString(cursor.getColumnIndex("courseId")),
                            cursor.getString(cursor.getColumnIndex("courseName")),
                            cursor.getString(cursor.getColumnIndex("programId")),
                            cursor.getString(cursor.getColumnIndex("programName")),
                            cursor.getString(cursor.getColumnIndex("maxEndTime")),
                            cursor.getString(cursor.getColumnIndex("dateFlag")),
                            cursor.getString(cursor.getColumnIndex("schoolName")),
                            cursor.getString(cursor.getColumnIndex("event_id")),
                            cursor.getString(cursor.getColumnIndex("allotted_lectures")),
                            cursor.getString(cursor.getColumnIndex("conducted_lectures")),
                            cursor.getString(cursor.getColumnIndex("remaining_lectures")),
                            cursor.getString(cursor.getColumnIndex("presentFacultyId")));

                    lecturesDataModelList.add(lecturesDataModel);
                }
                cursor.close();
            }

            return lecturesDataModel;
        }
        catch (Exception e)
        {
            new MyLog(NMIMSApplication.getAppContext()).debug("LecturesDataModel",e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    /*public TestDetailsDataModel getTestDetailsDataModel(String schoolName)
    {
        try
        {
            TestDetailsDataModel testDetailsDataModel = new TestDetailsDataModel();
            SQLiteDatabase db = this.getWritableDatabase();
            Cursor cursor = db.query("test_details",
                    new String[]{"id", "courseId", "testDate", "startDate", "endDate", "testName", "testType", "duration", "createdDate", "showResultsToStudent", "active", "facultyId", "maxmAttempt", "maxmScore", "studentCount", "schoolName"},"schoolName" + "=?",
                    new String[]{schoolName},null, null, null, null);

            if(cursor != null)
            {
                cursor.moveToFirst();
                if(cursor.getCount() > 0)
                {
                    testDetailsDataModel = new TestDetailsDataModel(
                            cursor.getString(cursor.getColumnIndex("id")),
                            cursor.getString(cursor.getColumnIndex("courseId")),
                            cursor.getString(cursor.getColumnIndex("testDate")),
                            cursor.getString(cursor.getColumnIndex("startDate")),
                            cursor.getString(cursor.getColumnIndex("endDate")),
                            cursor.getString(cursor.getColumnIndex("testName")),
                            cursor.getString(cursor.getColumnIndex("testType")),
                            cursor.getString(cursor.getColumnIndex("duration")),
                            cursor.getString(cursor.getColumnIndex("createdDate")),
                            cursor.getString(cursor.getColumnIndex("showResultsToStudent")),
                            cursor.getString(cursor.getColumnIndex("active")),
                            cursor.getString(cursor.getColumnIndex("facultyId")),
                            cursor.getString(cursor.getColumnIndex("maxmAttempt")),
                            cursor.getString(cursor.getColumnIndex("maxmScore")),
                            cursor.getString(cursor.getColumnIndex("studentCount")),
                            cursor.getString(cursor.getColumnIndex("schoolName")));
                }
            }

            cursor.close();
            return testDetailsDataModel;
        }
        catch (Exception e)
        {
            new MyLog(NMIMSApplication.getAppContext()).debug("TestDetailsDataModel",e.getMessage());
            e.printStackTrace();
            return null;
        }
    }*/

    public void deleteOldLecture(String lectureId)
    {
        try
        {
            SQLiteDatabase db = this.getWritableDatabase();
            String sql = "delete from " + "lecture_data" + " where lectureId= '" + lectureId + "'";
            db.execSQL(sql);
            db.close();
        }
        catch (Exception e)
        {
            new MyLog(NMIMSApplication.getAppContext()).debug("deleteOldLectureEx", e.getMessage());
        }
    }

    public void deleteOldLectureByDate(String class_date)
    {
        try
        {
            SQLiteDatabase db = this.getWritableDatabase();
            String sql = "delete from " + "lecture_data" + " where class_date= '" + class_date + "'";
            new MyLog(NMIMSApplication.getAppContext()).debug("deleteOldLectureByDate",sql);
            db.execSQL(sql);
            db.close();
        }
        catch (Exception e)
        {
            new MyLog(NMIMSApplication.getAppContext()).debug("deleteOldLectureByDate", e.getMessage());
        }
    }

    public void deleteOldStudentByDate(String dateEntry)
    {
        try
        {
            SQLiteDatabase db = this.getWritableDatabase();
            String sql = "delete from " + "student_data" + " where dateEntry= '" + dateEntry + "'";
            new MyLog(NMIMSApplication.getAppContext()).debug("deleteOldStudentBydateEx", sql);
            db.execSQL(sql);
            db.close();
        }
        catch (Exception e)
        {
            new MyLog(NMIMSApplication.getAppContext()).debug("deleteOldStudentBydateEx", e.getMessage());
        }
    }

    public void deleteOldStudent(String lectureId)
    {
        try
        {
            SQLiteDatabase db = this.getWritableDatabase();
            String sql = "delete from " + "student_data" + " where lectureId= '" + lectureId + "'";
            db.execSQL(sql);
            db.close();
        }
        catch (Exception e)
        {
            new MyLog(NMIMSApplication.getAppContext()).debug("deleteOldStudentEx", e.getMessage());
        }
    }

    public void deleteOldSurveyName(String survey_id)
    {
        try
        {
            SQLiteDatabase db = this.getWritableDatabase();
            String sql = "delete from " + "survey_name" + " where id= '" + survey_id + "'";
            db.execSQL(sql);
            db.close();
        }
        catch (Exception e)
        {
            new MyLog(NMIMSApplication.getAppContext()).debug("deleteOldSurveyName", e.getMessage());
        }
    }

    public void deleteOldSurveyQuestion(String survey_id)
    {
        try
        {
            SQLiteDatabase db = this.getWritableDatabase();
            String sql = "delete from " + "survey_question" + " where id= '" + survey_id + "'";
            db.execSQL(sql);
            db.close();
        }
        catch (Exception e)
        {
            new MyLog(NMIMSApplication.getAppContext()).debug("deleteOldSurveyQuestion", e.getMessage());
        }
    }



    public ArrayList<AttendanceStudentDataModel> getStudentData(String lectureId, String startTime, String endTime, String isAttendanceSubmitted, int y)
    {
        try
        {
            AttendanceStudentDataModel attendanceStudentDataModel = new AttendanceStudentDataModel();
            ArrayList<AttendanceStudentDataModel> attendanceStudentDataModelArrayList = new ArrayList<>();
            SQLiteDatabase db = this.getWritableDatabase();
            Cursor cursor = db.query("student_data",
                    new String[]{"studentName" , "studentSapId" , "studentRollNo" , "studentAbsentPresent" , "courseId" , "isMarked" , "startTime" , "endTime", "isAttendanceSubmitted", "dateEntry", "lectureId", "attendanceStatus" , "attendanceFlag" , "schoolName" , "blockMarking" ,"createdDate" , "lastModifiedDate"},"lectureId" + " = ? and " + "startTime" + " = ? and " + "endTime" + " = ? and " + "isAttendanceSubmitted" + " = ? ",
                    new String[]{lectureId,startTime,endTime,isAttendanceSubmitted },null, null, "studentRollNo ASC", null);

            if(cursor != null)
            {
                if(cursor.moveToFirst())
                {
                    do
                    {
                        attendanceStudentDataModel = new AttendanceStudentDataModel(
                                cursor.getString(cursor.getColumnIndex("studentName")),
                                cursor.getString(cursor.getColumnIndex("studentSapId")),
                                cursor.getString(cursor.getColumnIndex("studentRollNo")),
                                cursor.getString(cursor.getColumnIndex("studentAbsentPresent")),
                                cursor.getString(cursor.getColumnIndex("courseId")),
                                cursor.getString(cursor.getColumnIndex("isMarked")),
                                cursor.getString(cursor.getColumnIndex("startTime")),
                                cursor.getString(cursor.getColumnIndex("endTime")),
                                cursor.getString(cursor.getColumnIndex("isAttendanceSubmitted")),
                                cursor.getString(cursor.getColumnIndex("dateEntry")),
                                cursor.getString(cursor.getColumnIndex("lectureId")),
                                cursor.getString(cursor.getColumnIndex("attendanceStatus")),
                                cursor.getString(cursor.getColumnIndex("attendanceFlag")),
                                cursor.getString(cursor.getColumnIndex("schoolName")),
                                cursor.getString(cursor.getColumnIndex("blockMarking")),
                                cursor.getString(cursor.getColumnIndex("createdDate")),
                                cursor.getString(cursor.getColumnIndex("lastModifiedDate")));

                        attendanceStudentDataModelArrayList.add(attendanceStudentDataModel);
                    }
                    while(cursor.moveToNext());
                }
            }

            cursor.close();
            return attendanceStudentDataModelArrayList;
        }
        catch (Exception e)
        {
            new MyLog(NMIMSApplication.getAppContext()).debug("LecturesDataModelAbs",e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    public SchoolAttendanceDataModel getSchoolAttendanceDataModel(String schoolName)
    {
        try
        {
            SchoolAttendanceDataModel schoolAttendanceDataModel = new SchoolAttendanceDataModel();
            SQLiteDatabase db = this.getWritableDatabase();
            Cursor cursor = db.query("attendance_data",
                    new String[]{"schoolName", "date", "status", "isExpanded", "endTime", "isApiHit"},"schoolName" + "=?",
                    new String[]{schoolName},null, null, null, null);

            if(cursor != null)
            {
                cursor.moveToFirst();
                if(cursor.getCount() > 0)
                {
                    schoolAttendanceDataModel = new SchoolAttendanceDataModel(
                            cursor.getString(cursor.getColumnIndex("schoolName")),
                            cursor.getString(cursor.getColumnIndex("date")),
                            cursor.getString(cursor.getColumnIndex("status")),
                            cursor.getString(cursor.getColumnIndex("isExpanded")),
                            cursor.getString(cursor.getColumnIndex("endTime")),
                            cursor.getString(cursor.getColumnIndex("isApiHit")));
                }
            }

            cursor.close();
            return schoolAttendanceDataModel;
        }
        catch (Exception e)
        {
            new MyLog(NMIMSApplication.getAppContext()).debug("SchoolAttendanceEx",e.getMessage());
            e.printStackTrace();
            return null;
        }
    }



   /* public ArrayList<LaundryDataModel> getUnSubmittedLaundryList(String submitted)
    {
        try
        {
            LaundryDataModel laundryDataModel = new LaundryDataModel();
            ArrayList<LaundryDataModel> unSubmittedLaundryDataModelArrayList = new ArrayList<>();
            SQLiteDatabase db = this.getWritableDatabase();
            Cursor cursor = db.query("laundry_data",
                    new String[]{"id", "itemName", "itemType", "itemQuantity", "submitted" , "uniqueKey"},"submitted" + "=?",
                    new String[]{submitted},null, null, "id ASC", null);

            if(cursor != null)
            {
                cursor.moveToFirst();
                do
                {
                    laundryDataModel = new LaundryDataModel(
                            cursor.getInt(cursor.getColumnIndex("id")),
                            cursor.getString(cursor.getColumnIndex("itemName")),
                            cursor.getString(cursor.getColumnIndex("itemType")),
                            cursor.getString(cursor.getColumnIndex("itemQuantity")),
                            cursor.getString(cursor.getColumnIndex("submitted")),
                            cursor.getString(cursor.getColumnIndex("uniqueKey")));
                    unSubmittedLaundryDataModelArrayList.add(laundryDataModel);
                }
                while (cursor.moveToNext());
            }

            cursor.close();
            return unSubmittedLaundryDataModelArrayList;
        }
        catch (Exception e)
        {
            new MyLog(NMIMSApplication.getAppContext()).debug("SchoolAttendanceEx",e.getMessage());
            e.printStackTrace();
            return null;
        }
    }*/

    public MyDate getMyDate(long dateId)
    {
        MyDate myDate = new MyDate();
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.query("myDate",
                new String[]{"dateId", "currentDate", "startDate", "endDate"},"dateId" + "=?",
                new String[]{String.valueOf(dateId)},null, null, null, null);

        if(cursor != null)
        {
            cursor.moveToFirst();
            if(cursor.getCount() > 0)
            {
                myDate = new MyDate(
                        cursor.getString(cursor.getColumnIndex("dateId")),
                        cursor.getString(cursor.getColumnIndex("currentDate")),
                        cursor.getString(cursor.getColumnIndex("startDate")),
                        cursor.getString(cursor.getColumnIndex("endDate")));
            }
        }

        cursor.close();
        return myDate;
    }

    public MyPermission getMyPermission(long permissionId)
    {
        MyPermission  myPermission = new MyPermission();
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.query("myPermission",
                new String[]{"permissionId", "permissionName", "permissionStatus"},"permissionId" + "=?",
                new String[]{String.valueOf(permissionId)},null, null, null, null);

        if(cursor != null)
        {
            cursor.moveToFirst();
            if(cursor.getCount() > 0)
            {
                myPermission = new MyPermission(
                        cursor.getString(cursor.getColumnIndex("permissionId")),
                        cursor.getString(cursor.getColumnIndex("permissionName")),
                        cursor.getString(cursor.getColumnIndex("permissionStatus")));
            }
        }

        cursor.close();
        return myPermission;
    }

    public NotificationDataModel getNotificationData(long not_id)
    {
        NotificationDataModel  notificationDataModel = new NotificationDataModel();
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.query("myNotification",
                new String[]{"not_id", "playerId"},"not_id" + "=?",
                new String[]{String.valueOf(not_id)},null, null, null, null);

        if(cursor != null)
        {
            cursor.moveToFirst();
            if(cursor.getCount() > 0)
            {
                notificationDataModel = new NotificationDataModel(
                        cursor.getString(cursor.getColumnIndex("not_id")),
                        cursor.getString(cursor.getColumnIndex("playerId")));
            }
        }

        cursor.close();
        return notificationDataModel;
    }

    public void updateSchoolAttendanceDataDetails(String endTime, String status, String isApiHit, String isExpanded, String date, String schoolName)
    {
        try
        {
            SQLiteDatabase db = this.getWritableDatabase();
            String sql = "";
            sql = sql + "Update attendance_data SET ";
            sql = sql + "endTime = " + "'" + endTime + "',";
            sql = sql + "status = " + "'" + status + "',";
            sql = sql + "isApiHit = " + "'" + isApiHit + "',";
            sql = sql + "isExpanded = " + "'" + isExpanded + "',";
            sql = sql + "date = " + "'" + date + "'";
            sql = sql + " Where schoolName = " + "'" + schoolName + "'";
            new MyLog(NMIMSApplication.getAppContext()).debug("updateSAD_sql",sql);
            db.execSQL(sql);
            db.close();
        }
        catch (Exception e)
        {
            new MyLog(NMIMSApplication.getAppContext()).debug("updateSAD_DetailsEx",e.getMessage());
            e.printStackTrace();
        }
    }

    public void updateStudentData(String isMarked, String courseId, String startTime, String endTime, String dateEntry, String lectureId)
    {
        try
        {
            SQLiteDatabase db = this.getWritableDatabase();
            String sql = "";
            sql = sql + "Update student_data SET ";
            sql = sql + "isMarked = " + "'" + isMarked + "'";
            sql = sql + " Where courseId = " + "'" + courseId + "'";
            sql = sql + " and startTime = " + "'" + startTime + "'";
            sql = sql + " and endTime = " + "'" + endTime + "'";
            sql = sql + " and dateEntry = " + "'" + dateEntry + "'";
            sql = sql + " and lectureId = " + "'" + lectureId + "'";
            new MyLog(NMIMSApplication.getAppContext()).debug("updateStudentData",sql);
            db.execSQL(sql);
            db.close();
        }
        catch (Exception e)
        {
            new MyLog(NMIMSApplication.getAppContext()).debug("updateStudentDataEx",e.getMessage());
            e.printStackTrace();
        }
    }

    public void updateAttendanceStatus(String attendanceStatus, String lastModifiedDate, String courseId, String startTime, String endTime, String lectureId, String schoolName, String dateEntry)
    {
        try
        {
            SQLiteDatabase db = this.getWritableDatabase();
            String sql = "";
            sql = sql + "Update student_data SET ";
            sql = sql + "attendanceStatus = " + "'" + attendanceStatus + "',";
            sql = sql + "lastModifiedDate = " + "'" + lastModifiedDate + "'";
            sql = sql + " Where courseId = " + "'" + courseId + "'";
            sql = sql + " and startTime = " + "'" + startTime + "'";
            sql = sql + " and endTime = " + "'" + endTime + "'";
            sql = sql + " and lectureId = " + "'" + lectureId + "'";
            sql = sql + " and schoolName = " + "'" + schoolName + "'";
            sql = sql + " and dateEntry = " + "'" + dateEntry + "'";
            new MyLog(NMIMSApplication.getAppContext()).debug("updateAttendanceStatus",sql);
            db.execSQL(sql);
            db.close();
        }
        catch (Exception e)
        {
            new MyLog(NMIMSApplication.getAppContext()).debug("updateAttendanceStaEx",e.getMessage());
            e.printStackTrace();
        }
    }
    public void updateStudentStatusData(String studentAbsentPresent, String studentSapId, String startTime, String endTime, String isAttendanceSubmitted, String lectureId, String dateEntry)
    {
        try
        {
            SQLiteDatabase db = this.getWritableDatabase();
            String sql = "";
            sql = sql + "Update student_data SET ";
            sql = sql + "studentAbsentPresent = " + "'" + studentAbsentPresent + "',";
            sql = sql + "isAttendanceSubmitted = " + "'" + isAttendanceSubmitted + "'";
            sql = sql + " Where studentSapId = " + "'" + studentSapId + "'";
            sql = sql + " and startTime = " + "'" + startTime + "'";
            sql = sql + " and endTime = " + "'" + endTime + "'";
            sql = sql + " and lectureId = " + "'" + lectureId + "'";
            sql = sql + " and dateEntry = " + "'" + dateEntry + "'";
            new MyLog(NMIMSApplication.getAppContext()).debug("updateStudentData",sql);
            db.execSQL(sql);
            db.close();
        }
        catch (Exception e)
        {
            new MyLog(NMIMSApplication.getAppContext()).debug("updateStudentStatusEx",e.getMessage());
            e.printStackTrace();
        }
    }

    public Cursor getUpdateAttendanceStatus()
    {
        String sql = "select * from student_data";
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(sql, null);
        return cursor;
    }


    public void updateStudentAttendanceFlag(String lectureId, String startTime, String endTime, String attendanceFlag, String schoolName, String dateEntry)
    {
        try
        {
            SQLiteDatabase db = this.getWritableDatabase();
            String sql = "";
            sql = sql + "Update student_data SET ";
            sql = sql + "attendanceFlag = " + "'" + attendanceFlag + "'";
            sql = sql + " Where lectureId = " + "'" + lectureId + "'";
            sql = sql + " and startTime = " + "'" + startTime + "'";
            sql = sql + " and endTime = " + "'" + endTime + "'";
            sql = sql + " and schoolName = " + "'" + schoolName + "'";
            sql = sql + " and dateEntry = " + "'" + dateEntry + "'";
            new MyLog(NMIMSApplication.getAppContext()).debug("updateStudentData",sql);
            db.execSQL(sql);
            db.close();
        }
        catch (Exception e)
        {
            new MyLog(NMIMSApplication.getAppContext()).debug("updateStudentStatusEx",e.getMessage());
            e.printStackTrace();
        }
    }

    public void updateStudentAttendanceFlag(String lectureId, String startTime, String endTime, String attendanceFlag, String schoolName)
    {
        try
        {
            SQLiteDatabase db = this.getWritableDatabase();
            String sql = "";
            sql = sql + "Update student_data SET ";
            sql = sql + "attendanceFlag = " + "'" + attendanceFlag + "'";
            sql = sql + " Where lectureId = " + "'" + lectureId + "'";
            sql = sql + " and startTime = " + "'" + startTime + "'";
            sql = sql + " and endTime = " + "'" + endTime + "'";
            sql = sql + " and schoolName = " + "'" + schoolName + "'";
            new MyLog(NMIMSApplication.getAppContext()).debug("updateStudentData",sql);
            db.execSQL(sql);
            db.close();
        }
        catch (Exception e)
        {
            new MyLog(NMIMSApplication.getAppContext()).debug("updateStudentStatusEx",e.getMessage());
            e.printStackTrace();
        }
    }

    public void updateSurveyUserResponse(String survey_id, String question_id, String response)
    {
        try
        {
            SQLiteDatabase db = this.getWritableDatabase();
            String sql = "";
            sql = sql + "Update survey_user_response SET ";
            sql = sql + "response = " + "'" + response + "'";
            sql = sql + " Where survey_id = " + "'" + survey_id + "'";
            sql = sql + " and question_id = " + "'" + question_id + "'";
            new MyLog(NMIMSApplication.getAppContext()).debug("updateSurveyUserResponse",sql);
            db.execSQL(sql);
            db.close();
        }
        catch (Exception e)
        {
            new MyLog(NMIMSApplication.getAppContext()).debug("updateSurveyUserResponseEx",e.getMessage());
            e.printStackTrace();
        }
    }

    public void updateStudentStatusData(String lectureId, String startTime, String endTime, String isAttendanceSubmitted, String schoolName, int x)
    {
        try
        {
            SQLiteDatabase db = this.getWritableDatabase();
            String sql = "";
            sql = sql + "Update student_data SET ";
            sql = sql + "isAttendanceSubmitted = " + "'" + isAttendanceSubmitted + "'";
            sql = sql + " and startTime = " + "'" + startTime + "'";
            sql = sql + " and endTime = " + "'" + endTime + "'";
            sql = sql + " and lectureId = " + "'" + lectureId + "'";
            sql = sql + " and schoolName = " + "'" + schoolName + "'";
            new MyLog(NMIMSApplication.getAppContext()).debug("updateStudentStaData",sql);
            db.execSQL(sql);
            db.close();
        }
        catch (Exception e)
        {
            new MyLog(NMIMSApplication.getAppContext()).debug("updateStudentStatusEx",e.getMessage());
            e.printStackTrace();
        }
    }

    public void updateSchoolAttendanceDataDate(String date, String schoolName, String status)
    {
        try
        {
            SQLiteDatabase db = this.getWritableDatabase();
            String sql = "";
            sql = sql + "Update attendance_data SET ";
            sql = sql + "date = " + "'" + date + "',";
            sql = sql + "status = " + "'" + status + "'";
            sql = sql + " Where schoolName = " + "'" + schoolName + "'";
            new MyLog(NMIMSApplication.getAppContext()).debug("updateSAD_sql",sql);
            db.execSQL(sql);
            db.close();
        }
        catch (Exception e)
        {
            new MyLog(NMIMSApplication.getAppContext()).debug("updateSAD_DateEx",e.getMessage());
            e.printStackTrace();
        }
    }

    public void updateSchoolAttendanceDataIsExpanded(String isExpanded, String schoolName)
    {
        try
        {
            SQLiteDatabase db = this.getWritableDatabase();
            String sql = "";
            sql = sql + "Update attendance_data SET ";
            sql = sql + "isExpanded = " + "'" + isExpanded + "'";
            sql = sql + " Where schoolName = " + "'" + schoolName + "'";
            new MyLog(NMIMSApplication.getAppContext()).debug("updateSAD_sql",sql);
            db.execSQL(sql);
            db.close();
        }
        catch (Exception e)
        {
            new MyLog(NMIMSApplication.getAppContext()).debug("updateSAD_DateEx",e.getMessage());
            e.printStackTrace();
        }
    }


    public void updatePermission(MyPermission myPermission, long permissionId)
    {
        try
        {
            SQLiteDatabase db = this.getWritableDatabase();
            String sql = "";
            sql = sql + "Update myPermission SET ";
            sql = sql + "permissionStatus = " + "'" + myPermission.getPermissionStatus() + "'";
            sql = sql + " Where permissionId = " + "'" + permissionId + "'";

            db.execSQL(sql);
            db.close();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public void blockMarkingAttendance(String studentAbsentPresent, String lectureId, String studentSapId)
    {
        try
        {
            SQLiteDatabase db = this.getWritableDatabase();
            String sql = "";
            sql = sql + "Update student_data SET ";
            sql = sql + "studentAbsentPresent = " + "'" + studentAbsentPresent + "',";
            sql = sql + "blockMarking = " + "'Y'";
            sql = sql + " Where lectureId = " + "'" + lectureId + "' and";
            sql = sql + " studentSapId = " + "'" + studentSapId + "'";
            new MyLog(context).debug("sql---> ",sql);

            db.execSQL(sql);
            db.close();
        }
        catch (Exception e)
        {
            new MyLog(context).debug("blockMarkingAttendanceEx",e.getMessage());
            e.printStackTrace();
        }
    }

    public void updateStartDate(MyDate myDate, long dateId)
    {
        try
        {
            SQLiteDatabase db = this.getWritableDatabase();
            String sql = "";
            sql = sql + "Update myDate SET ";
            sql = sql + "startDate = " + "'" + myDate.getStartDate() + "'";
            sql = sql + " Where dateId = " + "'" + dateId + "'";

            db.execSQL(sql);
            db.close();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public void updateSurveyName(String submitted , String id)
    {
        try
        {
            SQLiteDatabase db = this.getWritableDatabase();
            String sql = "";
            sql = sql + "Update survey_name SET ";
            sql = sql + "submitted = " + "'" + submitted + "'";
            sql = sql + " Where id = " + "'" + id + "'";

            db.execSQL(sql);
            db.close();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public void updateEndDate(MyDate myDate, long dateId)
    {
        try
        {
            SQLiteDatabase db = this.getWritableDatabase();
            String sql = "";
            sql = sql + "Update myDate SET ";
            sql = sql + "endDate = " + "'" + myDate.getEndDate() + "'";
            sql = sql + " Where dateId = " + "'" + dateId + "'";

            db.execSQL(sql);
            db.close();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public void updateMyDate(MyDate myDate, long dateId)
    {
        try
        {
            SQLiteDatabase db = this.getWritableDatabase();
            String sql = " ";
            sql = sql + "Update myDate SET ";
            sql = sql + "currentDate = " + " ' " + myDate.getCurrentDate() + " ', ";
            sql = sql + "startDate = " + " ' " + myDate.getStartDate() + " ', ";
            sql = sql + "endDate = " + " ' " + myDate.getStartDate() + " ' ";
            sql = sql + " Where dateId = " + " ' " + dateId + " ' ";

            db.execSQL(sql);
            db.close();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public void deleteLectureData()
    {
        try
        {
            SQLiteDatabase db = this.getWritableDatabase();
            String sql = "DELETE FROM lecture_data";
            db.execSQL(sql);
            db.close();
            new MyLog(NMIMSApplication.getAppContext()).debug("deleteLectureData","deleteLectureData");
        }
        catch (Exception e)
        {
            new MyLog(NMIMSApplication.getAppContext()).debug("deleteLecture_Data", e.getMessage());
        }
    }

    public boolean deleteLaundryData(String uniqueKey)
    {
        boolean delFag = false;
        try
        {
            SQLiteDatabase db = this.getWritableDatabase();
            delFag = db.delete("laundry_data", "uniqueKey = '"+uniqueKey+"'"  , null) > 0;
            new MyLog(NMIMSApplication.getAppContext()).debug("deleteLaundryUniqueKey", String.valueOf(uniqueKey));
            new MyLog(NMIMSApplication.getAppContext()).debug("deleteLaundryData", String.valueOf(delFag));
            db.close();
            return delFag;
        }
        catch (Exception e)
        {
            new MyLog(NMIMSApplication.getAppContext()).debug("deleteLaundryDataEx", e.getMessage());
            return delFag;
        }
    }

    public void deleteAssignmentData()
    {
        try
        {
            SQLiteDatabase db = this.getWritableDatabase();
            String sql = "DELETE FROM assignment_data";
            db.execSQL(sql);
            db.close();
            new MyLog(NMIMSApplication.getAppContext()).debug("deleteAssignmentData","deleteAssignmentData");
        }
        catch (Exception e)
        {
            new MyLog(NMIMSApplication.getAppContext()).debug("deleteAssignmentDataEx", e.getMessage());
        }
    }

    public void deleteAnnouncementData()
    {
        try
        {
            SQLiteDatabase db = this.getWritableDatabase();
            String sql = "DELETE FROM announcement_data";
            db.execSQL(sql);
            db.close();
            new MyLog(NMIMSApplication.getAppContext()).debug("deleteAnnouncementData","deleteAnnouncementData");
        }
        catch (Exception e)
        {
            new MyLog(NMIMSApplication.getAppContext()).debug("deleteAnnouncementEx", e.getMessage());
        }
    }


    public void deleteSchoolAttendanceData()
    {
        try
        {
            SQLiteDatabase db = this.getWritableDatabase();
            String sql = "DELETE FROM student_data";
            db.execSQL(sql);
            db.close();
            new MyLog(NMIMSApplication.getAppContext()).debug("deleteSchoolAttendance","deleteSchoolAttendanceData");
        }
        catch (Exception e)
        {
            new MyLog(NMIMSApplication.getAppContext()).debug("deleteSchoolAtt_Data", e.getMessage());
        }
    }

    public void deleteMyDate()
    {
        try
        {
            SQLiteDatabase db = this.getWritableDatabase();
            String sql = "DELETE FROM myDate";
            db.execSQL(sql);
            db.close();
        }
        catch (Exception e)
        {
            new MyLog(NMIMSApplication.getAppContext()).debug("deleteMyDate", e.getMessage());
        }
    }

    public void deleteTestDetails()
    {
        try
        {
            SQLiteDatabase db = this.getWritableDatabase();
            String sql = "DELETE FROM test_details";
            db.execSQL(sql);
            db.close();
        }
        catch (Exception e)
        {
            new MyLog(NMIMSApplication.getAppContext()).debug("deleteTestDetails", e.getMessage());
        }
    }

    public void deleteTimeTableData()
    {
        try
        {
            SQLiteDatabase db = this.getWritableDatabase();
            String sql = "DELETE FROM timetable_data";
            db.execSQL(sql);
            db.close();
        }
        catch (Exception e)
        {
            new MyLog(NMIMSApplication.getAppContext()).debug("deleteTimeTableData", e.getMessage());
        }
    }

    public void deleteAllSurveyName()
    {
        try
        {
            SQLiteDatabase db = this.getWritableDatabase();
            String sql = "DELETE FROM survey_name";
            new MyLog(NMIMSApplication.getAppContext()).debug("deleteAllSurveyName", sql);
            db.execSQL(sql);
            db.close();
        }
        catch (Exception e)
        {
            new MyLog(NMIMSApplication.getAppContext()).debug("deleteAllSurveyNameEx", e.getMessage());
        }
    }

    public void deleteAllSurveyQuestion()
    {
        try
        {
            SQLiteDatabase db = this.getWritableDatabase();
            String sql = "DELETE FROM survey_question";
            new MyLog(NMIMSApplication.getAppContext()).debug("deleteAllSurveyQuestion", sql);
            db.execSQL(sql);
            db.close();
        }
        catch (Exception e)
        {
            new MyLog(NMIMSApplication.getAppContext()).debug("deleteAllSurveyQuestionEx", e.getMessage());
        }
    }

    public void deleteAllSurveyOption()
    {
        try
        {
            SQLiteDatabase db = this.getWritableDatabase();
            String sql = "DELETE FROM survey_option";
            new MyLog(NMIMSApplication.getAppContext()).debug("deleteAllSurveyOption", sql);
            db.execSQL(sql);
            db.close();
        }
        catch (Exception e)
        {
            new MyLog(NMIMSApplication.getAppContext()).debug("deleteAllSurveyOptionEx", e.getMessage());
        }
    }

    public void deleteAllSurveyUserResponse()
    {
        try
        {
            SQLiteDatabase db = this.getWritableDatabase();
            String sql = "DELETE FROM survey_user_response";
            new MyLog(NMIMSApplication.getAppContext()).debug("deleteAllSurveyUserResponse", sql);
            db.execSQL(sql);
            db.close();
        }
        catch (Exception e)
        {
            new MyLog(NMIMSApplication.getAppContext()).debug("deleteAllSurveyUserResponseEx", e.getMessage());
        }
    }


    public void deleteMyNotification()
    {
        try
        {
            SQLiteDatabase db = this.getWritableDatabase();
            String sql = "DELETE FROM myNotification";
            db.execSQL(sql);
            db.close();
        }
        catch (Exception e)
        {
            new MyLog(NMIMSApplication.getAppContext()).debug("deleteMyNotification", e.getMessage());
        }
    }

    public void deleteMyPermission()
    {
        try
        {
            SQLiteDatabase db = this.getWritableDatabase();
            String sql = "DELETE FROM myPermission";
            db.execSQL(sql);
            db.close();
        }
        catch (Exception e)
        {
            new MyLog(NMIMSApplication.getAppContext()).debug("deleteMyPermission", e.getMessage());
        }
    }

    public void updateCopyPreviousData(String copyPreviousAttendanceData)
    {
        try
        {
            SQLiteDatabase db = this.getWritableDatabase();
            String sql = "";
            sql = sql + "Update userData SET ";
            sql = sql + "copyPreviousAttendanceData = " + "'" + copyPreviousAttendanceData + "'";

            db.execSQL(sql);
            db.close();

            new MyLog(NMIMSApplication.getAppContext()).debug("updateCopyPreviousData",sql);
        }
        catch (Exception e)
        {
            e.printStackTrace();
            new MyLog(NMIMSApplication.getAppContext()).debug("updateCopyPreviousDataEx",e.getMessage());
        }
    }

    public void updateCurrentSchool(String currentSchool)
    {
        try
        {
            SQLiteDatabase db = this.getWritableDatabase();
            String sql = "";
            sql = sql + "Update userData SET ";
            sql = sql + "currentSchool = " + "'" + currentSchool + "'";

            db.execSQL(sql);
            db.close();

            new MyLog(NMIMSApplication.getAppContext()).debug("updateCurrentSchool",sql);
        }
        catch (Exception e)
        {
            e.printStackTrace();
            new MyLog(NMIMSApplication.getAppContext()).debug("updateCurrentSchoolEror",e.getMessage());
        }
    }

    public void updateLaundryData(String id, String submitted)
    {
        try
        {
            SQLiteDatabase db = this.getWritableDatabase();
            String sql = "";
            sql = sql + "Update laundry_data SET ";
            sql = sql + "submitted = " + "'" + submitted + "'";
            sql = sql + " Where id = " + "'" + id + "'";

            db.execSQL(sql);
            db.close();

            new MyLog(NMIMSApplication.getAppContext()).debug("updateLaundryData",sql);
        }
        catch (Exception e)
        {
            e.printStackTrace();
            new MyLog(NMIMSApplication.getAppContext()).debug("updateLaundryDataEx",e.getMessage());
        }
    }

    public void updateSurveyNameSubmitStatus(String id, String submitted)
    {
        try
        {
            SQLiteDatabase db = this.getWritableDatabase();
            String sql = "";
            sql = sql + "Update survey_name SET ";
            sql = sql + "submitted = " + "'" + submitted + "'";
            sql = sql + " Where id = " + "'" + id + "'";

            db.execSQL(sql);
            db.close();

            new MyLog(NMIMSApplication.getAppContext()).debug("updateSurveyNameSubmitStatus",sql);
        }
        catch (Exception e)
        {
            e.printStackTrace();
            new MyLog(NMIMSApplication.getAppContext()).debug("updateSurveyNameSubmitStatusEx",e.getMessage());
        }
    }

    public void updateSurveyNameResponseFlag(String id, String responseFlag)
    {
        try
        {
            SQLiteDatabase db = this.getWritableDatabase();
            String sql = "";
            sql = sql + "Update survey_name SET ";
            sql = sql + "responseFlag = " + "'" + responseFlag + "'";
            sql = sql + " Where id = " + "'" + id + "'";

            db.execSQL(sql);
            db.close();

            new MyLog(NMIMSApplication.getAppContext()).debug("updateSurveyNameResponseFlag",sql);
        }
        catch (Exception e)
        {
            e.printStackTrace();
            new MyLog(NMIMSApplication.getAppContext()).debug("updateSurveyNameResponseFlagEx",e.getMessage());
        }
    }

    public void updateSurveyNameLocallySubmitted(String id)
    {
        try
        {
            SQLiteDatabase db = this.getWritableDatabase();
            String sql = "";
            sql = sql + "Update survey_name SET ";
            sql = sql + "locallySubmitted = " + "'Y'";
            sql = sql + " Where id = " + "'" + id + "'";

            db.execSQL(sql);
            db.close();

            new MyLog(NMIMSApplication.getAppContext()).debug("updateSurveyNameLocallySubmitted",sql);
        }
        catch (Exception e)
        {
            e.printStackTrace();
            new MyLog(NMIMSApplication.getAppContext()).debug("updateSurveyNameLocallySubmittedEx",e.getMessage());
        }
    }

    public void deleteSurveyName()
    {
        try
        {
            SQLiteDatabase db = this.getWritableDatabase();
            String sql = "DELETE FROM survey_name where endDate < date() ";
            new MyLog(NMIMSApplication.getAppContext()).debug("deleteSurveyName",sql);
            db.execSQL(sql);
            db.close();
        }
        catch (Exception e)
        {
            new MyLog(NMIMSApplication.getAppContext()).debug("deleteSurveyNameEx", e.getMessage());
        }
    }

    public void deleteAllFacultyNotification()
    {
        try
        {
            SQLiteDatabase db = this.getWritableDatabase();
            String sql = "DELETE FROM faculty_notification ";
            new MyLog(NMIMSApplication.getAppContext()).debug("deleteAllFacultyNotification",sql);
            db.execSQL(sql);
            db.close();
        }
        catch (Exception e)
        {
            new MyLog(NMIMSApplication.getAppContext()).debug("deleteAllFacultyNotificationEx", e.getMessage());
        }
    }

    public void deleteFromAuthControl()
    {
        try
        {
            SQLiteDatabase db = this.getWritableDatabase();
            String sql = "DELETE FROM auth_control ";
            new MyLog(NMIMSApplication.getAppContext()).debug("deleteFromAuthControl",sql);
            db.execSQL(sql);
            db.close();
        }
        catch (Exception e)
        {
            new MyLog(NMIMSApplication.getAppContext()).debug("deleteFromAuthControlEx", e.getMessage());
        }
    }

    public void deleteFromBackEndControl()
    {
        try
        {
            SQLiteDatabase db = this.getWritableDatabase();
            String sql = "DELETE FROM backend_control ";
            new MyLog(NMIMSApplication.getAppContext()).debug("deleteFromBackEndControl",sql);
            db.execSQL(sql);
            db.close();
        }
        catch (Exception e)
        {
            new MyLog(NMIMSApplication.getAppContext()).debug("deleteFromBackEndControlEx", e.getMessage());
        }
    }

    public void deleteServerAddressFromBackEndControl()
    {
        try
        {
            SQLiteDatabase db = this.getWritableDatabase();
            String sql = "DELETE FROM backend_control where (key = 'myApiUrlUsermgmt') OR (key = 'myApiUrlLms') OR (key = 'myApiUrlUsermgmtCrud') OR  (key = 'myApiUrlSurvey') OR  (key = 'saltKey') OR  (key = 'secretKey')";
            new MyLog(NMIMSApplication.getAppContext()).debug("deleteServerAddressFromBackEndControl",sql);
            db.execSQL(sql);
            db.close();
        }
        catch (Exception e)
        {
            new MyLog(NMIMSApplication.getAppContext()).debug("deleteServerAddressFromBackEndControlEx", e.getMessage());
        }
    }



    public void deleteSurveyQuestion(String survey_id)
    {
        try
        {
            SQLiteDatabase db = this.getWritableDatabase();
            String sql = "DELETE FROM survey_question where survey_id = '"+survey_id+"' ";
            new MyLog(NMIMSApplication.getAppContext()).debug("deleteSurveyQuestion",sql);
            db.execSQL(sql);
            db.close();
        }
        catch (Exception e)
        {
            new MyLog(NMIMSApplication.getAppContext()).debug("deleteSurveyQuestionEx", e.getMessage());
        }
    }

    public void deleteSurveyOptions(String question_id)
    {
        try
        {
            SQLiteDatabase db = this.getWritableDatabase();
            String sql = "DELETE FROM survey_option where question_id = '"+question_id+"' ";
            new MyLog(NMIMSApplication.getAppContext()).debug("deleteSurveyOptions",sql);
            db.execSQL(sql);
            db.close();
        }
        catch (Exception e)
        {
            new MyLog(NMIMSApplication.getAppContext()).debug("deleteSurveyOptionsEx", e.getMessage());
        }
    }

    public void deleteSurveyUserResponse(String survey_id, String question_id)
    {
        try
        {
            SQLiteDatabase db = this.getWritableDatabase();
            String sql = "DELETE FROM survey_user_response where question_id = '"+question_id+"' and  survey_id = '"+survey_id+"' ";
            new MyLog(NMIMSApplication.getAppContext()).debug("deleteSurveyUserResponse",sql);
            db.execSQL(sql);
            db.close();
        }
        catch (Exception e)
        {
            new MyLog(NMIMSApplication.getAppContext()).debug("deleteSurveyUserResponseEx", e.getMessage());
        }
    }

    public void deleteFacultyNotification(String id)
    {
        try
        {
            SQLiteDatabase db = this.getWritableDatabase();
            String sql = "DELETE FROM faculty_notification where id = '"+id+"' ";
            new MyLog(NMIMSApplication.getAppContext()).debug("deleteFacultyNotification",sql);
            db.execSQL(sql);
            db.close();
        }
        catch (Exception e)
        {
            new MyLog(NMIMSApplication.getAppContext()).debug("deleteFacultyNotificationEx", e.getMessage());
        }
    }


    public void updateUserDetails(String email, String phone)
    {
        try
        {
            SQLiteDatabase db = this.getWritableDatabase();
            String sql = "";
            sql = sql + "Update userData SET ";
            sql = sql + "emailId = " + "'" + email + "',";
            sql = sql + "mobile = " + "'" + phone + "'";

            db.execSQL(sql);
            db.close();

            new MyLog(NMIMSApplication.getAppContext()).debug("updateUserDetails",sql);
        }
        catch (Exception e)
        {
            e.printStackTrace();

            new MyLog(NMIMSApplication.getAppContext()).debug("updateUserDetailsError",e.getMessage());
        }
    }

   /* public void updateAssignmentEvaluationStatus(String assignmentId)
    {
        try
        {
            SQLiteDatabase db = this.getWritableDatabase();
            String sql = "";
            sql = sql + "Update assignment_data SET ";
            sql = sql + "evaluationStatus = " + "'Y' ,";
            sql = sql + "assignmentStatus = " + "'Completed'";
            sql = sql + " Where assignmentId = " + "'" + assignmentId + "'";

            db.execSQL(sql);
            db.close();

            new MyLog(NMIMSApplication.getAppContext()).debug("updateAssignmentEvaluationStatus",sql);
        }
        catch (Exception e)
        {
            e.printStackTrace();
            new MyLog(NMIMSApplication.getAppContext()).debug("updateAssignmentEvaluationStatusEx",e.getMessage());
        }
    }*/

    public void updateAssignmentStatus(String assignmentId, String assignmentStatus)
    {
        try
        {
            SQLiteDatabase db = this.getWritableDatabase();
            String sql = "";
            sql = sql + "Update assignment_data SET ";
            sql = sql + "assignmentStatus = " + "'" + assignmentStatus + "'";
            sql = sql + " Where assignmentId = " + "'" + assignmentId + "' ";

            db.execSQL(sql);
            db.close();

            new MyLog(NMIMSApplication.getAppContext()).debug("updateAssignmentEvaluationStatus",sql);
        }
        catch (Exception e)
        {
            e.printStackTrace();
            new MyLog(NMIMSApplication.getAppContext()).debug("updateAssignmentEvaluationStatusEx",e.getMessage());
        }
    }

    public void updateAssignmentStudentFilePath(String assignmentId, String studentFilePath)
    {
        try
        {
            SQLiteDatabase db = this.getWritableDatabase();
            String sql = "";
            sql = sql + "Update assignment_data SET ";
            sql = sql + "studentFilePath = " + "'" + studentFilePath + "'";
            sql = sql + " Where assignmentId = " + "'" + assignmentId + "' ";

            db.execSQL(sql);
            db.close();

            new MyLog(NMIMSApplication.getAppContext()).debug("updateAssignmentStudentFilePath",sql);
        }
        catch (Exception e)
        {
            e.printStackTrace();
            new MyLog(NMIMSApplication.getAppContext()).debug("updateAssignmentStudentFilePathEX",e.getMessage());
        }
    }

    public void updateBackEndControl(String key, String value)
    {
        try
        {
            SQLiteDatabase db = this.getWritableDatabase();
            String sql = "";
            sql = sql + "Update backend_control SET ";
            sql = sql + "value = " + "'" + value + "'";
            sql = sql + " Where key = " + "'" + key + "' ";

            db.execSQL(sql);
            db.close();

            new MyLog(NMIMSApplication.getAppContext()).debug("updateBackEndControl",sql);
        }
        catch (Exception e)
        {
            e.printStackTrace();
            new MyLog(NMIMSApplication.getAppContext()).debug("updateBackEndControlEx",e.getMessage());
        }
    }

    public void updateFacultyAttendance(String presentFacultyId, String lectureId)
    {
        try
        {
            SQLiteDatabase db = this.getWritableDatabase();
            String sql = "";
            sql = sql + "Update lecture_data SET ";
            sql = sql + "presentFacultyId = " + "'" + presentFacultyId + "'";
            sql = sql + " Where lectureId = " + "'" + lectureId + "'";
            new MyLog(context).debug("sql---> ",sql);

            db.execSQL(sql);
            db.close();
        }
        catch (Exception e)
        {
            new MyLog(context).debug("updateFacultyAttendanceEx",e.getMessage());
            e.printStackTrace();
        }
    }

    /*public void updateFacultyAttendance(String presentFacultyId, String lectureId)
    {
        try
        {
            SQLiteDatabase db = this.getWritableDatabase();
            String sql = "";
            sql = sql + "Update lecture_data SET ";
            sql = sql + "presentFacultyId = " + "'" + presentFacultyId + "'";
            sql = sql + " Where lectureId = " + "'" + lectureId + "' and isAttendanceSubmitted = '' ";
            new MyLog(context).debug("sql---> ",sql);

            db.execSQL(sql);
            db.close();
        }
        catch (Exception e)
        {
            new MyLog(context).debug("updateFacultyAttendanceEx",e.getMessage());
            e.printStackTrace();
        }
    }*/

    public void updateFacultyNotificationReadStatus()
    {
        try
        {
            SQLiteDatabase db = this.getWritableDatabase();
            String sql = "";
            sql = sql + "Update faculty_notification SET ";
            sql = sql + "isRead = 'Y' ";
            db.execSQL(sql);
            db.close();

            new MyLog(NMIMSApplication.getAppContext()).debug("updateFacultyNotificationReadStatus",sql);
        }
        catch (Exception e)
        {
            e.printStackTrace();
            new MyLog(NMIMSApplication.getAppContext()).debug("updateFacultyNotificationReadStatusEx",e.getMessage());
        }
    }
    public ArrayList<LectureList> getStudentDateWiseTimetable(String date) {
        SQLiteDatabase db = this.getReadableDatabase();
        String sql = "select * from student_lecture_timetable where date_str = ? and active = 'A' order by start_time asc";
        Cursor cursor = db.rawQuery(sql, new String[]{date});
        ArrayList<LectureList> lectureList = new ArrayList<>();
        if (cursor.moveToFirst()) {
            do {
                String room_no = cursor.getString(1);
                String date_str = cursor.getString(2);
                String start_time = cursor.getString(3);
                String end_time = cursor.getString(4);
                String div = cursor.getString(5);
                String acad_session = cursor.getString(6);
                String event_name = cursor.getString(7);
                String sap_event_id=cursor.getString(8);
                lectureList.add(new LectureList(room_no, start_time, date_str, end_time, div, acad_session,event_name,sap_event_id));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return lectureList;
    }

    public ArrayList<String> getLecturesDate() {
        String sql = "select date_str from lecture_timetable";
        SQLiteDatabase db = this.getReadableDatabase();
        ArrayList<String> dateList = new ArrayList<>();
        Cursor cursor = db.rawQuery(sql, null);
        if (cursor.moveToFirst()) {
            do {
                String date = cursor.getString(0);
                dateList.add(date);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return dateList;
    }

    public ArrayList<String> getStudentLecturesDate() {
        String sql = "select distinct date_str from student_lecture_timetable where active = 'A'";
        SQLiteDatabase db = this.getReadableDatabase();
        ArrayList<String> dateList = new ArrayList<>();
        Cursor cursor = db.rawQuery(sql, null);
        if (cursor.moveToFirst()) {
            do {
                String date = cursor.getString(0);
                dateList.add(date);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return dateList;
    }

    public String getMinMaxDate(int minMax){
        SQLiteDatabase db = this.getReadableDatabase();
        String sql ;
        String date = null;
//        String sql = "SELECT MAX(SUBSTR(date_str, 7, 4) || SUBSTR(date_str, 4, 2) || SUBSTR(date_str, 1, 2)) as maxDate, MIN(SUBSTR(date_str, 7, 4) || SUBSTR(date_str, 4, 2) || SUBSTR(date_str, 1, 2)) as minDate from student_lecture_timetable";
        if(minMax == 1){
            sql = "SELECT date_str AS max_date_str, MAX(CAST(SUBSTR(date_str, 7, 4) || SUBSTR(date_str, 4, 2) || SUBSTR(date_str, 1, 2) AS INT)) as maxDate FROM student_lecture_timetable";
        }else{
            sql = "SELECT date_str AS max_date_str, MIN(CAST(SUBSTR(date_str, 7, 4) || SUBSTR(date_str, 4, 2) || SUBSTR(date_str, 1, 2) AS INT)) as minDate FROM student_lecture_timetable";
        }

        MinMaxDateModel lectureList = new MinMaxDateModel();
        Cursor cursor = db.rawQuery(sql, null);
        if (cursor.moveToFirst()) {
            do {
                date = cursor.getString(0);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return date;
    }

    public String getMinMaxDateForFaculty(int minMax){
        SQLiteDatabase db = this.getReadableDatabase();
        String sql ;
        String date = null;
//        String sql = "SELECT MAX(SUBSTR(date_str, 7, 4) || SUBSTR(date_str, 4, 2) || SUBSTR(date_str, 1, 2)) as maxDate, MIN(SUBSTR(date_str, 7, 4) || SUBSTR(date_str, 4, 2) || SUBSTR(date_str, 1, 2)) as minDate from student_lecture_timetable";
        if(minMax == 1){
            sql = "SELECT date_str AS max_date_str, MAX(CAST(SUBSTR(date_str, 7, 4) || SUBSTR(date_str, 4, 2) || SUBSTR(date_str, 1, 2) AS INT)) as maxDate FROM lecture_timetable";
        }else {
            sql = "SELECT date_str AS max_date_str, MIN(CAST(SUBSTR(date_str, 7, 4) || SUBSTR(date_str, 4, 2) || SUBSTR(date_str, 1, 2) AS INT)) as minDate FROM lecture_timetable";
        }
        Cursor cursor = db.rawQuery(sql, null);
        if (cursor.moveToFirst()) {
            do {
                date = cursor.getString(0);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return date;
    }

    public void insertSemesterDate(ArrayList<TimetableModel> timetableList) {
        try {
            SQLiteDatabase db = this.getWritableDatabase();
            for (int i = 0; i < timetableList.size(); i++) {
                String sql = "insert into timetable_date (dateString , isHoliday) values ('" + timetableList.get(i).dateString + "','" + timetableList.get(i).isHoliday + "')";
                db.execSQL(sql);

            }
            db.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void deleteStudentLectureList() {
        try {
            SQLiteDatabase db = this.getWritableDatabase();
            String sql = "DELETE FROM student_lecture_timetable";
            db.execSQL(sql);
            db.close();
        } catch (Exception e) {
            new MyLog(NMIMSApplication.getAppContext()).debug("deleteFromMicrosoftLoginEx", e.getMessage());
        }
    }

    public ArrayList<LectureList> getDateWiseTimetable(String date) {
        SQLiteDatabase db = this.getReadableDatabase();
        String sql = "select * from lecture_timetable where date_str = ? and active = 'A' order by start_time asc";
        Cursor cursor = db.rawQuery(sql, new String[]{date});
        ArrayList<LectureList> lectureList = new ArrayList<>();
        if (cursor.moveToFirst()) {
            do {
                String room_no = cursor.getString(1);
                String date_str = cursor.getString(2);
                String start_time = cursor.getString(3);
                String end_time = cursor.getString(4);
                String div = cursor.getString(5);
                String acad_session = cursor.getString(6);
                String event_name = cursor.getString(7);
                String sap_event_id=cursor.getString(8);
                lectureList.add(new LectureList(room_no, start_time, date_str, end_time, div, acad_session,event_name,sap_event_id));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return lectureList;
    }

    public void insertLectureList(ArrayList<LectureList> lectureLists) {
        try {
            SQLiteDatabase db = this.getWritableDatabase();
            for (int i = 0; i < lectureLists.size(); i++) {
                String sql = "insert into lecture_timetable (room_no, date_str, start_time, end_time, div, acad_session, event_name, sap_event_id,active) values('" +
                        lectureLists.get(i).room_no
                        + "','" +lectureLists.get(i).date_str
                        + "','" +lectureLists.get(i).start_time
                        + "','" +lectureLists.get(i).end_time
                        + "','" +lectureLists.get(i).division
                        + "','" +lectureLists.get(i).acad_session
                        + "','" +lectureLists.get(i).event_name
                        + "','" +lectureLists.get(i).sap_event_id
                        + "','" + "A" + "')";
                db.execSQL(sql);
            }
            db.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public ArrayList<TimetableModel> getDateList() {
        String sql = "select * from timetable_date";
        SQLiteDatabase db = this.getReadableDatabase();
        ArrayList<TimetableModel> timetableModels = new ArrayList<>();
        Cursor cursor = db.rawQuery(sql, null);
        if (cursor.moveToFirst()) {
            do {
                String date = cursor.getString(1);
                int isHoliday = cursor.getInt(2);
                timetableModels.add(new TimetableModel(date, isHoliday));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return timetableModels;
    }

    public void insertStudentLectureList(ArrayList<LectureList> lectureLists) {
        try {
            SQLiteDatabase db = this.getWritableDatabase();
            for (int i = 0; i < lectureLists.size(); i++) {
                String sql = "insert into student_lecture_timetable (room_no, date_str, start_time, end_time, div, acad_session, event_name,sap_event_id, active) values('" +
                        lectureLists.get(i).room_no
                        + "','" + lectureLists.get(i).date_str
                        + "','" + lectureLists.get(i).start_time
                        + "','" + lectureLists.get(i).end_time
                        + "','" + lectureLists.get(i).division
                        + "','" + lectureLists.get(i).acad_session
                        + "','" + lectureLists.get(i).event_name
                        + "','" +lectureLists.get(i).sap_event_id
                        + "','" + "A" +"')";
                db.execSQL(sql);
            }
            db.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public void deleteLecureListData() {
        try {
            SQLiteDatabase db = this.getWritableDatabase();
            String sql = "DELETE FROM lecture_timetable";
            db.execSQL(sql);
            db.close();
        } catch (Exception e) {
            new MyLog(NMIMSApplication.getAppContext()).debug("deleteFromMicrosoftLoginEx", e.getMessage());
        }
    }
    public void deleteDivisionList() {
        try {
            SQLiteDatabase db = this.getWritableDatabase();
            String sql = "DELETE FROM student_div";
            db.execSQL(sql);
            db.close();
        } catch (Exception e) {
            new MyLog(NMIMSApplication.getAppContext()).debug("deleteFromMicrosoftLoginEx", e.getMessage());
        }
    }

    public void daleteDateTable() {
        try {
            SQLiteDatabase db = this.getWritableDatabase();
            String sql = "DELETE FROM timetable_date";
            db.execSQL(sql);
            db.close();
        } catch (Exception e) {
            new MyLog(NMIMSApplication.getAppContext()).debug("deleteFromMicrosoftLoginEx", e.getMessage());
        }
    }

    public void deleteLectureDataBySchool(String schoolName)
    {
        try
        {
            SQLiteDatabase db = this.getWritableDatabase();
            String sql = "DELETE FROM lecture_data where schoolName = '"+schoolName+"'";
            db.execSQL(sql);
            db.close();
            new MyLog(NMIMSApplication.getAppContext()).debug("deleteLectureData","deleteLectureData");
        }
        catch (Exception e)
        {
            new MyLog(NMIMSApplication.getAppContext()).debug("deleteLecture_Data", e.getMessage());
        }
    }

    public void deleteSchoolAttendanceDataBySchool(String schoolName)
    {
        try
        {
            SQLiteDatabase db = this.getWritableDatabase();
            String sql = "DELETE FROM student_data where schoolName = '"+schoolName+"' ";
            db.execSQL(sql);
            db.close();
            new MyLog(NMIMSApplication.getAppContext()).debug("deleteSchoolAttendance","deleteSchoolAttendanceData");
        }
        catch (Exception e)
        {
            new MyLog(NMIMSApplication.getAppContext()).debug("deleteSchoolAtt_Data", e.getMessage());
        }
    }
}
