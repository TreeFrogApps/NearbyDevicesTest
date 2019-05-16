package com.treefrogapps.nearbydevicestest.messaging.message

import com.treefrogapps.nearbydevicestest.R
import com.treefrogapps.nearbydevicestest.app.BaseViewModelInjectionFragment
import com.treefrogapps.nearbydevicestest.messaging.message.MessagesViewModel.DataModel

class MessagesFragment : BaseViewModelInjectionFragment<MessagesViewModel, DataModel>() {

    override fun layout(): Int = R.layout.messages_fragment

}
