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

## Features
- Multiple font support
- Disable certain packets
- Custom "color code" for font
- Only allow text to change font if regex matches

## So, how does it work?

There's a packet listener and what it does is basically gets every text (in json) that is sent to the player and changes font property. This plugin might be resource-intensive, but there wasn't really a way for me to make it work in any other way on the event server I mentioned above.

## Usage
Just drop this plugin to your `/plugins/` folder, and you are good to go (also change the font's name in configuration).

## Configuration

New version now has migration (pretty useless ngl) from old configuration (v1.0.0/v1.0.1) to a new one (v1.1.1).

### Default config.yml

```yaml
fonts:
  default-font:
    name: namespace:key
    special-symbol: $u
regex:
  value: '[\p{Print}&&[^~,],]+'
  invert: false
packets:
  boss-bar:
    enable: true
    forced-font: default-font
  action-bar:
    enable: true
    forced-font: default-font
  scoreboard-title:
    enable: true
    forced-font: default-font
  scoreboard-scores:
    enable: true
    forced-font: ''
config-version: 1
```

<details>
  <summary>v1.0.1/v1.0.0 old config.yml</summary>

  ```yaml
  font: minecraft:key
  regex: '[\p{Print}&&[^~,],]+'
  invert-regex: false
  packets:
    boss-bar: true
    action-bar: true
    scoreboard-title: true
    scoreboard-scores: true
  special-symbol-for-scoreboards: $u
  ```
</details>

### Forced fonts
Forced font means that it will use that specific font only and special symbol won't work. **Note** that you need to specify configuration key and not actual font!

### What's a special symbol? (The default is `$u`)
The special symbol is a custom "color code" that replaces text's font. The font is changed until it crosses paths with another color (`$usome &ctext`, only `some` will get its font changed).

***Be aware*** that this does not really work as a real color code, in Minecraft, message is split by colors, so text `Hello &aWorld` will get split into two parts and this plugin does not create seperate part if `$u` is inserted in a middle of the word (e.g. `Hel$ulo`). That means that the symbol can be anywhere in the text (or text part as I explained 🤓), it will change the whole text's font. Do not try to insert the symbol between colors (`&a$u&b`), it's not going to work.

<div align="center">
    <img src="https://count.getloli.com/get/@:itstautvydas-textfontmodifier?theme=gelbooru" alt="views counter"/>
</div>
