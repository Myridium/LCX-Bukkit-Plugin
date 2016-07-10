/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.myridium.lcx.plugin;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

/**
 *
 * @author Murdock Grewar
 */
public class LCX_plugin extends JavaPlugin {
    
    @Override
    public void onEnable() {
        //
    }
    
    @Override
    public void onDisable() {
        //
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        
        if (sender instanceof Player) {
            // Command to be run is: /lcx
            Player playerSender = (Player) sender;
            
            playerSender.sendMessage("Hello " + playerSender.getName() + ".");
        }
        else
        {
            sender.sendMessage("This command must be run in-game.");
        }
        
        return true;
    }
}
