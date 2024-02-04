package com.example.shopping_assistance.ui.dashboard

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.OptIn
import androidx.fragment.app.Fragment
import androidx.appcompat.app.AlertDialog
import androidx.camera.core.ExperimentalGetImage
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.shopping_assistance.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.example.shopping_assistance.databinding.FragmentDashboardBinding
import com.example.shopping_assistance.ui.BarcodeClass
import com.example.shopping_assistance.ui.BarcodeScannerActivity
import com.example.shopping_assistance.ui.BarcodesAdapter
import java.util.*

class DashboardFragment : Fragment() {

    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!

    private lateinit var recyclerViewBarcodes: RecyclerView
    private lateinit var textViewNoBarcodes: TextView

    private lateinit var currentUserUid: String // Pobierz aktualne UID użytkownika

    private val firestore = FirebaseFirestore.getInstance()

    private val barcodesAdapter = BarcodesAdapter()

    @OptIn(ExperimentalGetImage::class) override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        val root: View = binding.root

        //toolbar = binding.toolbar
        recyclerViewBarcodes = binding.recyclerViewBarcodes
        textViewNoBarcodes = binding.textViewNoBarcodes

        currentUserUid = FirebaseAuth.getInstance().currentUser?.uid ?: ""

        recyclerViewBarcodes.layoutManager = LinearLayoutManager(requireContext())
        recyclerViewBarcodes.adapter = barcodesAdapter

        binding.fabAddBarcode.setOnClickListener {
            // Tu możesz dodać kod do obsługi kliknięcia FAB, np. otwieranie AlertDialog
            showAddBarcodeDialog()
        }

        binding.fabAddBarcodeByCamera.setOnClickListener{
            val builder = AlertDialog.Builder(requireContext())
            val input = EditText(requireContext())

            builder.setTitle("Dodaj kod kreskowy")
                .setMessage("Wprowadź nazwę kodu")
                .setView(input)
                .setPositiveButton("Dalej") { _, _ ->
                    val barcodeName = input.text.toString()

                    if (barcodeName.length > 20) {
                        Toast.makeText(requireContext(), "Nazwa kodu nie może być dłuższa niż 20 znaków", Toast.LENGTH_SHORT).show()
                    } else {
                        val intent = Intent(requireContext(), BarcodeScannerActivity::class.java)
                        intent.putExtra("codeName", barcodeName)
                        startActivity(intent)
                    }
                }
                .setNegativeButton("Anuluj") { dialog, _ ->
                    dialog.dismiss()
                }

            builder.create().show()

        }

        loadBarcodes()

        return root
    }

    private fun loadBarcodes() {
        firestore.collection("userData")
            .document(currentUserUid)
            .collection("codes")
            .get()
            .addOnSuccessListener { result ->
                val barcodes = mutableListOf<BarcodeClass>()

                for (document in result) {
                    val barcode = document.toObject(BarcodeClass::class.java)
                    barcodes.add(barcode)
                }

                // Dodaj logi debugujące
                Log.d(TAG, "Loaded ${barcodes.size} barcodes")

                // Aktualizuj listę w adapterze
                barcodes.sortBy { it.name }
                barcodesAdapter.submitList(barcodes)

                // Dodaj log, aby sprawdzić, czy kolekcja products nie jest pusta
                if (barcodes.isEmpty()) {
                    Log.d(TAG, "Barcodes collection is empty.")
                }
            }
            .addOnFailureListener { exception ->
                // Błąd podczas pobierania kodów kreskowych
                Log.e(TAG, "Error loading barcodes", exception)
            }
    }

    private fun showAddBarcodeDialog() {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Dodaj kod kreskowy")

        val view = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_add_barcode, null)
        val editTextName = view.findViewById<EditText>(R.id.editTextName)
        val editTextValue = view.findViewById<EditText>(R.id.editTextValue)

        builder.setView(view)

        builder.setPositiveButton("Dodaj") { _, _ ->
            val codeId = UUID.randomUUID().toString()
            val name = editTextName.text.toString().trim()
            val value = editTextValue.text.toString().trim()

            if (name.isNotEmpty() && value.isNotEmpty()) {
                addBarcodeToDatabase(codeId, name, value)
            } else {
                // Obsługa błędów, np. pusty name lub value
                Toast.makeText(requireContext(), "Podaj poprawne dane", Toast.LENGTH_SHORT).show()
            }
        }

        builder.setNegativeButton("Anuluj") { dialog, _ ->
            dialog.dismiss()
        }

        builder.create().show()
    }

    private fun addBarcodeToDatabase(codeId: String, name: String, value: String) {
        // Dodaj kod kreskowy do bazy danych
        val barcode = hashMapOf(
            "id" to codeId,
            "name" to name,
            "value" to value
        )

        firestore.collection("userData")
            .document(currentUserUid)
            .collection("codes")
            .document(codeId) // Ustaw codeId jako identyfikator dokumentu
            .set(barcode)
            .addOnSuccessListener {
                // Pomyślnie dodano kod kreskowy
                loadBarcodes()
            }
            .addOnFailureListener { exception ->
                // Błąd podczas dodawania kodu kreskowego
                Log.e(TAG, "Error adding barcode", exception)
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onResume() {
        super.onResume()
        loadBarcodes()
    }

    companion object {
        private const val TAG = "DashboardFragment"
    }
}