package com.simplemobiletools.notes.pro.adapters

import android.util.TypedValue
import android.view.View
import android.view.ViewGroup
import com.simplemobiletools.commons.activities.BaseSimpleActivity
import com.simplemobiletools.commons.adapters.MyRecyclerViewAdapter
import com.simplemobiletools.commons.extensions.applyColorFilter
import com.simplemobiletools.commons.views.MyRecyclerView
import com.simplemobiletools.notes.pro.R
import com.simplemobiletools.notes.pro.databinding.ItemCounterBinding
import com.simplemobiletools.notes.pro.extensions.config
import com.simplemobiletools.notes.pro.extensions.getPercentageFontSize
import com.simplemobiletools.notes.pro.models.CounterItem

class CounterAdapter(
    activity: BaseSimpleActivity,
    var items: MutableList<CounterItem>,
    recyclerView: MyRecyclerView,
    itemClick: (Any) -> Unit,
    private val plusClick: (CounterItem, Int) -> Unit,
    private val minusClick: (CounterItem, Int) -> Unit
) : MyRecyclerViewAdapter(activity, recyclerView, itemClick) {

    override fun getActionMenuId() = 0

    override fun actionItemPressed(id: Int) {}

    override fun getSelectableItemCount() = itemCount

    override fun getIsItemSelectable(position: Int) = false

    override fun getItemSelectionKey(position: Int) = items.getOrNull(position)?.id

    override fun getItemKeyPosition(key: Int) = items.indexOfFirst { it.id == key }

    override fun onActionModeCreated() {}

    override fun onActionModeDestroyed() {}

    override fun prepareActionMode(menu: android.view.Menu) {}

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return createViewHolder(ItemCounterBinding.inflate(layoutInflater, parent, false).root)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.bindView(item, true, false) { itemView, layoutPosition ->
            setupView(itemView, item, layoutPosition)
        }
        bindViewHolder(holder)
    }

    override fun getItemCount() = items.size

    private fun setupView(view: View, counterItem: CounterItem, position: Int) {
        ItemCounterBinding.bind(view).apply {
            counterTitle.apply {
                text = counterItem.title
                setTextColor(textColor)
                setTextSize(TypedValue.COMPLEX_UNIT_PX, context.getPercentageFontSize())
                gravity = context.config.getTextGravity()
            }

            counterValue.apply {
                text = counterItem.count.toString()
                setTextColor(textColor)
                setTextSize(TypedValue.COMPLEX_UNIT_PX, context.getPercentageFontSize())
            }

            counterPlus.applyColorFilter(textColor)
            counterMinus.applyColorFilter(textColor)

            counterPlus.setOnClickListener { plusClick(counterItem, position) }
            counterMinus.setOnClickListener { minusClick(counterItem, position) }
        }
    }
}
