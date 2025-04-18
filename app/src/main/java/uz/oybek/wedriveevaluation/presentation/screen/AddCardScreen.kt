package uz.oybek.wedriveevaluation.presentation.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import kotlinx.coroutines.flow.collectLatest
import org.koin.androidx.compose.koinViewModel
import uz.oybek.wedriveevaluation.presentation.util.CardNumberVisualTransformation
import uz.oybek.wedriveevaluation.presentation.util.ExpiryDateVisualTransformation
import uz.oybek.wedriveevaluation.presentation.viewModel.AddCardEffect
import uz.oybek.wedriveevaluation.presentation.viewModel.AddCardViewModel
import uz.oybek.wedriveevaluation.ui.theme.ButtonDisabledBackground
import uz.oybek.wedriveevaluation.ui.theme.ButtonDisabledContent
import uz.oybek.wedriveevaluation.ui.theme.TextFieldBackgroundColor
import uz.oybek.wedriveevaluation.ui.theme.TextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddCardScreen(
    navController: NavController,
    viewModel: AddCardViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsState()


    LaunchedEffect(Unit) {
        viewModel.effect.collectLatest { effect ->
            when (effect) {
                is AddCardEffect.NavigateBack -> {
                    navController.previousBackStackEntry
                        ?.savedStateHandle
                        ?.set(CARD_ADDED_SUCCESS_KEY, effect.success)
                    navController.popBackStack()
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Add Card") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(TextFieldBackgroundColor)
                        .padding(horizontal = 1.dp, vertical = 1.dp)
                ) {
                    OutlinedTextField(
                        value = state.cardNumber,
                        onValueChange = viewModel::onCardNumberChanged,
                        placeholder = { Text("0000 0000 0000 0000", color = TextSecondary) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        visualTransformation = CardNumberVisualTransformation(),
                        singleLine = true,
                        isError = state.error?.contains("Karta raqami", ignoreCase = true) == true,
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !state.isLoading,
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
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(TextFieldBackgroundColor)
                        .padding(horizontal = 1.dp, vertical = 1.dp)
                ) {
                    OutlinedTextField(
                        value = state.expiryInput,
                        onValueChange = viewModel::onExpiryDateChanged,
                        placeholder = { Text("00/00", color = TextSecondary) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        visualTransformation = ExpiryDateVisualTransformation(),
                        singleLine = true,
                        isError = state.error?.contains("Amal qilish muddati", ignoreCase = true) == true,
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !state.isLoading,
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
                        modifier = Modifier.padding(top = 8.dp).align(Alignment.Start)
                    )
                }
            }

            Button(
                onClick = viewModel::saveCard,
                enabled = !state.isLoading && state.isInputPotentiallyValid,
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp).height(50.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    disabledContainerColor = ButtonDisabledBackground,
                    disabledContentColor = ButtonDisabledContent
                )
            ) {
                if (state.isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = LocalContentColor.current, strokeWidth = 2.dp)
                } else {
                    Text("Save", fontSize = 17.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}