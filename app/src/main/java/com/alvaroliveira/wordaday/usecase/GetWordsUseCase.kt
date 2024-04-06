package com.alvaroliveira.wordaday.usecase

import com.alvaroliveira.wordaday.model.Word
import com.prof18.rssparser.RssParser
import org.jsoup.Jsoup

class GetWordsUseCase(
    private val rssParser: RssParser
) {
    suspend fun getWords(): List<Word> {
        val words: MutableList<Word> = mutableListOf()

        val feed = rssParser.getRssChannel("https://dicionario.priberam.org/DoDiaRSS.aspx")
        val documents = feed
            .items
            .map {
                it.description?.let {
                    Jsoup.parse(it)
                }
            }

        documents.forEach {
            val title = it?.selectFirst(".dp-definicao-header .varpt")?.text() ?: ""
            val definitions = it?.select(".dp-definicao-linha .p")?.map {
                it.text() ?: ""
            } ?: emptyList()
            words.add(
                Word(
                    name = title,
                    definitions = definitions
                )
            )
        }

        return words
    }
}