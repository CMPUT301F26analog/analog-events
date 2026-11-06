package com.example.birchbark

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.birchbark.ui.entrant.EditProfileForm
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class EditProfileScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    private val profile = UserProfile(
        fid = "test-device-123",
        firstName = "Alex",
        lastName = "Smith",
        email = "alex@example.com",
        phoneNumber = "7801234567",
        userName = "alexsmith",
        roles = listOf("entrant")
    )

    private fun showForm(
        saving: Boolean = false,
        message: String = "",
        onSave: (UserProfile) -> Unit = {}
    ) {
        composeRule.setContent {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
            ) {
                EditProfileForm(
                    profile = profile,
                    saving = saving,
                    message = message,
                    onSave = onSave
                )
            }
        }
    }

    @Test
    fun fieldsShowExistingProfileInformation() {
        showForm()

        composeRule.onNodeWithTag("firstNameField")
            .assertTextContains("Alex")

        composeRule.onNodeWithTag("lastNameField")
            .assertTextContains("Smith")

        composeRule.onNodeWithTag("emailField")
            .assertTextContains("alex@example.com")

        composeRule.onNodeWithTag("phoneField")
            .assertTextContains("7801234567")
    }

    @Test
    fun firstNameCanBeUpdated() {
        var saved: UserProfile? = null

        showForm(onSave = { saved = it })

        composeRule.onNodeWithTag("firstNameField")
            .performTextClearance()

        composeRule.onNodeWithTag("firstNameField")
            .performTextInput("Jordan")

        composeRule.onNodeWithTag("saveProfileButton")
            .performScrollTo()
            .performClick()

        assertEquals("Jordan", saved?.firstName)
        assertEquals("Smith", saved?.lastName)
    }

    @Test
    fun lastNameCanBeUpdated() {
        var saved: UserProfile? = null

        showForm(onSave = { saved = it })

        composeRule.onNodeWithTag("lastNameField")
            .performTextClearance()

        composeRule.onNodeWithTag("lastNameField")
            .performTextInput("Wilson")

        composeRule.onNodeWithTag("saveProfileButton")
            .performScrollTo()
            .performClick()

        assertEquals("Wilson", saved?.lastName)
    }

    @Test
    fun emailCanBeUpdated() {
        var saved: UserProfile? = null

        showForm(onSave = { saved = it })

        composeRule.onNodeWithTag("emailField")
            .performTextClearance()

        composeRule.onNodeWithTag("emailField")
            .performTextInput("jordan@example.com")

        composeRule.onNodeWithTag("saveProfileButton")
            .performScrollTo()
            .performClick()

        assertEquals("jordan@example.com", saved?.email)
    }

    @Test
    fun phoneNumberCanBeUpdated() {
        var saved: UserProfile? = null

        showForm(onSave = { saved = it })

        composeRule.onNodeWithTag("phoneField")
            .performTextClearance()

        composeRule.onNodeWithTag("phoneField")
            .performTextInput("5871234567")

        composeRule.onNodeWithTag("saveProfileButton")
            .performScrollTo()
            .performClick()

        assertEquals("5871234567", saved?.phoneNumber)
    }

    @Test
    fun savingKeepsIdentityAndRoleUnchanged() {
        var saved: UserProfile? = null

        showForm(onSave = { saved = it })

        composeRule.onNodeWithTag("saveProfileButton")
            .performScrollTo()
            .performClick()

        assertEquals("test-device-123", saved?.fid)
        assertEquals("alexsmith", saved?.userName)
        assertEquals(listOf("entrant"), saved?.roles)
    }

    @Test
    fun savingDisablesFieldsAndButton() {
        showForm(saving = true)

        composeRule.onNodeWithTag("firstNameField")
            .assertIsNotEnabled()

        composeRule.onNodeWithTag("emailField")
            .assertIsNotEnabled()

        composeRule.onNodeWithTag("saveProfileButton")
            .assertIsNotEnabled()
    }

    @Test
    fun errorMessageIsDisplayed() {
        showForm(message = "That email address doesn't look right.")

        composeRule.onNodeWithTag("profileMessage")
            .assertIsDisplayed()
    }

    @Test
    fun invalidEmailIsRejectedByRepository() {
        val repository = UserRepository()

        val invalidProfile = profile.copy(
            email = "not-an-email"
        )

        assertEquals(
            "That email address doesn't look right.",
            repository.validateProfile(invalidProfile)
        )
    }

    @Test
    fun validProfilePassesValidation() {
        val repository = UserRepository()

        assertNull(repository.validateProfile(profile))
    }
}