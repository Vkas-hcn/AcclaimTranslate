package com.vkas.secondtranslation.utils

import com.google.gson.reflect.TypeToken
import com.vkas.secondtranslation.R
import com.vkas.secondtranslation.app.App.Companion.mmkvSt
import com.vkas.secondtranslation.event.Constant
import com.vkas.secondtranslation.stbean.StAdBean
import com.vkas.secondtranslation.stbean.StDetailBean
import com.xuexiang.xui.utils.Utils
import com.xuexiang.xutil.net.JsonUtil
import com.xuexiang.xutil.resource.ResourceUtils

object AcclaimUtils {
     val langIconMap by lazy {
        hashMapOf<String, Int>().apply {
            put("af", R.drawable.za)//SOUTH AFRICA
            put("sq", R.drawable.al)//ALBANIA
            put("ar", R.drawable.dz)//ALGERIA
            put("be", R.drawable.by)//BELARUS
            put("bg", R.drawable.bg)//BULGARIA
            put("bn", R.drawable.bd)//BANGLADESH
            put("ca", R.drawable.ad)//ANDORRA
            put("zh", R.drawable.cn)//CHINA
            put("hr", R.drawable.hr)//CROATIA
            put("cs", R.drawable.cz)//CZSTH REPUBLIC(Czechia)
            put("da", R.drawable.dk)//DENMARK
            put("nl", R.drawable.nl)//NETHERLANDS
            put("en", R.drawable.us)//UNITED STATES
            put("eo", R.mipmap.ic_launcher_round) //Esperanto世界语
            put("et", R.drawable.ee)//ESTONIA
            put("fi", R.mipmap.ic_launcher_round) //FINLAND
            put("fr", R.drawable.fr)//FRANCE
            put("gl", R.drawable.es)//Galician(partly SPAIN)
            put("ka", R.drawable.ge)//GEORGIA
            put("de", R.drawable.de)//GERMANY
            put("el", R.drawable.gr)//GRESTE
            put("gu", R.drawable.india) //Gujarati(partly INDIA)
            put("ht", R.mipmap.ic_launcher_round) //HAITIAN_CREOLE(partly Haiti)
            put("he", R.drawable.il)//ISRAEL
            put("hi", R.drawable.india) //Hindi(partly INDIA)
            put("hu", R.drawable.hu)//HUNGARY
            put("is", R.mipmap.ic_launcher_round) //ICELAND
            put("id", R.drawable.id)//INDONESIA
            put("ga", R.drawable.ie)//IRELAND
            put("it", R.drawable.it)//ITALY
            put("ja", R.drawable.jp)//JAPAN
            put("kn", R.drawable.india) //Kannada(partly INDIA)
            put("ko", R.drawable.kr)//KOREA
            put("lt", R.drawable.lt)//LITHUANIA
            put("lv", R.drawable.lv)//LATVIA
            put("mk", R.drawable.mk)//North Macedonia
            put("mr", R.drawable.india) //Marathi(partly INDIA)
            put("ms", R.drawable.my)//MALAYSIA
            put("mt", R.drawable.mt)//MALTA
            put("no", R.drawable.no)//NORWAY
            put("fa", R.drawable.ir)//PERSIAN(partly IRAN)
            put("pl", R.drawable.pl)//POLAND
            put("pt", R.drawable.pt)//PORTUGAL
            put("ro", R.drawable.ro)//ROMANIA
            put("ru", R.drawable.ru)//RUSSIA
            put("sk", R.drawable.sk)//SLOVAKIA
            put("sl", R.drawable.si)//SLOVENIA
            put("es", R.drawable.es)//SPAIN
            put("sv", R.drawable.se)//SWEDEN
            put("sw", R.mipmap.ic_launcher_round) //UGANDA
            put("tl", R.drawable.ph)//TAGALOG(partly Philippines)
            put("ta", R.drawable.india) //TAMIL(partly INDIA)
            put("te", R.drawable.india) //Telugu(partly INDIA)
            put("th", R.drawable.th)//THAILAND
            put("tr", R.drawable.tr)//TURKEY
            put("uk", R.drawable.ua)//UKRAINE
            put("ur", R.drawable.pk)//URDU(partly Pakistan)
            put("vi", R.drawable.vn)//VIETNAM
            put("cy", R.drawable.gb)//WELSH(partly United Kingdom)
        }
    }


    /**
     * 广告排序
     */
    private fun adSortingSt(elAdBean: StAdBean): StAdBean {
        val adBean = StAdBean()
        adBean.st_open = sortByWeightDescending(elAdBean.st_open) { it.st_weight }.toMutableList()
        adBean.st_back = sortByWeightDescending(elAdBean.st_back) { it.st_weight }.toMutableList()
        adBean.st_home = sortByWeightDescending(elAdBean.st_home) { it.st_weight }.toMutableList()
        adBean.st_translation = sortByWeightDescending(elAdBean.st_translation) { it.st_weight }.toMutableList()
        adBean.st_language = sortByWeightDescending(elAdBean.st_language) { it.st_weight }.toMutableList()
        adBean.st_show_num = elAdBean.st_show_num
        adBean.st_click_num = elAdBean.st_click_num
        return adBean
    }
    /**
     * 根据权重降序排序并返回新的列表
     */
    private fun <T> sortByWeightDescending(list: List<T>, getWeight: (T) -> Int): List<T> {
        return list.sortedByDescending(getWeight)
    }

    /**
     * 取出排序后的广告ID
     */
    fun takeSortedAdIDSt(index: Int, elAdDetails: MutableList<StDetailBean>): String {
        return elAdDetails.getOrNull(index)?.st_id ?: ""
    }

    /**
     * 获取广告服务器数据
     */
    fun getAdServerDataSt(): StAdBean {
        val serviceData: StAdBean =
            if (Utils.isNullOrEmpty(mmkvSt.decodeString(Constant.ADVERTISING_ST_DATA))) {
                JsonUtil.fromJson(
                    ResourceUtils.readStringFromAssert(Constant.AD_LOCAL_FILE_NAME_ST),
                    object : TypeToken<
                            StAdBean?>() {}.type
                )
            } else {
                JsonUtil.fromJson(
                    mmkvSt.decodeString(Constant.ADVERTISING_ST_DATA),
                    object : TypeToken<StAdBean?>() {}.type
                )
            }
        return adSortingSt(serviceData)
    }

    /**
     * 是否达到阀值
     */
    fun isThresholdReached(): Boolean {
        val clicksCount = mmkvSt.decodeInt(Constant.CLICKS_ST_COUNT, 0)
        val showCount = mmkvSt.decodeInt(Constant.SHOW_ST_COUNT, 0)
        KLog.e("TAG", "clicksCount=${clicksCount}, showCount=${showCount}")
        KLog.e(
            "TAG",
            "st_click_num=${getAdServerDataSt().st_click_num}, getAdServerData().st_show_num=${getAdServerDataSt().st_show_num}"
        )
        if (clicksCount >= getAdServerDataSt().st_click_num || showCount >= getAdServerDataSt().st_show_num) {
            return true
        }
        return false
    }

    /**
     * 记录广告展示次数
     */
    fun recordNumberOfAdDisplaysSt() {
        var showCount = mmkvSt.decodeInt(Constant.SHOW_ST_COUNT, 0)
        showCount++
        MmkvUtils.set(Constant.SHOW_ST_COUNT, showCount)
    }

    /**
     * 记录广告点击次数
     */
    fun recordNumberOfAdClickSt() {
        var clicksCount = mmkvSt.decodeInt(Constant.CLICKS_ST_COUNT, 0)
        clicksCount++
        MmkvUtils.set(Constant.CLICKS_ST_COUNT, clicksCount)
    }


}