package one.cheily.dustgrain.core.formatting

import org.jsoup.Jsoup
import org.jsoup.parser.Parser

class WikitextSanitizer {
    private companion object {
        val RGX_EMPHASIS = Regex("''+")
        val RGX_WIKI_LINK = Regex("\\[\\[([^|\\]]*\\|)?([^\\]]+)\\]\\]")
        val RGX_OPEN_BRACKET_SPACE = Regex("\\[\\s+")
        val RGX_SPACE_CLOSE_BRACKET = Regex("\\s+\\]")
        val RGX_WHITESPACE = Regex("\\s+")
    }

    fun toPlainText(raw: String): String {
        val decoded = Parser.unescapeEntities(raw, false)
        val withoutHtml = Jsoup.parse(decoded).text()
        return withoutHtml
            .replace(RGX_EMPHASIS, "")
            .replace(RGX_WIKI_LINK, "$2")
            .replace(RGX_OPEN_BRACKET_SPACE, "[")
            .replace(RGX_SPACE_CLOSE_BRACKET, "]")
            .replace(RGX_WHITESPACE, " ")
            .trim()
    }
}
