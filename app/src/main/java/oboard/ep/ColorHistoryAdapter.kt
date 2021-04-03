package oboard.ep

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import oboard.ep.MainActivity.Companion.bottomSheetBinding

class ColorHistoryAdapter(val context: Context, var data: List<Int>) :
    RecyclerView.Adapter<ColorHistoryAdapter.VH>() {

    //内部类 继承
    inner class VH(v: View) : RecyclerView.ViewHolder(v) {
        val view: View = v.findViewById(R.id.ch_view)
    }

    //在该方法中创建ViewHolder
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val view = LayoutInflater.from(context).inflate(R.layout.color_history_item, parent, false)
        val vh = VH(view)
        vh.view.setOnClickListener {
            //拿到用户点击的位置
            val position = vh.adapterPosition
            val fruit = MainActivity.currentColor
            MainActivity.changeColor(MainActivity.data[position])
            MainActivity.currentColor = data[position]
            MainActivity.data[position] = fruit
            MainActivity.adapter.notifyItemChanged(position)
        }
        vh.view.setOnLongClickListener {
            //拿到用户点击的位置
            val position = vh.adapterPosition
            MainActivity.data.remove(position)
            MainActivity.adapter.notifyItemRemoved(position)
            true
        }
        return vh
    }
    override fun getItemCount(): Int {
        return data.size
    }
    //在该方法中数据绑定
    override fun onBindViewHolder(holder: VH, position: Int) {
        val fruit = data[position]
        val circleDrawable = CircleDrawable(holder.view.layoutParams.width, holder.view.layoutParams.height)
        circleDrawable.setColor(fruit)
        holder.view.background = circleDrawable
    }
}