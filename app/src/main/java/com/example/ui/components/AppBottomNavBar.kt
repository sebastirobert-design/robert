package com.example.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Article
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.Navy900

@Composable
fun AppBottomNavBar(
    activeTab: Int,
    isTamil: Boolean,
    onTabSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    NavigationBar(
        containerColor = Navy900,
        tonalElevation = 8.dp,
        modifier = modifier.testTag("bottom_nav_bar")
    ) {
        // Tab 0: Home (முகப்பு)
        NavigationBarItem(
            selected = activeTab == 0,
            onClick = { onTabSelected(0) },
            icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
            label = {
                Text(
                    text = if (isTamil) "முகப்பு" else "Home",
                    fontSize = 10.sp,
                    fontWeight = if (activeTab == 0) FontWeight.Bold else FontWeight.Normal
                )
            },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Navy900,
                selectedTextColor = GoldAccent,
                indicatorColor = GoldAccent,
                unselectedIconColor = Color(0xFF90A4AE),
                unselectedTextColor = Color(0xFF90A4AE)
            ),
            modifier = Modifier.testTag("nav_item_home")
        )

        // Tab 1: Form 1 Diary (படிவம் 1)
        NavigationBarItem(
            selected = activeTab == 1,
            onClick = { onTabSelected(1) },
            icon = { Icon(Icons.Default.Article, contentDescription = "Diary") },
            label = {
                Text(
                    text = if (isTamil) "படிவம் 1" else "Form 1",
                    fontSize = 10.sp,
                    fontWeight = if (activeTab == 1) FontWeight.Bold else FontWeight.Normal
                )
            },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Navy900,
                selectedTextColor = GoldAccent,
                indicatorColor = GoldAccent,
                unselectedIconColor = Color(0xFF90A4AE),
                unselectedTextColor = Color(0xFF90A4AE)
            ),
            modifier = Modifier.testTag("nav_item_form1")
        )

        // Tab 2: Form 2 TA Bill (படிவம் 2)
        NavigationBarItem(
            selected = activeTab == 2,
            onClick = { onTabSelected(2) },
            icon = { Icon(Icons.Default.Description, contentDescription = "TA Bill") },
            label = {
                Text(
                    text = if (isTamil) "படிவம் 2" else "Form 2",
                    fontSize = 10.sp,
                    fontWeight = if (activeTab == 2) FontWeight.Bold else FontWeight.Normal
                )
            },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Navy900,
                selectedTextColor = GoldAccent,
                indicatorColor = GoldAccent,
                unselectedIconColor = Color(0xFF90A4AE),
                unselectedTextColor = Color(0xFF90A4AE)
            ),
            modifier = Modifier.testTag("nav_item_form2")
        )

        // Tab 3: Settings (அமைப்புகள்)
        NavigationBarItem(
            selected = activeTab == 3,
            onClick = { onTabSelected(3) },
            icon = { Icon(Icons.Default.Settings, contentDescription = "Settings") },
            label = {
                Text(
                    text = if (isTamil) "அமைப்புகள்" else "Settings",
                    fontSize = 10.sp,
                    fontWeight = if (activeTab == 3) FontWeight.Bold else FontWeight.Normal
                )
            },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Navy900,
                selectedTextColor = GoldAccent,
                indicatorColor = GoldAccent,
                unselectedIconColor = Color(0xFF90A4AE),
                unselectedTextColor = Color(0xFF90A4AE)
            ),
            modifier = Modifier.testTag("nav_item_settings")
        )
    }
}
