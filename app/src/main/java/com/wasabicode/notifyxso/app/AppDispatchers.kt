package com.wasabicode.notifyxso.app

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainCoroutineDispatcher
import javax.inject.Inject

@Suppress("PropertyName") // matching Kotlin names
interface AppDispatchers {
    val IO: CoroutineDispatcher
    val Main: MainCoroutineDispatcher
    val Default: CoroutineDispatcher
}

class DefaultAppDispatchers @Inject constructor(): AppDispatchers {
    override val IO get() = Dispatchers.IO
    override val Main get() = Dispatchers.Main
    override val Default get() = Dispatchers.Default
}
