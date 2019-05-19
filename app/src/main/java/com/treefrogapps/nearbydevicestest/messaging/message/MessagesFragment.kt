package com.treefrogapps.nearbydevicestest.messaging.message

import com.treefrogapps.nearbydevicestest.R
import com.treefrogapps.nearbydevicestest.app.BaseViewModelInjectionFragment

class MessagesFragment : BaseViewModelInjectionFragment<MessagesViewModel, MessagesViewDataModel, MessagesModel, MessagesEvent>() {

    override fun layout(): Int = R.layout.messages_fragment

}
