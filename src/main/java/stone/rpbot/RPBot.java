/**
 * This file is part of RPBot2. 
 * Copyright (c) 2022, Stone, All rights reserved.
 * 
 * RPBot2 is free software: you can redistribute it and/or
 * modify it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * RPBot2 is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with RPBot2. If not, see <https://www.gnu.org/licenses/>.
 */
package stone.rpbot;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tomlj.Toml;
import org.tomlj.TomlInvalidTypeException;
import org.tomlj.TomlParseResult;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.MemberCachePolicy;

/**
 * 
 */
public class RPBot {

	public final JDA DISCORD;
	private final Logger log;

	public RPBot() throws URISyntaxException, IOException {
		this(Paths.get("config.toml"));
	}

	public RPBot(Path con) throws URISyntaxException, IOException {
		this.log = LoggerFactory.getLogger(RPBot.class);
		if (Files.notExists(con))
		{
			log.info("No Config detected, creating default config");
			Path defaultConfig = Paths.get(this.getClass().getResource("default.toml").toURI());
			Files.copy(defaultConfig, con);
		}
		Config config = new Config(con);

		// sets up the gateway intents to tell discord which events the bot wants to
		// receive
		List<GatewayIntent> intents = new ArrayList<>();
		intents.add(GatewayIntent.GUILD_BANS);
		intents.add(GatewayIntent.GUILD_MEMBERS);
		intents.add(GatewayIntent.GUILD_MESSAGES);
		intents.add(GatewayIntent.MESSAGE_CONTENT);

		JDABuilder builder = JDABuilder.create(config.getApiKey(), intents);
		builder.setMemberCachePolicy(MemberCachePolicy.ALL); // helps with doing things related to members

		DISCORD = builder.build();
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

	public class Config {

		private final String API_KEY;
		private final Connection DATABASE_CONNECTION;

		private static final String API_KEY_KEY = "api_key";
		private static final String DATABASE_CONNECTION_KEY = "database_name";

		public Config(Path config) throws IOException {
			TomlParseResult tomlConfig = Toml.parse(config);
			if (!tomlConfig.contains(API_KEY_KEY))
			{
				log.error("The config must have the \"api_key\" field filled in order for the bot to start up!");
				throw new Error(); // TODO: make custom errors
			}

			try
			{
				API_KEY = tomlConfig.getString(API_KEY_KEY);
			} catch (TomlInvalidTypeException e)
			{
				log.error("\"" + API_KEY_KEY + "\" must be a string to be used as the api key to discord");
				throw new Error(); // TODO: make custom errors
			}
			try
			{
				String database = "jdbc:derby:" + tomlConfig.getString(DATABASE_CONNECTION_KEY);
				try
				{
					DATABASE_CONNECTION = DriverManager.getConnection(database);

				} catch (SQLException e)
				{
					log.error("Database connection failed to be established!", e);
					throw new Error(); // TODO: make custom errors
				}
			} catch (TomlInvalidTypeException e)
			{
				log.error("\"" + DATABASE_CONNECTION_KEY + "\" must be a string to be used as the api key to discord");
				throw new Error(); // TODO: make custom errors
			}

		}

		private String getApiKey() {
			return API_KEY;
		}

		private Connection getDatabaseConnection() {
			return DATABASE_CONNECTION;
		}
	}

}
