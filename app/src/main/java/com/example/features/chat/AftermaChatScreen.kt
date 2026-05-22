package com.example.features.chat

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import com.example.ui.viewmodel.MainViewModel
import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.GenerativeBackend
import com.google.firebase.ai.type.content
import androidx.compose.ui.draw.shadow
import androidx.compose.foundation.BorderStroke
import kotlinx.coroutines.launch

data class ChatMessage(
    val text: String,
    val isUser: Boolean,
    val timestamp: Long = System.currentTimeMillis()
)

private const val SYSTEM_PROMPT = """
You are **Afterma**, a compassionate and clinically-informed maternal wellness companion built for mothers navigating the postpartum period, pediatric milestones, therapy journeys, and safe motherhood. You were created by the Afterma team.

---

## Your Core Identity

- You speak with warmth, empathy, and gentle authority — like a trusted women's health nurse who also happens to be a close friend.
- You never minimise a mother's experience. Pain, exhaustion, confusion, grief, and joy all deserve equal acknowledgment.
- You are trauma-informed and sensitive to the diversity of birth experiences — vaginal, caesarean, loss, NICU stays, adoption, surrogacy, and more.
- You are culturally inclusive and avoid assumptions about family structures, economic backgrounds, or traditions.

---

## What You Help With

1. **Postpartum Physical Recovery** — wound care, lochia, breastfeeding, pelvic floor, sleep deprivation, nutrition
2. **Postpartum Mental Health** — baby blues vs. PPD, postpartum anxiety (PPA), postpartum OCD, postpartum psychosis (know the warning signs)
3. **Infant & Pediatric Support** — feeding (breast and formula), sleep schedules, developmental milestones, common illnesses
4. **Therapy Journeys** — helping mothers understand their therapy options, destigmatising seeking help, between-session coping strategies
5. **Safe Motherhood** — maternal health rights, warning signs to escalate immediately, navigating the healthcare system
6. **Partner & Family Dynamics** — communicating needs, relationship shifts after birth, grandparent boundaries

---

## How You Respond (Your Communication Rules)

1. **Empathy First**: Before offering any information or guidance, validate the mother's feelings. Use phrases like, "It is completely understandable to feel overwhelmed by this," or "I hear how exhausting that must be."
2. **Clinical-Grade Safe Boundaries**:
   - You are a companion, NOT a doctor. You never diagnose.
   - For physical symptoms (fever, bleeding, pain), explain why it happens and outline common care practices, but always include: "Please check in with your midwife, OB/GYN, or GP to make sure you're healing safely."
   - For medication questions, explain general drug purposes and safety categories (e.g., compatibility with breastfeeding), but never prescribe or suggest dosages.
3. **Trauma-Informed & Non-Judgmental**:
   - Never say "at least you have a healthy baby."
   - Avoid toxic positivity. Meet them where they are.
   - Respect feeding choices. Breastfeeding, pumping, formula feeding, and mixed feeding are all valid.
4. **No Jargon**: Explain medical terms (like lochia, involution, perineal care) in plain, gentle language.
5. **Structure**: Keep answers digestible. Use bullet points and bold text so an exhausted parent can read them in 5 seconds.

---

## Crisis Escalation & Red Lines

If a mother indicates any of the following, you must provide immediate crisis helpline info in a clear, highlighted block at the very top of your response:

1. Postpartum Psychosis / Severe PPD: Expressing thoughts of harming herself or the baby, hearing voices, extreme paranoia, feeling completely detached from reality.
2. Domestic Abuse: Expressing fear of her partner or feeling unsafe at home.
3. Severe Medical Danger: Mentioning heavy bleeding (soaking a pad in an hour), high fever (over 100.4°F/38°C), severe chest pain, or difficulty breathing.

Helpline Block Format:
> If you or your baby are in immediate physical danger, please call your local emergency services (like 911 or 112) or go to the nearest emergency room.
> - National Maternal Mental Health Hotline (US): Call or text 1-833-TLC-MAMA (1-833-852-6262) for free, confidential, 24/7 support.
> - Crisis Text Line: Text HOME to 741741 to connect with a crisis counselor 24/7.
> - Domestic Violence Hotline: Call 1-800-799-SAFE (7233) or text START to 88788.
> - International Help: If you are outside the US, please visit findahelpline.com to find immediate support in your country.

---

## Tone Examples

❌ Don't say:
> "It sounds like you may have postpartum depression. You should see a therapist."

✅ Do say:
> "What you're feeling sounds really hard, and I want you to know it makes complete sense given everything your mind and body have been through. A lot of mothers experience similar waves of sadness or numbness after birth — it doesn't mean you're broken or a bad mum. It might be worth bringing this up with your doctor, even just to have a conversation. Would it help if I explained what they might ask you and what options exist?"

---

## Formatting Rules

- Use bold for key terms or action items.
- Use bullet points for lists of symptoms, tips, or steps.
- Use headers (##) only for responses longer than ~200 words.
- Respond in the same language the user writes in.
- Keep the tone conversational — avoid sounding like a medical leaflet.

---

## What You Do Not Do

- You do not discuss topics unrelated to maternal, infant, or family wellness (politics, sports, coding, general trivia, etc.). Politely redirect: "I'm here specifically to support you through your motherhood journey — is there something in that space I can help you with?"
- You do not share personal opinions on parenting philosophies (attachment parenting vs. Ferber, breastfeeding vs. formula) beyond presenting evidence. You respect the mother's autonomy completely.
- You do not store or ask for personally identifiable information.

---

You are Afterma. Every mother who reaches out to you matters. Hold that with care.
"""

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AftermaChatScreen(
    viewModel: MainViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()
    val listState = rememberLazyListState()
    val keyboardController = LocalSoftwareKeyboardController.current

    // Chat messages state
    val messages = remember {
        mutableStateListOf<ChatMessage>().apply {
            add(
                ChatMessage(
                    text = "Hello, mama. I am Afterma, your maternal wellness companion. I am here to hold space for you, offer clinical-grade postpartum guidance, and support you through this transition. What is on your heart today?",
                    isUser = false
                )
            )
        }
    }

    var inputText by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var showCrisisBanner by remember { mutableStateOf(false) }

    // Initialize Gemini model
    val generativeModel = remember {
        Firebase.ai(backend = GenerativeBackend.vertexAI())
            .generativeModel(
                modelName = "gemini-1.5-flash",
                systemInstruction = content { text(SYSTEM_PROMPT) }
            )
    }

    // Scroll to bottom when list size changes or typing status changes
    LaunchedEffect(messages.size, isLoading) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    // Crisis keywords to intercept client-side
    val crisisKeywords = remember {
        listOf(
            "heavy bleeding", "high fever", "thoughts of self-harm",
            "postpartum psychosis", "suicide", "chest pain",
            "self harm", "psychosis", "kill myself", "harm myself",
            "bleeding heavily", "severe bleeding", "high temperature"
        )
    }

    val checkCrisisText = { text: String ->
        val lowercaseText = text.lowercase()
        crisisKeywords.any { keyword -> lowercaseText.contains(keyword) }
    }

    val handleSendMessage = {
        val query = inputText.trim()
        if (query.isNotEmpty() && !isLoading) {
            inputText = ""
            keyboardController?.hide()
            messages.add(ChatMessage(text = query, isUser = true))

            // Check for crisis terms
            if (checkCrisisText(query)) {
                showCrisisBanner = true
            }

            isLoading = true
            coroutineScope.launch {
                try {
                    val response = generativeModel.generateContent(query)
                    val responseText = response.text ?: "I am having trouble responding right now. Please try again, mama."
                    messages.add(ChatMessage(text = responseText, isUser = false))
                } catch (e: Exception) {
                    messages.add(
                        ChatMessage(
                            text = "I encountered an issue connecting. Please ensure your device is connected to the internet, or try again in a moment.",
                            isUser = false
                        )
                    )
                } finally {
                    isLoading = false
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Filled.AutoAwesome,
                            contentDescription = null,
                            tint = PrimaryPink,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Afterma AI Guide",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryText
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = PrimaryPink
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = SurfaceWhite
                ),
                modifier = Modifier.border(0.dp, Color.Transparent).shadow(elevation = 1.dp)
            )
        },
        containerColor = Background,
        modifier = modifier.fillMaxSize().testTag("afterma_chat_screen")
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Emergency Crisis Banner if triggered
            AnimatedVisibility(
                visible = showCrisisBanner,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                CrisisWarningBanner(
                    onDismiss = { showCrisisBanner = false },
                    modifier = Modifier.testTag("crisis_warning_banner")
                )
            }

            // Message list
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                contentPadding = PaddingValues(top = 16.dp, bottom = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(messages) { message ->
                    ChatBubble(message = message)
                }

                if (isLoading) {
                    item {
                        TypingIndicator()
                    }
                }
            }

            // Input panel
            Surface(
                color = SurfaceWhite,
                modifier = Modifier.fillMaxWidth().shadow(4.dp),
                tonalElevation = 2.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                        .navigationBarsPadding()
                        .imePadding(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = inputText,
                        onValueChange = { inputText = it },
                        placeholder = {
                            Text(
                                text = "Ask Afterma...",
                                color = MutedText,
                                fontSize = 14.sp
                            )
                        },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("chat_input_field"),
                        maxLines = 4,
                        shape = RoundedCornerShape(24.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PrimaryPink,
                            unfocusedBorderColor = Border,
                            focusedContainerColor = Background,
                            unfocusedContainerColor = Background
                        ),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Text,
                            imeAction = ImeAction.Send
                        ),
                        keyboardActions = KeyboardActions(
                            onSend = { handleSendMessage() }
                        )
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    FloatingActionButton(
                        onClick = { handleSendMessage() },
                        containerColor = PrimaryPink,
                        contentColor = Color.White,
                        shape = CircleShape,
                        modifier = Modifier
                            .size(48.dp)
                            .testTag("chat_send_button")
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Send,
                            contentDescription = "Send Message",
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CrisisWarningBanner(
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFFFF0F0)
        ),
        border = BorderStroke(1.5.dp, ErrorColor),
        shape = RoundedCornerShape(16.dp),
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp)
            .shadow(2.dp, shape = RoundedCornerShape(16.dp))
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.Warning,
                        contentDescription = "Warning",
                        tint = ErrorColor,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "EMERGENCY SAFETY WARNING",
                        fontWeight = FontWeight.Bold,
                        color = ErrorColor,
                        fontSize = 14.sp,
                        letterSpacing = 0.5.sp
                    )
                }
                Text(
                    text = "Dismiss",
                    color = SecondaryText,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier
                        .clickable { onDismiss() }
                        .padding(4.dp)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "If you or your baby are in immediate physical danger, please call your local emergency services (like 911 or 112) or go to the nearest emergency room.",
                color = PrimaryText,
                fontWeight = FontWeight.SemiBold,
                fontSize = 13.sp,
                lineHeight = 18.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Emergency Resources:",
                fontWeight = FontWeight.Bold,
                color = PrimaryText,
                fontSize = 12.sp
            )

            Spacer(modifier = Modifier.height(4.dp))

            val bullet = "•"
            Text(
                text = "$bullet National Maternal Mental Health Hotline: Call/text 1-833-TLC-MAMA (1-833-852-6262)",
                fontSize = 12.sp,
                color = PrimaryText,
                lineHeight = 16.sp
            )
            Text(
                text = "$bullet Crisis Text Line: Text HOME to 741741",
                fontSize = 12.sp,
                color = PrimaryText,
                lineHeight = 16.sp
            )
            Text(
                text = "$bullet Domestic Violence Hotline: Call 1-800-799-SAFE (7233)",
                fontSize = 12.sp,
                color = PrimaryText,
                lineHeight = 16.sp
            )
            Text(
                text = "$bullet India Helpline Fallback: 9152987821",
                fontSize = 12.sp,
                color = PrimaryText,
                lineHeight = 16.sp
            )
            Text(
                text = "$bullet International Help: Visit findahelpline.com",
                fontSize = 12.sp,
                color = PrimaryText,
                lineHeight = 16.sp
            )
        }
    }
}

@Composable
fun ChatBubble(message: ChatMessage) {
    val bubbleShape = if (message.isUser) {
        RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomStart = 16.dp, bottomEnd = 4.dp)
    } else {
        RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomStart = 4.dp, bottomEnd = 16.dp)
    }

    val align = if (message.isUser) Alignment.End else Alignment.Start
    val bubbleColor = if (message.isUser) PrimaryPink else SoftPink
    val textColor = if (message.isUser) Color.White else PrimaryText

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = align
    ) {
        Box(
            modifier = Modifier
                .widthIn(max = 280.dp)
                .clip(bubbleShape)
                .background(bubbleColor)
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            if (message.isUser) {
                Text(
                    text = message.text,
                    color = textColor,
                    fontSize = 14.sp,
                    lineHeight = 20.sp
                )
            } else {
                FormattedMessageText(text = message.text, color = textColor)
            }
        }
    }
}

@Composable
fun FormattedMessageText(text: String, color: Color) {
    val annotatedString = remember(text) {
        parseMarkdown(text)
    }
    Text(
        text = annotatedString,
        color = color,
        fontSize = 14.sp,
        lineHeight = 20.sp
    )
}

/**
 * A basic parser to handle bold (**text**) and lists/headers for clean maternal rendering.
 */
fun parseMarkdown(text: String): AnnotatedString {
    return buildAnnotatedString {
        var cursor = 0
        while (cursor < text.length) {
            val nextBoldStart = text.indexOf("**", cursor)
            if (nextBoldStart == -1) {
                append(text.substring(cursor))
                break
            }
            append(text.substring(cursor, nextBoldStart))
            val nextBoldEnd = text.indexOf("**", nextBoldStart + 2)
            if (nextBoldEnd == -1) {
                append(text.substring(nextBoldStart))
                break
            }
            withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                append(text.substring(nextBoldStart + 2, nextBoldEnd))
            }
            cursor = nextBoldEnd + 2
        }
    }
}

@Composable
fun TypingIndicator() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 8.dp),
        horizontalArrangement = Arrangement.Start
    ) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .background(SoftPink)
                .padding(horizontal = 14.dp, vertical = 10.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "Afterma is typing",
                    fontSize = 12.sp,
                    color = DeepPink,
                    fontWeight = FontWeight.Medium
                )
                DotAnimation()
            }
        }
    }
}

@Composable
fun DotAnimation() {
    // Simple three dots indicator animation
    var dotCount by remember { mutableStateOf(1) }
    LaunchedEffect(Unit) {
        while (true) {
            kotlinx.coroutines.delay(500)
            dotCount = (dotCount % 3) + 1
        }
    }
    Text(
        text = ".".repeat(dotCount),
        fontSize = 12.sp,
        color = DeepPink,
        fontWeight = FontWeight.Bold
    )
}
