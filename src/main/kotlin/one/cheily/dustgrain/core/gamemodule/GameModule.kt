package one.cheily.dustgrain.core.gamemodule

import kotlinx.coroutines.runBlocking
import one.cheily.dustgrain.core.Application
import one.cheily.dustgrain.core.fetching.DataFetchService

data class GameModule(
    val game: String,
    val moveTable: String?,
    val charTable: String?,
    val tables: List<String>
)

class GameModules(
    val fetchService: DataFetchService = Application.dataFetchService,
) {
    private val modules = mutableMapOf<String, GameModule>()

    fun registerModule(module: GameModule) {
        modules[module.game] = module
    }

    suspend fun initializeModule(game: String) {
        val tables = fetchService.getTableList()
        val gameTables = tables.filter { it.contains(game, ignoreCase = true) }
        val moveTable = gameTables.firstOrNull { it.contains("move", ignoreCase = true) }
        val charTable = gameTables.firstOrNull { it.contains("char", ignoreCase = true) }

        val module = GameModule(
            game = game,
            moveTable = moveTable,
            charTable = charTable,
            tables = gameTables
        )
        registerModule(module)
    }

    fun initializeModuleBlocking(game: String) = runBlocking { initializeModule(game) }

    fun getModule(game: String): GameModule? = modules[game]

    fun isRegistered(game: String): Boolean = modules.containsKey(game)


    operator fun get(game: String): GameModule? = getModule(game)
    operator fun set(game: String, module: GameModule) = registerModule(module)
    operator fun contains(game: String): Boolean = isRegistered(game)
}