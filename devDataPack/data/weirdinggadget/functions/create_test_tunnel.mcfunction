#Build the tunnel
forceload add ~ ~ ~208 ~

fill ~1 ~-1 ~-1 ~176 ~10 ~10 minecraft:stone outline
fill ~177 ~-1 ~-1 ~192 ~10 ~10 minecraft:granite outline
fill ~193 ~-1 ~-1 ~200 ~10 ~10 minecraft:obsidian outline
fill ~ ~ ~ ~199 ~9 ~9 minecraft:air replace
fill ~1 ~ ~1 ~199 ~ ~1 minecraft:torch replace
fill ~1 ~ ~8 ~199 ~ ~8 minecraft:torch replace

#Redstone run to the chunk active distance
fill ~4 ~ ~ ~199 ~ ~ minecraft:redstone_wire

#Repeaters to carry the signal
setblock ~16 ~ ~ minecraft:repeater[facing=west,delay=1]
setblock ~32 ~ ~ minecraft:repeater[facing=west,delay=1]
setblock ~48 ~ ~ minecraft:repeater[facing=west,delay=1]
setblock ~64 ~ ~ minecraft:repeater[facing=west,delay=1]
setblock ~80 ~ ~ minecraft:repeater[facing=west,delay=1]
setblock ~96 ~ ~ minecraft:repeater[facing=west,delay=1]
setblock ~112 ~ ~ minecraft:repeater[facing=west,delay=1]
setblock ~128 ~ ~ minecraft:repeater[facing=west,delay=1]
setblock ~144 ~ ~ minecraft:repeater[facing=west,delay=1]
setblock ~160 ~ ~ minecraft:repeater[facing=west,delay=1]
setblock ~176 ~ ~ minecraft:repeater[facing=west,delay=1]
setblock ~192 ~ ~ minecraft:repeater[facing=west,delay=1]

#Device to show redstone activity
fill ~190 ~ ~ ~190 ~ ~3 minecraft:redstone_wire
setblock ~190 ~ ~1 minecraft:repeater[facing=north,delay=1] replace
setblock ~190 ~ ~4 minecraft:piston[facing=south] replace

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

#Redstone clock starter
setblock ~7 ~ ~6 minecraft:redstone_wire
setblock ~8 ~ ~6 minecraft:repeater[facing=east,delay=1]
setblock ~8 ~ ~5 minecraft:repeater[facing=north,delay=1]
setblock ~9 ~ ~5 minecraft:repeater[facing=north,delay=1]
setblock ~9 ~ ~6 minecraft:redstone_wire
setblock ~8 ~ ~4 minecraft:redstone_wire
setblock ~9 ~ ~3 minecraft:stone
setblock ~9 ~ ~4 minecraft:redstone_wall_torch[facing=south]
setblock ~10 ~ ~3 minecraft:redstone_wire[east=side,west=side,north=none,south=none]
setblock ~11 ~ ~3 minecraft:redstone_torch

forceload remove ~ ~ ~208 ~
