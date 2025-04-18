package uz.oybek.wedriveevaluation.presentation.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.flow.collectLatest
import org.koin.androidx.compose.koinViewModel
import uz.oybek.wedriveevaluation.presentation.viewModel.PhoneEntryEffect
import uz.oybek.wedriveevaluation.presentation.viewModel.PhoneEntryViewModel
import uz.oybek.wedriveevaluation.ui.theme.ActionRowBackground
import uz.oybek.wedriveevaluation.ui.theme.ButtonDisabledBackground
import uz.oybek.wedriveevaluation.ui.theme.ButtonDisabledContent
import uz.oybek.wedriveevaluation.ui.theme.TextFieldBackgroundColor
import uz.oybek.wedriveevaluation.ui.theme.TextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PhoneEntryScreen(
    viewModel: PhoneEntryViewModel = koinViewModel(),
    onNavigateToWallet: () -> Unit
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.effect.collectLatest { effect ->
            if (effect is PhoneEntryEffect.NavigateToWallet) {
                onNavigateToWallet()
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Telefon raqamni kiriting") },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp, vertical = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Phone,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(48.dp)
                )

                Text(
                    "Davom etish uchun telefon raqamingizni kiriting",
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(ActionRowBackground)
                        .padding(horizontal = 1.dp, vertical = 1.dp)
                ) {
                    OutlinedTextField(
                        value = state.phoneNumber,
                        onValueChange = viewModel::onPhoneChanged,
                        label = { Text("Telefon raqam (+998..)") },
                        placeholder = { Text("+998 90 123 45 67", color = TextSecondary) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        singleLine = true,
                        isError = state.error != null,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(11.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = TextFieldBackgroundColor,
                            unfocusedContainerColor = TextFieldBackgroundColor,
                            disabledContainerColor = TextFieldBackgroundColor,
                            focusedBorderColor = Color.Transparent,
                            unfocusedBorderColor = Color.Transparent
                        )
                    )
                }

                state.error?.let {
                    Text(
                        text = it,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(top = 4.dp, start = 4.dp).align(Alignment.Start)
                    )
                }
            }

            Button(
                onClick = viewModel::submitPhone,
                enabled = !state.isLoading,
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp).height(50.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    disabledContainerColor = ButtonDisabledBackground,
                    disabledContentColor = ButtonDisabledContent
                )
            ) {
                if (state.isLoading) {
                    CircularProgressIndicator(Modifier.size(24.dp), color = LocalContentColor.current, strokeWidth = 2.dp)
                } else {
                    Text("Yuborish", fontSize = 17.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}