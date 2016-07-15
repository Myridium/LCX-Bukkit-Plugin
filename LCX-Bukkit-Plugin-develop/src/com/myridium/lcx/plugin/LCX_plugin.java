/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.myridium.lcx.plugin;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import shared.LCXDelegate;

/**
 *
 * @author Murdock Grewar <https://github.com/Myridium>
 */
public class LCX_plugin extends JavaPlugin {
    
    //Variables pulled from config.yml:
    private int TASK_END_CHECK_PERIOD_IN_TICKS;
    private int LCXTimeout;
    
    //A map of in-game player names to the authentication token strings associated with their bank session.
    //May cause thread issues...
    private Map<String,LCXDelegate> playerBankSessions;
    //Lists the players who are waiting for the MC chat to give them a reply to their request.
    private Map<String,Future<String>> awaitingCallback;
    private int replyTask;
    
    @Override
    public void onEnable() {
        //
        LCXTimeout = this.getConfig().getInt("LCXTimeout");
        TASK_END_CHECK_PERIOD_IN_TICKS = this.getConfig().getInt("refreshPeriodInTicks");
        
        
        playerBankSessions = new HashMap<>();
        awaitingCallback = new HashMap<>();
        
        replyTask = this.getServer().getScheduler().scheduleSyncRepeatingTask(this, new Runnable() {
            public void run() {
                sendReplies();
            }}, 0, TASK_END_CHECK_PERIOD_IN_TICKS);
    }
    
    @Override
    public void onDisable() {
        //Loop over all key value pairs in the playerBankSessions and log everyone out of the server.
        
        playerBankSessions.entrySet().stream().forEach((kvp) -> {
            try {logoutPlayer(kvp);} 
            catch (LCXDelegate.CommunicationException | LCXDelegate.UnexpectedResponseException e) {e.printStackTrace();}
        });
        
        this.getServer().getScheduler().cancelTask(replyTask);
        /*
        for(Entry<String,LCXDelegate> kvp : playerBankSessions.entrySet())
            logoutPlayer(kvp);
        */
            
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        
        if (sender instanceof Player) {
            
            //When the player sends a command like "/lcx hello chum", then:
            //arg[0] = hello
            //arg[1] = chum
            //label = lcx
            
            
            
            Player playerSender = (Player) sender;
            String playerUUID = playerSender.getUniqueId().toString();
            
            //Simply ignore the message if the user has already sent something.
            if (awaitingCallback.containsKey(playerUUID))
                return true;
            
                
            Future<String> ChatReply = this.getServer().getScheduler().callSyncMethod(this, new Callable<String>() {
                        public String call() {
                            return generateReply(playerUUID,label,args);
                        }
            });

            awaitingCallback.put(playerUUID, ChatReply);
            
            return true;
            
        }
        else
        {
            sender.sendMessage("This command must be run in-game.");
            return true;
        }
        
    }
    
    private void sendReplies() {
        
        for (Entry<String,Future<String>> entry : awaitingCallback.entrySet()) {
            Player player = Bukkit.getPlayer(UUID.fromString(entry.getKey()));
            if (!player.isOnline()) {
                awaitingCallback.entrySet().remove(entry);
                continue;
            }
            if (entry.getValue().isDone()) {
                try {
                    String msg;
                    msg = entry.getValue().get();
                    player.sendMessage(msg);
                } catch (InterruptedException ex) {
                    Logger.getLogger(LCX_plugin.class.getName()).log(Level.SEVERE, null, ex);
                    ex.printStackTrace();
                } catch (ExecutionException ex) {
                    Logger.getLogger(LCX_plugin.class.getName()).log(Level.SEVERE, null, ex);
                    ex.printStackTrace();
                }
                
                //Not sure about this:
                entry.getValue().cancel(true);
                awaitingCallback.entrySet().remove(entry);
                continue;
            }
        }
        
    }

    private String loginPlayer(String playerUUID, String[] args) throws LCXDelegate.CommunicationException, LCXDelegate.UnexpectedResponseException {
        
                if (args.length != 2) {
                    return EnumUserInfo.USAGE_LOGIN.msg();
                }
                String accountID = args[0];
                String password = args[1];

                //Begin a new session and add it to the `playerBankSessions' record.
                LCXDelegate newSession = new LCXDelegate(LCXTimeout);
                boolean loginSuccess;
                    
                loginSuccess = newSession.login(accountID,password);
                
                if (loginSuccess) {
                    playerBankSessions.put(playerUUID, newSession);
                }
                
                if (loginSuccess) {
                    return (EnumUserInfo.LOGIN_SUCCESS.msg() + accountID);
                } else {
                    return (EnumUserInfo.LOGIN_FAIL.msg());
                }
    }
    
    private String logoutPlayer(Entry<String,LCXDelegate> kvp) throws LCXDelegate.CommunicationException, LCXDelegate.UnexpectedResponseException {
        
        //Attempt to end the banking session.
        
        kvp.getValue().dispose();
        playerBankSessions.remove(kvp.getKey());
        
        return EnumUserInfo.LOGOUT_SUCCESS.msg();
        
    }
    
    private Entry<String,LCXDelegate> getPlayerSessionEntry(String playerUUID) {
        Entry<String,LCXDelegate> playerSessionEntry = null;
        for(Entry<String,LCXDelegate> kvp : playerBankSessions.entrySet()) {
            if(kvp.getKey().equals(playerUUID)) {
                playerSessionEntry = kvp;
                break;
            }
        }
        
        return playerSessionEntry;
    }
    
    private String transfer(Entry<String,LCXDelegate> playerUUIDandSession, String[] args) throws LCXDelegate.CommunicationException, LCXDelegate.UnexpectedResponseException {
        if (args.length != 2) {
            return EnumUserInfo.USAGE_TRANSFER.msg();
        }   
        
        LCXDelegate playerSession = playerUUIDandSession.getValue();

        try {
            Double.parseDouble(args[1]);
        } catch (NumberFormatException ne) {
            return EnumUserInfo.INVALID_LATINUM_AMOUNT.msg();
        }
        try {
            if(playerSession.transfer(args[0], args[1])) {
                return EnumUserInfo.TRANSFER_SUCCESS.msg();
            } else {
                return (EnumUserInfo.TRANSFER_FAIL.msg());
            }
        } catch (LCXDelegate.NotLoggedInException ne) {
            return (EnumUserInfo.ERROR_GENERIC.msg());
        }
    }
    
    private String balance(Entry<String,LCXDelegate> UUIDSession, String[] args) throws LCXDelegate.CommunicationException {
        if (args.length > 0)
            return EnumUserInfo.USAGE_BALANCE.msg();

        LCXDelegate playerSession = UUIDSession.getValue();
        
        String latAmount;
        
        try {
            latAmount = playerSession.balance();
            return ("Your Latinum balance is: " + latAmount);
        } catch (LCXDelegate.NotLoggedInException | LCXDelegate.UnexpectedResponseException ex) {
            Logger.getLogger(LCX_plugin.class.getName()).log(Level.SEVERE, null, ex);
            ex.printStackTrace();
        }
        
        return (EnumUserInfo.BALANCE_FAIL.msg());
    }
    
    private String generateReply(String playerUUID, String label, String[] args) {
        
        try {
                
                //This doesn't depend on whether or not the player is in a banking session:
                if (label.equals("create")) {
                    if (args.length != 3) {
                        return EnumUserInfo.USAGE_CREATE.msg();
                    }
                    if (!args[1].equals(args[2])) {
                        return EnumUserInfo.PASSWORD_CONFIRM_MISMATCH.msg();
                    }
                    
                    LCXDelegate accountCreateDelegate = new LCXDelegate(LCXTimeout);
                    String newAccountNum = accountCreateDelegate.newAccount(args[0],args[1]);
                    
                    return (EnumUserInfo.NEW_ACCOUNT_NUMBER.msg() + newAccountNum);
                    
                }

                //Check to see if the player is already in a banking session
                if (playerBankSessions.containsKey(playerUUID)) {
                    //The player is already in session.

                    Entry<String,LCXDelegate> UUIDSession = getPlayerSessionEntry(playerUUID);
                    LCXDelegate playerSession = UUIDSession.getValue();

                    if (label.equals("login")) {
                        if (playerSession.isLoggedIn()) {
                                return EnumUserInfo.ALREADY_LOGGED_IN.msg();
                            } else {
                                playerBankSessions.remove(playerUUID);
                                return loginPlayer(playerUUID,args);
                            }
                    }

                    if (playerSession.isLoggedIn()) {
                        switch(label) {

                            case "transfer":
                                return transfer(UUIDSession,args);
                            case "logout":
                                return logoutPlayer(UUIDSession);
                            case "balance":
                                return balance(UUIDSession,args);
                            default:
                                return EnumUserInfo.ERROR_GENERIC.msg();
                        }
                    } else {
                        try {logoutPlayer(UUIDSession);} catch (LCXDelegate.UnexpectedResponseException e) {
                            //Bukkit.getConsoleSender().sendMessage(EnumUserInfo.ERROR_LCX_SERVER_UNKNOWN_RESPONSE.msg());
                            //e.printStackTrace();
                        }
                        return (EnumUserInfo.WARN_LOGIN_FIRST.msg());
                    }

                }

                //The player is not already in session.
                if (!label.equals("login")) {
                    //Player is not logged in, yet is trying to issue a command that can only be done when logged in.
                    return (EnumUserInfo.WARN_LOGIN_FIRST.msg());
                }

                return loginPlayer(playerUUID,args);

            } catch (LCXDelegate.CommunicationException e) {
                return (EnumUserInfo.ERROR_LCX_SERVER_COMMUNICATION.msg());
            } catch (LCXDelegate.UnexpectedResponseException e) {
                return (EnumUserInfo.ERROR_LCX_SERVER_UNKNOWN_RESPONSE.msg());
            }
    }
    
    /*
    private Future<String> FutureString(String futStr) {
        return (this.getServer().getScheduler().callSyncMethod(this, new Callable<String>() {
 
                            public String call() {
                                return futStr;
                            }

                        }));
    }
    */
}
