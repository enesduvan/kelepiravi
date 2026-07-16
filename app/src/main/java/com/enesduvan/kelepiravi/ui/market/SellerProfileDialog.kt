package com.enesduvan.kelepiravi.ui.market

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.enesduvan.kelepiravi.R
import com.enesduvan.kelepiravi.ui.shared.formatBalance
import com.enesduvan.kelepiravi.ui.shared.getPainterResourceByName
import com.enesduvan.kelepiravi.ui.theme.*

@Composable
fun SellerProfileDialog(
    profile: SellerProfileState,
    onDismiss: () -> Unit,
    onItemClick: (com.enesduvan.kelepiravi.data.model.MarketItem) -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.85f)
                .clip(RoundedCornerShape(24.dp)),
            color = Background
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Header
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Surface)
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            modifier = Modifier
                                .size(80.dp)
                                .clip(androidx.compose.foundation.shape.CircleShape)
                                .background(SurfaceVariant),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(painterResource(id = R.drawable.person), contentDescription = null, tint = TextSecondary, modifier = Modifier.size(40.dp))
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(profile.sellerName, color = TextPrimary, fontSize = 22.sp, fontWeight = FontWeight.ExtraBold)
                        Text(profile.sellerTitle, color = PrimaryOrange, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Row(
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFFFFD700), modifier = Modifier.size(16.dp))
                                    Text(" ${String.format("%.1f", profile.rating)}", color = TextPrimary, fontWeight = FontWeight.Bold)
                                }
                                Text("Puan", color = TextSecondary, fontSize = 12.sp)
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("${profile.totalSales}", color = TextPrimary, fontWeight = FontWeight.Bold)
                                Text("Satış", color = TextSecondary, fontSize = 12.sp)
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("${profile.joinYear}", color = TextPrimary, fontWeight = FontWeight.Bold)
                                Text("Katılım", color = TextSecondary, fontSize = 12.sp)
                            }
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Diğer ilanlar
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(horizontal = 16.dp)
                ) {
                    Text("Bu Satıcının Diğer İlanları", color = TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(bottom = 16.dp)
                    ) {
                        items(profile.otherItems) { item ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 6.dp)
                                    .clickable { onItemClick(item) },
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = Surface)
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(60.dp)
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(CardSecondary)
                                    ) {
                                        Image(
                                            painter = getPainterResourceByName(item.imageName),
                                            contentDescription = null,
                                            contentScale = ContentScale.Fit,
                                            modifier = Modifier.fillMaxSize()
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(item.itemName, color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                        Text(item.condition, color = TextSecondary, fontSize = 12.sp)
                                        Text("₺${formatBalance(item.salesValue)}", color = MoneyGreen, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }
                
                // Footer
                Box(modifier = Modifier.fillMaxWidth().background(Surface).padding(16.dp)) {
                    Button(
                        onClick = onDismiss,
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = SurfaceVariant),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Kapat", color = TextPrimary, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
