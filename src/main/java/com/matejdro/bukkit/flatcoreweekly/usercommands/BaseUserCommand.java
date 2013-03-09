package com.matejdro.bukkit.flatcoreweekly.usercommands;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.matejdro.bukkit.flatcoreweekly.Setting;
import com.matejdro.bukkit.flatcoreweekly.Settings;
import com.matejdro.bukkit.flatcoreweekly.Util;

public abstract class BaseUserCommand {
	public Boolean needPlayer;
	public String desc;

	public abstract Boolean run(CommandSender sender, String[] args);
	
	public Boolean execute(CommandSender sender, String[] args)
	{
		if (args.length > 0 && !Util.isInteger(args[0]))
		{
			String[] newargs = new String[args.length - 1];
			for (int i = 1; i < args.length; i++)
			{
				newargs[i - 1] = args[i];
			}
			args = newargs;			
		}
		
		if (!(sender instanceof Player) && needPlayer) 
		{
			Util.Message("Sorry, but you need to execute this command as player.", sender);
			return false;
		}
		if (sender instanceof Player && !((Player)sender).hasPermission("flatcoreweekly.command.ch")) 
		{
			Util.Message(Settings.getString(Setting.MESSAGE_NO_PERMISSION), sender);
			return false;
		}
		
		return run(sender, args);
	}

}