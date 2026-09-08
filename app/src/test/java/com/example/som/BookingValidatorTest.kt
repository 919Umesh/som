package com.example.som

import com.example.som.viewmodel.BookingValidator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class BookingValidatorTest {
    @Test
    fun requiredBookingFieldsAreValidated() {
        val errors = BookingValidator.validate(name = "", contact = "", address = "")

        assertEquals(setOf("customerName", "contact", "address"), errors.keys)
    }

    @Test
    fun validBookingFormHasNoErrors() {
        val errors = BookingValidator.validate("Aarav", "9800000000", "Kathmandu")

        assertTrue(errors.isEmpty())
    }
}
