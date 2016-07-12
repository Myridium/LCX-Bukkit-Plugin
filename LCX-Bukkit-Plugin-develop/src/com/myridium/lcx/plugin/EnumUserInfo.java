/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.myridium.lcx.plugin;

/**
 *
 * @author Murdock
 */
public enum EnumUserInfo
    {
    LOGOUT_SUCCESS("§2You are logged out."),
    LOGOUT_FAIL("§cLogout failed!"),
    LOGIN_SUCCESS("§aLogin successful."),
    LOGIN_FAIL("§cFailed to login. Check your credentials."),
    
    BALANCE_FAIL("§cUnable to retrieve balance!"),
    
    WARN_LOGIN_FIRST("§3You must first login using /login."),
    
    ALREADY_LOGGED_IN("§3You are already logged in to an account!"),
    
    ERROR_LCX_SERVER_COMMUNICATION("§cThere was a problem communicating with the LCX server."),
    ERROR_LCX_SERVER_UNKNOWN_RESPONSE("§cUnexpected response from the server. Contact an administrator.");
    
    
    private String msg;
    
    private EnumUserInfo(String msg) {
            this.msg = msg;
        }
    
    public static EnumUserInfo fromString(String text) {
            if (text != null) {
                for (EnumUserInfo b : EnumUserInfo.values()) {
                    if (text.equals(b.msg())) {
                        return b;
                    }
                }
            }
            return null;
        }

        public String msg() {
            return msg;
        }
        
    public boolean equals(String inString)
        {
        boolean isEqual = false;
        if(inString.equals(msg))
            {
            isEqual = true;
            }
        return isEqual;
        }
    }
