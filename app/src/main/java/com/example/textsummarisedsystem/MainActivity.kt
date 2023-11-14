package com.example.textsummarisedsystem

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.textsummarisedsystem.ui.theme.Pink80
import com.example.textsummarisedsystem.ui.theme.Purple40
import com.example.textsummarisedsystem.ui.theme.TextSummarisedSystemTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.serialization.json.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TextSummarisedSystemTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    TextSummarisedApp()
                }
            }
        }
    }
}



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TextSummarisedApp() {
    Column(modifier = Modifier
        .fillMaxSize()
        .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center) {
        var inputText by remember { mutableStateOf("") }
        var summaryText by remember { mutableStateOf("") }
        var length = "medium"

        Text(text = "TEXT SUMMARIZATION",
            modifier = Modifier.padding(10.dp),
            fontSize = 30.sp,
            fontWeight = FontWeight.Bold,
            color = Purple40)

        OutlinedTextField(
            modifier = Modifier
                .padding(10.dp)
                .height(200.dp),
            value = inputText,
            onValueChange = { inputText = it },
            placeholder = { Text(text = "Enter the text to be summarized...")}
        )

        Text(text ="How long do you want your summarised text to be:")

        Row {
            val interactionSource = remember{ MutableInteractionSource() }
            val isPressed by interactionSource.collectIsPressedAsState()
            // Use the state to change the background color
            val color = if (isPressed) Color(0xFFEFB8C8) else Color(0xFF6650a4)
            Button(onClick = {
                length = "short"
            },
                modifier = Modifier.padding(5.dp),
                interactionSource = interactionSource,
                colors= ButtonDefaults.buttonColors(color)
            ) {
                Text(text = "short")
            }
            Button(onClick = {
                length = "medium"
            },
                modifier = Modifier.padding(5.dp),
                interactionSource = interactionSource,
                colors= ButtonDefaults.buttonColors(color)) {
                Text(text = "medium")
            }
            Button(onClick = {
                length = "long"
            },
                modifier = Modifier.padding(5.dp),
                interactionSource = interactionSource,
                colors= ButtonDefaults.buttonColors(color)) {
                Text(text = "long")
            }
        }


        Button(
            onClick = {
                // Define your OkHttpClient and request here as shown in your original code

                // Execute the network request in a background thread
                GlobalScope.launch(Dispatchers.IO) {
                    try {
                        val client = OkHttpClient()

                        val mediaType = "application/json".toMediaType()
//                    val body =  RequestBody.create(mediaType, "{\"length\":\"length\",\"format\":\"paragraph\",\"model\":\"command\",\"extractiveness\":\"low\",\"temperature\":0,\"text\":\"inputText\"}")
                        val body = RequestBody.create(
                            mediaType,
                            "{\"length\":\"$length\",\"format\":\"paragraph\",\"model\":\"command\",\"extractiveness\":\"high\",\"temperature\":0,\"text\":\"$inputText\"}"
                        )
                        val request = Request.Builder()
                            .url("https://api.cohere.ai/v1/summarize")
                            .post(body)
                            .addHeader("accept", "application/json")
                            .addHeader("content-type", "application/json")
                            .addHeader("authorization", "Bearer YyTWJKbHicvx8yT4Hd1twdP9zWW91B9cOzfctDUG")
                            .build()

                        val response = client.newCall(request).execute()
                        val responseBody = response.body?.string()

                        if (response.isSuccessful) {
                            // Parse the JSON response and extract the summary
                            val json = responseBody?.let { Json { ignoreUnknownKeys = true }.parseToJsonElement(it) }
                            val summaryJson = json?.jsonObject?.get("summary")
                            // Update the summaryText with the response from the API
                            // Update the summaryText with the extracted summary
                            summaryText = summaryJson?.jsonPrimitive?.content ?: "No summary found in the response"
//                            summaryText = responseBody ?: "No response data"
                        }
                        else {
                            // Handle the errorwwwwq
                            summaryText = "Error: ${response.code} - ${response.message}\n This text is too short to be summarised.\n" +
                                    "The format of your text is wrong.\n Do not have any line- breaks!!"
                        }
                    }
                    catch (e:Exception){
                        summaryText = "Error: ${e.message}"
                    }
                }
                // Remember to cancel the job if the composable is removed
//                DisposableEffect(Unit) {
//                    onDispose {
//                        job.cancel()
//                    }
//                }
            },
            modifier = Modifier.padding(10.dp),
            colors = ButtonDefaults.buttonColors(Purple40)
        ) {
            Text("Summarize")
        }
        Text(summaryText)
    }
}



@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    TextSummarisedSystemTheme {
        TextSummarisedApp()
    }
}