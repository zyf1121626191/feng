package com.example.myapp.model.common.util

import com.youth.xframe.utils.XPreferencesUtils
import okhttp3.Cookie
import okhttp3.CookieJar
import okhttp3.HttpUrl
import java.util.*

class CookieJarImpl : CookieJar {

    private val cookieStore = HashMap<String, List<Cookie>>()

    override fun loadForRequest(url: HttpUrl): List<Cookie> {
        return cookieStore[url.host] ?: ArrayList()
    }

    override fun saveFromResponse(url: HttpUrl, cookies: List<Cookie>) {
        cookieStore[url.host] = cookies
        XPreferencesUtils.put(url.host, cookies)
    }
}