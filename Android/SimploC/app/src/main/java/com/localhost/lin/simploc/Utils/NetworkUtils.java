package com.localhost.lin.simploc.Utils;

/**
 * Created by Lin on 2015/11/13.
 */
public class NetworkUtils {
    public static final String TEST_HOST_URL        = "http://172.21.100.58:8080/SimploServer";
    //public static final String HOST_URL             = "http://www.pockitcampus.com/SimploServer";
    public static final String HOST_URL             ="http://172.21.100.24:8080/SimploServer";
//    public static final String TEST_HOST_URL        = "http://192.168.1.102:8080/SimploServer";
//    public static final String HOST_URL             = "http://192.168.1.102:8080/SimploServer";
    public static final String LOGIN_URL            = HOST_URL + "/LoginPageServlet";
    public static final String TRY_LOGIN_URL        = HOST_URL + "/TryLoginServlet";
    public static final String C_IMG_URL            = HOST_URL + "/CheckImgServlet";
    public static final String TEST_QUERY_URL       = TEST_HOST_URL + "/QueryGradeServlet";
    public static final String TEST_AVATOR_URL      = TEST_HOST_URL + "/TouxiangTest";
    public static final String AVATOR_URL           = HOST_URL + "/GetAvatorServlet";
    public static final String XN_OPTIONS_URL       = HOST_URL + "/GradeOptionServlet";
    public static final String TB_XN_OP_URL         = HOST_URL + "/CourseOptionServlet";    //课表学年选项
    public static final String LESSON_URL           = HOST_URL + "/QueryCourseServlet";
    public static final String EXAM_URL             = HOST_URL + "/QueryExamServlet";
    public static final String CET_URL              = HOST_URL + "/QueryCETServlet";
    public static final String TEST_LESSON_URL        = TEST_HOST_URL +  "/QueryCourseServlet";
    public static final String ONE_KEY_COMMENT      = HOST_URL + "/OneKeyCommentServlet";
    public static final String LOGOUT               = HOST_URL + "/Logout";

    public static final String RQ_K_OPENID          = "openUserId";
}
