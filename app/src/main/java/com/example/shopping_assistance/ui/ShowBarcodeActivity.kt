package com.example.shopping_assistance.ui

import android.content.ContentValues.TAG
import android.content.Intent
import android.graphics.Bitmap
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContentProviderCompat.requireContext
import com.example.shopping_assistance.R
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter
import com.google.zxing.common.BitMatrix

class ShowBarcodeActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_show_barcode)
        val barcodeName = intent.getStringExtra("barcodeName")
        val barcodeValue = intent.getStringExtra("barcodeValue")
        val barcodeId = intent.getStringExtra("barcodeId")

        val fab = findViewById<FloatingActionButton>(R.id.fabDelCode)
        fab.setOnClickListener {
            val alertDialogBuilder = AlertDialog.Builder(this)

            // Ustaw odpowiednie parametry dla AlertDialog
            alertDialogBuilder.setTitle("Potwierdzenie")
            alertDialogBuilder.setMessage("Czy na pewno chcesz usunąć ten kod?")

            // Dodaj przycisk "Tak" z obsługą usuwania
            alertDialogBuilder.setPositiveButton("Tak") { _, _ ->
                deleteBarcodeFromCollection(barcodeId!!)
            }

            // Dodaj przycisk "Nie" bez dodatkowej obsługi
            alertDialogBuilder.setNegativeButton("Nie", null)

            // Utwórz i wyświetl AlertDialog
            val alertDialog = alertDialogBuilder.create()
            alertDialog.show()
        }


        // Ustaw tytuł na toolbarze zgodnie z nazwą kodu kreskowego
        supportActionBar?.title = barcodeName

        // Możesz również wykorzystać barcodeId do dalszych operacji, jeśli potrzebujesz

        val imageView = findViewById<ImageView>(R.id.imageViewBarcode)

        // Przykładowy tekst, z którego generujemy kod kreskowy
        val barcodeText = intent.getStringExtra("barcodeValue")
        val text = findViewById<TextView>(R.id.textViewBarcodeValue)
        text.text = barcodeValue

        // Generowanie kodu kreskowego
        val barcodeBitmap = generateBarcode(barcodeText!!, 800, 400)
        imageView.setImageBitmap(barcodeBitmap)
    }

    private fun generateBarcode(text: String, width: Int, height: Int): Bitmap? {
        return try {
            val multiFormatWriter = MultiFormatWriter()
            val bitMatrix: BitMatrix =
                multiFormatWriter.encode(text, BarcodeFormat.CODE_128, width, height)
            val barcodeBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)

            for (x in 0 until width) {
                for (y in 0 until height) {
                    barcodeBitmap.setPixel(x, y, if (bitMatrix[x, y]) android.graphics.Color.BLACK else android.graphics.Color.WHITE)
                }
            }

            barcodeBitmap
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun deleteBarcodeFromCollection(barcodeId: String) {
        val firestore = FirebaseFirestore.getInstance()

        // Zastosuj odpowiednie ścieżki i kolekcje w swojej bazie danych
        val currentUserUid = FirebaseAuth.getInstance().currentUser?.uid // Zastąp własnym UID aktualnie zalogowanego użytkownika
        val codesCollectionPath = "userData/$currentUserUid/codes"

        firestore.collection(codesCollectionPath)
            .document(barcodeId)
            .delete()
            .addOnSuccessListener {
                // Kod został pomyślnie usunięty z kolekcji
                // Tutaj można umieścić dodatkowe działania po usunięciu, jeśli są potrzebne
                finish()  // Zamknij aktywność po usunięciu kodu
            }
            .addOnFailureListener { e ->
                // Obsługa błędów przy usuwaniu kodu
                Log.e(TAG, "Błąd podczas usuwania kodu z kolekcji: $e")
                // Tutaj można dodać powiadomienie o błędzie lub inne działania
            }
    }
}