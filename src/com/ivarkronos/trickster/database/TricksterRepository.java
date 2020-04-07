package com.ivarkronos.trickster.database;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.bukkit.entity.Player;

import com.ivarkronos.trickster.MainPlugin;

public class TricksterRepository {

	private Connection con;
	private MainPlugin plugin;
	
	public TricksterRepository(MainPlugin mainPlugin) {
		this.plugin = mainPlugin;
		this.con = plugin.getDbc().getConnection();
	}
	
	// IF NEVER PLAYED. INSERT PLAYER INTO DATABASE WITH DEFAULT VALUES
	public boolean hasPlayed(Player player) throws SQLException {
		ResultSet playerCheck = con.prepareStatement("SELECT COUNT(uuid) FROM TRICKSTER_STATS WHERE uuid = '"+player.getUniqueId()+"';").executeQuery();
		playerCheck.next();
		if (playerCheck.getInt(1) == 0) { // player not in the system
			con.prepareStatement("INSERT INTO TRICKSTER_STATS VALUES('"+player.getUniqueId()+"',DEFAULT, DEFAULT, DEFAULT, DEFAULT, DEFAULT, DEFAULT);").executeUpdate();
			return false;
		} 
		return true;
	}
	public boolean hasPlayedNoInitialize(Player player) throws SQLException {
		ResultSet playerCheck = con.prepareStatement("SELECT COUNT(uuid) FROM TRICKSTER_STATS WHERE uuid = '"+player.getUniqueId()+"';").executeQuery();
		playerCheck.next();
		if (playerCheck.getInt(1) == 0) { // player not in the system
			return false;
		} 
		return true;
	}
	
	public void deleteTricksterStats(Player player) throws SQLException {
		con.prepareStatement("DELETE FROM TRICKSTER_STATS WHERE uuid = '"+player.getUniqueId()+"';").execute();
	}
	
	// COLLECT TRICKSTER KILL STATS
	public int getTotalPlayerKills(Player player) throws SQLException {
		ResultSet rs = con.prepareStatement("SELECT total_player_kills FROM TRICKSTER_STATS WHERE uuid = '"+player.getUniqueId()+"';").executeQuery();
		rs.next();
		return rs.getInt("total_player_kills");
	}
	public int getTotalMobKills(Player player) throws SQLException {
		ResultSet rs = con.prepareStatement("SELECT total_mob_kills FROM TRICKSTER_STATS WHERE uuid = '"+player.getUniqueId()+"';").executeQuery();
		rs.next();
		return rs.getInt("total_mob_kills");
	}
	public int getRecentPlayerKills(Player player) throws SQLException {
		ResultSet rs = con.prepareStatement("SELECT recent_player_kills FROM TRICKSTER_STATS WHERE uuid = '"+player.getUniqueId()+"';").executeQuery();
		rs.next();
		return rs.getInt("recent_player_kills");
	}
	public int getRecentMobKills(Player player) throws SQLException {
		ResultSet rs = con.prepareStatement("SELECT recent_mob_kills FROM TRICKSTER_STATS WHERE uuid = '"+player.getUniqueId()+"';").executeQuery();
		rs.next();
		return rs.getInt("recent_mob_kills");
	}
	
	
	// ENABLE/DISABLE TRICKSTER KIT
	public boolean isKitEnabled (Player player) throws SQLException {
		ResultSet rs = con.prepareStatement("SELECT isEnabled FROM TRICKSTER_STATS WHERE uuid = '"+player.getUniqueId()+"';").executeQuery();
		rs.next();
		return rs.getBoolean("isEnabled");
	}
	public void toggleKitEnabled (Player player) throws SQLException {
		boolean enabled = isKitEnabled(player);
		con.prepareStatement("UPDATE TRICKSTER_STATS SET isEnabled="+!enabled+" WHERE uuid = '"+player.getUniqueId()+"';").executeUpdate();
	}
	
	
	// TOGGLE SHADOWSTEP ABILITY
	public boolean isShadowPlayer (Player player) throws SQLException {
		ResultSet rs = con.prepareStatement("SELECT isShadowPlayer FROM TRICKSTER_STATS WHERE uuid = '"+player.getUniqueId()+"';").executeQuery();
		rs.next();
		return rs.getBoolean("isShadowPlayer");
	}
	public void toggleShadowStep (Player player) throws SQLException {
		boolean enabled = isShadowPlayer(player);
		con.prepareStatement("UPDATE TRICKSTER_STATS SET isShadowPlayer="+!enabled+" WHERE uuid = '"+player.getUniqueId()+"';").executeUpdate();
	}
	
	
	// UPDATE TRICKSTER KILL STATS
	public void updateMobKills(Player player, int mobKills, int recentMobKills) throws SQLException {
		con.prepareStatement("UPDATE TRICKSTER_STATS SET total_mob_kills="+mobKills+", recent_mob_kills="+recentMobKills+" WHERE uuid = '"+player.getUniqueId()+"';").executeUpdate();
	}
	public void updatePlayerKills(Player player, int playerKills, int recentPlayerKills) throws SQLException {
		con.prepareStatement("UPDATE TRICKSTER_STATS SET total_player_kills="+playerKills+", recent_player_kills="+recentPlayerKills+" WHERE uuid = '"+player.getUniqueId()+"';").executeUpdate();	
	}
	public void resetRecentKills(Player player) throws SQLException {
		ResultSet rs = con.prepareStatement("SELECT COUNT(uuid) FROM TRICKSTER_STATS WHERE uuid = '"+player.getUniqueId()+"';").executeQuery();
		rs.next();
		if (rs.getInt(1) == 1) { // player not in the system
			player.sendMessage("recent reset");
			con.prepareStatement("UPDATE TRICKSTER_STATS SET recent_player_kills=DEFAULT, recent_mob_kills=DEFAULT WHERE uuid = '"+player.getUniqueId()+"';").executeUpdate();
		} 
	}
	
}
