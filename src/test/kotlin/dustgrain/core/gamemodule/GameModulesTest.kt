package dustgrain.core.gamemodule

import io.kotest.core.spec.style.FeatureSpec
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.mockk
import one.cheily.dustgrain.core.fetching.DataFetchService
import one.cheily.dustgrain.core.gamemodule.GameModule
import one.cheily.dustgrain.core.gamemodule.GameModules

class GameModulesTest : FeatureSpec({

    lateinit var fetchService: DataFetchService
    lateinit var gameModules: GameModules

    val game = "GGST"
    val ggstTables = listOf(
        "Patches_GGST",
        "ggstCharacters",
        "MoveData_GGST",
    )
    val allTables = ggstTables.plus(listOf(
        "Glossary",
        "NewsItems",
        "lcbEgo"
    ))

    beforeEach {
        fetchService = mockk()
        gameModules = GameModules(fetchService)
    }

    feature("GameModules registration") {
        scenario("should register a module") {
            // given
            val module = GameModule("GGST", null, null, emptyList())

            // when
            gameModules.registerModule(module)

            // then
            gameModules.isRegistered("GGST").shouldBeTrue()
            gameModules.getModule("GGST") shouldBe module
        }

        scenario("should check if a module is registered") {
            // given
            val module = GameModule("GGST", null, null, emptyList())
            gameModules.registerModule(module)

            // then
            gameModules.isRegistered("GGST").shouldBeTrue()
            gameModules.isRegistered("BBCF").shouldBeFalse()
        }

        scenario("should use operators for registration and access") {
            // given
            val module = GameModule("GGST", null, null, emptyList())

            // when
            gameModules["GGST"] = module

            // then
            ("GGST" in gameModules).shouldBeTrue()
            gameModules["GGST"] shouldBe module
        }
    }

    feature("GameModules initialization") {
        scenario("should initialize a module from fetch service") {
            // given
            // game, ggstTables, allTables
            coEvery { fetchService.getTableList() } returns allTables

            // when
            gameModules.initializeModule(game)

            // then
            val module = gameModules.getModule(game)
            module.shouldNotBeNull()
            module.game shouldBe game
            module.moveTable shouldBe "MoveData_GGST"
            module.charTable shouldBe "ggstCharacters"
            module.tables shouldContainExactlyInAnyOrder ggstTables
        }

        scenario("should initialize a module from fetch service (blocking)") {
            // given
            // game, ggstTables, allTables
            coEvery { fetchService.getTableList() } returns allTables

            // when
            gameModules.initializeModuleBlocking(game)

            // then
            val module = gameModules.getModule(game)
            module.shouldNotBeNull()
            module.game shouldBe game
            module.moveTable shouldBe "MoveData_GGST"
            module.charTable shouldBe "ggstCharacters"
            module.tables shouldContainExactlyInAnyOrder ggstTables
        }

        scenario("should handle initialization when no tables are found") {
            // given
            val game = "SF6"
            coEvery { fetchService.getTableList() } returns emptyList()

            // when
            gameModules.initializeModule(game)

            // then
            val module = gameModules.getModule(game)
            module.shouldNotBeNull()
            module.game shouldBe game
            module.moveTable shouldBe null
            module.charTable shouldBe null
            module.tables.isEmpty().shouldBeTrue()
        }
    }
})

