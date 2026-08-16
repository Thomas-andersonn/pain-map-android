package com.example.painmap.data.repository

import com.example.painmap.domain.model.AnatomicalRegion
import com.example.painmap.domain.model.PainDuration
import com.example.painmap.domain.model.PainPoint
import com.example.painmap.domain.model.PainType
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class PainRecordRepositoryTest {

    private lateinit var repository: PainRecordRepositoryImpl

    @Before
    fun setUp() {
        repository = PainRecordRepositoryImpl()
    }

    @Test
    fun savePainPoint_addsNewPointToActiveList() = runBlocking {
        val painPoint = PainPoint(
            region = AnatomicalRegion.KNEE_RIGHT,
            intensity = 6,
            painTypes = setOf(PainType.STABBING, PainType.STIFFNESS),
            duration = PainDuration.ACUTE
        )

        val result = repository.savePainPoint(painPoint)
        assertTrue(result.isSuccess)

        val list = repository.getActivePainPoints().first()
        assertEquals(1, list.size)
        assertEquals(painPoint.id, list[0].id)
        assertEquals(AnatomicalRegion.KNEE_RIGHT, list[0].region)
        assertEquals(6, list[0].intensity)
    }

    @Test
    fun savePainPoint_updatesExistingPoint() = runBlocking {
        val initialPoint = PainPoint(
            id = "point-123",
            region = AnatomicalRegion.SHOULDER_LEFT,
            intensity = 4
        )
        repository.savePainPoint(initialPoint)

        val updatedPoint = initialPoint.copy(intensity = 8, triggers = "Overhead lifting")
        repository.savePainPoint(updatedPoint)

        val list = repository.getActivePainPoints().first()
        assertEquals(1, list.size)
        assertEquals(8, list[0].intensity)
        assertEquals("Overhead lifting", list[0].triggers)
    }

    @Test
    fun removePainPoint_removesPointById() = runBlocking {
        val p1 = PainPoint(id = "p1", region = AnatomicalRegion.HEAD)
        val p2 = PainPoint(id = "p2", region = AnatomicalRegion.NECK_CERVICAL)

        repository.savePainPoint(p1)
        repository.savePainPoint(p2)
        assertEquals(2, repository.getActivePainPoints().first().size)

        repository.removePainPoint("p1")
        val remaining = repository.getActivePainPoints().first()
        assertEquals(1, remaining.size)
        assertEquals("p2", remaining[0].id)
    }

    @Test
    fun clearActivePainPoints_emptiesList() = runBlocking {
        val p1 = PainPoint(region = AnatomicalRegion.LOWER_BACK_LUMBAR)
        repository.savePainPoint(p1)
        assertEquals(1, repository.getActivePainPoints().first().size)

        repository.clearActivePainPoints()
        assertTrue(repository.getActivePainPoints().first().isEmpty())
    }
}
