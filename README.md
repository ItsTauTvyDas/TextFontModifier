# TextFontModifier

[![License](https://img.shields.io/badge/license-MIT-blue.svg)](/LICENSE)

![boss-bar example image](assets/bossbar.png)

This plugin replaces font for actionbar, scoreboard (title included) and boss-bar texts. There are no commands or anything, it starts to work when plugin gets enabled. This plugin is made only for **1.19.2**!

Made this quite a while ago for Drag Championship's (MCC recreation) project. Maybe someone might find this useful dunno.

#### Requires ProtocolLib!

## How it works

There's a packet listener and what it does is basically gets every text (in json) that is sent to the player and changes font attribute/property. This plugin might be resource-intensive, but there wasn't really a way for me to make it work in any other way on the event server I mentioned above.

(You can also notice that I use some reflection, when I saw of how much I will need to use it, I just gave up and used NMS lol.)

## Usage
Just drop this plugin to your */plugin/* folder, and you are good to go (also change the font's name in configuration).

### What's a special symbol?
The special symbol for scoreboards in configuration is a custom colour code that replaces text's font. If there's a color in its way, then it stops, it's like with `&u`, `&o` and other similar ones.