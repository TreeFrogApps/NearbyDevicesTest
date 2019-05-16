package com.treefrogapps.nearbydevicestest.app

import android.support.annotation.StringRes


data class ErrorEvent(@StringRes val reason : Int, val error : Throwable?)