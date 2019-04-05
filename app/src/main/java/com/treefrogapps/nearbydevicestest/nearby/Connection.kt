package com.treefrogapps.nearbydevicestest.nearby

import io.reactivex.Flowable


/**
 * Generic Interface for all connection types :
 *
 *  - Advertising
 *  - Discovery
 *  - Payload
 *
 *  @param [C] Callback type
 *  @param [R] additional options
 *  @param [T] Observable class
 */
interface Connection<C, R, T> {

    /**
     * @return [C] the callback type
     */
    fun callback() : C

    /**
     * @return [R] additional options for the connection
     */
    fun options() : R

    /**
     * @return [Flowable] of type [T]
     */
    fun observe() : Flowable<T>

    /**
     * Publish error streams separately as [observe] and [observeErrors] are mutually exclusive
     * and doesn't necessarily warrant a failure
     * @return [Flowable] of type [ConnectionError]
     */
    fun observeErrors() : Flowable<ConnectionError>
}