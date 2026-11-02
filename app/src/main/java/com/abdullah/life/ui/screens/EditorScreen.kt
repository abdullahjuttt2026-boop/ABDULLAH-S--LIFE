package com.abdullah.life.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.abdullah.life.data.*
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditorScreen(
    entryId: Long? = null,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val dao = remember { AppDatabase.getDatabase(context).diaryDao() }

    var title by remember { mutableStateOf("") }
    val blocks = remember { mutableStateListOf<ContentBlock>(ContentBlock(type = BlockType.TEXT, content = "")) }
    var saveStatus by remember { mutableStateOf("Saved") }

    // Visual Media Picker (Photos & Videos)
    val mediaPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        uri?.let {
            val isVideo = context.contentResolver.getType(it)?.startsWith("video") == true
            blocks.add(
                ContentBlock(
                    type = if (isVideo) BlockType.VIDEO else BlockType.IMAGE,
                    content = it.toString()
                )
            )
            // Add a text block below for continued writing
            blocks.add(ContentBlock(type = BlockType.TEXT, content = ""))
        }
    }

    // Auto-Save Function
    fun autoSave() {
        scope.launch {
            saveStatus = "Saving..."
            val jsonArray = JSONArray()
            blocks.forEach { block ->
                val obj = JSONObject()
                obj.put("id", block.id)
                obj.put("type", block.type.name)
                obj.put("content", block.content)
                jsonArray.put(obj)
            }
            val entry = DiaryEntry(
                id = entryId ?: 0,
                title = title.ifBlank { "Untitled Memory" },
                contentBlocksJson = jsonArray.toString(),
                isDraft = false
            )
            dao.insertOrUpdate(entry)
            saveStatus = "Saved"
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Write Memory", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        Text(saveStatus, color = Color(0xFF81D4FA), fontSize = 12.sp)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { autoSave(); onBack() }) {
                        Icon(Icons.Rounded.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                actions = {
                    IconButton(onClick = { autoSave(); onBack() }) {
                        Icon(Icons.Rounded.Check, contentDescription = "Done", tint = Color(0xFFA5D6A7))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF0B132B))
            )
        },
        bottomBar = {
            // Floating Multimedia Action Toolbar
            BottomAppBar(
                containerColor = Color(0xFF111827),
                tonalElevation = 8.dp
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = {
                        mediaPickerLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    }) {
                        Icon(Icons.Rounded.AddPhotoAlternate, contentDescription = "Insert Photo", tint = Color(0xFF81D4FA))
                    }

                    IconButton(onClick = {
                        mediaPickerLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.VideoOnly)
                        )
                    }) {
                        Icon(Icons.Rounded.VideoLibrary, contentDescription = "Insert Video", tint = Color(0xFFFFCC80))
                    }

                    IconButton(onClick = {
                        blocks.add(ContentBlock(type = BlockType.AUDIO, content = "Voice Memo [Recorded]"))
                        blocks.add(ContentBlock(type = BlockType.TEXT, content = ""))
                        autoSave()
                    }) {
                        Icon(Icons.Rounded.Mic, contentDescription = "Insert Voice Note", tint = Color(0xFFEF9A9A))
                    }
                }
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF030712))
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Title Input
            item {
                TextField(
                    value = title,
                    onValueChange = { title = it; autoSave() },
                    placeholder = { Text("Title of your entry...", color = Color.Gray, fontSize = 22.sp) },
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent
                    ),
                    textStyle = LocalTextStyle.current.copy(fontSize = 22.sp, fontWeight = FontWeight.Bold)
                )
            }

            // Interactive Multimedia Content Blocks
            itemsIndexed(blocks) { index, block ->
                when (block.type) {
                    BlockType.TEXT -> {
                        TextField(
                            value = block.content,
                            onValueChange = { updated ->
                                blocks[index] = block.copy(content = updated)
                                autoSave()
                            },
                            placeholder = { Text("Write your thoughts freely here...", color = Color(0xFF64748B)) },
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
                                focusedTextColor = Color(0xFFF1F5F9),
                                unfocusedTextColor = Color(0xFFF1F5F9),
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    BlockType.IMAGE -> {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFF1E293B),
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Rounded.Image, contentDescription = null, tint = Color(0xFF81D4FA))
                                Spacer(modifier = Modifier.width(12.dp))
                                Text("Attached Photo (Original Quality)", color = Color.White, fontSize = 14.sp)
                            }
                        }
                    }
                    BlockType.VIDEO -> {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFF1E293B),
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Rounded.PlayCircle, contentDescription = null, tint = Color(0xFFFFCC80))
                                Spacer(modifier = Modifier.width(12.dp))
                                Text("Attached Video", color = Color.White, fontSize = 14.sp)
                            }
                        }
                    }
                    BlockType.AUDIO -> {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFF1E293B),
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Rounded.GraphicEq, contentDescription = null, tint = Color(0xFFEF9A9A))
                                Spacer(modifier = Modifier.width(12.dp))
                                Text("Voice Memo", color = Color.White, fontSize = 14.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}