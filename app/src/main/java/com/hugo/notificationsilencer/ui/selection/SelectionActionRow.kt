package com.hugo.notificationsilencer.ui.selection

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.hugo.notificationsilencer.theme.MistRed
import com.hugo.notificationsilencer.theme.PrimaryText
import com.hugo.notificationsilencer.theme.SecondaryText

@Composable
fun SelectionActionRow(
    selectedCount: Int,
    allSelected: Boolean,
    onSelectAllChange: (Boolean) -> Unit,
    onDeleteSelected: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(Color.White.copy(alpha = 0.92f))
            .padding(start = 6.dp, top = 2.dp, end = 8.dp, bottom = 2.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Checkbox(
            checked = allSelected,
            onCheckedChange = onSelectAllChange,
            colors = CheckboxDefaults.colors(
                checkedColor = PrimaryText,
                uncheckedColor = SecondaryText,
                checkmarkColor = Color.White,
            ),
        )
        Text(
            text = "已选 $selectedCount",
            style = MaterialTheme.typography.bodyMedium,
            color = PrimaryText,
        )
        IconButton(onClick = onDeleteSelected, modifier = Modifier.size(40.dp)) {
            Icon(
                imageVector = Icons.Filled.Delete,
                contentDescription = "删除所选",
                tint = MistRed,
            )
        }
        Spacer(modifier = Modifier.weight(1f))
    }
}
