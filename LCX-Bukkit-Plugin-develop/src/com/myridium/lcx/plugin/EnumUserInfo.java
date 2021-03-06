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
    
    USAGE_CREATE("Used 'create' incorrectly."),
    USAGE_LOGOUT("Used 'logout' incorrectly."),
    USAGE_LOGIN("Used 'login' incorrectly."),
    USAGE_TRANSFER("Used 'transfer' incorrectly."),
    USAGE_BALANCE("Used 'balance' incorrectly."),
    
    LOGOUT_SUCCESS("§a§nLogout successful."),
    LOGOUT_FAIL("§cLogout failed!"),
    LOGIN_SUCCESS("§a§nLogin successful. Account:   "),
    LOGIN_FAIL("§cFailed to login. Check your credentials."),
    
    PASSWORD_CONFIRM_MISMATCH("§cThe passwords provided did not match."),
    NEW_ACCOUNT_NUMBER("§a§nAccount created. Account number:   "),
    
    BALANCE_FAIL("§cUnable to retrieve balance!"),
    
    WARN_LOGIN_FIRST("§3You must first login using /login."),
    
    TRANSFER_SUCCESS("§aFunds transferred."),
    TRANSFER_FAIL("§cTransfer failed."),
    
    ALREADY_LOGGED_IN("§3You are already logged in to an account!"),
    
    ERROR_LCX_SERVER_COMMUNICATION("§cThere was a problem communicating with the LCX server."),
    ERROR_LCX_SERVER_UNKNOWN_RESPONSE("§cUnexpected response from the server. Contact an administrator."),
    ERROR_GENERIC("§cAn unspecified error occurred."),
    INVALID_LATINUM_AMOUNT("§cInvalid Latinum amount specified.");
    
    
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
