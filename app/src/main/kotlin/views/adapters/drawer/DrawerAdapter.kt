package com.michalfaber.drawertemplate.views.adapters.drawer

import android.support.v7.widget.RecyclerView
import android.view.ViewGroup
import com.michalfaber.drawertemplate.MainApplication
import com.michalfaber.drawertemplate.R
import com.michalfaber.drawertemplate.views.adapters.AdapterItem
import com.michalfaber.drawertemplate.views.adapters.AdapterItemsSupervisor
import com.michalfaber.drawertemplate.views.adapters.ViewHolderProvider
import javax.inject.Inject

/**
 *  Adapter handles multiple view types. Type of view is determined by the hashCode of the specific ViewHolder class
 *
 */
public class DrawerAdapter(val adapterItems: List<AdapterItem>) : RecyclerView.Adapter<RecyclerView.ViewHolder>(), AdapterItemsSupervisor<AdapterItem> {
    private val items: MutableList<AdapterItem> = adapterItems.toCollection(arrayListOf<AdapterItem>())
    private var selectedId: Long? = null

    var viewHolderProvider: ViewHolderProvider? = null
        [Inject] set

    init {
        // TODO: Create separate component
        MainApplication.graph.inject(this)

        /*
            Register functions which will be used to create fresh instances of specific View Holder and layout
         */

        viewHolderProvider!!.registerViewHolderFactory(ViewHolderMedium::class, R.layout.drawer_item_medium, { drawerItemView ->
            ViewHolderMedium(drawerItemView)
        })

        viewHolderProvider!!.registerViewHolderFactory(ViewHolderSmall::class, R.layout.drawer_item_small, { drawerItemView ->
            ViewHolderSmall(drawerItemView)
        })

        viewHolderProvider!!.registerViewHolderFactory(ViewHolderSeparator::class, R.layout.drawer_item_separator, { drawerItemView ->
            ViewHolderSeparator(drawerItemView)
        })

        viewHolderProvider!!.registerViewHolderFactory(ViewHolderHeader::class, R.layout.drawer_item_header, { drawerItemView ->
            ViewHolderHeader(drawerItemView)
        })

        viewHolderProvider!!.registerViewHolderFactory(ViewHolderSpinner::class, R.layout.drawer_item_spinner, { drawerItemView ->
            ViewHolderSpinner(drawerItemView, this)
        })

        viewHolderProvider!!.registerViewHolderFactory(ViewHolderSpinnerItem::class, R.layout.drawer_item_spinner_item, { drawerItemView ->
            ViewHolderSpinnerItem(drawerItemView, this)
        })
    }

    public fun getItemIdAt(adapterPosition: Int): Long? {
        return if (adapterPosition >= 0 && adapterPosition < items.size()) items[adapterPosition].id else null
    }

    public fun select(id: Long) {
        if (id != selectedId) {
            items.filter { it.id == id && it.selectable == true }
                    .take(1)
                    .forEach {
                        unselectPreviousAdapterItem()
                        selectAdapterItem(it)
                    }
        }
    }

    private fun selectAdapterItem(adapterItem: AdapterItem) {
        selectedId = adapterItem.id
        notifyItemChanged(indexOfItem(adapterItem));
    }

    private fun unselectPreviousAdapterItem() {
        val prevSelectedItemIdx = items.indexOfFirst { it.id == selectedId }
        if (prevSelectedItemIdx >= 0) {
            notifyItemChanged(prevSelectedItemIdx);
        }
    }

    override fun removeItems(startsFrom: Int, subItems: List<AdapterItem>) {
        items.removeAll(subItems)
        notifyItemRangeRemoved(startsFrom, subItems.size())
    }

    override fun addItems(startsFrom: Int, subItems: List<AdapterItem>) {
        items.addAll(startsFrom, subItems)
        notifyItemRangeInserted(startsFrom, subItems.size())
    }

    override fun indexOfItem(item: AdapterItem): Int {
        return items.lastIndexOf(item)
    }

    override fun swapItem(index: Int, item: AdapterItem) {
        items[index] = item
        notifyItemChanged(index)
    }

    override fun getItemViewType(position: Int): Int {
        return items[position].viewType
    }

    override fun getItemCount(): Int {
        return items.size()
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): RecyclerView.ViewHolder? {
        return viewHolderProvider!!.provideViewHolder(viewGroup, viewType)
    }

    override fun onBindViewHolder(viewHolder: RecyclerView.ViewHolder, position: Int) {
        items[position].bindViewHolder(viewHolder)
        viewHolder.itemView.setActivated(items[position].id == selectedId)
    }

    override fun getItemId(position: Int): Long {
        return items[position].id
    }
}
