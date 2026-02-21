package com.swappy.aicalcount.data.health

import android.content.Context
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZoneOffset

/**
 * Minimal Health Connect integration for weight: read latest weight, write weight.
 * Requires Health Connect app installed (Android 9+) and user to grant READ_WEIGHT / WRITE_WEIGHT.
 */
class HealthConnectRepository(private val context: Context) {

    private val client: Any? by lazy {
        try {
            val healthConnectClientClass = Class.forName("androidx.health.connect.client.HealthConnectClient")
            val getSdkStatus = healthConnectClientClass.getMethod("getSdkStatus", Context::class.java)
            val status = getSdkStatus.invoke(null, context)
            val sdkAvailable = healthConnectClientClass.getField("SDK_AVAILABLE").getInt(null)
            if (status == sdkAvailable) {
                val getOrCreate = healthConnectClientClass.getMethod("getOrCreate", Context::class.java)
                getOrCreate.invoke(null, context)
            } else null
        } catch (_: Throwable) {
            null
        }
    }

    val isAvailable: Boolean
        get() = client != null

    /**
     * Fetches the most recent weight record from Health Connect, if any.
     * Returns weight in kg and the date it was recorded, or null if none.
     */
    suspend fun getLatestWeight(): Pair<Double, LocalDate>? {
        val c = client ?: return null
        return try {
            val readRecordsRequestClass = Class.forName("androidx.health.connect.client.request.ReadRecordsRequest")
            val weightRecordClass = Class.forName("androidx.health.connect.client.records.WeightRecord")
            val timeRangeFilterClass = Class.forName("androidx.health.connect.client.time.TimeRangeFilter")
            val before = timeRangeFilterClass.getMethod("before", Instant::class.java).invoke(null, Instant.now())
            val requestBuilder = readRecordsRequestClass.getConstructor(
                Class::class.java,
                timeRangeFilterClass,
                Int::class.javaPrimitiveType
            ).newInstance(weightRecordClass, before, 1)
            val readRecords = c.javaClass.getMethod("readRecords", readRecordsRequestClass)
            val response = readRecords.invoke(c, requestBuilder)
            val records = response?.javaClass?.getMethod("getRecords")?.invoke(response) as? List<*> ?: return null
            val record = records.firstOrNull() ?: return null
            val time = record.javaClass.getMethod("getTime").invoke(record) as Instant
            val weight = record.javaClass.getMethod("getWeight").invoke(record)
            val inKg = weight?.javaClass?.getMethod("getInKilograms")?.invoke(weight) as? Double ?: return null
            val date = time.atZone(ZoneId.systemDefault()).toLocalDate()
            inKg to date
        } catch (_: Throwable) {
            null
        }
    }

    /**
     * Writes a weight entry to Health Connect for the given date.
     */
    suspend fun writeWeight(weightKg: Double, date: LocalDate) {
        val c = client ?: return
        try {
            val massClass = Class.forName("androidx.health.connect.client.records.mass.Mass")
            val kilograms = massClass.getMethod("kilograms", Double::class.javaPrimitiveType).invoke(null, weightKg)
            val instant = date.atStartOfDay(ZoneId.systemDefault()).toInstant()
            val weightRecordClass = Class.forName("androidx.health.connect.client.records.WeightRecord")
            val record = weightRecordClass.getConstructor(
                massClass,
                Instant::class.java,
                ZoneOffset::class.java
            ).newInstance(kilograms, instant, ZoneOffset.UTC)
            c.javaClass.getMethod("insertRecords", List::class.java).invoke(c, listOf(record))
        } catch (_: Throwable) { }
    }
}
