package com.example.kanjireader.data.local
// data/local/KanjiDao.kt
import androidx.room.Dao
import androidx.room.Query

@Dao
interface KanjiDao {
    @Query("SELECT * FROM kanji WHERE character = :searchedChar LIMIT 1")
    suspend fun geKanji(searchedChar: String): KanjiEntity?
    // data/local/KanjiDao.kt
    @Query("""
    SELECT character FROM kanji 
    WHERE character = :query 
    OR onyomi LIKE '%' || :query || '%' 
    OR kunyomi LIKE '%' || :query || '%'
""")
    suspend fun searchMatchingCharacters(query: String): List<String>
}