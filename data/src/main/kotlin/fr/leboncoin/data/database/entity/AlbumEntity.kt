package fr.leboncoin.data.database.entity

import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Cache local des photos : c'est la source de verite de l'application.
 * L'appel de l'api permet d'alimenter cette table.
 */
@Entity(
    tableName = "albums",
    indices = [Index(value = ["album_id"])],
)
data class AlbumEntity(
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: Int,
    @ColumnInfo(name = "album_id")
    val albumId: Int,
    @ColumnInfo(name = "title")
    val title: String,
    @ColumnInfo(name = "url")
    val url: String,
    @ColumnInfo(name = "thumbnail_url")
    val thumbnailUrl: String,
)

/**
 * Table dediee aux favoris.
 *
 * Choix : une table separee plutot qu'une colonne `is_favorite` dans `albums`.
 * La synchronisation reseau ecrase les lignes `albums` =>
 * isoler les favoris garantit qu'une donnee saisie par l'utilisateur ne soit jamais perdue par un refresh.
 */
@Entity(tableName = "favorites")
data class FavoriteEntity(
    @PrimaryKey
    @ColumnInfo(name = "photo_id")
    val photoId: Int,
    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis(),
)

/** Resultat de la jointure albums + favoris. */
data class PopulatedAlbum(
    @Embedded val album: AlbumEntity,
    @ColumnInfo(name = "is_favorite") val isFavorite: Boolean,
)
