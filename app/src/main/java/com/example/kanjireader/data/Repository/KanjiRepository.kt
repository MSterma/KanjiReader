package com.example.kanjireader.data.Repository

import com.example.kanjireader.data.Model.KanjiInfo
import com.example.kanjireader.data.local.KanjiDao

class KanjiRepository (private  val dao: KanjiDao) {
    suspend fun fetchKanjiData(character: Char): KanjiInfo? {
        val entity = dao.geKanji(character.toString()) ?: return null;
        return KanjiInfo(character=entity.character.first(), meaning = entity.meaning, kunyomi = entity.kunyomi,entity.onyomi)
    }

    fun getKanji(tekst: String): List<Char> {
        return tekst.filter { it in '\u4E00'..'\u9FAF' }.toSet().toList()
    }
}