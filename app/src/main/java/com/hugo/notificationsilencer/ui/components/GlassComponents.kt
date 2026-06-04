package com.hugo.notificationsilencer.ui.components

import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.os.Build
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmap
import com.hugo.notificationsilencer.theme.AppBackground
import com.hugo.notificationsilencer.theme.GlassWhite
import com.hugo.notificationsilencer.theme.MutedText
import com.hugo.notificationsilencer.theme.PrimaryText
import com.hugo.notificationsilencer.theme.SecondaryText
import com.hugo.notificationsilencer.theme.SoftGray

@Composable
fun GlassBackground(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(AppBackground),
    ) {
        content()
    }
}

@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    containerColor: Color = GlassWhite,
    content: @Composable () -> Unit,
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        color = containerColor,
        shadowElevation = 1.dp,
        tonalElevation = 0.dp,
    ) {
        content()
    }
}

@Composable
fun PageHeader(
    title: String,
    subtitle: String? = null,
    icon: ImageVector? = null,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.padding(bottom = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (icon != null) {
            Surface(
                shape = CircleShape,
                color = Color.White.copy(alpha = 0.75f),
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = PrimaryText,
                    modifier = Modifier.padding(10.dp).size(22.dp),
                )
            }
        }
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.SemiBold,
                color = PrimaryText,
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = SecondaryText,
                )
            }
        }
    }
}

@Composable
fun StatusPill(
    text: String,
    icon: ImageVector,
    containerColor: Color,
    contentColor: Color,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(999.dp))
            .background(containerColor)
            .padding(horizontal = 10.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = contentColor,
            modifier = Modifier.size(15.dp),
        )
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium,
            color = contentColor,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
fun AppAvatar(
    appName: String,
    packageName: String? = null,
    modifier: Modifier = Modifier,
    showContainer: Boolean = true,
) {
    val label = appName.firstOrNull()?.uppercaseChar()?.toString() ?: "A"
    val context = LocalContext.current
    val icon = remember(packageName) {
        packageName?.let {
            runCatching {
                context.packageManager.getApplicationIcon(it)
            }.getOrNull()
        }
    }
    Box(
        modifier = modifier.avatarContainer(showContainer),
        contentAlignment = Alignment.Center,
    ) {
        if (icon != null) {
            Image(
                bitmap = icon.asAvatarBitmap(),
                contentDescription = appName,
                modifier = Modifier
                    .size(32.dp)
                    .clip(RoundedCornerShape(9.dp)),
                contentScale = ContentScale.Fit,
            )
        } else {
            Text(
                text = label,
                style = MaterialTheme.typography.titleMedium,
                color = PrimaryText,
                fontWeight = FontWeight.SemiBold,
            )
        }
    }
}

private fun Modifier.avatarContainer(showContainer: Boolean): Modifier {
    return if (showContainer) {
        this
            .size(44.dp)
            .clip(CircleShape)
            .background(Color.White.copy(alpha = 0.78f))
    } else {
        this.size(44.dp)
    }
}

private fun Drawable.asAvatarBitmap() = toBitmap(width = 96, height = 96).asImageBitmap()

@Composable
fun rememberAppLabel(packageName: String, fallback: String): String {
    val context = LocalContext.current
    return remember(packageName, fallback) {
        packageName.let {
            runCatching {
                val info = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    context.packageManager.getApplicationInfo(it, PackageManager.ApplicationInfoFlags.of(0))
                } else {
                    @Suppress("DEPRECATION")
                    context.packageManager.getApplicationInfo(it, 0)
                }
                info.loadLabel(context.packageManager).toString()
            }.getOrNull()
        } ?: fallback
    }
}

@Composable
fun IconLabelButton(
    text: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    containerColor: Color = PrimaryText,
    contentColor: Color = Color.White,
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = containerColor,
            contentColor = contentColor,
            disabledContainerColor = SoftGray,
            disabledContentColor = MutedText,
        ),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp),
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(18.dp),
            )
            Text(text = text)
        }
    }
}

@Composable
fun StatItem(
    label: String,
    value: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    selected: Boolean = false,
    onClick: (() -> Unit)? = null,
) {
    GlassCard(
        modifier = modifier.then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier),
        containerColor = if (selected) SoftGray else GlassWhite,
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = SecondaryText,
                    modifier = Modifier.size(18.dp),
                )
                Spacer(modifier = Modifier.size(8.dp))
                Text(text = label, style = MaterialTheme.typography.labelMedium, color = SecondaryText)
            }
            Text(
                text = value,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.SemiBold,
                color = PrimaryText,
            )
        }
    }
}

@Composable
fun GlassActionRow(
    modifier: Modifier = Modifier,
    content: @Composable RowScope.() -> Unit,
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(Color.White.copy(alpha = 0.9f))
            .padding(10.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically,
        content = content,
    )
}
