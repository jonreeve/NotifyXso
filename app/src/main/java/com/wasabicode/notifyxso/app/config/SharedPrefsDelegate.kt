package com.wasabicode.notifyxso.app.config

import android.content.SharedPreferences
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

class SharedPrefsDelegate<T : Any?>(
    private val sharedPrefs: SharedPreferences,
    private val key: String? = null,
    private val defaultValue: T,
    private val get: SharedPreferences.(String, T) -> T,
    private val set: SharedPreferences.Editor.(String, T) -> SharedPreferences.Editor
) : ReadWriteProperty<Any, T> {

    override fun getValue(thisRef: Any, property: KProperty<*>): T =
        sharedPrefs.get(key ?: property.name, defaultValue)

    override fun setValue(thisRef: Any, property: KProperty<*>, value: T) =
        sharedPrefs.edit().set(key ?: property.name, value).apply()

    companion object {
        fun string(sharedPrefs: SharedPreferences, key: String? = null, defaultValue: String): SharedPrefsDelegate<String> =
            SharedPrefsDelegate(sharedPrefs, key, defaultValue, Companion::getNonNullString, SharedPreferences.Editor::putString)

        fun nullableString(sharedPrefs: SharedPreferences, key: String? = null, defaultValue: String? = null): SharedPrefsDelegate<String?> =
            SharedPrefsDelegate(sharedPrefs, key, defaultValue, SharedPreferences::getString, SharedPreferences.Editor::putString)

        fun stringSet(sharedPrefs: SharedPreferences, key: String? = null, defaultValue: Set<String> = emptySet()): SharedPrefsDelegate<Set<String>> =
            SharedPrefsDelegate(sharedPrefs, key, defaultValue, Companion::getNonNullStringSet, SharedPreferences.Editor::putStringSet)

        fun boolean(sharedPrefs: SharedPreferences, key: String? = null, defaultValue: Boolean = false): SharedPrefsDelegate<Boolean> =
            SharedPrefsDelegate(sharedPrefs, key, defaultValue, SharedPreferences::getBoolean, SharedPreferences.Editor::putBoolean)

        fun int(sharedPrefs: SharedPreferences, key: String? = null, defaultValue: Int = 0): SharedPrefsDelegate<Int> =
            SharedPrefsDelegate(sharedPrefs, key, defaultValue, SharedPreferences::getInt, SharedPreferences.Editor::putInt)

        fun float(sharedPrefs: SharedPreferences, key: String? = null, defaultValue: Float = 0f): SharedPrefsDelegate<Float> =
            SharedPrefsDelegate(sharedPrefs, key, defaultValue, SharedPreferences::getFloat, SharedPreferences.Editor::putFloat)

        private fun getNonNullString(sharedPrefs: SharedPreferences, key: String, defaultValue: String) =
            sharedPrefs.getString(key, defaultValue) ?: defaultValue

        private fun getNonNullStringSet(sharedPrefs: SharedPreferences, key: String, defaultValue: Set<String>) =
            sharedPrefs.getStringSet(key, defaultValue) ?: defaultValue
    }

    fun <U> mapped(get: (T) -> U, set: (U) -> T) = Mapped(get, set, this)

    class Mapped<T, U>(
        private val toGet: (T) -> U,
        private val toSet: (U) -> T,
        private val delegate: ReadWriteProperty<Any, T>
    ) : ReadWriteProperty<Any, U> {
        override fun getValue(thisRef: Any, property: KProperty<*>): U = toGet(delegate.getValue(thisRef, property))

        override fun setValue(thisRef: Any, property: KProperty<*>, value: U) {
            delegate.setValue(thisRef, property, toSet(value))
        }
    }
}

