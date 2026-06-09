package dustgrain.core.gamemodule

import dustgrain.core.ApiMockTest
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldNotBeEmpty
import io.kotest.matchers.shouldBe
import one.cheily.dustgrain.core.gamemodule.GameModules

class GameModulesMockTest : ApiMockTest({

    lateinit var gameModules: GameModules

    beforeTest {
        gameModules = GameModules(mockDataFetchService, mockDataHeaderCache, mockFormattingService)
        gameModules.clear()
        mockDataHeaderCache.clear()
    }

    feature("listCharacters") {
        scenario("should return a list of characters for a given game") {
            // given
            val game = "bbcf"
            val charTable = "bbcfCharacters"
            thereAreCargoTables(directory = "gamemodule")
            thereAreCargoFields(charTable, directory = "gamemodule")
            thereIsACargoQueryResult("character_list", directory = "gamemodule")

            // when
            val characters = gameModules.listCharactersBlocking(game)

            // then
            characters.shouldNotBeEmpty()
            characters.size shouldBe 5
            characters[0] shouldBe "Amane Nishiki"
            characters[4] shouldBe "Bullet"
        }

        scenario("should return an empty list if char table does not exist") {
            // given
            val game = "nonexistent"
            thereAreCargoTables()

            // when
            val characters = gameModules.listCharactersBlocking(game)

            // then
            characters.shouldBeEmpty()
        }
    }

    feature("getAllCharacterData") {
        scenario("should return all data for a character") {
            // given
            val game = "gbvsr"
            val moveTable = "gbvsrCharacters"
            val character = "Djeeta"
            thereAreCargoTables(directory = "gamemodule")
            thereAreCargoFields(moveTable, directory = "gamemodule")
            thereIsACargoQueryResult("character_data", directory = "gamemodule")
            thereIsImageData("1")

            // when
            val characterData = gameModules.getAllCharacterDataBlocking(game, character)

            // then
            characterData.shouldNotBeEmpty()
            characterData.size shouldBe 22
            val health = characterData.first { it.header.name == "health" }
            health.contents.first() shouldBe "16000"
            characterData.first { it.header.nameFormatted == "close l range" }.contents.first() shouldBe "102.5"
        }
    }

    feature("listMoves") {
        scenario("should return a list of moves for a character") {
            // given
            val game = "bbcf"
            val moveTable = "MoveData_BBCF"
            val character = "Noel Vermillion"
            thereAreCargoTables(directory = "gamemodule")
            thereAreCargoFields(moveTable, directory = "gamemodule")
            thereIsACargoQueryResult("move_list", directory = "gamemodule")

            // when
            val moves = gameModules.listMovesBlocking(game, character)

            // then
            moves.shouldNotBeEmpty()
            moves.size shouldBe 5
            val firstMoveName = moves.first().grains.first { it.header.name == "name" }.contents.first()
            firstMoveName shouldBe "1st Air Dash"
            val firstMoveInput = moves.first().grains.first { it.header.name == "input" }.contents.first()
            firstMoveInput shouldBe "j.66/44"
            val lastMoveName = moves.last().grains.first { it.header.name == "name" }.contents.first()
            lastMoveName shouldBe "214D Additional Attack"
        }
    }

    feature("getAllMoveDataByName") {
        scenario("should return move data by name") {
            // given
            val game = "gbvsr"
            val moveTable = "MoveData_GBVSR"
            val character = "Djeeta"
            val moveName = "M Reginleiv"
            thereAreCargoTables(directory = "gamemodule")
            thereAreCargoFields(moveTable, directory = "gamemodule")
            thereIsACargoQueryResult("move_data", directory = "gamemodule")

            // when
            val moveData = gameModules.getAllMoveDataByNameBlocking(game, character, moveName)

            // then
            moveData.shouldNotBeEmpty()
            moveData.size shouldBe 1
            val move = moveData.first()
            move.grains.first { it.header.name == "name" }.contents.first() shouldBe "M Reginleiv"
            move.grains.first { it.header.name == "damage" }.contents.first() shouldBe "800"
            move.grains.first { it.header.name == "recovery" }.contents.first() shouldBe "Total 47"
        }
    }

    feature("getAllMoveDataByInput") {
        scenario("should return move data by input") {
            // given
            val game = "gbvsr"
            val moveTable = "MoveData_GBVSR"
            val character = "Djeeta"
            val input = "236M"
            thereAreCargoTables(directory = "gamemodule")
            thereAreCargoFields(moveTable, directory = "gamemodule")
            thereIsACargoQueryResult("move_data", directory = "gamemodule")

            // when
            val moveData = gameModules.getAllMoveDataByInputBlocking(game, character, input)

            // then
            moveData.shouldNotBeEmpty()
            moveData.size shouldBe 1
            val move = moveData.first()
            move.grains.first { it.header.name == "name" }.contents.first() shouldBe "M Reginleiv"
            move.grains.first { it.header.name == "damage" }.contents.first() shouldBe "800"
            move.grains.first { it.header.name == "recovery" }.contents.first() shouldBe "Total 47"
        }
    }

    feature("getAllMoveDataByCustomQuery") {
        scenario("should return move data by custom query") {
            // given
            val game = "gbvsr"
            val moveTable = "MoveData_GBVSR"
            val where = "name = 'M Reginleiv'"
            thereAreCargoTables(directory = "gamemodule")
            thereAreCargoFields(moveTable, directory = "gamemodule")
            thereIsACargoQueryResult("move_data", directory = "gamemodule")

            // when
            val moveData = gameModules.getAllMoveDataByCustomQueryBlocking(game, where)

            // then
            moveData.shouldNotBeEmpty()
            moveData.size shouldBe 1
            val move = moveData.first()
            move.grains.first { it.header.name == "name" }.contents.first() shouldBe "M Reginleiv"
            move.grains.first { it.header.name == "damage" }.contents.first() shouldBe "800"
            move.grains.first { it.header.name == "recovery" }.contents.first() shouldBe "Total 47"
        }
    }

    feature("listMovesByCustomQuery") {
        scenario("should return a list of moves by custom query") {
            // given
            val game = "bbcf"
            val moveTable = "MoveData_BBCF"
            val where = "character = 'Noel Vermillion'"
            thereAreCargoTables(directory = "gamemodule")
            thereAreCargoFields(moveTable, directory = "gamemodule")
            thereIsACargoQueryResult("move_list", directory = "gamemodule")

            // when
            val moves = gameModules.listMovesByCustomQueryBlocking(game, where)

            // then
            moves.shouldNotBeEmpty()
            moves.size shouldBe 5
            val firstMoveName = moves.first().grains.first { it.header.name == "name" }.contents.first()
            firstMoveName shouldBe "1st Air Dash"
            val firstMoveInput = moves.first().grains.first { it.header.name == "input" }.contents.first()
            firstMoveInput shouldBe "j.66/44"
            val lastMoveName = moves.last().grains.first { it.header.name == "name" }.contents.first()
            lastMoveName shouldBe "214D Additional Attack"
        }
    }

    feature("listCharactersByCustomQuery") {
        scenario("should return a list of characters by custom query") {
            // given
            val game = "bbcf"
            val charTable = "bbcfCharacters"
            val where = "name LIKE '%A%'"
            thereAreCargoTables(directory = "gamemodule")
            thereAreCargoFields(charTable, directory = "gamemodule")
            thereIsACargoQueryResult("character_list", directory = "gamemodule")

            // when
            val characters = gameModules.listCharactersByCustomQueryBlocking(game, where)

            // then
            characters.shouldNotBeEmpty()
            characters.size shouldBe 5
            characters[0] shouldBe "Amane Nishiki"
            characters[4] shouldBe "Bullet"
        }
    }

    feature("getAllCharacterDataByCustomQuery") {
        scenario("should return all data for a character by custom query") {
            // given
            val game = "gbvsr"
            val moveTable = "gbvsrCharacters"
            val where = "name = 'Djeeta'"
            thereAreCargoTables(directory = "gamemodule")
            thereAreCargoFields(moveTable, directory = "gamemodule")
            thereIsACargoQueryResult("character_data", directory = "gamemodule")
            thereIsImageData("1")

            // when
            val characterData = gameModules.getAllCharacterDataByCustomQueryBlocking(game, where)

            // then
            characterData.shouldNotBeEmpty()
            characterData.size shouldBe 22
            val health = characterData.first { it.header.name == "health" }
            health.contents.first() shouldBe "16000"
            characterData.first { it.header.nameFormatted == "close l range" }.contents.first() shouldBe "102.5"
        }
    }
})
