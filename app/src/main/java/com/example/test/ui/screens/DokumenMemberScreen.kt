package com.example.test.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.test.ui.dataType.Branch
import com.example.test.ui.dataType.Member
import com.example.test.ui.viewModels.MemberViewModel



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DokumenMemberScreen(
    navController: NavHostController,
    paddingValues: PaddingValues,
    userId: String,
    memberViewModel: MemberViewModel = viewModel()
) {
    var isLoading by remember { mutableStateOf(true) }
    var member by remember { mutableStateOf<Member?>(null) }
    var branch by remember { mutableStateOf<Branch?>(null) }

    LaunchedEffect(userId) {
        memberViewModel.fetchMember(userId) { fetchedMember, fetchedBranch ->
            member = fetchedMember
            branch = fetchedBranch
            isLoading = false
        }
    }
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Dokumen Member") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                        )
                    }
                },
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    top = innerPadding.calculateTopPadding(),
                    bottom = innerPadding.calculateBottomPadding(),
                    start = 16.dp,
                    end = 16.dp
                )
                .verticalScroll(rememberScrollState())
        ) {

        }
        }
}