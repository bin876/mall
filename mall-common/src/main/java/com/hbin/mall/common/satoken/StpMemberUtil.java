package com.hbin.mall.common.satoken;

import cn.dev33.satoken.SaManager;
import cn.dev33.satoken.jwt.StpLogicJwtForSimple;
import cn.dev33.satoken.session.SaSession;
import cn.dev33.satoken.stp.StpLogic;

import java.util.List;

public class StpMemberUtil {

    public static final String TYPE = "member";

    private static final StpLogic STP = new StpLogicJwtForSimple(TYPE);

    static {
        SaManager.putStpLogic(STP);
    }

    public static StpLogic getStpLogic() {
        return STP;
    }

    private StpMemberUtil() {
    }

    public static String getTokenValue() {
        return STP.getTokenValue();
    }

    public static SaSession getSession() {
        return STP.getSession();
    }

    public static void login(Object id) {
        STP.login(id);
    }

    public static void logout() {
        STP.logout();
    }

    public static boolean isLogin() {
        return STP.isLogin();
    }

    public static void checkLogin() {
        STP.checkLogin();
    }

    public static Object getLoginId() {
        return STP.getLoginId();
    }

    public static Long getLoginIdAsLong() {
        return STP.getLoginIdAsLong();
    }

    public static String getLoginType(){
        return STP.getLoginType();
    }

    public static cn.dev33.satoken.stp.SaTokenInfo getTokenInfo() {
        return STP.getTokenInfo();
    }

    public static List<String> getPermissionList() {
        return STP.getPermissionList();
    }

    public static boolean hasPermission(String permission) {
        return STP.hasPermission(permission);
    }

    public static void checkPermission(String permission) {
        STP.checkPermission(permission);
    }

    public static boolean hasPermissionOr(String... permissions) {
        return STP.hasPermissionOr(permissions);
    }

    public static void checkPermissionOr(String... permissions) {
        STP.checkPermissionOr(permissions);
    }
}
