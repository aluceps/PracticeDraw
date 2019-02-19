package me.aluceps.practicedraw

import android.annotation.SuppressLint
import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.annotation.IdRes
import android.support.v4.content.res.ResourcesCompat
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import me.aluceps.practicedraw.databinding.ActivityMainBinding
import me.aluceps.practicedraw.databinding.ItemPalletBinding

class MainActivity : AppCompatActivity() {

    private val binding by lazy {
        DataBindingUtil.setContentView<ActivityMainBinding>(this, R.layout.activity_main)
    }

    private val colors = listOf(
        R.color.colorTypeRed,
        R.color.colorTypeOrange,
        R.color.colorTypeYellow,
        R.color.colorTypeGreen,
        R.color.colorTypePurple,
        R.color.colorTypeBlue,
        R.color.colorTypeLightBlue,
        R.color.colorTypeBlack,
        R.color.colorTypeWhite
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupClick()
        setupRecyclerView()
    }

    private fun setupClick() {
        with(binding.toolbar) {
            undo.setOnClickListener { }
            redo.setOnClickListener { }
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
        private val items: MutableList<Int> = mutableListOf()
        private var listener: OnClickListener? = null

        override fun onCreateViewHolder(p0: ViewGroup, p1: Int): RecyclerView.ViewHolder =
            PalletHolder(LayoutInflater.from(p0.context).inflate(R.layout.item_pallet, p0, false))

        override fun getItemCount(): Int =
            items.size

        override fun onBindViewHolder(p0: RecyclerView.ViewHolder, p1: Int) {
            (p0 as PalletHolder).apply {
                setup(items[p1])
                setOnClickListener(object : PalletHolder.OnClickLisnter {
                    override fun click(view: View) {
                        Log.d("MainActivity", "click")
                        AnimationHelper.setAnimation(view)
                        listener?.click(p1)
                    }
                })
            }
        }

        fun setItems(items: List<Int>) {
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
        private var listener: OnClickLisnter? = null

        @SuppressLint("ResourceType")
        fun setup(@IdRes resId: Int) {
            with(binding) {
                inner.setTint(resId)
                outer.setTint(android.R.color.white)
                executePendingBindings()
            }
            itemView.setOnClickListener { listener?.click(it) }
        }

        fun setOnClickListener(listener: OnClickLisnter) {
            this.listener = listener
        }

        interface OnClickLisnter {
            fun click(view: View)
        }

        @SuppressLint("ResourceType")
        private fun View.setTint(@IdRes resId: Int) {
            background.setTint(ResourcesCompat.getColor(resources, resId, null))
        }
    }
}
