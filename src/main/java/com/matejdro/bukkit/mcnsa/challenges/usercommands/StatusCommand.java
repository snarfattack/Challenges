package com.matejdro.bukkit.mcnsa.challenges.usercommands;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.matejdro.bukkit.mcnsa.challenges.IO;
import com.matejdro.bukkit.mcnsa.challenges.MCNSAChallenges;
import com.matejdro.bukkit.mcnsa.challenges.Setting;
import com.matejdro.bukkit.mcnsa.challenges.Settings;
import com.matejdro.bukkit.mcnsa.challenges.TimePrint;
import com.matejdro.bukkit.mcnsa.challenges.Util;
import com.matejdro.bukkit.mcnsa.challenges.WeekUtil;

public class StatusCommand extends BaseUserCommand {
	public StatusCommand()
	{
		desc = "Show status of your challenges";
		needPlayer = true;
		permission = "status";
	}


	public Boolean run(CommandSender sender, String[] args) {	
		Player player = (Player) sender;

		int week;
		if (args.length > 0 && Util.isInteger(args[0]))
			week = Integer.parseInt(args[0]);
		else
			week = WeekUtil.getCurrentWeek();

		String[] levels = null;
		List<Integer> levelValue = new ArrayList<Integer>(10);

		try {
			PreparedStatement statement = IO.getConnection().prepareStatement("SELECT Points FROM weekly_levels WHERE weekID = ? ORDER BY Level ASC");
			statement.setInt(1, week);
			ResultSet set = statement.executeQuery();

			int size = 0;
			while (set.next())
			{
				size++;
				levelValue.add(set.getInt(1));
			}

			statement.close();

			levels = new String[size];

			if (size > 0)
			{
				statement = IO.getConnection().prepareStatement("SELECT * FROM weekly_completed WHERE weekID = ? AND Player = ? ORDER BY Level ASC");
				statement.setInt(1, week);
				statement.setString(2, player.getName());

				set = statement.executeQuery();

				while (set.next())
				{
					int level = set.getInt("level");
					int state = set.getInt("State");

					String timeString = null;
					int time;
					int timeDiff;


					if (state > 0)
					{
						time = set.getInt("lastUpdate");
						timeDiff = (int) (System.currentTimeMillis() / 1000 - time);

						if (timeDiff > 31536000)
							timeString = "days ago";
						else
							timeString = TimePrint.formatSekunde(timeDiff);

					}

					for (int i = 0; i < level; i++)
					{
						if (levels[i] != null)
							continue;

						switch (state)
						{
						case 0:
							levels[i] = Settings.getString(Setting.MESSAGE_STATUS_WAITING_REVIEW);
							break;
						case 1:
							String status = Settings.getString(Setting.MESSAGE_STATUS_APPROVED);
							status = status.replace("<Time>", timeString);
							status = status.replace("<Points>", TimePrint.formatPoints(levelValue.get(i)));

							levels[i] = status;
							break;
						case 2:
						case 3:
							String comment = set.getString("ModResponse");
							if (comment == null || comment.trim().length() == 0)
							{
								status = Settings.getString(Setting.MESSAGE_STATUS_REJECTED);
							}
							else
							{
								 status = Settings.getString(Setting.MESSAGE_STATUS_REJECTED_COMMENT);
								 status = status.replace("<Comment>", comment);
							}
							status = status.replace("<Time>", timeString);

							levels[i] = status;

							break;
						}
					}
				}
			}

			
			String header = Settings.getString(Setting.MESSAGE_STATUS_HEADER);
			header = header.replace("<ID>", Integer.toString(week));
			
			long start = WeekUtil.getWeekStart(week);
			long end = start + WeekUtil.SECONDS_PER_WEEK - 1;
			header = header.replace("<From>", TimePrint.formatDate(start));
			header = header.replace("<To>", TimePrint.formatDate(end));
			header = header.replace("<Left>", TimePrint.formatSekunde(end - WeekUtil.getCurrentTime()));
			Util.Message(header, sender);


			for (int i = 0; i < levels.length; i++)
			{
				String row = Settings.getString(Setting.MESSAGE_STATUS_ENTRY);
				
				row = row.replace("<Level>", Integer.toString(i + 1));
				
				String status = levels[i];
				if (status == null)
					status = Settings.getString(Setting.MESSAGE_STATUS_NOT_SUBMITTED);
				
				row = row.replace("<Status>", status);
				
				Util.Message(row, sender);
			}


		}
		catch (SQLException e) {
			MCNSAChallenges.log.log(Level.SEVERE, "[FlatcoreWeekly]: Error while running list command! - " + e.getMessage());
			e.printStackTrace();
		}
		
		
		return true;		
	}

}