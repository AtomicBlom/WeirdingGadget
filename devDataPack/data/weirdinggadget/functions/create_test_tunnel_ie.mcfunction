#Build the tunnel
forceload add ~ ~ ~192 ~

fill ~1 ~-1 ~-1 ~176 ~10 ~10 minecraft:stone outline
fill ~177 ~-1 ~-1 ~184 ~10 ~10 minecraft:obsidian outline
fill ~ ~ ~ ~183 ~9 ~9 minecraft:air replace
fill ~1 ~ ~1 ~183 ~ ~1 minecraft:torch replace
fill ~1 ~ ~8 ~183 ~ ~8 minecraft:torch replace

forceload remove ~ ~ ~192 ~

#Redstone Clock
setblock ~4 ~ ~4 minecraft:redstone_wire
setblock ~6 ~ ~4 minecraft:redstone_wire
setblock ~6 ~ ~6 minecraft:redstone_wire
setblock ~4 ~ ~6 minecraft:redstone_wire

setblock ~5 ~ ~4 minecraft:repeater[facing=west,delay=3] replace
setblock ~6 ~ ~5 minecraft:repeater[facing=north,delay=3] replace
setblock ~5 ~ ~6 minecraft:repeater[facing=east,delay=3] replace
setblock ~4 ~ ~5 minecraft:repeater[facing=south,delay=3] replace

setblock ~4 ~ ~3 immersiveengineering:connector_redstone[facing=down] replace

#Redstone run to the chunk active distance
setblock ~4 ~3 ~ immersiveengineering:steel_wallmount[facing=north,orientation=side_up] replace
setblock ~4 ~4 ~ immersiveengineering:connector_redstone[facing=down] replace

setblock ~15 ~3 ~ immersiveengineering:steel_wallmount[facing=north,orientation=side_up] replace
setblock ~15 ~4 ~ immersiveengineering:connector_redstone[facing=down] replace
setblock ~18 ~3 ~ immersiveengineering:steel_wallmount[facing=north,orientation=side_up] replace
setblock ~18 ~4 ~ immersiveengineering:connector_redstone[facing=down] replace

setblock ~47 ~3 ~ immersiveengineering:steel_wallmount[facing=north,orientation=side_up] replace
setblock ~47 ~4 ~ immersiveengineering:connector_redstone[facing=down] replace
setblock ~50 ~3 ~ immersiveengineering:steel_wallmount[facing=north,orientation=side_up] replace
setblock ~50 ~4 ~ immersiveengineering:connector_redstone[facing=down] replace

setblock ~79 ~3 ~ immersiveengineering:steel_wallmount[facing=north,orientation=side_up] replace
setblock ~79 ~4 ~ immersiveengineering:connector_redstone[facing=down] replace
setblock ~82 ~3 ~ immersiveengineering:steel_wallmount[facing=north,orientation=side_up] replace
setblock ~82 ~4 ~ immersiveengineering:connector_redstone[facing=down] replace

setblock ~111 ~3 ~ immersiveengineering:steel_wallmount[facing=north,orientation=side_up] replace
setblock ~111 ~4 ~ immersiveengineering:connector_redstone[facing=down] replace
setblock ~114 ~3 ~ immersiveengineering:steel_wallmount[facing=north,orientation=side_up] replace
setblock ~114 ~4 ~ immersiveengineering:connector_redstone[facing=down] replace

setblock ~143 ~3 ~ immersiveengineering:steel_wallmount[facing=north,orientation=side_up] replace
setblock ~143 ~4 ~ immersiveengineering:connector_redstone[facing=down] replace
setblock ~146 ~3 ~ immersiveengineering:steel_wallmount[facing=north,orientation=side_up] replace
setblock ~146 ~4 ~ immersiveengineering:connector_redstone[facing=down] replace

setblock ~175 ~3 ~ immersiveengineering:steel_wallmount[facing=north,orientation=side_up] replace
setblock ~175 ~4 ~ immersiveengineering:connector_redstone[facing=down] replace

#Device to show redstone activity
setblock ~175 ~ ~4 minecraft:piston[facing=south] replace
setblock ~175 ~1 ~4 immersiveengineering:connector_redstone[facing=down] replace
