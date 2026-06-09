package dustgrain.core.formatting

import dustgrain.core.ApiMockTest
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.equals.shouldBeEqual
import io.kotest.matchers.nulls.shouldNotBeNull
import one.cheily.dustgrain.core.domain.DataField
import one.cheily.dustgrain.core.domain.DataHeader
import one.cheily.dustgrain.core.domain.DataStruct
import one.cheily.dustgrain.core.formatting.FormatterRef

class FormattingServiceMockTest : ApiMockTest({
    fun FormatterRef.toSomeDataHeader(delimiter: String? = null) = DataHeader(
        name = "someDataField",
        type = this,
        delimiter = delimiter
    )

    val someSingleDataField = DataField(
        content = "someContent",
        header = FormatterRef.PASS.toSomeDataHeader()
    )

    val someListDataField = DataField(
        content = "someContent1;someContent2;someContent3",
        header = FormatterRef.PASS.toSomeDataHeader(delimiter = ";")
    )

    val someDataStruct = DataStruct(
        structureName = "someDataStruct",
        fields = listOf(
            someSingleDataField,
            someListDataField
        )
    )

    val allDataHeaders = FormatterRef.entries.map(FormatterRef::toSomeDataHeader)

    feature("FormattingService#matchFormat") {
        scenario("matches reference to formatter") {
            FormatterRef.IMAGE.toSomeDataHeader().let {
                mockFormattingService.matchFormat(it) shouldBeEqual mockFormattingService.formatImage
            }
            FormatterRef.PASS.toSomeDataHeader().let {
                mockFormattingService.matchFormat(it) shouldBeEqual mockFormattingService.formatPass
            }
            FormatterRef.PASS_ERROR.toSomeDataHeader().let {
                mockFormattingService.matchFormat(it) shouldBeEqual mockFormattingService.formatErrorPass
            }
            FormatterRef.WIKITEXT.toSomeDataHeader().let {
                mockFormattingService.matchFormat(it) shouldBeEqual mockFormattingService.formatWikitext
            }
        }
    }

    feature("FormattingService#formatPass") {
        scenario("parses single item content") {
            // given
            val data = someSingleDataField

            // when
            val result = mockFormattingService.formatPass.format(data)

            // then
            result.header.name shouldBeEqual someSingleDataField.header.name
            result.contents shouldBeEqual listOf(someSingleDataField.content)
        }

        scenario("parses list content") {
            // given
            val data = someListDataField

            // when
            val result = mockFormattingService.formatPass.format(data)

            // then
            result.header.name shouldBeEqual someListDataField.header.name
            result.contents shouldBeEqual listOf("someContent1", "someContent2", "someContent3")
        }
    }

    feature("FormattingService#formatErrorPass") {
        scenario("parses single item content") {
            // given
            val data = someSingleDataField.copy(header = FormatterRef.PASS_ERROR.toSomeDataHeader())

            // when
            val result = mockFormattingService.formatErrorPass.format(data)

            // then
            result.header.name shouldBeEqual someSingleDataField.header.name
            result.contents shouldBeEqual listOf(someSingleDataField.content)
        }

        scenario("parses list content as single item") {
            // given
            val data = someListDataField.copy(header = FormatterRef.PASS_ERROR.toSomeDataHeader(delimiter = ";"))

            // when
            val result = mockFormattingService.formatErrorPass.format(data)

            // then
            result.header.name shouldBeEqual someListDataField.header.name
            result.contents shouldBeEqual listOf(someListDataField.content)
        }
    }

    feature("FormattingService#formatImage") {
        scenario("parses single item content") {
            // given
            thereIsImageData("1")
            val data = someSingleDataField.copy(header = FormatterRef.IMAGE.toSomeDataHeader())

            // when
            val result = mockFormattingService.formatImage.format(data)

            // then
            result.header.name shouldBeEqual someSingleDataField.header.name
            result.contents shouldBeEqual listOf("https://www.dustloop.com/wiki/images/e/e8/BBCF_Noel_Vermillion_d623D.png")
        }

        scenario("parses list content") {
            // given
            thereIsImageData("1")
            val data = someListDataField.copy(header = FormatterRef.IMAGE.toSomeDataHeader(delimiter = ";"))

            // when
            val result = mockFormattingService.formatImage.format(data)

            // then
            result.header.name shouldBeEqual someListDataField.header.name
            result.contents shouldBeEqual listOf(
                "https://www.dustloop.com/wiki/images/e/e8/BBCF_Noel_Vermillion_d623D.png",
                "https://www.dustloop.com/wiki/images/e/e8/BBCF_Noel_Vermillion_d623D.png",
                "https://www.dustloop.com/wiki/images/e/e8/BBCF_Noel_Vermillion_d623D.png"
            )
        }
    }

    feature("FormattingService#formatWikitext") {
        scenario("parses content as plain text") {
            // given
            val data = someSingleDataField.copy(header = FormatterRef.WIKITEXT.toSomeDataHeader())

            // when
            val result = mockFormattingService.formatWikitext.format(data)

            // then
            result.header.name shouldBeEqual data.header.name
            result.contents shouldBeEqual listOf(data.content)
        }
    }

    feature("FormattingService#format for DataField") {
        scenario("formats data according to header type") {
            // given
            thereIsImageData("1")
            val dataPass = someSingleDataField.copy(header = FormatterRef.PASS.toSomeDataHeader())
            val dataError = someSingleDataField.copy(header = FormatterRef.PASS_ERROR.toSomeDataHeader())
            val dataImage = someSingleDataField.copy(header = FormatterRef.IMAGE.toSomeDataHeader())
            // todo #14
//            val dataWikitext = someSingleDataField.copy(header = FormatterRef.WIKITEXT.toSomeDataHeader())

            // when
            val resultPass = mockFormattingService.format(dataPass)
            val resultError = mockFormattingService.format(dataError)
            val resultImage = mockFormattingService.format(dataImage)
//            val resultWikitext = mockFormattingService.format(dataWikitext)


            // then
            resultPass.contents shouldBeEqual listOf(someSingleDataField.content)
            resultError.contents shouldBeEqual listOf(someSingleDataField.content)
            resultImage.contents shouldBeEqual listOf("https://www.dustloop.com/wiki/images/e/e8/BBCF_Noel_Vermillion_d623D.png")
//            resultWikitext.contents shouldBeEqual listOf(someSingleDataField.content)
        }
    }

    feature("FormattingService#format for DataStruct") {
        scenario("formats all fields in the structure") {
            // given
            val data = someDataStruct

            // when
            val result = mockFormattingService.format(data)

            // then
            result.structureName.shouldNotBeNull()
            result.structureName shouldBeEqual "someDataStruct"
            result.grains.size shouldBeEqual 2
            result.grains[0].contents shouldBeEqual listOf(someSingleDataField.content)
            result.grains[1].contents shouldBeEqual listOf("someContent1", "someContent2", "someContent3")
        }
    }
})