package one.cheily.dustgrain.core.gamemodule

import kotlinx.coroutines.runBlocking
import one.cheily.dustgrain.core.Application
import one.cheily.dustgrain.core.cache.DataHeaderCache
import one.cheily.dustgrain.core.domain.DataField
import one.cheily.dustgrain.core.domain.DataGrain
import one.cheily.dustgrain.core.domain.DataHeader
import one.cheily.dustgrain.core.domain.DataSpike
import one.cheily.dustgrain.core.domain.DataStruct
import one.cheily.dustgrain.core.fetching.DataFetchService
import one.cheily.dustgrain.core.fetching.TableDataRequest
import one.cheily.dustgrain.core.formatting.FormattingService
import kotlin.collections.listOf

data class GameModule(
    val game: String,
    val moveTable: String?,
    val charTable: String?,
    val tables: List<String>
)

class GameModules(
    val fetchService: DataFetchService = Application.dataFetchService,
    val dataHeaderCache: DataHeaderCache = Application.dataHeaderCache,
    val formattingService: FormattingService = Application.formattingService
) {
    private val modules = mutableMapOf<String, GameModule>()
    val registered: Map<String, GameModule> get() = modules.toMap()

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

    suspend fun getOrLoadModule(game: String) = getModule(game) ?: initializeModule(game).let { getModule(game) }

    fun getOrLoadModuleBlocking(game: String) = runBlocking { getOrLoadModule(game) }

    fun isRegistered(game: String): Boolean = modules.containsKey(game)

    fun clear() = modules.clear()

    operator fun get(game: String): GameModule? = getOrLoadModuleBlocking(game)
    operator fun set(game: String, module: GameModule) = registerModule(module)
    operator fun contains(game: String): Boolean = isRegistered(game)


    // -------------- common fetch api --------------
    suspend fun listCharacters(game: String): List<String> = getOrLoadModule(game)?.charTable?.let { charTable ->
        val nameCol = "name"
        val nameHeader = dataHeaderCache.getOrLoad(charTable)?.firstOrNull { it.name == nameCol }
        if (nameHeader == null)
            return@let null

        fetchService.getTableData(
            TableDataRequest(
                tables = listOf(charTable),
                fields = listOf("name")
            )
        ).mapNotNull { struct ->
            struct["name"]
        }.map {
            DataField(it, nameHeader)
        }.let {
            formattingService.format(it)
        }.mapNotNull { it.contents.firstOrNull() }
    } ?: emptyList()

    fun listCharactersBlocking(game: String): List<String> = runBlocking { listCharacters(game) }


    suspend fun getAllCharacterData(game: String, character: String): List<DataSpike> =
        getOrLoadModule(game)?.charTable?.let { charTable ->
            val headers = dataHeaderCache.getOrLoad(charTable)
            if (headers == null || headers.isEmpty())
                return@let null
            val nameHeader = findHeader(charTable, "name")
                ?.name
                ?: return@let null

            fetchService.getTableData(
                TableDataRequest(
                    tables = listOf(charTable),
                    fields = headers.map { it.name },
                    where = "$nameHeader = \"$character\""
                )
            ).map { entries ->
                DataStruct(
                    structureName = "charData",
                    fields = entries
                        .filter { it.value != null }
                        .mapNotNull { (key, value) ->
                            headers.firstOrNull { header -> header.nameInResponse == key }
                                ?.let {
                                    DataField(value!!, it)
                                }
                        }
                )
            }.map {
                formattingService.format(it)
            }
        } ?: emptyList()

    fun getAllCharacterDataBlocking(game: String, character: String): List<DataSpike> = runBlocking { getAllCharacterData(game, character) }


    suspend fun listMoves(game: String, character: String): List<DataSpike> =
        getOrLoadModule(game)?.moveTable?.let { moveTable ->
            val headers = dataHeaderCache.getOrLoad(moveTable)
            if (headers == null || headers.isEmpty())
                return@let null
            val charHeader = findHeader(moveTable, "character", "char")
                ?.name
                ?: return@let null
            val nameHeader = findHeader(moveTable, "name")
            val inputHeader = findHeader(moveTable, "input")
            val typeHeader = findHeader(moveTable, "type")
            val fieldHeaders = listOfNotNull(nameHeader, inputHeader, typeHeader)
            val fields = fieldHeaders.map { it.name }

            if (fields.isEmpty()) return@let null

            fetchService.getTableData(TableDataRequest(
                tables = listOf(moveTable),
                fields = fields,
                where = "$charHeader = \"$character\""
            )).map { entries ->
                DataStruct(
                    structureName = "moveData",
                    fields = entries.mapNotNull { entry ->
                        fieldHeaders.firstOrNull { it.nameInResponse == entry.key }
                            ?.let { DataField(entry.value!!, it) }
                    }
                )
            }.map {
                formattingService.format(it)
            }
        } ?: emptyList()

    fun listMovesBlocking(game: String, character: String): List<DataSpike> = runBlocking { listMoves(game, character) }


    suspend fun getAllMoveDataByName(game: String, character: String, move: String): List<DataSpike> =
        getAllMoveDataByHeader(game, character, "name", move)

    fun getAllMoveDataByNameBlocking(game: String, character: String, move: String): List<DataSpike> = runBlocking { getAllMoveDataByName(game, character, move) }

    suspend fun getAllMoveDataByInput(game: String, character: String, input: String): List<DataSpike> =
        getAllMoveDataByHeader(game, character, "input", input)

    fun getAllMoveDataByInputBlocking(game: String, character: String, input: String): List<DataSpike> = runBlocking { getAllMoveDataByInput(game, character, input) }

    private suspend fun getAllMoveDataByHeader(game: String, character: String, headerEq: String, value: String, headerCont: String? = null): List<DataSpike> =
        getOrLoadModule(game)?.moveTable?.let { moveTable ->
            val headers = dataHeaderCache.getOrLoad(moveTable)
            if (headers == null || headers.isEmpty())
                return@let null
            val charHeader = findHeader(moveTable, "character", "char")
                ?.name
                ?: return@let null
            val customHeader = findHeader(moveTable, headerEq, headerCont)
                ?.name
                ?: return@let null

            fetchService.getTableData(TableDataRequest(
                tables = listOf(moveTable),
                fields = headers.map { it.name },
                where = "$charHeader = \"$character\" AND $customHeader = \"$value\""
            )).map { entries ->
                DataStruct(
                    structureName = "moveData",
                    fields = entries.mapNotNull { entry ->
                        headers.firstOrNull { it.nameInResponse == entry.key }
                            ?.let { DataField(entry.value!!, it) }
                    }
                )
            }.map {
                formattingService.format(it)
            }
        } ?: emptyList()


    suspend fun getAllMoveDataByCustomQuery(game: String, where: String): List<DataSpike> =
        getOrLoadModule(game)?.moveTable?.let { moveTable ->
            val headers = dataHeaderCache.getOrLoad(moveTable)
            if (headers == null || headers.isEmpty())
                return@let null

            fetchService.getTableData(TableDataRequest(
                tables = listOf(moveTable),
                fields = headers.map { it.name },
                where = where
            )).map { entries ->
                DataStruct(
                    structureName = "moveData",
                    fields = entries.mapNotNull { entry ->
                        headers.firstOrNull { it.nameInResponse == entry.key }
                            ?.let { DataField(entry.value!!, it) }
                    }
                )
            }.map {
                formattingService.format(it)
            }
        } ?: emptyList()

    fun getAllMoveDataByCustomQueryBlocking(game: String, where: String): List<DataSpike> = runBlocking { getAllMoveDataByCustomQuery(game, where) }


    suspend fun listMovesByCustomQuery(game: String, where: String): List<DataSpike> =
        getOrLoadModule(game)?.moveTable?.let { moveTable ->
            val headers = dataHeaderCache.getOrLoad(moveTable)
            if (headers == null || headers.isEmpty())
                return@let null
            val nameHeader = findHeader(moveTable, "name")
            val inputHeader = findHeader(moveTable, "input")
            val typeHeader = findHeader(moveTable, "type")
            val fieldHeaders = listOfNotNull(nameHeader, inputHeader, typeHeader)
            val fields = fieldHeaders.map { it.name }

            if (fields.isEmpty()) return@let null

            fetchService.getTableData(TableDataRequest(
                tables = listOf(moveTable),
                fields = fields,
                where = where
            )).map { entries ->
                DataStruct(
                    structureName = "moveData",
                    fields = entries.mapNotNull { entry ->
                        fieldHeaders.firstOrNull { it.nameInResponse == entry.key }
                            ?.let { DataField(entry.value!!, it) }
                    }
                )
            }.map {
                formattingService.format(it)
            }
        } ?: emptyList()

    fun listMovesByCustomQueryBlocking(game: String, where: String): List<DataSpike> = runBlocking { listMovesByCustomQuery(game, where) }


    suspend fun listCharactersByCustomQuery(game: String, where: String): List<String> = getOrLoadModule(game)?.charTable?.let { charTable ->
        val nameCol = "name"
        val nameHeader = dataHeaderCache.getOrLoad(charTable)?.firstOrNull { it.name == nameCol }
        if (nameHeader == null)
            return@let null

        fetchService.getTableData(
            TableDataRequest(
                tables = listOf(charTable),
                fields = listOf("name"),
                where = where
            )
        ).mapNotNull { struct ->
            struct["name"]
        }.map {
            DataField(it, nameHeader)
        }.let {
            formattingService.format(it)
        }.mapNotNull { it.contents.firstOrNull() }
    } ?: emptyList()

    fun listCharactersByCustomQueryBlocking(game: String, where: String): List<String> = runBlocking { listCharactersByCustomQuery(game, where) }


    suspend fun getAllCharacterDataByCustomQuery(game: String, where: String): List<DataSpike> =
        getOrLoadModule(game)?.charTable?.let { charTable ->
            val headers = dataHeaderCache.getOrLoad(charTable)
            if (headers == null || headers.isEmpty())
                return@let null

            fetchService.getTableData(
                TableDataRequest(
                    tables = listOf(charTable),
                    fields = headers.map { it.name },
                    where = where
                )
            ).map { entries ->
                DataStruct(
                    structureName = "charData",
                    fields = entries
                        .filter { it.value != null }
                        .mapNotNull { (key, value) ->
                            headers.firstOrNull { header -> header.nameInResponse == key }
                                ?.let {
                                    DataField(value!!, it)
                                }
                        }
                )
            }.map {
                formattingService.format(it)
            }
        } ?: emptyList()

    fun getAllCharacterDataByCustomQueryBlocking(game: String, where: String): List<DataSpike> = runBlocking { getAllCharacterDataByCustomQuery(game, where) }


    suspend fun listCharactersForMoveTable(game: String): List<String> = getOrLoadModule(game)?.moveTable?.let { moveTable ->
        val charHeader = findHeader(moveTable, "character", "char")
            ?: return@let null

        fetchService.getTableData(
            TableDataRequest(
                tables = listOf(moveTable),
                fields = listOf(charHeader.name),
                groupBy = charHeader.name
            )
        ).mapNotNull { struct ->
            struct[charHeader.nameInResponse]
        }.map {
            DataField(it, charHeader)
        }.let {
            formattingService.format(it)
        }.mapNotNull { it.contents.firstOrNull() }
    } ?: emptyList()

    fun listCharactersForMoveTableBlocking(game: String): List<String> = runBlocking { listCharactersForMoveTable(game) }


    private suspend fun findHeader(table: String, eq: String, cont: String? = null): DataHeader? =
        dataHeaderCache.getOrLoad(table)?.let { headers ->
            headers.firstOrNull { it.name == eq }
                ?: if (cont != null) headers.firstOrNull{ it.name.contains(cont) } else null
        }

}
