package com.example.spotifysync.schema;

import java.util.List;

/**
 * Schema class for a music board
 *
 * Music boards are boards to aggregate playlists, texts, pictures, and videos
 */
public class MusicBoard {
    private static class BoardItem {
        enum ItemType {
            SPOTIFY_SONG,
            SPOTIFY_PLAYLIST,
            YOUTUBE_VIDEO,
            YOUTUBE_PLAYLIST,
            TEXT,
            IMAGE
        }

        long itemId;
        ItemType itemType;
        String itemUrl;
        String itemText;
    }

    long userId;

    long boardId;
    String boardTitle;
    String boardDescription;
    List<BoardItem> boardItems;
}
