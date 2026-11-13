package com.example.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.model.CloudConfig
import com.example.model.CustomerProfile
import com.example.model.DeliveryZone
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.json.JSONArray
import org.json.JSONObject

private val Context.appDataStore: DataStore<Preferences> by preferencesDataStore(name = "akleh_helwa_prefs")

class AppPreferences(private val context: Context) {

    private val servicePrefs by lazy {
        context.getSharedPreferences(SERVICE_PREFS_NAME, Context.MODE_PRIVATE)
    }

    private object Keys {
        val IS_DARK_MODE = booleanPreferencesKey("is_dark_mode")
        val ADMIN_PIN = stringPreferencesKey("admin_pin")
        val FIREBASE_PROJECT_ID = stringPreferencesKey("firebase_project_id")
        val FIREBASE_APP_ID = stringPreferencesKey("firebase_app_id")
        val FIREBASE_API_KEY = stringPreferencesKey("firebase_api_key")
        val ADMIN_WHATSAPP_NUMBER = stringPreferencesKey("admin_whatsapp_number")

        val CUSTOMER_USER_ID = stringPreferencesKey("customer_user_id")
        val CUSTOMER_USERNAME = stringPreferencesKey("customer_username")
        val CUSTOMER_PASSWORD = stringPreferencesKey("customer_password")
        val CUSTOMER_PHONE = stringPreferencesKey("customer_phone")
        val CUSTOMER_ADDRESS = stringPreferencesKey("customer_address")

        val IS_ADMIN_DEVICE = booleanPreferencesKey("is_admin_device")
        val FAVORITE_MENU_IDS = stringPreferencesKey("favorite_menu_ids")
        val LAST_REORDER_JSON = stringPreferencesKey("last_reorder_json")

        val KITCHEN_OPEN = booleanPreferencesKey("kitchen_open")
        val DAILY_OFFER_TEXT = stringPreferencesKey("daily_offer_text")
        val DELIVERY_ZONES_JSON = stringPreferencesKey("delivery_zones_json")
    }

    // Gourmet Dark Mode is the default on fresh launch
    val isDarkMode: Flow<Boolean> = context.appDataStore.data.map { prefs ->
        prefs[Keys.IS_DARK_MODE] ?: true
    }

    val adminPin: Flow<String> = context.appDataStore.data.map { prefs ->
        prefs[Keys.ADMIN_PIN]?.takeIf { it.isNotBlank() } ?: DEFAULT_ADMIN_PIN
    }

    val adminWhatsAppNumber: Flow<String> = context.appDataStore.data.map { prefs ->
        prefs[Keys.ADMIN_WHATSAPP_NUMBER].orEmpty()
    }

    val cloudConfig: Flow<CloudConfig> = context.appDataStore.data.map { prefs ->
        CloudConfig(
            projectId = prefs[Keys.FIREBASE_PROJECT_ID]?.takeIf { it.isNotBlank() } ?: CloudConfig.DEFAULT_PROJECT_ID,
            appId = prefs[Keys.FIREBASE_APP_ID]?.takeIf { it.isNotBlank() } ?: CloudConfig.DEFAULT_APP_ID,
            apiKey = prefs[Keys.FIREBASE_API_KEY]?.takeIf { it.isNotBlank() } ?: CloudConfig.DEFAULT_API_KEY
        )
    }

    val customerProfile: Flow<CustomerProfile> = context.appDataStore.data.map { prefs ->
        CustomerProfile(
            userId = prefs[Keys.CUSTOMER_USER_ID].orEmpty(),
            username = prefs[Keys.CUSTOMER_USERNAME].orEmpty(),
            password = prefs[Keys.CUSTOMER_PASSWORD].orEmpty(),
            phone = prefs[Keys.CUSTOMER_PHONE].orEmpty(),
            address = prefs[Keys.CUSTOMER_ADDRESS].orEmpty()
        )
    }

    val isAdminDevice: Flow<Boolean> = context.appDataStore.data.map { prefs ->
        prefs[Keys.IS_ADMIN_DEVICE] ?: false
    }

    val favoriteMenuItemIds: Flow<Set<Long>> = context.appDataStore.data.map { prefs ->
        val raw = prefs[Keys.FAVORITE_MENU_IDS].orEmpty()
        if (raw.isBlank()) emptySet()
        else raw.split(",").mapNotNull { it.trim().toLongOrNull() }.toSet()
    }

    val lastReorderItems: Flow<List<Pair<Long, Double>>> = context.appDataStore.data.map { prefs ->
        decodeReorderJson(prefs[Keys.LAST_REORDER_JSON].orEmpty())
    }

    val kitchenOpen: Flow<Boolean> = context.appDataStore.data.map { prefs ->
        prefs[Keys.KITCHEN_OPEN] ?: true
    }

    val dailyOfferText: Flow<String> = context.appDataStore.data.map { prefs ->
        prefs[Keys.DAILY_OFFER_TEXT].orEmpty()
    }

    val localDeliveryZones: Flow<List<DeliveryZone>> = context.appDataStore.data.map { prefs ->
        decodeDeliveryZones(prefs[Keys.DELIVERY_ZONES_JSON]) ?: DeliveryZone.DEFAULT_ZONES
    }

    suspend fun setDarkMode(enabled: Boolean) {
        context.appDataStore.edit { prefs -> prefs[Keys.IS_DARK_MODE] = enabled }
    }

    suspend fun setAdminPin(newPin: String) {
        val clean = newPin.trim()
        if (clean.length < 4) return
        context.appDataStore.edit { prefs -> prefs[Keys.ADMIN_PIN] = clean }
    }

    suspend fun setAdminWhatsAppNumber(phoneNumber: String) {
        context.appDataStore.edit { prefs -> prefs[Keys.ADMIN_WHATSAPP_NUMBER] = phoneNumber.trim() }
    }

    suspend fun saveCustomerProfile(profile: CustomerProfile) {
        context.appDataStore.edit { prefs ->
            prefs[Keys.CUSTOMER_USER_ID] = profile.userId
            prefs[Keys.CUSTOMER_USERNAME] = profile.username.trim()
            prefs[Keys.CUSTOMER_PASSWORD] = profile.password
            if (profile.phone.isNotBlank()) prefs[Keys.CUSTOMER_PHONE] = profile.phone.trim()
            if (profile.address.isNotBlank()) prefs[Keys.CUSTOMER_ADDRESS] = profile.address.trim()
        }
    }

    suspend fun updateCustomerContactInfo(phone: String, address: String) {
        context.appDataStore.edit { prefs ->
            prefs[Keys.CUSTOMER_PHONE] = phone.trim()
            prefs[Keys.CUSTOMER_ADDRESS] = address.trim()
        }
    }

    suspend fun setAdminDevice(isAdmin: Boolean) {
        servicePrefs.edit().putBoolean(KEY_SERVICE_IS_ADMIN, isAdmin).apply()
        context.appDataStore.edit { prefs -> prefs[Keys.IS_ADMIN_DEVICE] = isAdmin }
    }

    fun isAdminDeviceSync(): Boolean {
        return servicePrefs.getBoolean(KEY_SERVICE_IS_ADMIN, false)
    }

    suspend fun toggleFavoriteMenuItem(menuItemId: Long) {
        context.appDataStore.edit { prefs ->
            val current = prefs[Keys.FAVORITE_MENU_IDS].orEmpty()
                .split(",")
                .mapNotNull { it.trim().toLongOrNull() }
                .toMutableSet()
            if (!current.add(menuItemId)) {
                current.remove(menuItemId)
            }
            prefs[Keys.FAVORITE_MENU_IDS] = current.joinToString(",")
        }
    }

    suspend fun saveLastReorderItems(items: List<Pair<Long, Double>>) {
        context.appDataStore.edit { prefs ->
            prefs[Keys.LAST_REORDER_JSON] = encodeReorderJson(items)
        }
    }

    suspend fun setKitchenOpen(isOpen: Boolean) {
        context.appDataStore.edit { prefs -> prefs[Keys.KITCHEN_OPEN] = isOpen }
    }

    suspend fun setDailyOfferText(text: String) {
        context.appDataStore.edit { prefs -> prefs[Keys.DAILY_OFFER_TEXT] = text.trim() }
    }

    suspend fun saveDeliveryZones(zones: List<DeliveryZone>) {
        context.appDataStore.edit { prefs ->
            prefs[Keys.DELIVERY_ZONES_JSON] = encodeDeliveryZones(zones)
        }
    }

    suspend fun saveCloudConfig(projectId: String, appId: String, apiKey: String) {
        context.appDataStore.edit { prefs ->
            prefs[Keys.FIREBASE_PROJECT_ID] = projectId.trim()
            prefs[Keys.FIREBASE_APP_ID] = appId.trim()
            prefs[Keys.FIREBASE_API_KEY] = apiKey.trim()
        }
    }

    suspend fun clearCloudConfig() {
        context.appDataStore.edit { prefs ->
            prefs.remove(Keys.FIREBASE_PROJECT_ID)
            prefs.remove(Keys.FIREBASE_APP_ID)
            prefs.remove(Keys.FIREBASE_API_KEY)
        }
    }

    companion object {
        const val DEFAULT_ADMIN_PIN = "1234"
        private const val SERVICE_PREFS_NAME = "akleh_helwa_service_prefs"
        private const val KEY_SERVICE_IS_ADMIN = "is_admin_device"

        fun encodeDeliveryZones(zones: List<DeliveryZone>): String {
            val arr = JSONArray()
            zones.forEach { z ->
                arr.put(
                    JSONObject().apply {
                        put("id", z.id)
                        put("name", z.name)
                        put("fee", z.fee)
                    }
                )
            }
            return arr.toString()
        }

        fun decodeDeliveryZones(json: String?): List<DeliveryZone>? {
            if (json.isNullOrBlank()) return null
            return try {
                val arr = JSONArray(json)
                val list = buildList {
                    for (i in 0 until arr.length()) {
                        val obj = arr.optJSONObject(i) ?: continue
                        val name = obj.optString("name", "").trim()
                        if (name.isNotBlank()) {
                            add(
                                DeliveryZone(
                                    id = obj.optString("id", "zone_$i"),
                                    name = name,
                                    fee = obj.optDouble("fee", 0.0).coerceAtLeast(0.0)
                                )
                            )
                        }
                    }
                }
                list.ifEmpty { null }
            } catch (_: Exception) {
                null
            }
        }

        private fun encodeReorderJson(items: List<Pair<Long, Double>>): String {
            val arr = JSONArray()
            items.forEach { (id, qty) ->
                arr.put(JSONObject().apply {
                    put("id", id)
                    put("qty", qty)
                })
            }
            return arr.toString()
        }

        private fun decodeReorderJson(json: String): List<Pair<Long, Double>> {
            if (json.isBlank()) return emptyList()
            return try {
                val arr = JSONArray(json)
                buildList {
                    for (i in 0 until arr.length()) {
                        val obj = arr.optJSONObject(i) ?: continue
                        val id = obj.optLong("id", 0L)
                        val qty = obj.optDouble("qty", 1.0)
                        if (id > 0L && qty > 0.0) add(id to qty)
                    }
                }
            } catch (_: Exception) {
                emptyList()
            }
        }
    }
}
