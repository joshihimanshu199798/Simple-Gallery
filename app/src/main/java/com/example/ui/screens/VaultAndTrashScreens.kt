package com.example.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.RestoreFromTrash
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.data.PhotoEntity
import com.example.data.PhotoImageProcessor

@Composable
fun VaultScreen(
    isUnlocked: Boolean,
    currentPinHint: String,
    vaultPhotos: List<PhotoEntity>,
    biometricEnabled: Boolean = true,
    onToggleBiometric: () -> Unit = {},
    onAttemptUnlock: (String) -> Boolean,
    onLockVault: () -> Unit,
    onChangePin: (String) -> Unit,
    onPhotoClick: (PhotoEntity) -> Unit,
    onUnhidePhoto: (PhotoEntity) -> Unit
) {
    var enteredDigits by remember { mutableStateOf("") }
    var pinError by remember { mutableStateOf(false) }
    var showChangePinDialog by remember { mutableStateOf(false) }

    AnimatedContent(
        targetState = isUnlocked,
        transitionSpec = {
            (scaleIn(
                initialScale = 0.92f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioLowBouncy,
                    stiffness = Spring.StiffnessMediumLow
                )
            ) + fadeIn(tween(220))) togetherWith (scaleOut(
                targetScale = 1.04f,
                animationSpec = tween(160)
            ) + fadeOut(tween(160)))
        },
        label = "vault_lock_state_transition",
        modifier = Modifier.fillMaxSize()
    ) { unlockedState ->
    if (!unlockedState) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
                .testTag("vault_locked_screen"),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.size(72.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.Security,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(36.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Private Photo Vault",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "Enter your 4-digit PIN to inspect hidden photographs (Current PIN: $currentPinHint)",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(20.dp))

            // 4-dot PIN indicator with bouncy spring animation
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                repeat(4) { index ->
                    val filled = index < enteredDigits.length
                    val dotScale by animateFloatAsState(
                        targetValue = if (filled) 1.25f else 1f,
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioHighBouncy,
                            stiffness = Spring.StiffnessMedium
                        ),
                        label = "pin_dot_scale_$index"
                    )
                    val dotColor by animateColorAsState(
                        targetValue = when {
                            pinError -> MaterialTheme.colorScheme.error
                            filled -> MaterialTheme.colorScheme.primary
                            else -> MaterialTheme.colorScheme.surfaceVariant
                        },
                        animationSpec = spring(stiffness = Spring.StiffnessMedium),
                        label = "pin_dot_color_$index"
                    )
                    Box(
                        modifier = Modifier
                            .scale(dotScale)
                            .size(18.dp)
                            .clip(CircleShape)
                            .background(dotColor)
                    )
                }
            }

            AnimatedVisibility(visible = pinError) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Incorrect PIN. Try $currentPinHint",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Tactile Numeric Keypad
            val rows = listOf(
                listOf("1", "2", "3"),
                listOf("4", "5", "6"),
                listOf("7", "8", "9"),
                listOf("CLR", "0", "DEL")
            )

            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                rows.forEach { row ->
                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        row.forEach { key ->
                            Surface(
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                modifier = Modifier
                                    .size(68.dp)
                                    .clip(CircleShape)
                                    .clickable {
                                        pinError = false
                                        when (key) {
                                            "CLR" -> enteredDigits = ""
                                            "DEL" -> if (enteredDigits.isNotEmpty()) {
                                                enteredDigits = enteredDigits.dropLast(1)
                                            }
                                            else -> if (enteredDigits.length < 4) {
                                                val next = enteredDigits + key
                                                enteredDigits = next
                                                if (next.length == 4) {
                                                    if (!onAttemptUnlock(next)) {
                                                        pinError = true
                                                        enteredDigits = ""
                                                    } else {
                                                        enteredDigits = ""
                                                    }
                                                }
                                            }
                                        }
                                    }
                                    .testTag("vault_key_$key")
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    if (key == "DEL") {
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Filled.Backspace,
                                            contentDescription = "Backspace",
                                            tint = MaterialTheme.colorScheme.onSurface
                                        )
                                    } else {
                                        Text(
                                            text = key,
                                            style = MaterialTheme.typography.titleLarge,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Biometric / Passkey 1-Tap Quick Unlock Button (when enabled)
            if (biometricEnabled) {
                OutlinedButton(
                    onClick = {
                        pinError = false
                        enteredDigits = ""
                        onAttemptUnlock(currentPinHint)
                    },
                    shape = RoundedCornerShape(50),
                    modifier = Modifier.testTag("vault_biometric_unlock_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.LockOpen,
                        contentDescription = "Biometric Passkey Unlock",
                        modifier = Modifier.size(18.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Quick Unlock with Biometrics / Passkey")
                }
            } else {
                Text(
                    text = "Biometric quick-unlock disabled • Enter PIN to open",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    } else {
        // Unlocked Vault Content
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier
                .fillMaxSize()
                .testTag("vault_unlocked_grid"),
            contentPadding = PaddingValues(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 120.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item(span = { GridItemSpan(maxLineSpan) }) {
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Unlocked Private Vault",
                                    style = MaterialTheme.typography.headlineMedium
                                )
                                Text(
                                    text = "${vaultPhotos.size} hidden photo(s) isolated from public timeline",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedButton(
                                    onClick = { showChangePinDialog = true },
                                    modifier = Modifier.testTag("change_vault_pin_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Key,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("PIN")
                                }
                                Button(
                                    onClick = onLockVault,
                                    modifier = Modifier.testTag("lock_vault_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Lock,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Lock")
                                }
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Biometric & Passkey Unlock",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = if (biometricEnabled) "Enabled for fast unlock" else "PIN required every unlock",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            androidx.compose.material3.Switch(
                                checked = biometricEnabled,
                                onCheckedChange = { onToggleBiometric() },
                                modifier = Modifier.testTag("vault_biometric_toggle")
                            )
                        }
                    }
                }
            }

            if (vaultPhotos.isEmpty()) {
                item(span = { GridItemSpan(maxLineSpan) }) {
                    Card(
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 24.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(28.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.LockOpen,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(44.dp)
                            )
                            Text(
                                text = "No hidden photos in Vault",
                                style = MaterialTheme.typography.titleLarge
                            )
                            Text(
                                text = "Open any photo in the Lightbox or long-press to select photos and tap 'Vault' to hide sensitive shots.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            } else {
                items(vaultPhotos, key = { it.id }) { photo ->
                    val vaultContext = LocalContext.current
                    val vaultThumbModel = remember(photo.id, photo.filePath) {
                        PhotoImageProcessor.buildThumbnailRequest(vaultContext, photo.filePath, 360, 360)
                    }
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .clickable { onPhotoClick(photo) }
                    ) {
                        Column {
                            AsyncImage(
                                model = vaultThumbModel,
                                contentDescription = photo.title,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .aspectRatio(1.1f)
                            )
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = photo.title,
                                        style = MaterialTheme.typography.labelLarge,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = photo.formattedSize,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                TextButton(onClick = { onUnhidePhoto(photo) }) {
                                    Text("Unhide")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
    }

    if (showChangePinDialog) {
        var newPin by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showChangePinDialog = false },
            title = { Text("Set New 4-Digit Vault PIN") },
            text = {
                OutlinedTextField(
                    value = newPin,
                    onValueChange = { input ->
                        if (input.length <= 4 && input.all { it.isDigit() }) {
                            newPin = input
                        }
                    },
                    label = { Text("4-digit numeric PIN") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    enabled = newPin.length == 4,
                    onClick = {
                        onChangePin(newPin)
                        showChangePinDialog = false
                    }
                ) {
                    Text("Save PIN")
                }
            },
            dismissButton = {
                TextButton(onClick = { showChangePinDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun RecycleBinScreen(
    trashedPhotos: List<PhotoEntity>,
    onRestorePhoto: (PhotoEntity) -> Unit,
    onDeletePermanently: (PhotoEntity) -> Unit,
    onEmptyTrash: () -> Unit
) {
    var showEmptyConfirm by remember { mutableStateOf(false) }
    var photoPendingPermanentDelete by remember { mutableStateOf<PhotoEntity?>(null) }

    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = Modifier
            .fillMaxSize()
            .testTag("recycle_bin_grid"),
        contentPadding = PaddingValues(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 120.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item(span = { GridItemSpan(maxLineSpan) }) {
            val totalReclaimableBytes = remember(trashedPhotos) {
                trashedPhotos.sumOf { it.sizeBytes }
            }
            val reclaimableText = remember(totalReclaimableBytes) {
                when {
                    totalReclaimableBytes >= 1_048_576L -> String.format("%.1f MB", totalReclaimableBytes / 1_048_576.0)
                    totalReclaimableBytes >= 1_024L -> "${totalReclaimableBytes / 1_024L} KB"
                    else -> "$totalReclaimableBytes B"
                }
            }

            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Recycle Bin & Recovery",
                                style = MaterialTheme.typography.headlineMedium
                            )
                            Text(
                                text = "${trashedPhotos.size} deleted item(s) • $reclaimableText reclaimable",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.14f)
                        ) {
                            Text(
                                text = "30-Day Window",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    }

                    if (trashedPhotos.isNotEmpty()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = {
                                    trashedPhotos.forEach { onRestorePhoto(it) }
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("restore_all_trash_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.RestoreFromTrash,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Restore All")
                            }
                            Button(
                                onClick = { showEmptyConfirm = true },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.error,
                                    contentColor = MaterialTheme.colorScheme.onError
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("empty_trash_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.DeleteSweep,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Empty")
                            }
                        }
                    }
                }
            }
        }

        if (trashedPhotos.isEmpty()) {
            item(span = { GridItemSpan(maxLineSpan) }) {
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 24.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(28.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.RestoreFromTrash,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(44.dp)
                        )
                        Text(
                            text = "Recycle Bin is clean",
                            style = MaterialTheme.typography.titleLarge
                        )
                        Text(
                            text = "Deleted photos stay here safely so you never lose a precious memory by accident.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        } else {
            items(trashedPhotos, key = { it.id }) { photo ->
                val trashContext = LocalContext.current
                val trashThumbModel = remember(photo.id, photo.filePath) {
                    PhotoImageProcessor.buildThumbnailRequest(trashContext, photo.filePath, 360, 360)
                }
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                ) {
                    Column {
                        AsyncImage(
                            model = trashThumbModel,
                            contentDescription = photo.title,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .fillMaxWidth()
                                .aspectRatio(1.2f)
                        )
                        Column(
                            modifier = Modifier.padding(10.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = photo.title,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = "${photo.albumName} • ${photo.formattedSize}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                OutlinedButton(
                                    onClick = { onRestorePhoto(photo) },
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("restore_photo_${photo.id}"),
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text("Restore", style = MaterialTheme.typography.labelMedium)
                                }
                                Button(
                                    onClick = { photoPendingPermanentDelete = photo },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.error,
                                        contentColor = MaterialTheme.colorScheme.onError
                                    ),
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("delete_forever_${photo.id}"),
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.DeleteForever,
                                        contentDescription = "Delete forever",
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    val targetPhoto = photoPendingPermanentDelete
    if (targetPhoto != null) {
        AlertDialog(
            onDismissRequest = { photoPendingPermanentDelete = null },
            modifier = Modifier.testTag("delete_confirmation_dialog"),
            title = { Text("Delete Photo from Local Storage?") },
            text = {
                Text(
                    "Permanently delete \"${targetPhoto.title}\" (${targetPhoto.formattedSize}) from local storage? This action cannot be undone."
                )
            },
            confirmButton = {
                Button(
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    ),
                    onClick = {
                        onDeletePermanently(targetPhoto)
                        photoPendingPermanentDelete = null
                    },
                    modifier = Modifier.testTag("confirm_delete_photos_button")
                ) {
                    Text("Delete Permanently")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { photoPendingPermanentDelete = null },
                    modifier = Modifier.testTag("cancel_delete_photos_button")
                ) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showEmptyConfirm) {
        AlertDialog(
            onDismissRequest = { showEmptyConfirm = false },
            modifier = Modifier.testTag("delete_confirmation_dialog"),
            title = { Text("Empty Recycle Bin?") },
            text = {
                Text(
                    "This will permanently remove ${trashedPhotos.size} file(s) from local storage. This action cannot be undone."
                )
            },
            confirmButton = {
                Button(
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    ),
                    onClick = {
                        onEmptyTrash()
                        showEmptyConfirm = false
                    },
                    modifier = Modifier.testTag("confirm_delete_photos_button")
                ) {
                    Text("Delete Permanently")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showEmptyConfirm = false },
                    modifier = Modifier.testTag("cancel_delete_photos_button")
                ) {
                    Text("Cancel")
                }
            }
        )
    }
}
