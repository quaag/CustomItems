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

## Resource pack

`resourcepack/CustomLegendaries` provides the CMD 1 / CMD 2 item models with placeholder textures. See the pack notes for the worn-appearance limitation.

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
