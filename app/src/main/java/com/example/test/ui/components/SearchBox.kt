package com.example.test.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldColors
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material.LocalContentAlpha
import com.algolia.instantsearch.compose.R
import com.algolia.instantsearch.compose.searchbox.SearchBoxState

//Hit(
//objectID=34mVWoep3yt8ZpbEMDEX,
//highlightResult= {
//    author = MapOfkotlinStringHighlightResultValue(
//        value={
//            name=HighlightResultOption(
//                value="",
//                matchLevel=full,
//                matchedWords=[gri],
//                fullyHighlighted=false)
//        }
//    ),
//    title = HighlightResultOption(
//        value = "",
//        matchLevel=full,
//        matchedWords=[gri],
//        fullyHighlighted=false
//    ),
//    content = ListOfHighlightResultValue(
//        value=[
//            MapOfkotlinStringHighlightResultValue(
//                value={
//                    text = HighlightResultOption(
//                        value="",
//                    matchLevel=full,
//                    matchedWords=[gri],
//                    fullyHighlighted=false
//                    )
//                }
//            ),
//
//        ]
//    )
//                 },
//snippetResult=null,
//rankingInfo=null,
//distinctSeqID=null,
//additionalProperties={
//    comments=[]
//},
//featured=false,
//author={
//    "profileComplete":false,
//    "createdAt":1741387473973,
//    "uid":"cw1TuSoGAeUrCH6ajfiafeKg5bR2",
//    "lastSeen":1741387473973,
//    "role":"user",
//    "profilePicUrl":"",
//    "phone":"",
//    "name":"Redaksi Grib",
//    "online":false},
//title="Hercules Bakal Siapkan 10 Ribu Kader GRIB Jaya Sambut Pelantikan Prabowo",
//content=[
//{
//    "text":
//    ""
//},
//{"text":
//    ""
//},
//]