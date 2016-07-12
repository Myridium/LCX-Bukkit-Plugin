/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.myridium.lcx.plugin;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
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
 * @author Murdock Grewar
 */
public class LCX_plugin extends JavaPlugin {
    
    //A map of in-game player names to the authentication token strings associated with their bank session.
    Map<String,LCXDelegate> playerBankSessions;
    
    @Override
    public void onEnable() {
        //
        playerBankSessions = new HashMap<>();
    }
    
    @Override
    public void onDisable() {
        //Loop over all key value pairs in the playerBankSessions and log everyone out of the server.
        
        playerBankSessions.entrySet().stream().forEach((kvp) -> {
            try {logoutPlayer(kvp);} 
            catch (LCXDelegate.CommunicationException | LCXDelegate.UnexpectedResponseException e) {e.printStackTrace();}
        });
        
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
            
            
            try {
                //Check to see if the player is already in a banking session
                if (playerBankSessions.containsKey(playerUUID)) {
                    //The player is already in session.

                    LCXDelegate playerSession = playerBankSessions.get(playerUUID);

                    if (label.equals("login")) {
                        if (playerSession.isLoggedIn()) {
                                playerSender.sendMessage("You are already logged in!");
                                return true;
                            } else {
                                playerBankSessions.remove(playerUUID);
                                return loginPlayer(playerSender,args);
                            }
                    }

                    if (playerSession.isLoggedIn()) {
                        switch(label) {

                            case "transfer":
                                
                                playerSender.sendMessage("This feature is currently not supported.");
                                return true;
                            case "logout":

                                //Find the entry corresponding to this user.
                                try {
                                    logoutPlayer(playerSender);
                                } catch (LCXDelegate.UnexpectedResponseException e) {
                                    playerSender.sendMessage("Unexpected response from server. Contact an administrator.");
                                }

                                return true;

                            case "balance":
                                if (args.length > 0)
                                    return false;

                                double latAmount;
                                boolean successB = false;
                                try {
                                    latAmount = playerSession.balance();
                                    playerSender.sendMessage("Your Latinum balance is: " + String.valueOf(latAmount));
                                    successB = true;
                                } catch (LCXDelegate.NotLoggedInException | LCXDelegate.UnexpectedResponseException ex) {
                                    Logger.getLogger(LCX_plugin.class.getName()).log(Level.SEVERE, null, ex);
                                    ex.printStackTrace();
                                }
                                if (!successB)
                                    playerSender.sendMessage("Unable to retrieve balance!");
                                return true;
                            default:
                                return false;
                        }
                    } else {
                        try {logoutPlayer(playerSender);} catch (LCXDelegate.UnexpectedResponseException e) {
                            Bukkit.getConsoleSender().sendMessage("Unexpected response from Latinum server when logging player out.");
                            e.printStackTrace();
                        }
                        playerSender.sendMessage("You must first login.");
                        return true;
                    }

                }

                //The player is not already in session.
                if (!label.equals("login")) {
                    //Player is not logged in, yet is trying to issue a command that can only be done when logged in.
                    playerSender.sendMessage("You must first login using /login.");
                    return true;
                }

                return loginPlayer(playerSender,args);

            } catch (LCXDelegate.CommunicationException e) {
                playerSender.sendMessage("An error occured when trying to communicate with the server.");
                return true;
            } catch (LCXDelegate.UnexpectedResponseException e) {
                //Many of these exceptions are already handled.
                playerSender.sendMessage("The server sent a response that I couldn't interpret.");
                return true;
            }
            
        }
        else
        {
            sender.sendMessage("This command must be run in-game.");
            return true;
        }
        
    }
    
    private boolean loginPlayer(Player playerSender, String[] args) throws LCXDelegate.CommunicationException {
        //If we get to this point, then the player is trying to use the login command.
                if (args.length != 2) {
                    playerSender.sendMessage("Incorrect usage of the /login command.");

                    //I'm guessing that returning false causes the usage message to display.
                    return false;
                }

                try {
                    return loginPlayer(playerSender,args[0],args[1]);
                } catch (LCXDelegate.UnexpectedResponseException e) {
                    playerSender.sendMessage("Did not recognise response from server. Login failed.");
                    return true;
                }
    }

    private boolean loginPlayer(Player playerSender, String accountID, String password) throws LCXDelegate.CommunicationException, LCXDelegate.UnexpectedResponseException {
        //Begin a new session and add it to the `playerBankSessions' record.
                LCXDelegate newSession = new LCXDelegate();
                boolean loginSuccess;
                    
                Bukkit.getConsoleSender().sendMessage("Attempting login now...");
                loginSuccess = newSession.login(accountID,password);
                
                if (loginSuccess) {
                    playerBankSessions.put(playerSender.getUniqueId().toString(), newSession);
                    playerSender.sendMessage("Login successful.");
                    return true;
                } else {
                    playerSender.sendMessage("Login unsuccessful.");
                    return true;
                }
    }
    
    private void logoutPlayer(Entry<String,LCXDelegate> kvp) throws LCXDelegate.CommunicationException, LCXDelegate.UnexpectedResponseException {
        
        //Attempt to end the banking session.
        try {
            kvp.getValue().dispose();
        } catch (LCXDelegate.NotLoggedInException e) {
            //Do nothing.
        }
        playerBankSessions.remove(kvp.getKey());
    }
    
    private void logoutPlayer(Player playerSender) throws LCXDelegate.CommunicationException, LCXDelegate.UnexpectedResponseException {
        Entry<String,LCXDelegate> playerSessionEntry = null;
        for(Entry<String,LCXDelegate> kvp : playerBankSessions.entrySet()) {
            if(kvp.getKey().equals(playerSender.getUniqueId().toString())) {
                playerSessionEntry = kvp;
                break;
            }
        }
        logoutPlayer(playerSessionEntry);
    }
}
