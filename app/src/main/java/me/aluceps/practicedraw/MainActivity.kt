package me.aluceps.practicedraw

import android.annotation.SuppressLint
import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.v4.content.res.ResourcesCompat
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import me.aluceps.practicedraw.databinding.ActivityMainBinding
import me.aluceps.practicedraw.databinding.ItemPalletBinding

class MainActivity : AppCompatActivity() {

    private val binding by lazy {
        DataBindingUtil.setContentView<ActivityMainBinding>(this, R.layout.activity_main)
    }

    private val colors by lazy {
        ColorPallet.values().toList()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupClick()
        setupRecyclerView()
    }

    private fun setupClick() {
        with(binding.toolbar) {
            undo.setOnClickListener { binding.drawView.redo() }
            redo.setOnClickListener {}
            reset.setOnClickListener { binding.drawView.reset() }
        }
    }

    private fun setupRecyclerView() {
        with(binding.toolbar.recyclerView) {
            adapter = PalletAdapter().apply {
                setItems(colors)
                setOnClickListener(object : PalletAdapter.OnClickListener {
                    override fun click(position: Int) {
                        binding.drawView.color(colors[position])
                    }
                })
            }
            layoutManager = LinearLayoutManager(context).apply {
                orientation = LinearLayoutManager.HORIZONTAL
            }
            isMotionEventSplittingEnabled = false
        }
    }

    class PalletAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
        private val items: MutableList<ColorPallet> = mutableListOf()
        private var listener: OnClickListener? = null

        override fun onCreateViewHolder(p0: ViewGroup, p1: Int): RecyclerView.ViewHolder =
            PalletHolder(LayoutInflater.from(p0.context).inflate(R.layout.item_pallet, p0, false))

        override fun getItemCount(): Int =
            items.size

        override fun onBindViewHolder(p0: RecyclerView.ViewHolder, p1: Int) {
            (p0 as PalletHolder).apply {
                setup(items[p1])
                setOnClickListener(object : PalletHolder.OnClickListener {
                    override fun click(view: View) {
                        AnimationHelper.setAnimation(view)
                        listener?.click(p1)
                    }
                })
            }
        }

        fun setItems(items: List<ColorPallet>) {
            items.forEach { this.items.add(it) }
        }

        fun setOnClickListener(listener: OnClickListener) {
            this.listener = listener
        }

        interface OnClickListener {
            fun click(position: Int)
        }
    }

    class PalletHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val binding = ItemPalletBinding.bind(itemView)!!
        private var listener: OnClickListener? = null

        fun setup(pallet: ColorPallet) {
            with(binding) {
                inner.setTint(pallet)
                outer.setTint(ColorPallet.White)
                executePendingBindings()
            }
            itemView.setOnClickListener { listener?.click(it) }
        }

        fun setOnClickListener(listener: OnClickListener) {
            this.listener = listener
        }

        interface OnClickListener {
            fun click(view: View)
        }

        @SuppressLint("ResourceType")
        private fun View.setTint(pallet: ColorPallet) {
            background.setTint(ResourcesCompat.getColor(resources, pallet.resId, null))
        }
    }
}
