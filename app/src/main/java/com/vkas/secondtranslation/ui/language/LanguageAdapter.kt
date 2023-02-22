package com.vkas.secondtranslation.ui.language

import android.view.View
import android.widget.LinearLayout
import androidx.recyclerview.widget.RecyclerView
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.google.mlkit.nl.translate.TranslateLanguage
import com.vkas.secondtranslation.R
import com.vkas.secondtranslation.stbean.Language
import com.vkas.secondtranslation.utils.AcclaimUtils
import com.vkas.secondtranslation.utils.KLog
import java.util.*

class LanguageAdapter (data: MutableList<Language>?) :
    BaseQuickAdapter<Language, BaseViewHolder>(
        R.layout.item_language,
        data
    ) {
    override fun convert(holder: BaseViewHolder, item: Language) {
        holder.setText(R.id.tv_country_name, Locale(item.code).displayLanguage)
        AcclaimUtils.langIconMap[item.code]?.let { holder.setImageResource(R.id.img_flag, it) }
        setVisibility(item.searchForMatches,holder.itemView)
        if(item.isCheck){
            holder.setTextColor(R.id.tv_country_name,context.getColor(R.color.tv_chek_language))
        }else{
            holder.setTextColor(R.id.tv_country_name,context.getColor(R.color.white))
        }
        when(item.downloadStatus){
            0->{
                holder.setVisible(R.id.img_down_state,true)
                holder.setVisible(R.id.pro_down_state,false)
                holder.setImageResource(R.id.img_down_state,R.drawable.ic_download)
            }
            1->{
                holder.setVisible(R.id.img_down_state,false)
                holder.setVisible(R.id.pro_down_state,true)
            }
            2->{
                holder.setVisible(R.id.img_down_state,true)
                holder.setVisible(R.id.pro_down_state,false)
                holder.setImageResource(R.id.img_down_state,R.drawable.ic_lan_delete)
            }
        }
        if(item.code == TranslateLanguage.ENGLISH){
            holder.setVisible(R.id.img_down_state,false)
            holder.setVisible(R.id.pro_down_state,false)
        }

    }
    private fun setVisibility(isVisible: Boolean, itemView: View) {
        val param = itemView.layoutParams as RecyclerView.LayoutParams
        if (isVisible) {
            param.height = LinearLayout.LayoutParams.WRAP_CONTENT
            param.width = LinearLayout.LayoutParams.MATCH_PARENT
            itemView.visibility = View.VISIBLE
        } else {
            itemView.visibility = View.GONE
            param.height = 0
            param.width = 0
        }
        itemView.layoutParams = param
    }
}