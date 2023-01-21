# Greev.eu AdvancedBan Fork

Yes, this is supposed to be open source, feel free to add pull request and/or use it yourself if you find it useful.

_Base code from Leoko, extended by the greev.eu developers_ 

## Description
AdvancedBan is an All-In-One Punishment-System with warns, tempwarns, mutes, tempmutes, bans, tempbans, ipbans and kicks.
There is also a PlayerHistory, so you can see the players past punishments and 
the plugin has configurable Time & Message-Layouts which automatically calculate and increase the Punishment-Time for certain reasons.
AdvancedBan also provides a full Message-File, so you can change and translate all messages & a detailed config-file with plenty of useful settings.
This is a BungeeCord Plugin and it requires MySQL.

## API
To use the API, you need to add AdvancedBan to your project and declare it as a dependency in the plugin.yml.

Add AdvancedBan to your project by adding the AdvancedBan.jar to your build-path or as a:
#### Maven dependency in your pom.xml
Due to our rewrite, the API from AdvancedBan and our fork are incompatible. You will have to host it on your own maven instance to add it to your plugins.
