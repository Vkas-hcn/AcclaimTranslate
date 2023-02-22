package com.vkas.secondtranslation.ui.language

import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.vkas.secondtranslation.R
import com.vkas.secondtranslation.stbean.Language
import com.vkas.secondtranslation.utils.AcclaimUtils
import java.util.*

class LanguageRecentlyAdapter(data: MutableList<Language>?) :
    BaseQuickAdapter<Language, BaseViewHolder>(
        R.layout.item_language,
        data
    ) {
    override fun convert(holder: BaseViewHolder, item: Language) {
        holder.setText(R.id.tv_country_name, Locale(item.code).displayLanguage)
        holder.setTextColor(R.id.tv_country_name, context.getColor(R.color.tv_chek_language))
        AcclaimUtils.langIconMap[item.code]?.let { holder.setImageResource(R.id.img_flag, it) }
        holder.setVisible(R.id.img_down_state, false)
    }
}