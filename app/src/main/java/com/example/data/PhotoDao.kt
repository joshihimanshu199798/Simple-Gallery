package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface PhotoDao {
    @Query("SELECT * FROM photos WHERE isTrashed = 0 AND isVaultLocked = 0 ORDER BY dateTaken DESC")
    fun observeVisiblePhotos(): Flow<List<PhotoEntity>>

    @Query("SELECT * FROM photos WHERE isTrashed = 0 AND isVaultLocked = 0 AND isFavorite = 1 ORDER BY dateTaken DESC")
    fun observeFavoritePhotos(): Flow<List<PhotoEntity>>

    @Query("SELECT * FROM photos WHERE isTrashed = 0 AND isVaultLocked = 1 ORDER BY dateTaken DESC")
    fun observeVaultPhotos(): Flow<List<PhotoEntity>>

    @Query("SELECT * FROM photos WHERE isTrashed = 1 ORDER BY trashedAt DESC, dateTaken DESC")
    fun observeTrashedPhotos(): Flow<List<PhotoEntity>>

    @Query("SELECT * FROM custom_albums ORDER BY createdAt ASC")
    fun observeCustomAlbums(): Flow<List<CustomAlbumEntity>>

    @Query("SELECT * FROM little_memories ORDER BY isPinnedHighlight DESC, memoryDate DESC")
    fun observeAllMemories(): Flow<List<MemoryEntryEntity>>

    @Query("SELECT * FROM memory_milestones ORDER BY isCompleted ASC, targetDate ASC")
    fun observeAllMilestones(): Flow<List<MilestoneReminderEntity>>

    @Query("SELECT COUNT(*) FROM photos")
    suspend fun getTotalPhotoCount(): Int

    @Query("SELECT COUNT(*) FROM little_memories")
    suspend fun getTotalMemoriesCount(): Int

    @Query("SELECT * FROM photos WHERE id = :id LIMIT 1")
    suspend fun getPhotoById(id: Long): PhotoEntity?

    @Query("SELECT * FROM photos WHERE id IN (:ids)")
    suspend fun getPhotosByIds(ids: List<Long>): List<PhotoEntity>

    @Query("SELECT * FROM photos WHERE isTrashed = 1")
    suspend fun getTrashedPhotosOnce(): List<PhotoEntity>

    @Query("SELECT * FROM photos WHERE isTrashed = 0 AND isVaultLocked = 0 ORDER BY dateTaken DESC")
    suspend fun getVisiblePhotosOnce(): List<PhotoEntity>

    @Query("SELECT * FROM photos")
    suspend fun getAllPhotosOnce(): List<PhotoEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPhoto(photo: PhotoEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPhotos(photos: List<PhotoEntity>)

    @Update
    suspend fun updatePhoto(photo: PhotoEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCustomAlbum(album: CustomAlbumEntity)

    @Query("DELETE FROM custom_albums WHERE name = :albumName")
    suspend fun deleteCustomAlbum(albumName: String)

    @Query("UPDATE photos SET isFavorite = :isFavorite WHERE id = :id")
    suspend fun setFavorite(id: Long, isFavorite: Boolean)

    @Query("UPDATE photos SET isFavorite = :isFavorite WHERE id IN (:ids)")
    suspend fun setFavoriteBatch(ids: List<Long>, isFavorite: Boolean)

    @Query("UPDATE photos SET albumName = :albumName WHERE id IN (:ids)")
    suspend fun movePhotosToAlbum(ids: List<Long>, albumName: String)

    @Query("UPDATE photos SET isVaultLocked = :locked WHERE id IN (:ids)")
    suspend fun setVaultLockedBatch(ids: List<Long>, locked: Boolean)

    @Query("UPDATE photos SET isTrashed = 1, trashedAt = :timestamp WHERE id IN (:ids)")
    suspend fun moveToTrashBatch(ids: List<Long>, timestamp: Long = System.currentTimeMillis())

    @Query("UPDATE photos SET isTrashed = 0, trashedAt = NULL WHERE id IN (:ids)")
    suspend fun restoreFromTrashBatch(ids: List<Long>)

    @Query("DELETE FROM photos WHERE id IN (:ids)")
    suspend fun deletePhotosByIds(ids: List<Long>)

    @Query("SELECT * FROM photos WHERE filePath LIKE '%IMG_2026_%'")
    suspend fun getDefaultSamplePhotos(): List<PhotoEntity>

    @Query("DELETE FROM photos WHERE filePath LIKE '%IMG_2026_%'")
    suspend fun deleteDefaultSamplePhotos(): Int

    // Little Memories DAO operations
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMemory(memory: MemoryEntryEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMemories(memories: List<MemoryEntryEntity>)

    @Update
    suspend fun updateMemory(memory: MemoryEntryEntity)

    @Query("DELETE FROM little_memories WHERE id = :id")
    suspend fun deleteMemoryById(id: Long)

    @Query("UPDATE little_memories SET isCapsuleOpened = 1 WHERE id = :id")
    suspend fun unlockTimeCapsule(id: Long)

    @Query("UPDATE little_memories SET isPinnedHighlight = :pinned WHERE id = :id")
    suspend fun setMemoryPinned(id: Long, pinned: Boolean)

    // Milestones & Growth Tracker DAO operations
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMilestone(milestone: MilestoneReminderEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMilestones(milestones: List<MilestoneReminderEntity>)

    @Update
    suspend fun updateMilestone(milestone: MilestoneReminderEntity)

    @Query("UPDATE memory_milestones SET isCompleted = :completed WHERE id = :id")
    suspend fun setMilestoneCompleted(id: Long, completed: Boolean)

    @Query("DELETE FROM memory_milestones WHERE id = :id")
    suspend fun deleteMilestoneById(id: Long)

    @Query("UPDATE photos SET milestoneSticker = :sticker WHERE id = :photoId")
    suspend fun updateMilestoneSticker(photoId: Long, sticker: String)

    // People & Growth Timeline DAO operations
    @Query("SELECT * FROM person_clusters WHERE isHidden = 0 AND mergedIntoPersonId IS NULL ORDER BY hasGrowthTimeline DESC, isPet ASC, name DESC, id ASC")
    fun observeVisiblePersons(): Flow<List<PersonClusterEntity>>

    @Query("SELECT * FROM person_clusters")
    suspend fun getAllPersonsOnce(): List<PersonClusterEntity>

    @Query("SELECT * FROM person_clusters WHERE id = :personId LIMIT 1")
    suspend fun getPersonById(personId: Long): PersonClusterEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPerson(person: PersonClusterEntity): Long

    @Update
    suspend fun updatePerson(person: PersonClusterEntity)

    @Query("UPDATE person_clusters SET name = :name WHERE id = :personId")
    suspend fun renamePerson(personId: Long, name: String)

    @Query("UPDATE person_clusters SET isHidden = :hidden WHERE id = :personId")
    suspend fun setPersonHidden(personId: Long, hidden: Boolean)

    @Query("UPDATE person_clusters SET hasGrowthTimeline = 1, childDisplayName = :childName, childDobMs = :dobMs, name = CASE WHEN name = '' THEN :childName ELSE name END WHERE id = :personId")
    suspend fun setupGrowthTimeline(personId: Long, childName: String, dobMs: Long)

    @Query("UPDATE person_clusters SET mergedIntoPersonId = :targetPersonId WHERE id = :sourcePersonId")
    suspend fun markPersonMerged(sourcePersonId: Long, targetPersonId: Long)

    @Query("DELETE FROM person_clusters")
    suspend fun deleteAllPersons()

    @Query("SELECT * FROM photo_face_tags")
    fun observeAllFaceTags(): Flow<List<PhotoFaceTagEntity>>

    @Query("SELECT * FROM photo_face_tags WHERE photoId = :photoId")
    suspend fun getFaceTagsForPhoto(photoId: Long): List<PhotoFaceTagEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFaceTag(tag: PhotoFaceTagEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFaceTags(tags: List<PhotoFaceTagEntity>)

    @Query("UPDATE photo_face_tags SET personId = :targetPersonId WHERE personId = :sourcePersonId")
    suspend fun reassignFaceTags(sourcePersonId: Long, targetPersonId: Long)

    @Query("DELETE FROM photo_face_tags WHERE photoId IN (:photoIds)")
    suspend fun deleteFaceTagsForPhotos(photoIds: List<Long>)

    @Query("DELETE FROM photo_face_tags")
    suspend fun deleteAllFaceTags()
}
