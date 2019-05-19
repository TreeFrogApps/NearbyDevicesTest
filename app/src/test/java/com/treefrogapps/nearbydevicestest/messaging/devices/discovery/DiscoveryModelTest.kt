package com.treefrogapps.nearbydevicestest.messaging.devices.discovery

import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.whenever
import com.treefrogapps.nearbydevicestest.RxTestScheduler
import com.treefrogapps.nearbydevicestest.messaging.devices.discovery.DiscoveryEvent.ConnectionEvent
import com.treefrogapps.nearbydevicestest.nearby.AdvertisingConnection.InboundDevice
import com.treefrogapps.nearbydevicestest.nearby.ConnectionManager
import com.treefrogapps.nearbydevicestest.nearby.ConnectionState.CONNECTED
import com.treefrogapps.nearbydevicestest.nearby.ConnectionState.INITIATED
import com.treefrogapps.nearbydevicestest.nearby.DiscoverConnection
import com.treefrogapps.nearbydevicestest.nearby.DiscoveryState
import io.reactivex.Flowable
import io.reactivex.Single
import io.reactivex.processors.BehaviorProcessor
import io.reactivex.schedulers.TestScheduler
import io.reactivex.subscribers.TestSubscriber
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.verifyNoMoreInteractions
import org.mockito.MockitoAnnotations.initMocks
import java.util.concurrent.TimeUnit.SECONDS

class DiscoveryModelTest {

    companion object {
        @JvmStatic private val INITIAL_DISCOVERING_EVENT = DiscoveryEvent.DiscoveringEvent()
        @JvmStatic private val STARTED_DISCOVERING_EVENT = DiscoveryEvent.DiscoveringEvent(isDiscovering = true)

        @JvmStatic private val TEST_DISCOVERED_DEVICE = DiscoverConnection.DiscoveredDevice("test1", DiscoveryState.FOUND, null)
        @JvmStatic private val TEST_DISCOVERED_DEVICE_B = DiscoverConnection.DiscoveredDevice("test2", DiscoveryState.FOUND, null)
        @JvmStatic private val TEST_USERNAME = "test user name"
        @JvmStatic private val TEST_ENDPOINT_ID = "test endpoint"

        @JvmStatic private val TEST_INITIATED_DEVICE =
                InboundDevice(TEST_ENDPOINT_ID, INITIATED, TEST_USERNAME)
        @JvmStatic private val TEST_CONNECTED_DEVICE =
                InboundDevice(TEST_ENDPOINT_ID, CONNECTED, TEST_USERNAME)

        @JvmStatic private val INITIAL_CONNECTION_EVENT =
                ConnectionEvent(connectionSuccess = false,
                                connectionRequested = true,
                                remoteUsername = TEST_USERNAME)

        @JvmStatic private val SUCCESSFUL_CONNECTION_EVENT =
                ConnectionEvent(connectionSuccess = true,
                                connectionRequested = false,
                                remoteUsername = "")

        @JvmStatic private val UNSUCCESSFUL_CONNECTION_EVENT =
                ConnectionEvent(connectionSuccess = false,
                                connectionRequested = false,
                                remoteUsername = "")
    }

    @Mock private lateinit var connectionManager: ConnectionManager
    private val testScheduler = TestScheduler()
    private val rxTestScheduler = RxTestScheduler(testScheduler)

    private lateinit var model: DiscoveryModel

    @Before fun setUp() {
        initMocks(this)
        model = DiscoveryModel(connectionManager, rxTestScheduler)
    }

    @After fun teardown() {
        model.onCleared()
    }

    @Test fun `given start discovery when successful then correct DiscoveringEvents`() {
        whenever(connectionManager.startDiscovery()).thenReturn(Single.fromCallable { true })

        val eventSubscriber: TestSubscriber<DiscoveryEvent> = model.observeEvents().test()
        model.start()
        testScheduler.advanceTimeBy(1, SECONDS)

        eventSubscriber.assertNoErrors()
        eventSubscriber.assertValueCount(1)

        eventSubscriber.assertValueAt(0, STARTED_DISCOVERING_EVENT)
    }

    @Test fun `given start discovery when unsuccessful then correct DiscoveringEvents`() {
        whenever(connectionManager.startDiscovery()).thenReturn(Single.fromCallable { false })

        val eventSubscriber: TestSubscriber<DiscoveryEvent> = model.observeEvents().test()
        model.start()
        testScheduler.advanceTimeBy(1, SECONDS)

        eventSubscriber.assertNoErrors()
        eventSubscriber.assertValueCount(1)

        eventSubscriber.assertValueAt(0, INITIAL_DISCOVERING_EVENT)
    }

    @Test fun `given listen for devices when devices discovered then correct DiscoveringEvents`() {
        val devicesEmpty = listOf<DiscoverConnection.DiscoveredDevice>()
        val devices1 = listOf(TEST_DISCOVERED_DEVICE)
        val devices2 = listOf(TEST_DISCOVERED_DEVICE, TEST_DISCOVERED_DEVICE_B)

        whenever(connectionManager.discoveredDevices()).thenReturn(
                Flowable.fromArray(
                        devicesEmpty,
                        devices1,
                        devices2,
                        devices1))

        val eventSubscriber: TestSubscriber<DiscoveryEvent> = model.observeEvents().test()
        model.start()
        testScheduler.advanceTimeBy(1, SECONDS)

        eventSubscriber.assertNoErrors()
        eventSubscriber.assertValueCount(4)

        eventSubscriber.assertValueAt(0, DiscoveryEvent.DevicesEvent(devicesEmpty))
        eventSubscriber.assertValueAt(1, DiscoveryEvent.DevicesEvent(devices1))
        eventSubscriber.assertValueAt(2, DiscoveryEvent.DevicesEvent(devices2))
        eventSubscriber.assertValueAt(3, DiscoveryEvent.DevicesEvent(devices1))
    }

    @Test fun `given request connection when successful then correct successful DiscoveringEvents`() {
        whenever(connectionManager.requestConnectionInitiation(eq(TEST_ENDPOINT_ID)))
                .thenReturn(Single.fromCallable { true })
        whenever(connectionManager.initiatedConnections())
                .thenReturn(Flowable.fromCallable { listOf(TEST_INITIATED_DEVICE) })
        whenever(connectionManager.acceptConnection(eq(TEST_ENDPOINT_ID)))
                .thenReturn(Single.fromCallable { true })
        whenever(connectionManager.activeConnections())
                .thenReturn(Flowable.fromCallable { listOf(TEST_CONNECTED_DEVICE) })

        val testSubscriber: TestSubscriber<DiscoveryEvent> = model.observeEvents().test()

        model.requestConnection(TEST_USERNAME, TEST_ENDPOINT_ID)
        testScheduler.advanceTimeBy(1, SECONDS)

        testSubscriber.assertNoErrors()
        testSubscriber.assertValueCount(2)
        testSubscriber.assertValueAt(0, INITIAL_CONNECTION_EVENT)
        testSubscriber.assertValueAt(1, SUCCESSFUL_CONNECTION_EVENT)
        verify(connectionManager, times(1)).requestConnectionInitiation(eq(TEST_ENDPOINT_ID))
        verify(connectionManager, times(1)).initiatedConnections()
        verify(connectionManager, times(1)).acceptConnection(eq(TEST_ENDPOINT_ID))
        verify(connectionManager, times(1)).activeConnections()
        verifyNoMoreInteractions(connectionManager)
    }

    @Test fun `given request connection when unsuccessful then correct unsuccessful DiscoveringEvents`() {
        whenever(connectionManager.requestConnectionInitiation(eq(TEST_ENDPOINT_ID)))
                .thenReturn(Single.fromCallable { false })

        val testSubscriber: TestSubscriber<DiscoveryEvent> = model.observeEvents().test()
        //val errorSubscriber: TestSubscriber<DiscoveryEvent.ErrorEvent> = model.observeErrorEvents().test()
        model.requestConnection(TEST_USERNAME, TEST_ENDPOINT_ID)
        testScheduler.advanceTimeBy(1, SECONDS)

        testSubscriber.assertNoErrors()
        testSubscriber.assertValueCount(2)
        testSubscriber.assertValueAt(0, INITIAL_CONNECTION_EVENT)
        testSubscriber.assertValueAt(1, UNSUCCESSFUL_CONNECTION_EVENT)
        //errorSubscriber.assertValueCount(1)
        verify(connectionManager, times(1)).requestConnectionInitiation(eq(TEST_ENDPOINT_ID))
        verifyNoMoreInteractions(connectionManager)
    }

    @Test fun `given request connection when unsuccessful with no initiated devices timeout then correct unsuccessful DiscoveringEvents`() {
        whenever(connectionManager.requestConnectionInitiation(eq(TEST_ENDPOINT_ID)))
                .thenReturn(Single.fromCallable { true })
        whenever(connectionManager.initiatedConnections())
                .thenReturn(BehaviorProcessor.createDefault(listOf()))

        val testSubscriber: TestSubscriber<DiscoveryEvent> = model.observeEvents().test()
       // val errorSubscriber: TestSubscriber<DiscoveryEvent.ErrorEvent> = model.observeErrorEvents().test()

        model.requestConnection(TEST_USERNAME, TEST_ENDPOINT_ID)
        testScheduler.advanceTimeBy(11, SECONDS)

        testSubscriber.assertNoErrors()
        testSubscriber.assertValueCount(2)
        testSubscriber.assertValueAt(0, INITIAL_CONNECTION_EVENT)
        testSubscriber.assertValueAt(1, UNSUCCESSFUL_CONNECTION_EVENT)
       // errorSubscriber.assertValueCount(1)
        verify(connectionManager, times(1)).requestConnectionInitiation(eq(TEST_ENDPOINT_ID))
        verify(connectionManager, times(1)).initiatedConnections()
        verifyNoMoreInteractions(connectionManager)
    }


    @Test fun `given request connection when unsuccessful with not accepted error then correct unsuccessful DiscoveringEvents`() {
        whenever(connectionManager.requestConnectionInitiation(eq(TEST_ENDPOINT_ID)))
                .thenReturn(Single.fromCallable { true })
        whenever(connectionManager.initiatedConnections())
                .thenReturn(Flowable.fromCallable { listOf(TEST_INITIATED_DEVICE) })
        whenever(connectionManager.acceptConnection(eq(TEST_ENDPOINT_ID)))
                .thenReturn(Single.fromCallable { false })

        val testSubscriber: TestSubscriber<DiscoveryEvent> = model.observeEvents().test()
        //val errorSubscriber: TestSubscriber<DiscoveryEvent.ErrorEvent> = model.observeErrorEvents().test()
        model.requestConnection(TEST_USERNAME, TEST_ENDPOINT_ID)
        testScheduler.advanceTimeBy(1, SECONDS)

        testSubscriber.assertNoErrors()
        testSubscriber.assertValueCount(2)
        testSubscriber.assertValueAt(0, INITIAL_CONNECTION_EVENT)
        testSubscriber.assertValueAt(1, UNSUCCESSFUL_CONNECTION_EVENT)
        //errorSubscriber.assertValueCount(1)
        verify(connectionManager, times(1)).requestConnectionInitiation(eq(TEST_ENDPOINT_ID))
        verify(connectionManager, times(1)).initiatedConnections()
        verify(connectionManager, times(1)).acceptConnection(eq(TEST_ENDPOINT_ID))
        verifyNoMoreInteractions(connectionManager)
    }

    @Test fun `given request connection when unsuccessful with connection timeout then correct unsuccessful DiscoveringEvents`() {
        whenever(connectionManager.requestConnectionInitiation(eq(TEST_ENDPOINT_ID)))
                .thenReturn(Single.fromCallable { true })
        whenever(connectionManager.initiatedConnections())
                .thenReturn(Flowable.fromCallable { listOf(TEST_INITIATED_DEVICE) })
        whenever(connectionManager.acceptConnection(eq(TEST_ENDPOINT_ID)))
                .thenReturn(Single.fromCallable { true })
        whenever(connectionManager.activeConnections())
                .thenReturn(BehaviorProcessor.createDefault(listOf()))

        val testSubscriber: TestSubscriber<DiscoveryEvent> = model.observeEvents().test()
       // val errorSubscriber: TestSubscriber<DiscoveryEvent.ErrorEvent> = model.observeErrorEvents().test()

        model.requestConnection(TEST_USERNAME, TEST_ENDPOINT_ID)
        testScheduler.advanceTimeBy(11, SECONDS)

        testSubscriber.assertNoErrors()
        testSubscriber.assertValueCount(2)
        testSubscriber.assertValueAt(0, INITIAL_CONNECTION_EVENT)
        testSubscriber.assertValueAt(1, UNSUCCESSFUL_CONNECTION_EVENT)
        //errorSubscriber.assertValueCount(1)
        verify(connectionManager, times(1)).requestConnectionInitiation(eq(TEST_ENDPOINT_ID))
        verify(connectionManager, times(1)).initiatedConnections()
        verify(connectionManager, times(1)).acceptConnection(eq(TEST_ENDPOINT_ID))
        verify(connectionManager, times(1)).activeConnections()
        verifyNoMoreInteractions(connectionManager)
    }
}