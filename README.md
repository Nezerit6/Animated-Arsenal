# Animated Arsenal

> Based on
> [Simple Animated Guns](https://github.com/elidhan/Simple-Animated-Guns) by **Elidhan**
> 1.3 (Fabric 1.20.1) for **NeoForge 1.21.1**. The gun models, animations, textures and sounds are Elidhan's work.
> Please report bugs here, not to the original author.

## Requirements

* Minecraft 1.21.1, NeoForge 21.1.x
* [GeckoLib](https://modrinth.com/mod/geckolib) 4.9.3 or newer for NeoForge 1.21.1
* Optional: [Crawl](https://github.com/fewizz/crawl) (prone shooting)

## What this port changes

* Rewritten for NeoForge 1.21.1 and GeckoLib 4 (the original 1.3 uses AzureLib): data components, NeoForge
  networking, events instead of most mixins.
* Guns can't be put in the offhand.
* Reload and bash sounds are heard by nearby players.
* Dynamic lighting from muzzle flashes (client side).
* Bullet holes on blocks.
* Third-person recoil kick and stock bash animations.
* Crawl compatibility: prone aiming/shooting with movement blocked, prone reload, long guns slung on the back.

## Configuration

* `config/animated_arsenal-client.toml`: dynamic lights, bullet holes (and how long they stay), screen recoil.
* `<world>/serverconfig/animated_arsenal-server.toml` (synced to players): global multipliers for damage,
  recoil, spread, fire delay and reload time, bullet speed and lifetime, infinite ammo, glass breaking, impact
  sounds, bash cooldown, offhand guns, and a section per gun with damage, fire delay, magazine size, reload
  time, pellets, spread, recoil, firing mode and an on/off switch.

## Building

```bash
./gradlew build
```

The jar is written to `build/libs/`.

## Credits and license

* Original mod, models, animations, textures and sounds: **Elidhan** —
  [Simple Animated Guns](https://github.com/elidhan/Simple-Animated-Guns).
* Animated Arsenal: **Nezerit6**.

The original mod is licensed under [CC BY 4.0](https://creativecommons.org/licenses/by/4.0/); Animated Arsenal is an adaptation released under the same license. Elidhan does not endorse this mod. See [LICENSE](LICENSE).