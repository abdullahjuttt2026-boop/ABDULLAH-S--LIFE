package com.abdullah.life.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.abdullah.life.data.AppDatabase
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrashScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val dao = remember { AppDatabase.getDatabase(context).diaryDao() }
    val deletedEntries by dao.getDeletedEntries().collectAsState(initial = emptyList())

    LaunchedEffect(Unit) {
        // Purge items older than 30 days (30L * 24 * 60 * 60 * 1000)
        val thirtyDaysAgo = System.currentTimeMillis() - (30L * 24 * 60 * 60 * 1000)
        dao.purgeOldDeletedEntries(thirtyDaysAgo)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Recently Deleted", color = Color.White, fontWeight = FontWeight.Bold) },
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
                .padding(16.dp)
        ) {
            Text(
                "Entries stay here for 30 days before being permanently deleted.",
                fontSize = 13.sp,
                color = Color.Gray,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            if (deletedEntries.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Trash is empty", color = Color.Gray)
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(deletedEntries, key = { it.id }) { item ->
                        val daysLeft = remember(item.deletedTimestamp) {
                            val elapsedDays = ((System.currentTimeMillis() - (item.deletedTimestamp ?: System.currentTimeMillis())) / (1000 * 60 * 60 * 24)).toInt()
                            maxOf(0, 30 - elapsedDays)
                        }

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF111827))
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(item.title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                    Text("$daysLeft days until permanent delete", color = Color(0xFFEF9A9A), fontSize = 12.sp)
                                }

                                // Restore button
                                Button(
                                    onClick = {
                                        scope.launch {
                                            dao.insertOrUpdate(item.copy(isDeleted = false, deletedTimestamp = null))
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E293B))
                                ) {
                                    Icon(Icons.Rounded.Restore, contentDescription = "Restore", tint = Color(0xFFA5D6A7))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Restore", color = Color(0xFFA5D6A7), fontSize = 13.sp)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}