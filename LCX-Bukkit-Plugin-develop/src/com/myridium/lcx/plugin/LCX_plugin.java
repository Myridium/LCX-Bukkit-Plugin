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
            logoutPlayer(kvp);
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
            
            //Check to see if the player is already in a banking session
            if (playerBankSessions.containsKey(playerUUID)) {
                //The player is already in session.
                
                LCXDelegate playerSession = playerBankSessions.get(playerUUID);
                switch(label) {
                    case "login":
                        playerSender.sendMessage("You are already logged in!");
                        return true;
                    case "transfer":
                        /*
                        double transferAmount;
                        //Parsing the number could throw an error.
                        try {
                            transferAmount = Double.parseDouble(args[1]);
                        }
                        catch (NumberFormatException exc) {
                            playerSender.sendMessage("Invalid transfer amount specified.");
                            return false;
                        }
                        
                        if (playerSession.transfer(args[0],transferAmount)) {
                            playerSender.sendMessage("Transfer successful.");
                            return true;
                        } else {
                            playerSender.sendMessage("Transfer was unsuccessful.");
                            return true;
                        }
                        
                        */
                        playerSender.sendMessage("This feature is currently not supported.");
                        return true;
                    case "logout":
                        
                        //Find the entry corresponding to this user.
                        Entry<String,LCXDelegate> playerSessionEntry = null;
                        for(Entry<String,LCXDelegate> kvp : playerBankSessions.entrySet()) {
                            if(kvp.getKey().equals(playerUUID)) {
                                playerSessionEntry = kvp;
                                break;
                            }
                        }
                        
                        if (logoutPlayer(playerSessionEntry)) {
                            playerSender.sendMessage("Logout successful.");
                        } else {
                            playerSender.sendMessage("Logout unsuccessful! Consult an administrator.");
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
                        } catch (IOException ex) {
                            Logger.getLogger(LCX_plugin.class.getName()).log(Level.SEVERE, null, ex);
                        }
                        if (!successB)
                            playerSender.sendMessage("Unable to retrieve balance!");
                        return true;
                    default:
                        return false;
                }
                
                
            } else {
                //The player is not already in session.
                if (!label.equals("login")) {
                    //Player is not logged in, yet is trying to issue a command that can only be done when logged in.
                    playerSender.sendMessage("You must first login using /login.");
                    return true;
                }
                
                //If we get to this point, then the player is trying to use the login command.
                if (args.length != 2) {
                    playerSender.sendMessage("Incorrect usage of the /login command.");
                    
                    //I'm guessing that returning false causes the usage message to display.
                    return false;
                }
                
                //Begin a new session and add it to the `playerBankSessions' record.
                LCXDelegate newSession = new LCXDelegate();
                boolean loginSuccess;
                try {
                    Bukkit.getConsoleSender().sendMessage("Attempting login now...");
                    loginSuccess = newSession.login(args[0],args[1]);
                } catch (IOException ex) {
                    Bukkit.getConsoleSender().sendMessage("There was an IOException while attempting login.");
                    Logger.getLogger(LCX_plugin.class.getName()).log(Level.SEVERE, null, ex);
                    loginSuccess = false;
                }
                
                if (loginSuccess) {
                    playerBankSessions.put(playerSender.getUniqueId().toString(), newSession);
                    playerSender.sendMessage("Login successful.");
                    return true;
                } else {
                    playerSender.sendMessage("Login unsuccessful.");
                    return true;
                }
            }
            
        }
        else
        {
            sender.sendMessage("This command must be run in-game.");
            return true;
        }
        
    }

    private boolean logoutPlayer(Entry<String,LCXDelegate> kvp) {
        
        try {
            //Attempt to end the banking session.
            //If unsuccessful:
            if (!kvp.getValue().logout()) {
                Bukkit.getConsoleSender().sendMessage("Failed to log user " + kvp.getKey() + " out of their banking session.");
                return false;
            } else {
                playerBankSessions.remove(kvp.getKey());
                return true;
            }
        } catch (IOException ex) {
            Bukkit.getConsoleSender().sendMessage("Failed to log user " + kvp.getKey() + " out of their banking session.");
            return false;
        }
    }
}
