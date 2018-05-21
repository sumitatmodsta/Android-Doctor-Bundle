package com.modastadoc.doctors.gcm;

/**
 * Created by vijay.hiremath on 11/10/16.
 */
public class GCMContracts
{
    public static final String NEW_QUERY_NOTIFY = "new_query";
    public static final String FOLLOWUP_QUERY   = "followup_query";
    public static final String DOC_SUGGESTION   = "suggestion_by_doctor";
    public static final String DOC_MESSAGE      = "modasta_info"; //TODO confirm
    public static final String APP_UPDATE       = "app_update";   //TODO confirm

    public static final String D_BOOKING_SUCCESS = "d_booking_success";
    public static final String D_BOOKING_REMINDER = "d_booking_reminder";
    public static final String D15_BOOKING_REMINDER = "d15_booking_reminder";
    public static final String PATIENT_READY = "patient_ready";
    public static final String DOCTOR_RESHEDULE = "doctor_resedule";
    public static final String DOCTOR_CANCEL = "doctor_cancel";



    public static final String GCM_SLUG         = "gcm_slug";

    public static final int PUSH_NOTIFICATION_ID = 9189;
}
