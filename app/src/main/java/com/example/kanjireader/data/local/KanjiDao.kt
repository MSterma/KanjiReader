package com.example.kanjireader.data.local
// data/local/KanjiDao.kt
import androidx.room.Dao
import androidx.room.Query

@Dao
interface KanjiDao {
    @Query("SELECT * FROM kanji WHERE character = :searchedChar LIMIT 1")
    suspend fun geKanji(searchedChar: String): KanjiEntity?
}