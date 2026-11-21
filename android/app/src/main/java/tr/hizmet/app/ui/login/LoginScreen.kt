package tr.hizmet.app.ui.login

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.EventAvailable
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.delay
import tr.hizmet.app.R
import tr.hizmet.app.ui.components.PrimaryButton
import tr.hizmet.app.ui.currentLocale
import tr.hizmet.app.ui.theme.Border
import tr.hizmet.app.ui.theme.Field
import tr.hizmet.app.ui.theme.Muted
import tr.hizmet.app.ui.theme.Purple
import tr.hizmet.app.ui.toggleLanguage

@Composable
fun LoginScreen(viewModel: LoginViewModel = viewModel(factory = LoginViewModel.Factory)) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val codeFocus = remember { FocusRequester() }

    // Countdown for "Resend code: 0:42"
    LaunchedEffect(state.codeSent, state.resendSeconds) {
        if (state.codeSent && state.resendSeconds > 0) {
            delay(1000)
            viewModel.tick()
        }
    }
    LaunchedEffect(state.codeSent) {
        if (state.codeSent) codeFocus.requestFocus()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .systemBarsPadding()
            .imePadding()
            .padding(horizontal = 20.dp),
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState()),
        ) {
            Spacer(Modifier.height(16.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Purple),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(Icons.Outlined.EventAvailable, contentDescription = null, tint = Color.White)
                }
                Spacer(Modifier.weight(1f))
                LanguageChip()
            }

            Spacer(Modifier.height(28.dp))
            Text(stringResource(R.string.login_title), style = MaterialTheme.typography.headlineSmall)
            Spacer(Modifier.height(6.dp))
            Text(stringResource(R.string.login_subtitle), style = MaterialTheme.typography.bodySmall)

            Spacer(Modifier.height(24.dp))
            FieldLabel(stringResource(R.string.phone_number))
            PhoneField(
                digits = state.phoneDigits,
                enabled = !state.codeSent,
                onChange = viewModel::onPhoneChange,
                onEdit = viewModel::editPhone,
            )
            if (state.error == LoginError.PHONE) ErrorText(stringResource(R.string.error_phone))

            if (state.codeSent) {
                Spacer(Modifier.height(24.dp))
                FieldLabel(stringResource(R.string.sms_code))
                CodeField(
                    code = state.code,
                    onChange = viewModel::onCodeChange,
                    modifier = Modifier.focusRequester(codeFocus),
                )
                Spacer(Modifier.height(10.dp))
                if (state.resendSeconds > 0) {
                    val time = "%d:%02d".format(state.resendSeconds / 60, state.resendSeconds % 60)
                    Text(stringResource(R.string.resend_code_in, time), style = MaterialTheme.typography.bodySmall)
                } else {
                    TextButton(onClick = viewModel::sendCode) { Text(stringResource(R.string.resend_code)) }
                }
                Text(stringResource(R.string.demo_code_hint), style = MaterialTheme.typography.bodySmall)
            }
            when (state.error) {
                LoginError.CODE -> ErrorText(stringResource(R.string.error_code))
                LoginError.NETWORK -> ErrorText(stringResource(R.string.error_network))
                else -> Unit
            }
        }

        PrimaryButton(
            text = stringResource(if (state.codeSent) R.string.sign_in else R.string.send_code),
            onClick = if (state.codeSent) viewModel::signIn else viewModel::sendCode,
            loading = state.loading,
            enabled = if (state.codeSent) state.code.length == LoginViewModel.CODE_LENGTH else state.phoneDigits.isNotEmpty(),
        )
        Text(
            text = stringResource(R.string.terms_notice),
            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
        )
    }
}

@Composable
private fun LanguageChip() {
    val language = currentLocale().language
    TextButton(onClick = { toggleLanguage(language) }) {
        Icon(Icons.Outlined.Language, contentDescription = null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.onSurface)
        Spacer(Modifier.width(4.dp))
        Text(language.uppercase(), color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun FieldLabel(text: String) {
    Text(text, style = MaterialTheme.typography.labelSmall, color = Muted, modifier = Modifier.padding(bottom = 8.dp))
}

@Composable
private fun ErrorText(text: String) {
    Text(text, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.error), modifier = Modifier.padding(top = 8.dp))
}

@Composable
private fun PhoneField(digits: String, enabled: Boolean, onChange: (String) -> Unit, onEdit: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Field)
            .padding(horizontal = 14.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text("+90", style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.width(12.dp))
        BasicTextField(
            value = digits,
            onValueChange = onChange,
            enabled = enabled,
            singleLine = true,
            textStyle = TextStyle(fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurface),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
            visualTransformation = PhoneTransformation,
            modifier = Modifier
                .weight(1f)
                .padding(vertical = 12.dp),
            decorationBox = { inner ->
                if (digits.isEmpty()) Text("532 123 45 67", color = Muted, fontSize = 16.sp)
                inner()
            },
        )
        if (!enabled) {
            TextButton(onClick = onEdit) { Text(stringResource(R.string.change), color = Purple) }
        }
    }
}

/** Shows "5321234567" as "532 123 45 67". */
private object PhoneTransformation : VisualTransformation {
    private val spacesAfter = setOf(3, 6, 8)

    override fun filter(text: AnnotatedString): TransformedText {
        val out = StringBuilder()
        text.text.forEachIndexed { i, c ->
            if (i in spacesAfter) out.append(' ')
            out.append(c)
        }
        val mapping = object : OffsetMapping {
            override fun originalToTransformed(offset: Int) = offset + spacesAfter.count { it < offset }
            override fun transformedToOriginal(offset: Int): Int {
                var original = offset
                for (s in spacesAfter) if (offset > s + spacesAfter.count { it < s }) original--
                return original.coerceIn(0, text.length)
            }
        }
        return TransformedText(AnnotatedString(out.toString()), mapping)
    }
}

/** One hidden text field drawn as separate boxes, one per digit. */
@Composable
private fun CodeField(code: String, onChange: (String) -> Unit, modifier: Modifier = Modifier) {
    BasicTextField(
        value = code,
        onValueChange = onChange,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
        modifier = modifier,
        decorationBox = {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                repeat(LoginViewModel.CODE_LENGTH) { index ->
                    val isCurrent = index == code.length
                    Box(
                        modifier = Modifier
                            .size(width = 52.dp, height = 56.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isCurrent) Color.White else Field)
                            .border(
                                width = if (isCurrent) 1.5.dp else 1.dp,
                                color = if (isCurrent) Purple else Border,
                                shape = RoundedCornerShape(12.dp),
                            ),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = code.getOrNull(index)?.toString() ?: "",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                }
            }
        },
    )
}
