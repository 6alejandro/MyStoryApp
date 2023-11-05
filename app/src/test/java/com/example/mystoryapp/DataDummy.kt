package com.example.mystoryapp

import com.example.mystoryapp.data.response.ListStoryItem
import kotlin.text.Typography.quote

object DataDummy {

    fun generateDummyStoryResponse(): List<ListStoryItem> {
        val items: MutableList<ListStoryItem> = arrayListOf()
        for (i in 0..100) {
            val story = ListStoryItem(
                id = "story-6Cz33Gby--XR_BOUk",
                name = "6alejandro",
                description = "It is testing number ${i+1}.",
                photoUrl = "https://story-api.dicoding.dev/images/stories/photos-61699167960197_vuc8U6F5.jpg",
                createdAt = "2023-11-05T07:06:00.203Z",
                lat = -7.7558361,
                lon = 110.4960597
            )
            items.add(story)
        }
        return items
    }
}
