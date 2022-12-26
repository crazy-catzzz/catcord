# catcord
WIP custom Discord Client written in Java.

## Why?
I don't like the constant Nitro ads on the official client and I want to try making one myself.

## The state of the project
It's in a very early state, there is no GUI, the wrapper around the Discord API isn't complete either, in other words, there's still a lot of work to do.

## Credits
- [ripcord-api](https://github.com/saucecode/ripcord-api) by saucecode, it gave me a general idea of how the Discord API works as well as an example implementation.
- [arikawa](https://github.com/diamondburned/arikawa) by diamondburned (who also created a [custom client](https://github.com/diamondburned/gtkcord4)), it also gave me an idea on how I could implement certain features.

## Other resources used
- [Discord API documentation](https://discord.com/developers/docs) to research the [Discord Gateway API](https://discord.com/developers/docs/topics/gateway).
- [Oracle Java networking API documentation](https://docs.oracle.com/en/java/javase/17/docs/api/java.net.http/java/net/http/), specifically for the [WebSocket](https://docs.oracle.com/en/java/javase/17/docs/api/java.net.http/java/net/http/WebSocket.html) and [Listener](https://docs.oracle.com/en/java/javase/17/docs/api/java.net.http/java/net/http/WebSocket.Listener.html) classes.


## TODO
- Probably a complete rewrite.