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

## Crown

A golden helmet variant identified by a PersistentDataContainer key (`custom_item_id = crown`).

- Unbreakable with enchant glint
- CustomModelData 1 for resource pack support
- While worn in the helmet slot, grants +10 hearts, Resistance I and Regeneration I
- Bonuses are tracked through a named attribute modifier and removed cleanly when unequipped

## Commands

- `/customitems version`
- `/customitems give crown [player]`
- `/customitems reload`
- Alias: `/ci`

## Permissions

- `customitems.admin`
- `customitems.give`
- `customitems.reload`
