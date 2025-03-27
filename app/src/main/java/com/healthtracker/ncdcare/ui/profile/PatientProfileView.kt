package com.healthtracker.ncdcare.ui.profile

import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults.topAppBarColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.healthtracker.ncdcare.R
import com.healthtracker.ncdcare.ui.theme.AppTheme
import org.hl7.fhir.r4.model.Enumerations
import org.hl7.fhir.r4.model.HumanName
import org.hl7.fhir.r4.model.Identifier
import org.hl7.fhir.r4.model.Patient
import org.hl7.fhir.r4.model.StringType
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.Period
import java.time.ZoneId
import java.util.Date

class PatientProfileView : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val patient = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getSerializableExtra(EXTRA_NAME, Patient::class.java)
        } else {
            @Suppress("DEPRECATION")
            (intent.getSerializableExtra(EXTRA_NAME) as? Patient)
        }
        setContent {
            AppTheme(darkTheme = false) {
                ProfileScaffold(patient, onBackPressed = {
                    finish()
                })
            }
        }
        val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                Log.d("PatientProfileView", "Back pressed")
                finish()
            }
        }
        onBackPressedDispatcher.addCallback(this, callback)
    }

    companion object {
        const val EXTRA_NAME = "extra_name"
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScaffold(patient: Patient?, onBackPressed: () -> Unit) {
    AppTheme(darkTheme = false, dynamicColor = false) {
        Scaffold(
            topBar = {
                TopAppBar(
                    colors = topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        titleContentColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    title = { Text(text = "${patient?.name?.firstOrNull()?.given?.firstOrNull()}") },
                    navigationIcon = {
                        IconButton(onClick = { onBackPressed() }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                    }
                )
            },
            bottomBar = {
                BottomAppBar(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Home,
                        contentDescription = "Person Icon",
                        modifier = Modifier
                            .padding(8.dp)
                            .size(24.dp),
                    )
                }
            }, floatingActionButton = {
                FloatingActionButton(onClick = { Log.d("TAG", "Floating Button Clicked") }) {
                    Icon(Icons.Default.Add, contentDescription = "Add")
                }
            }
        ) { innerPadding ->
            ElevatedCard(
                elevation = CardDefaults.cardElevation(
                    defaultElevation = 6.dp
                ),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
            ) {
                Column(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.Top,
                    horizontalAlignment = Alignment.Start
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = Icons.Default.AccountCircle,
                            contentDescription = "Person Icon",
                            modifier = Modifier.size(85.dp),
                            tint = when (patient?.gender) {
                                Enumerations.AdministrativeGender.MALE -> Color(0xFF6495ED)
                                Enumerations.AdministrativeGender.FEMALE -> Color(0xFFFF69B4)
                                else -> MaterialTheme.colorScheme.primary
                            }
                        )
                        Column(
                            modifier = Modifier.padding(start = 12.dp)
                        ) {
                            Text(
                                text = "${
                                    patient?.name?.firstOrNull()?.let {
                                        it.given?.firstOrNull().toString() + " " + it.family
                                    }
                                }, ${getAge(patient?.birthDate)}",
                                fontSize = 18.sp,
                                fontFamily = FontFamily(Font(R.font.google_sans_bold)),
                                textAlign = TextAlign.Center
                            )
                            Text(
                                modifier = Modifier.padding(top = 4.dp),
                                textAlign = TextAlign.Center,
                                text = "ID: ${
                                    patient?.identifier?.find {
                                        it.system == "opensrp_id"
                                    }?.value
                                }",
                                fontSize = 14.sp,
                                fontFamily = FontFamily(Font(R.font.google_sans_regular)),
                            )
                            Text(
                                modifier = Modifier.padding(top = 4.dp),
                                textAlign = TextAlign.Center,
                                text = "${
                                    patient?.gender
                                }",
                                fontSize = 14.sp,
                                fontFamily = FontFamily(Font(R.font.google_sans_regular)),
                            )
                        }
                        Icon(
                            imageVector = Icons.Rounded.Warning,
                            contentDescription = "Vital Signs",
                            modifier = Modifier
                                .padding(16.dp)
                                .size(44.dp)
                                .alpha(0F),
                            tint = MaterialTheme.colorScheme.secondary
                        )
                    }
                }
                ElevatedButton(
                    colors = ButtonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary,
                        disabledContentColor = Color.Gray,
                        disabledContainerColor = Color.LightGray,
                    ),
                    onClick = { Log.d("TAG", "Button Clicked") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .height(IntrinsicSize.Min),
                    shape = RoundedCornerShape(16.dp),
                ) {
                    Text(
                        "Screening",
                        fontSize = 16.sp,
                        fontFamily = FontFamily(Font(R.font.google_sans_bold)),
                        textAlign = TextAlign.Center,
                    )
                }
            }

        }
    }
}

fun getAge(dateOfBirth: Date?): Int {
    // Convert Date to LocalDate
    val localDateOfBirth = dateOfBirth?.toInstant()?.atZone(ZoneId.systemDefault())?.toLocalDate()

    // Get the current date
    val currentDate = LocalDate.now()

    // Calculate the age
    val period = Period.between(localDateOfBirth, currentDate)
    return period.years
}

@Preview(showBackground = true)
@Composable
fun PatientProfileViewPreView() {
    AppTheme {
        val patient = Patient().apply {
            id = "12345"
            gender = Enumerations.AdministrativeGender.MALE
            birthDate = SimpleDateFormat("yyyy-MM-dd").parse("1988-09-09")

            name = listOf(
                HumanName().apply {
                    given = listOf(org.hl7.fhir.r4.model.StringType("John"))
                    family = "Doe"
                }
            )

            identifier = listOf(
                Identifier().apply {
                    system = "opensrp_id"
                    value = "OP123456"
                }
            )
        }
        ProfileScaffold(patient, onBackPressed = {})
    }
}
