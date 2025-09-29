# NerdNuCustomDrops

A Minecraft plugin for [Paper](https://papermc.io/) 1.21.8 that adds custom mob head drops for all vanilla mobs and variants, with persistent metadata, textures, and integration with noteblocks.

## 💡 Features

- 🎯 **Mob-Specific Head Drops** – Configurable drops for all vanilla mobs, including variants.
- 🧠 **Lore & Metadata** – Dropped heads include `[Certified Authentic]` lore and retain it when placed/picked up.
- 🔊 **NoteBlock Integration** – When placed on a NoteBlock, heads play matching mob sounds.
- 🧪 **Drop Chance Tuning** – Drop chances scale with Looting level and per-entity configuration.
- ⚙️ **Fully Configurable** – Texture, lore, drop rates, and more via `config.yml`.
- 🧾 Commands & Debugging – /mobhead command with subcommands:
- /mobhead list – Displays all available mob heads.
- /mobhead give <mob> – Gives the specified mob head directly.
- 🛠️ Debugging Tools – Debug logging for developers. Enable it in config.yml under debug: true.

## 📦 Installation

1. Download the latest `.jar` from [Releases]([https://github.com/Mats1300/NerdNuCustomDrops/releases]).
2. Drop it into your server’s `plugins` folder.
3. Start your server to generate the config.
4. Customize `plugins/NerdNuCustomDrops/config.yml` as needed.

## ⚙️ Configuration

Example
```yaml
Armadillo:
    base-drop-chance: 0.01 # 0.01 = 1% Base drop chance 
    looting-bonus: 0.02 # 0.02 = Adds 2% per looting level 
    itemstack:
      type: PLAYER_HEAD # Do not Change
      display-name: "<italic><gold>Armadillo Head</gold></italic>" # Use Adventure that built into Paper
      lore: "<italic><light_purple>[Certified Authentic]</light_purple></italic>"
      head-sound: "minecraft:entity.armadillo.ambient" # Get sounds from the official Minecraft Wiki 
      internal: "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvOTg1MmIzM2JhMjk0ZjU2MDA5MDc1MmQxMTNmZTcyOGNiYzdkZDA0MjAyOWEzOGQ1MzgyZDY1YTIxNDYwNjhiNyJ9fX0="  
      url: http://textures.minecraft.net/texture/9852b33ba294f560090752d113fe728cbc7dd042029a38d5382d65a2146068b7 # Get Internal and Url from here https://minecraft-heads.com/
