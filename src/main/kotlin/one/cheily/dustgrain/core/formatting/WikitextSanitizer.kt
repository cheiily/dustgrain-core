package one.cheily.dustgrain.core.formatting

import org.jsoup.Jsoup
import org.jsoup.parser.Parser

class WikitextSanitizer {
    fun toPlainText(raw: String): String {
        val decoded = Parser.unescapeEntities(raw, false)
        val withoutHtml = Jsoup.parse(decoded).text()
        return withoutHtml
            .replace(Regex("''+"), "")
            .replace(Regex("\\[\\[([^|\\]]*\\|)?([^\\]]+)\\]\\]"), "$2")
            .replace(Regex("\\[\\s+"), "[")
            .replace(Regex("\\s+\\]"), "]")
            .replace(Regex("\\s+"), " ")
            .trim()
    }
}
