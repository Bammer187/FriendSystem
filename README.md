# Minecraft Friendsystem

## Table of Contents
- [Disclaimer](#disclaimer)
- [Description](#description)
- [Installation](#installation)
- [License](#license)

## Disclaimer

There are likely better alternatives out there — this project was primarily created as a way for me to learn Java and get familiar with the Minecraft API. If you still wish to use this system, you're absolutely free to do so.

## Description
This plugin, designed for Minecraft **1.21.X** with MySQL integration, includes familiar friend system commands inspired by those from popular servers:
    
    /friend add <player>      - Send a friend request  
    /friend remove <player>   - Remove a player from your friends list  
    /friend accept <player>   - Accept a friend request  
    /friend deny <player>     - Deny a friend request  
    /friend list              - View your list of friends  
    /message <player>         - Send a message to a player  
    /respond                  - Respond to the last message

Additionally, a simple GUI for the friend list is available by right-clicking a **player head** in-game.

## Installation

1. Download  
Download the latest version of the plugin from the Releases tab.  
*(Alternatively: compile it yourself using Maven or Gradle if you've made changes to the source code.)*

2. Place in Plugins Folder  
Put the downloaded **.jar** file into your server’s **plugins/** directory.

3. Create config.yml  
Create a folder **Friendsystem** in your plugins/ folder and create a file named config.yml in there.
In this config.yml, you should write the connection identifiers to your database:

```
mysql:
  host: "host"
  port: 3306
  database: "db_name"
  username: "your_username"
  password: "your_password"

```

4. Start the server  
Start (or restart) the server to load the plugin. You can also use */reload* if needed.

## License

This project is licensed under the [MIT License](LICENSE).
However, please note that usage of this plugin is also subject to the [Minecraft End User License Agreement (EULA)](https://www.minecraft.net/eula).
You are responsible for ensuring that your use complies with both the MIT License and Mojang's EULA.
