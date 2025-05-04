package com.healthtracker.ncdcare.ui.home

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults.topAppBarColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.navigation.findNavController
import androidx.navigation.fragment.navArgs
import com.google.android.material.search.SearchView
import com.healthtracker.ncdcare.MainActivity
import com.healthtracker.ncdcare.R
import com.healthtracker.ncdcare.ui.theme.AppTheme
import com.healthtracker.ncdcare.utils.createUri
import com.healthtracker.ncdcare.utils.getQuestionnaireJsonStringFromAssets
import com.healthtracker.ncdcare.utils.getQuestionnaireJsonStringFromFileUri
import kotlinx.coroutines.launch
import org.hl7.fhir.r4.model.Enumerations
import org.hl7.fhir.r4.model.HumanName
import org.hl7.fhir.r4.model.Identifier
import org.hl7.fhir.r4.model.Patient
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.Period
import java.time.ZoneId
import java.util.Date

class PatientProfileFragment : Fragment() {

    private val args: PatientProfileFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return ComposeView(requireContext()).apply {
            setContent {
                AppTheme(darkTheme = false) {
                    // You can pass patient args here via ViewModel or SafeArgs
                    ProfileScaffold(
                        patient = args.patient, // Replace with real data
                        onBackPressed = { findNavController().navigateUp() }
                    )
                }
            }
        }
    }

}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScaffold(patient: Patient?, onBackPressed: () -> Unit) {
    val context = LocalContext.current
    val navController = LocalView.current.findNavController()
    val coroutineScope = rememberCoroutineScope()
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
                    onClick = {
                        coroutineScope.launch {
                            val startTime = System.currentTimeMillis()
                            val questionnaireUriString = createUri(
                                context = context,
                                backgroundContext = coroutineContext,
                                fileName = "screening-questionnaire.json"
                            )
                            val action = PatientProfileFragmentDirections.actionPatientProfileToScreening(
                                questionnaireTitleKey = context.getString(R.string.screening_questionnaire_title),
                                questionnaireUriString = questionnaireUriString.toString(),
                                questionnaireWithValidationJsonStringKey = null,
                                patientId = patient?.id,
                            )
                            navController.navigate(action)

                            val endTime = System.currentTimeMillis()
                            Log.d("PerformanceDebug", "Time taken for onClick logic: ${endTime - startTime} ms")

                        }
                    },
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
