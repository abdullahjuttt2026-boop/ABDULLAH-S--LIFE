package com.abdullah.life.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.abdullah.life.data.AppDatabase
import com.abdullah.life.data.DiaryEntry
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimelineScreen(
    favoritesOnly: Boolean = false,
    onNavigateToEditor: (Long) -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val dao = remember { AppDatabase.getDatabase(context).diaryDao() }
    
    var searchQuery by remember { mutableStateOf("") }
    
    val entriesFlow = remember(searchQuery, favoritesOnly) {
        when {
            searchQuery.isNotBlank() -> dao.searchEntries(searchQuery)
            favoritesOnly -> dao.getFavoriteEntries()
            else -> dao.getAllActiveEntries()
        }
    }
    val entries by entriesFlow.collectAsState(initial = emptyList())
    val dateFormat = remember { SimpleDateFormat("MMMM dd, yyyy · hh:mm a", Locale.getDefault()) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        if (favoritesOnly) "Favorites" else "All Memories",
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Rounded.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF0B132B))
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF030712))
                .padding(padding)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(12.dp))

            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search your diary...", color = Color.Gray) },
                leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = null, tint = Color(0xFF81D4FA)) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Rounded.Close, contentDescription = "Clear", tint = Color.Gray)
                        }
                    }
                },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF81D4FA),
                    unfocusedBorderColor = Color(0xFF1E293B),
                    focusedContainerColor = Color(0xFF111827),
                    unfocusedContainerColor = Color(0xFF111827),
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (entries.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No memories found", color = Color.Gray, fontSize = 16.sp)
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(entries, key = { it.id }) { item ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onNavigateToEditor(item.id) },
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF111827))
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = item.title,
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        modifier = Modifier.weight(1f)
                                    )
                                    // Favorite Toggle Button
                                    IconButton(onClick = {
                                        scope.launch {
                                            dao.insertOrUpdate(item.copy(isFavorite = !item.isFavorite))
                                        }
                                    }) {
                                        Icon(
                                            imageVector = if (item.isFavorite) Icons.Rounded.Star else Icons.Rounded.StarBorder,
                                            contentDescription = "Favorite",
                                            tint = if (item.isFavorite) Color(0xFFFFF59D) else Color.Gray
                                        )
                                    }
                                    // Move to Trash Button
                                    IconButton(onClick = {
                                        scope.launch {
                                            dao.insertOrUpdate(
                                                item.copy(
                                                    isDeleted = true,
                                                    deletedTimestamp = System.currentTimeMillis()
                                                )
                                            )
                                        }
                                    }) {
                                        Icon(Icons.Rounded.DeleteOutline, contentDescription = "Delete", tint = Color(0xFFEF9A9A))
                                    }
                                }

                                Text(
                                    text = dateFormat.format(Date(item.createdAt)),
                                    fontSize = 12.sp,
                                    color = Color(0xFF81D4FA)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}