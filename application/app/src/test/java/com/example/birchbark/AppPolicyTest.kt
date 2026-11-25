package com.example.birchbark

import com.example.birchbark.model.AppPolicy
import org.junit.Assert
import org.junit.Test

/**
 * Unit tests for the app policy text.
 * Covers US 01.11.01: the policy has real content, so the page shows the
 * full policy and guidelines.
 */
class AppPolicyTest {

    @Test
    fun policy_hasSections() {
        Assert.assertTrue(AppPolicy.sections.isNotEmpty())
    }

    @Test
    fun policy_hasATitle() {
        Assert.assertTrue(AppPolicy.TITLE.isNotBlank())
    }

    @Test
    fun everySection_hasTitleAndBody() {
        for (section in AppPolicy.sections) {
            Assert.assertTrue("A section has a blank title", section.title.isNotBlank())
            Assert.assertTrue("'${section.title}' has a blank body", section.body.isNotBlank())
        }
    }

    // two sections with the same title would be confusing, and break the screen tests
    @Test
    fun sectionTitles_areUnique() {
        val titles = AppPolicy.sections.map { it.title }
        Assert.assertEquals(titles.size, titles.toSet().size)
    }

    // the policy should talk about the things entrants actually do in the app
    @Test
    fun policy_coversTheMainTopics() {
        val titles = AppPolicy.sections.joinToString(" ") { it.title }.lowercase()
        for (topic in listOf("lottery", "cancel", "notification", "information")) {
            Assert.assertTrue("No section about '$topic'", titles.contains(topic))
        }
    }
}