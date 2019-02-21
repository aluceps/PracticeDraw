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
        setupSeekbar()
        setupRecyclerView()
    }

    private fun setupClick() {
        with(binding.toolbar) {
            undo.setOnClickListener { binding.drawView.undo() }
            redo.setOnClickListener { binding.drawView.redo() }
            reset.setOnClickListener { binding.drawView.reset() }
        }
    }

    private fun setupSeekbar() {
        with(binding.strokeWidth) {
            setOnSliderProgressChangeListener {
                binding.drawView.setStrokeWidth(it * 6)
            }
        }
    }

    private fun setupRecyclerView() {
        with(binding.toolbar.recyclerView) {
            adapter = PalletAdapter().apply {
                setItems(colors)
                setOnClickListener(object : PalletAdapter.OnEventListener {
                    override fun setFocus(view: View, position: Int) {
                        binding.drawView.setColor(colors[position])
                        AnimationHelper.scaleUp(view)
                    }

                    override fun lostFocus(view: View?) {
                        view?.let { AnimationHelper.scaleDown(it) }
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
        private var listener: OnEventListener? = null
        private var currentFocus = -1
        private var previousView: View? = null

        override fun onCreateViewHolder(parent: ViewGroup, index: Int): RecyclerView.ViewHolder =
            PalletHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_pallet, parent, false))

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, index: Int) {
            (holder as PalletHolder).apply {
                setup(items[index])
                setOnClickListener(object : PalletHolder.Listener {
                    override fun click(view: View) {
                        if (currentFocus >= 0 && currentFocus != index) {
                            listener?.lostFocus(previousView)
                        }
                        previousView = view
                        currentFocus = index
                        listener?.setFocus(view, index)
                    }
                })
            }
        }

        override fun getItemCount(): Int = items.size

        fun setItems(items: List<ColorPallet>) {
            items.forEach { this.items.add(it) }
            notifyDataSetChanged()
        }

        fun setOnClickListener(listener: OnEventListener) {
            this.listener = listener
        }

        interface OnEventListener {
            fun setFocus(view: View, position: Int)
            fun lostFocus(view: View?)
        }
    }

    class PalletHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val binding = ItemPalletBinding.bind(itemView)!!
        private var listener: Listener? = null

        fun setup(pallet: ColorPallet) {
            with(binding) {
                inner.setTint(pallet)
                outer.setTint(ColorPallet.White)
                executePendingBindings()
            }
            itemView.setOnClickListener {
                listener?.click(it)
            }
        }

        fun setOnClickListener(listener: Listener) {
            this.listener = listener
        }

        interface Listener {
            fun click(view: View)
        }

        private fun View.setTint(pallet: ColorPallet) {
            background.setTint(getColor(pallet))
        }

        @SuppressLint("ResourceType")
        private fun View.getColor(pallet: ColorPallet): Int =
            ResourcesCompat.getColor(resources, pallet.resId, null)
    }
}
