package com.treefrogapps.nearbydevicestest.nearby

import com.google.android.gms.nearby.connection.*
import com.google.android.gms.tasks.Task
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verifyNoMoreInteractions
import com.nhaarman.mockitokotlin2.whenever
import com.treefrogapps.nearbydevicestest.nearby.AdvertisingConnection.InboundDevice
import com.treefrogapps.nearbydevicestest.nearby.ConnectionState.CONNECTED
import com.treefrogapps.nearbydevicestest.nearby.ConnectionState.INITIATED
import com.treefrogapps.nearbydevicestest.nearby.DiscoverConnection.DiscoveredDevice
import com.treefrogapps.nearbydevicestest.nearby.DiscoveryState.FOUND
import io.reactivex.Flowable
import io.reactivex.Single
import io.reactivex.observers.TestObserver
import io.reactivex.schedulers.Schedulers
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.Mockito.CALLS_REAL_METHODS
import org.mockito.Mockito.verify
import org.mockito.MockitoAnnotations.initMocks
import java.util.function.Supplier

class ConnectionManagerTest {

    companion object {
        @Suppress("UNCHECKED_CAST")
        @JvmStatic private fun createTask(isSuccessful: Boolean): Task<Void> {
            val task: Task<Void> = Mockito.mock(Task::class.java, CALLS_REAL_METHODS) as Task<Void>
            whenever(task.isSuccessful).thenReturn(isSuccessful)
            return task
        }

        @JvmStatic private fun createDiscoveredDevice(id: String, state: DiscoveryState, name: String): DiscoveredDevice {
            val info = Mockito.mock(DiscoveredEndpointInfo::class.java)
            whenever(info.endpointName).thenReturn(name)
            return DiscoveredDevice(id, state, info)
        }

        @JvmStatic private fun createInboundDevice(id: String, state: ConnectionState, name: String): InboundDevice {
            return InboundDevice(id, state, name)
        }
    }

    @Mock private lateinit var connectionsClient: ConnectionsClient
    @Mock private lateinit var advertisingConnection: Connection<ConnectionLifecycleCallback, AdvertisingOptions, InboundDevice>
    @Mock private lateinit var discoveryConnection: Connection<EndpointDiscoveryCallback, DiscoveryOptions, DiscoveredDevice>
    @Mock private lateinit var payloadConnection: Connection<PayloadCallback, Unit, Pair<String, Payload>>
    @Mock private lateinit var taskDelegate: TaskDelegate
    @Mock private lateinit var payloadDelegate: PayloadDelegate
    private val endpointId = "test endpoint id"
    private val username = Supplier { "test remoteUsername" }
    private lateinit var manager: ConnectionManager

    @Before fun setUp() {
        initMocks(this)
    }

    @Test
    fun `given a device in a found state when requestConnectionInitiation then successful`() {
        val validTask = createTask(true)
        val callback = Mockito.mock(ConnectionLifecycleCallback::class.java)
        val endpointId = "test endpoint id"
        val state = FOUND
        val name = "test name"
        val discoveredDevice = createDiscoveredDevice(endpointId, state, name)
        whenever(advertisingConnection.callback()).thenReturn(callback)
        whenever(connectionsClient.requestConnection(eq(username.get()), eq(endpointId), eq(callback))).thenReturn(validTask)
        whenever(taskDelegate.toSingle(eq(validTask))).thenReturn(Single.just(true))

        initialiseManagerDiscovery(Flowable.just(discoveredDevice))
        val testSubscriber: TestObserver<Boolean> = manager.requestConnectionInitiation(endpointId).test().await()

        testSubscriber.assertNoErrors()
        testSubscriber.assertValueAt(0, true)
        verify(advertisingConnection, times(1)).observe()
        verify(advertisingConnection, times(1)).observeErrors()
        verify(advertisingConnection, times(1)).callback()
        verify(connectionsClient, times(1)).requestConnection(eq(username.get()), eq(endpointId), eq(callback))
        verify(taskDelegate, times(1)).toSingle(eq(validTask))
        verifyNoMoreInteractions(advertisingConnection, connectionsClient, taskDelegate)
    }

    @Test
    fun `given a device in a found state when requestConnectionInitiation task fails then unsuccessful`() {
        val invalidTask = createTask(false)
        val callback = Mockito.mock(ConnectionLifecycleCallback::class.java)
        val endpointId = "test endpoint id"
        val state = FOUND
        val name = "test name"
        val discoveredDevice = createDiscoveredDevice(endpointId, state, name)
        whenever(advertisingConnection.callback()).thenReturn(callback)
        whenever(connectionsClient.requestConnection(eq(username.get()), eq(endpointId), eq(callback))).thenReturn(invalidTask)
        whenever(taskDelegate.toSingle(eq(invalidTask))).thenReturn(Single.just(false))

        initialiseManagerDiscovery(Flowable.just(discoveredDevice))
        val testSubscriber: TestObserver<Boolean> = manager.requestConnectionInitiation(endpointId).test().await()

        testSubscriber.assertNoErrors()
        testSubscriber.assertValueAt(0, false)
        verify(advertisingConnection, times(1)).observe()
        verify(advertisingConnection, times(1)).observeErrors()
        verify(advertisingConnection, times(1)).callback()
        verify(connectionsClient, times(1)).requestConnection(eq(username.get()), eq(endpointId), eq(callback))
        verify(taskDelegate, times(1)).toSingle(eq(invalidTask))
        verifyNoMoreInteractions(advertisingConnection, connectionsClient, taskDelegate)
    }

    @Test
    fun `given a device not in a found state when requestConnectionInitiation then not successful`() {
        initialiseManagerDiscovery(Flowable.empty())
        val testSubscriber: TestObserver<Boolean> = manager.requestConnectionInitiation(endpointId).test().await()

        testSubscriber.assertError(ConnectionException::class.java)
        verify(advertisingConnection, times(1)).observe()
        verify(advertisingConnection, times(1)).observeErrors()
        verifyNoMoreInteractions(advertisingConnection, connectionsClient, taskDelegate)
    }

    @Test
    fun `given one device in a found state but not requested endpoint when requestConnectionInitiation then error thrown`() {
        val endpointId = "test endpoint id"
        val state = FOUND
        val name = "test name"
        val discoveredDevice = createDiscoveredDevice(endpointId, state, name)

        val anotherEndpoint = "not found endpoint Id"

        initialiseManagerDiscovery(Flowable.just(discoveredDevice))
        val testSubscriber: TestObserver<Boolean> = manager.requestConnectionInitiation(anotherEndpoint).test().await()

        testSubscriber.assertError(ConnectionException::class.java)
        verify(advertisingConnection, times(1)).observe()
        verify(advertisingConnection, times(1)).observeErrors()
        verifyNoMoreInteractions(advertisingConnection, connectionsClient, taskDelegate)
    }

    @Test
    fun `given a device in an initiated state when accept connection then successful return`() {
        val validTask = createTask(true)
        val callback = Mockito.mock(PayloadCallback::class.java)
        val endpointId = "test endpoint id"
        val state = INITIATED
        val name = "test name"
        val inboundDevice = createInboundDevice(endpointId, state, name)
        whenever(payloadConnection.callback()).thenReturn(callback)
        whenever(connectionsClient.acceptConnection(eq(endpointId), eq(callback))).thenReturn(validTask)
        whenever(taskDelegate.toSingle(eq(validTask))).thenReturn(Single.just(true))

        initialiseManagerAdvertising(Flowable.just(inboundDevice))
        val testSubscriber: TestObserver<Boolean> = manager.acceptConnection(endpointId).test().await()

        testSubscriber.assertNoErrors()
        testSubscriber.assertValueAt(0, true)
        verify(payloadConnection, times(1)).observe()
        verify(payloadConnection, times(1)).observeErrors()
        verify(payloadConnection, times(1)).callback()
        verify(connectionsClient, times(1)).acceptConnection(eq(endpointId), eq(callback))
        verify(taskDelegate, times(1)).toSingle(eq(validTask))
        verifyNoMoreInteractions(payloadConnection, connectionsClient, taskDelegate)
    }

    @Test
    fun `given a device in an initiated state when acceptConnection task fails then unsuccessful`() {
        val invalidTask = createTask(false)
        val callback = Mockito.mock(PayloadCallback::class.java)
        val endpointId = "test endpoint id"
        val state = INITIATED
        val name = "test name"
        val inboundDevice = createInboundDevice(endpointId, state, name)
        whenever(payloadConnection.callback()).thenReturn(callback)
        whenever(connectionsClient.acceptConnection(eq(endpointId), eq(callback))).thenReturn(invalidTask)
        whenever(taskDelegate.toSingle(eq(invalidTask))).thenReturn(Single.just(false))

        initialiseManagerAdvertising(Flowable.just(inboundDevice))
        val testSubscriber: TestObserver<Boolean> = manager.acceptConnection(endpointId).test().await()

        testSubscriber.assertNoErrors()
        testSubscriber.assertValueAt(0, false)
        verify(payloadConnection, times(1)).observe()
        verify(payloadConnection, times(1)).observeErrors()
        verify(payloadConnection, times(1)).callback()
        verify(connectionsClient, times(1)).acceptConnection(eq(endpointId), eq(callback))
        verify(taskDelegate, times(1)).toSingle(eq(invalidTask))
        verifyNoMoreInteractions(payloadConnection, connectionsClient, taskDelegate)
    }

    @Test
    fun `given one device in an initiated state but not requested endpoint when acceptConnection then error thrown`() {
        val endpointId = "test endpoint id"
        val state = INITIATED
        val name = "test name"
        val inboundDevice = createInboundDevice(endpointId, state, name)

        val anotherEndpoint = "not found endpoint Id"

        initialiseManagerAdvertising(Flowable.just(inboundDevice))
        val testSubscriber: TestObserver<Boolean> = manager.acceptConnection(anotherEndpoint).test().await()

        testSubscriber.assertError(ConnectionException::class.java)
        verify(payloadConnection, times(1)).observeErrors()
        verify(payloadConnection, times(1)).observe()
        verifyNoMoreInteractions(payloadConnection, connectionsClient, taskDelegate)
    }

    @Test
    fun `given a device in an initiated state when reject connection then successful return`() {
        val validTask = createTask(true)
        val endpointId = "test endpoint id"
        val state = INITIATED
        val name = "test name"
        val inboundDevice = createInboundDevice(endpointId, state, name)
        whenever(connectionsClient.rejectConnection(eq(endpointId))).thenReturn(validTask)
        whenever(taskDelegate.toSingle(eq(validTask))).thenReturn(Single.just(true))

        initialiseManagerAdvertising(Flowable.just(inboundDevice))
        val testSubscriber: TestObserver<Boolean> = manager.rejectConnection(endpointId).test().await()

        testSubscriber.assertNoErrors()
        testSubscriber.assertValueAt(0, true)
        verify(connectionsClient, times(1)).rejectConnection(eq(endpointId))
        verify(taskDelegate, times(1)).toSingle(eq(validTask))
        verifyNoMoreInteractions(connectionsClient, taskDelegate)
    }

    @Test
    fun `given a device in an initiated state when reject connection task fails then unsuccessful`() {
        val invalidTask = createTask(false)
        val endpointId = "test endpoint id"
        val state = INITIATED
        val name = "test name"
        val inboundDevice = createInboundDevice(endpointId, state, name)

        whenever(taskDelegate.toSingle(eq(invalidTask))).thenReturn(Single.just(false))
        whenever(connectionsClient.rejectConnection(eq(endpointId))).thenReturn(invalidTask)

        initialiseManagerAdvertising(Flowable.just(inboundDevice))
        val testSubscriber: TestObserver<Boolean> = manager.rejectConnection(endpointId).test().await()

        testSubscriber.assertNoErrors()
        testSubscriber.assertValueAt(0, false)
        verify(connectionsClient, times(1)).rejectConnection(eq(endpointId))
        verify(taskDelegate, times(1)).toSingle(eq(invalidTask))
        verifyNoMoreInteractions(connectionsClient, taskDelegate)
    }

    @Test
    fun `given one device in an initiated state but not requested endpoint when reject connection then error thrown`() {
        val endpointId = "test endpoint id"
        val state = INITIATED
        val name = "test name"
        val inboundDevice = createInboundDevice(endpointId, state, name)

        val anotherEndpoint = "not found endpoint Id"

        initialiseManagerAdvertising(Flowable.just(inboundDevice))
        val testSubscriber: TestObserver<Boolean> = manager.rejectConnection(anotherEndpoint).test().await()

        testSubscriber.assertError(ConnectionException::class.java)
        verifyNoMoreInteractions(connectionsClient, taskDelegate)
    }

    @Test
    fun `given a device in an connected state when send payload connection then successful return`() {
        val validTask = createTask(true)
        val endpointId = "test endpoint id"
        val state = CONNECTED
        val name = "test name"
        val inboundDevice = createInboundDevice(endpointId, state, name)
        val bytes = "test message".toByteArray()
        val payload = Mockito.mock(Payload::class.java)
        whenever(payloadDelegate.fromBytes(eq(bytes))).thenReturn(payload)
        whenever(connectionsClient.sendPayload(eq(endpointId), eq(payload))).thenReturn(validTask)
        whenever(taskDelegate.toSingle(eq(validTask))).thenReturn(Single.just(true))

        initialiseManagerAdvertising(Flowable.just(inboundDevice))
        val testSubscriber: TestObserver<Boolean> = manager.sendMessage(endpointId, bytes).test().await()

        testSubscriber.assertNoErrors()
        testSubscriber.assertValueAt(0, true)
        verify(connectionsClient, times(1)).sendPayload(eq(endpointId), eq(payload))
        verify(taskDelegate, times(1)).toSingle(eq(validTask))
        verify(payloadDelegate, times(1)).fromBytes(eq(bytes))
        verifyNoMoreInteractions(connectionsClient, payloadDelegate, taskDelegate)
    }

    @Test
    fun `given a device in an initiated state when send payload task fails then exception thrown`() {
        val endpointId = "test endpoint id"
        val state = INITIATED
        val name = "test name"
        val inboundDevice = createInboundDevice(endpointId, state, name)
        val bytes = "test message".toByteArray()

        initialiseManagerAdvertising(Flowable.just(inboundDevice))
        val testSubscriber: TestObserver<Boolean> = manager.sendMessage(endpointId, bytes).test().await()

        testSubscriber.assertError(ConnectionException::class.java)
        verifyNoMoreInteractions(connectionsClient, taskDelegate)
    }

    @Test
    fun `given a device in an connected state when send payload task fails then unsuccessful`() {
        val invalidTask = createTask(false)
        val endpointId = "test endpoint id"
        val state = CONNECTED
        val name = "test name"
        val inboundDevice = createInboundDevice(endpointId, state, name)
        val bytes = "test message".toByteArray()
        val payload = Mockito.mock(Payload::class.java)
        whenever(payloadDelegate.fromBytes(eq(bytes))).thenReturn(payload)
        whenever(connectionsClient.sendPayload(eq(endpointId), eq(payload))).thenReturn(invalidTask)
        whenever(taskDelegate.toSingle(eq(invalidTask))).thenReturn(Single.just(false))


        initialiseManagerAdvertising(Flowable.just(inboundDevice))
        val testSubscriber: TestObserver<Boolean> = manager.sendMessage(endpointId, bytes).test().await()

        testSubscriber.assertNoErrors()
        testSubscriber.assertValueAt(0, false)
        verify(connectionsClient, times(1)).sendPayload(eq(endpointId), eq(payload))
        verify(taskDelegate, times(1)).toSingle(eq(invalidTask))
        verifyNoMoreInteractions(connectionsClient, taskDelegate)
    }

    @Test
    fun `given one device in an connected state but not requested endpoint when send paylod then error thrown`() {
        val endpointId = "test endpoint id"
        val state = CONNECTED
        val name = "test name"
        val inboundDevice = createInboundDevice(endpointId, state, name)
        val bytes = "test message".toByteArray()
        val anotherEndpoint = "not found endpoint Id"

        initialiseManagerAdvertising(Flowable.just(inboundDevice))
        val testSubscriber: TestObserver<Boolean> = manager.sendMessage(anotherEndpoint, bytes).test().await()

        testSubscriber.assertError(ConnectionException::class.java)
        verifyNoMoreInteractions(connectionsClient, taskDelegate)
    }


    private fun initialiseManagerDiscovery(flowable: Flowable<DiscoveredDevice>) {
        whenever(discoveryConnection.observe()).thenReturn(flowable)
        whenever(discoveryConnection.observeErrors()).thenReturn(Flowable.never())
        whenever(advertisingConnection.observe()).thenReturn(Flowable.never())
        whenever(advertisingConnection.observeErrors()).thenReturn(Flowable.never())
        whenever(payloadConnection.observe()).thenReturn(Flowable.never())
        whenever(payloadConnection.observeErrors()).thenReturn(Flowable.never())

        manager = ConnectionManager(connectionsClient,
                                    discoveryConnection,
                                    advertisingConnection,
                                    payloadConnection,
                                    endpointId,
                                    Schedulers.trampoline(),
                                    username,
                                    taskDelegate,
                                    payloadDelegate)
    }

    private fun initialiseManagerAdvertising(flowable: Flowable<InboundDevice>) {
        whenever(discoveryConnection.observe()).thenReturn(Flowable.never())
        whenever(discoveryConnection.observeErrors()).thenReturn(Flowable.never())
        whenever(advertisingConnection.observe()).thenReturn(flowable)
        whenever(advertisingConnection.observeErrors()).thenReturn(Flowable.never())
        whenever(payloadConnection.observe()).thenReturn(Flowable.never())
        whenever(payloadConnection.observeErrors()).thenReturn(Flowable.never())

        manager = ConnectionManager(connectionsClient,
                                    discoveryConnection,
                                    advertisingConnection,
                                    payloadConnection,
                                    endpointId,
                                    Schedulers.trampoline(),
                                    username,
                                    taskDelegate,
                                    payloadDelegate)
    }
}