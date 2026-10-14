package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.model.UserRole
import com.example.data.model.VehicleStatus
import com.example.data.repository.AppRepository
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Sekolah Rakyat", appName)
  }

  @Test
  fun `repository has initial vehicles and admin user`() {
    val repository = AppRepository.getInstance()
    val vehicles = repository.vehicles.value
    assertEquals(3, vehicles.size)
    assertTrue(vehicles.any { it.name.contains("N-Max") })
    assertTrue(vehicles.any { it.name.contains("Gran Max") })
    assertTrue(vehicles.any { it.name.contains("Avanza") })

    val users = repository.users.value
    assertTrue(users.any { it.role == UserRole.ADMIN })
    assertTrue(users.any { it.role == UserRole.STAFF })
    assertTrue(users.any { it.role == UserRole.KEAMANAN })
  }

  @Test
  fun `loan and security verification workflow`() {
    val repository = AppRepository.getInstance()
    val staff = repository.users.value.first { it.role == UserRole.STAFF }
    val security = repository.users.value.first { it.role == UserRole.KEAMANAN }

    val loanResult = repository.requestVehicleLoan(
      staff = staff,
      vehicleId = "avanza",
      purpose = "Kunjungan Siswa",
      destination = "Gedung Juang"
    )
    assertTrue(loanResult.isSuccess)
    val loan = loanResult.getOrThrow()

    // Keamanan verifies loan at gate
    val verifyResult = repository.verifyLoan(
      loanId = loan.id,
      securityUser = security,
      approve = true,
      notes = "Izin keluar gerbang diberikan"
    )
    assertTrue(verifyResult.isSuccess)

    // Vehicle status should now be SEDANG_DIPAKAI
    val updatedVehicle = repository.vehicles.value.first { it.id == "avanza" }
    assertEquals(VehicleStatus.SEDANG_DIPAKAI, updatedVehicle.status)

    // Return to garage
    val returnResult = repository.returnVehicleToGarage(loan.id)
    assertTrue(returnResult.isSuccess)
    val returnedVehicle = repository.vehicles.value.first { it.id == "avanza" }
    assertEquals(VehicleStatus.GARASI, returnedVehicle.status)
  }

  @Test
  fun `loanRequest firestore data class supports default constructor and properties`() {
    val emptyLoan = com.example.data.model.LoanRequest()
    assertEquals("", emptyLoan.id)
    assertEquals("", emptyLoan.userId)
    assertEquals("", emptyLoan.vehicleId)
    assertEquals(com.example.data.model.LoanStatus.PENDING_KEAMANAN, emptyLoan.status)
    org.junit.Assert.assertNull(emptyLoan.timestamp)

    val customTimestamp = com.google.firebase.Timestamp.now()
    val fullLoan = com.example.data.model.LoanRequest(
      id = "doc-123",
      userId = "user-abc",
      vehicleId = "granmax",
      status = com.example.data.model.LoanStatus.fromString("APPROVED"),
      timestamp = customTimestamp
    )
    assertEquals("doc-123", fullLoan.id)
    assertEquals("user-abc", fullLoan.userId)
    assertEquals("granmax", fullLoan.vehicleId)
    assertEquals(com.example.data.model.LoanStatus.APPROVED_IN_TRIP, fullLoan.status)
    assertEquals(customTimestamp, fullLoan.timestamp)
  }
}
