package com.modastadoc.doctors.common.constants;

/**
 * Created by kunasi on 10/08/17.
 */

public class ServerConstants {

    //    public static final String DOMAIN = "http://192.168.1.7:81/";
    public static final String DOMAIN = "https://dashboard.modasta.com/";
    //public static final String DOMAIN = "https://devdashboard.modasta.com/";


    public static final String BASE_URL = "https://www.modasta.com";
    //public static final String BASE_URL = "https://dev.modasta.com";
//    public static final String BASE_URL = "http://192.168.1.7";
    /********************************************************/


    public static final String LOGIN = DOMAIN + "authenticate";


    /*Both UnAnswered and Followup details are in same api*/
    public static String GET_USER_PROFILE    = DOMAIN + "api/v1/patient/getprofile";
    public static final String UNANSWERED_QUERY = "api/v1/doctor/unanswered-query";
    public static final String FOLLLOW_UP_QUERY = "api/v1/doctor/followup/questions";

    public static final String QUERY_DETAIL = "api/v1/doctor/querydetail/";

    public static final String ACCEPTED_QUERY = "api/v1/doctor/accepted/question";
    public static final String ANSWERED_QUERY = "api/v1/doctor/answered/question";
    public static final String SUMMARY_REPORT = "api/v1/doctor/bookingdetail/report";

    public static final String ACCEPT_QUERY = "api/v1/doctor/accept/";
    public static final String SUBMIT_QUERY = "/api/v1/doctor/store";

    public static final String UNANSWERED_QUERY_DETAIL = "api/v1/doctor/view-unanswered-query/";

    public static final String VIEW_REPORT_API = "api/v1/doctor/viewreport/";

    public static final String VIEW_LAB_TESTS = "/api/v1/doctor/lablist/?from=";

    public static final String INHOUSE_REVIEW = "/api/v1/doctor/review/questions";

    public static String GCM_REGISTER = BASE_URL + "/api/user/registerdevice_doctor";

    public static final String CONSULTATION_FEE = DOMAIN + "api/v1/doctor/feesnew";

    public static String FORGOT_PASSWORD = BASE_URL + "/api/user/retrieve_password?";


    /*************************************************
     * Doc-Connect apis
     ************************************************/
    public static String GET_MAIN_FORUMS    = BASE_URL + "/api/user/getMainForum";
    public static String GET_CHILD_FORUMS   = BASE_URL + "/api/user/getchildForum";
    public static String GET_FORUM_TOPICS   = BASE_URL + "/api/user/allTopics/";
    public static String POST_TOPIC_COMMENT = BASE_URL + "/api/user/postReply/";


    public static String GET_ALL_TOPICS        = BASE_URL + "/api/user/allTopics/";
    public static String REQUEST_NEW_GROUP     = BASE_URL + "/api/user/requestNewGroup";
    public static String LOGIN_FOR_COOKIE      = BASE_URL + "/api/user/generate_auth_cookie";
    public static String CREATE_NEW_TOPIC      = BASE_URL + "/api/user/insertNewTopic/";
    public static String GET_EDITABLE_COMMENTS = BASE_URL + "/api/user/editable";
    public static String TOPIC_DETAIL_EDITABLE = BASE_URL + "/api/user/topicEditable";
    public static String UPLOAD_DOCS           = DOMAIN   + "api/v1/docconnect";
    public static String CHECK_SUBSCRIBE_TOPIC = BASE_URL + "/api/user/checksubscribed";
    public static String SUBSCRIBE_TOPIC       = BASE_URL + "/api/user/getsubscribe";
    public static String JOIN_GROUP            = BASE_URL + "/api/user/joinDocGroup";
    public static String COMMENTS_EDITABLE     = BASE_URL + "/api/user/replyEditable";
    public static String DELETE_ATTACHMENT     = BASE_URL + "/api/user/removeAttachment";

    public static String VIEW_LAB_REPORT       = DOMAIN   + "api/v1/lab/docviewreport/";
    public static final String UPLOAD_AUDIO = DOMAIN + "/apply/upload_doc_audio";

    /*
    *  Appointments APIS
    * */
    public static final String GET_APPOINTMENTS = DOMAIN + "api/v1/doctor/ajax-video-apmt";
    public static final String CANCEL_APPOINTMENT = DOMAIN + "api/v1/doctor/cancelapmt";
    public static final String DELAY_APPOINTMENT = DOMAIN + "api/v1/doctor/updatedslots/";
    public static final String EXTEND_APPOINTMENT = DOMAIN + "api/v1/doctor/updated-upcoming-slots/";
    public static final String GET_SESSION = DOMAIN + "api/v1/doctor/callpatient";
    public static final String GET_PATIENT_DETAILS = DOMAIN + "api/v1/doctor/doctor-video-chat/";
    public static final String LOAD_LAB_TESTS = DOMAIN + "api/v1/lab/load-lab-tests";
    public static final String SUMMARY_ADD_AND_SUBMIT = DOMAIN + "api/v1/doctor/closeapmt";
    public static final String SUMMARY_DOCUMENT_UPLOAD = DOMAIN + "api/v1/doctor/upload-pres/";
    public static final String GET_VIDEO_STATUS = DOMAIN + "api/v1/doctor/getvideostaus/";
    public static final String GET_ORDER_SUMMARY = DOMAIN + "api/v1/doctor/prevconsult/";
    public static final String GET_TIME_SLOT_STATUS = DOMAIN + "api/v1/doctor/getctime/";
    public static final String GET_NEW_LAB_LIST = DOMAIN + "api/v1/doctor/newlablist";

    public static final String GET_PROFILE = DOMAIN + "api/v1/doctor/editMyInfo/";
    public static final String UPDATE_PROFILE = DOMAIN + "api/v1/doctor/updateDocMyInfo/";

    public static final String PRECALL_TEST = DOMAIN+ "api/v1/doctor/pretest/";


    public static String GET_TOPICS = BASE_URL + "/wp-json/forums/";
    public static String getForumTopics(String forum_id ) {
        return GET_TOPICS + forum_id + "/topics";
    }

    public static String GET_TOPIC_DETAIL = BASE_URL + "/wp-json/topics/";
    public static String getTopicDetails(String topic_id)
    {
        return GET_TOPIC_DETAIL + topic_id;
    }

    public static String GET_TOPIC_COMMENTS = BASE_URL + "/wp-json/topics/";
    public static String getTopicComments(String topic_id) {
        return GET_TOPIC_COMMENTS + topic_id + "/replies";
    }
}
