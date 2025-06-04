package com.faith.securedigitalwallet.ui

import android.content.Context
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.faith.securedigitalwallet.data.BankProfile
import com.faith.securedigitalwallet.data.BankProfileDao
import com.faith.securedigitalwallet.data.User
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import androidx.datastore.preferences.preferencesDataStore
import com.faith.securedigitalwallet.data.PasswordManager

private val Context.dataStore by preferencesDataStore(name = "settings")
enum class PasswordAction {
    VIEW, EDIT, DELETE
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BankProfilesScreen(
    user: User,
    bankProfileDao: BankProfileDao,
    onBack: () -> Unit,
    onNoProfiles: () -> Unit
) {
    val context = LocalContext.current

    // Handle system back button press
    BackHandler {
        onBack()
    }

    val profiles by bankProfileDao.getProfilesForUser(user.id).collectAsState(initial = emptyList())

    var showDialog by remember { mutableStateOf(false) }
    var editingProfile by remember { mutableStateOf<BankProfile?>(null) }
    var deletingProfile by remember { mutableStateOf<BankProfile?>(null) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var isDeleting by remember { mutableStateOf(false) }

    var viewingProfile by remember { mutableStateOf<BankProfile?>(null) }
    var showViewDialog by remember { mutableStateOf(false) }

    var showPasswordPrompt by remember { mutableStateOf(false) }
    var masterPassword by remember { mutableStateOf<String?>(null) }
    
    var isBiometricAuthAvailable by remember { mutableStateOf(false) }
    var passwordAction by remember { mutableStateOf<PasswordAction?>(null) }


    // Load master password from DataStore once
    LaunchedEffect(Unit) {
        masterPassword  = PasswordManager.getMasterPassword(context)
        val biometricManager = BiometricManager.from(context)
        isBiometricAuthAvailable = biometricManager.canAuthenticate(
            BiometricManager.Authenticators.BIOMETRIC_STRONG or
                    BiometricManager.Authenticators.DEVICE_CREDENTIAL
        ) == BiometricManager.BIOMETRIC_SUCCESS
        if (masterPassword == null) {
            showPasswordPrompt = true
        }
    }

    LaunchedEffect(profiles, isDeleting) {
        if (isDeleting && profiles.isEmpty()) {
            onNoProfiles()
            isDeleting = false
        }
    }
<<<<<<< Updated upstream

    val biometricPrompt = remember {
        val executor = ContextCompat.getMainExecutor(context)
        BiometricPrompt(context as androidx.fragment.app.FragmentActivity, executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    showResetPasswordDialog = true
                }

                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    // Optional error handling
                }
            })
    }

    val promptInfo = remember {
        BiometricPrompt.PromptInfo.Builder()
            .setTitle("Authenticate to reset password")
            .setSubtitle("Use your device screen lock or biometric to reset master password")
            .setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.DEVICE_CREDENTIAL)
            .build()
    }

=======
>>>>>>> Stashed changes
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Bank Profiles for ${user.name}") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = {
                editingProfile = null
                showDialog = true
            }) {
                Text("+")
            }
        }
    ) { padding ->
        if (profiles.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text("No bank profiles found. Click + to add one.")
            }
        } else {
            LazyColumn(modifier = Modifier.padding(padding)) {
                items(profiles) { profile ->
                    Card(
                        modifier = Modifier
                            .padding(8.dp)
                            .fillMaxWidth(),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Account: ${profile.accountNumber}", style = MaterialTheme.typography.titleMedium)
                            Text("Bank: ${profile.bank}", style = MaterialTheme.typography.bodyMedium)

                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End
                            ) {
                                TextButton(onClick = {
                                    viewingProfile = profile
                                    passwordAction = PasswordAction.VIEW
                                    if (masterPassword == null) {
                                        showPasswordPrompt = true
                                    } else {
                                        showPasswordPrompt = true
                                    }
                                }) {
                                    Text("View")
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                TextButton(onClick = {
                                    editingProfile = profile
                                    passwordAction = PasswordAction.EDIT
                                    showPasswordPrompt = true
                                }) {
                                    Icon(Icons.Filled.Edit, contentDescription = "Edit")
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Edit")
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                TextButton(onClick = {
                                    deletingProfile = profile
                                    passwordAction = PasswordAction.DELETE
                                    showPasswordPrompt = true
                                }) {
                                    Icon(Icons.Filled.Delete, contentDescription = "Delete")
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Delete")
                                }
                            }
                        }
                    }
                }
            }
        }

        if (showDialog) {
            AddOrEditBankProfileDialog(
                existingProfile = editingProfile,
                existingProfiles = profiles,   // <-- add this line
                onDismiss = { showDialog = false },
                onSave = { profile ->
                    showDialog = false
                    CoroutineScope(Dispatchers.IO).launch {
                        if (profile.id == 0) {
                            bankProfileDao.insertProfile(profile.copy(userOwnerId = user.id))
                        } else {
                            bankProfileDao.updateProfile(profile)
                        }
                    }
                }
            )
        }

        if (showDeleteDialog) {
            AlertDialog(
                onDismissRequest = { showDeleteDialog = false },
                title = { Text("Delete Bank Profile") },
                text = { Text("Are you sure you want to delete this bank profile?") },
                confirmButton = {
                    TextButton(onClick = {
                        deletingProfile?.let { profile ->
                            CoroutineScope(Dispatchers.IO).launch {
                                bankProfileDao.deleteProfile(profile)
                                isDeleting = true
                            }
                        }
                        deletingProfile = null
                        showDeleteDialog = false
                    }) {
                        Text("Delete", color = MaterialTheme.colorScheme.error)
                    }
                },
                dismissButton = {
                    TextButton(onClick = {
                        deletingProfile = null
                        showDeleteDialog = false
                    }) {
                        Text("Cancel")
                    }
                }
            )
        }

        if (showViewDialog && viewingProfile != null) {
            AlertDialog(
                onDismissRequest = { showViewDialog = false },
                title = { Text("Bank Profile Details") },
                text = {
                    Column {
                        Text("Account Number: ${viewingProfile!!.accountNumber}")
                        Text("Bank: ${viewingProfile!!.bank}")
                        Text("Type: ${viewingProfile!!.type}")
                        Text("User ID: ${viewingProfile!!.userId}")
                        Text("Profile Password: ${viewingProfile!!.profilePassword}")
                        Text("Mobile Login Pin: ${viewingProfile!!.mobileLoginPin}")
                        Text("UPI PIN: ${viewingProfile!!.upiPin}")
                        Text("ATM PIN: ${viewingProfile!!.atmPin}")
                        Text("Password: ${viewingProfile!!.password}")
                        Text("Mobile: ${viewingProfile!!.mobile}")
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showViewDialog = false }) {
                        Text("Close")
                    }
                }
            )
        }

        if (showPasswordPrompt) {
            PasswordPromptDialog(
                isSettingPassword = (masterPassword == null),
                onDismiss = { showPasswordPrompt = false },
                onPasswordEntered = { enteredPassword ->
                    showPasswordPrompt = false
                    if (masterPassword == null) {
                        CoroutineScope(Dispatchers.IO).launch {
                            PasswordManager.saveMasterPassword(context, enteredPassword)
                            masterPassword = enteredPassword
                        }
                    } else {
                        if (enteredPassword == masterPassword) {
                            when (passwordAction) {
                                PasswordAction.VIEW -> showViewDialog = true
                                PasswordAction.EDIT -> showDialog = true
                                PasswordAction.DELETE -> showDeleteDialog = true
                                else -> {}
                            }
                        } else {
                            Toast.makeText(context, "❌ Invalid password", Toast.LENGTH_SHORT).show()
                        }
                    }
                    passwordAction = null
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddOrEditBankProfileDialog(
    existingProfile: BankProfile?,
    existingProfiles: List<BankProfile>,  // Pass existing profiles to check duplicates
    onDismiss: () -> Unit,
    onSave: (BankProfile) -> Unit
) {
    var accountNumber by remember { mutableStateOf(existingProfile?.accountNumber ?: "") }
    var type by remember { mutableStateOf(existingProfile?.type ?: "") }
    var userId by remember { mutableStateOf(existingProfile?.userId ?: "") }
    var bank by remember { mutableStateOf(existingProfile?.bank ?: "") }
    var profilePassword by remember { mutableStateOf(existingProfile?.profilePassword ?: "") }
    var mobileLoginPin by remember { mutableStateOf(existingProfile?.mobileLoginPin ?: "") }
    var upiPin by remember { mutableStateOf(existingProfile?.upiPin ?: "") }
    var atmPin by remember { mutableStateOf(existingProfile?.atmPin ?: "") }
    var password by remember { mutableStateOf(existingProfile?.password ?: "") }
    var mobile by remember { mutableStateOf(existingProfile?.mobile ?: "") }
    val context = LocalContext.current


    var errorMessage by remember { mutableStateOf<String?>(null) }

    val scrollState = rememberScrollState()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(if (existingProfile == null) "Add Bank Profile" else "Edit Bank Profile")
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(scrollState)
            ) {
                OutlinedTextField(
                    value = accountNumber,
                    onValueChange = { accountNumber = it },
                    label = { Text("Account Number") }
                )
                val types = listOf("Saving", "Salary", "Deposit", "Current", "PPF", "Sukaniya", "Credit")
                val banks = listOf(
                    "SBI", "HDFC", "ICICI", "PNB", "BOB", "RBI", "NABARD", "BOI", "UBI", "UCO",
                    "IOB", "CBI", "BOM", "CNB", "KMB", "IDBI", "DCB", "AXB", "YESB", "HSBC",
                    "KVB", "TMB", "CSB", "LVB", "SIB", "FDB"
                )
                var expanded by remember { mutableStateOf(false) }

                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded },
                ) {
                    OutlinedTextField(
                        value = type,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Type") },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                        },
                        modifier = Modifier.menuAnchor()
                    )

                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        types.forEach { selectionOption ->
                            DropdownMenuItem(
                                text = { Text(selectionOption) },
                                onClick = {
                                    type = selectionOption
                                    expanded = false
                                }
                            )
                        }
                    }
                }
                OutlinedTextField(value = userId, onValueChange = { userId = it }, label = { Text("User ID") })
                var bankDropdownExpanded by remember { mutableStateOf(false) }

                ExposedDropdownMenuBox(
                    expanded = bankDropdownExpanded,
                    onExpandedChange = { bankDropdownExpanded = !bankDropdownExpanded },
                ) {
                    OutlinedTextField(
                        value = bank,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Bank") },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = bankDropdownExpanded)
                        },
                        modifier = Modifier.menuAnchor()
                    )

                    ExposedDropdownMenu(
                        expanded = bankDropdownExpanded,
                        onDismissRequest = { bankDropdownExpanded = false }
                    ) {
                        banks.forEach { selectionOption ->
                            DropdownMenuItem(
                                text = { Text(selectionOption) },
                                onClick = {
                                    bank = selectionOption
                                    bankDropdownExpanded = false
                                }
                            )
                        }
                    }
                }
                OutlinedTextField(value = profilePassword, onValueChange = { profilePassword = it }, label = { Text("Profile Password") })
                OutlinedTextField(value = mobileLoginPin, onValueChange = { mobileLoginPin = it }, label = { Text("Mobile Login Pin") })
                OutlinedTextField(value = upiPin, onValueChange = { upiPin = it }, label = { Text("UPI PIN") })
                OutlinedTextField(value = atmPin, onValueChange = { atmPin = it }, label = { Text("ATM PIN") })
                OutlinedTextField(value = password, onValueChange = { password = it }, label = { Text("Password") })
                OutlinedTextField(value = mobile, onValueChange = { mobile = it }, label = { Text("Mobile") })

                errorMessage?.let { error ->
                    Text(
                        text = error,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                if (accountNumber.isBlank()) {
                    errorMessage = "Account Number cannot be empty"
                    return@TextButton
                }
                // Check for duplicates except for the profile currently being edited
                val duplicate = existingProfiles.any {
                    it.accountNumber == accountNumber && it.id != existingProfile?.id
                }
                if (duplicate) {
                    Toast.makeText(context, "Account Number already exists", Toast.LENGTH_SHORT).show()
                    return@TextButton
                }

                errorMessage = null
                onSave(
                    BankProfile(
                        id = existingProfile?.id ?: 0,
                        userOwnerId = existingProfile?.userOwnerId ?: 0,
                        accountNumber = accountNumber,
                        type = type,
                        userId = userId,
                        bank = bank,
                        profilePassword = profilePassword,
                        mobileLoginPin = mobileLoginPin,
                        upiPin = upiPin,
                        atmPin = atmPin,
                        password = password,
                        mobile = mobile
                    )
                )
            }) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
