package com.example.birchbark
// This class is used to keep the sample data
// The data here is used in the AdminCampsiteScreen to keep a record of the campsites and
// also used for filtering in the AdminProfileScreen
data class CampsitePreview(
    val name: String,
    val location: String,
    val rating: Double,
    val owner: String,
    val keywords: String = "",
    val siteType: String = "",
    val capacity: Int = 0,
    val status: String = "Open"
)

object SampleData {
    val campsites = listOf(
        CampsitePreview(
            name = "Jasper National Park",
            location = "Near Jasper",
            rating = 4.9,
            owner = "Jordan Lee"
        ),
        CampsitePreview(
            name = "Eagle Creek",
            location = "Near Hinton",
            rating = 4.6,
            owner = "Sam Patel"
        ),
        CampsitePreview(
            name = "Willow Lake",
            location = "Near Banff",
            rating = 4.8,
            owner = "Taylor Chen"
        )
    )
}