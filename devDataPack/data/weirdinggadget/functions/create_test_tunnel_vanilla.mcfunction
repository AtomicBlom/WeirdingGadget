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

fill ~4 ~ ~ ~4 ~ ~3 minecraft:redstone_wire
setblock ~4 ~ ~1 minecraft:repeater[facing=south,delay=1] replace

#Redstone run to the chunk active distance
fill ~4 ~ ~ ~183 ~ ~ minecraft:redstone_wire

setblock ~16 ~ ~ minecraft:repeater[facing=west,delay=1] replace
setblock ~32 ~ ~ minecraft:repeater[facing=west,delay=1] replace
setblock ~48 ~ ~ minecraft:repeater[facing=west,delay=1] replace
setblock ~64 ~ ~ minecraft:repeater[facing=west,delay=1] replace
setblock ~80 ~ ~ minecraft:repeater[facing=west,delay=1] replace
setblock ~96 ~ ~ minecraft:repeater[facing=west,delay=1] replace
setblock ~112 ~ ~ minecraft:repeater[facing=west,delay=1] replace
setblock ~128 ~ ~ minecraft:repeater[facing=west,delay=1] replace
setblock ~144 ~ ~ minecraft:repeater[facing=west,delay=1] replace
setblock ~160 ~ ~ minecraft:repeater[facing=west,delay=1] replace
setblock ~176 ~ ~ minecraft:repeater[facing=west,delay=1] replace

#Device to show redstone activity
fill ~174 ~ ~ ~174 ~ ~3 minecraft:redstone_wire
setblock ~174 ~ ~1 minecraft:repeater[facing=north,delay=1] replace
setblock ~174 ~ ~4 minecraft:piston[facing=south] replace