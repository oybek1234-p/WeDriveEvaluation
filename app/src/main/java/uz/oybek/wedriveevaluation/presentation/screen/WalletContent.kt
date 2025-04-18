package uz.oybek.wedriveevaluation.presentation.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import uz.oybek.wedriveevaluation.R
import uz.oybek.wedriveevaluation.domain.model.PaymentMethod
import uz.oybek.wedriveevaluation.domain.model.WalletData
import uz.oybek.wedriveevaluation.ui.theme.ActionRowBackground
import uz.oybek.wedriveevaluation.ui.theme.BalanceCardBackground
import uz.oybek.wedriveevaluation.ui.theme.BalanceLabelColor
import uz.oybek.wedriveevaluation.ui.theme.ButtonDisabledBackground
import uz.oybek.wedriveevaluation.ui.theme.ButtonDisabledContent
import uz.oybek.wedriveevaluation.ui.theme.ChevronColor
import uz.oybek.wedriveevaluation.ui.theme.IconTint
import uz.oybek.wedriveevaluation.ui.theme.IdentificationWarning
import uz.oybek.wedriveevaluation.ui.theme.OnBalanceCard
import uz.oybek.wedriveevaluation.ui.theme.SwitchBorderColorUnchecked
import uz.oybek.wedriveevaluation.ui.theme.SwitchThumbColor
import uz.oybek.wedriveevaluation.ui.theme.SwitchTrackColorChecked
import uz.oybek.wedriveevaluation.ui.theme.SwitchTrackColorUnchecked
import uz.oybek.wedriveevaluation.ui.theme.TextPrimary
import uz.oybek.wedriveevaluation.ui.theme.TextSecondary

@Composable
fun WalletContent(
    walletData: WalletData,
    isLoading: Boolean,
    isUpdatingPayment: Boolean,
    generalError: String?,
    onPaymentMethodSelected: (PaymentMethod) -> Unit,
    onAddCard: () -> Unit,
    onAddPromoCode: () -> Unit,
    modifier: Modifier = Modifier
) {
    val firstCard = walletData.cards.firstOrNull()

    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        BalanceCard(balance = walletData.balanceFormatted)

        Spacer(modifier = Modifier.height(8.dp))

        ActionRow(iconResId = R.drawable.ic_identification,
            iconTint = IdentificationWarning,
            text = "Identification required",
            onClick = { })

        ActionRow(
            iconResId = R.drawable.ic_promo_code,
            iconTint = Color.Unspecified,
            text = "Add Promo code",
            onClick = onAddPromoCode
        )

        PaymentMethodSwitchRow(iconResId = R.drawable.ic_cash,
            text = "Cash",
            isChecked = walletData.activeMethod is PaymentMethod.Cash,
            isEnabled = !isUpdatingPayment,
            onCheckChanged = {
                if (walletData.activeMethod !is PaymentMethod.Cash) {
                    onPaymentMethodSelected(PaymentMethod.Cash)
                } else {
                    if (walletData.cards.isNotEmpty()) {
                        val card = walletData.cards.first()
                        onPaymentMethodSelected(
                            PaymentMethod.Card(
                                card.id, card.last4, card.displayNumber, card.expiryDate
                            )
                        )
                    }
                }
            })

        walletData.cards.forEach { card ->
            PaymentMethodSwitchRow(iconResId = R.drawable.ic_card,
                text = "Card ${card.displayNumber}",
                isChecked = walletData.activeMethod == card,
                isEnabled = !isUpdatingPayment, onCheckChanged = {
                    if (walletData.activeMethod != card) {
                        onPaymentMethodSelected(card)
                    }
                })
        }
        if (isUpdatingPayment) {
            LinearProgressIndicator(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(2.dp)
                    .padding(vertical = 4.dp)
            )
        }

        ActionRow(
            iconResId = R.drawable.ic_add_card,
            iconTint = Color.Unspecified,
            text = "Add new card", onClick = onAddCard
        )

        generalError?.let {
            Text(
                text = it,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(top = 8.dp, start = 4.dp)
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
fun BalanceCard(balance: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = BalanceCardBackground)
    ) {
        Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp)) {
            Text("Balance", style = MaterialTheme.typography.labelMedium, color = BalanceLabelColor)
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = balance, style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.SemiBold, fontSize = 30.sp
                ), color = OnBalanceCard
            )
        }
    }
}

@Composable
fun ActionRow(
    iconResId: Int, iconTint: Color, text: String, onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        color = ActionRowBackground
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    painter = painterResource(id = iconResId),
                    contentDescription = text,
                    tint = iconTint,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(16.dp))
                Text(text = text, style = MaterialTheme.typography.bodyLarge, color = TextPrimary)
            }
            Icon(
                imageVector = Icons.Filled.KeyboardArrowRight,
                contentDescription = "Go",
                tint = ChevronColor
            )
        }
    }
}

@Composable
fun PaymentMethodSwitchRow(
    iconResId: Int,
    text: String,
    isChecked: Boolean,
    isEnabled: Boolean,
    onCheckChanged: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(enabled = isEnabled, onClick = onCheckChanged),
        shape = RoundedCornerShape(12.dp), color = ActionRowBackground
    ) {
        Row(
            modifier = Modifier.padding(start = 16.dp, end = 10.dp, top = 12.dp, bottom = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                Icon(
                    painter = painterResource(id = iconResId),
                    contentDescription = text,
                    tint = Color.Unspecified,
                    modifier = Modifier.size(28.dp)
                )
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = text,
                    style = MaterialTheme.typography.bodyLarge,
                    color = TextPrimary,
                    modifier = Modifier.padding(end = 8.dp)
                )
            }
            Switch(
                checked = isChecked,
                onCheckedChange = null,
                enabled = isEnabled,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = SwitchThumbColor,
                    checkedTrackColor = SwitchTrackColorChecked,
                    checkedBorderColor = SwitchTrackColorChecked,
                    uncheckedThumbColor = SwitchThumbColor,
                    uncheckedTrackColor = SwitchTrackColorUnchecked,
                    uncheckedBorderColor = SwitchBorderColorUnchecked
                ),
                modifier = Modifier.padding(start = 8.dp)
            )
        }
    }
}

@Composable
fun PromoCodeSheetContent(
    code: String,
    error: String?,
    isLoading: Boolean,
    onCodeChange: (String) -> Unit,
    onApply: () -> Unit,
    onDismiss: () -> Unit
) {
    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current

    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(100)
        focusRequester.requestFocus()
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            IconButton(onClick = onDismiss, modifier = Modifier.align(Alignment.CenterVertically)) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
            }
            Text(
                "Enter Promo code",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 48.dp),
                textAlign = TextAlign.Center
            )
        }

        OutlinedTextField(
            value = code,
            onValueChange = onCodeChange,
            placeholder = { Text("Enter code here", color = TextSecondary) },
            modifier = Modifier
                .fillMaxWidth()
                .focusRequester(focusRequester),
            isError = error != null,
            singleLine = true,
            enabled = !isLoading,
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Characters, imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus(); onApply() }),
            shape = RoundedCornerShape(12.dp)
        )
        error?.let {
            Text(
                text = it,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier
                    .padding(horizontal = 8.dp)
                    .fillMaxWidth()
            )
        }

        Spacer(Modifier.height(10.dp))

        Button(
            onClick = { focusManager.clearFocus(); onApply() },
            enabled = !isLoading && code.isNotBlank(),
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                disabledContainerColor = ButtonDisabledBackground,
                disabledContentColor = ButtonDisabledContent
            )
        ) {
            if (isLoading) CircularProgressIndicator(
                Modifier.size(24.dp), color = LocalContentColor.current, strokeWidth = 2.dp
            )
            else Text("Save", fontWeight = FontWeight.SemiBold, fontSize = 17.sp)
        }
    }
}

@Composable
fun ErrorState(message: String, onRetry: () -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            Icons.Filled.Info,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.error,
            modifier = Modifier.size(48.dp)
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = message,
            color = MaterialTheme.colorScheme.error,
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.bodyLarge
        )
        Spacer(Modifier.height(16.dp))
        Button(onClick = onRetry) { Text("Qayta urinish") }
    }
}