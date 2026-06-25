# CustomItems

Server-side Paper plugin adding custom items, starting with the Crown.

## Requirements

- Minecraft 1.21.11
- Paper 1.21.11
- Java 21

## Build

```
.\gradlew.bat clean build
```

The jar is produced at `build/libs/CustomItems-1.0.0.jar`.

## Items

### Crown

A diamond helmet variant identified by a PersistentDataContainer key (`custom_item_id = crown`).

- Diamond helmet defensive stats (armor 3, toughness 2) by base material
- Unbreakable with enchant glint, CustomModelData 1
- While worn in the helmet slot, grants +10 hearts, Resistance I and Regeneration I
- Health bonus tracked through a named attribute modifier and removed cleanly when unequipped

### Mask

A leather helmet variant identified by `custom_item_id = mask`, CustomModelData 2, unbreakable, glint off by default.

- While worn it changes chat name, tab name and masks the killer name in death messages to "Masked Player"
- The overhead nameplate cannot be rewritten to arbitrary text with vanilla Paper/Bukkit; the team option only hides it. True replacement needs ProtocolLib/NMS.

### Worn rendering

Worn appearance is driven by the item `equippable` component (`asset_id`), not CustomModelData. Both items set their equippable model to `customitems:crown` / `customitems:mask`, which the resource pack resolves to humanoid equipment layers. CustomModelData still drives the inventory/hand icon.

### Masked tab skin

`mask.change-tab-skin` spoofs the player profile texture via the Paper `PlayerProfile` API (no ProtocolLib required) and refreshes viewers with a brief hide/show. It activates only when `mask.skin.value` is set. Mojang clients only render skins hosted on their CDN, so the local PNG must first be uploaded to https://mineskin.org to obtain `value` (and optional `signature`); paste those into `config.yml`. The NameMC source skin is for private server use; licensing of third-party skins is not guaranteed for redistribution.

## Resource pack

`resourcepack/CustomLegendaries` is the single combined pack: CMD 1/2 inventory models plus `equipment/` worn layers, with original CC0 placeholder textures.

## Commands

- `/customitems version`
- `/customitems give crown [player]`
- `/customitems give mask [player]`
- `/customitems reload`
- Alias: `/ci`

## Permissions

- `customitems.admin`
- `customitems.give`
- `customitems.reload`
