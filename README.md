# SetHomes
A simple homes plugin that I made for fun, and to get back into Java/Minecraft!

# Installation
Simply place the downloaded jar from https://dev.bukkit.org/projects/set-homes into your server plugins folder.

# Commands
- **/sethome [HomeName] [HomeDescription]** - This command will allow the issuer to set a home with a given name and description of their choosing at your standing location. If no name and description are given then you will set a default home at your standing location.
- **/home [HomeName]** - This command will teleport the user to the given "HomeName". If no home name is given then the user is teleported to their default set by **/sethome**
- **/delhome [HomeName]** - This command will delete the home at the given home name. If no home name is given then it will attempt to remove the default home.
- **/homes** - This command will list all of the players currently set homes if there are any.
- **/blacklist [Add/Remove] [WorldName]** - This command is used to manipulate the world blacklist. If a worlds name is in the blacklist then no players will be allowed to set homes in that world. Type /blacklist to list all worlds in the blacklist
- **/strike** - Have fun admins

# Permissions
- **homes.*** - A player given this permission will be allowed all commands under the Set Homes plugin
- **homes.home** - A player with this permission is allowed to teleport to named homes
- **homes.sethome** - A player with this permission is allowed to set named homes
- **homes.strike** - Give the power to others!
- **homes.blacklist_list** - Give the ability to list worlds currently in the blacklist
- **homes.blacklist_add** - Give the ability to add worlds to the blacklist
- **homes.blacklist_remove** - Give the ability to remove worlds from the blacklist
