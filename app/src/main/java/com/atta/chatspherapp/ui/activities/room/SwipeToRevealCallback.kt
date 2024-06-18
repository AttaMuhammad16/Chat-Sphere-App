package com.atta.chatspherapp.ui.activities.room

import android.animation.ValueAnimator
import android.graphics.Canvas
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.atta.chatspherapp.models.MessageModel
import com.atta.chatspherapp.adapters.ChatAdapter

class SwipeToRevealCallback(
    private val adapter: ChatAdapter,
    private val onSwipeAction: (MessageModel) -> Unit
) : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {

    override fun onMove(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        target: RecyclerView.ViewHolder
    ): Boolean {
        return false
    }

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
        val position = viewHolder.adapterPosition
        val message = adapter.mylist[position]
        onSwipeAction(message)
        adapter.notifyItemChanged(position)
    }

    override fun onChildDraw(
        c: Canvas,
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        dX: Float,
        dY: Float,
        actionState: Int,
        isCurrentlyActive: Boolean
    ) {
        val maxSwipeDistance = recyclerView.width / 3
        if (Math.abs(dX) >= maxSwipeDistance) {
            val position = viewHolder.adapterPosition
            if (position != RecyclerView.NO_POSITION) {
                val message = adapter.mylist[position]
                onSwipeAction(message)
                animateViewReset(viewHolder)
            }
        } else {
            getDefaultUIUtil().onDraw(c, recyclerView, viewHolder.itemView, dX, dY, actionState, isCurrentlyActive)
        }
    }

    private fun animateViewReset(viewHolder: RecyclerView.ViewHolder) {
        val itemView = viewHolder.itemView
        val animator = ValueAnimator.ofFloat(itemView.translationX, 0f)
        animator.addUpdateListener { animation ->
            itemView.translationX = animation.animatedValue as Float
        }
        animator.duration = 50
        animator.start()
    }
}

