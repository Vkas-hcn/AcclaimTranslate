package com.vkas.secondtranslation.stbean

import androidx.annotation.Keep

@Keep
data class StAdBean (
    var st_open: MutableList<StDetailBean> = ArrayList(),
    var st_back: MutableList<StDetailBean> = ArrayList(),
    var st_home: MutableList<StDetailBean> = ArrayList(),
    var st_translation: MutableList<StDetailBean> = ArrayList(),
    var st_language: MutableList<StDetailBean> = ArrayList(),

    var st_click_num: Int = 0,
    var st_show_num: Int = 0
)
@Keep
data class StDetailBean(
    val st_id: String,
    val st_platform: String,
    val st_type: String,
    val st_weight: Int
)