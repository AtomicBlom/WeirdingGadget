# Testing Weirding Gadget
Before you do any testing, 
1. run the appropriate gradle task to make your run configurations, `genIntellijRuns` for IntelliJ
2. Duplicate the runClient configuration
   1. add `--username GuinneaPig57` to the duplicate's application arguments


## Testing in Single Player
### Does it chunk load?
In order to use the test functions,
1. Run a client
2. Start a single player world in creative mode
3. Create a world
4. inside the world's save location, there will be a directory `datapacks`
5. Copy the devDataPack directory into that location
6. run `/reload` to reload the datapacks on the server
7. run `/function weirdinggadget:create_test_tunnel`

## Testing Multiplayer
Before you can test multiplayer, you'll need to configure the server
1. Run an instance of the server
2. In `eula.txt` change `eula` to `true`
3. In `server.properties`, change `online-mode` to `false`
4. run `/gamerule doDaylightCycle false`
5. run `/gamerule doWeatherCycle false`
6. run `/gamerule doMobSpawning false`
7. run `/gamerule doMobLoot false`
8. run `/gamerule doEntityDrops false`
9. run `/time set day`
10. run `/weather clear`

11. copy the `devDataPack` data pack to `.\run\world\datapacks`

fixme: autoload the rules?

### Does it chunk load?
In order to use the test functions,
1. Run an instance of the server
2. Run an instance of the client
3. Connect to the server
4. run `/op Dev` on the server
5. Inside the server world's save location, there will be a directory `datapacks`
6. Copy the devDataPack directory into that location
7. run `/reload` to reload the datapacks on the server
8. run `/function weirdinggadget:create_test_tunnel`