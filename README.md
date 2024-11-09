# LazyDirector

LazyDirector is a Minecraft Paper plugin that introduce a special spectator mode to automatically observe player activities.

This plugin is designed to free spectators' hands from controlling themselves flying all around by automatically managing the spectating target and camera view. It can be used to spectate players in minigames, to live stream your server's holiday events, or just to record daily lives in your server.

**NOTICE: Still in EARLY DEVELOPMENT.**

## Getting Started

LazyDirector is not automatically activated when the server starts. To activate it, run command

`` /lazydirector activate <config_name>``

<font color=grey>*If your server have multiverse plugins installed and the "world" dimension is not enabled, you'll have to check the config file first and make sure the location in ``hotspotManager.defaultHotspot`` is valid.*</font>

After activation, you need to attach a player (probably yourself for now) to LazyDirector's camera:

`` /lazydirector output attach <player_name> <camera_name>``

Now this player will act as the output of LazyDirector's camera. His position and view will be controlled by LazyDirector, spectating other players in server.

To detach a player from LazyDirector's camera, run

`` /lazydirector output detach <player_name>``

To deactivate LazyDirector, run

`` /lazydirector shutdown``

## How LazyDirector Works


## Configuration


## Commands


## Integrating With Your Server

