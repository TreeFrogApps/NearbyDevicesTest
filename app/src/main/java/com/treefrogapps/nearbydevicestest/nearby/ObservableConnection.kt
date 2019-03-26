package com.treefrogapps.nearbydevicestest.nearby


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
interface ObservableConnection<C, R, T> {

    /**
     * @return [C] the callback type
     */
    fun callback() : C

    /**
     * @return [R] additional options
     */
    fun options() : R

    /**
     * @return [Observable/Flowable] of type [T]
     */
    fun observe() : T
}