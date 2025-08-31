package com.example.vampire_system.features.backup

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.documentfile.provider.DocumentFile
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.vampire_system.R
import com.example.vampire_system.data.db.AppDatabase
import kotlinx.coroutines.*

class BackupSettingsFragment : Fragment() {
    private val scope = CoroutineScope(Dispatchers.IO)
    private var folderUri: Uri? = null

    private val pickFolder = registerForActivityResult(ActivityResultContracts.OpenDocumentTree()) { uri ->
        uri ?: return@registerForActivityResult
        requireContext().contentResolver.takePersistableUriPermission(
            uri, Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
        )
        folderUri = uri
        scope.launch { BackupPrefs.setFolder(requireContext(), uri.toString()) }
        Toast.makeText(requireContext(), "Folder selected", Toast.LENGTH_SHORT).show()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.fragment_backup_settings, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        // Set up back button to navigate to notifications
        view.findViewById<Button>(R.id.btnBack)?.setOnClickListener {
            findNavController().navigate(R.id.navigation_notifications)
        }
        
        val tvFolder = view.findViewById<TextView>(R.id.tvFolder)
        val swEncrypt = view.findViewById<Switch>(R.id.swEncrypt)
        val etHint = view.findViewById<EditText>(R.id.etHint)
        val etPass = view.findViewById<EditText>(R.id.etPass)
        val etPass2 = view.findViewById<EditText>(R.id.etPass2)

        view.findViewById<Button>(R.id.btnPickFolder).setOnClickListener { pickFolder.launch(null) }

        scope.launch {
            val uriStr = BackupPrefs.getFolder(requireContext())
            val enc = BackupPrefs.getEncrypt(requireContext())
            withContext(Dispatchers.Main) {
                folderUri = uriStr?.let { Uri.parse(it) }
                tvFolder.text = uriStr ?: "(none selected)"
                swEncrypt.isChecked = enc
            }
        }

        swEncrypt.setOnCheckedChangeListener { _, isChecked ->
            scope.launch { BackupPrefs.setEncrypt(requireContext(), isChecked) }
        }

        view.findViewById<Button>(R.id.btnSavePassphrase).setOnClickListener {
            val p1 = etPass.text?.toString() ?: ""
            val p2 = etPass2.text?.toString() ?: ""
            if (p1.length < 6 || p1 != p2) {
                Toast.makeText(requireContext(),"Enter matching passphrases (>=6)", Toast.LENGTH_SHORT).show(); return@setOnClickListener
            }
            val hint = etHint.text?.toString()
            scope.launch {
                val db = AppDatabase.get(requireContext())
                val rootUri = BackupPrefs.getFolder(requireContext())?.let { Uri.parse(it) }
                if (rootUri == null) {
                    withContext(Dispatchers.Main) { Toast.makeText(requireContext(),"Pick a folder first", Toast.LENGTH_SHORT).show() }
                    return@launch
                }
                val root = DocumentFile.fromTreeUri(requireContext(), rootUri) ?: return@launch
                val backups = root.findFile("VampireModePlusBackups") ?: root.createDirectory("VampireModePlusBackups")!!
                val soft = Crypto.getOrCreateSoftKey(requireContext())
                val enc = Crypto.wrapSoftKeyWithPassphrase(soft, p1.toCharArray())
                val keyFile = backups.findFile("key.enc") ?: backups.createFile("application/octet-stream","key.enc")!!
                requireContext().contentResolver.openOutputStream(keyFile.uri)?.use { it.write(enc) }
                BackupPrefs.setPassphraseHint(requireContext(), hint)
                withContext(Dispatchers.Main) { Toast.makeText(requireContext(),"Passphrase set; key.enc written", Toast.LENGTH_LONG).show() }
            }
        }

        view.findViewById<Button>(R.id.btnFullNow).setOnClickListener {
            scope.launch {
                val uriStr = BackupPrefs.getFolder(requireContext())
                if (uriStr == null) {
                    withContext(Dispatchers.Main){ Toast.makeText(requireContext(),"Pick a folder first", Toast.LENGTH_SHORT).show() }
                    return@launch
                }
                val encrypt = BackupPrefs.getEncrypt(requireContext())
                val passphrase: CharArray? = if (encrypt) etPass.text?.toString()?.takeIf { it.isNotBlank() }?.toCharArray() else null
                val ok = BackupManager(requireContext(), AppDatabase.get(requireContext()))
                    .backupFull(Uri.parse(uriStr), passphrase)
                withContext(Dispatchers.Main) { Toast.makeText(requireContext(), if (ok) "Full backup ok" else "Full backup failed", Toast.LENGTH_SHORT).show() }
            }
        }

        view.findViewById<Button>(R.id.btnIncNow).setOnClickListener {
            scope.launch {
                val uriStr = BackupPrefs.getFolder(requireContext())
                if (uriStr == null) {
                    withContext(Dispatchers.Main){ Toast.makeText(requireContext(),"Pick a folder first", Toast.LENGTH_SHORT).show() }
                    return@launch
                }
                val ok = BackupManager(requireContext(), AppDatabase.get(requireContext()))
                    .backupIncremental(Uri.parse(uriStr))
                withContext(Dispatchers.Main) { Toast.makeText(requireContext(), if (ok) "Incremental ok" else "Incremental failed", Toast.LENGTH_SHORT).show() }
            }
        }

        view.findViewById<Button>(R.id.btnRestore).setOnClickListener {
            registerForActivityResult(ActivityResultContracts.OpenDocumentTree()) { uri ->
                uri ?: return@registerForActivityResult
                val enc = swEncrypt.isChecked
                val pass = etPass.text?.toString()?.toCharArray()
                scope.launch {
                    val ok = BackupManager(requireContext(), AppDatabase.get(requireContext()))
                        .restoreFromFolder(uri, if (enc) pass else null)
                    withContext(Dispatchers.Main) { Toast.makeText(requireContext(), if (ok) "Restore complete" else "Restore failed", Toast.LENGTH_LONG).show() }
                }
            }.launch(null)
        }
    }
}


