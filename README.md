# LANWhitelist

A few days ago, I stumbled accross [LANGuard](https://github.com/Ovilli/LANGuard),
a mod that achieves the same thing as this one and also contributed to it.
As this was my first ever contact with Java and Minecraft Mod Development,
I started to work on a new PR but soon noticed that I'm basically rewriting the entire mod.
So I thought, it would be better if I just start a fully own mod that uses a different approach to enforce the whitelist.
And here we are :)
<br>
<br>
Also a thanks to [Ovilli](https://github.com/Ovilli) for creating their mod that is the start of this one.

## Reason for this mod

More and more players are using mods like [e4mc](https://github.com/vgskye/e4mc-minecraft-architectury/) to make their LAN worlds publicly available.
But griefers have noticed this too and as LAN worlds provide no tooling to prevent anyone from joining, this starts to become a risk.
With this mod I tried to recreate the Minecraft Server whitelist system as closely as possible (with my non existant Java knowledge).

## Feature(s)

- a whitelist similar to the server counterpart
    - stores name and UUID
    - UUID is used to check if connecting player is on the whitelist
- config screen made with Cloth Config in modmenu for easy configuration
    - automatically resolves UUID for added playernames
        - because I'm to lazy/stupid to do this asynchronous saving can take longer the more new players are added in one go
    - option to disable the whitelist entirely
    - option to use names instead of UUIDs for checking (for testing)

## Technical

Other than [LANGuard](https://github.com/Ovilli/LANGuard), LANWhitelist doesn't even allow non whitelisted
players to join the server by modifying the connection process with a mixin.
So this and the usage of UUIDs differentiate this mod from [LANGuard](https://github.com/Ovilli/LANGuard)
and at least from my understanding of programming should make it more secure.

## License

[MIT](LICENSE)
