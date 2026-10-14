package com.example.data.repository

import android.content.Context
import android.util.Log
import com.example.R
import com.example.data.model.LoanRequest
import com.example.data.model.LoanStatus
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

class FirestoreLoanRepository(context: Context) {
    private val db = FirebaseFirestore.getInstance(
        context.applicationContext.getString(R.string.firestore_database_id)
    )

    private fun parseDocument(doc: DocumentSnapshot): LoanRequest {
        val rawStatus = doc.getString("status")
        val statusEnum = LoanStatus.fromString(rawStatus)
        val timestamp = doc.getTimestamp("timestamp") ?: doc.getTimestamp("createdAt")
        val timeMillis = timestamp?.toDate()?.time ?: doc.getLong("requestTimeMillis") ?: System.currentTimeMillis()

        return LoanRequest(
            id = doc.id,
            userId = doc.getString("userId") ?: doc.getString("staffId") ?: "",
            staffId = doc.getString("staffId") ?: doc.getString("userId") ?: "",
            staffName = doc.getString("staffName") ?: "Staff Sekolah",
            vehicleId = doc.getString("vehicleId") ?: "",
            vehicleName = doc.getString("vehicleName") ?: "Unit Sekolah Rakyat",
            vehiclePlate = doc.getString("vehiclePlate") ?: "",
            purpose = doc.getString("purpose") ?: "Keperluan Sekolah",
            destination = doc.getString("destination") ?: "Surabaya",
            status = statusEnum,
            timestamp = timestamp,
            createdAt = timestamp,
            requestTimeMillis = timeMillis,
            approvedTimeMillis = doc.getLong("approvedTimeMillis"),
            returnTimeMillis = doc.getLong("returnTimeMillis"),
            securityNotes = doc.getString("securityNotes"),
            verifiedBySecurityName = doc.getString("verifiedBySecurityName")
        )
    }

    fun observePendingLoanRequests(): Flow<List<LoanRequest>> = callbackFlow {
        val listener = db.collection("loans")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.w("FirestoreLoanRepo", "Listen failed: ${error.message}")
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val requests = snapshot.documents.map { doc ->
                        parseDocument(doc)
                    }.filter { loan ->
                        loan.status == LoanStatus.PENDING ||
                        loan.status == LoanStatus.PENDING_KEAMANAN
                    }
                    trySend(requests)
                }
            }
        awaitClose { listener.remove() }
    }

    fun observeAllLoanRequests(): Flow<List<LoanRequest>> = callbackFlow {
        val listener = db.collection("loans")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.w("FirestoreLoanRepo", "Listen failed: ${error.message}")
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val requests = snapshot.documents.map { doc ->
                        parseDocument(doc)
                    }
                    trySend(requests)
                }
            }
        awaitClose { listener.remove() }
    }

    fun saveLoanRequest(loan: LoanRequest, onComplete: (Boolean) -> Unit = {}) {
        val docId = if (loan.id.isNotBlank()) loan.id else db.collection("loans").document().id
        val data = mapOf(
            "id" to docId,
            "userId" to loan.userId,
            "staffId" to loan.staffId,
            "staffName" to loan.staffName,
            "vehicleId" to loan.vehicleId,
            "vehicleName" to loan.vehicleName,
            "vehiclePlate" to loan.vehiclePlate,
            "purpose" to loan.purpose,
            "destination" to loan.destination,
            "status" to "pending",
            "statusEnum" to loan.status.name,
            "securityNotes" to (loan.securityNotes ?: ""),
            "verifiedBySecurityName" to (loan.verifiedBySecurityName ?: ""),
            "requestTimeMillis" to loan.requestTimeMillis,
            "timestamp" to FieldValue.serverTimestamp(),
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp()
        )

        db.collection("loans").document(docId).set(data, SetOptions.merge())
            .addOnSuccessListener { onComplete(true) }
            .addOnFailureListener {
                Log.e("FirestoreLoanRepo", "Failed to save loan: ${it.message}")
                onComplete(false)
            }
    }

    fun updateLoanStatus(
        loanId: String,
        status: LoanStatus,
        reviewerName: String,
        notes: String,
        onComplete: (Boolean) -> Unit = {}
    ) {
        val statusString = when (status) {
            LoanStatus.APPROVED, LoanStatus.APPROVED_IN_TRIP -> "approved"
            LoanStatus.REJECTED, LoanStatus.REJECTED_BY_KEAMANAN -> "rejected"
            LoanStatus.PENDING, LoanStatus.PENDING_KEAMANAN -> "pending"
            else -> status.name.lowercase()
        }

        val data = mapOf(
            "status" to statusString,
            "statusEnum" to status.name,
            "verifiedBySecurityName" to reviewerName,
            "securityNotes" to notes,
            "approvedTimeMillis" to System.currentTimeMillis(),
            "updatedAt" to FieldValue.serverTimestamp()
        )

        db.collection("loans").document(loanId).update(data)
            .addOnSuccessListener { onComplete(true) }
            .addOnFailureListener {
                Log.e("FirestoreLoanRepo", "Failed to update loan status in Firestore: ${it.message}")
                onComplete(false)
            }
    }

    fun createSampleLoanRequest(
        vehicleId: String = "granmax",
        vehicleName: String = "Daihatsu Gran Max",
        vehiclePlate: String = "L 1945 SR",
        staffName: String = "Bu Sari, S.Pd",
        purpose: String = "Distribusi Buku & Seragam Sekolah",
        destination: String = "Kecamatan Kenjeran, Surabaya",
        onComplete: (Boolean) -> Unit = {}
    ) {
        val docId = db.collection("loans").document().id
        val newLoan = LoanRequest(
            id = docId,
            userId = "usr_staff_${System.currentTimeMillis().toString().takeLast(4)}",
            staffId = "usr_staff_01",
            staffName = staffName,
            vehicleId = vehicleId,
            vehicleName = vehicleName,
            vehiclePlate = vehiclePlate,
            purpose = purpose,
            destination = destination,
            status = LoanStatus.PENDING_KEAMANAN
        )
        saveLoanRequest(newLoan, onComplete)
    }

    companion object {
        @Volatile
        private var instance: FirestoreLoanRepository? = null

        fun getInstance(context: Context): FirestoreLoanRepository {
            return instance ?: synchronized(this) {
                instance ?: FirestoreLoanRepository(context.applicationContext).also { instance = it }
            }
        }
    }
}
