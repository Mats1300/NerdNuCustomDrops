# ✅ NerdNuCustomDrops Plugin - Major Testing Checklist

## 🔧 1. Plugin Initialization
- [X] Plugin enables without errors on server startup
- [X] `config.yml` is created/loaded correctly
- [X] Debug mode is respected (no debug logs unless enabled)

## ⚰️ 2. Mob Head Drops
- [X] Mobs drop heads with correct texture and display name
- [X] Drop chance reflects looting level and config-defined `drop-scale`
- [X] All mobs and their variants are supported
- [X] Heads have correct persistent lore (`[Certified Authentic]`)
- [X] Charged creepers drop correct player/mob heads
- [X] Heads never drop when killed by non-player sources (unless config allows)
- [X] Entity type not in config does not drop head // eg. Nitwit

## 💀 2. Mob Drops
- [X] Allay
- [X] Armadillo
- [X] Axolotl (Lucy)
- [X] Axolotl (Wild)
- [X] Axolotl (Gold)
- [X] Axolotl (Cyan)
- [X] Axolotl (Blue)
- [X] Bat
- [X] Bee (Calm)
- [X] Bee (Angry)
- [X] Blaze
- [X] Bogged
- [X] Breeze
- [X] Camel
- [X] Cat (Tabby)
- [X] Cat (Black)
- [X] Cat (Siamese)
- [X] Cat (Red)
- [X] Cat (British Shorthair)
- [X] Cat (Calico)
- [X] Cat (Persian)
- [X] Cat (Ragdoll)
- [X] Cat (White)
- [X] Cat (Jellie)
- [X] Cat (All Black)
- [X] Cave Spider
- [X] Chicken (Cold)
- [X] Chicken (Temperate)
- [X] Chicken (Warm)
- [X] Cod
- [X] Cow (Cold)
- [X] Cow (Temperate)
- [X] Cow (Warm)
- [X] Creaking
- [X] Creeper
- [X] Dolphin
- [X] Donkey
- [X] Drowned
- [X] Elder Guardian
- [X] Ender Dragon
- [X] Enderman
- [X] Endermite
- [X] Evoker
- [X] Fox (Red)
- [X] Fox (Snow)
- [X] Frog (Green)
- [X] Frog (Orange)
- [X] Frog (White)
- [X] Ghast (Idle)  Good but check name once edited after delete comment
- [X] Ghast (Shooting)
- [X] Glow Squid
- [X] Goat          Good but check name once edited after delete comment
- [X] Screaming Goat
- [X] Guardian
- [X] Happy Ghast
- [X] Hoglin
- [X] Horse (Creamy)
- [X] Horse (Chestnut)
- [X] Horse (Black)
- [X] Horse (Brown)
- [X] Horse (Dark_Brown)
- [X] Horse (Gray)
- [X] Horse (White)
- [X] Husk
- [X] Illusioner
- [X] Iron Golem
- [X] Llama (Brown)
- [X] Llama (Creamy)
- [X] Llama (Gray)
- [X] Llama (White)
- [X] Magma Cube
- [X] Mooshroom (Red)
- [X] Mooshroom (Brown)
- [X] Mule
- [X] Ocelot
- [X] Panda (Normal)
- [X] Panda (Lazy)
- [X] Panda (Worried)
- [X] Panda (Playful)
- [X] Panda (Brown)
- [X] Panda (Weak)
- [X] Parrot (Red)
- [X] Parrot (Blue)
- [X] Parrot (Green)
- [X] Parrot (Cyan)
- [X] Parrot (Gray)
- [X] Phantom
- [X] Pig (Warm)
- [X] Pig (Temperate)
- [X] Pig (Cold)
- [X] Piglin
- [X] Piglin Brute
- [X] Pillager
- [X] Polar Bear
- [X] Pufferfish
- [X] Rabbit (Brown)
- [X] Rabbit (White)
- [X] Rabbit (Black)
- [X] Rabbit (Black & White)
- [X] Rabbit (Gold)
- [X] Rabbit (Salt&Pepper)
- [X] Rabbit (The Killer Bunny)
- [X] Ravager
- [X] Salmon
- [X] Sheep (White)
- [X] Sheep (Black)
- [X] Sheep (Blue)
- [X] Sheep (Brown)
- [X] Sheep (Cyan)
- [X] Sheep (Gray)
- [X] Sheep (Green)
- [X] Sheep (Light Blue)
- [X] Sheep (Light Gray)
- [X] Sheep (Lime)
- [X] Sheep (Magenta)
- [X] Sheep (Orange)
- [X] Sheep (Pink)
- [X] Sheep (Purple)
- [X] Sheep (Red)
- [X] Sheep (Yellow)
- [X] Sheep (jeb_)
- [X] Shulker
- [X] Silverfish
- [X] Skeleton
- [X] Skeleton Horse
- [X] Slime
- [X] Sniffer
- [X] Snow Golem (normal)
- [X] Snow Golem (derp)
- [X] Spider
- [X] Squid
- [X] Stray
- [X] Strider 
- [X] Strider  (Cold)
- [X] Tadpole
- [X] Trader Llama (Brown)
- [X] Trader Llama (Creamy)
- [X] Trader Llama (Gray)
- [X] Trader Llama (White)
- [X] Tropical Fish
- [X] Turtle
- [X] Vex
- [X] Villager (various)
- [X] Vindicator
- [X] Wandering Trader
- [X] Warden
- [X] Witch
- [X] Wither
- [X] Wither Skeleton
- [X] Wolf (Variants)
- [X] Zoglin
- [X] Zombie
- [X] Zombie Horse
- [X] Zombie Villager (various)
- [X] Zombified Piglin

## 🧠 3. Head Metadata & NBT
  - Head retains NBT/lore data after:
  - [X] Being placed on a block
  - [X] Being broken and re-picked up
  - [X] No data loss or corruption on head reuse

## 🎵 4. Noteblock Sound System
- [X] Placing a head on a noteblock causes the correct mob sound to play
- [X] Sound does not play if head is removed
- [X] Different mob heads produce different sounds

## 📜 5. `/mobhead list` Command
- [X] Command shows all configured mob types from `config.yml`
- [X] Works without permission (no permission required)
- [X] Shows a clear and clean list
- [X] Works only in-game (not from console)

## 🔐 6. Permissions & Config
- [X] Only players can run `/mobhead list`
- [X] Plugin behavior is consistent across server reloads
- [X] Configurable drop scales function correctly

## 📁 7. Edge Cases
- [X] No head drops when mobs die from explosions (unless allowed)
- [X] Multiple mobs killed at once drop heads correctly
- [X] Mob heads work correctly across worlds/dimensions

## ✅ Final Validation
- [X] All features tested on Paper 1.21.8
- [X] Plugin works on a clean server with no other plugins
- [X] Plugin does not produce console errors or stack traces

Once out of Snapshot this file will be deleted