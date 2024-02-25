package com.example.shopping_assistance.ui.dashboard

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.OptIn
import androidx.appcompat.app.AlertDialog
import androidx.camera.core.ExperimentalGetImage
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.shopping_assistance.R
import com.example.shopping_assistance.databinding.FragmentDashboardBinding
import com.example.shopping_assistance.ui.BarcodeScannerActivity
import com.example.shopping_assistance.ui.adapters.BarcodesAdapter
import com.example.shopping_assistance.ui.models.BarcodeClass
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.UUID

class DashboardFragment : Fragment() {

    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!

    private lateinit var recyclerViewBarcodes: RecyclerView
    private lateinit var textViewNoBarcodes: TextView

    private lateinit var currentUserUid: String

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
            showAddBarcodeDialog()
        }

        binding.fabAddBarcodeByCamera.setOnClickListener{
            val builder = AlertDialog.Builder(requireContext())
            val input = EditText(requireContext())

            builder.setTitle(R.string.add_barcode)
                .setMessage(R.string.give_code_name)
                .setView(input)
                .setPositiveButton(R.string.next) { _, _ ->
                    val barcodeName = input.text.toString()

                    if (barcodeName.length > 20) {
                        Toast.makeText(requireContext(), R.string.code_cant_be_longer_than, Toast.LENGTH_SHORT).show()
                    } else {
                        val intent = Intent(requireContext(), BarcodeScannerActivity::class.java)
                        intent.putExtra("codeName", barcodeName)
                        startActivity(intent)
                    }
                }
                .setNegativeButton(R.string.cancel) { dialog, _ ->
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

                Log.d(TAG, "Loaded ${barcodes.size} barcodes")

                // Update the list in the adapter
                barcodes.sortBy { it.name.lowercase() }
                barcodesAdapter.submitList(barcodes)

                // Log to see if the collection isn't empty
                if (barcodes.isEmpty()) {
                    Log.d(TAG, "Barcodes collection is empty.")
                }
            }
            .addOnFailureListener { exception ->
                Log.e(TAG, "Error loading barcodes", exception)
            }
    }

    private fun showAddBarcodeDialog() {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle(R.string.add_barcode)

        val view = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_add_barcode, null)
        val editTextName = view.findViewById<EditText>(R.id.editTextName)
        val editTextValue = view.findViewById<EditText>(R.id.editTextValue)

        builder.setView(view)

        builder.setPositiveButton(R.string.add) { _, _ ->
            val codeId = UUID.randomUUID().toString()
            val name = editTextName.text.toString().trim()
            val value = editTextValue.text.toString().trim()

            if (name.isNotEmpty() && value.isNotEmpty()) {
                addBarcodeToDatabase(codeId, name, value)
            } else {
                Toast.makeText(requireContext(), R.string.provide_correct_details, Toast.LENGTH_SHORT).show()
            }
        }

        builder.setNegativeButton(R.string.cancel) { dialog, _ ->
            dialog.dismiss()
        }

        builder.create().show()
    }

    private fun addBarcodeToDatabase(codeId: String, name: String, value: String) {
        // Add barcode to the database
        val barcode = hashMapOf(
            "id" to codeId,
            "name" to name,
            "value" to value
        )

        firestore.collection("userData")
            .document(currentUserUid)
            .collection("codes")
            .document(codeId)
            .set(barcode)
            .addOnSuccessListener {
                loadBarcodes()
            }
            .addOnFailureListener { exception ->
                Log.e(TAG, "Error adding barcode", exception)
            }
    }

    override fun onStart() {
        super.onStart()
        loadBarcodes()
        Log.d("YourActivity", "onStart")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onResume() {
        super.onResume()
        Log.d("YourActivity", "onResume")
        loadBarcodes()
    }

    companion object {
        private const val TAG = "DashboardFragment"
    }
}