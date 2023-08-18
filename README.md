<h1 align="center">TextFontModifier</h1>

<div align="center">

[![License](https://img.shields.io/badge/license-MIT-blue.svg)](/LICENSE.md)

</div>

<div align="center">
    <img src="assets/bossbar.png" alt="bossbar example"/>
</div>
<br>

This plugin replaces font for actionbar, scoreboard (title included) and boss-bar texts. There are no commands or anything, it starts to work when plugin gets enabled. This plugin was initially made for 1.19.2 but it should work 1.19+

Made this quite a while ago for Drag Championship (MCC recreation). Maybe someone might find this useful, dunno.

***Requires PaperMC and ProtocolLib!***

## So, how does it work?

There's a packet listener and what it does is basically gets every text (in json) that is sent to the player and changes font property. This plugin might be resource-intensive, but there wasn't really a way for me to make it work in any other way on the event server I mentioned above.

## Usage
Just drop this plugin to your `/plugins/` folder, and you are good to go (also change the font's name in configuration).

### What's a special symbol? (The default is `$u`)
The special symbol for scoreboards (the owner only asked this for scoreboards) in configuration is a custom "color code" that replaces text's font. The font is changed until it crosses paths with another color (`$usome &ctext`, only `some` will get its font changed).

***Be aware*** that this does not really work as a real color code, in Minecraft, message is split by colors, so text `Hello &aWorld` will get split into two parts and this plugin does not create seperate part if `$u` is inserted in a middle of the word (e.g. `Hel$ulo`). That means that the symbol can be anywhere in the text (or text part as I explained 🤓), it will change the whole text's font. Do not try to insert the symbol between colors (`&a$u&b`), it's not going to work.

<div align="center">
    <img src="https://count.getloli.com/get/@:itstautvydas-textfontmodifier?theme=gelbooru" alt="views counter"/>
</div>
