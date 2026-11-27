package com.example.birchbark.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.birchbark.model.Campsite
import com.example.birchbark.ui.theme.BirchBarkColors
import com.example.birchbark.util.ratingLabel
import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale

// See [TBD 6] in LLM.md

/**
 * Makes initials from the first letters of up to two words
 *
 * @param name the person's name.
 * @return the initials in capitals, or an empty string for a blank name.
 */
fun initialsOf(name: String): String =
    name.split(" ").mapNotNull { it.firstOrNull() }.take(2).joinToString("").uppercase()

/**
 * The big gold heading at the top of a page, like "Your Lotteries" in the mockup.
 *
 * @param text the heading.
 * @param modifier modifier from the parent layout.
 */
@Composable
fun ScreenTitle(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        style = MaterialTheme.typography.headlineMedium,
        fontWeight = FontWeight.ExtraBold,
        color = BirchBarkColors.Gold,
        modifier = modifier
    )
}

/**
 * A round circle with the person's initials. Swap for their profile photo
 * once image upload exists.
 *
 * @param name the person's name, used for the initials.
 * @param modifier modifier from the parent layout.
 * @param size the diameter of the circle.
 */
@Composable
fun Avatar(
    name: String,
    modifier: Modifier = Modifier,
    size: Dp = 40.dp,
    imageBytes: ByteArray? = null
) {
    //decode once per photo, not on every redraw
    val bitmap = remember(imageBytes) {
        imageBytes?.let { BitmapFactory.decodeByteArray(it, 0, it.size) }?.asImageBitmap()
    }

    if (bitmap != null) {
        Image(
            bitmap = bitmap,
            contentDescription = "Profile photo of $name",
            contentScale = ContentScale.Crop,
            modifier = modifier.size(size).clip(CircleShape).testTag("profileImage")
        )
    } else {
        Box(
            modifier = modifier
                .size(size)
                .background(BirchBarkColors.AvatarGreen, CircleShape)
                .testTag("profileInitials"),
            contentAlignment = Alignment.Center
        ) {
            Text(initialsOf(name), color = BirchBarkColors.Cream)
        }
    }
}

/**
 * The strip at the top of the entrant screens: avatar, "Welcome" and the name,
 * and an optional notifications button on the right.
 *
 * @param name the entrant's name.
 * @param modifier modifier from the parent layout.
 * @param onNotificationsClick called when the bell is tapped, or null to hide the bell
 *   (until the notifications screen exists).
 */
@Composable
fun UserHeader(
    name: String,
    modifier: Modifier = Modifier,
    onNotificationsClick: (() -> Unit)? = null,
    imageBytes: ByteArray? = null
){
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Avatar(name, imageBytes = imageBytes)
        Spacer(modifier = Modifier.width(10.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text("Welcome", color = BirchBarkColors.MutedText)
            Text(name, color = BirchBarkColors.Cream)
        }
        if (onNotificationsClick != null) {
            Text(
                "࿄",
                modifier = Modifier
                    .clickable(onClick = onNotificationsClick)
                    .padding(8.dp)
                    .testTag("notificationsButton")
            )
        }
    }
}

/** The colours a [StatusChip] can have. */
enum class ChipKind { NEUTRAL, ACTIVE, PAST, CANCELLED }

/**
 * A small rounded label, like the rating or the status marker on a card in the mockup.
 *
 * @param text what the chip says.
 * @param kind which colours to use.
 * @param modifier modifier from the parent layout.
 */
@Composable
fun StatusChip(text: String, kind: ChipKind = ChipKind.NEUTRAL, modifier: Modifier = Modifier) {
    val background = when (kind) {
        ChipKind.NEUTRAL -> BirchBarkColors.Scrim
        ChipKind.ACTIVE -> BirchBarkColors.Teal
        ChipKind.PAST -> BirchBarkColors.AvatarGreen
        ChipKind.CANCELLED -> Color(0xFFB85C5C)
    }
    Text(
        text = text,
        color = BirchBarkColors.Cream,
        style = MaterialTheme.typography.labelMedium,
        fontWeight = FontWeight.Bold,
        modifier = modifier
            .background(background, RoundedCornerShape(50))
            .padding(horizontal = 10.dp, vertical = 4.dp)
    )
}

/**
 * The campsite photo placeholder: the dark green gradient from the mockup.
 * Replace the gradient with the real promo image when image upload is done.
 *
 * @param modifier modifier from the parent layout, set the size here.
 */
@Composable
fun PhotoPlaceholder(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.background(
            Brush.verticalGradient(
                listOf(BirchBarkColors.PhotoTop, BirchBarkColors.PhotoMid, BirchBarkColors.PhotoBottom)
            )
        )
    )
}

/**
 * A big rounded card with a photo behind it, chips along the top and text along
 * the bottom. Used for campsites and reservations, like the cards in the mockup.
 *
 * @param modifier modifier from the parent layout (tests can add a testTag here).
 * @param onClick called when the card is tapped, or null if it isn't tappable.
 * @param height how tall the card is.
 * @param topRow chips shown in the top left corner.
 * @param bottomStart the text shown along the bottom, on a dark strip.
 * @param bottomEnd something shown at the bottom right, like a status marker or button.
 */
@Composable
fun PhotoCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    height: Dp = 190.dp,
    topRow: @Composable RowScope.() -> Unit = {},
    bottomStart: @Composable ColumnScope.() -> Unit,
    bottomEnd: @Composable () -> Unit = {}
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .clip(RoundedCornerShape(20.dp))
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
    ) {
        PhotoPlaceholder(modifier = Modifier.fillMaxSize())

        Row(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            content = topRow
        )

        Row(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .fillMaxWidth()
                .background(BirchBarkColors.Scrim)
                .padding(12.dp),
            verticalAlignment = Alignment.Bottom
        ) {
            Column(modifier = Modifier.weight(1f), content = bottomStart)
            bottomEnd()
        }
    }
}

/**
 * The campsite card for lists: rating on top, then name, location and capacity.
 *
 * @param campsite the campsite to show.
 * @param onClick called when the card is tapped.
 * @param modifier modifier from the parent layout (tests can add a testTag here).
 * @param bottomEnd something shown at the bottom right, like a status marker.
 */
@Composable
fun CampsiteCard(
    campsite: Campsite,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    bottomEnd: @Composable () -> Unit = {}
) {
    PhotoCard(
        modifier = modifier,
        onClick = onClick,
        topRow = { StatusChip(campsite.ratingLabel()) },
        bottomStart = {
            Text(
                campsite.name,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.ExtraBold,
                color = BirchBarkColors.Cream
            )
            Text(campsite.location, color = BirchBarkColors.Cream)
            Text(
                if (campsite.maxOccupancy == 1) "Up to 1 guest" else "Up to ${campsite.maxOccupancy} guests",
                color = BirchBarkColors.MutedText
            )
        },
        bottomEnd = bottomEnd
    )
}

/**
 * One tappable row in a menu, like "Your Profile" in the mockup's profile page.
 *
 * @param label the row's text.
 * @param modifier modifier from the parent layout.
 * @param onClick called when the row is tapped.
 */
@Composable
fun MenuRow(label: String, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(BirchBarkColors.TealCard)
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, color = BirchBarkColors.Cream, modifier = Modifier.weight(1f))
        Text("›", color = BirchBarkColors.MutedText)
    }
}

/**
 * The big centred message for an empty page, like "Site not found" in the mockup.
 *
 * @param title the big bold text.
 * @param hint a smaller line underneath.
 * @param modifier modifier from the parent layout (tests can add a testTag here).
 */
@Composable
fun EmptyState(title: String, hint: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            title,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.ExtraBold,
            color = BirchBarkColors.Cream,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(hint, color = BirchBarkColors.MutedText, textAlign = TextAlign.Center)
    }
}
