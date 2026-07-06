package com.qpeyba.surf_slop_summer_school_2026.ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.qpeyba.surf_slop_summer_school_2026.ui.theme.Card

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun PhotoCarousel(
    photoUrls: List<String>,
    modifier: Modifier = Modifier,
    height: Dp = 200.dp
) {
    if (photoUrls.isEmpty()) return

    val pagerState = rememberPagerState(pageCount = { photoUrls.size })

    Box(modifier = modifier.fillMaxWidth().height(height)) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxWidth().height(height)
        ) { page ->
            AsyncImage(
                model = photoUrls[page],
                contentDescription = "Фото ${page + 1}",
                modifier = Modifier.fillMaxWidth().height(height),
                contentScale = ContentScale.Crop
            )
        }

        if (photoUrls.size > 1) {
            Box(
                modifier = Modifier.align(Alignment.BottomCenter),
                contentAlignment = Alignment.Center
            ) {
                repeat(photoUrls.size) { index ->
                    Surface(
                        modifier = Modifier,
                        shape = CircleShape,
                        color = if (index == pagerState.currentPage) Card else Card.copy(alpha = 0.5f)
                    ) {}
                }
            }
        }
    }
}
