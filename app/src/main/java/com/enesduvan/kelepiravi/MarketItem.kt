package com.enesduvan.kelepiravi

import androidx.compose.ui.graphics.painter.Painter
import kotlinx.serialization.Serializable

@Serializable
data class MarketItem(val condition: String ,
                      val sellerName: String ,
                      val itemName: String ,
                      val salesValue: String ,
                      val estimatedValue: String ,
                      val imageName : String)
